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
// ISO.java
// Independent system operator
package amesmarket;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.PSSTSCUC;
import java.util.ArrayList;
import java.sql.*;
import java.text.DecimalFormat;
import fncs.JNIfncs;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISO {

    // ISO's data;
    private int currentMonth;   //to check if it has been a month

    private double[][] supplyOfferByGen;
    private double[][] loadProfileByLSE;
    private double[][] nextDayLoadProfileByLSE;
    private double[][][] demandBidByLSE;
    private int[][] demandHybridByLSE;
    private double[][] supplyOfferByGenRealTime;
    private double[][] committedLoadByLSERealTime;
    private double[][][] demandBidByLSERealTime;
    private int[][] demandHybridByLSERealTime;
    private double[][] dailyPriceSensitiveDispatch, dailyPriceSensitiveDispatchRT;
    private double[][] dailycommitment, dailyrealtimecommitment, dailyrealtimebranchflow, supplyOfferRT;
    private double[][] dailylmp;
    private double[][] dailyrealtimelmp;
    private double[][] dailyBranchFlow;
    private ArrayList commitmentListByDay; // hourly commitments list for each agent by day (d)
    private ArrayList lmpListByDay;        // hourly LMPs list for each bus by day (d)

    private double[][] dailyRTcommitment, dailyRTbranchflow, dailyRTlmp;
    //private double[] dailyRTlmp;
    private double[][] dailyRTProductionCost;
    private double[][] dailyRTStartupCost;
    private double[][] dailyRTShutdownCost;

    private List<CommitmentDecision> genSchedule, genScheduleRT;

    private static final int A_INDEX = 0;
    private static final int B_INDEX = 1;
    private static final int CAP_LOWER = 2;
    private static final int CAP_UPPER = 3;

    private AMESMarket ames;
    private DAMarket dam;
    private RTMarket rtm;
    private PSSTSCUC scuc;
    private SCED sced;
    private BUC buc;
    private int H, I, J, K;

    // constructor
    public ISO(AMESMarket model) {

        //System.out.println("Creating the ISO object: iso \n");
        commitmentListByDay = new ArrayList();
        lmpListByDay = new ArrayList();
        ames = model;
        H = ames.NUM_HOURS_PER_DAY;
        I = ames.getNumGenAgents();
        J = ames.getNumLSEAgents();
        K = ames.getNumNodes();
        supplyOfferRT = new double[I][4];
        dailyPriceSensitiveDispatchRT = new double[H][J];

        dam = new DAMarket(ames);
        rtm = new RTMarket(this, ames);
        buc = new BUC(this, ames);
        this.scuc = new PSSTSCUC(this, this.ames);

        // Temporary initialization
        // nextDayLoadProfileByLSE = new double[J][24];
        // Ignore for now - sced type (should modify BUC Class)
        /*
        String scedProp = System.getProperty("SCED", "pyomo");
        if ("pyomo".equals(scedProp)) {
                System.out.println("Using the external pyomo SCED.");
                // this.sced = new PSSTSCED(this, this.ames);
        } else if ("dcopfj".equals(scedProp)) {
                System.out.println("Using DCOPFJ SCED.");
                this.sced = this.buc;
        } else {
                //TODO-XX Better error message
                throw new IllegalArgumentException("Unknown SCED engine " + scedProp);
        }
         */
    }

    public void computeCompetitiveEquilibriumResults() {
        //System.out.println("Compute competitive equilibrium results before the market is run\n");
        dam.submitTrueSupplyOffersAndDemandBids();
        supplyOfferByGen = dam.getTrueSupplyOfferByGen();
        loadProfileByLSE = dam.getLoadProfileByLSE();
        //this.nextDayLoadProfileByLSE = this.dam.getNextDayLoadProfileByLSE();
        demandBidByLSE = dam.getTrueDemandBidByLSE();
        demandHybridByLSE = dam.getDemandHybridByLSE();

////         Carry out BUC (Bid-based Unit Commitment) problem by solving DC OPF problem
////        System.out.printf("Solving the DC-OPF problem \n");
//        buc.solveOPF();
//        dailycommitment = buc.getDailyCommitment();
//        dailylmp = buc.getDailyLMP();
//        dailyPriceSensitiveDispatch = buc.getDailyPriceSensitiveDemand();
////        NOT update generator's commitment, daily lmp, profit
////        dam.post(dailycommitment,dailylmp,1);
//        ames.addGenAgentCommitmentWithTrueCost(dailycommitment);
//        ames.addLMPWithTrueCost(dailylmp);
//        ames.addLSEAgentPriceSensitiveDemandWithTrueCost(dailyPriceSensitiveDispatch);
//        dam.postTrueSupplyOfferAndDemandBids(dailycommitment, dailylmp, dailyPriceSensitiveDispatch);
    }

    //newversion
    public void marketOperation(int m, int interval, int h, int d) {
        final int tomorrow = d + 1;
        //System.out.println("time_granted: "+ames.getTimeGranted());
        if (h == 1 && m == 0) { // previously h == 0
            dam.dayAheadOperation(h, d);  // fncs.get_events() is called to receive DAM forecast (LSE)

            //newly added
            this.loadProfileByLSE = this.dam.getLoadProfileByLSE();

            //uncommented to ignore rolling horizon for now
            //if(d<this.ames.DAY_MAX) {
            //        this.nextDayLoadProfileByLSE = this.dam.getNextDayLoadProfileByLSE();
            //}
            this.sanityCheck(d);
        }

        if (d != 1 && m % this.ames.M == 0) {
            if (h == 0 && m == 0) {
                System.out.println("Entered iso.RTMOperation at h:" + h + " interval: " + interval + " m: " + m);
                rtm.realTimeOperation(h, d);
            }
            System.out.println("iso.RTMOperation is called at h:" + h + " interval: " + interval + " m: " + m);
            // evaluateRealTimeBidsOffers(h, d); // fncs.get_events() is called to receive RTM forecast (ISO does it in BUC)
            double[][] realtimeload = this.getRealTimeLoad(h, d);

            // Added additionally
            //this.rtm.realTimeOperation(h, d);
            this.rtm.evaluateRealTimeBidsOffers(this.genScheduleRT,
                    realtimeload, m, interval, h, d); // fncs.get_events() is called to receive RTM forecast - inside getRealTimeLoad
            //Temp Fix it - Swathi
            //this.postRealTimeSolutions(d);
            this.dailyrealtimelmp = this.rtm.getRtLMPs(); // check and validate
        }

        if (h == 12 && m == 0) {
            System.out.println("iso.dayAheadOperation is called at h:" + h);
            //evaluateBidsOffers(h, d); //uncommented to add scuc formulation
            System.out.println("SCUC for DAY " + tomorrow + " executing");

            //Exception handling done through suggestion by Netbeans
            try {
                this.scuc.calcSchedule(tomorrow);
            } catch (IOException ex) {
                Logger.getLogger(ISO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AMESMarketException ex) {
                Logger.getLogger(ISO.class.getName()).log(Level.SEVERE, null, ex);
            }

            this.genSchedule = this.scuc.getSchedule();
            this.dailylmp = this.scuc.getDAMLMP();
            this.dailycommitment = this.scuc.getDailyCommitment();
            ames.addGenAgentCommitmentByDay(dailycommitment);
            ames.addLMPByDay(dailylmp);
            // ames.addLSEAgenPriceSensitiveDemandByDay(dailyPriceSensitiveDispatch); // TODO:Swathi
            // ames.addBranchFlowByDay(dailyBranchFlow); // TODO:Swathi
            //ames.addHasSolutionByDay(buc.getHasSolution()); // TODO:Swathi 

        }
        if (h == 17 && m == 0) {
            if (d == 321) {
                int stop = 1;
            }
            // initialPost(h, d);
        }

        //if (h == 18 && m == 0) {
        //    //produceCommitmentSchedule(h,d+1,m);
        //}

        /*
        if (h == 23) {
            forecastLoad();         // Real-time supply over is same as day-ahead supply offer and carry over the price sensitive dispatch to the next day
            if (d != 1) {
                postRealTimeSolutions(h, d);
            }
        }
         */
        //Added additionally
        if ((h == 23) && (d < this.ames.DAY_MAX) && m == 0) {

            ArrayList<GenAgent> genAgentList = this.ames.getGenAgentList();
            int[] tempVector = new int[this.ames.NUM_HOURS_PER_DAY];
            for (int j = 0; j < this.ames.getNumGenAgents(); j++) {
                GenAgent gc = genAgentList.get(j);
                for (int hour = 0; hour < this.ames.NUM_HOURS_PER_DAY; hour++) {
                    tempVector[hour] = this.scuc.getGenDAMCommitmentStatusNextDay()[hour][j];
                }
                gc.setCommitmentStatus(tempVector);
            }

            this.endOfDayCleanup();
            this.postScheduleToGenCos(tomorrow, this.genScheduleRT);
        }
    }

    private double[][] getRealTimeLoad(int h, int d) {

        int ColSize = this.ames.M * this.ames.NUM_INTERVALS_PER_HOUR; // previous usage was this.ames.M * this.ames.NUM_INTERVALS_PER_HOUR

        System.out.println("printing size of RTL array: " + ColSize);
        double[][] hourlyLoadProfileByLSE = new double[J][ColSize];
        double[][] hourlyNDGProfileByBus = new double[J][ColSize];

        //receiving load forecast for realtimeLMP calculation
        String[] events = JNIfncs.get_events();
        //System.out.println("RTM events length: " + events.length);
        for (int i = 0; i < events.length; ++i) {
            //String value = JNIfncs.get_value(events[i]);
            String[] values = JNIfncs.get_values(events[i]);

            for (int j = 0; j < J; j++) {
                if (events[i].equals("loadforecastRTM_" + String.valueOf(j + 1))) {
                    System.out.println("receiving loadforecast: " + values[0]);

                    //Hourly forecast is converted uniformly into per interval forecast
                    for (int k = 0; k < (ColSize); k++) {
                        hourlyLoadProfileByLSE[j][k] = Double.parseDouble(values[0]);
                        //System.out.println("i:"+i);
                    }
                }
                if (events[i].equals("ndgforecastRTM_" + String.valueOf(j + 1))) {
                    System.out.println("receiving ndgforecast: " + values[0]);

                    //Hourly forecast is converted uniformly into per interval forecast
                    for (int k = 0; k < (ColSize); k++) {
                        hourlyNDGProfileByBus[j][k] = Double.parseDouble(values[0]);
                        hourlyLoadProfileByLSE[j][k] = hourlyLoadProfileByLSE[j][k] - hourlyNDGProfileByBus[j][k];
                        //System.out.println("i:"+i);
                    }
                }
            }
        }

        //Updating NetLoadForecast
        /*
        for (int j = 0; j < J; j++) {
            for(int k = 0; k < (this.ames.TAU * this.ames.NUM_INTERVALS_PER_HOUR); k++){
                hourlyLoadProfileByLSE[j][k] = hourlyLoadProfileByLSE[j][k] - hourlyNDGProfileByBus[j][k];
            }
        }
         */
        //temporary - constant assignment to hourlyloadprofile
        /*
        for (int j = 0; j < J; j++) {
            hourlyLoadProfileByLSE[j] = 250;//ToDo- Double.parseDouble(rs.getString("LSE"+(j+1)));    
            System.out.println("hourlyLoadProfileByLSE: " + hourlyLoadProfileByLSE[j]);
        }
         */
        // temp fix
        for (int j = 0; j < J; j++) {
            //System.out.println("Temp RealTime Load: ");
            //Hourly forecast is temperorily set uniformly into per minute forecast
            for (int k = 0; k < (ColSize); k++) {
                hourlyLoadProfileByLSE[j][k] = 126;
                //System.out.println("i:"+i);
            }
        }

        return hourlyLoadProfileByLSE;
    }

    public void endOfDayCleanup() {
        for (int i = 0; i < this.I; i++) {
            for (int j = 0; j < 4; j++) {
                this.supplyOfferRT[i][j] = this.supplyOfferByGen[i][j];
            }
        }
        //Copy the commitment schedule over to the RT datastructures.
        for (int i = 0; i < this.I; i++) {
            this.genScheduleRT = new ArrayList<CommitmentDecision>();
            for (CommitmentDecision cd : this.genSchedule) {
                // clone to keep from overwriting the DA data.
                CommitmentDecision rtcd = new CommitmentDecision(cd);

                GenAgent ga = this.ames.getGenAgentByName(cd.generatorName);

                // Always commit the generator in the realtime market if
                // it is a canary. Work around to ensure enough capacity for any
                // load when a reserve market doesn't exist in the program.
                if (ga.isCanary()) {
                    for (int h = 0; h < rtcd.commitmentDecisions.length; h++) {
                        // Assigning ones throughout the hour interval
                        for (int m = 0; m < (this.ames.NUM_INTERVALS_PER_HOUR * this.ames.M); m++) {
                            rtcd.commitmentDecisions[h][m] = 1;
                        }
                    }
                }

                this.genScheduleRT.add(rtcd);

            }
        }
        for (int i = 0; i < this.H; i++) {
            for (int j = 0; j < this.J; j++) {
                if (this.demandHybridByLSE[j][i] != 1) {
                    //System.out.println(demandHybridByLSE[j][i]);
                    //FIXME: Crashes if non-fixed demand is turned on.
                    this.dailyPriceSensitiveDispatchRT[i][j] = this.dailyPriceSensitiveDispatch[i][j];
                }
            }
        }
    }

    /**
     * Send the commitment decisions to the correct GenAgents.
     *
     * @param day
     * @param commDecision
     */
    private void postScheduleToGenCos(int day, List<CommitmentDecision> commDecision) {
        if (commDecision == null) {
            return; //skip if the commDecision null. Nothing to be done.
        }

        for (CommitmentDecision cd : commDecision) {
            //copy the array to make sure the data doesn't get changed accidentally.
            //int[][] commCopy = new Arrays();[this.ames.NUM_HOURS_PER_DAY][this.ames.TAU * this.ames.NUM_INTERVALS_PER_HOUR];
            //for(int h=0; h<this.ames.NUM_HOURS_PER_DAY; h++){
            //int[][] commCopy = Arrays.copyOf(cd.commitmentDecisions, cd.commitmentDecisions.length);
            //}

            int[][] result = new int[this.ames.NUM_HOURS_PER_DAY][this.ames.M * this.ames.NUM_INTERVALS_PER_HOUR];
            for (int i = 0; i < cd.commitmentDecisions.length; i++) {
                result[i] = Arrays.copyOf(cd.commitmentDecisions[i], cd.commitmentDecisions[i].length);
                // For Java versions prior to Java 6 use the next:
                // System.arraycopy(original[i], 0, result[i], 0, original[i].length);
            }

            this.ames.getGenAgentByName(cd.generatorName).addCommitmentForDay(day, result); //previously commCopy
        }
    }

    public void forecastLoad() {
        int k;
        /*for (int i = 0; i < I; i++) {
            for (int j = 0; j < 4; j++) {
                supplyOfferRT[i][j] = supplyOfferByGen[i][j];
            }
        }*/
        for (int i = 0; i < H; i++) {
            k = 0;
            for (int j = 0; j < J; j++) {
                if (demandHybridByLSE[j][i] == 1) {
                    //System.out.println('h');
                    //System.out.println(J);
                    dailyPriceSensitiveDispatchRT[i][j] = 0;
                    k = k - 1;
                } else {
                    dailyPriceSensitiveDispatchRT[i][j] = dailyPriceSensitiveDispatch[i][k];
                    //System.out.println('I');
                }
                //System.out.println(dailyPriceSensitiveDispatchRT[i][j]);
                //System.out.println(demandHybridByLSE[j][i]);
                k = k + 1;
            }
        }

    }


    /*
    public void postRealTimeSolutions(int h, int d) {
        dailyrealtimecommitment = buc.getDailyRealTimeCommitment();
        dailyrealtimelmp = buc.getDailyRealTimeLMP();
        dailyrealtimebranchflow = buc.getDailyRealTimeBranchFlow();
        ames.addRealTimeLMPByDay(dailyrealtimelmp);
        ames.addGenAgentRealTimeCommitmentByDay(dailyrealtimecommitment);
        ames.addRealTimeBranchFlowByDay(dailyrealtimebranchflow);
    }
     */
    /**
     * Copy the solutions to the interested parties.
     *
     * @param day
     */
    public void postRealTimeSolutions(int day) {
        this.dailyRTcommitment = this.rtm.getRtDispatches(); //buc.getDailyRealTimeCommitment();
        this.postRTDispatchesToGenAgents(day, this.dailyRTcommitment);
        this.dailyRTlmp = this.rtm.getRtLMPs(); //buc.getDailyRealTimeLMP();
        this.dailyRTbranchflow = this.rtm.getRtBranchFlow(); //buc.getDailyRealTimeBranchFlow();

        this.ames.addRealTimeLMPByDay(this.dailyrealtimelmp);//(this.dailyRTlmp);
        this.ames.addGenAgentRealTimeCommitmentByDay(this.dailyRTcommitment);
        this.ames.addRealTimeBranchFlowByDay(this.dailyRTbranchflow);

        this.dailyRTProductionCost = this.rtm.getRtProductionCost();
        this.dailyRTStartupCost = this.rtm.getRtStartupCost();
        this.dailyRTShutdownCost = this.rtm.getRtShutdownCost();
        //this.ames.addActualProductionCostsByDay( this.dailyRTProductionCost );
        //this.ames.addActualStartupCostsByDay( this.dailyRTStartupCost );
        //this.ames.addActualShutdownCostsByDay( this.dailyRTShutdownCost );
        //this.postCostsToGenAgents(day, this.dailyRTStartupCost, this.dailyRTProductionCost, this.dailyRTShutdownCost);
    }

    private void postCostsToGenAgents(final int day, double[][] dailyRTStartupCost,
            double[][] dailyRTProductionCost, double[][] dailyRTShutdownCost) {

        final ArrayList<GenAgent> genCos = this.ames.getGenAgentList();
        for (int gc = 0; gc < this.I; gc++) {
            final double[] genStartCosts = new double[this.H];
            final double[] genProductionCosts = new double[this.H];
            final double[] genShutdownCosts = new double[this.H];

            for (int h = 0; h < this.H; h++) {
                genStartCosts[h] = dailyRTStartupCost[h][gc];
                genProductionCosts[h] = dailyRTProductionCost[h][gc];
                genShutdownCosts[h] = dailyRTShutdownCost[h][gc];
            }

            genCos.get(gc).addDailyCosts(day,
                    genStartCosts,
                    genProductionCosts,
                    genShutdownCosts
            );
        }
    }

    public static String getStrings(double[][] a, int index) {
        //String[][] output = new String[a.length][];
        //int i = 0;
        String output = "";
        DecimalFormat LMPFormat = new DecimalFormat("###.####");
        for (int i = 0; i < a.length; i++) {
            //System.out.print( a[i][index]);
            String temp1 = LMPFormat.format(a[i][index]);
            output = output + temp1 + ",";
            //System.out.println("temp.."+temp);
            //output[i++] = Arrays.toString(d).replace("[", "").replace("]", "").split(",");
        }

        //System.out.println();
        return output;
    }

//    public void evaluateRealTimeBidsOffers(int h, int d) {
//
//        buc.solveRTOPF(supplyOfferRT, dailyPriceSensitiveDispatchRT[h], h, d);
//        dailyrealtimelmp = buc.getDailyRealTimeLMP();
//        for (int i = 0; i < I; i++) {
//            for (int j = 0; j < 4; j++) {
//                //System.out.println("supplyOfferRT: "+supplyOfferRT[i][j]);
//            }
//            //System.out.println("printing real-time LMP h: " + h + " :" + getStrings(dailyrealtimelmp, i));
//        }
//    }
    public void evaluateBidsOffers(int h, int d) {
        //System.out.println("Hour " + h + " Day " + d  +
        //                   ": Evaluate LSEs' bids and GenCos' offers.");

        supplyOfferByGen = dam.getSupplyOfferByGen();
        //System.out.println("SupplyOffer b is "+supplyOfferByGen[0][1]);
        loadProfileByLSE = dam.getLoadProfileByLSE();
        demandBidByLSE = dam.getDemandBidByLSE();
        demandHybridByLSE = dam.getDemandHybridByLSE();

        // Carry out BUC (Bid-based Unit Commitment) problem by solving DC OPF problem
        //System.out.printf("Solving the DC-OPF problem for day %1$d \n", d+1);
        buc.solveOPF();
        //Realtime OPF

        dailycommitment = buc.getDailyCommitment();  //dailycommittment -> damcommittment
        //System.out.println("Pls check is "+dailycommitment[0][1]);
        dailylmp = buc.getDailyLMP();  //dailylmp -> damlmps
        dailyBranchFlow = buc.getDailyBranchFlow(); //dailybranchflow -> dambranchflow
        dailyPriceSensitiveDispatch = buc.getDailyPriceSensitiveDemand();

        ames.addLSEAgenPriceSensitiveDemandByDay(dailyPriceSensitiveDispatch);
        ames.addBranchFlowByDay(dailyBranchFlow);
        ames.addGenAgentCommitmentByDay(dailycommitment);
        ames.addLMPByDay(dailylmp);
        ames.addHasSolutionByDay(buc.getHasSolution());
    }

    public void initialPost(int h, int d) {
        //System.out.println("Hour " + h + " Day " + d +
        //                   ": Post hourly commitment schedule and hourly LMPs.");

        dam.post(dailycommitment, dailylmp, dailyPriceSensitiveDispatch, 2);

    }

    public void produceCommitmentSchedule(int h, int d) {
        System.out.println("Hour " + h + " Day " + d
                + ": produce commitment schedule.");
    }

    private void sanityCheck(int day) {
        //make sure that the unit commitment for day d, matches the
        //actually producution. That is, if something wasn't committed
        //and still generated power, there is a problem!

        for (GenAgent ga : this.ames.getGenAgentList()) {
            ga.sanityCheck(day);
        }
    }

    public void DayAheadMarketCheckLastDayAction() {
        dam.checkGenLastDayAction();
    }

    /**
     * Post/store the commitment for each hour/genagent.
     *
     * @param dispatches hour X genco grid.
     */
    private void postRTDispatchesToGenAgents(int day, double[][] dispatches) {
        final ArrayList<GenAgent> genCos = this.ames.getGenAgentList();
        for (int gc = 0; gc < this.I; gc++) {
            final double[] genDispatches = new double[this.H];
            for (int h = 0; h < this.H; h++) {
                genDispatches[h] = dispatches[h][gc];
            }
            genCos.get(gc).addActualDispatch(day, genDispatches);
        }
    }

    // Get and set method
    public double[][] getSupplyOfferByGen() {
        return supplyOfferByGen;
    }

    public double[][] getLoadProfileByLSE() {
        return loadProfileByLSE;
    }

    public double[][] getNextDayLoadProfileByLSE() {
        return this.nextDayLoadProfileByLSE;
    }

    public double[][][] getDemandBidByLSE() {
        return demandBidByLSE;
    }

    public int[][] getDemandHybridByLSE() {
        return demandHybridByLSE;
    }

    public double[][] getSupplyOfferByGenRT() {
        return supplyOfferRT;
    }

    public double[][] getPriceSensitiveDispatchRT() {
        return dailyPriceSensitiveDispatch;
    }

    public int[][] getRealTimeDemandHybridByLSE() {
        return demandHybridByLSERealTime;
    }

    public DAMarket getDAMarket() {
        return dam;
    }

    public RTMarket getRTMarket() {
        return rtm;
    }

    public BUC getBUC() {
        return buc;
    }

    // Previously the following method is used 
//    public double[][] getDailyLMP() {
//        return buc.getDailyLMP();
//    }
    public double[][] getDailyLMP() {
        return this.dailylmp;
    }
    // Previously the following method is used 
//    public double[][] getDailyRealTimeLMP() {
//        return buc.getDailyRealTimeLMP();
//    }

//    public double[] getDailyRealTimeLMP() {
//        return this.dailyrealtimelmp;
//    }  
    public double[][] getDailyRealTimeLMP() {
        return this.dailyrealtimelmp;
    }

}
