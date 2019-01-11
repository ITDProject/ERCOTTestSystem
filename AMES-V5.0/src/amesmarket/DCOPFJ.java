/* ============================================================================
 * AMES Wholesale Power Market Test Bed (Java): A Free Open-Source Test-Bed  
 *         for the Agent-based Modeling of Electricity Systems 
 * ============================================================================
 *
 * (C) Copyright 2008, by Hongyan Li, Junjie Sun, and Leigh Tesfatsion
 *
 *    Homepage: http://www.econ.iastate.edu/tesfatsi/AMESMarketHome.htm
 *
 * LICENSING TERMS
 * The AMES Market Package is licensed by the copyright holders (Junjie Sun, 
 * Hongyan Li, and Leigh Tesfatsion) as free open-source software under the       
 * terms of the GNU General Public License (GPL). Anyone who is interested is 
 * allowed to view, modify, and/or improve upon the code used to produce this 
 * package, but any software generated using all or part of this code must be 
 * released as free open-source software in turn. The GNU GPL can be viewed in 
 * its entirety as in the following site: http://www.gnu.org/licenses/gpl.html
 */

package amesmarket;

// DCOPFJ implementation Version 2.0
// DCOPFJ.java
// DC Optimal Power Flow (OPF) solver
//
// DCOPFJ formulates the needed matrices and vectors for QuadProgJ
//
// DC-OPF paper: Junjie Sun and Leigh Tesfatsion, (2006) "DC OPF Formulation
//     and Solution Using QuadProgJ", ISU Econ Working Paper Series #06014

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import java.io.*;
//import quadprogj.*;

/**
 * What's new: this version of DCOPFJ can handle both case of fixed demands
 * (as in the old version) and price-sensitive demands (as in this version). 
 *  
 *      LSE demand  = fixed demand
 *   OR
 *      LSE demand = fixed demand + price-sensitive demand bid function
 * 
 * where fixed demand is simply the hourly load profiles, and the price-sensitive
 * demand bid function for each LSE j is expressed as follows:
 *
 *      D_j(sLoad_j) = c_j - 2 * d_j * sLoad_j
 *
 * where sLoad is the price-sensitive load demand quantity
 *
 */

/**
 * Conform the DC-OPF problem to a strictly convex QP problem.  See DC-OPF paper
 *
 * To form the input matrices/vector for QuadProgJ, we need the following internal
 * variable names for two cases: fixed demand and price-sensitive demand:
 *=======================================================================
 * CASE 1: FIXED DEMAND
 *-------------------------------
 * G:     U, Wrr
 * a:     A_1...A_I
 * Ceq:   II, rBusAdm
 * beq:   FDemand
 * Ciq:   Oni, Z, rAdj, Iii, Oik
 * biq:   pU, capL, capU
 * ------------------------------
 * B:       supplyOffer[][1]; cost coefficient b; as appeared in matrix U
 * A:       supplyOffer[][0]; cost coefficient a
 * Wrr:     grid.getReducedVADWeight() 
 * II:      someFunction(atNodeByGen)
 * rBusAdm: grid.getReducedBusAdmittance()
 * FDemand: someFunction(atNodeByLSE, loadProfile[]) ~ Section 4.3 in DC-OPF paper
 * Oni:     NxI zero matrix
 * Z:       grid.getDiagonalAdmittance() 
 * rAdj:    grid.getReducedAdjacency() 
 * Iii:     IxI identity matrix
 * Oik:     Ix(K-1) zero matrix
 * pU:      grid.getLineCap()
 * capL:    supplyOffer[][2]; capL; GenCo's lower operating capacity limit
 * capU:    supplyOffer[][3]; capU; GenCo's upper operating capacity limit
 *=======================================================================
 * CASE 2: PRICE-SENSITIVE DEMAND
 *-------------------------------
 * G:     U, Wrr
 * a:     A_1...A_I, C_1...C_J
 * Ceq:   II, JJ, rBusAdm
 * beq:   FDemand
 * Ciq:   Oni, Onj, Z, rAdj; Iii, Oij, Oik; Oji, Ijj, Ojk
 * biq:   pU, capL, capU, sLoadL, sLoadU
 * ------------------------------
 * U:       B_1...B_I, D_1...D_J
 * B:       supplyOffer[][1]; cost coefficient b; as appeared in matrix U
 * D:       psDemandBid[][1]; demand coefficient d; as appeared in matrix U
 * A:       supplyOffer[][0]; cost coefficient a
 * C:       psDemandBid[][0]; demand coefficient c
 * Wrr:     grid.getReducedVADWeight() 
 * II:      someFunction(atNodeByGen)
 * JJ:      someFunction(atNodeByLSE)
 * rBusAdm: grid.getReducedBusAdmittance()
 * FDemand: someFunction(atNodeByLSE, loadProfile[]) ~ Section 4.3 in DC-OPF paper
 * Oni:     NxI zero matrix
 * Onj:     NxJ zero matrix
 * Z:       grid.getDiagonalAdmittance() 
 * rAdj:    grid.getReducedAdjacency() 
 * Iii:     IxI identity matrix
 * Oij:     IxJ zero matrix
 * Oik:     Ix(K-1) zero matrix
 * Oji:     JxI zero matrix
 * Ijj:     JxJ identity matrix
 * Ojk:     JxK zero matrix
 * pU:      grid.getLineCap()
 * capL:    supplyOffer[][2]; capL; GenCo's lower operating capacity limit
 * capU:    supplyOffer[][3]; capU; GenCo's upper operating capacity limit
 * sLoadL:  psDemandBid[][2]; sLoadL; LSE's price-sensitive lower load limit
 * sLoadU:  psDemandBid[][3]; sLoadU; LSE's price-sensitive upper load limit
 */
public class DCOPFJ {

  // Index for supplyOffer parameters, i.e., in the form of {a,b,capL,capU}
  private static final int A_INDEX    = 0;
  private static final int B_INDEX    = 1;
  private static final int CAP_LOWER  = 2;
  private static final int CAP_UPPER  = 3;

  // Index for psDemandBid parameters, i.e., in the form of {c,d,sLoadL,sLoadU}
  private static final int C_INDEX   = 0;
  private static final int D_INDEX   = 1;
  private static final int DEMAND_MAX    = 2;
  private static final int SLOAD_UPPER    = 2;

  private DoubleFactory1D fac1d = DoubleFactory1D.dense;
  private DoubleFactory2D fac2d = DoubleFactory2D.dense;
    // for using Colt's methods e.g. diagonal(), identity(), etc.
  private cern.jet.math.Functions F = cern.jet.math.Functions.functions;
    // F: Naming shortcut to save some keystrokes for calling Colt's functions

  // Input data from BSCUC
  private DoubleMatrix2D supplyOffer;
  private DoubleMatrix2D psDemandBid; //psDemandBid = price-sensitive demand bid
  private double[] loadProfile; // loadProfile = fixed demand
  private int[] loadHybridFlag; // loadHybridFlag = 0 -> use loadProfile, 1 -> use price-sensitive demand bid
  private int iLoadProfile;
  private int iPriceSensitiveDemand;
  private int[] atNodeByGen;
  private int[] atNodeByLSE;
  private TransGrid grid;

  // Intermediate input to form (G,a,Ceq,beq,Ciq,biq)
  private int K;               // numNodes
  private int N;               // numBranches
  private int I;               // numGenAgents
  private int J;               // numLSEAgents
  private boolean check;
  private DoubleMatrix1D B;    // Ix1
  private DoubleMatrix2D U;    // Ix1
  private DoubleMatrix2D Wrr;  // (K-1)x(K-1)
  private DoubleMatrix1D A;    // Ix1
  private DoubleMatrix2D II;  // KxI
  private DoubleMatrix2D rBusAdm;   // (K-1)xK
  private DoubleMatrix1D FDemand;  // Kx1
  private DoubleMatrix2D Oni;   // NxI
  private DoubleMatrix2D Z;    // NxN
  private DoubleMatrix2D rAdj; // Nx(K-1)
  private DoubleMatrix2D Iii;   // IxI
  private DoubleMatrix2D Oik;   // Ix(K-1)
  private DoubleMatrix1D pU;   // Nx1
  private DoubleMatrix1D capL;  // Ix1
  private DoubleMatrix1D capU;  // Ix1
  //Below are the additional variables for the price-sensitive demand case
  private DoubleMatrix1D D; // Jx1
  private DoubleMatrix1D C; // Jx1
  private DoubleMatrix2D JJ; // KxJ 
  private DoubleMatrix2D Onj; // NxJ
  private DoubleMatrix2D Oij; // IxJ
  private DoubleMatrix2D Oji; // JxI
  private DoubleMatrix2D Ijj; // JxJ
  private DoubleMatrix2D Ojk; // JxK
  private DoubleMatrix1D sLoadL; // Jx1
  private DoubleMatrix1D sLoadU; // Jx1
  
  // Input for QuadProgJ
  private DoubleMatrix2D G;
  private DoubleMatrix1D a;
  private DoubleMatrix2D Ceq;
  private DoubleMatrix1D beq;
  private DoubleMatrix2D Ciq;
  private DoubleMatrix1D biq;

  private QuadProgJ qpj;

  // Solution from QuadProgJ
  private double[] commitment; // power production quantity
  private double[] voltAngle;  // voltage angle in radians
  private double[] voltAngleDegree;  // voltage angle in degrees
  private double[] lmp;  // lmp (locational marginal prices) == eqMultiplier
  private double[] ineqMultiplier;
  private String [] ineqMultiplierName;
  private double minTVC; // minTVC = SUM (Ai*PGi + Bi*PGi^2)
  private double[] branchFlow; // branchFlow_km = (1/x_km)(delta_k - delta_m)
  private double[][] bi; // branch index
  private double sumSquaredAngleDifference;  // SUM(delta_k - delta_m)^2
  private double[] sLoad; //price-sensitive load demand quantity


  
  // Constructor for hybrid demand
  public DCOPFJ(double[][] so, double[][] db, double[] lp, int [] hf, int[] ng, int[] nl,
      TransGrid tg,int h,boolean checktemp){

    loadHybridFlag = hf; // Jx1; hybrid demand flag
    atNodeByGen = ng;  // Ix1; GenCo location on the grid
    atNodeByLSE = nl;  // Jx1; LSE location on the grid
    grid        = tg;  // Grid data
    check=checktemp;
    K = grid.getNumNodes();
    N = grid.getNumBranches();
    I = atNodeByGen.length;
    J = atNodeByLSE.length;


      if(h==0)
      {
      for(int i=0; i<so.length; i++){
          // Convert A from SI to PU-adjusted
      so[i][A_INDEX] = so[i][A_INDEX]*INIT.getBaseS();

      // Convert B from SI to PU-adjusted
      so[i][B_INDEX] = so[i][B_INDEX]*INIT.getBaseS()*INIT.getBaseS();

      // Convert CapMin from SI to PU
      so[i][CAP_LOWER] = so[i][CAP_LOWER]/INIT.getBaseS();

      // Convert CapMax from SI to PU
      so[i][CAP_UPPER] = so[i][CAP_UPPER]/INIT.getBaseS();

    }
      so = Support.correctRoundingError(so);
      }

    supplyOffer = new DenseDoubleMatrix2D(so);// Ix4; supply offer

    // SI to PU conversion for price sensitive demand

    for(int i=0; i<db.length; i++){
              // Convert C from SI to PU-adjusted
              db[i][C_INDEX] = db[i][C_INDEX]*INIT.getBaseS();

              // Convert D from SI to PU-adjusted
              db[i][D_INDEX] = db[i][D_INDEX]*INIT.getBaseS()*INIT.getBaseS();

              // Convert DemandMax from SI to PU
              db[i][DEMAND_MAX] = db[i][DEMAND_MAX]/INIT.getBaseS();
        }

    db = Support.correctRoundingError(db);

    psDemandBid = new DenseDoubleMatrix2D(db);// Jx4; price-sensitive demand bid

       
   
         

     for(int j=0; j<J; j++){
            // Convert hourly LP from SI to PU
            lp[j] = lp[j]/INIT.getBaseS();
          }

    lp = Support.correctRoundingError(lp);

     loadProfile = lp;  // Jx1; fixed demand
    iLoadProfile=0;
    iPriceSensitiveDemand=0;
    for(int i=0; i<J; i++){
        if((loadHybridFlag[i]&1)==1)
            iLoadProfile++;
       
        if((loadHybridFlag[i]&2)==2)
            iPriceSensitiveDemand++;
    }  

    //System.out.println("PS Flag = "+iPriceSensitiveDemand);
    solveDCOPF( );

  }
  
  
  private void solveDCOPF(){
    formG();
    forma();
    formCeq();
    formbeq();
    formCiq();
    formbiq();

    /*if(check)
    {
        FileOutputStream out;
        PrintStream p;
        try
        {
        out = new FileOutputStream("DATA/Matrices.xls");
        p = new PrintStream( out );
        p.println("G Matrix");
        for (int i=0;i<G.rows();i++)
        {
            for(int j=0;j<G.columns();j++)
            {
                p.print(G.get(i,j)+"\t");
            }
        p.print("\n");
        }
        p.println("a Matrix");
        for (int i=0;i<a.size();i++)
        {
             p.print(a.get(i)+"\t");
        }
        p.print("\n");

        p.println("Ceq Matrix");
        for (int i=0;i<Ceq.rows();i++)
        {
            for(int j=0;j<Ceq.columns();j++)
            {
                p.print(Ceq.get(i,j)+"\t");
            }
        p.print("\n");
        }
        p.println("Beq Matrix");
        for (int i=0;i<beq.size();i++)
        {
             p.print(beq.get(i)+"\t");
        }
        p.print("\n");
        p.println("Ciq Matrix");
        for (int i=0;i<Ciq.rows();i++)
        {
            for(int j=0;j<Ciq.columns();j++)
            {
                p.print(Ciq.get(i,j)+"\t");
            }
        p.print("\n");
        }
        p.println("Biq Matrix");
        for (int i=0;i<biq.size();i++)
        {
             p.print(biq.get(i)+"\t");
        }
        p.print("\n");
        p.close();
        }

        catch(Exception e)
        {

        }
    }*/
/*    
    System.out.println("G: " + G);
    System.out.println("a: " + a);
    System.out.println("Ceq': " + Ceq.viewDice());
    System.out.println("beq: " + beq);
    System.out.println("Ciq': " + Ciq.viewDice());
    System.out.println("biq: " + biq); 
*/
    
    qpj = new QuadProgJ(G,a,Ceq,beq,Ciq,biq);
    boolean bHaveSolution=qpj.getIsFeasibleAndOptimal();
    
    commitment = new double[I];   // in MWs
    voltAngle = new double[K-1];  // in radians
    voltAngleDegree = new double[K-1]; // in degress
    lmp        = new double[K];
    ineqMultiplier = new double[2*N+2*I+2*iPriceSensitiveDemand];
    minTVC = 0;  // in $/h
    branchFlow = new double[N]; //in MWs
    bi = new double[N][2]; // columns are FROM and TO; rows are branches
    bi = grid.getBranchIndex();
    double[] fullVoltAngle = new double[K]; // including delta_1 = 0
    sumSquaredAngleDifference = 0;
    

    // NOTE FOR THE SOLUTION STRUCTURE x* = qpj.getMinX()
    // x* = (p_{G1}...p_{GI}, delta_2...delta_K) for fixed demand
    // x* = (p_{G1}...p_{GI}, p_{L1}^S...p_{LJ}^S, delta_2...delta_K) 
    //      for price-sensitive demand
    
    sLoad = new double[iPriceSensitiveDemand];
    
    if(bHaveSolution) { // QuadProgJ has a solution
        // DC-OPF solution for (p_{G1},...,p_{GI}) in SI
        for(int i=0; i<I; i++){
          commitment[i] = qpj.getMinX()[i]*INIT.getBaseS();
        }
          // DC-OPF solution for (p_{L1}^S,...,p_{LJ}^S) in SI
          for(int j=I; j<I+iPriceSensitiveDemand; j++){
            sLoad[j-I] = qpj.getMinX()[j]*INIT.getBaseS();
          }
          // DC-OPF solution for (delta_2,...,delta_K)
          for(int k=I+iPriceSensitiveDemand; k<I+iPriceSensitiveDemand+K-1; k++){
            voltAngle[k-I-iPriceSensitiveDemand] = qpj.getMinX()[k];  // voltAngle in radians
          }   

        // Convert voltage angle from radian to degree
        for(int k=1; k<K-1; k++){
          voltAngleDegree[k] = (voltAngle[k]*180)/Math.PI; // volt angle in degrees
        }

        // lmp: locational marginal prices in SI
        for(int k=0; k<K; k++){
          lmp[k] = qpj.getEqMultipliers()[k]/INIT.getBaseS();
        }
        
        for(int j=0; j<2*N+2*I+2*iPriceSensitiveDemand; j++){
          ineqMultiplier[j] = qpj.getIneqMultipiers()[j]/INIT.getBaseS();
        }
        for(int i=0; i<I; i++){
          minTVC = minTVC + (A.get(i)/INIT.getBaseS())*commitment[i]
          +(B.get(i)/(INIT.getBaseS()*INIT.getBaseS()))*commitment[i]*commitment[i];
        }

        for(int k=1; k<K; k++){
          fullVoltAngle[k] = voltAngle[k-1];
        }
        //System.out.println("fullVoltAngle");
        //Support.print(fullVoltAngle,4);
        for(int n=0; n<N; n++){
          //System.out.println("1/x_km: " +(1/grid.getReactance()[n]));
          //System.out.println("k,m: " + bi[n][0] + "," + bi[n][1]);
          //System.out.println("delta_k: " + fullVoltAngle[(int)bi[n][0]-1]);
          //System.out.println("delta_m: " + fullVoltAngle[(int)bi[n][1]-1]);
          branchFlow[n] = (1/grid.getReactance()[n])*(fullVoltAngle[(int)bi[n][0]-1]
                          - fullVoltAngle[(int)bi[n][1]-1])*INIT.getBaseS();

          sumSquaredAngleDifference = sumSquaredAngleDifference
              + Math.pow((fullVoltAngle[(int)bi[n][0]-1]
                          - fullVoltAngle[(int)bi[n][1]-1]),2);
        }
    }
  }

  // G = blockDiag(U,Wr), where U = diag(2B) or U = diag(2B, 2D)
  private void formG(){
    B = new DenseDoubleMatrix1D(supplyOffer.viewColumn(B_INDEX).toArray());
    if (iPriceSensitiveDemand<1){
      U = new DenseDoubleMatrix2D(fac2d.diagonal(B.assign(F.mult(2))).toArray());
      G = new DenseDoubleMatrix2D(I+K-1,I+K-1);
    }
    else {
      double [] dSensitiveDemand = new double [iPriceSensitiveDemand];  
      int iIndex=0;
      for(int i=0; i<J; i++){
          if((loadHybridFlag[i]&2)==2)
              dSensitiveDemand[iIndex++]=psDemandBid.get(i, D_INDEX);
      }  
      
      D = new DenseDoubleMatrix1D(dSensitiveDemand);
      U = new DenseDoubleMatrix2D
         (fac2d.diagonal(fac1d.append(B,D).assign(F.mult(2))).toArray());
      G = new DenseDoubleMatrix2D(I+iPriceSensitiveDemand+K-1,I+iPriceSensitiveDemand+K-1);
    }
    Wrr = new DenseDoubleMatrix2D(grid.getReducedVADWeight());
    G.assign(fac2d.composeDiagonal(U,Wrr));

  }

  // a = (A, 0...0) or a = (A,-C,0...0)
  private void forma(){
    A = new DenseDoubleMatrix1D(supplyOffer.viewColumn(A_INDEX).toArray());
    if (iPriceSensitiveDemand<1){
      a = new DenseDoubleMatrix1D(I+K-1);
      a.viewPart(0,I).assign(A); // viewPart(fromIndex, width)
    }
    else{
      double [] dSensitiveDemand = new double [iPriceSensitiveDemand];  
      int iIndex=0;
      for(int i=0; i<J; i++){
          if((loadHybridFlag[i]&2)==2)
              dSensitiveDemand[iIndex++]=psDemandBid.get(i, C_INDEX);
      }  
      
      C = new DenseDoubleMatrix1D(dSensitiveDemand);
      a = new DenseDoubleMatrix1D(I+iPriceSensitiveDemand+K-1);
      a.viewPart(0,I).assign(A);
      a.viewPart(I,iPriceSensitiveDemand).assign(C.assign(F.neg));
    }
  }
  
  // CeqTranspose = (II, -Br'); Ceq = CeqTranspose'; where Br' is rBusAdm here
  private void formCeq(){
    II = new DenseDoubleMatrix2D(K,I);
      for(int k=0; k<K; k++){
      for(int i=0; i<I; i++){
        if(atNodeByGen[i]==k+1){
          II.set(k,i,1);  // change set to setQuick() later
        }
      }
    }
    rBusAdm = new DenseDoubleMatrix2D(grid.getReducedBusAdmittance());
    
    if (iPriceSensitiveDemand<1){
      DoubleMatrix2D[][] parts = {{ II, rBusAdm.viewDice().assign(F.neg)}};
      DoubleMatrix2D CeqTranspose = new DenseDoubleMatrix2D(K,I+K-1);
      CeqTranspose.assign(fac2d.compose(parts));
      Ceq = new DenseDoubleMatrix2D(I+K-1,K);
      Ceq.assign(CeqTranspose.viewDice());
    }
    else{
      JJ = new DenseDoubleMatrix2D(K,iPriceSensitiveDemand);
      int iCount=0;
      for(int k=0; k<K; k++){
        for(int j=0; j<J; j++){
          if((atNodeByLSE[j]==k+1)&&((loadHybridFlag[j]&2)==2)) {
            JJ.set(k,iCount,1);  // change set to setQuick() later
            iCount++;
          }
        }
      }
     DoubleMatrix2D[][] parts = {{ II, JJ.assign(F.neg),
         rBusAdm.viewDice().assign(F.neg)}};
     DoubleMatrix2D CeqTranspose = new DenseDoubleMatrix2D(K,I+iPriceSensitiveDemand+K-1);
     CeqTranspose.assign(fac2d.compose(parts));
     Ceq = new DenseDoubleMatrix2D(I+iPriceSensitiveDemand+K-1,K);
     Ceq.assign(CeqTranspose.viewDice());          
    }
  }
  // FDemand = someFunction(atNodeByLSE, loadProfile); beq = FDemand
  private void formbeq(){
    if (iPriceSensitiveDemand<1){
        FDemand = new DenseDoubleMatrix1D(K);
        for(int k=0; k<K; k++){
          double lp=0;
          for(int j=0; j<J; j++){
            if(atNodeByLSE[j]==k+1){
              lp = lp + loadProfile[j];
            }
          }
          FDemand.set(k,lp);
        }
        beq = new DenseDoubleMatrix1D(K);
        beq.assign(FDemand);
    }
    else{
        FDemand = new DenseDoubleMatrix1D(K);
        for(int k=0; k<K; k++){
          double lp=0;
          for(int j=0; j<J; j++){
            if((atNodeByLSE[j]==k+1)&&((loadHybridFlag[j]&1)==1)){
              lp = lp + loadProfile[j];
            }
          }
          FDemand.set(k,lp);
        }
        beq = new DenseDoubleMatrix1D(K);
        beq.assign(FDemand);
    }
  }
  
  // Ciq matrix formulation
  //
  // (1) FIXED DEMAND CASE:
  // CiqTranspose = {{Oni, Z*rAdj},{Oni, -Z*rAdj},{Iii, Oik},{-Iii, Oik}};
  // Ciq = CiqTranspose'
  //
  // (2) PRICE-SENSITIVE DEMAND CASE:
  // CiqTranspose = {{MatrixT}, {MatrixG}, {MatrixL}};
  // MatrixT = {{Oni, Onj, Z*rAdj}, {Oni, Onj, -Z*rAdj}};
  // MatrixG = {{Iii, Oij, Oik},{-Iii, Oij, Oik}};
  // MatrixL = {{Oji, Ijj, Ojk},{Oji, -Ijj, Ojk}};
  private void formCiq(){
    Oni   = new DenseDoubleMatrix2D(N,I);
    Z    = new DenseDoubleMatrix2D(grid.getDiagonalAdmittance());
    rAdj = new DenseDoubleMatrix2D(grid.getReducedAdjacency());
    Iii = new DenseDoubleMatrix2D(I,I).assign(fac2d.identity(I));
    Oik = new DenseDoubleMatrix2D(I,K-1);
    
    if (iPriceSensitiveDemand<1){
      DoubleMatrix2D[][] parts = {
      { Oni,                      Z.zMult(rAdj,null)                     },
      { Oni,                      Z.copy().assign(F.neg).zMult(rAdj,null)},
      { Iii,                      Oik                                    },
      { Iii.copy().assign(F.neg), Oik                                    }};
      DoubleMatrix2D CiqTranspose = new DenseDoubleMatrix2D(2*N+2*I,I+K-1);
      CiqTranspose.assign(fac2d.compose(parts));
      Ciq = new DenseDoubleMatrix2D(I+K-1,2*N+2*I);
      Ciq.assign(CiqTranspose.viewDice()); 
    }
    else {
      Onj = new DenseDoubleMatrix2D(N,iPriceSensitiveDemand);
      Oij = new DenseDoubleMatrix2D(I,iPriceSensitiveDemand);
      Oji = new DenseDoubleMatrix2D(iPriceSensitiveDemand,I);
      Ijj = new DenseDoubleMatrix2D(iPriceSensitiveDemand,iPriceSensitiveDemand).assign(fac2d.identity(iPriceSensitiveDemand));
      Ojk = new DenseDoubleMatrix2D(iPriceSensitiveDemand,K-1);
      DoubleMatrix2D[][] parts = {
        { Oni,                      Onj, Z.zMult(rAdj,null)},
        { Oni,                      Onj, Z.copy().assign(F.neg).zMult(rAdj,null)},
        { Iii,                      Oij, Oik},
        { Iii.copy().assign(F.neg), Oij, Oik},
        { Oji,                      Ijj, Ojk},
        { Oji,                      Ijj.copy().assign(F.neg), Ojk}};
      DoubleMatrix2D CiqTranspose = new DenseDoubleMatrix2D(2*N+2*I+2*iPriceSensitiveDemand,I+iPriceSensitiveDemand+K-1);
      CiqTranspose.assign(fac2d.compose(parts));
      Ciq = new DenseDoubleMatrix2D(I+iPriceSensitiveDemand+K-1,2*N+2*I+2*iPriceSensitiveDemand);
      Ciq.assign(CiqTranspose.viewDice());       
    }
  }
  
  // biq = (-pU, -pU, capL, -capU) or biq = (-pU, -pU, capL, -capU, sLoadL, -sLoadU)
  private void formbiq(){
    pU = new DenseDoubleMatrix1D(grid.getLineCap());
    capL = new DenseDoubleMatrix1D(supplyOffer.viewColumn(CAP_LOWER).toArray());
    capU = new DenseDoubleMatrix1D(supplyOffer.viewColumn(CAP_UPPER).toArray());

    if (iPriceSensitiveDemand<1){
      DoubleMatrix1D[] parts = {pU.copy().assign(F.neg), 
                pU.copy().assign(F.neg), capL, capU.copy().assign(F.neg)};
      biq = new DenseDoubleMatrix1D(2*N+2*I);
      biq.assign(fac1d.make(parts));
      
      ineqMultiplierName=new String[2*N+2*I];
      for(int i=0; i<N; i++){
          ineqMultiplierName[i]="-BFlow "+(i+1);
          ineqMultiplierName[N+i]="+BFlow "+(i+1);
      }
      for(int i=0; i<I; i++){
          ineqMultiplierName[2*N+i]="capL "+(i+1);
          ineqMultiplierName[2*N+I+i]="capU "+(i+1);
      }
    }
    else {
      double [] dSensitiveDemandL = new double [iPriceSensitiveDemand];  
      double [] dSensitiveDemandU = new double [iPriceSensitiveDemand];  
      int iIndex=0;
      for(int i=0; i<J; i++){
          if((loadHybridFlag[i]&2)==2) {
              dSensitiveDemandU[iIndex]=psDemandBid.get(i, SLOAD_UPPER);
              iIndex++;
          }
      }  
      
      sLoadL = new DenseDoubleMatrix1D(dSensitiveDemandL);
      sLoadU = new DenseDoubleMatrix1D(dSensitiveDemandU);
      DoubleMatrix1D[] parts = 
        {pU.copy().assign(F.neg), pU.copy().assign(F.neg), 
         capL,                    capU.copy().assign(F.neg),
         sLoadL,                  sLoadU.copy().assign(F.neg)};
      biq = new DenseDoubleMatrix1D(2*N+2*I+2*iPriceSensitiveDemand);
      biq.assign(fac1d.make(parts));
      
      ineqMultiplierName=new String[2*N+2*I+2*iPriceSensitiveDemand];
      for(int i=0; i<N; i++){
          ineqMultiplierName[i]="-BFlow "+(i+1);
          ineqMultiplierName[N+i]="+BFlow "+(i+1);
      }
      for(int i=0; i<I; i++){
          ineqMultiplierName[2*N+i]="capL "+(i+1);
          ineqMultiplierName[2*N+I+i]="capU "+(i+1);
      }
      for(int i=0; i<iPriceSensitiveDemand; i++){
          ineqMultiplierName[2*N+2*I+i]="-PS "+(i+1);
          ineqMultiplierName[2*N+2*I+iPriceSensitiveDemand+i]="+PS "+(i+1);
      }
      
    }
      
  }

  public double[] getCommitment(){
    return commitment;
  }
  public double[] getSLoad(){
      return sLoad;
  }

  public double[] getVoltAngle(){
    return voltAngle;
  }
  public double[] getVoltAngleDegree(){
  return voltAngleDegree;
 }
  public double[] getLMP(){
    return lmp;
  }
  public double[] getIneqMultiplier(){
    return ineqMultiplier;
  }
  public String [] getIneqMultiplierName(){
    return ineqMultiplierName;
  }
  public double getMinTVC(){
    return minTVC;
  }
  public double[] getBranchFlow(){
    return branchFlow;
  }
  public double getSumSquaredAngleDifference(){
    return sumSquaredAngleDifference;
  }
  public int getNumBindingConstraints(){
    return qpj.getNumBC();
  }
  public int[] getActiveSet(){
    return qpj.getActiveSet();
  }
  public boolean getIsSolutionFeasibleAndOptimal(){
    return qpj.getIsFeasibleAndOptimal();
  }





}
