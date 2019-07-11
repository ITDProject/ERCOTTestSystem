package Interface;

import AMESGUIFrame.AMESFrame;
import amesmarket.AMESMarket;
import amesmarket.DAMarket;
import amesmarket.RTMarket;
import py4j.GatewayServer;
import uchicago.src.sim.util.Random;


/**
 * 
 * Wholesale market procedure:
 * 
 * Need to run at least one day Day-ahead market before the next day real-time market operation
 * 
 *    hour == 2, collect bids from demand and generators for next DA
 *    hour == 12, run SCUC to clear the DA market
 *    
 *    For other hours and minutes, run RT market operation, usually at the time internal of 5 minutes
 * 
 * 
 * @author huan289
 *
 */
public class TSOInterface {
	
	private static AMESFrame mainFrameWindow = null;
	
	private AMESMarket amesMkt = null;
	
	private DAMarket daMkt = null;
	
	private RTMarket rtMkt = null;
	
	//read in the base case
	public boolean readBaseCase(String caseFile) {
		
		boolean isCaseLoaded = false;
		
		 mainFrameWindow = new AMESFrame( );        

	       
	     mainFrameWindow.noGUI = true;
	            
	     /*
	      * The following loadCase function read in the case file, and then initialize the AMESMarket by calling
	      * InitializeAMESMarket() function of AMESFrame class 
	      */
	     
	     amesMkt = mainFrameWindow.loadCase(caseFile); 
	     
	     isCaseLoaded = mainFrameWindow.getbLoadCase();
	     
	     if (isCaseLoaded) {
	    	 
	    	 System.out.println("Case loaded: "+caseFile);
	     }
	     else {
	    	 System.out.println("!!!!!Error when trying to load case: "+caseFile+ "\n"+ "Please check the case file or data!");
	     }
	     
	     return isCaseLoaded;
	    		 
	    
	    
	}
	
    public void startMarket() {
    	//mainFrameWindow.startAMESMarket();
    	amesMkt.buildModel();
    	Random.createUniform();  // Create random uniform
    }
	
	public boolean runDAMarket(int day) {
		boolean flag = amesMkt.getISO().runDayAheadMarket(day);
		amesMkt.getISO().dayAheadPostProcess(day);
		return flag;
	}

	
	public boolean runOneStepRTMarket(int min, int interval, int hour, int day) {
		return amesMkt.getISO().runOneStepRealTimeMarket(min, interval, hour, day);
	}
	
	public double[][] getDALMPs(){
		return amesMkt.getISO().getDailyLMP();
	}
	public double[][] getDAUnitPower(){
		return amesMkt.getISO().getDAMktUnitPower();
	}
	public int[][] getDAUnitSchedule(){
		return amesMkt.getISO().getDAMktUnitSchedule();
	}
	
	public double[] getRealTimeLMPs(){
		return amesMkt.getISO().getRTMarket().getIntervalRtLMPs();
	}
	
	/**
	 * The format of the 2-d matrix [hour][generator index]
	 * @return
	 */
	public double[] getRealTimeGeneratorPowerinMW(){
		return amesMkt.getISO().getRTMarket().getIntervalRtDispatches();
	}
	
	public void updateAMESInternalTimer() {
		amesMkt.updateInternalTimer();
	}
	
    
	// keep one base case model in memory, update the SCUC model by reading in data
	
	/**
	 *  The original data initialization is in setInitialStructuralParameters() of AMESMarket class.
	 *      setNodeData(init.getNodeData());
	        setBranchData(init.getBranchData());
	        setGenData(init.getGenData());
	        setAlertGenMarkers(init.getAlertGenMarker());
	        setExtraGenData(init.getExtraGenCoParams());
	        setStorageData(init.getStorageParams());
	        setLSEData(init.getLSEDataFixedDemand());
	        setNDGData(init.getNDGData());
	        setLSEPriceSensitiveData(init.getLSEDataPriceSensitiveDemand());
	        setLSEHybridData(init.getLSEDataHybridDemand());
	 * 
	 * @param unRespMW (double[][] array, dimension 1 - lse index, dimension 2 - 24 hour non-responsive load MW)
	 * @param respMaxMW (similar data structure to above)
	 * @param respC2 (similar data structure to above)
	 * @param respC1 (similar data structure to above)
	 * @param resp_deg (similar data structure to above)
	 * @param forecast (double[][] array, dimension 1 - lse index, dimension 2 - 24 hour forecast wind power)
	 * @return boolean
	 */
	public boolean prepareDayAheadSCUCCase(double[][] unRespMW, double[][] respMaxMW, double[][] respC2, double[][] respC1, double[][] resp_deg, double[][] forecast) {
		boolean flag = true;
		
		/**
		 * Note currently DA market does not support responsive loads. 
		 * The following data not used!!!!
		 * 
		 * double[][] respMaxMW, double[][] respC2, double[][] respC1, double[][] resp_deg data not used!!!!
		 */
		 
		double[][] loadProfileLSE = this.amesMkt.getISO().getLoadProfileByLSE();
		//update it, and save it back to AMES market
		
		
		
		//TODO what about forecast
		
		return flag;
	}
	
	
	//update the model SCED by reading in data
	/**
	 * 
	 * @param unRespMW
	 * @param respMaxMW
	 * @param respC2
	 * @param respC1
	 * @param resp_deg
	 * @param forecast
	 * @param UnitCommitmentResults
	 * @return boolean
	 */
	public boolean prepareRealTimeSCEDCase(double[] unRespMW, double[] respMaxMW, double[] respC2, double[] respC1, double[] resp_deg, double[] forecast) {
	
		boolean flag = true;
		
		// format of init_lse: 
		double [][] init_lse = amesMkt.getLSEData(); 
		//TO update it, and save it back to AMES market
		// unRespMW is only one entry in the init_lse array. Need to update the corresponding entry.
		
        double [][] updated_lse = init_lse;
		
		amesMkt.setLSEData(updated_lse);
		
		
		double[][][] init_priceSen_lse = amesMkt.getLSEPriceSensitiveData();
		//TO update it, and save it back to AMES market
		
		
		amesMkt.setLSEPriceSensitiveData(init_priceSen_lse);
		
		int[][] init_hybrid_lse = amesMkt.getLSEHybridData();
		//TO update it, and save it back to AMES market
		
		
		//TODO wind power for the generator record??
		
		return flag;
		
	}
	
	public int getRTMarketIntervalInMins() {
		return amesMkt.M;
	}
	
	//NOTE: We will run SCUC and SCED via calling PSST Python directly, not through Java API.
	//The code is in the PSST command line interface (cli) file: AMES-V5.0\psst\psst\cli.py
    //public void runSCUC(); 
	
	//public void runSCED();
	
	//NOTE: all results are obtained in fncsTSO wrapper in memory or via files
	
	public static void main(String[] args) {
		
		TSOInterface app = new TSOInterface();
		// app is now the gateway.entry_point
		int port = 26000;
		
		String caseFile = "";
		
		if (args.length>0) {
			
		    port = Integer.valueOf(args[0]);
		    
		    if(args.length>1) {
			   caseFile = String.valueOf(args[1]);
			   app.readBaseCase(caseFile);
		    }
			
			
		}
			
		GatewayServer server = new GatewayServer(app,port);

		System.out.println("AMES TSO Agent Interface for TESP");

		System.out.println("Starting Py4J " + app.getClass().getTypeName() + " at port ="+port);
		server.start();
		
		
	}
		
	
}
