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
// DAMarket.java
// Day-ahead market
package amesmarket;

import java.util.ArrayList;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;

public class DAMarket {

    private double CONVERGED_PROBABILITY;

    // Day-ahead market's data
    private AMESMarket ames;
    private ArrayList genAgentList;
    private ArrayList lseAgentList;
    private ArrayList NDGenAgentList;
    private double[] supplyOffer;
    private double[] loadProfile;
    private double[] NDGProfile;
    private double[][] demandBid;
    private int[] demandHybrid;
    private double[] trueSupplyOffer;
    private double[][] trueDemandBid;
    private double[][] supplyOfferByGen, supplyOffertemp;
    private double[][] GenProfileByNDG;
    private double[][] loadProfileByLSE;
    private double[][][] demandBidByLSE;
    private int[][] demandHybridByLSE;
    private double[][] trueSupplyOfferByGen;
    private double[][][] trueDemandBidByLSE;
    private double[] choiceProbability; // store each gen's learning choice probability

    private int numGenAgents;
    private int numLSEAgents;
    private int numNDGenAgents;
    private int numSupplyOfferParams;
    private int numHoursPerDay;

    // constructor
    public DAMarket(AMESMarket model) {
        ames = model;
        CONVERGED_PROBABILITY = ames.getThresholdProbability();
        genAgentList = ames.getGenAgentList();
        lseAgentList = ames.getLSEAgentList();
        NDGenAgentList = ames.getNDGenAgentList();

        numGenAgents = ames.getNumGenAgents();
        numLSEAgents = ames.getNumLSEAgents();
        numNDGenAgents = ames.getNumNDGAgents();
        numHoursPerDay = ames.getNumHoursPerDay();

        numSupplyOfferParams = 4;

        supplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];
        supplyOffertemp = new double[numGenAgents][numSupplyOfferParams];
        demandBidByLSE = new double[numLSEAgents][24][3];
        trueDemandBidByLSE = new double[numLSEAgents][24][3];
        GenProfileByNDG = new double[numLSEAgents][numHoursPerDay];
        loadProfileByLSE = new double[numNDGenAgents][numHoursPerDay];
        demandHybridByLSE = new int[numLSEAgents][numHoursPerDay];
        trueSupplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];
        choiceProbability = new double[numGenAgents];
    }

    public void submitTrueSupplyOffersAndDemandBids() {
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            trueSupplyOffer = gen.submitTrueSupplyOffer();
            trueSupplyOfferByGen[i] = trueSupplyOffer;
        }
        /*    
    System.out.println("True Supply Offers Reported by GenCos");
    String strTemp=String.format("%1$10s\t%2$15s\t%3$15s\t%4$15s\t%5$15s","GenCo","A","B","CapMin","CapMax");
    System.out.println(strTemp);
    double dGenMinCapacity=0.0;
    double dGenMaxCapacity=0.0;
    for(int i = 0; i<trueSupplyOfferByGen.length; i++){
        dGenMinCapacity+=trueSupplyOfferByGen[i][2];
        dGenMaxCapacity+=trueSupplyOfferByGen[i][3];

        strTemp=String.format("%1$10d", i+1);
        for(int j = 0 ; j<trueSupplyOfferByGen[0].length; j++){
            strTemp+=String.format("\t%1$15.4f",trueSupplyOfferByGen[i][j]);
            }

        System.out.println(strTemp);
    }

    System.out.println();
    System.out.println("The true min total gen capacity is: "+dGenMinCapacity);
    System.out.println("The true max total gen capacity is: "+dGenMaxCapacity);
    System.out.println();
         */
        for (int j = 0; j < numLSEAgents; j++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(j);
            loadProfile = lse.submitTrueLoadProfile();
            loadProfileByLSE[j] = loadProfile;  // Add each load profile from LSE_j to loadProfileByLSE in row j

            trueDemandBid = lse.submitTrueDemandBid();
            trueDemandBidByLSE[j] = trueDemandBid;

            demandHybrid = lse.submitHybridFlag();
            demandHybridByLSE[j] = demandHybrid;
        }
    }

    public void postTrueSupplyOfferAndDemandBids(double[][] dcomm, double[][] dlmp, double[][] dps) {
        DoubleMatrix2D dailyCommitment = new DenseDoubleMatrix2D(dcomm);
        DoubleMatrix2D dayAheadLMP = new DenseDoubleMatrix2D(dlmp);
        double[][] genProfitAndRevenue = new double[numGenAgents][3 + 24 + 24 + 24]; // profit, netEarnings, revenue
        double[][] lseSurplus = new double[numLSEAgents][1 + 24];

        // Set daily commitment and LMPs for each genAgent i, and genAgent i
        // updates its supply offers according to its profit (no learning)
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            gen.setCommitment(dailyCommitment.viewColumn(i).toArray());

            int k = gen.getAtNode() - 1;
            gen.setDayAheadLMP(dayAheadLMP.viewColumn(k).toArray());

            gen.updateProfit();

            genProfitAndRevenue[i][0] = gen.getProfit();
            genProfitAndRevenue[i][1] = gen.getNetEarning();
            genProfitAndRevenue[i][2] = gen.getDailyRevenue();

            double[] hProfit = gen.getHourlyProfit();
            for (int j = 0; j < 24; j++) {
                genProfitAndRevenue[i][3 + j] = hProfit[j];
            }

            double[] hNetEarning = gen.getHourlyNetEarning();
            for (int j = 0; j < 24; j++) {
                genProfitAndRevenue[i][3 + 24 + j] = hNetEarning[j];
            }

            double[] hRevenue = gen.getHourlyRevenue();
            for (int j = 0; j < 24; j++) {
                genProfitAndRevenue[i][3 + 24 + 24 + j] = hRevenue[j];
            }
        }
        // Set daily price-sensitive demand dispatch for each LSEAgent i, 
        // and LSEAgent updates its profit
        double[][] lseDailyPSDispatch = new double[numLSEAgents][24];

        for (int h = 0; h < 24; h++) {
            int psLoadIndex = 0;
            for (int i = 0; i < numLSEAgents; i++) {
                int hourlyLoadHybridFlagByLSE = demandHybridByLSE[i][h];

                if ((hourlyLoadHybridFlagByLSE & 2) == 2) {
                    lseDailyPSDispatch[i][h] = dps[h][psLoadIndex++];
                } else {
                    lseDailyPSDispatch[i][h] = 0.0;
                }
            }
        }

        for (int i = 0; i < numLSEAgents; i++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(i);
            lse.setPSDispatch(lseDailyPSDispatch[i]);

            int k = lse.getAtNode() - 1;
            lse.setDayAheadLMP(dayAheadLMP.viewColumn(k).toArray());

            lse.updateSurplus();

            lseSurplus[i][0] = lse.getSurplus();

            double[] netHourSurplus = lse.getNetHourSurplus();

            for (int j = 0; j < 24; j++) {
                lseSurplus[i][1 + j] = netHourSurplus[j];
            }
        }

        ames.addGenAgentProfitAndNetGainWithTrueCost(genProfitAndRevenue);
        ames.addLSEAgentSurplusWithTrueCost(lseSurplus);
    }

    public void dayAheadOperation(int h, int d, boolean IsFNCS) {
        //System.out.println("\n----------------------------------------------" +
        //                   "-----------------------");
        System.out.println("\nHour " + h + " Day " + d + ": Day-Ahead Market operation.");
        // Each generator updates its supply offer;
        // Each generator submits its supply offer;
        // DAMarket records all supply offers into supplyOfferByGen.

        //With learning
        /*for (int i = 0; i < numGenAgents; i++) {
        GenAgent gen = (GenAgent) genAgentList.get(i);
        gen.chooseNextAction();
        supplyOffer = gen.submitSupplyOffer();
        supplyOfferByGen[i] = supplyOffer; // Add supply offer from GEN_i to supplyOfferByGen in row i
      }*/
        //Without learning
        submitTrueSupplyOffersAndDemandBids();
        supplyOfferByGen = getTrueSupplyOfferByGen();

        //System.out.println("Supply Offers Reported by GenCos");
        String strTemp = String.format("%1$10s\t%2$15s\t%3$15s\t%4$15s\t%5$15s", "GenCo", "aReported", "bReported", "CapMin", "CapMaxReported");
        //System.out.println(strTemp);
        /*
        double dGenMinCapacity = 0.0;
        double dGenMaxCapacity = 0.0;
        for (int i = 0; i < supplyOfferByGen.length; i++) {
            dGenMinCapacity += supplyOfferByGen[i][2];
            dGenMaxCapacity += supplyOfferByGen[i][3];

            strTemp = String.format("%1$10d", i + 1);
            for (int j = 0; j < supplyOfferByGen[0].length; j++) {
                strTemp += String.format("\t%1$15.4f", supplyOfferByGen[i][j]);
            }

            //System.out.println(strTemp);
        }
        */
        
        //System.out.println();
        //System.out.println("The min total gen capacity is: "+dGenMinCapacity);
        //System.out.println("The max total gen capacity is: "+dGenMaxCapacity);
        //System.out.println();
        ames.addGenAgentSupplyOfferByDay(supplyOfferByGen);

        // Each LSE submits its load profile;
        // DAMarket records all load profils into loadProfileList.
        strTemp = String.format("%1$15s", "Hour");
        for (int j = 0; j < numLSEAgents; j++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(j);
            //Rohit - Addl comment - receive fixed demand bid into loadProfile
            loadProfile = lse.submitDAMLoadForecast(d, j, ames.getPsLse(), IsFNCS);
            //System.out.println("DAM"+loadProfile);
            loadProfileByLSE[j] = loadProfile;  // Add each load profile from LSE_j to loadProfileByLSE in row j

            //Rohit - Addl comment - receive price sensitive demand bid into demandBid and then into demandBidbyLSE for each LSE
            demandBid = lse.submitTrueDemandBid();
            //Rohit - New method inside lse to submit demand bid
            demandBid = lse.submitDemandBid(d, j, ames.getPsLse(), IsFNCS);
            demandBidByLSE[j] = demandBid;

            
            //Rohit - Addl comment - receives hybrid flag i.e., 1. fixed 2. PS 3. Hybrid
            demandHybrid = lse.submitHybridFlag();
            demandHybridByLSE[j] = demandHybrid;

            String lseName = String.format("%1$10d", lse.getID());
            strTemp += String.format("\t%1$15s", lseName);
        }

        System.out.println("numNDGenAgents:"+numNDGenAgents);
        for (int j = 0; j < numNDGenAgents; j++) {
            
            NDGenAgent ndg = (NDGenAgent) NDGenAgentList.get(j);
            NDGProfile = ndg.submitDAMForecast(d, j, IsFNCS);
            GenProfileByNDG[j] = NDGProfile;
            
        }
        
        /* Temperory remove output -----------    
      System.out.println("Demand Bids Reported by LSEs - Fixed Loads");
      strTemp+=String.format("\t%1$15s", "TotalFixedLoads");
      System.out.println(strTemp);
      
      for(int i = 0; i<24; i++){
         strTemp=String.format("%1$15s", i);
         double dTotalDemand=0.0;
         for(int j=0; j<numLSEAgents; j++){
             if((demandHybridByLSE[j][i]&1)==1){
                strTemp+=String.format("\t%1$15.2f", loadProfileByLSE[j][i]);
                dTotalDemand+=loadProfileByLSE[j][i];
             }
             else
                strTemp+=String.format("\t%1$15.2f", 0.0);
         }
         
         strTemp+=String.format("\t%1$15.2f", dTotalDemand);
         System.out.println(strTemp);
      }
       
      System.out.println();

      System.out.println("Demand Bids Reported by LSEs - Price-Sensitive Demands");
      strTemp=String.format("%1$10s\t%2$10s\t%3$15s\t%4$15s\t%5$15s","LSE", "Hour", "cReported","dReported","SLMax");
      System.out.println(strTemp);
      
     for(int j=0; j<numLSEAgents; j++){
      LSEAgent lse = (LSEAgent) lseAgentList.get(j);
      String lseName=String.format("%1$10d", lse.getID());

      for( h=0; h<24; h++){
          strTemp=String.format("%1$s", lseName);
          strTemp+=String.format("\t%1$10d\t%2$15.2f\t%3$15.2f\t%4$15.2f", h, demandBidByLSE[j][h][0], demandBidByLSE[j][h][1], demandBidByLSE[j][h][2]);

          System.out.println(strTemp);
      }
     }
 -------------------------------------------------*/
        //  System.out.println();
    }

    public void post(double[][] dcomm, double[][] dlmp, double[][] dps, int flag) {
        DoubleMatrix2D dailyCommitment = new DenseDoubleMatrix2D(dcomm);
        DoubleMatrix2D dayAheadLMP = new DenseDoubleMatrix2D(dlmp);
        double[][] genProfitAndRevenue = new double[numGenAgents][3];
        double[][] genPropAndProb = new double[numGenAgents][3];
        double[][] lseSurplus = new double[numLSEAgents][1];

        // Set daily commitment and LMPs for each genAgent i, and genAgent i
        // updates its supply offers according to its profit (learning)
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            gen.setCommitment(dailyCommitment.viewColumn(i).toArray());

            int k = gen.getAtNode() - 1;
            gen.setDayAheadLMP(dayAheadLMP.viewColumn(k).toArray());

            gen.updateSupplyOffer(flag); // If flag = 1, it's the CE case (no learning)

            genProfitAndRevenue[i][0] = gen.getProfit();
            genProfitAndRevenue[i][1] = gen.getNetEarning();
            genProfitAndRevenue[i][2] = gen.getDailyRevenue();

            genPropAndProb[i][0] = gen.getChoiceID();
            genPropAndProb[i][1] = gen.getChoicePropensity();
            genPropAndProb[i][2] = gen.getChoiceProbability();

            choiceProbability[i] = gen.getChoiceProbability();
        }
        // Set daily price-sensitive demand dispatch for each LSEAgent i, 
        // and LSEAgent updates its profit
        double[][] lseDailyPSDispatch = new double[numLSEAgents][24];

        for (int h = 0; h < 24; h++) {
            int psLoadIndex = 0;
            for (int i = 0; i < numLSEAgents; i++) {
                int hourlyLoadHybridFlagByLSE = demandHybridByLSE[i][h];

                if ((hourlyLoadHybridFlagByLSE & 2) == 2) {
                    lseDailyPSDispatch[i][h] = dps[h][psLoadIndex++];
                } else {
                    lseDailyPSDispatch[i][h] = 0.0;
                }
            }
        }

        for (int i = 0; i < numLSEAgents; i++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(i);
            lse.setPSDispatch(lseDailyPSDispatch[i]);

            int k = lse.getAtNode() - 1;
            lse.setDayAheadLMP(dayAheadLMP.viewColumn(k).toArray());

            lse.updateSurplus();

            lseSurplus[i][0] = lse.getSurplus();
        }

        ames.addGenAgentProfitAndNetGainByDay(genProfitAndRevenue);
        ames.addGenAgentActionPropensityAndProbilityByDay(genPropAndProb);
        ames.addLSEAgentSurplusByDay(lseSurplus);

        checkModelConvergence(choiceProbability);
        checkGenActionProbConvergence();
        checkGenLearningResultConvergence();
    }

    public void checkModelConvergence(double[] choiceProbability) {
        boolean isConverged = true;
        for (int i = 0; i < numGenAgents; i++) {
            if (choiceProbability[i] < CONVERGED_PROBABILITY) {
                isConverged = false;
                break;
            }
        }

        ames.setIsConverged(isConverged);
    }

    public void checkGenActionProbConvergence() {
        boolean isActionProbConverged = true;
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            if (gen.isActionProbabilityCheck()) {
                if (!gen.isActionProbabilityConverge()) {
                    isActionProbConverged = false;
                    break;
                }
            }
        }

        ames.setIsGenActionProbabilityConverged(isActionProbConverged);
    }

    public void checkGenLearningResultConvergence() {
        boolean isLearningResultConverged = true;
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            if (gen.isLearningResultCheck()) {
                if (!gen.isLearningResultConverge()) {
                    isLearningResultConverged = false;
                    break;
                }
            }
        }

        ames.setIsGenLearningResultConverged(isLearningResultConverged);
    }

    public void checkGenDailyNetEarningConvergence() {
        boolean isDailyNetEarningConverged = true;
        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            if (gen.isDailyNetEarningCheck()) {
                if (!gen.isDailyNetEarningConverge()) {
                    isDailyNetEarningConverged = false;
                    break;
                }
            }
        }

        ames.setIsGenDailyNetEarningConverged(isDailyNetEarningConverged);
    }

    public void checkGenLastDayAction() {
        double[][] genActions = new double[numGenAgents][];

        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            genActions[i] = gen.LastDayCheckAction();
        }

        ames.addGenActions(genActions);
    }

    public double[][] getTrueSupplyOfferByGen() {
        return trueSupplyOfferByGen;
    }

    public double[][] getSupplyOfferByGen() {
        return supplyOfferByGen;
    }

    public double[][] getGenProfileByNDG() {
        return GenProfileByNDG;
    }
    
    public double[][] getLoadProfileByLSE() {
        return loadProfileByLSE;
    }

    public double[][][] getTrueDemandBidByLSE() {
        return trueDemandBidByLSE;
    }

    public double[][][] getDemandBidByLSE() {
        return demandBidByLSE;
    }

    public int[][] getDemandHybridByLSE() {
        return demandHybridByLSE;
    }

}
