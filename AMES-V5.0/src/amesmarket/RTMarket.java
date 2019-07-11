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
// RTMarket.java
// Real-time market
package amesmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.DataFileWriter;
import amesmarket.extern.psst.PSSTSCED;
import amesmarket.filereaders.BadDataFileFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Real-time market.
 */
public class RTMarket {

    //Real time market's data
    private AMESMarket ames;
    private ISO iso;

    /*
     * The values of these arrays come from
     * the SCED. The PSSTSced allocates fresh arrays
     * each time it is called. Which means we do not
     * need to worry about aliasing issues with these
     * arrays and can just return the references.
     */
    private int[] hasSolution;
    private double[][] rtDispatches;
    private double[] intervalRtDispatches;
    private double[][] rtBranchFlow;
    private double[][] rtLMPs;
    private double[] intervalRTLMPs;
    private double[][] rtProductionCost;
    private double[][] rtStartupCost;
    private double[][] rtShutdownCost;

    private double[][] supplyOfferByGen;
    private double[][] priceSensitiveDispatch;

    private int numGenAgents;
    private int numLSEAgents;
    private int numSupplyOfferParams;
    private int numHoursPerDay;
    private int numIntervalsInSim;

    BUC buc;
    private final PSSTSCED sced;

    //TODO-X : Parameterize file paths. Probably should be accessible
    //from the AMESMarket instance.
    private final File scedOutFile = new File("RTSCED.dat");
    private final File rtRefModelFile = new File("SCUCresources/ScenarioData/RTReferenceModel.dat");// new File("SCUCresources/ScenarioData/RTRefernceModel.dat");
    private final File unitCommitmentFile = new File("rt-unitcommitments.dat");

    // constructor
    public RTMarket(ISO iso, AMESMarket model) {

        //System.out.println("Created a RTMarket objecct");
        ames = model;
        this.iso = iso;
        numGenAgents = ames.getNumGenAgents();
        numLSEAgents = ames.getNumLSEAgents();
        numHoursPerDay = ames.getNumHoursPerDay();
        numSupplyOfferParams = 4;
        numIntervalsInSim = ames.M / ames.INTERVAL_SIZE;

        supplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];

        priceSensitiveDispatch = new double[numHoursPerDay][numLSEAgents];

        sced = new PSSTSCED(model, model.getBaseS(),
                unitCommitmentFile,
                rtRefModelFile,
                scedOutFile);
    }

    public void realTimeOperation(int h, int d) {
        System.out.println("Hour " + h + " Day " + d + ": Real Time Market operation.");
        supplyOfferByGen = iso.getSupplyOfferByGenRT();
        priceSensitiveDispatch = iso.getPriceSensitiveDispatchRT();
    }

    /**
     *
     * @param genCoCommitments
     * @param h
     * @param d
     * @param m
     * @param interval
     * @throws AMESMarketException
     */
    public void evaluateRealTimeBidsOffers(
            List<CommitmentDecision> genCoCommitments,
            //double[][] supplyOfferRT, double[] dailyPriceSensitiveDispatchRT,
            double[][] rtDemand, int m, int interval,
            int h, int d) {

        DataFileWriter dfw = new DataFileWriter();

        File unitCommitmentFileMod = new File(unitCommitmentFile + "_hour" + h);
        try {
            //write the correct data files.
            dfw.writeGenCommitments(ames.M, m, h, genCoCommitments, unitCommitmentFile);
            //dfw.writeGenCommitments(10, m, h, genCoCommitments, unitCommitmentFile); //ames.M is changed to 10
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = rtRefModelFile + "_hour" + h;
        File rtRefModelFileMod = new File(rtRefModelFile + "_hour" + h);
        try {
            // dfw.writeScenDatFile(rtRefModelFile, ames, d, rtDemand, ames.NUM_HOURS_PER_DAY);     // Initial usage
            //dfw.writeScedScenDatFile(rtRefModelFile, ames, m, h, d, rtDemand, ames.M);   //Rohit's modification
            dfw.writeScedScenDatFile(rtRefModelFile, ames, m, interval, h, d, rtDemand, ames.M, numIntervalsInSim);  // latest modification 
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //
            sced.solveOPF(interval);
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pull the data back out
        hasSolution = sced.getHasSolution();
        //power
        rtDispatches = sced.getDailyCommitment();
        rtBranchFlow = sced.getDailyBranchFlow();
        //costs
        rtLMPs = sced.getDailyLMP();
        intervalRTLMPs = sced.getIntervalLMP();
        intervalRtDispatches = sced.getIntervalGenDispatches();
        //sced.getDailyPriceSensitiveDemand();
        rtProductionCost = sced.getProductionCost();
        rtStartupCost = sced.getStartupCost();
        rtShutdownCost = sced.getShutdownCost();
    }

//    public double[][] getSupplyOfferByGen() {
//        return supplyOfferByGen;
//    }
//
//    public double[][] getPriceSensitiveDispatch() {
//        return priceSensitiveDispatch;
//    }
    public void setBUC(BUC buc) {
        this.buc = buc;
    }

    public int[] hasSolution() {
        return hasSolution;
    }

    /**
     * @return the rtDispatches
     */
    public double[][] getRtDispatches() {
        return rtDispatches;
    }
    
    public double[] getIntervalRtDispatches() {
        return intervalRtDispatches;
    }

    /**
     * @return the rtBranchFlow
     */
    public double[][] getRtBranchFlow() {
        return rtBranchFlow;
    }

    /**
     * @return the rtLMPs
     */
    public double[][] getDailyRtLMPs() {
        return rtLMPs;
    }
    public double[] getIntervalRtLMPs() {
        return intervalRTLMPs;
    }

    /**
     * @return the rtProductionCost
     */
    public double[][] getRtProductionCost() {
        return rtProductionCost;
    }

    /**
     * @return the rtStartupCost
     */
    public double[][] getRtStartupCost() {
        return rtStartupCost;
    }

    /**
     * @return the rtShutdownCost
     */
    public double[][] getRtShutdownCost() {
        return rtShutdownCost;
    }

    /**
     * @return the rtRefModelFile
     */
    public File getRtRefModelFile() {
        return rtRefModelFile;
    }

}
