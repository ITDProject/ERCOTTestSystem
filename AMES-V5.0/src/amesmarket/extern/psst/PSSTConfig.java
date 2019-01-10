package amesmarket.extern.psst;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Configuration information for using a PSST program to run some
 * optimization problem.
 *
 * This class is generating commands used to run the SCUC/SCED
 * optimizations.
 *
 * @author Dheepak Krishnamurthy
 *
 */
public class PSSTConfig {

	//    private final String PSSTProg;
	//    private final File referenceModelDir;
	//    private final File scenarioModelDir;
	//    private final String solutionWriter;

	private final String[] args;
	/**
	 * Create a psst program.
	 * @param psstProg
	 * @param solutionWriter
	 */
	private PSSTConfig(String ... psstProgArgs) {
		this.args = psstProgArgs;
	}

	/**
	 *
	 * @param referenceModelDir
	 * @param scenarioModelDir
	 * @param solutionWriter
	 * @return
	 */
	public static PSSTConfig createStochasticPSST(File referenceModelDir, File scenarioModelDir, String solutionWriter) {
		return new PSSTConfig(
				"pyomo" ,
				"-m",referenceModelDir.getAbsolutePath(),
				"-i",scenarioModelDir.getAbsolutePath(),
				"--solve", "--solution-writer="+solutionWriter, "--solver-options=\"threads=2\" "
				);
	}

	/**
	 *
	 * @param pyomoSolPrint
	 * @param referenceModel
	 * @param referenceFile
	 * @return
	 */
	public static PSSTConfig createDeterministicPSST(File referenceModel, File referenceFile) {
		return new PSSTConfig("psst", "scuc",
				"--data", referenceFile.getAbsolutePath(),
				"--output", "xfertoames.dat");
	}

	/**
	 * Get the arguments that will on invoked to start the process.
	 * @return
	 */
	public String[] getExecCmd() {
		return this.args;
	}

	/**
	 * Start a new process that runs a PSST program.
	 * @return
	 * @throws IOException
	 */
	public Process createPSSTProcess() throws IOException{
		ProcessBuilder pb = new ProcessBuilder(this.getExecCmd());
		this.setupEnv(pb.environment());
		return pb.start();
	}

	/**
	 * Ensure the environment is set up correctly for the PSST call.
	 * @param env
	 */
	private void setupEnv(Map<String, String> env){
		//check to see if the PYTHONPATH has defined
		String pyPathVar = "PYTHONPATH";
		if(!env.containsKey( pyPathVar )) { //The python path wasn't defined
			String PSSTPath = this.pythonPath();
			//TODO: Delete printing when debugged.
			System.out.println(pyPathVar + " not defined\n" +
					"Adding " + pyPathVar + "=" + PSSTPath + " to the env path.");
			env.put(pyPathVar, PSSTPath);
		}
	}

	/**
	 * @return the path to use as the python path
	 */
	public String pythonPath() {
		//TODO: Enable setting this path via a system property or from the constructor
		//create file to the SCUCresources folder to make it easy to get the full path.
		java.io.File scucres = new java.io.File("SCUCresources");
		if(!scucres.exists()) {
			System.err.println(scucres.getAbsolutePath() + " does not exist. SCUC may fail.");
		}
		return scucres.getAbsolutePath();
	}
}

