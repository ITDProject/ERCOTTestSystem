package Interface;

import java.util.ArrayList;
import java.util.Map;

import com.sun.prism.paint.Stop;

import amesmarket.GenAgent;
import uchicago.src.sim.engine.Schedule;

public class TestTSOInterface {
	
	
	public static void main(String[] args) {
		
//		Map<String, String> env = System.getenv();
//        for (String envName : env.keySet()) {
//            System.out.format("%s=%s%n",
//                              envName,
//                              env.get(envName));
//        }
		TSOInterface app = new TSOInterface();
		String caseFile = "DATA/AMESTestInput.dat";
		app.readBaseCase(caseFile);
		app.startMarket();
		
		int min = 0;
		int interval = 0;
		int hour = 1;
		int day =1;
		int dayMax =2;
		
		int M =app.getRTMarketIntervalInMins();
		int NIH = 60/M;
		
		boolean market_done = false;
		
		while( !market_done) {
			int tomorrow = day+1;
			interval = (hour-1)*NIH + (int) min/M;
			
			if(hour ==2 && min==0) {
				app.prepareDayAheadSCUCCase(null,null,null,null,null,null);
			}
			if(hour ==16 && min==0) {
				app.runDAMarket(tomorrow);
				int[][] schdule = app.getDAUnitSchedule();
				
				System.out.println("Unit schedule:[hour][unit id]:");
				for (int i = 0; i<schdule.length; i++) {
					for (int j = 0; j<schdule[0].length; j++) {
						System.out.println("hour, unit id, Schedule ="+i+","+j+","+schdule[i][j]);
					}
				}
				
			   double[][] daLMPs = app.getDALMPs();
				
				System.out.println("Day ahead LMP:[hour][bus id]:");
				for (int i = 0; i<daLMPs.length; i++) {
					for (int j = 0; j<daLMPs[0].length; j++) {
						System.out.println("hour, bus id, LMP ="+i+","+j+","+daLMPs[i][j]);
					}
				}
				
				System.out.println("end of DA Market for day= "+tomorrow);
			}
			
		
			if(day >=2) {
				app.prepareRealTimeSCEDCase(null,null,null,null,null,null);
				app.runOneStepRTMarket(min, interval, hour, day);
				
				System.out.println("end of Real time Market for day, hour, min ="+day+","+hour+","+min);
				
		        double[] rtLMPs = app.getRealTimeLMPs();
		        double[] genDispatches = app.getRealTimeGeneratorPowerinMW();
				
				System.out.println("Real time LMP:[bus id]:");
				for (int i = 0; i<rtLMPs.length; i++) {
					 {
						System.out.println("bus id, LMP ="+i+","+rtLMPs[i]);
					}
				}
				
				System.out.println("Real time genDispatches:[gen id]:");
				for (int i = 0; i<genDispatches.length; i++) {
					 {
						System.out.println("gen id, MW ="+i+","+genDispatches [i]);
					}
				}
				
				
			}
			min = min + M;
			app.updateAMESInternalTimer();
			
			if (min % 60 == 0){    
                hour++;
                min = 0;
                if (hour == 25) {
                    min = 0;
                    hour = 1;
                    day++;
                    
                }
               
            }
			 
		    if ((hour == 24) && (day == dayMax)) {
             	market_done = true;
            }

		}
			
		
			
			
	}

}
