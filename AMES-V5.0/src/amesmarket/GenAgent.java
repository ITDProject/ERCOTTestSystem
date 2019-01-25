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
// GenAgent.java
// GenCo (wholesale power producer)
package amesmarket;

import java.awt.*;

import uchicago.src.sim.gui.*;
import java.util.ArrayList;
import java.math.*;
import uchicago.src.sim.util.SimUtilities;
import edu.iastate.jrelm.core.JReLMAgent;
import edu.iastate.jrelm.core.SimpleAction;
import edu.iastate.jrelm.rl.ReinforcementLearner;
import edu.iastate.jrelm.rl.SimpleStatelessLearner;
import edu.iastate.jrelm.rl.rotherev.variant.VREParameters;
import edu.iastate.jrelm.rl.rotherev.REPolicy;
;

import java.util.Arrays;
import java.util.HashMap;
import amesmarket.CaseFileData.SCUCInputData;
import java.util.Map;

/**
 * Example showing what genData[i] contains
 *
 * GenData //ID	atBus	FCost	A	B	CapMin	CapMax	initMoney 1	1	5	15	0.10	0	100	1000
 *
 */


public class GenAgent implements Drawable, JReLMAgent {

    private static final int ID = 0;
    private static final int AT_NODE = 1;
    private static final int F_COST = 2;
    private static final int A = 3;
    private static final int B = 4;
    private static final int CAP_MIN = 5;
    private static final int CAP_MAX = 6;
    private static final int INIT_MONEY = 7;
    private static final int HOURS_PER_DAY = 24;

    // GenCo's data
    private int xCoord;      // Coordinate x on trans grid
    private int yCoord;      // Coordinate y on trans grid

    private int id;            // GenCo's ID
    private final int index;         // index for the genco when it is in an array.
    private int atBus;        // GenCo's location (at which bus)
    private double fixedCost;  // GenCo's fixed cost
    private double a;          // GenCo's (true) cost attribute
    private double b;          // GenCo's (true) cost attribute
    private double capMin;     // GenCo's (true) minimum production capacity limit
    private double capMax;     // GenCo's (true) maximum production capacity limit
    private double[] trueSupplyOffer; // (a,b,capMin,capMax), true

    private double noLoadCost = 0;
    private double coldStartUpCost = 0;
    private double hotStartUpCost = 0;
    private double shutDownCost = 0;

    private double aReported;      // GenCo's reported cost attribute
    private double bReported;      // GenCo's reported cost attribute
    private double capMaxReported; // GenCo's reported maximum production capacity limit
    private double[] reportedSupplyOffer;   // (aReported,bReported,capMin,capMaxReported), strategic

    // GenCo's records by hours (within a day)
    private double[] commitment;  // Day-Ahead hourly power commitment quantity
    private double[] dispatch;    // Real-Time hourly power dispatch quantity
    private int[] commitmentStatus;
    private double[] dayAheadLMP; // Day-Ahead hourly locational marginal price
    private double[] realTimeLMP; // Real-Time hourly locational marginal price
    private double[] totalVariableCost;  // totalVariableCost = A*power + B*power^2
    private double[] hourlyTotalCost;          // hourlyTotalCost = totalVariableCost + FCost

    private double[] hourlyVariableCost;// hourlyVariableCost[h] = a*power + b*power^2
    private double[] hourlyNetEarning;// hourlyNetEarning[h] = dispatch[h]*lmp[h]
    private double[] hourlyProfit;// hourlyProfit[h] = dispatch[h]*lmp[h] - hourlyTotalCost[h]
    private double dailyNetEarnings;   // dailyNetEarnings = sum of hourlyNetEarning over 24 hours
    private double dailyProfit;   // dailyProfit = sum of hourlyProfit over 24 hours
    private double money;    // GenCo's accumulative money holding,
    // money (new) = money(previous) + dailyProfit(new)

    private double[] hourlyRevenue;
    private double dailyRevenue;  // 
    private double dailyRevenueCE; // dailyRevenue under CE (Competitive Equil) case
    private double marketAdvantage; // marketAdvantage = (dailyNetGain - dailyNetGainCE)/dailyNetGainCE
    private double[] lernerIndex;  // lerner index (at dispatched level for each hour)
    private double[] marketPower;  // market power measure (at dispatched level for each hour)

    private double choiceProbability; // gen's learning choice probability
    private double choicePropensity;  // gen's learning choice Propensity
    private int choiceID;  // gen's learning choice ID

    //GenCo's records by day
    //private ArrayList commitmentByDay;
    //private ArrayList dispatchByDay;
    //private ArrayList dayAheadLMPByDay;
    //private ArrayList realTimeLMPByDay;
    //GenCo's records by day
    /**
     * Commit vector for each day d. Now modified to matrix
     */
    private final HashMap<Integer, int[]> commitmentByDay;
    /**
     * Dispatch levels for each hour hour of each day.
     */
    private final HashMap<Integer, double[]> dispatchByDay;
    /**
     * Costs for each hour of a day.
     */
    private final HashMap<Integer, double[]> productionCostsByDay;
    //NOT USED. Stored in the AMESMARKET.
    private final HashMap<Integer, double[]> startupCostsByDay;
    private final HashMap<Integer, double[]> shutdownCostsByDay;

    private final ArrayList<double[][]> dayAheadLMPByDay;
    private final ArrayList<double[][]> realTimeLMPByDay;

    // JReLM component
    private SimpleStatelessLearner learner;
    private int randomSeed;

    // Learning variables
    private double lowerRI; // lower Range Index
    private double upperRI; // upper Range Index
    private double upperRCap; // upper relative capacity
    private double slopeStart;

    private double priceCap;  // max price for LMP
    private int iRewardSelection; // 0->profit, 1->net earnings

    // Check if Action Probability Stable
    private int iStartDay;
    private int iCheckDayLength;
    private double dActionProbability;
    private boolean bActionProbabilityCheck;

    private boolean bActionProbabilityConverge;
    private int iCheckDayLengthCount;
    private int iDayCount;
    private double[] oldActionProbability;
    private double[] newActionProbability;

    // Check if Learning Result Stable
    private int iLearningCheckStartDay;
    private int iLearningCheckDayLength;
    private double dLearningCheckDifference;
    private boolean bLearningCheck;

    private boolean bLearningCheckConverge;
    private int iLearningCheckDayLengthCount;
    private int iLearningCheckDayCount;
    private double[] oldLearningResult;

    private boolean bDailyNetEarningConverge;
    private int iDailyNetEarningDayLengthCount;
    private int iDailyNetEarningDayCount;
    private boolean bDailyNetEarningThreshold;
    private double dDailyNetEarningThreshold;
    private int iDailyNetEarningStartDay;
    private int iDailyNetEarningDayLength;
    private double[] oldDailyNetEarningResult;

    private int iActinDomain;
    ArrayList newActionDomain;

    private String genID;

    /**
     * Value for PowerGeneratedT0 in SCUC input file. This is the default t0
     * power only. Once the simulation starts running this will be ignored.
     */
    private double defaultT0pwer = 1000;
    private double T0NextDayPower = 1000;
    private double PrevIntervalPower = 1000;

    private int HourUnitON = 0;
    private int T0NextDayUnitOnState = 0;

    private int UnitOnT0State = -2;
    private int minUpTime = 4;
    private int minDownTime = 6;
    private double nominalRampUpLim = 9999;
    private double nominalRampDownLim = 9999;
    private double startupRampLim = 9999;
    private double shutdownRampLim = 9999;

    private String fuelType = "Unknown";

    // Constructor
    /**
     * Flag for whether or not the generator represents an alert or canary
     * generator. Certain calculations (e.g. gen capacity) need to exclude the
     * canary generators.
     */
    private final boolean isCanary;

    /**
     * @param genData
     * @param learningParams
     * @param actionDomain
     * @param ss
     * @param dCap
     * @param random
     * @param iStart
     * @param iLength
     * @param dCheck
     * @param bCheck
     * @param iLearnStart
     * @param iLearnLength
     * @param dLearnCheck
     * @param bLearnCheck
     * @param dEarningThreshold
     * @param bEarningThresh
     * @param iEarningStart
     * @param iEarningLength
     * @param iReward
     * @param isCanary
     */
    public GenAgent(double[] genData, VREParameters learningParams,
            ArrayList actionDomain, double ss, double dCap, int random, int iStart, int iLength, double dCheck, boolean bCheck,
            int iLearnStart, int iLearnLength, double dLearnCheck, boolean bLearnCheck, double dEarningThreshold, boolean bEarningThresh,
            int iEarningStart, int iEarningLength, int iReward, boolean isCanary, int index) {

        xCoord = -1;
        yCoord = -1;

        // Parse genData
        id = (int) genData[ID];
        atBus = (int) genData[AT_NODE];
        fixedCost = genData[F_COST];
        a = genData[A];
        b = genData[B];
        capMin = genData[CAP_MIN];
        capMax = genData[CAP_MAX];
        money = genData[INIT_MONEY];

        // trueSupplyOffer = (a, b, capMin, capMax)
        trueSupplyOffer = new double[CAP_MAX - A + 1];
        for (int i = 0; i < CAP_MAX - A + 1; i++) {
            trueSupplyOffer[i] = genData[i + A];
        }

        // Initialize reportedSupplyOffer to all zeros
        // reportedSupplyOffer = (aReported, bReported, capMin, capMaxReported)
        reportedSupplyOffer = new double[trueSupplyOffer.length];

        commitment = new double[HOURS_PER_DAY];
        dispatch = new double[HOURS_PER_DAY];
        dayAheadLMP = new double[HOURS_PER_DAY];
        realTimeLMP = new double[HOURS_PER_DAY];
        totalVariableCost = new double[HOURS_PER_DAY];

        commitmentStatus = new int[HOURS_PER_DAY];

        hourlyTotalCost = new double[HOURS_PER_DAY];
        hourlyProfit = new double[HOURS_PER_DAY];
        hourlyNetEarning = new double[HOURS_PER_DAY];
        hourlyVariableCost = new double[HOURS_PER_DAY];
        hourlyRevenue = new double[HOURS_PER_DAY];

        dailyRevenue = 0;
        dailyRevenueCE = 0;
        marketAdvantage = 0;
        lernerIndex = new double[HOURS_PER_DAY];
        marketPower = new double[HOURS_PER_DAY];

        // Create historical data records (initialized all to zeros)
        commitmentByDay = new HashMap<Integer, int[]>();
        dispatchByDay = new HashMap<Integer, double[]>();
        dayAheadLMPByDay = new ArrayList<double[][]>();
        realTimeLMPByDay = new ArrayList<double[][]>();
        productionCostsByDay = new HashMap<Integer, double[]>();
        startupCostsByDay = new HashMap<Integer, double[]>();
        shutdownCostsByDay = new HashMap<Integer, double[]>();

        randomSeed = random;
        slopeStart = ss;
        priceCap = dCap;
        iStartDay = iStart;
        iCheckDayLength = iLength;
        dActionProbability = dCheck;
        bActionProbabilityCheck = bCheck;
        iCheckDayLengthCount = 0;
        iDayCount = 1;
        bActionProbabilityConverge = false;

        iLearningCheckStartDay = iLearnStart;
        iLearningCheckDayLength = iLearnLength;
        dLearningCheckDifference = dLearnCheck;
        bLearningCheck = bLearnCheck;
        bLearningCheckConverge = false;
        iLearningCheckDayLengthCount = 0;
        iLearningCheckDayCount = 1;
        oldLearningResult = new double[3];

        bDailyNetEarningConverge = false;
        iDailyNetEarningDayLengthCount = 0;
        iDailyNetEarningDayCount = 1;
        dDailyNetEarningThreshold = dEarningThreshold;
        bDailyNetEarningThreshold = bEarningThresh;
        iDailyNetEarningStartDay = iEarningStart;
        iDailyNetEarningDayLength = iEarningLength;
        oldDailyNetEarningResult = new double[iDailyNetEarningDayLength];

        iRewardSelection = iReward;

        newActionDomain = checkActionDomain(actionDomain);
        //System.out.println("\nActionDomain Size "+newActionDomain.size()+" for GenCo :"+id);
        learner = new SimpleStatelessLearner(learningParams, newActionDomain);

        iActinDomain = newActionDomain.size();
        oldActionProbability = new double[iActinDomain];
        newActionProbability = new double[iActinDomain];

        //System.out.println("GenCo ID="+id+" maxmum profit="+getMaxPotentialProfit());
        this.isCanary = isCanary;
        this.index = index;
    }

    public ArrayList checkActionDomain(ArrayList actionList) {
        ArrayList newActionList = new ArrayList();
        for (int i = 0; i < actionList.size(); i++) {
            double[] action = (double[]) actionList.get(i);
            double[] newAction = action.clone();

            if (!checkOverPriceCap(newAction)) {
                newActionList.add(newAction);
            }
        }

        return newActionList;
    }

    public boolean checkOverPriceCap(double[] action) {
        double lRI = action[0];
        double uRI = action[1];
        double uRCap = action[2];
        // Step 0: To get capMaxCalculated
        double capMaxCalculated = uRCap * (capMax - capMin) + capMin;

        // Step 1: To get lR
        double lR = (a + 2 * b * capMin) / (1 - lRI);

        // Step 2: To get uStart
        double u = a + 2 * b * capMaxCalculated;
        double uStart;
        if (lR < u) {
            uStart = u;
        } else {
            uStart = lR + slopeStart;
        }

        if (uStart >= priceCap) {
            return true;

        }

        // Step 3: To get uR
        double uR = uStart / (1 - uRI);

        // Step 4: To get bReported
        action[1] = 0.5 * ((uR - lR) / (capMaxCalculated - capMin));

        // Step 5: To get aReported
        action[0] = lR - 2 * action[1] * capMin;

        // for PriceCap
        double maxPrice = action[0] + 2 * action[1] * capMaxCalculated;
        if (maxPrice > priceCap) {
            action[2] = (priceCap - action[0]) / (2 * action[1]);
        } else {
            action[2] = capMaxCalculated;
        }

        return false;
    }

    public double getMaxPotentialProfit() {
        int iMaxActionIndex = 0;
        double aRMax = 0.0;
        double bRMax = 0.0;
        double CapPriceMax = 0.0; // for maximum capacity output price
        double CapMax = 0.0;
        double dMaxProfit = 0.0;

        for (int i = 0; i < newActionDomain.size(); i++) {
            double[] action = (double[]) newActionDomain.get(i);

            double aR = action[0];
            double bR = action[1];
            double capMaxCalculated = action[2];
            // calculate max price
            double maxPrice = aR + 2 * bR * capMaxCalculated;
            if (maxPrice > CapPriceMax) {
                aRMax = aR;
                bRMax = bR;
                CapPriceMax = maxPrice;
                CapMax = capMaxCalculated;
                iMaxActionIndex = i;
            }
        }

        dMaxProfit = CapPriceMax * CapMax - 0.5 * (a + a + 2 * b * capMax) * capMax;
        //System.out.println("aRMax="+aRMax+" bRMax="+bRMax+" CapMax="+CapMax+" CapPriceMax="+CapPriceMax);
        return dMaxProfit;
    }

    public void chooseNextAction() {
        // Use chooseActionRaw to get the unwrapped supply offer values
        double[] actionTriplet = (double[]) learner.chooseActionRaw();
        aReported = actionTriplet[0];
        bReported = actionTriplet[1];
        capMaxReported = actionTriplet[2];
    }

    public double[] submitSupplyOffer() {
        //calculateReportedSupplyOfferValues();
        reportedSupplyOffer[0] = aReported;
        reportedSupplyOffer[1] = bReported;
        reportedSupplyOffer[2] = capMin;
        reportedSupplyOffer[3] = capMaxReported;
        return reportedSupplyOffer;
    }

    // Refer to DynTest paper Appendix 5.2 "Implementation of GenAgent's Learning"
    private void calculateReportedSupplyOfferValues() {

        // Step 0: To get capMaxReported
        capMaxReported = upperRCap * (capMax - capMin) + capMin;

        // Step 1: To get lR
        double lR = (a + 2 * b * capMin) / (1 - lowerRI);

        // Step 2: To get uStart
        double u = a + 2 * b * capMaxReported;
        double uStart;
        if (lR < u) {
            uStart = u;
        } else {
            uStart = lR + slopeStart;
        }

        // Step 3: To get uR
        double uR = uStart / (1 - upperRI);

        // Step 4: To get bReported
        bReported = 0.5 * ((uR - lR) / (capMaxReported - capMin));

        // Step 5: To get aReported
        aReported = lR - 2 * bReported * capMin;

        // for PriceCap
        double maxPrice = aReported + 2 * bReported * capMaxReported;
        //System.out.println("\n Before maxPrice "+maxPrice+" for GenCo :"+id);
        if (maxPrice > priceCap) {
            capMaxReported = (priceCap - aReported) / (2 * bReported);
        }
        //maxPrice=aReported+2*bReported*capMaxReported;
        //System.out.println("\n After maxPrice "+maxPrice+" for GenCo :"+id+"\n");

    }

    public double[] submitTrueSupplyOffer() {
        double[] trueOffer = new double[4];
        // for PriceCap
        double maxPrice = trueSupplyOffer[0] + 2 * trueSupplyOffer[1] * trueSupplyOffer[3];

        trueOffer[0] = trueSupplyOffer[0];
        trueOffer[1] = trueSupplyOffer[1];
        trueOffer[2] = trueSupplyOffer[2];

        if (priceCap < trueOffer[0]) {
            trueOffer[2] = 0;
            trueOffer[3] = 0;

            return trueOffer;
        }

        if (maxPrice <= priceCap) {
            trueOffer[3] = trueSupplyOffer[3];
        } else {
            trueOffer[3] = (priceCap - trueSupplyOffer[0]) / (2 * trueSupplyOffer[1]);
        }

        return trueOffer;
    }

    public void updateSupplyOffer(int flag) {
        updateProfit();

        if (flag != 1) {
            updateMoney();
        }

        learn();

        iDayCount++;
        if (bActionProbabilityCheck && (iDayCount >= iStartDay)) {
            updateActionProbabilities();
        }

        iLearningCheckDayCount++;
        if (bLearningCheck && (iLearningCheckDayCount >= iLearningCheckStartDay)) {
            updateLearningResult();
        }

        iDailyNetEarningDayCount++;
        if (bDailyNetEarningThreshold && (iDailyNetEarningDayCount >= iDailyNetEarningStartDay)) {
            updateDailyNetEarningResult();
        }
    }

    private void updateActionProbabilities() {
        REPolicy policy = (REPolicy) learner.getPolicy();
        double[] dProbability = policy.getProbabilities();

        boolean bConverged = true;
        for (int i = 0; i < iActinDomain; i++) {
            oldActionProbability[i] = newActionProbability[i];
            newActionProbability[i] = dProbability[i];
            if ((bConverged) && (Math.abs(newActionProbability[i] - oldActionProbability[i]) > dActionProbability)) {
                bConverged = false;
                iCheckDayLengthCount = 0;
                bActionProbabilityConverge = false;
            }
        }

        if (bConverged) {
            iCheckDayLengthCount++;
            if (iCheckDayLengthCount > iCheckDayLength) {
                bActionProbabilityConverge = true;
            }
        }
    }

    private void updateLearningResult() {

        boolean bConverged = true;

        if (Math.abs(oldLearningResult[0] - aReported) > dLearningCheckDifference) {
            bConverged = false;
        }

        if (Math.abs(oldLearningResult[1] - bReported) > dLearningCheckDifference) {
            bConverged = false;
        }

        if (Math.abs(oldLearningResult[2] - capMaxReported) > dLearningCheckDifference) {
            bConverged = false;
        }

        oldLearningResult[0] = aReported;
        oldLearningResult[1] = bReported;
        oldLearningResult[2] = capMaxReported;

        if (bConverged) {
            iLearningCheckDayLengthCount++;
            if (iLearningCheckDayLengthCount > iLearningCheckDayLength) {
                bLearningCheckConverge = true;
            }
        } else {
            iLearningCheckDayLengthCount = 0;
            bLearningCheckConverge = false;
        }
    }

    private void updateDailyNetEarningResult() {

        boolean bConverged = true;

        for (int i = 0; i < iDailyNetEarningDayLengthCount; i++) {
            if (Math.abs(oldDailyNetEarningResult[i] - dailyNetEarnings) > dDailyNetEarningThreshold) {
                bConverged = false;
                break;
            }
        }

        if (bConverged) {
            iDailyNetEarningDayLengthCount++;
            if (iDailyNetEarningDayLengthCount >= iDailyNetEarningDayLength) {
                bDailyNetEarningConverge = true;
                iDailyNetEarningDayLengthCount = iDailyNetEarningDayLength;

                for (int j = 0; j < iDailyNetEarningDayLengthCount - 1; j++) {
                    oldDailyNetEarningResult[j] = oldDailyNetEarningResult[j + 1];
                }
                oldDailyNetEarningResult[iDailyNetEarningDayLengthCount - 1] = dailyNetEarnings;
            } else {
                oldDailyNetEarningResult[iDailyNetEarningDayLengthCount - 1] = dailyNetEarnings;
            }
        } else {
            oldDailyNetEarningResult[0] = dailyNetEarnings;
            iDailyNetEarningDayLengthCount = 0;
            bDailyNetEarningConverge = false;
        }
    }

    // Lerner Index = (LMP - MC)/LMP
    private void computeLernerIndex() {
        for (int h = 0; h < HOURS_PER_DAY; h++) {
            lernerIndex[h] = (dayAheadLMP[h] - (a + 2 * b * commitment[h])) / dayAheadLMP[h];
        }
        System.out.println(getID() + " Lerner Index at each hour: ");
        Support.print2dpByRow(lernerIndex);
    }

    // Market Power = (LMP - MC)/MC
    private void computeMarketPower() {
        for (int h = 0; h < HOURS_PER_DAY; h++) {
            marketPower[h] = (dayAheadLMP[h] - (a + 2 * b * commitment[h])) / (a + 2 * b * commitment[h]);
        }
        System.out.println(getID() + " market power at each hour: ");
        Support.print2dpByRow(marketPower);
    }

    public void updateProfit() {
        dailyProfit = 0;
        dailyNetEarnings = 0;
        dailyRevenue = 0;

        for (int h = 0; h < HOURS_PER_DAY; h++) {
            hourlyVariableCost[h] = a * commitment[h] + b * commitment[h] * commitment[h];
            hourlyTotalCost[h] = hourlyVariableCost[h] + fixedCost;
            hourlyRevenue[h] = commitment[h] * dayAheadLMP[h];

            hourlyProfit[h] = hourlyRevenue[h] - hourlyTotalCost[h];
            hourlyNetEarning[h] = hourlyRevenue[h] - hourlyVariableCost[h];

            dailyProfit += hourlyProfit[h];
            dailyNetEarnings += hourlyNetEarning[h];
            dailyRevenue += hourlyRevenue[h];
        }

        //System.out.println("GenCo "+getGenID()+" daily dailyProfit: "+Support.roundOff(dailyProfit,2));
    }

    private void computeTotalCost() {
        for (int h = 0; h < HOURS_PER_DAY; h++) {
            hourlyTotalCost[h] = a * commitment[h] + b * commitment[h] * commitment[h] + fixedCost;
        }
    }

    private void computeProductionCost() {
        for (int h = 0; h < HOURS_PER_DAY; h++) {
            hourlyTotalCost[h] = a * commitment[h] + b * commitment[h] * commitment[h] + fixedCost;
        }
    }

    private void updateMoney() {
        money = money + dailyProfit;
        //System.out.println("GenCo "+getGenID()+" money holdings: "+Support.roundOff(money,2));
    }

    // genAgent learning (updating propensity based on current period dailyProfit)
    private void learn() {
        //System.out.println("Learning Report for GenCo: " + getID() );
        SimpleAction lastAction = (SimpleAction) learner.getPolicy().getLastAction();
        choiceID = lastAction.getID();
        double[] act = (double[]) lastAction.getAct();
        REPolicy policy = (REPolicy) learner.getPolicy();
        /*
    System.out.printf("\tLast action chosen:  id= " + id +
                       ";\t(lowerRI, upperRI, upperRCap)=(%1$6.4f, %2$6.4f, %3$6.4f)\n",
                       act[0], act[1], act[2]);
    
    if(Double.isNaN(policy.getProbability(id))){
        System.out.printf("\tBefore update --> the policy.getProbability return value is not a number!!!\n");

        System.out.printf("\tBefore updating with daily profit: probability=%1$6.4f\tpropensity=%2$f\n",
                         1.0,policy.getPropensity(id));
    }
    else
        System.out.printf("\tBefore updating with daily profit: probability=%1$6.4f\tpropensity=%2$f\n",
                         policy.getProbability(id),policy.getPropensity(id));
         */
        if (iRewardSelection == 0)// profit
        {
            learner.update(new Double(dailyProfit));
        }

        if (iRewardSelection == 1)// net earnings
        {
            learner.update(new Double(dailyNetEarnings));
        }
        /*
    if(Double.isNaN(policy.getProbability(id))){
        System.out.printf("\tAfter update --> the policy.getProbability return value is not a number!!!\n");
        
        System.out.printf("\tAfter updating with daily profit:  probability=%1$6.4f\tpropensity=%2$f\n\n",
                         1.0,policy.getPropensity(id));
    }
    else
        System.out.printf("\tAfter updating with daily profit:  probability=%1$6.4f\tpropensity=%2$f\n\n",
                         policy.getProbability(id),policy.getPropensity(id));
    
         */
        choiceProbability = policy.getProbability(choiceID);
        choicePropensity = policy.getPropensity(choiceID);

    }

    public boolean isSolvent() {
        boolean solvency = true;
        if (money < 0) {
            solvency = false;
            System.out.println("GenCo " + getGenID() + " is out of market.");
        }
        return solvency;
    }

    public double[] LastDayCheckAction() {
        REPolicy policy = (REPolicy) learner.getPolicy();
        double[] dProbability = policy.getProbabilities();

        int iAction = newActionDomain.size();
        double[] action = new double[iAction];

        for (int i = 0; i < newActionDomain.size(); i++) {
            action[i] = dProbability[i];
        }

        return action;
    }

    public boolean isLearningResultConverge() {
        return bLearningCheckConverge;
    }

    public boolean isLearningResultCheck() {
        return bLearningCheck;
    }

    public boolean isActionProbabilityConverge() {
        return bActionProbabilityConverge;
    }

    public boolean isActionProbabilityCheck() {
        return bActionProbabilityCheck;
    }

    public boolean isDailyNetEarningConverge() {
        return bDailyNetEarningConverge;
    }

    public boolean isDailyNetEarningCheck() {
        return bDailyNetEarningThreshold;
    }

    /**
     * Whether or not the generator is a 'canary' that is used to indicate the
     * load was too much for the standard generators. This is a simple way to
     * deal with the lack of reserve problem.
     */
    public boolean isCanary() {
        return isCanary;
    }

    /**
     * Check to see if there are dispatch values for hours when the gen co was
     * not committed. Writes error messages directly to sys out.
     *
     * @param day
     */
    public void sanityCheck(int day) {
        //if(isCanary()) return; //don't check for the canaries. They are always commited for the actual rt market calculations.
        int[] commitmentsForDay = getCommitmentsForDay(day);
        double[] dispatchesForDay = getDispatchesForDay(day);

        if (dispatchesForDay == null) {
            if (day > 1) {
                System.err.println("WARNING: " + getID() + " has no dispatch information "
                        + "for day " + day);
            }

            //nothing to be done if the list is null.
            //this situation can happen for the first day of operation.
            return;
        }

        if (commitmentsForDay == null && dispatchesForDay != null) {
            System.err.println(getID() + " has dispatch values for day " + day
                    + " but no commitment vector.");
        } else {
            for (int h = 0; h < commitmentsForDay.length; h++) {
                if (commitmentsForDay[h] == 0 && dispatchesForDay[h] > 0) {
                    System.err.println("WARNING: " + getID() + " was not commited "
                            + "on day " + day + " hour " + h + ", but is has a "
                            + "dispatch value for " + dispatchesForDay[h]
                            );
                } else if (
                        !isCanary &&
                        commitmentsForDay[h] == 1 &&  //FIXME: Negation logic to complicated
                        !Support.doubleIsDifferent(dispatchesForDay[h], 1e-6)) {//close to zero
                    //System.out.println(getID() + " committed but not dispatched at hour " + (h + 1));
                }
//                for (int m = 0; m < commitmentsForDay[h].length; m++) {
//                    if (commitmentsForDay[h][m] == 0 && dispatchesForDay[h] > 0) {
//                        System.err.println("WARNING: " + getID() + " was not commited "
//                                + "on day " + day + " hour " + h + ", but is has a "
//                                + "dispatch value for " + dispatchesForDay[h]
//                        );
//                    } else if (!isCanary
//                            && commitmentsForDay[h][m] == 1
//                            && //FIXME: Negation logic to complicated
//                            !Support.doubleIsDifferent(dispatchesForDay[h], 1e-6)) {//close to zero
//                        //System.out.println(getID() + " committed but not dispatched at hour " + (h + 1));
//                    }
//                }
            }
        }
    }

    /**
     * Get the dispatch values for day d.
     *
     * @param day
     * @return array of dispatches, or null if no data for the day.
     */
    public double[] getDispatchesForDay(int day) {
        return dispatchByDay.get(day);
    }

    public int getStartDay() {
        return iStartDay;
    }

    public int getCheckDayLength() {
        return iCheckDayLength;
    }

    // GenCo's get and set methods
    public void setXY(int newX, int newY) {
        xCoord = newX;
        yCoord = newY;
    }

    public int getGenID() { // This method name cannot be changed to "int getID"
        return id;           // because it'll conflict with JReLM interface method "String getID"
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    public Map<Integer, int[]> getCommitmentDecisions() {
        return commitmentByDay;
    }

    /**
     * TODO-X Comment
     *
     * @param day
     * @return
     */
    public double[] getHourlyProductionCostsForDay(int day) {
        return productionCostsByDay.get(day);
    }

    public int getAtNode() {
        return atBus;
    }

    public double[] getSupplyOffer() {
        return reportedSupplyOffer;
    }

    public double getMoney() {
        return money;
    }

    public double[] getHourlyRevenue() {
        return hourlyRevenue;
    }

    public double[] getHourlyProfit() {
        return hourlyProfit;
    }

    public double[] getHourlyNetEarning() {
        return hourlyNetEarning;
    }

    public double[] getCommitment() {
        return commitment;
    }

    public void setCommitment(double[] comm) {
        commitment = comm;
    }

    public void setCommitmentStatus(int[] status) {
        System.arraycopy(status, 0, commitmentStatus, 0, HOURS_PER_DAY);
    }

//    public void setCommitmentStatus(int hour, int status){
//        commitmentStatus[hour] = status;
//    }
    public int getCommitmentStatus(int h) {
        return commitmentStatus[h];
    }

    public double[] getDayAheadLMP() {
        return dayAheadLMP;
    }

    public void setDayAheadLMP(double[] lmprice) {
        dayAheadLMP = lmprice;
    }

    /*    public  getCommitmentByDay() {
        return commitmentByDay;
    }
     */
    public ArrayList getDayAheadLMPByDay() {
        return dayAheadLMPByDay;
    }

    public double getChoiceProbability() {
        return choiceProbability;
    }

    public double getChoicePropensity() {
        return choicePropensity;
    }

    public int getChoiceID() {
        return choiceID;
    }

    public void report() {
        System.out.println(getID() + " at (" + xCoord + "," + yCoord + ") has "
                + "current money holding " + money);
    }

    // Implemented methods for JReLMAgent interface
    public String getID() {
        //return "GenCo" + id;
        return genID;
    }

    public void setID(String gencoName) {
        this.genID = gencoName;
    }

    public ReinforcementLearner getLearner() {
        return learner;
    }

    // Implemented methods for Drawable interface
    public int getX() {
        return xCoord;
    }

    public int getY() {
        return yCoord;
    }

    public void draw(SimGraphics sg) {
        sg.drawFastRoundRect(Color.blue);         //GEN is blue colored
    }

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public double getProfit() {
        return dailyProfit;
    }

    public double getNetEarning() {
        return dailyNetEarnings;
    }

    public void setNoLoadCost(double d) {
        this.noLoadCost = d;
    }

    public void setColdStartUpCost(double d) {
        this.coldStartUpCost = d;
    }

    public void setHotStartUpCost(double d) {
        this.hotStartUpCost = d;
    }

    public void setShutDownCost(double d) {
        this.shutDownCost = d;
    }

    public double getNoLoadCost() {
        return this.noLoadCost;
    }

    public double getColdStartUpCost() {
        return this.coldStartUpCost;
    }

    public double getHotStartUpCost() {
        return this.hotStartUpCost;
    }

    public double getShutDownCost() {
        return this.shutDownCost;
    }

    /**
     * Get the commitment decision for day d.
     *
     * @param d
     * @return
     */
    public int[] getCommitmentsForDay(int d) {
        return commitmentByDay.get(d);
    }

    /**
     * @return the Minimum generation capacity.
     */
    public double getCapacityMin() {
        return capMin;
    }

    /**
     * @return the Maximum generation capacity.
     */
    public double getCapacityMax() {
        return capMax;
    }

    /**
     * @return the Maximum generation capacity the genco reports.
     */
    public double getReportedCapacityMax() {
        return capMaxReported;
    }

    /**
     * On/off Status for T0, day d. From the pyomo model:
     * <blockquote>
     * if positive, the number of hours prior to (and including) t=0 that the
     * unit has been on. if negative, the number of hours prior to (and
     * including) t=0 that the unit has been off. the value cannot be 0, by
     * definition.
     *
     * </blockquote>
     *
     * @param day
     * @return The on/off is whether or not the unit was on/off on the last hour
     * of day-1, or, 1, if the generator does not have a record for day d-1.
     *
     */
    public int getUnitOnT0State(int day) {
        if(day ==1){
            return UnitOnT0State;
        }
        final int[] commitmentsForDay = getCommitmentsForDay(day);
        // Don't know anything about the day. Assume it has been on
        // for 1 hour.
        if (commitmentsForDay == null) {
            return 1;
        }
        
         final int lastHourState = commitmentsForDay[commitmentsForDay.length - 1];
        int numHourSame = 0;
        // Assumes last min state == last hour state
        //final int lastMinState = commitmentsForDay[commitmentsForDay.length - 1][commitmentsForDay[commitmentsForDay.length - 1].length - 1];
        //int numMinSame = 0;

                //count how many hours were the same generator state (on or off)
        for (int i=commitmentsForDay.length-1; i>=0; i--) {
            if (commitmentsForDay[i] == lastHourState) {
                numHourSame++;
                //System.out.println("i: " + i + " numHourSame:" + numHourSame);
            } else { //state changed, done counting.
                break;
            }
        }
        
//        //count how many minutes were the same generator state (on or off)
//        for (int i = commitmentsForDay.length - 1; i >= 0; i--) {
//            for (int j = commitmentsForDay[i].length - 1; j >= 0; j--) {
//                if (commitmentsForDay[i][j] == lastMinState) {
//                    numMinSame++;
//                } else { //state changed, done counting.
//                    break;
//                }
//            }
//        }

        //'guess' the genco will have the same state for its initial first
        // hour. See method comment for reason. 
        // numHourSame++; // Modified:Swathi - commented it as the first hour has already been counted 

        if (lastHourState == 1) { //Generator has been on.
            return numHourSame;
        } else { //invert the sign. Generator has been off.
            return -numHourSame;
        }
        
//        numMinSame++;
//
//        if (lastMinState == 1) { //Generator has been on.
//            return numMinSame;
//        } else { //invert the sign. Generator has been off.
//            return -numMinSame;
//        }
    }

    public int getUnitOnHour() {
        return HourUnitON; //
    }

    public void setUnitOnHour(int i) {
        HourUnitON = i;
    }

    public double getPowerT0(int day) {
        double[] powerGen = dispatchByDay.get(day);
        if (powerGen != null) {
            return powerGen[powerGen.length - 1]; //last hour of the day.
        } else {
            return getCapacityMax();
        }
    }

    public void setPowerT0(double p) {
        defaultT0pwer = p;
        T0NextDayPower = p;
    }

    public double getPowerT0NextDay() {
        return T0NextDayPower; //25th hour of the day.
    }

    public void setPowerT0NextDay(double p) {
        T0NextDayPower = p;
    }

    public int getUnitOnStateT0NextDay() {
        return T0NextDayUnitOnState; //24th hour of the day.
    }

    public void setUnitOnStateT0NextDay(int i) {
        T0NextDayUnitOnState = i;
    }

    public double getPowerPrevInterval() {
        return PrevIntervalPower; //
    }

    public void setPowerPrevInterval(double p) {
        PrevIntervalPower = p;
    }
    
    public void setUnitOnT0State(int n){
       UnitOnT0State = n;
    }

    public int getMinUpTime() {
        return minUpTime;
    }

    public void setMinUpTime(int t) {
        minUpTime = t;
    }

    public int getMinDownTime() {
        return minDownTime;
    }

    public void setMinDownTime(int t) {
        minDownTime = t;
    }

    public double getNominalRampUpLim() {
        return nominalRampUpLim;
    }

    /**
     * @param nominalRampUpLim the nominalRampUpLim to set
     */
    public void setNominalRampUpLim(double nominalRampUpLim) {
        this.nominalRampUpLim = nominalRampUpLim;
    }

    public double getNominalRampDownLim() {
        return nominalRampDownLim;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    /**
     * @param nominalRampDownLim the nominalRampDownLim to set
     */
    public void setNominalRampDownLim(double nominalRampDownLim) {
        this.nominalRampDownLim = nominalRampDownLim;
    }

    public double getStartupRampLim() {
        return startupRampLim;
    }

    /**
     * @param startupRampLim the startupRampLim to set
     */
    public void setStartupRampLim(double startupRampLim) {
        this.startupRampLim = startupRampLim;
    }

    public double getShutdownRampLim() {
        return shutdownRampLim;
    }

    /**
     * @param shutdownRampLim the shutdownRampLim to set
     */
    public void setShutdownRampLim(double shutdownRampLim) {
        this.shutdownRampLim = shutdownRampLim;
    }

    public void addExtraData(SCUCInputData sid) {
        setPowerT0(sid.getPowerT0());
        setUnitOnT0State(sid.getUnitOnT0State());
        setMinUpTime(sid.getMinUpTime());
        setMinDownTime(sid.getMinDownTime());
        setNominalRampUpLim(sid.getNominalRampUp());
        setNominalRampDownLim(sid.getNominalRampDown());
        setStartupRampLim(sid.getStartupRampLim());
        setShutdownRampLim(sid.getShutdownRampLim());

        //TODO-XXX: Finish these parameters.
        //setSchedule
        //setSChedule2
    }

    /**
     *
     * Put the 'actual', that is realtime computed dispatch for the GenCo.
     *
     * @param day actual day number
     * @param dispatchLevels dispatch for each hour, 0 to 23.
     */
    public void addActualDispatch(int day, double[] dispatchLevels) {
        dispatchByDay.put(day, dispatchLevels);
        setCommitment(dispatchLevels);
        computeProductionCost();
        //productionCostsByDay.put(day, Arrays.copyOf(hourlyTotalCost, hourlyTotalCost.length));
    }

    public void addDailyCosts(int day, double[] startCosts,
            double[] productionCosts, double[] shutdownCosts) {
        productionCostsByDay.put(day, Arrays.copyOf(productionCosts, productionCosts.length));
        startupCostsByDay.put(day, Arrays.copyOf(startCosts, startCosts.length));
        shutdownCostsByDay.put(day, Arrays.copyOf(shutdownCosts, shutdownCosts.length));
    }

    /**
     * TODO-X comment.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public double computeTotalCosts() {
        double cost = 0;

        //Startup costs
        //Production Costs
        //Shutdown Costs
        for (HashMap<Integer, double[]> costMap
                : Arrays.asList(
                        startupCostsByDay,
                        productionCostsByDay,
                        shutdownCostsByDay
                )) {
            for (double[] c : costMap.values()) {
                if (c == null) {
                    continue;
                }

                for (int i = 0; i < c.length; i++) {
                    cost += c[i];
                }
            }
        }

        return cost;
    }

    /**
     * Put the commitment for day d. This stores the commitment by day d, but
     * the values are generated by the SCUC/DAM on day d-1. In otherwords, if we
     * are running the DAM on day d, add commitment for d+1.
     *
     * @param d
     * @param commitment
     */
    public void addCommitmentForDay(int d, int[] commitment) {
        commitmentByDay.put(d, commitment);
    }

}
