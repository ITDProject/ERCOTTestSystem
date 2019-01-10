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

// QuadProgJ.java

import java.util.ArrayList;
import cern.colt.matrix.*; 
import cern.colt.matrix.impl.*; 
import cern.colt.matrix.linalg.*; 
import cern.jet.math.*; 
import cern.colt.Timer;

/**
 * QuadProgJ implements the dual active set method by Goldfarb and Idnani (GI)
 * to solve a strictly convex quadratic programming (SCQP) problem subject to
 * linear equality and linear inequality constraints.
 *
 * Reference:
 * Goldfarb, D., and A. Idnani (1983), "A Numerically Stable Dual Method for
 * Solving Strictly Convex Quadratic Programs", Mathematical Programming (27)1-33.
 *
 *
 * SCQP Problem setup:
 *
 *         min f(x) = 0.5*x'*G*x + a'*x + a0
 * s.t.
 *         seq(x) = Ceq' * x - beq  = 0 (Equality constraints, meq of them)
 *         siq(x) = Ciq' * x - biq >= 0 (Inequality constraints, miq of them)
 *
 * and let C = [Ceq,Ciq] so that the first meq columns of C is Ceq and
 * the rest miq columns of C is Ciq; and let b = [beq,biq] so that the first meq
 * elements of b is beq and rest miq elements of b is biq. Also let m be the total
 * number of constraints so that m = meq + miq.  In summary:
 *
 *         C = [Ceq,Ciq]
 *         b = [beq',biq']'
 *         m = meq + miq
 *
 * where x and a are n-vectors, G is an nxn symmetric positive definite matrix,
 * Ceq is an n-by-meq matrix, beq is an meq-vector, Ciq is an n-by-miq matrix,
 * and biq is an miq-vector, C is an nxm matrix, b is an m-vector.
 *
 */


public class QuadProgJ {

  private static final double PINF =  1.0E50; // PINF: positive infinite
  private static final double NINF = -1.0E50; // NINF: negative infinite
  private static final double TOL = 1.0E-11; // TOL: tolerance (in correctRoundingError())

  private Timer time = new Timer(); // recording time for getting the solution.
  // NOTE: you may remove Timer if passing tests to improve algorithm speed.

  private int n; // n: number of decision variables, x's
  private int m; // m: number of all constraints (m = meq + miq)
  private int meq; // meq: number of equality constraints
  private int miq; // miq: number of inequality constraints
  private int scp; // scp: strategy of choosing constraint p:
                   //      1 = choose most violated constraint
                   //      2 = choose least violated constraint
                   //      3 = choose first violated constraint
                   //      4 = choose last violated constraint
                   //      5 = choose randomly picked violated constraint
  private DoubleMatrix2D G; // G: nxn
  private DoubleMatrix1D a; // a: nx1
  private DoubleMatrix2D C; // C: nxm,  C = [Ceq Ciq]
  private DoubleMatrix1D b; // b: mx1,  b = [beq' biq']'
  private DoubleMatrix2D Ceq; // Ceq: nxmeq
  private DoubleMatrix1D beq; // beq: meqx1
  private DoubleMatrix2D Ciq; // Ciq: nxmiq
  private DoubleMatrix1D biq; // biq: miqx1
  private double a0; // a scalar possibly appearing in objective function f(x)

  private cern.jet.math.Functions F = cern.jet.math.Functions.functions;
    // F: Naming shortcut to save some keystrokes for calling Colt's functions
  private DoubleFactory1D fac1 = DoubleFactory1D.dense;
  private DoubleFactory2D fac2 = DoubleFactory2D.dense;
  private Algebra alg = new Algebra();
  private DoubleMatrix2D L; // L: Cholesky decomp lower matrix s.t. G = L*L'

  // A is the active set whose elements are the indices of binding constraints,
  //  i.e., A = {i | 0 <= i < m = meq + miq, i-th column in C }
  private int[]  A;  // NOTE: A will always have the indices of first meq columns in C filled up
  private int    q;  // Running number of constraints in A, q<=min(m,n), where m=meq+miq
  private int[]  V;  // Violation set: V={j in K\A | sj(x) < 0}, for inequality constraints only
  private int    p;  // Current violated constraint, including first meq eq constraints
  private int    k;  // Constraint to be dropped, including first meq eq constraints
  private double t;  // Step length, t = min(t1,t2)
  private double t1; // Partial step length
  private double t2; // Full step length
  private boolean isFullStep;
  private boolean isInfeasible = false;
  private boolean isFeasibleAndOptimal = false;
  // NOTE(1): the main loop goes back to Step 1 if isFullStep is true;
  //          otherwise it goes back to Step 2(a)
  // NOTE(2): the main loop of GI's algorithm can only be terminated by either
  //          of the two conditions: solution is found (isFeasibleAndOptimal =
  //          true) or solution does not exist (isInfeasible = true).

  private double f; // f: min objective function evaluated at current iteration
  private DoubleMatrix1D x;     // x: minimizer, nx1
  private DoubleMatrix2D N;     // N: active constraint matrix, nxq => nxm
  private DoubleMatrix2D H;     // H: 'reduced' inverse Hessian operator, nxn
  private DoubleMatrix1D nplus; // nplus: current chosen violated constraint, nx1
  private DoubleMatrix2D Nstar; // Nstar: pseudo-inverse of N, qxn
  private DoubleMatrix1D z; // z: step direction in primal space, nx1
  private DoubleMatrix1D r; // r: negative step direction in dual space,(q-1)x1
  private DoubleMatrix1D u;     // u: Lagrangian multiplier, qx1
  private DoubleMatrix1D uplus; // uplus: transitional Lagrangian multiplier,(q+1)x1
  private DoubleMatrix1D siq;   // siq = Ciq' * x - biq (to get V)

  private int numIter = 0;  // # of total iterations
  private int numAdd  = 0;  // # of ineq. constraints added to active set
  private int numDrop = 0;  // # of ineq. constraints dropped from active set
  private double fBPMPD = NINF; // for comparing optimal f from BPMPD and QuadProgJ

  private String[] step = {
      " Step 0(a) Find the unconstrained minimum ",
      " Step 0(b) Find the equality constrained minimum ",
      " Step 1 Choose a violated constraint ",
      " Step 2(a) Determine step direction ",
      " Step 2(b) Compute step length ",
      " Step 2(c) Determine new S-pair and take step "};

  private String[] strategy = {
      "Choose the most violated constraint (cp = 1) ",
      "Choose the least violated constraint (cp = 2)",
      "Choose the first violated constraint (cp = 3)",
      "Choose the last violated constraint (cp = 4)",
      "Choose the randomly-picked violated constraint (cp = 5)"};

 

  // Constructor used in solving DC-OPF problem
  public QuadProgJ(DoubleMatrix2D GMat,   DoubleMatrix1D aVec,
                   DoubleMatrix2D CeqMat, DoubleMatrix1D beqVec,
                   DoubleMatrix2D CiqMat, DoubleMatrix1D biqVec){
    time.start();

    n   = aVec.size();
    meq = beqVec.size();
    miq = biqVec.size();
    m   = meq + miq;

    G   = new DenseDoubleMatrix2D(n,n).assign(GMat);     // G: nxn
    a   = new DenseDoubleMatrix1D(n).assign(aVec);       // a: nx1
    Ceq = new DenseDoubleMatrix2D(n,meq).assign(CeqMat); // Ceq: nxmeq
    beq = new DenseDoubleMatrix1D(meq).assign(beqVec);   // beq: meqx1
    Ciq = new DenseDoubleMatrix2D(n,miq).assign(CiqMat); // Ciq: nxmiq
    biq = new DenseDoubleMatrix1D(miq).assign(biqVec);   // biq: miqx1

    C = fac2.appendColumns(Ceq,Ciq); // C = [Ceq,Ciq]_(nxm)
    b = fac1.append(beq,biq);        // b = [beq',biq']'_(mx1)

    scp = 1; // choose the most violated constraint at each iteration
    a0  = 0; // scalar term in the objective function
    isInfeasible = false;
    isFeasibleAndOptimal = false;

    L = new CholeskyDecomposition(G).getL();
    // L is the lower triangular matrix of Cholesky decomposition s.t. G = L*L'

    N = new DenseDoubleMatrix2D(n, Math.min(m, n));// N: n x min(m,n)
    // NOTE: N is initialized as a max-sized zero matrix, so later on it is
    //       easier for a constraint to be added or dropped.  The same for A.
    //       So the N or A we are talking about are actually N[1:q] and A[1:q].

    dualActiveSetSolver(); // the 'main' method

    // Correct potential rounding error for final result: x, f and u
    x.assign(correctRoundingError(x));
    f = correctRoundingError(f) + a0;
    if(q>0) u.assign(correctRoundingError(u));

    time.stop();
  }



  public void printInputData(){
    System.out.println("G: " + G + "\n");
    System.out.println("a: " + a + "\n");
    System.out.println("C: " + C + "\n");
    System.out.println("b: " + b + "\n");
    if(meq > 0){
      System.out.println("Ceq: " + Ceq + "\n");
      System.out.println("beq: " + beq + "\n");
    }
    if(miq > 0){
      System.out.println("Ciq: " + Ciq + "\n");
      System.out.println("biq: " + biq + "\n");
      System.out.println("Strategy: " + strategy[scp-1] + "\n");
    }
  }

  public void printOutputSolution(){

    System.out.println("**************************************************");
    System.out.println("\t      Solution (x*,f*,u*)");
    System.out.println("**************************************************");

    if(isFeasibleAndOptimal==true){
      System.out.println("This solution is both feasible and optimal.\n");
    }
    else if(isInfeasible==true){
      System.out.println("This QP problem is infeasible!\n");
    }

    System.out.println("x: " + x + "\n");
    System.out.println("f: " + f + "\n");
    if(q>0) System.out.println("u: " + u + "\n");

    System.out.println("**************************************************");
    System.out.println("\t      Diagnostic Checks");
    System.out.println("**************************************************");

    // ece = equality constraint error = Ceq'*x - beq
    DoubleMatrix1D ece = new DenseDoubleMatrix1D(meq);
    ece = Ceq.copy().viewDice().zMult(x,null).assign(beq,F.minus);
    System.out.println("Equality Constraint Error (ECE): " + ece + "\n");
    // AbsMean(ece)
    System.out.println("AbsMean: "
                       + ece.aggregate(F.plus,F.abs)/ece.size()+ "\n");
    // AbsMax(ece)
    System.out.println("AbsMax: " + ece.aggregate(F.max,F.abs)+ "\n");

    // ice = inequality constraint error = Ciq'*x - biq
    DoubleMatrix1D ice = new DenseDoubleMatrix1D(miq);
    ice = Ciq.copy().viewDice().zMult(x,null).assign(biq,F.minus);
    System.out.println("Inequality Constraint Error (ICE): " + ice + "\n");
    // numVICE(ice): number of Violated Inequality Constraints
    int numVICE = 0;
    for(int i=0; i<ice.size(); i++){
      if(ice.getQuick(i)<-1.0E-9){
        numVICE++;
      }
    }
    System.out.println("numVICE: " + numVICE+ "\n");

    System.out.println("**************************************************");
    System.out.println("     Comparison between QuadProgJ and BPMPD");
    System.out.println("**************************************************");

    if(fBPMPD>NINF){
      //f = Math.rint(f/TOL)*TOL;
      System.out.println("f:      " + f);
      System.out.println("fBPMPD: " + fBPMPD);
      System.out.println("f - fBPMPD: " + (f-fBPMPD));
      System.out.println("(f - fBPMPD)/abs(fBPMPD): "
                         + (f-fBPMPD)/Math.abs(fBPMPD)+"\n");
    }

    System.out.println("**************************************************");
    System.out.println("\t      Misc. Outputs");
    System.out.println("**************************************************");
    System.out.println("Number of decision variables (n): " + n);
    System.out.println("Number of total constraints (m): " + m);
    System.out.println("Number of eq. constraints (meq): " + meq);
    System.out.println("Number of ineq. constraints (miq): " + miq + "\n");

    System.out.println("Binding ineq. constraint indices:");
    System.out.print("  "); printActiveIneq(A);
    System.out.println("Number of binding ineq. constraints (q-meq): " + (q-meq));
    System.out.println("Number of binding total constraints (q): " + q + "\n");

    System.out.println("Strategy of choosing violated constraints: ");
    System.out.println("  " + strategy[scp-1]);
    System.out.println("Number of total iterations: " + numIter);
    System.out.println("Number of ineq. constraints added: " + numAdd);
    System.out.println("Number of ineq. constraints dropped: " + numDrop);

    if(numAdd > 0){
      System.out.println("Efficiency = (numAdd-numDrop)/numAdd: "
           + (double)((numAdd-numDrop)*10000/numAdd)/100 + "%");
    }
    if(time.elapsedTime() > 120){
      System.out.println("Elapsed time: " +  time.minutes() + " minutes");
    }
    else{
      System.out.println("Elapsed time: " +  time.elapsedTime() + " seconds");
    }
    System.out.println("Tolerance: " + TOL);

    //System.out.println("siq: " + siq);

  }

  private void dualActiveSetSolver() {
    findUnconstrainedMin();                        // Step 0(a)
    //System.out.println("Finding the Constrained Solution ... ...\n");
    if(meq > 0){
      findEqualityConstrainedMin_Lagrangian();     // Step 0(b)
    }
    // If miq > 0, add inequality constraints if they get binding (violated)
    
    if(miq > 0){
      chooseViolatedConstraint();       // Step 1
      // The 'main' loop
      while (isFeasibleAndOptimal==false && isInfeasible==false) {
        //System.out.println("Run " + numIter);
        numIter++;
        determineStepDirection();       // Step 2(a)
        computeStepLength();            // Step 2(b)
        determineNewSpairAndTakeStep(); // Step 2(c)
        if(isFullStep == true){
          chooseViolatedConstraint();   // Step 1
        }
      }
      //System.out.println("meq is "+meq);
      //System.out.println("q is "+q);
    }
  }

  private void findUnconstrainedMin(){
    //System.out.println("\nThe Unconstrained Solution: \n");
    x = alg.inverse(G).zMult(a, null).assign(F.neg); // x <- -G^{-1}*a
    //System.out.println("x: " + x + "\n");
    f = 0.5 * a.zDotProduct(x); // f <- 0.5*a'*x
    //System.out.println("f: " + f + "\n");
    H = alg.inverse(G).copy(); // H <- G^{-1}
    A = new int[Math.min(m, n)]; // A <- empty set (all zeros)
    q = 0; // q <- 0
  }

  /** This method uses the direct Lagrangian formulation to derive the equality
   *  constrained minimization solution (Fletcher (1987) p.236-238)
   *
   * Problem:  min  a' * x + 0.5 * x' * G * x
   *     s.t.  Ceq' * x - beq  = 0
   *
   * Linear system of equation:
   *        [G -Ceq ; -Ceq' 0] * [x u]' = [-a -beq]'
   *
   *   or            GCeq * ecSol = abeq
   *
   * Solution:
   *      [x u]' = [G -Ceq ; -Ceq' 0]^{-1} * [-a -beq]'
   *
   *   or          ecSol = GCeq^{-1} * abeq
   *
   * where GCeq  = [G -Ceq ; -Ceq' 0]
   *       abeq  = [-a -beq]'
   *       ecSol = [x u]'
   *
   * Dimension:
   *   GCeq:  (n+meq)x(n+meq)
   *   abeq:  (n+meq)x1
   *   ecSol: (n+meq)x1
   */
  private void findEqualityConstrainedMin_Lagrangian(){

    DoubleMatrix2D GCeq;   // Lagrangian matrix consisting of G and Ceq
    DoubleMatrix1D abeq;   // Lagrangian RHS vector consisting of a and beq
    DoubleMatrix1D ecsol;  // equality constrained solution vector

    DoubleMatrix2D[][] parts ={{G, Ceq.copy().assign(F.neg)},
                              {Ceq.copy().viewDice().assign(F.neg), null}};
    GCeq = fac2.compose(parts);
    abeq = fac1.append(a.copy().assign(F.neg), beq.copy().assign(F.neg));
    ecsol = alg.inverse(GCeq).zMult(abeq, null);

    x = ecsol.viewPart(0,n);
    u = new DenseDoubleMatrix1D(meq).assign(ecsol.viewPart(n,meq));
    f = 0.5*x.zDotProduct(G.zMult(x,null)) + a.zDotProduct(x);
        // f <- 0.5*x'*G*x + a'*x

    // Correct potential rounding error for x, f and u
    x.assign(correctRoundingError(x));
    f = correctRoundingError(f);
    if(q>0) u.assign(correctRoundingError(u));

    for(int i=0; i<meq; i++){
      A[i] = i;  //Add the index of Ceq's column to active set A
    }
    q = meq; // # of binding constraints is the # of eq. constraints for now.
    N.viewPart(0,0,n,meq).assign(Ceq.copy()); // N_active: n x meq
    updateHandNstar();
  }


  private void chooseViolatedConstraint() {

    // siq = Ciq' * x - biq
    siq = Ciq.viewDice().zMult(x, null).copy().assign(biq, F.minus);
    siq.assign(correctRoundingError(siq));
    //System.out.println("siq: " + siq + "\n");
    /*for(int i=0;i<siq.size();i++)
        if(Math.abs(siq.get(i))<1.0e-8)
            siq.set(i,0.0);*/
    //System.out.println("siq: " + siq + "\n");

    int nvc = 0; // number of violated constraints
    for (int j=0; j<siq.size(); j++) {
      if (siq.getQuick(j)<0) {
        nvc++;
      }
    }
    //System.out.println(nvc);
    if(nvc==0){
      isFeasibleAndOptimal = true;
    }
    else{
      V = new int[nvc]; // Violation set: V={j \belong K\A | sj(x) < 0}

      int i = 0;     // Index for setting elements of V
      for(int j=0; j<siq.size(); j++){
        if (siq.getQuick(j)<0){
          V[i] = j;
          i++;
        }
      }
      double temp = siq.getQuick(V[0]);  // for storing temperary smallest s
      switch(scp){
        case 1: // choose p as the most violated constraint (smallest s)
          for(int j=0; j<V.length; j++){
            if(siq.getQuick(V[j]) <= temp){
              temp = siq.getQuick(V[j]);
              p = meq + V[j];
            }
          }
          break;
        case 2: // choose p as the least violated constraint (largest s)
          for(int j=0; j<V.length; j++){
            if(siq.getQuick(V[j]) >= temp){
              temp = siq.getQuick(V[j]);
              p = meq + V[j];
            }
          }
          break;
        case 3: // choose p as the 1st violated constraint in V
          p = meq + V[0];
          break;
        case 4: // choose p as the last violated constraint in V
          p = meq + V[V.length-1];
          break;
        case 5: // choose p as the randomly-picked violated constraint in V
          p = meq + V[(int)((Math.random())*(V.length))];
          break;
        default:// if scp is not matched, the default p is the 1st violated constraint
          p = meq + V[0];
      }
      //System.out.println("p is"+(p+1));
      // NOTE: p includes equality constraints as its first meq elements
      nplus = C.viewColumn(p).copy(); // nplus <- np (nx1)
      //System.out.println("nplus is"+nplus);
      if (q==0){
        uplus = new DenseDoubleMatrix1D(1); // uplus <- 0 if q==0
        u = new DenseDoubleMatrix1D(1); // u <- 0 if q==0
      }
      else {
        uplus = fac1.append(u, new DenseDoubleMatrix1D(1)); // uplus <- (u,0)
      }
    }
  }

  private void determineStepDirection(){

    // Compute step direction in the primal space
      //System.out.println("H is: "+H);
    z = H.zMult(nplus, null); // z = H*nplus
    z.assign(correctRoundingError(z));

    //System.out.println("z is: "+z);

    // Compute the negative of the step direction in the dual space
    if (q > 0) {
      // r = Nstar*nplus
      r = new DenseDoubleMatrix1D(Nstar.rows()).assign(Nstar.zMult(nplus, null));
      r.assign(correctRoundingError(r));
    }
    else{
      // If q==0, r does not exist, and is purposely set to be a zero-size matrix
      //  to accomodate a uniform expression for uplus <- uplus + t*(r 1)' in
      //  Step 2(c) so that it includes the case when q==0, uplus <- uplus + t*1
      r = new DenseDoubleMatrix1D(0);
    }
  }

  private void computeStepLength(){
    computePartialStepLength();  // Step 2(b)-(i)
    computeFullStepLength();     // Step 2(b)-(ii)
    t = Math.min(t1,t2);         // Step 2(b)-(iii) Compute step length, t.
  }

  private void determineNewSpairAndTakeStep(){

    // (i) No step in primal or dual space:
    if(t == PINF){
      isInfeasible = true;
    }

    // (ii) Step in dual space (dual step):
    else if(t2 == PINF){
      isFullStep = false;
      DoubleMatrix1D rplus = fac1.append(
          r.copy().assign(F.neg),new DenseDoubleMatrix1D(1).assign(1));
      uplus.assign(rplus, F.plusMult(t)); // uplus <- uplus + t * rplus
      dropZeroMultiplierCorrespondingToConstraintK();
      //System.out.println("Here1");
      dropConstraintK();
    }

    // (iii) Step in primal and dual space:
    else{
      x.assign(z, F.plusMult(t)); // x <- x + t*z

      f = f + t*z.zDotProduct(nplus)*(0.5*t + uplus.getQuick(uplus.size()-1));
      // f <- f + t*z'*nplus*(0.5t + u(q+1)plus)
      // NOTE: uplus.getQuick(q) is always zero if a full step is taken;
      //       if a partial or dual step is taken, uplus is not zero!

      DoubleMatrix1D rplus = fac1.append(
       r.copy().assign(F.neg),new DenseDoubleMatrix1D(1).assign(1)); //rplus<-(-r 1)'
      uplus.assign(rplus, F.plusMult(t)); // uplus <- uplus + t * rplus

      // Full step
      if(t2 <= t1){// ie, t==t2 but including the case when t1==t2 (t=min(t1,t2))
        isFullStep = true;
        u = new DenseDoubleMatrix1D(uplus.size()).assign(uplus); // u <- uplus
        addConstraintP();
      }
      // Partial step
      else{
        isFullStep = false;
        dropZeroMultiplierCorrespondingToConstraintK();
        //System.out.println("Here2");
        dropConstraintK();
      }
    }
  }


  // Add the constraint p to active set A and update N, H and Nstar.
  private void addConstraintP(){
    //System.out.print(" Add p=" + (p+1) + "\n");
    numAdd++;
    //System.out.println("q: " + q);
    A[q] = p;
    //System.out.println("Add: "+(p+1));
    N.viewColumn(q).assign(C. viewColumn(p).copy());
    q++; // Update the number of binding constraints
    updateHandNstar();

    /** Example: showing how to add constraint p,
     *  Let meq = 3, p = 5 (the 5th constraint in Ciq and 8th constraint in C),
     *
     *  N, A and q before update:
     *        N = {n1,n2,n3,n5,n7}
     *        A = { 1, 2, 3, 5, 7}
     *    index = { 0, 1, 2, 3, 4}
     *        q = 5
     *
     *  N, A and q after update:
     *        N = {n1,n2,n3,n5,n7,n8}
     *        A = { 1, 2, 3, 5, 7, 8}
     *    index = { 0, 1, 2, 3, 4, 5}
     *        q = 6
     *
     * NOTE: to be precise, here N and A are the non-zero (active) part of
     *       actuall N and A, i.e., N is actually N[1:q] and A is actaully A[1:q]
     */
  }


  // Drop the constraint k from active set A and update N, H and Nstar.
  //
  // This method works by first finding the index (i) of A whose value is k, i.e.,
  //  finding i such that A[i] == k.  Then starting from i, replace the current
  //  A and N by the next A and N, i.e., A[j] = A[j+1] for j = i,...,uplus.size()-1.
  //  Lastly, get rid of the last element of uplus by defining a double array,
  //  utemp, which stores the first uplus.size()-1 elements of uplus, and
  //  declare a new instance of DenseDoubleMatrix1D with utemp as its intialization.
  private void dropConstraintK(){
    //System.out.print(" Drop k=" + (k+1) + "\n");
    numDrop++;
    //System.out.println("Drop: "+(k+1));
    q--; // Update the number of binding constraints
    for(int i=meq; i<A.length; i++){
      if(A[i] == k){
        for(int j=i; j<q; j++){  // NOTE: j<q, not j<q-1, b/c q--
          A[j] = A[j+1];
          N.viewColumn(j).assign(N.viewColumn(j+1).copy());
        }
        A[q] = 0; // maybe unnecessary?
        N.viewColumn(q).assign(0);  // maybe unnecessary?
        break;
      }
    }
    updateHandNstar();  // Update H and Nstar

    /** Example: showing how to drop constraint k,
     *  Let meq = 3, k = 8 (the 8th constraint in C and 5th constraint in Ciq),
     *
     *  N and A before update:
     *        N = {n1,n2,n3,n6,n8,n5,n7}
     *        A = { 1, 2, 3, 6, 8, 5, 7}
     *    index = { 0, 1, 2, 3, 4, 5, 6}
     *        q = 7
     *
     *  Here k = 8 = A[4] => i = 4, then staring from index 4, copy the value
     *  in the next cell into this cell for N and A and set the last cell to zero,
     *  i.e., A[4] = A[5] and N[4] = N[5];
     *        A[5] = A[6] and N[5] = N[6];
     *        A[6] = 0    and N[6] = 0;
     *
     *  N and A after update:
     *        N = {n1,n2,n3,n6,n5,n7}
     *        A = { 1, 2, 3, 6, 5, 7}
     *    index = { 0, 1, 2, 3, 4, 5}
     *        q = 6
     *
     * NOTE: to be precise, here N and A are the non-zero (active) part of
     *       actuall N and A, i.e., N is actually N[1:q] and A is actaully A[1:q]
     */
  }

  ////////////////////////////////////////////////////////////////////////
  // Comments for dropZeroMultiplierCorrespondingToConstraintK():
  //
  // This is the most tricky part of GI algorithm. If we are taking either dual
  //  step or partial step, it means the constraint k is about to be dropped,
  //  and the lagrangian multiplier corresponding to constraint k (uplus_k) becomes
  //  zero, which is true by the constructions of t1 and uplus.  To prove this,
  //  t1 <- min{uplus_j / r_j} = uplus_k / r_k  ===> uplus_k - t1*r_k = 0  (1);
  //  since we are taking either dual step (t1<t2=inf) or partial step (t1<t2),
  //  then t = min{t1,t2} = t1  (2);
  //  uplus <- uplus + t*(-r 1)' ===>
  //  (uplus_1,..,uplus_k,..)<-(uplus_1,..,uplus_k,..) + t*(-r_1,..,r_k,.., 1)
  //   ===> then for the kth element of uplus, we have:
  //   ===> uplus_k <- uplus_k - t1*r_k = 0 by (1) and (2)! Q.E.D.
  //
  // The exact reason why uplus_k has to be dropped from uplus before the next
  //  iteration is because in the next iteration constraint k (n_k) will be dropped
  //  from the active constraint matrix N and q is decremented by 1.  Since the
  //  calculation of r depends on Nstar, which in turn depends on N, r will not
  //  depend on n_k anymore.  So it only makes sense to also drop u_k before
  //  attempting to calculate t1 = min{uplus_j / r_j} in the next iteration.
  //  Also in this way the dimension is right for updating uplus<-uplus + t(-r 1)'
  //  in the next iteration.
  //
  // This is apparently implicitly assumed by GI when they carry out the numerical
  //  example in the lower half of Table A.1 (p.30), where u = (0 6)' in iteration 3'
  //  becomes u = (6) in iteration 4', since there is no way to drop zero multiplier
  //  from uplus in the GI algorithm (p.6-7, Goldfarb and Idnani (1983)).
  //
  // This method works by first finding the index (i) of A whose value is k, i.e.,
  //  finding i such that A[i] == k.  Then starting from i, replace the current
  //  uplus by the next uplus, i.e., uplus[j] = uplus[j+1] for j = i,...,uplus.size()-1.
  //  Lastly, get rid of the last element of uplus by defining a double array,
  //  utemp, which stores the first uplus.size()-1 elements of uplus, and
  //  declare a new instance of DenseDoubleMatrix1D with utemp as its intialization.
  private void dropZeroMultiplierCorrespondingToConstraintK(){
    for(int i=meq; i<A.length; i++){
      if(A[i] == k){
        for(int j=i; j<(uplus.size()-1); j++){
          uplus.set(j,uplus.get(j+1));
        }
        double[] utemp = uplus.viewPart(0,uplus.size()-1).toArray();
        uplus = new DenseDoubleMatrix1D(utemp);
        break;
      }
    }
  }


  // Step 2(b)-(i) Compute partial step length, t1:
  private void computePartialStepLength(){
    if(q==0){
      t1 = PINF; // For 1st round, dual step, or partial step
    }
    else{
      // Check if r has positive elements (among the entries correponding to
      //   inequality constraints)
      int npe = 0; // npe: number of postive elements
      for(int i=0; i<q-meq; i++){
        if(r.get(meq+i)>0){
          npe++;
        }
      }
      // If all elements in r are non-positive (i.e., npe == 0), t1<-INF
      if(npe==0){
        t1 = PINF;
      }
      // If r has positive elements, find the partial step length t1, which is
      //  the maximum step in dual space without violating dual feasibility.
      else{
        double tempMin = 0; // temporary min to be stored.
        int ctbd = 0;  // constraint to be dropped, will be set to k at the end.
        t1 = PINF;
        for(int j=0; j<q-meq; j++){
          if(r.get(meq+j)>0){
            tempMin = uplus.get(meq+j)/r.get(meq+j);
            if(tempMin < t1){
              t1 = tempMin;
              ctbd = meq+j;
            }
          }
        }
        k = A[ctbd]; // NOTE: ctbd is equivalent to l (el, not one) in GI step 2(b)(i)
      }
    }
  }

  // Step 2(b)-(ii) Compute full step length, t2:
  private void computeFullStepLength(){
    if (z.zDotProduct(z) == 0.0) {
         t2 = PINF;   // If |z| == 0, set t2 <- INF;
    }
    else {
      double sp = nplus.zDotProduct(x) - b.getQuick(p); // sp = nplus'*x - b[p]
      t2 = -sp / z.zDotProduct(nplus); // Otherwise, set t2 <-sp(x)/(z'*nplus)
    }
  }


  // Use QR decomposition to update H and Nstar in a numerically stable way.
  private void updateHandNstar(){

    // B = L^{-1} * N
    DoubleMatrix2D B = alg.inverse(L).zMult(N.viewPart(0,0,n,q),null);
    // Get economy-sized Q1 and R from Colt's QR decomposition
    QRDecomposition qr = new QRDecomposition(B);
    DoubleMatrix2D Q1 = qr.getQ(); // Q1: nxq
    DoubleMatrix2D R = qr.getR();  //  R: qxq
    DoubleMatrix2D J1 = alg.solve(L.viewDice(),Q1); //J1=L^{-T}*Q1 (nxq)

    // Update H <- J2 * J2' == L^{-T}* Q2*Q2' * L^{-1} (H: nxn),
    //   or equivalently (as in Fletcher(1987) p.238),
    // H = L^{-T} * (I - Q1*Q1') * L^{-1}, or
    // H = L^{-T} * Q2Q2T * L^{-1}, where Q2Q2T = I-Q1*Q1', (Q2*Q2'==I-Q1*Q1')
    // NOTE: by computing H this way, we are avoiding computing Q2 directly.
    //       Getting Q2 requires getting the full-sized Q from QR decomposition
    //       instead of the economy-sized Q1, which is avaible through Colt.

    DoubleMatrix2D Q2Q2T
        = fac2.identity(n).assign(Q1.zMult(Q1.viewDice(),null),F.minus);
    H = alg.inverse(L.viewDice()).zMult(Q2Q2T,null).zMult(alg.inverse(L),null);

    // Update Nstar <- R^{-1}*J1' or R*Nstar = J1' (Nstar: qxn)
    Nstar = new DenseDoubleMatrix2D(q,n).assign(alg.solve(R,J1.viewDice()));
  }

  // correctRoundingError() corrects potential rounding error such as
  //  11.0000000000000005 to be 11.0 or -2.775558E-017 to be 0.0.
  //  Note that this method uses a tolerance value TOL to judge if the target
  //  value needs to be round off. Here TOL is set to be 1.0E-9.
  //
  // This method is essential when condition if(|z| == 0), if(r<=0), and
  //  if(siq <= 0) are evaluated, because rounding error can lead to totally
  //  different result such as if |z| == 0, then t2 is supposed to be INF, but
  //  round error may lead t2 to different value which in turn lead to a full
  //  step instead of dual or partial step, etc.
  //
  // In this program, only 6 variables get this treatment: z, r and siq for
  //  reason given above, and x, f and u for polishing the final solutions.
  private DoubleMatrix1D correctRoundingError(DoubleMatrix1D vector){
    for(int i=0; i<vector.size(); i++){
      if( Math.abs(vector.getQuick(i) - Math.rint(vector.getQuick(i))) < TOL){
        vector.setQuick(i,Math.rint(vector.getQuick(i)));
      }
    }
    return vector;
  }

  private double correctRoundingError(double scalar){
    if( Math.abs(scalar - Math.rint(scalar)) < TOL){
      scalar = Math.rint(scalar);
    }
    return scalar;
  }

  private void print(int[] a){
    for(int i=0; i<a.length; i++){
      System.out.print("  " + a[i]);
    }
    System.out.println();
  }

  private void printActive(int[] a){
    for(int i=0; i<q; i++){
      System.out.print("  " + (a[i]+1));
    }
    System.out.println("\n");
  }
  private void printActiveIneq(int[] a){
    System.out.print("{");
    for(int i=meq; i<q; i++){
      if(i!=q-1) System.out.print((a[i]+1) + ",");
      else System.out.print((a[i]+1));
    }
    System.out.print("}\n");
  }

  /************************* Get and set methods **************************/

  /**
   * Computes and returns nX1 solution vector (x)
   * @return double[]
   */
  public double[] getMinX() {
    return x.toArray();
  }

  public DoubleMatrix1D getMinXdm1d(){
    return x;
  }

  /**
   * Computes and returns the minimized function value (f(x*))
   * @return double
   */
  public double getMinF() {
    return f;
  }

  /**
   * Computes and returns qx1 Lagrangian multiplier vector (u)
   * Note: the first meq elements of u is the Lagrangian multipliers
   *       for equality constraints
   * @return doulbe[]
   */
  public double[] getAllMultipliers() {
    return u.toArray();
  }

  /**
   * Computes and returns meqx1 Lagrangian multiplier vector for equality
   * constraints only
   * @return doulbe[]
   */
  public double[] getEqMultipliers() {
    return u.viewPart(0,meq).toArray();
  }
  /**
   * Computes and returns (q-meq)x1 Lagrangian multiplier vector for binding
   * inequality constraints only
   */
  public double[] getBindingIneqMultipliers() {
    return u.viewPart(meq,q-meq).toArray();
  }

  /**
   * Computes and returns the full miqx1 Lagrangian multiplier vector for both
   * binding and un-binding inequality constraints (the multipliers for un-binding
   * constraints are zeros)
   */
  public double[] getIneqMultipiers(){
    double[] lambdaIneq = new double[miq];
    //System.out.println("q: " + q);
    for(int i=meq; i<q; i++){
     //System.out.println("A[" + i + "]: " + A[i]);
     //System.out.println("u[i]: " + u.get(i));
     lambdaIneq[A[i]-meq] = u.get(i);
    }
    return lambdaIneq;
  }

  /**
   * Returns the number of iterations for solving the current QP problem
   * @return int
   */
  public int getNumIterations() {
    return numIter;
  }

  /**
   * Returns the number of constraints that have been added to active set
   * @return int
   */
  public int getNumConstraintsAdded() {
    return numAdd;
  }

  /**
   * Returns the number of constraints that have been dropped from active set
   * @return int
   */
  public int getNumConstraintsDropped() {
    return numDrop;
  }

  /**
   * Returns the number of total binding constraints in the final active set.
   * @return int
   */
  public int getTotalNumBC() {
    return q;
  }

  /**
   * Returns the number of binding constraints in the final active set.
   * Note: The binding constraints here exclude the equality
   *       constraints, because equality constraints are always binding.
   * @return int
   */
  public int getNumBC() {
    return q - meq;
  }

  /**
   * Returns the original indices of binding constraints as the ith column in
   * the constraint matrix C, including eq. constraints as the first meq
   * elements.
   * @return int[]
   */
  public int[] getActiveSet() {
    int[] Aactive = new int[q];
    for(int i=0; i<q; i++){
      Aactive[i] = A[i];
    }
    return Aactive;
  }




  public boolean getIsFeasibleAndOptimal() {
    return isFeasibleAndOptimal;
  }



}
