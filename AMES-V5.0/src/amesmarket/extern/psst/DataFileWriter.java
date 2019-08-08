/*
 * FIXME: LICENSE
 */
package amesmarket.extern.psst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.CaseFileData;
import amesmarket.CaseFileData.StorageInputData;
import amesmarket.GenAgent;
import amesmarket.ISO;
import amesmarket.LSEAgent;
import amesmarket.NDGenAgent;
import amesmarket.StorageAgent;
import amesmarket.extern.common.CommitmentDecision;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * Write the data files used to transfer information over to the various coopr
 * programs.
 *
 * @author Sean L. Mooney
 *
 */
public class DataFileWriter {

    //TODO-XX: make sure everywhere that writes data is using the same
    //object, instead of hard coded paths.
    private final File scenDir = new File("SCUCresources/ScenarioData");

    /**
     *
     * @param fileObj
     * @param ames
     * @param day
     * @param LoadProfileLSE
     * @param GenProfileNDG
     * @param numIntervalsInSim
     * @throws AMESMarketException
     */
    public void writeScucScenDatFile(File fileObj, AMESMarket ames, int day, double[][] LoadProfileLSE, double[][] GenProfileNDG, int numIntervalsInSim) throws AMESMarketException {
        //set up all the elements we need.
        final int numHoursPerDay = ames.NUM_HOURS_PER_DAY;
//        final int numIntervalsInSim = ames.NUM_HOURS_PER_DAY_UC;
        final ISO iso = ames.getISO();
        final int numNodes = ames.getNumNodes();
        final double baseS = ames.getBaseS();
        final int numGenAgents = ames.getNumGenAgents();
        final int numLSEAgents = ames.getNumLSEAgents();
        final int numNDGAgents = ames.getNumNDGAgents();

        final double[][] branchIndex;
        final double[][] numBranchData;
        final double ReserveDownSystemPercent;
        final double ReserveUpSystemPercent;
        final double hasStorage;
        final double hasNDG;

        if (!ensureFileParentExists(fileObj)) {
            throw new AMESMarketException("Could not create the directory for " + fileObj.getPath());
        }

        numBranchData = ames.getBranchData();
        ReserveDownSystemPercent = ames.getReserveDownSystemPercent();
        ReserveUpSystemPercent = ames.getReserveUpSystemPercent();
        branchIndex = ames.getTransGrid().getBranchIndex();
        hasStorage = ames.gethasStorage();
        hasNDG = 0; //ames.gethasNDG();

        DecimalFormat Format = new DecimalFormat("###.####");
        
        //Now that we have all the parameters. Write it out.
        try {

            BufferedWriter refBufferWriter = new BufferedWriter(new FileWriter(fileObj));

            refBufferWriter.write("# Written by AMES per unit ");
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM/dd/yyyy HH:mm:ss\n\n");
            Date date = new Date();
            refBufferWriter.write(dateFormat.format(date));

            refBufferWriter.write("set StageSet := FirstStage SecondStage ;\n");
            refBufferWriter.write("\n");

            refBufferWriter
                    .write("set CommitmentTimeInStage[FirstStage] := 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 ;\n");
            refBufferWriter
                    .write("set CommitmentTimeInStage[SecondStage] := ;\n\n");

            refBufferWriter
                    .write("set GenerationTimeInStage[FirstStage] := ;\n");
            refBufferWriter
                    .write("set GenerationTimeInStage[SecondStage] := 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 ;\n\n");

            refBufferWriter.write("set Buses := ");

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("Bus" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set TransmissionLines :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write("Bus" + (int) branchIndex[i][0] + " Bus"
                        + (int) branchIndex[i][1] + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("param NumTransmissionLines := "
                    + branchIndex.length + " ;\n\n");

            refBufferWriter
                    .write("param: BusFrom BusTo ThermalLimit Reactance :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write((i + 1) + " Bus"
                        + (int) numBranchData[i][0] + " Bus"
                        + (int) numBranchData[i][1] + " " + numBranchData[i][2]
                        + " " + numBranchData[i][3] + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set ThermalGenerators := ");

            for (GenAgent gc : ames.getGenAgentList()) {
                refBufferWriter.write(gc.getID() + " ");
            }

            refBufferWriter.write(";\n\n");

            GenAgent[][] genNodeBusTable = new GenAgent[numNodes][numGenAgents];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numGenAgents; j++) {
                    GenAgent gen = (GenAgent) ames.getGenAgentList().get(j);
                    //System.out.println(gen.getAtNode());

                    if (gen.getAtNode() - 1 == i) {
                        genNodeBusTable[i][j] = gen;
                    } else {
                        genNodeBusTable[i][j] = null;
                    }
                }
            }

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("set ThermalGeneratorsAtBus[Bus"
                        + (i + 1) + "] := ");

                for (int j = 0; j < numGenAgents; j++) {
                    if (genNodeBusTable[i][j] != null) {
                        refBufferWriter.write(genNodeBusTable[i][j].getID() + " ");
                    }

                }

                refBufferWriter.write(" ;\n");
            }

            int BalPenPos = ames.getTestCaseConfig().BalPenPos ;
            int BalPenNeg = ames.getTestCaseConfig().BalPenNeg ;
            
            refBufferWriter.write("\nparam BalPenPos := " + BalPenPos
                    + " ;\n\n");
            
            refBufferWriter.write("\nparam BalPenNeg := " + BalPenNeg
                    + " ;\n\n");
            
            int TimePeriodLength = 1;
            refBufferWriter.write("\nparam TimePeriodLength := " + TimePeriodLength
                    + " ;\n\n");
            
            numIntervalsInSim = (numIntervalsInSim/TimePeriodLength);
                    
            refBufferWriter.write("\nparam NumTimePeriods := " + numIntervalsInSim
                    + " ;\n\n");
                        

            refBufferWriter
                    .write("param: PowerGeneratedT0 UnitOnT0State MinimumPowerOutput MaximumPowerOutput MinimumUpTime MinimumDownTime NominalRampUpLimit NominalRampDownLimit StartupRampLimit ShutdownRampLimit ColdStartHours ColdStartCost HotStartCost ShutdownCostCoefficient :=\n");

            for (GenAgent ga : ames.getGenAgentList()) {
                refBufferWriter.write(genAgentToSCUCDesc(ga, day, baseS, true));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter
                    .write("param: ID atBus EndPointSoc MaximumEnergy NominalRampDownInput NominalRampUpInput NominalRampDownOutput NominalRampUpOutput MaximumPowerInput MinimumPowerInput MaximumPowerOutput MinimumPowerOutput MinimumSoc EfficiencyEnergy :=\n");

            for (StorageAgent su : ames.getStorageAgentList()) {
                refBufferWriter.write(StorageAgentToFile(su, day, baseS));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter.write("param StorageFlag := "
                    + hasStorage + " ;\n\n");

            refBufferWriter.write("param ReserveDownSystemPercent := "
                    + ReserveDownSystemPercent + " ;\n\n");

            refBufferWriter.write("param ReserveUpSystemPercent := "
                    + ReserveUpSystemPercent + " ;\n\n");

            boolean HasZonalReserves = false;
            if(ames.getTestCaseConfig().NumberOfReserveZones > 1){
                HasZonalReserves = true;
            }            
            
            if(HasZonalReserves) {
            refBufferWriter.write("param HasZonalReserves := "
                    + HasZonalReserves + " ;\n\n");
            
            refBufferWriter.write("param NumberOfZones := "
                    + ames.getTestCaseConfig().NumberOfReserveZones + " ;\n\n");

            
            refBufferWriter.write("set Zones := ");

            for (int i = 0; i < ames.getTestCaseConfig().NumberOfReserveZones; i++) {
                refBufferWriter.write("Zone" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter
                    .write("param: Buses ReserveDownZonalPercent ReserveUpZonalPercent :=\n");

            for (String zName : ames.getTestCaseConfig().getZonalData().keySet()) {
                CaseFileData.ZonalData ZoneData = ames.getTestCaseConfig().getZonalData().get(zName);
                int[] buses = ZoneData.getBuses();
                String stemp = "";
                for (int i = 0; i < buses.length; i++) {
                    stemp = stemp + "Bus" + buses[i] + ",";
                }
                refBufferWriter.write(zName + " " + stemp + " " + ZoneData.getReserveDownZonalPercent() + " " + ZoneData.getReserveUpZonalPercent());
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(";\n\n");
            
            }

//            refBufferWriter.write("param: ReserveDownSystemPercent := \n");
//
//            for (int h = 0; h < numIntervalsInSim; h++) {
//                refBufferWriter.write((h + 1) + " " + ReserveDownSystemPercent + "\n");
//            }      
//            
//            refBufferWriter.write("; \n");
//            
//            refBufferWriter.write("param: ReserveUpSystemPercent := \n");
//
//            for (int h = 0; h < numIntervalsInSim; h++) {
//                refBufferWriter.write((h + 1) + " " + ReserveUpSystemPercent + "\n");
//            }
//
//            refBufferWriter.write("; \n");
            refBufferWriter.write("param: NetDemand :=\n");

            //System.out.println("LoadProfileLSE length: " + LoadProfileLSE[0].length);
            double[][] NetDemand = new double[numNodes][numIntervalsInSim];
            for (int n = 0; n < numNodes; n++) {
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if ((n+1) == lseNode) {
                        for (int h = 0; h < numIntervalsInSim; h++) {
                            NetDemand[n][h] = NetDemand[n][h] + LoadProfileLSE[i][h];
                        }
                    }
                }
                
                for (int i = 0; i < numNDGAgents; i++) {
                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
                    int ndgNode = ndg.getAtNode();
                    if ((n+1) == ndgNode) {
                        for (int h = 0; h < numIntervalsInSim; h++) {
                            //TODO: FIX TSO
//                            NetDemand[n][h] = NetDemand[n][h]; // - GenProfileNDG[i][h];
                            NetDemand[n][h] = NetDemand[n][h] - GenProfileNDG[i][h];
                        }
                    }
                }

                for (int h = 0; h < numIntervalsInSim; h++) {
                    refBufferWriter.write("Bus" + (n+1) + " " + (h + 1) + " "
                            + Format.format(NetDemand[n][h] / baseS) + "\n");
                }
                refBufferWriter.write("\n");
            }
            
            refBufferWriter.write("; \n\n");
            
//            refBufferWriter.write("param: Demand :=\n");
//
//            for (int i = 0; i < numLSEAgents; i++) {
//                LSEAgent lse = ames.getLSEAgentList().get(i);
//                int lseNode = lse.getAtNode();
//
//                for (int h = 0; h < numIntervalsInSim; h++) {
//                    //System.out.print(" h: "+ h + " LoadProfileLSE[i][h]: " + LoadProfileLSE[i][h]);
//                    refBufferWriter.write("Bus" + lseNode + " " + (h + 1) + " "
//                            + LoadProfileLSE[i][h] / baseS + "\n");
//                }
//
//                refBufferWriter.write("\n");
//            }
//
//            refBufferWriter.write("; \n\n");
//
//            if (hasNDG > 0) {
//                refBufferWriter.write("param: NDG :=\n");
//
//                for (int i = 0; i < numNDGAgents; i++) {
//                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
//                    int ndgNode = ndg.getAtNode();
//
//                    for (int h = 0; h < numIntervalsInSim; h++) {
//                        refBufferWriter.write("Bus" + ndgNode + " " + (h + 1) + " "
//                                + GenProfileNDG[i][h] / baseS + "\n");
//                    }
//
//                    refBufferWriter.write("\n");
//                }
//
//                refBufferWriter.write("; \n");
//
//            }

            refBufferWriter
                    .write("param: ProductionCostA0 ProductionCostA1 ProductionCostA2 NS :=\n");

            double[][] supplyOfferByGen = iso.getSupplyOfferByGen();
                
            final ArrayList<GenAgent> genagents = ames.getGenAgentList();
            for (int i = 0; i < numGenAgents; i++) {
                GenAgent ga = genagents.get(i);
                refBufferWriter.write(
                        ga.getID() + " "
                        + //ga.getSupplyOffer()[0] + " "
                        +ga.getNoLoadCost() + " "
                        + //FIXME: Is this supposed be part of the getSupplyOffer?
                        + (supplyOfferByGen[i][0] * baseS) + " "
                        + Format.format(supplyOfferByGen[i][1] * baseS * baseS) + " " 
                        + ga.getNS() + " " + "\n");
            }

            refBufferWriter.write("; \n");

            refBufferWriter.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the reference model.", e);
        }
    }

    /**
     *
     * @param fileObj
     * @param ames
     * @param min
     * @param day
     * @param LoadProfileLSE
     * @param numIntervalsInSim
     * @param GenProfileNDG
     * @throws AMESMarketException
     */
    public void writeScedScenDatFile(File fileObj, AMESMarket ames, int min, int interval, int hour, int day, double[][] LoadProfileLSE, double[][] GenProfileNDG, int IntervalSize, int numIntervalsInSim) throws AMESMarketException {
        //set up all the elements we need.
        // Important: Resolve the minimum up time and minimum down time values of the generators

        final int numHoursPerDay = ames.NUM_HOURS_PER_DAY;
//        final int numIntervalsInSim = ames.NUM_HOURS_PER_DAY_UC;
        final ISO iso = ames.getISO();
        final int numNodes = ames.getNumNodes();
        final double baseS = ames.getBaseS();
        final int numGenAgents = ames.getNumGenAgents();
        final int numLSEAgents = ames.getNumLSEAgents();
        final int numNDGAgents = ames.getNumNDGAgents();

        final double[][] branchIndex;
        final double[][] numBranchData;
        final double ReserveDownSystemPercent;
        final double ReserveUpSystemPercent;
        final double hasStorage;
        final double hasNDG;
        
        if (!ensureFileParentExists(fileObj)) {
            throw new AMESMarketException("Could not create the directory for " + fileObj.getPath());
        }

        numBranchData = ames.getBranchData();
        ReserveDownSystemPercent = ames.getReserveDownSystemPercent();
        ReserveUpSystemPercent = ames.getReserveUpSystemPercent();
        branchIndex = ames.getTransGrid().getBranchIndex();
        hasStorage = ames.gethasStorage();
        hasNDG = 0; //ames.gethasNDG();

        DecimalFormat Format = new DecimalFormat("###.####");
        
        //Now that we have all the parameters. Write it out.
        try {

            BufferedWriter refBufferWriter = new BufferedWriter(new FileWriter(fileObj));

            refBufferWriter.write("# Written by AMES per unit ");
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM/dd/yyyy HH:mm:ss\n\n");
            Date date = new Date();
            refBufferWriter.write(dateFormat.format(date));

            refBufferWriter.write("set StageSet := FirstStage SecondStage ;\n");
            refBufferWriter.write("\n");

            refBufferWriter
                    .write("set CommitmentTimeInStage[FirstStage] := 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 ;\n");
            refBufferWriter
                    .write("set CommitmentTimeInStage[SecondStage] := ;\n\n");

            refBufferWriter
                    .write("set GenerationTimeInStage[FirstStage] := ;\n");
            refBufferWriter
                    .write("set GenerationTimeInStage[SecondStage] := 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 ;\n\n");

            refBufferWriter.write("set Buses := ");

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("Bus" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set TransmissionLines :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write("Bus" + (int) branchIndex[i][0] + " Bus"
                        + (int) branchIndex[i][1] + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("param NumTransmissionLines := "
                    + branchIndex.length + " ;\n\n");

            refBufferWriter
                    .write("param: BusFrom BusTo ThermalLimit Reactance :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write((i + 1) + " Bus"
                        + (int) numBranchData[i][0] + " Bus"
                        + (int) numBranchData[i][1] + " " + numBranchData[i][2]
                        + " " + numBranchData[i][3] + "\n");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set ThermalGenerators := ");

            for (GenAgent gc : ames.getGenAgentList()) {
                refBufferWriter.write(gc.getID() + " ");
            }

            refBufferWriter.write(";\n\n");

            GenAgent[][] genNodeBusTable = new GenAgent[numNodes][numGenAgents];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numGenAgents; j++) {
                    GenAgent gen = (GenAgent) ames.getGenAgentList().get(j);
                    //System.out.println(gen.getAtNode());

                    if (gen.getAtNode() - 1 == i) {
                        genNodeBusTable[i][j] = gen;
                    } else {
                        genNodeBusTable[i][j] = null;
                    }
                }
            }

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("set ThermalGeneratorsAtBus[Bus"
                        + (i + 1) + "] := ");

                for (int j = 0; j < numGenAgents; j++) {
                    if (genNodeBusTable[i][j] != null) {
                        refBufferWriter.write(genNodeBusTable[i][j].getID() + " ");
                    }

                }

                refBufferWriter.write(" ;\n");
            }
            
            
            int BalPenPos = ames.getTestCaseConfig().BalPenPos ;
            int BalPenNeg = ames.getTestCaseConfig().BalPenNeg ;
            
            refBufferWriter.write("\nparam BalPenPos := " + BalPenPos
                    + " ;\n\n");
            
            refBufferWriter.write("\nparam BalPenNeg := " + BalPenNeg
                    + " ;\n\n");
            
            int TimePeriodLength = ames.getTestCaseConfig().RTDeltaT;
            refBufferWriter.write("\nparam TimePeriodLength := " + TimePeriodLength
                    + " ;\n\n");
            
            numIntervalsInSim = numIntervalsInSim/TimePeriodLength;
            
            refBufferWriter.write("\nparam NumTimePeriods := " + (numIntervalsInSim)
                    + " ;\n\n");
            //System.out.println("numIntervalsInSim: " + numIntervalsInSim);


            refBufferWriter.write("\nparam Interval := " + interval
                    + " ;\n\n");

            refBufferWriter
                    .write("param: PowerGeneratedT0 UnitOnT0State MinimumPowerOutput MaximumPowerOutput MinimumUpTime MinimumDownTime NominalRampUpLimit NominalRampDownLimit StartupRampLimit ShutdownRampLimit ColdStartHours ColdStartCost HotStartCost ShutdownCostCoefficient :=\n");

            for (GenAgent ga : ames.getGenAgentList()) {
                refBufferWriter.write(genAgentToSCEDDesc(ames.getTestCaseConfig().RTDeltaT, ga, hour, baseS, false));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter
                    .write("param: ID atBus EndPointSoc MaximumEnergy NominalRampDownInput NominalRampUpInput NominalRampDownOutput NominalRampUpOutput MaximumPowerInput MinimumPowerInput MaximumPowerOutput MinimumPowerOutput MinimumSoc EfficiencyEnergy :=\n");

            for (StorageAgent su : ames.getStorageAgentList()) {
                refBufferWriter.write(StorageAgentToFile(su, day, baseS));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter.write("param StorageFlag := "
                    + hasStorage + " ;\n\n");

            refBufferWriter.write("param ReserveDownSystemPercent := "
                    + ReserveDownSystemPercent + " ;\n\n");

            refBufferWriter.write("param ReserveUpSystemPercent := "
                    + ReserveUpSystemPercent + " ;\n\n");

            boolean HasZonalReserves = false;
            if(ames.getTestCaseConfig().NumberOfReserveZones > 1){
                HasZonalReserves = true;
            }
            
            if(HasZonalReserves){
            refBufferWriter.write("param HasZonalReserves := "
                    + HasZonalReserves + " ;\n\n");
            refBufferWriter.write("param NumberOfZones := "
                    + ames.getTestCaseConfig().NumberOfReserveZones + " ;\n\n");

 
            refBufferWriter.write("set Zones := ");

            for (int i = 0; i < ames.getTestCaseConfig().NumberOfReserveZones; i++) {
                refBufferWriter.write("Zone" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter
                    .write("param: Buses ReserveDownZonalPercent ReserveUpZonalPercent :=\n");

            for (String zName : ames.getTestCaseConfig().getZonalData().keySet()) {
                CaseFileData.ZonalData ZoneData = ames.getTestCaseConfig().getZonalData().get(zName);
                int[] buses = ZoneData.getBuses();
                String stemp = "";
                for (int i = 0; i < buses.length; i++) {
                    stemp = stemp + "Bus" + buses[i] + ",";
                }
                refBufferWriter.write(zName + " " + stemp + " " + ZoneData.getReserveDownZonalPercent() + " " + ZoneData.getReserveUpZonalPercent());
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(";\n\n");

            }
            
//            // Should update reserve requirements by minute
//            refBufferWriter.write("param: ReserveDownSystemPercent := \n");
//
//            for (int h = 0; h < numIntervalsInSim; h++) {
//                refBufferWriter.write((h + 1) + " " + ReserveDownSystemPercent + "\n");
//            }      
//            
//            refBufferWriter.write("param: ReserveUpSystemPercent := \n");
//
//            for (int h = 0; h < numIntervalsInSim; h++) {
//                refBufferWriter.write((h + 1) + " " + ReserveUpSystemPercent + "\n");
//            }
//
//            refBufferWriter.write("; \n");
            refBufferWriter.write("param: NetDemand :=\n");

            double[][] NetDemand = new double[numNodes][numIntervalsInSim];
            for (int n = 0; n < numNodes; n++) {
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if ((n+1) == lseNode) {
                        for (int h = 0; h < numIntervalsInSim; h++) {
                            //TODO: FIX TSO
//                            NetDemand[n][h] = NetDemand[n][h]; // + LoadProfileLSE[i][min+h];
                            NetDemand[n][h] = NetDemand[n][h] + LoadProfileLSE[i][min+h];
                        }
                    }
                }
                
                for (int i = 0; i < numNDGAgents; i++) {
                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
                    int ndgNode = ndg.getAtNode();
                    if ((n+1) == ndgNode) {
                        for (int h = 0; h < numIntervalsInSim; h++) {
                            //TODO: FIX TSO
//                            NetDemand[n][h] = NetDemand[n][h]; // - GenProfileNDG[i][min+h];
                            NetDemand[n][h] = NetDemand[n][h] - GenProfileNDG[i][min+h];
                        }
                    }
                }

                for (int h = 0; h < numIntervalsInSim; h++) {
                    refBufferWriter.write("Bus" + (n+1) + " " + (min+h + 1) + " "
                            + Format.format(NetDemand[n][h] / baseS) + "\n");
                }
                refBufferWriter.write("\n");
            }
            
            refBufferWriter.write("; \n\n");
            
//            refBufferWriter.write("param: Demand :=\n");
//
//            for (int i = 0; i < numLSEAgents; i++) {
//                LSEAgent lse = ames.getLSEAgentList().get(i);
//                int lseNode = lse.getAtNode();
//
//                for (int h = 0; h < numIntervalsInSim; h++) {
//                    //System.out.println("min+h: "+ (min+h));
//                    refBufferWriter.write("Bus" + lseNode + " " + (min + h + 1) + " "
//                            + LoadProfileLSE[i][min + h] / baseS + "\n");
//                }
//
//                refBufferWriter.write("\n");
//            }
//
//            refBufferWriter.write("; \n");
//            if (hasNDG > 0) {
//                refBufferWriter.write("param: NDG :=\n");
//
//                for (int i = 0; i < numNDGAgents; i++) {
//                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
//                    int ndgNode = ndg.getAtNode();
//
//                    for (int h = 0; h < numIntervalsInSim; h++) {
//                        refBufferWriter.write("Bus" + ndgNode + " " + (min + h + 1) + " "
//                                + GenProfileNDG[i][min + h] / baseS + "\n");
//                    }
//
//                    refBufferWriter.write("\n");
//                }
//
//                refBufferWriter.write("; \n");
//
//            }

            refBufferWriter
                    .write("param: ProductionCostA0 ProductionCostA1 ProductionCostA2 NS :=\n");

            double[][] supplyOfferByGen = iso.getSupplyOfferByGenRT(); //iso.getSupplyOfferByGen();

            final ArrayList<GenAgent> genagents = ames.getGenAgentList();
            for (int i = 0; i < numGenAgents; i++) {
                GenAgent ga = genagents.get(i);
                refBufferWriter.write(
                          ga.getID() + " "
                        + //ga.getSupplyOffer()[0] + " "
                        + ga.getNoLoadCost() + " "
                        + //FIXME: Is this supposed be part of the getSupplyOffer?
                        + (supplyOfferByGen[i][0] * baseS * 1/60) + " "
                        + Format.format(supplyOfferByGen[i][1] * baseS * baseS * 1/60) + " " 
                        + ga.getNS() + " " + "\n");
            }

            refBufferWriter.write("; \n");

            refBufferWriter.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the reference model.", e);
        }
    }

    /**
     * Get a string to write into the SCUC input file describing the genco.
     *
     * @param ga
     * @param baseS
     * @return a string with all of the parameters, or an empty string if the ga
     * parameter is null.
     */
    private String genAgentToSCUCDesc(GenAgent ga, int day, double baseS, boolean scuctype) {
        if (ga == null) {
            return "";
        }
        
        DecimalFormat Format = new DecimalFormat("###.####");
        
        //do all the conversions
        double powerT0 = Math.round(ga.getPowerT0NextDay() / baseS); 
        double capMin = ga.getCapacityMin() / baseS;
        double capMax = ga.getCapacityMax() / baseS;
        double nominalUp;
        double nominalDown;
        double startupramplimit;
        double shutdownramplimit;

        if (scuctype == true) {
            // DAM SCUC : Units MW/hour
            nominalUp = ga.getNominalRampUpLim() / baseS;
            nominalDown = ga.getNominalRampDownLim() / baseS;
            startupramplimit = ga.getStartupRampLim() / baseS;
            shutdownramplimit = ga.getShutdownRampLim() / baseS;
        } else {
            // RTM SCED : Converting into MW/min
            nominalUp = ga.getNominalRampUpLim() / (baseS * 60);
            nominalDown = ga.getNominalRampDownLim() / (baseS * 60);
            startupramplimit = ga.getStartupRampLim() / (baseS * 60);
            shutdownramplimit = ga.getShutdownRampLim() / (baseS * 60);
        }
        
        int coldstarthours = ga.getColdStartUpHours();
        double coldstartupcost = ga.getColdStartUpCost();
        double hotstartupcost = ga.getHotStartUpCost();
        double shutdowncost = ga.getShutDownCost();

        //some rounding checks
        if (powerT0 < capMin && ga.getUnitOnStateT0NextDay() > 0) { 
            System.err.println("SCUC Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " is less than capMin of " + capMin
                    + ". Adjusting to " + capMin
            );
            powerT0 = capMin;
        } else if (powerT0 > capMax) {
            System.err.println("Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " excedes the capMax value of " + capMax
                    + ". Adjusting to " + capMax
            );
            powerT0 = capMax;
        }
        // System.out.print(ga.getMinUpTime());
        return String.format(
                // 1     2          3       4       5       6       7       8               9               10          11
                //Name, powerTO, On/OffT0, MinPow, MaxPow, MinUp, MinDown, NominalRampUP, NominalRampDown, StartupLim, ShutdownLim, ColdStartHours, ColdStartupCost, HotStartupCost, ShutDownCost
                "%1$s %2$f %3$d %4$f %5$f %6$d %7$d %8$f %9$f %10$f %11$f %12$d %13$f %14$f %15$f" //TODO-XX Decimal precision.
                ,
                 ga.getID() //1
                ,
                 powerT0 //2
                ,
                 ga.getUnitOnT0State(day - 1) //ga.getUnitOnStateT0NextDay() // ga.getUnitOnT0State(day - 1) //3
                ,
                 capMin //4
                ,
                 capMax//5
                ,
                 ga.getMinUpTime() //6
                ,
                 ga.getMinDownTime() //7
                ,
                 nominalUp //8
                ,
                 nominalDown//9
                ,
                 startupramplimit //10
                ,
                 shutdownramplimit //11
                ,
                 coldstarthours //12
                ,
                 coldstartupcost //13
                ,
                 hotstartupcost //14
                ,
                 shutdowncost //15
        );
    }

    private String genAgentToSCEDDesc(int RTDeltaT, GenAgent ga, int hour, double baseS, boolean scuctype) {
        if (ga == null) {
            return "";
        }
        
        DecimalFormat Format = new DecimalFormat("###.####");
        
        //do all the conversions
        double powerT0 = ga.getPowerPrevInterval() / baseS; 
        double capMin = ga.getCapacityMin() / baseS;
        double capMax = ga.getCapacityMax() / baseS;
        double nominalUp;
        double nominalDown;
        double startupramplimit;
        double shutdownramplimit;

        
        // RTM SCED : scaled ramp up and down limits (MW)
        nominalUp = (ga.getNominalRampUpLim() * RTDeltaT) / (baseS * 60);
        nominalDown = (ga.getNominalRampDownLim()* RTDeltaT) / (baseS * 60);
        startupramplimit = (ga.getStartupRampLim()* RTDeltaT) / (baseS * 60);
        shutdownramplimit = (ga.getShutdownRampLim()* RTDeltaT) / (baseS * 60);
        
        int coldstarthours = ga.getColdStartUpHours();
        double coldstartupcost = ga.getColdStartUpCost();
        double hotstartupcost = ga.getHotStartUpCost();
        double shutdowncost = ga.getShutDownCost();

        //some rounding checks
        if (powerT0 < capMin && ga.getCommitmentStatus(hour - 1) > 0) { 
            System.err.println("SCED Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " is less than capMin of " + capMin
                    + ". Adjusting to " + capMin
            );
            powerT0 = capMin;
        } else if (powerT0 > capMax) {
            System.err.println("Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " excedes the capMax value of " + capMax
                    + ". Adjusting to " + capMax
            );
            powerT0 = capMax;
        }
        //System.out.println("");
        //System.out.print(""+ga.getID()+" "+powerT0+" ");
        return String.format(
                // 1     2          3       4       5       6       7       8               9               10          11
                //Name, powerTO, On/OffT0, MinPow, MaxPow, MinUp, MinDown, NominalRampUP, NominalRampDown, StartupLim, ShutdownLim, ColdStartHours, ColdStartupCost, HotStartupCost, ShutDownCost
                "%1$s %2$f %3$d %4$f %5$f %6$d %7$d %8$f %9$f %10$f %11$f %12$d %13$f %14$f %15$f" //TODO-XX Decimal precision.
                ,
                 ga.getID() //1
                ,
                 powerT0 //2
                ,
                 ga.getCommitmentStatus(hour - 1) //3
                ,
                 capMin //4
                ,
                 capMax//5
                ,
                 ga.getMinUpTime() //6
                ,
                 ga.getMinDownTime() //7
                ,
                 nominalUp //8
                ,
                 nominalDown//9
                ,
                 startupramplimit //10
                ,
                 shutdownramplimit //11
                ,
                 coldstarthours //12
                ,
                 coldstartupcost //13
                ,
                 hotstartupcost //14
                ,
                 shutdowncost //15
        );
    }

    /**
     * Get a string to write into the Storage input file describing the Storage
     * Unit.
     *
     * @param su
     * @param baseS
     * @return a string with all of the parameters, or an empty string if the ga
     * parameter is null.
     */
    private String StorageAgentToFile(StorageAgent sa, int day, double baseS) {
        if (sa == null) {
            return "";
        }

        StorageInputData Data = sa.getData();

        //do all the conversions
        double ID = Data.getID();
        double atBus = Data.getatBus();
        double EndPointSoc = Data.getEndPointSoc();
        double MaxEnergy = Data.getMaximumEnergy();
        double NomRampDownInput = Data.getNominalRampDownInput();
        double NomRampUpInput = Data.getNominalRampDownInput();
        double NomRampDownOutput = Data.getNominalRampDownOutput();
        double NomRampUpOutput = Data.getNominalRampUpOutput();
        double MaxPowInput = Data.getMaximumPowerInput();
        double MinPowInput = Data.getMinimumPowerInput();
        double MaxPowOutput = Data.getMaximumPowerOutput();
        double MinPowOutput = Data.getMinimumPowerOutput();
        double MinimumSoc = Data.getMinimumSoc();
        double EfficiencyEnergy = Data.getEfficiencyEnergy();

        // System.out.print(ga.getMinUpTime());
        return String.format(
                // 1     2          3       4       5       6       7       8               9               10          11
                // ID, atBus EndPointSoc MaxEnergy NomRampDownInput NomRampUpInput NomRampDownOutput NomRampUpOutput MaxPowInput MinPowInput MaxPowOutput  MinPowOutput MinimumSoc EfficiencyEnergy
                "%1$s %2$f %3$f %4$f %5$f %6$f %7$f %8$f %9$f %10$f %11$f %12$f %13$f %14$f " //TODO-XX Decimal precision.
                ,
                 ID //1
                ,
                 atBus //2
                ,
                 EndPointSoc //3
                ,
                 MaxEnergy //4
                ,
                 NomRampDownInput//5
                ,
                 NomRampUpInput //6
                ,
                 NomRampDownOutput //7
                ,
                 NomRampUpOutput //8
                ,
                 MaxPowInput//9
                ,
                 MinPowInput //10
                ,
                 MaxPowOutput //11
                ,
                 MinPowOutput //12
                ,
                 MinimumSoc //13
                ,
                 EfficiencyEnergy //14
        );
    }

    /**
     * Write out the generator commitments for the sced.
     *
     * @param ames
     * @param TAU length of interval to write the commitments
     * @param m beginning of the minute interval
     * @param h corresponding hour of the interval
     * @param gencoCommitments not null
     * @param ucVectorFile file to write the information into.
     * @throws AMESMarketException
     * @throws IllegalArgumentException if gencoCommitments is null.
     */
    public void writeGenCommitments(AMESMarket ames, int TAU, int m, int h, List<CommitmentDecision> gencoCommitments, File ucVectorFile) throws AMESMarketException {
        
        TAU = (int) (TAU/ames.getTestCaseConfig().RTDeltaT);
        if (gencoCommitments == null) {
            throw new IllegalArgumentException();
        }

        if (!ensureFileParentExists(ucVectorFile)) {
            throw new AMESMarketException("Could not create the directory for " + ucVectorFile.getPath());
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(ucVectorFile));

            final String eol = System.getProperty("line.separator");
            final String indent = "\t";
            //strings for whether or not the unit is committed.
            final String ucOn = "1";
            final String ucOff = "0";

            for (CommitmentDecision cd : gencoCommitments) {
                out.println(cd.generatorName); //FIXME: ?BUG?
                StringBuilder sb = new StringBuilder();

                int[] commitmentVector = new int[TAU]; 
                for (int k = 0; k < TAU; k++) {
                    commitmentVector[k] = cd.commitmentDecisions[h - 1];
                }
                //Boolean[] commitmentVector = gencoCommitments.get(g);
                if (commitmentVector == null) { //yes, I'm being very cautious.
                    System.err.println("[Warning External SCED] No commit vector for " + cd.generatorName);
                    continue;
                }

                for (int b : commitmentVector) {
                    sb.append(indent);
                    sb.append((b == 1 ? ucOn : ucOff)); //convert boolean to the format the external SCED expects.
                    sb.append(eol);
                }
                out.print(sb.toString());
            }

            out.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the generator commitment schedule.", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private boolean ensureFileParentExists(File f) {
        File parent = f.getParentFile();
        if (parent == null) {
            return true; //no parent file. nothing to be done
        } else if (parent.exists()) {
            return true; //nothing to be done.
        } else {
            parent.mkdirs(); //try and make it. return whether or not it exists.
            return parent.exists();
        }
    }
}
