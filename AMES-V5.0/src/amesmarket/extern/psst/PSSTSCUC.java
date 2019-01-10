/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amesmarket.extern.psst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.GenAgent;
import amesmarket.ISO;
//import amesmarket.LoadCaseControl;
//import amesmarket.LoadProfileCollection;
import amesmarket.SCUC;
import amesmarket.Support;
import amesmarket.extern.common.CommitmentDecision;
import amesmarket.filereaders.BadDataFileFormatException;

/**
 *
 * @author Dheepak Krishnamurthy
 */
public class PSSTSCUC implements SCUC {

    /**
     * Whether or not to delete the files created.
     */
    private final boolean deleteFiles;

    private final AMESMarket ames;
    private final ISO iso;
    private List<CommitmentDecision> genSchedule;
    private double[][] DAMLMP;
    private double[][] GenDAMSchedule;
    private int[][] GenDAMCommitmentStatusPresentDay;
    private int[][] GenDAMCommitmentStatusNextDay;
    private final int numGenAgents, numLSEAgents, numHours, numIntervals; // numGenAgents;

    private final File python_Input, DAMLMPDataFile, referenceFile, pyomoSolPrint, runefSolPrint, referenceModelDir,
            scenarioModelDir;

    //TODO-X: make pyomo/cplex user settable, somewhere.
    //Probably an option in the TestCase file.
    /**
     * Name/path to psst program.
     */
    private final File psstProg;

    /**
     * Configuration for external coopr call. Determines if a deterministic or
     * stochastic scuc is written.
     */
    private final PSSTConfig PSSTExt;

    public PSSTSCUC(ISO independentSystemOperator, AMESMarket model) {
        this.ames = model;
        this.iso = independentSystemOperator;
        this.numGenAgents = this.ames.getNumGenAgents();
        this.numLSEAgents = this.ames.getNumLSEAgents();
        this.numHours = this.ames.NUM_HOURS_PER_DAY;
        this.numIntervals = this.ames.NUM_HOURS_PER_DAY_UC;
        //final int scucType = model.getSCUCType(); //uncommented for future use, in case stocastic opt is used
        //final int scucType = SCUC_DETERM;
        this.GenDAMCommitmentStatusNextDay = new int[this.numHours][this.numGenAgents];
        this.GenDAMCommitmentStatusPresentDay = new int[this.numHours][this.numGenAgents];
        //genSchedule=new int[numGenAgents][numHoursPerDay];
        this.python_Input = new File("xfertoames.dat");
        this.referenceModelDir = new File("SCUCresources/Models");
        this.scenarioModelDir = new File("SCUCresources/ScenarioData");
        this.referenceFile = new File(this.scenarioModelDir, "ReferenceModel.dat");
        this.pyomoSolPrint = new File("SCUCresources/pyomosolprint.py");
        this.runefSolPrint = new File("SCUCresources/runefsolprint.py");
        //referenceModel=new File("SCUCresources/Models/ReferenceModel.py");
        this.DAMLMPDataFile = new File("DAMLMP.dat");

        //switch(scucType) {
        //case SCUC_STOC :
        //	//Run the External SCUC.
        //	System.out.print("Running Stochastic SCUC with external call to PSST ");
        //	this.PSSTExt = PSSTConfig.createStochasticPSST(this.referenceModelDir, this.scenarioModelDir, "runefsolprint");
        //	break;
        //case SCUC_DETERM :
        System.out.print("Running Deterministic SCUC with external call to PSST ");
        this.PSSTExt = PSSTConfig.createDeterministicPSST(new File(this.referenceModelDir, "ReferenceModel.py"),
                this.referenceFile);
        //	break;
        //default :
        //	throw new IllegalArgumentException("Unknown SCUC type");
        //}

        //set up the run paths of pyomo and cplex
        this.psstProg = Support.findExecutableInPath("psst");

        this.deleteFiles = model.isDeleteIntermediateFiles();
    }

    /* (non-Javadoc)
	 * @see amesmarket.extern.coopr.SCUC#calcSchedule(int)
     */
    @Override
    public void calcSchedule(int day) throws IOException, AMESMarketException, BadDataFileFormatException {

        String strTemp;

//        //Write out the configuration for the pyomo model.
//        double[][] loadProfileLSE = this.iso.getLoadProfileByLSE();
//        // double[][] nextDayLoadProfileLSE;
//        double[][] loadProfileLSEALL = null;
//
//        /*
//		if(day<this.ames.DAY_MAX){
//			nextDayLoadProfileLSE = this.iso.getNextDayLoadProfileByLSE();
//			loadProfileLSEALL = new double[loadProfileLSE.length][loadProfileLSE[0].length + nextDayLoadProfileLSE[0].length];
//			for (int i=0; i < loadProfileLSE.length; i++) {
//				System.arraycopy(loadProfileLSE[i], 0, loadProfileLSEALL[i], 0, loadProfileLSE[i].length);
//				System.arraycopy(nextDayLoadProfileLSE[i], 0, loadProfileLSEALL[i], loadProfileLSE[i].length, nextDayLoadProfileLSE[i].length);
//			}
//		} else {
//         */ loadProfileLSEALL = new double[loadProfileLSE.length][loadProfileLSE[0].length + loadProfileLSE[0].length];
//
//        for (int i = 0; i < loadProfileLSE.length; i++) {
//            System.arraycopy(loadProfileLSE[i], 0, loadProfileLSEALL[i], 0, loadProfileLSE[i].length);
//        }
//        //}
        //Write out the configuration for the pyomo model.
        double[][] loadProfileLSE = this.iso.getLoadProfileByLSE();
        double[][] nextDayLoadProfileLSE;
        double[][] loadProfileLSEALL = null;

        //System.out.println(" day: " + day + " DAY_MAX: " + this.ames.DAY_MAX);
        if (day < this.ames.DAY_MAX) {
            nextDayLoadProfileLSE = this.iso.getLoadProfileByLSE(); // TODO:Swathi - replace it with - this.iso.getNextDayLoadProfileByLSE();
          
            loadProfileLSEALL = new double[loadProfileLSE.length][loadProfileLSE[0].length + nextDayLoadProfileLSE[0].length];
            for (int i = 0; i < loadProfileLSE.length; i++) {
                System.arraycopy(loadProfileLSE[i], 0, loadProfileLSEALL[i], 0, loadProfileLSE[i].length);
                System.arraycopy(nextDayLoadProfileLSE[i], 0, loadProfileLSEALL[i], loadProfileLSE[i].length, nextDayLoadProfileLSE[i].length);
            }
        } else {
            loadProfileLSEALL = new double[loadProfileLSE.length][loadProfileLSE[0].length + loadProfileLSE[0].length];
            for (int i = 0; i < loadProfileLSE.length; i++) {
                System.arraycopy(loadProfileLSE[i], 0, loadProfileLSEALL[i], 0, loadProfileLSE[i].length);
            }
            
        }

        DataFileWriter dfw = new DataFileWriter();
//        for (int i = 0; i < ames.getNumLSEAgents(); i++) {
//            for (int j = 0; j < loadProfileLSEALL[0].length; j++) {
//                System.out.print(" - " + loadProfileLSEALL[i][j]);
//            }
//            System.out.println("");
//        }
        dfw.writeScenDatFile(this.referenceFile, this.ames, day, loadProfileLSEALL, this.ames.NUM_HOURS_PER_DAY_UC);
        //dfw.writeScenDatFile(this.referenceFile, this.ames, day, loadProfileLSEALL, this.ames.NUM_HOURS_PER_DAY); //Rohit's version

        /*
		LoadCaseControl loadCaseControl = this.ames.getLoadScenarioProvider().getLoadCaseControl();

		double[] scenProb=new double[loadCaseControl.getNumLoadScenarios()];
		for (int i=0;i<loadCaseControl.getNumLoadScenarios();i++){
			scenProb[i]=loadCaseControl.getScenarioProbability(day, i+1);
		}
		dfw.writeScenarioStructures(loadCaseControl.getNumLoadScenarios(),scenProb);

		double[][] scenarioLoadProfileLSE=new double[this.numLSEAgents][this.numHours];
		double[][] nextDayScenarioLoadProfileLSE=new double[this.numLSEAgents][this.numHours];
		double[][] scenarioLoadProfileLSEALL=new double[this.numLSEAgents][this.numHours+this.numHours];

		for (int i = 0; i < loadCaseControl.getNumLoadScenarios(); i++) {
			LoadProfileCollection scenario = loadCaseControl.getLoadScenario(i + 1);

			for (int j = 0; j < this.numLSEAgents; j++) {
				for (int k = 0; k < this.numHours; k++) {
					scenarioLoadProfileLSE[j][k] = scenario.get(day)
							.getLoadByHour(k)[j];
				}

			}

			if(day<this.ames.DAY_MAX){

				for (int j = 0; j < this.numLSEAgents; j++) {
					for (int k = 0; k < this.numHours; k++) {
						nextDayScenarioLoadProfileLSE[j][k] = scenario.get(day+1)
								.getLoadByHour(k)[j];
					}

				}

				for (int k=0; k < loadProfileLSE.length; k++) {
					System.arraycopy(scenarioLoadProfileLSE[k], 0, scenarioLoadProfileLSEALL[k], 0, scenarioLoadProfileLSE[k].length);
					System.arraycopy(nextDayScenarioLoadProfileLSE[k], 0, scenarioLoadProfileLSEALL[k], scenarioLoadProfileLSE[k].length, nextDayScenarioLoadProfileLSE[k].length);
				}
			} else {
				for (int k=0; k < loadProfileLSE.length; k++) {
					System.arraycopy(scenarioLoadProfileLSE[k], 0, scenarioLoadProfileLSEALL[k], 0, scenarioLoadProfileLSE[k].length);
				}
			}


			File fileObj = new File("SCUCresources/ScenarioData/Scen" + (i + 1)
					+ ".dat");
			//dfw.writeScenDatFile(fileObj, this.ames, day, scenarioLoadProfileLSEALL, this.ames.NUM_HOURS_PER_DAY_UC);
                        dfw.writeScenDatFile(fileObj, this.ames, day, scenarioLoadProfileLSEALL, this.ames.NUM_HOURS_PER_DAY); // changed in ames 5.0
		}
         */
        this.syscall(this.PSSTExt);

        //Read the data file back in to get the GenCo commitments.
        if (!this.python_Input.exists()) {
            throw new BadDataFileFormatException(new FileNotFoundException(
                    this.python_Input.getPath()));
        }

        System.out.println("Reading GenCo schedule from " + this.python_Input.getPath());
        java.util.Scanner raf = new Scanner(this.python_Input);

        //Read the results from the external scuc back in.
        //AMESMarket.LOGGER.log(Level.FINER, "Reading GenCo schedule.");
        this.genSchedule = new ArrayList<CommitmentDecision>();
        this.GenDAMCommitmentStatusPresentDay = new int[this.numHours][this.numGenAgents];
        System.out.println("len: " + this.GenDAMCommitmentStatusNextDay.length);
        for (int j = 0; j < this.numGenAgents; j++) {
            for (int hour = 0; hour < this.ames.NUM_HOURS_PER_DAY; hour++) {
                GenDAMCommitmentStatusPresentDay[hour][j] = GenDAMCommitmentStatusNextDay[hour][j];
                //System.out.print(" v: " + GenDAMCommitmentStatusNextDay[hour][j]);
            }
            //System.out.println("");
        }
        this.GenDAMCommitmentStatusNextDay = new int[this.numHours][this.numGenAgents];
        this.GenDAMSchedule = new double[this.numHours][this.numGenAgents];
        for (int j = 0; j < this.numGenAgents; j++) {
            //System.out.print("j: " + j + " ");
            int[][] schedule = new int[this.numHours][this.ames.NUM_INTERVALS_PER_HOUR * this.ames.M];
            int i = 0;
            String genCoMarker = raf.nextLine().trim();
            //System.out.println("genCoMarker: "+genCoMarker);
            GenAgent gc = this.ames.getGenAgentByName(genCoMarker);
            //System.out.println("gc: "+gc);
            if (gc == null) {
                throw new BadDataFileFormatException("Unknown GenAgent, "
                        + genCoMarker + ", in SCUC results");
            }

            while (i < this.numHours) {
                strTemp = raf.nextLine();
                //System.out.println("strTemp: "+strTemp);
                if (strTemp == null) {
                    throw new BadDataFileFormatException(
                            "No schedule for " + gc.getID() + " hour " + i);
                }

                int iIndex = strTemp.indexOf(" ");
                //System.out.println("iIndex: "+ iIndex);
                String delims = "[ ]+";
                String[] values = strTemp.split(delims);
//				for (int m = 0; m < values.length; m++)
//                                    System.out.print(" m: "+ m + " "+ values[m]);
//                                System.out.println("");
                // Assigning 1's throughout the hour interval as of now
                for (int k = 0; k < (this.ames.NUM_INTERVALS_PER_HOUR * this.ames.M); k++) {
                    schedule[i][k] = Integer.parseInt(strTemp.substring(iIndex + 1, iIndex + 2));
                }
                //System.out.println("Double.parseDouble(values[1]): "+ Double.parseDouble(values[2]));
                GenDAMSchedule[i][j] = Double.parseDouble(values[2]) * ames.getBaseS();
                GenDAMCommitmentStatusNextDay[i][j] = Integer.parseInt(values[1]);
                i++;

                double temp1 = Double.parseDouble(values[2]) * ames.getBaseS();
                int temp2 = Integer.parseInt(values[1]);
                gc.setPowerT0NextDay(temp1);
                gc.setUnitOnStateT0NextDay(temp2);
            }

            // reading the 25th hour gen sechule
//            strTemp = raf.nextLine();
//            String delims = "[ ]+";
//            String[] values = strTemp.split(delims);
//            double temp1 = Double.parseDouble(values[2]) * ames.getBaseS();
//            int temp2 = Integer.parseInt(values[1]);
//            //System.out.println("temp: "+temp);
//            gc.setPowerT0NextDay(temp1);
//            gc.setUnitOnStateT0NextDay(temp2);
//            i++;
            while (i < this.ames.NUM_HOURS_PER_DAY_UC) //this.numIntervals) //needs to be changed for rolling horizon
            {
                strTemp = raf.nextLine();
                //System.out.println("check strTemp: " + strTemp + " i: " + i);
                i++;
            }
            //System.out.println("checking.."+gc.getID()+gc.getIndex());
            this.genSchedule.add(new CommitmentDecision(gc.getID(), gc.getIndex(), schedule));
        }

        //Sort the collection by array index. Keeps the list
        //in the 'expected' order for ames. The output from the external
        //solver is sorted by name, which means GenCo10 comes after GenCo1
        Collections.sort(this.genSchedule, new Comparator<CommitmentDecision>() {
            @Override
            public int compare(CommitmentDecision o1, CommitmentDecision o2) {
                if (o1.generatorIdx < o2.generatorIdx) {
                    return -1;
                }
                if (o1.generatorIdx == o2.generatorIdx) {
                    return 0;
                }
                return 1;
            }
        });

        raf.close();

        this.cleanup();
        //END Read in GenCo commitments

        //Read the data file back in to get the GenCo commitments.
        if (!this.DAMLMPDataFile.exists()) {
            throw new BadDataFileFormatException(new FileNotFoundException(
                    this.DAMLMPDataFile.getPath()));
        }

        System.out.println("Reading DAM LMP data from " + this.DAMLMPDataFile.getPath());
        java.util.Scanner ReadLMPFile = new Scanner(this.DAMLMPDataFile);

        //Read the results from the external scuc back in.
        //AMESMarket.LOGGER.log(Level.FINER, "Reading GenCo schedule.");
        this.DAMLMP = new double[this.numHours][ames.getNumNodes()];
        String LMPMarker = ReadLMPFile.nextLine().trim();
        if (LMPMarker == null) {
            throw new BadDataFileFormatException("Unknown, "
                    + LMPMarker + ", in SCUC results");
        } else {
            System.out.println("strTemp: " + LMPMarker);
        }
        for (int j = 0; j < this.numHours; j++) {
            System.out.print("j: " + j + " ");
            //double[] LMP = new double[this.numHours][this.ames.NUM_INTERVALS_PER_HOUR * this.ames.M];
            int i = 0;

            while (i < ames.getNumNodes()) {
                strTemp = ReadLMPFile.nextLine();
                //System.out.print("strTemp: "+strTemp);
                String[] data = strTemp.split(":");
                double lmp = Double.parseDouble(data[2]) / ames.getBaseS();
                System.out.print(" " + lmp);
                this.DAMLMP[j][i] = lmp;
                i++;
            }

            System.out.println(" ");
        }
        System.out.println("");
        //Sort the collection by array index. Keeps the list
        //in the 'expected' order for ames. The output from the external
        //solver is sorted by name, which means GenCo10 comes after GenCo1
        Collections.sort(this.genSchedule, new Comparator<CommitmentDecision>() {
            @Override
            public int compare(CommitmentDecision o1, CommitmentDecision o2) {
                if (o1.generatorIdx < o2.generatorIdx) {
                    return -1;
                }
                if (o1.generatorIdx == o2.generatorIdx) {
                    return 0;
                }
                return 1;
            }
        });

        raf.close();
    }

    public void syscall(PSSTConfig runefConfig) throws IOException {
        Process p = runefConfig.createPSSTProcess();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        String s = null;
        System.out.println("Here is the standard output of the command with SCUC:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println("SCUC output: " + s);
        }

        // read any errors from the attempted command
        System.err.println("Here is the standard error of the command with SCUC (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println("There is error with SCUC: ");
            System.out.println(s);
        }
    }

    /**
     * @return The file where the reference model for pyomo was written.
     */
    public File getReferenceModelFile() {
        return this.referenceFile;
    }

    /* (non-Javadoc)
	 * @see amesmarket.extern.coopr.SCUC#getSchedule()
     */
    @Override
    public List<CommitmentDecision> getSchedule() {

        return this.genSchedule;
    }

    public double[][] getDAMLMP() {
        return this.DAMLMP;
    }

    public double[][] getDailyCommitment() {
        return this.GenDAMSchedule;
    }

    public int[][] getGenDAMCommitmentStatusNextDay() {
        return this.GenDAMCommitmentStatusNextDay;
    }

    private void cleanup() {
        if (this.deleteFiles) {
            Support.deleteFiles(Arrays.asList(this.python_Input));
        }
    }
}
