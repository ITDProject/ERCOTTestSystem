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
// BUC.java
package amesmarket;

import java.io.*;
import java.util.ArrayList;
import java.sql.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import fncs.JNIfncs;

public class BUC {

    // BUC's data
    private boolean a;
    private int K; // numNodes
    private int N; // numBranches
    private int I; // numGenAgents;
    private int J; // numLSEAgents;
    private int M; // numBranches;
    private int H; // numHoursPerDay = # of load profiles for each LSE ;
    private boolean check;

    private File loadProfileFile;

    private double[][] dailyCommitment, dailyRealTimeCommitment, dailyRealTimeBranchFlow;     // daily commitments (24 hours by row)
    private double[][] dailyVoltAngle;      // daily voltage angles (delta)
    private double[][] dailyLMP, dailyRealTimeLMP;            // daily LMPs
    private double[] dailyMinTVC;
    private double[][] dailyBranchFlow;
    private double[][] dailyPriceSensitiveDemand;

    private boolean[] bDCOPFHasSolution;

    private AMESMarket ames;
    private ISO iso;
    private DCOPFJ opf;

    private double[] ineqMultiplier;
    private String[] ineqMultiplierName;

    // Index for supplyOfferByGen parameters, i.e., in the form of {A,B,CapMin,CapMax}
    private static final int A_INDEX = 0;
    private static final int B_INDEX = 1;
    private static final int CAP_MIN = 2;
    private static final int CAP_MAX = 3;
    private double[][] supplyOfferByGen;
    private double[] priceSensitiveDispatchRT;
    private double[][] supplyOfferByGenRT;
    // Index for psDemandBidByLSE parameters, i.e., in the form of {C,D,DemandMax}
    private static final int C_INDEX = 0;
    private static final int D_INDEX = 1;
    private static final int DEMAND_MAX = 2;
    private double[][] psDemandBidByLSE;

    private double[][] dailySLoad = new double[H][J]; // in price-sensitive demand case only

    // constructor
    public BUC(ISO independentSystemOperator, AMESMarket model) {
        ames = model;
        iso = independentSystemOperator;

        K = ames.getNumNodes();
        N = ames.getNumBranches();
        I = ames.getNumGenAgents();
        J = ames.getNumLSEAgents();
        H = ames.NUM_HOURS_PER_DAY;  // H=24
        check = false;
        a = true;

        loadProfileFile = new File("DATA/LoadProfile.dat");
        dailyCommitment = new double[H][I];
        dailyRealTimeCommitment = new double[H][I];
        dailyRealTimeBranchFlow = new double[H][N];
        dailyVoltAngle = new double[H][K];
        dailyLMP = new double[H][K];
        dailyRealTimeLMP = new double[H][K];
        dailyMinTVC = new double[H];
        dailyBranchFlow = new double[H][N];
        dailyPriceSensitiveDemand = new double[H][J];

        bDCOPFHasSolution = new boolean[24];
    }

    /**
     * Solve OPF DC approximation problem by invoking DCOPFJ
     */
    public void solveOPF() {

        supplyOfferByGen = iso.getSupplyOfferByGen();

        // Store supplyOfferByGen to dSupplyOfferByGen for later check
        int iRow = supplyOfferByGen.length;
        int iCol = supplyOfferByGen[0].length;

        double[][] dSupplyOfferByGen = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                dSupplyOfferByGen[i][j] = supplyOfferByGen[i][j];
            }
        }

        double dMinGenCapacity = 0.0;
        double dMaxGenCapacity = 0.0;

        // SI to PU conversion for supply offer and load profile
        for (int i = 0; i < supplyOfferByGen.length; i++) {
            dMinGenCapacity += supplyOfferByGen[i][CAP_MIN];
            dMaxGenCapacity += supplyOfferByGen[i][CAP_MAX];
            //System.out.println("\n supplyOfferByGen-> dMaxGenCapacity "+supplyOfferByGen[i][CAP_MAX]+" for GenCo :"+i);

            /*// Convert A from SI to PU-adjusted
      supplyOfferByGen[i][A_INDEX] = supplyOfferByGen[i][A_INDEX]*INIT.getBaseS();

      // Convert B from SI to PU-adjusted
      supplyOfferByGen[i][B_INDEX] = supplyOfferByGen[i][B_INDEX]*INIT.getBaseS()*INIT.getBaseS();

      // Convert CapMin from SI to PU
      supplyOfferByGen[i][CAP_MIN] = supplyOfferByGen[i][CAP_MIN]/INIT.getBaseS();

      // Convert CapMax from SI to PU
      supplyOfferByGen[i][CAP_MAX] = supplyOfferByGen[i][CAP_MAX]/INIT.getBaseS();*/
        }

        int[] atNodeByGen = new int[I];
        for (int i = 0; i < I; i++) {
            GenAgent gen = (GenAgent) ames.getGenAgentList().get(i);
            atNodeByGen[i] = gen.getAtNode();
        }

        int[] atNodeByLSE = new int[J];
        for (int j = 0; j < J; j++) {
            LSEAgent lse = (LSEAgent) ames.getLSEAgentList().get(j);
            atNodeByLSE[j] = lse.getAtNode();
        }

        //psDemandBidByLSE = Support.correctRoundingError(psDemandBidByLSE);
        double[] dLoad = new double[24]; // Total Demand
        for (int h = 0; h < H; h++) {
            //NOTE: phaseAngle is assumed to be zero at first bus, i.e. phaseAngle[0]=0

            double[] hourlyLoadProfileByLSE = new double[J];
            int[] hourlyLoadHybridFlagByLSE = new int[J];

            dLoad[h] = 0.0;
            for (int j = 0; j < J; j++) {
                hourlyLoadProfileByLSE[j] = iso.getLoadProfileByLSE()[j][h];
                // Calculate total demand
                hourlyLoadHybridFlagByLSE[j] = iso.getDemandHybridByLSE()[j][h];

                if ((hourlyLoadHybridFlagByLSE[j] & 1) == 1) {
                    dLoad[h] += hourlyLoadProfileByLSE[j];
                }

            }

            /*for(int i=0;i<hourlyLoadProfileByLSE.length;i++)
                      System.out.println("LSE Load: "+hourlyLoadProfileByLSE[i]+" MW"+"for hour: "+h);*/
            double[][][] priceSensitiveDemandBidByLSE = iso.getDemandBidByLSE();
            iRow = priceSensitiveDemandBidByLSE.length;
            iCol = priceSensitiveDemandBidByLSE[0][0].length;
            psDemandBidByLSE = new double[iRow][iCol];
            for (int i = 0; i < iRow; i++) {
                for (int j = 0; j < iCol; j++) {
                    psDemandBidByLSE[i][j] = priceSensitiveDemandBidByLSE[i][h][j];
                }
            }

            boolean bCheckMinMaxGenCapacityOK = true;
            if (dMinGenCapacity > dLoad[h]) {
                System.out.println("GenCo total reported lower required operating capacity is greater than total fixed demand at hour " + h + " \n");
                bCheckMinMaxGenCapacityOK = false;
            }

            if (dMaxGenCapacity < dLoad[h]) {
                System.out.println("GenCo total reported upper operating capacity under supply-offer price cap is less than total fixed demand at hour " + h + "\n");
                bCheckMinMaxGenCapacityOK = false;
            }

            if (bCheckMinMaxGenCapacityOK) {
                //System.out.println("Check DAM:"+supplyOfferByGen[0][3]);

                opf = new DCOPFJ(supplyOfferByGen, psDemandBidByLSE, hourlyLoadProfileByLSE, hourlyLoadHybridFlagByLSE,
                        atNodeByGen, atNodeByLSE, ames.getTransGrid(), h, false);
                // System.out.println("Check DAM:"+supplyOfferByGen[0][3]);

                bDCOPFHasSolution[h] = opf.getIsSolutionFeasibleAndOptimal();
                dailyCommitment[h] = opf.getCommitment();
                /*for(int i=0;i<dailyCommitment[h].length;i++)
                      System.out.println("DAM Commit: "+h+" "+dailyCommitment[h][i]);*/

                dailyVoltAngle[h] = opf.getVoltAngle();
                dailyLMP[h] = opf.getLMP();
                /*for(int i=0;i<dailyLMP[h].length;i++)
                      System.out.println("DAM Commit: "+h+" "+dailyLMP[h][i]);*/
                dailyMinTVC[h] = opf.getMinTVC();
                dailyBranchFlow[h] = opf.getBranchFlow();
                dailyPriceSensitiveDemand[h] = opf.getSLoad();
                dailyPriceSensitiveDemand[h] = Support.correctRoundingError(dailyPriceSensitiveDemand[h]);


                /*for(int i=0;i<dailyPriceSensitiveDemand[h].length;i++)
                      System.out.println("DAM PS: "+dailyPriceSensitiveDemand[h][i]+" MW"+" for hour: "+h);
          /*for(int i=0;i<dailyCommitment[h].length;i++)
                      System.out.println("DAM Commit: "+dailyCommitment[h][i]+" MW"+" for hour: "+h);
          for(int i=0;i<dailyLMP[h].length;i++)
                      System.out.println("DAM LMP: "+dailyLMP[h][i]+" $/MWh"+" for hour: "+h);
          for(int i=0;i<dailyBranchFlow[h].length;i++)
                      System.out.println("DAM Branch Flow: "+dailyBranchFlow[h][i]+" MW"+" for hour: "+h);
          for(int i=0;i<opf.getIneqMultiplier().length;i++)
                      System.out.println("DAM Ineq Multiplier: "+opf.getIneqMultiplier()[i]+" for hour: "+h+"Name"+opf.getIneqMultiplierName()[i]);
          //System.out.println(bDCOPFHasSolution[h]);*/
                if (h == 17) {// get inequality multiplier
                    ineqMultiplier = opf.getIneqMultiplier();
                    ineqMultiplierName = opf.getIneqMultiplierName();
                }
            } else {
                bDCOPFHasSolution[h] = false;
                double[] commitment = new double[I];   // in MWs
                double[] voltAngle = new double[K - 1];  // in radians
                double[] lmp = new double[K];
                double[] branchFlow = new double[N]; //in MWs
                double[] psLoad = new double[J]; //in MWs

                dailyCommitment[h] = commitment;

                dailyVoltAngle[h] = voltAngle;
                dailyLMP[h] = lmp;
                dailyMinTVC[h] = 0.0;
                dailyBranchFlow[h] = branchFlow;
                dailyPriceSensitiveDemand[h] = psLoad;
            }
        }

        for (int h = 0; h < H; h++) {
            if (!bDCOPFHasSolution[h]) {
                System.out.println("  At hour " + h + " DCOPF has no solution!");
            }
        }

        /*Temperory remove output ---------------------------------------    
    System.out.println("  Daily (24 Hour) branch flow for each branch");
    String strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<N; i++){
        String lineName="branch"+(i+1);
        strTemp+=String.format("\t%1$15s", lineName);
    }
    System.out.println(strTemp);
    
    for(int i = 0; i<dailyBranchFlow.length; i++){
      System.out.printf("%1$15d", i);
      for(int j = 0 ; j<dailyBranchFlow[0].length; j++){
          System.out.printf("\t%1$15.2f", Support.roundOff(dailyBranchFlow[i][j],2));
        }
        System.out.println();
    }
    System.out.println();
 
    System.out.println("  Daily (24 Hour) LMP for each bus");
    strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<K; i++){
        String nodeName="bus"+(i+1);
        strTemp+=String.format("\t%1$15s", nodeName);
    }
    System.out.println(strTemp);
    for(int i = 0; i<dailyLMP.length; i++){
      System.out.printf("%1$15d", i);
      for(int j = 0 ; j<dailyLMP[0].length; j++){
          System.out.printf("\t%1$15.2f", Support.roundOff(dailyLMP[i][j],2));
        }
        System.out.println();
    }
    System.out.println();
    
    ///
    System.out.println("  (17 Hour) inequality multiplier:");
    strTemp="";
    for(int i = 0; i<ineqMultiplier.length; i++){
        strTemp+=String.format("\t%1$10s", ineqMultiplierName[i]);
    }
    System.out.println(strTemp);
    strTemp="";
    for(int i = 0; i<ineqMultiplier.length; i++){
        strTemp+=String.format("\t%1$10.5f", ineqMultiplier[i]);
    }
    System.out.println(strTemp);
    System.out.println();

    
    System.out.println("  Daily (24 Hour) commitment for each generator");
    strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<I; i++){
        String genName="GenCo"+(i+1);
        strTemp+=String.format("\t%1$15s", genName);
    }
    System.out.println(strTemp);
    for(int i = 0; i<dailyCommitment.length; i++){
      System.out.printf("%1$15d", i);
      for(int j = 0 ; j<dailyCommitment[0].length; j++){
          System.out.printf("\t%1$15.2f", Support.roundOff(dailyCommitment[i][j],2));
        }
        System.out.println();
    }
    System.out.println();

    System.out.println("  Daily (24 Hour) price-sensitive demand dispatch for each LSE");
    strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<J; i++){
        String LSEName="LSE"+(i+1);
        strTemp+=String.format("\t%1$15s", LSEName);
    }
    System.out.println(strTemp);
    for(int h = 0; h<24; h++){
      System.out.printf("%1$15d", h);
      int psLoadIndex=0;
      for(int j=0; j<J; j++){
        int hourlyLoadHybridFlagByLSE;
        hourlyLoadHybridFlagByLSE = iso.getDemandHybridByLSE()[j][h];
        
        if((hourlyLoadHybridFlagByLSE&2)==2){
            System.out.printf("\t%1$15.2f", Support.roundOff(dailyPriceSensitiveDemand[h][psLoadIndex],2));
            psLoadIndex++;
        }
        else
            System.out.printf("\t%1$15.2f", 0.00);
        
      }

      System.out.println();
    }
    System.out.println();
    
    // CHECK TO SEE THAT LMP_k IS AT LEAST AS GREAT AS MC^R_i FOR ANY GENERATOR 
    // AT NODE K WITH A POSITIVE POWER COMMITMENT P_Gi, FOR EACH HOUR H
    supplyOfferByGen = iso.getSupplyOfferByGen();
    System.out.println("  Daily (24 Hour) commitment for each generator with LMPs");
    strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<I; i++){
        String genName="GenCo"+(i+1);
        strTemp+=String.format("\t%1$15s", genName);
        strTemp+=String.format("\t%1$15s", "Res Price");
        strTemp+=String.format("\t%1$15s", "Node LMP");
    }
    System.out.println(strTemp);
    for(int i = 0; i<dailyCommitment.length; i++){
      System.out.printf("%1$15d", i);
      for(int j = 0 ; j<dailyCommitment[0].length; j++){
          System.out.printf("\t%1$15.2f", Support.roundOff(dailyCommitment[i][j],2));
          
          double reportedLMP=dSupplyOfferByGen[j][A_INDEX]+2.0*dSupplyOfferByGen[j][B_INDEX]*dailyCommitment[i][j];
          System.out.printf("\t%1$15.2f", Support.roundOff(reportedLMP,2));
          
          System.out.printf("\t%1$15.2f", Support.roundOff(dailyLMP[i][atNodeByGen[j]-1],2));
       }
        System.out.println();
    }
    System.out.println();

    // CHECK TO SEE THAT LMP_k DOES NOT EXCEED MAX WILLINGNESS TO PAY OF ANY LSEJ 
    // AT NODE K WITH POSITIVE PRICE_SENSITIVE DEMAND, FOR EACH HOUR H = 00,...,23
    System.out.println("  Daily (24 Hour) price-sensitive demand dispatch for each LSE with LMPs");
    strTemp=String.format("%1$15s", "Hour");
    for(int i = 0; i<J; i++){
        String LSEName="LSE"+(i+1);
        strTemp+=String.format("\t%1$15s", LSEName);
        strTemp+=String.format("\t%1$15s", "Res Price");
        strTemp+=String.format("\t%1$15s", "Node LMP");
    }
    System.out.println(strTemp);
    for(int h = 0; h<24; h++){
      System.out.printf("%1$15d", h);
      int psLoadIndex=0;
      for(int j=0; j<J; j++){
        int hourlyLoadHybridFlagByLSE;
        hourlyLoadHybridFlagByLSE = iso.getDemandHybridByLSE()[j][h];
        
        if((hourlyLoadHybridFlagByLSE&2)==2){
            double dPDemand=dailyPriceSensitiveDemand[h][psLoadIndex];
            System.out.printf("\t%1$15.2f", Support.roundOff(dPDemand,2));
            double reportedLMP=priceSensitiveDemandBidByLSE[j][h][C_INDEX]-2.0*priceSensitiveDemandBidByLSE[j][h][D_INDEX]*dPDemand;
            System.out.printf("\t%1$15.2f", Support.roundOff(reportedLMP,2));

            System.out.printf("\t%1$15.2f", Support.roundOff(dailyLMP[h][atNodeByLSE[j]-1],2));
            
            psLoadIndex++;
        }
        else{
            System.out.printf("\t%1$15.2f", 0.00);
            System.out.printf("\t%1$15.2f", 0.00);
            System.out.printf("\t%1$15.2f", 0.00);
        }
          
        
      }

      System.out.println();
    }
    System.out.println();
 //------------------------------------------------------*/
        System.gc();
    }

    //Real time OPF
    public void solveRTOPF(double[][] dc, double[] psd, int hour, int day) {

        FileReader caseFileReader;
        BufferedReader caseBufferReader;
        FileChangeListener fcl = new FileChangeListener();
        /*try
      {
      fcl.startListening();
      }
      catch (Exception ex) {
      }*/

        supplyOfferByGenRT = dc;
        String strTemp = null;
        for (int i = 0; i < supplyOfferByGenRT.length; i++) {
            // Convert A from SI to PU-adjusted
            supplyOfferByGenRT[i][A_INDEX] = supplyOfferByGenRT[i][A_INDEX] / INIT.getBaseS();

            // Convert B from SI to PU-adjusted
            supplyOfferByGenRT[i][B_INDEX] = supplyOfferByGenRT[i][B_INDEX] / (INIT.getBaseS() * INIT.getBaseS());

            // Convert CapMin from SI to PU
            supplyOfferByGenRT[i][CAP_MIN] = supplyOfferByGenRT[i][CAP_MIN] * INIT.getBaseS();

            // Convert CapMax from SI to PU
            supplyOfferByGenRT[i][CAP_MAX] = supplyOfferByGenRT[i][CAP_MAX] * INIT.getBaseS();
        }

        /*try
            {
        PrintWriter printWriter = new PrintWriter(new File("DATA/Check.dat"));
        printWriter.println("Hello Threads!");
        printWriter.close();
            }
            catch (Exception ex){

            }*/
        priceSensitiveDispatchRT = psd;

        // Store supplyOfferByGen to dSupplyOfferByGen for later check
        int iRow = supplyOfferByGenRT.length;
        int iCol = supplyOfferByGenRT[0].length;
        double[][] dSupplyOfferByGen = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                dSupplyOfferByGen[i][j] = supplyOfferByGenRT[i][j];
            }
        }

        double dMinGenCapacity = 0.0;
        double dMaxGenCapacity = 0.0;

        // SI to PU conversion for supply offer and load profile
        for (int i = 0; i < supplyOfferByGenRT.length; i++) {
            dMinGenCapacity += supplyOfferByGenRT[i][CAP_MIN];
            dMaxGenCapacity += supplyOfferByGenRT[i][CAP_MAX];
            //System.out.println("\n supplyOfferByGen-> dMaxGenCapacity "+supplyOfferByGen[i][CAP_MAX]+" for GenCo :"+i);

        }

        int[] atNodeByGen = new int[I];
        for (int i = 0; i < I; i++) {
            GenAgent gen = (GenAgent) ames.getGenAgentList().get(i);
            atNodeByGen[i] = gen.getAtNode();
        }

        int[] atNodeByLSE = new int[J];
        for (int j = 0; j < J; j++) {
            LSEAgent lse = (LSEAgent) ames.getLSEAgentList().get(j);
            atNodeByLSE[j] = lse.getAtNode();
        }

        double[][][] priceSensitiveDemandBidByLSE = iso.getDemandBidByLSE();
        iRow = priceSensitiveDemandBidByLSE.length;
        iCol = priceSensitiveDemandBidByLSE[0][0].length;
        psDemandBidByLSE = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                psDemandBidByLSE[i][j] = priceSensitiveDemandBidByLSE[i][hour][j];
                if (j == 2) {
                    psDemandBidByLSE[i][j] = 0.0;
                }
            }
        }

        double dLoad; // Total Demand

        double[] hourlyLoadProfileByLSE = new double[J];
        int[] hourlyLoadHybridFlagByLSE = new int[J];

        dLoad = 0.0;

        //receiving load forecast for realtimeLMP calculation
        String[] events = JNIfncs.get_events();
        //System.out.println("RTM events length: " + events.length);
        for (int i = 0; i < events.length; ++i) {
            //String value = JNIfncs.get_value(events[i]);
            String[] values = JNIfncs.get_values(events[i]);

            for (int j = 0; j < J; j++) {
                /*    if (j == 2 && events[i].equals("loadforecastRTM_" + String.valueOf(j + 1))) {
                    System.out.println("receiving loadforecast: " + values[0]);
                    hourlyLoadProfileByLSE[j] = 200 + Double.parseDouble(values[0]);
                } else if(j==1 || j ==3) {
                    hourlyLoadProfileByLSE[j] = 200;
                }
                 */
                if (events[i].equals("loadforecastRTM_" + String.valueOf(j + 1))) {
                    System.out.println("receiving loadforecast: " + values[0]);
                    hourlyLoadProfileByLSE[j] = Double.parseDouble(values[0]);
                    //System.out.println("i:"+i);
                }
                //hourlyLoadProfileByLSE[j] = 200 + Double.parseDouble(values[0]);
                //System.out.println("hourlyLoadProfileByLSE: " + hourlyLoadProfileByLSE[j]);
            }
        }

        //temporary - constant assignment to hourlyloadprofile
        /*
        for (int j = 0; j < J; j++) {
            hourlyLoadProfileByLSE[j] = 250;//ToDo- Double.parseDouble(rs.getString("LSE"+(j+1)));    
            System.out.println("hourlyLoadProfileByLSE: " + hourlyLoadProfileByLSE[j]);
        }
        */
        for (int j = 0; j < J; j++) {
            //hourlyLoadProfileByLSE[j] = iso.getLoadProfileByLSE()[j][hour];
            hourlyLoadHybridFlagByLSE[j] = 1;
            hourlyLoadProfileByLSE[j] += priceSensitiveDispatchRT[j];
            hourlyLoadProfileByLSE[j] = Support.correctRoundingError(hourlyLoadProfileByLSE[j]);
            dLoad += hourlyLoadProfileByLSE[j];
        }

        /*for(int i=0;i<hourlyLoadProfileByLSE.length;i++)
                      System.out.println("LSE RT Load: "+hourlyLoadProfileByLSE[i]+" MW"+" for hour: "+hour);*/
        boolean bCheckMinMaxGenCapacityOK = true;
        if (dMinGenCapacity > dLoad) {
            System.out.println("GenCo total reported lower required operating capacity is greater than total fixed demand at hour " + hour + " \n");
            bCheckMinMaxGenCapacityOK = false;
        }

        if (dMaxGenCapacity < dLoad) {
            System.out.println("GenCo total reported upper operating capacity under supply-offer price cap is less than total fixed demand at hour " + hour + "\n");
            bCheckMinMaxGenCapacityOK = false;
        }

        if (bCheckMinMaxGenCapacityOK) {

            if (hour == 10) {
                check = true;
            } else {
                check = false;
            }

            opf = new DCOPFJ(supplyOfferByGenRT, psDemandBidByLSE, hourlyLoadProfileByLSE, hourlyLoadHybridFlagByLSE,
                    atNodeByGen, atNodeByLSE, ames.getTransGrid(), 0, check);

            boolean check = opf.getIsSolutionFeasibleAndOptimal();
            dailyRealTimeCommitment[hour] = opf.getCommitment();
            dailyRealTimeLMP[hour] = opf.getLMP();
            dailyRealTimeBranchFlow[hour] = opf.getBranchFlow();

            /*for(int i=0;i<dailyRealTimeCommitment[hour].length;i++)
                      System.out.println("RT Commitment: "+dailyRealTimeCommitment[hour][i]+" MW"+" for hour: "+hour);
                  for(int i=0;i<opf.getLMP().length;i++)
                      System.out.println("RT LMP: "+opf.getLMP()[i]+" $/MWh"+" for hour: "+hour);
                  for(int i=0;i<opf.getBranchFlow().length;i++)
                      System.out.println("RT Branch Flow: "+opf.getBranchFlow()[i]+" MW"+" for hour: "+hour);
                  for(int i=0;i<opf.getIneqMultiplier().length;i++)
                      System.out.println("RT Ineq Multiplier: "+opf.getIneqMultiplier()[i]+" for hour: "+hour+"Name"+opf.getIneqMultiplierName()[i]);*/
 /*if(h==17) {// get inequality multiplier
              ineqMultiplier=opf.getIneqMultiplier();
              ineqMultiplierName=opf.getIneqMultiplierName();
          }*/
        } else {

        }

    }
    // dailyCommitment: Hour-by-Node

    public double[][] getDailyBranchFlow() {
        return dailyBranchFlow;
    }

    public double[][] getDailyRealTimeBranchFlow() {
        return dailyRealTimeBranchFlow;
    }
    // dailyCommitment: Hour-by-GenCo

    public double[][] getDailyCommitment() {
        return dailyCommitment;
    }

    public double[][] getDailyRealTimeCommitment() {
        return dailyRealTimeCommitment;
    }
    // dailyPriceSensitiveDemand: Hour-by-LSE

    public double[][] getDailyPriceSensitiveDemand() {
        return dailyPriceSensitiveDemand;
    }
    // dailyPhaseAngle: Hour-by-Node (excluding Node 1)

    public double[][] getDailyVoltAngle() {
        return dailyVoltAngle;
    }
    // dailyLMP: Hour-by-Node

    public double[][] getDailyLMP() {
        return dailyLMP;
    }

    public double[][] getDailyRealTimeLMP() {
        return dailyRealTimeLMP;
    }

    public int[] getHasSolution() {
        int[] hasSolution = new int[H];

        for (int i = 0; i < H; i++) {
            if (bDCOPFHasSolution[i]) {
                hasSolution[i] = 1;
            }
        }

        return hasSolution;
    }

    public class FileChangeListener implements FileListener {

        DefaultFileMonitor fm;
        public final File logFile = new File("DATA/LoadProfile.dat");

        public void startListening() throws FileSystemException {
            final FileSystemManager fsManager = VFS.getManager();
            final FileObject listendir = fsManager.toFileObject(logFile);

            fm = new DefaultFileMonitor(this);
            fm.addFile(listendir);
            fm.start();
        }

        public void fileCreated(FileChangeEvent fce) throws Exception {
            fileChanged(fce);
        }

        public void fileDeleted(FileChangeEvent fce) throws Exception {
            //hmm..why deleted?
        }

        public void fileChanged(FileChangeEvent fce) throws Exception {
            BufferedReader base = new BufferedReader(new FileReader(new File("DATA/LoadProfile.dat")));
            System.out.println("Read Successful");
            base.close();
            //a=false;
            //System.out.println("Exec");
        }
    }

}
