/*
 * FIXME: LICENCE
 */
package AMESGUIFrame;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import amesmarket.AMESMarket;
import amesmarket.GenAgent;

/**
 * A class to contain all of the 'write out the data' logic.
 *
 * Work in progress. Adapting, moving new 'output' from AMESFrame here.
 * Individual methods for each type of writing are easier to grok than one
 * all-mighty write it all method.
 *
 * @author Sean L. Mooney, Dheepak Krishnamurthy
 *
 */
public class MarketResultsWriter {

  //Common label for the costs section.
    // TODO: Labels aren't spacing out correctly.
    private String rtCostsLabel;

    private final static DecimalFormat decForm = new DecimalFormat("#0.00");

    private final String COL_SEP = "\t\t";

    public MarketResultsWriter(AMESMarket ames) {

        rtCostsLabel = String.format("//%1$5s\t%2$5s", "Day", "Hour");
        final String formString = COL_SEP + "%1$10s";
        for(GenAgent ga : ames.getGenAgentList()) {
            String GenID = ga.getID();
            rtCostsLabel+=String.format(formString, GenID);
        }
        rtCostsLabel+="\n";
    }

    public void formatGenCoCommitments(AMESMarket ames, Writer out) throws IOException {
        out.write("#CommitmentDecisionsStart\n");
        StringBuilder sb = new StringBuilder("\\\\Name");
        sb.append(COL_SEP);
        sb.append("Day");
        sb.append(COL_SEP);
        //TODO: Yet another hard coded 24
        for(int h = 1; h <= 24; h++) {
            sb.append(h);
            sb.append("\t");
        }

        sb.append("\n");
        out.write(sb.toString());

        for( GenAgent ga : ames.getGenAgentList() ) {
            Map<Integer, int[][]> cds = ga.getCommitmentDecisions();
            for(int d = 0; d <= ames.DAY_MAX; d++) {
                sb = new StringBuilder(); //zero out the stringbuilder.
                sb.append(ga.getID()); sb.append(COL_SEP);
                int[][] cd = cds.get(d);
                if(cd != null) {
                    sb.append(d); sb.append(COL_SEP);
                    for(int h = 0; h<cd.length; h++) {
                        sb.append(cd[h]);
                        if(h < cd.length - 1)
                            sb.append("\t");
                    }
                    sb.append("\n");
                    out.write(sb.toString());
                }
            }
        }
        out.write("#CommitmentDecisionsEnd\n");
    }

    public void formatStartupCosts(AMESMarket amesMarket, Writer out) throws IOException {

        out.write("#GeneratorStartupCostsStart\n");

        out.write(rtCostsLabel);
        if(amesMarket.getActualStartupCostsByDay().size() > 0){
            StringBuilder sb = new StringBuilder();

            int d = 2; //start at 2,  rtm did run on day 1.
            for (double[][] costs : amesMarket.getActualStartupCostsByDay()) {
                //concat the strings as a hack to curry formatting the day and hour.
                String prefix = String.format("%1$5d", d) + "\t%1$5d";
                d++;
                sb.append(formatMatrix(costs, prefix,
                        decForm, COL_SEP, "\n", "No Data\n"));
            }

            out.write(sb.toString());

        } else {
            out.write("NONE\n") ;
        };
        out.write("#GeneratorStartupCostsEnd\n");
        out.write("\n");

    }

    public void formatShutdownCosts(AMESMarket amesMarket, Writer out) throws IOException {
        out.write("#GeneratorShutdownCostsStart\n");
        out.write(rtCostsLabel);
        if(amesMarket.getActualShutdownCostsByDay().size() > 0) {
            StringBuilder sb = new StringBuilder();
            int d = 2;
            for (double[][] costs : amesMarket.getActualShutdownCostsByDay()) {
                String prefix = String.format("%1$5d", d) + "\t%1$5d";
                d++;
                sb.append(formatMatrix(costs, prefix,
                        decForm, COL_SEP, "\n", "No Data\n"));
            }
            out.write(sb.toString());
        } else {
            out.write("NONE\n") ;
        }
        out.write("#GeneratorShutdownCostsEnd\n");
        out.write("\n");

    }

    public  void formatTotalProductionCosts(AMESMarket amesMarket, List<GenAgent> agents,  Writer out) throws IOException {
        out.write("#GeneratorProductionCostsStart\n");
        for (GenAgent g : agents) {
            StringBuilder sb = new StringBuilder();
            for (int d = 0; d <= amesMarket.DAY_MAX; d++) {
                double[] c = g.getHourlyProductionCostsForDay(d);
                sb.append(g.getID()); sb.append("\t");
                sb.append(d); sb.append("\t");
                if (c == null) {
                    sb.append("NO DATA\n");
                    continue;
                }
//                sb.append((d + 1));
                for (int h = 0; h < c.length; h++) {
                    sb.append(decForm.format(c[h]));
                    if(h < c.length - 1) sb.append("\t");
                }
                sb.append("\n");
            }
            out.write(sb.toString());
        }
        out.write("#GeneratorProductionCostsEnd\n");
    }

    public void formatTotalCosts(AMESMarket amesMarket,  Writer out) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("\\\\TotalCosts are the sum of all startup, shutdown and production costs\n");
        sb.append("#GeneratorTotalCostsStart\n");

        final ArrayList<double[][]> startup, production, shutdown;
        startup = amesMarket.getActualStartupCostsByDay();
        production = amesMarket.getActualProductionCostsByDay();
        shutdown = amesMarket.getActualShutdownCostsByDay();
        final ArrayList<GenAgent> agents = amesMarket.getGenAgentList();

        double[] dailyCostsByGenCo = new double[agents.size()];
        for(int i = 0; i<dailyCostsByGenCo.length; i++)
            dailyCostsByGenCo[i] = 0.0;

        for(int i = 0; i<agents.size(); i++ ) {
            GenAgent g =  agents.get(i);
            Map<Integer, int[][]> commitments =g.getCommitmentDecisions();
            int hoursCommitted = 0;
            for(int[][] cs : commitments.values()) {
                for(int h = 0; h < cs.length; h++) {
                    
                    // Hardcoded, update
                    if(cs[h][0] == 1){
                        hoursCommitted++;
                    }
                }
            }
            dailyCostsByGenCo[i] += hoursCommitted * g.getNoLoadCost();
            System.out.println(g.getID() + " was committed for " + hoursCommitted + " total hours");
        }

        System.out.println("Basic commitment costs by genco:");
        for(int i = 0; i < dailyCostsByGenCo.length; i++) {
            System.out.println(agents.get(i).getID() + " " + dailyCostsByGenCo[i]);
        }

        //TODO-X wrong place! A formatter has no business computing costs. But it is quick to put it here.
        for(double[][] c : startup){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                }
            }
        }

        for(double[][] c : production){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                }
            }
        }

        for(double[][] c : shutdown){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                }
            }
        }

        for(int g = 0; g < dailyCostsByGenCo.length; g++){
            sb.append(agents.get(g).getID()); sb.append(": ");
            sb.append(decForm.format(dailyCostsByGenCo[g]));
            sb.append("\n");
        }
        
        sb.append("#GeneratorTotalCostsEnd\n");

        sb.append("#ObjectiveCostStart\n");

        double totalCosts = 0;

        for (int g = 0; g < dailyCostsByGenCo.length; g++) {

            totalCosts += dailyCostsByGenCo[g];

        }

        sb.append("The objective function value is : ");
        sb.append(decForm.format(totalCosts));
        sb.append("\n");

        sb.append("#ObjectiveTotalCostsEnd\n");



        out.write(sb.toString());
    }
    
    /**
     * Apply the format string to each row of the matrix.
     *
     * Remember to add '\n' to the end of the format string if
     * each row should be on its own line.
     * @param v array to format.
     * @param formRowPrefix prefix for row, may be null.
     * @param formElement format string
     * @param rowSep string to put between rows
     * @param nullMessage print this instead of the data if a row is null.
     * @return
     */
    private String formatMatrix(double[][] v, String formRowPrefix, DecimalFormat format, String colSep, String rowSep, String nullMessage) {
        if(v == null) return "";

        StringBuilder sb = new StringBuilder();

        for(int h = 0; h < v.length; h++){
            if(formRowPrefix!= null)
                sb.append(String.format(formRowPrefix, h));
                sb.append(colSep);
            if(v[h] != null){
                for(int i = 0; i < v[h].length; i++) {
                    sb.append( format.format(v[h][i]) );
                    sb.append(colSep);
                }
            } else {
                sb.append(nullMessage);
            }
            sb.append(rowSep);
        }

        return sb.toString();
    }

    public static void formatTotalCostsSplitByType(AMESMarket amesMarket,  Writer out) throws IOException {
        // Calculates and prints costs by type of cost
        StringBuilder sb = new StringBuilder();

        sb.append("\\\\TotalCosts are the sum of all startup, shutdown, no load  and production costs\n");
        sb.append("#GeneratorTotalCostsByTypeStart\n");
        sb.append("Day\t GenCoName\t Startup\t Shutdown\t Production\t NoLoad\t Total\n");

        final ArrayList<double[][]> startup, production, shutdown, fueltype;
        startup = amesMarket.getActualStartupCostsByDay();
        production = amesMarket.getActualProductionCostsByDay();
        shutdown = amesMarket.getActualShutdownCostsByDay();
        final ArrayList<GenAgent> agents = amesMarket.getGenAgentList();

        double[][] dailyCostsByGenCo = new double[agents.size()][amesMarket.DAY_MAX-1];
        double[][] dailyStartupCostsByGenCo = new double[agents.size()][amesMarket.DAY_MAX-1];
        double[][] dailyShutdownCostsByGenCo = new double[agents.size()][amesMarket.DAY_MAX-1];
        double[][] dailyProductionCostsByGenCo = new double[agents.size()][amesMarket.DAY_MAX-1];
        double[][] dailyNoLoadCostsByGenCo = new double[agents.size()][amesMarket.DAY_MAX-1];
        //int numDay = 0;
        
        // numDay starts from 1. For a 3 day simulation run, SCUC is run twice. 
        // numDay-1 is used because java indices start from 0
        
        for(int numDay = 1; numDay < amesMarket.DAY_MAX; numDay++) {
            for(int i = 0; i<dailyCostsByGenCo.length; i++) {
                    dailyStartupCostsByGenCo[i][numDay-1] = 0.0;
                    dailyShutdownCostsByGenCo[i][numDay-1] = 0.0;
                    dailyProductionCostsByGenCo[i][numDay-1] = 0.0;
                    dailyNoLoadCostsByGenCo[i][numDay-1] = 0.0;
                    dailyCostsByGenCo[i][numDay-1] = 0.0;
                }
        }

        
        for(int i = 0; i<agents.size(); i++ ) {
            GenAgent g =  agents.get(i);
            Map<Integer, int[][]> commitments =g.getCommitmentDecisions();
            int hoursCommitted = 0;
            int numDay = 1; //start from day 1 for every generator
            for(int[][] cs : commitments.values()) {
                for(int h = 0; h < cs.length; h++) {
                    // hardcoded, update
                    if(cs[h][0] == 1){
                        hoursCommitted++;
                    }
                }
            dailyCostsByGenCo[i][numDay-1] += hoursCommitted * g.getNoLoadCost();
            dailyNoLoadCostsByGenCo[i][numDay-1] += hoursCommitted * g.getNoLoadCost();
            numDay=numDay+1;
            System.out.println(g.getID() + " was committed for " + hoursCommitted + " total hours in day " + numDay);
            }

        }

        for(int numDay=1 ; numDay < amesMarket.DAY_MAX ; numDay++) { 
                System.out.println("Basic commitment costs by genco:");
                for(int i = 0; i < dailyCostsByGenCo.length; i++) {
                    System.out.println(agents.get(i).getID() + " in day " + numDay + " is " + dailyCostsByGenCo[i][numDay-1]);
                }
        }

        //TODO-X wrong place! A formatter has no business computing costs. But it is quick to put it here.
        int numDay = 1;
        for(double[][] c : startup){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g][numDay-1] += c[h][g];
                    dailyStartupCostsByGenCo[g][numDay-1] += c[h][g];
                }
            }
            numDay=numDay+1;
        }
        
        numDay = 1;
        for(double[][] c : production){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g][numDay-1] += c[h][g];
                    dailyProductionCostsByGenCo[g][numDay-1] += c[h][g];
                }
            }
        numDay=numDay+1;
        }

        numDay = 1;
        for(double[][] c : shutdown){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g][numDay-1] += c[h][g];
                    dailyShutdownCostsByGenCo[g][numDay-1] += c[h][g];
                }
            }
        numDay=numDay+1;
        }

        for(int g = 0; g < dailyCostsByGenCo.length; g++){
             for(numDay=1; numDay<amesMarket.DAY_MAX; numDay++){
                sb.append(String.valueOf(numDay)); sb.append("\t");
                sb.append(agents.get(g).getID()); sb.append(": \t");
                sb.append(decForm.format(dailyStartupCostsByGenCo[g][numDay-1]));sb.append("\t\t");
                sb.append(decForm.format(dailyShutdownCostsByGenCo[g][numDay-1]));sb.append("\t\t");
                sb.append(decForm.format(dailyProductionCostsByGenCo[g][numDay-1]));sb.append("\t\t");
                sb.append(decForm.format(dailyNoLoadCostsByGenCo[g][numDay-1]));sb.append("\t\t");
                sb.append(decForm.format(dailyCostsByGenCo[g][numDay-1]));sb.append("\t\t");
                sb.append("\n");
            }
        }
        
        sb.append("#GeneratorTotalCostsByTypeEnd\n");
        
        sb.append("#ObjectiveCostStart\n");

        double[] totalCosts = new double[amesMarket.DAY_MAX-1]; //initialized to zero

        for (int g = 0; g < dailyCostsByGenCo.length; g++) {
            for(numDay=1; numDay < amesMarket.DAY_MAX; numDay++) {
                totalCosts[numDay-1] += dailyCostsByGenCo[g][numDay-1];
            }
        }
        for(numDay=1;numDay<amesMarket.DAY_MAX;numDay++) {
            sb.append("The objective function value for day " + numDay + " is : ");
            sb.append(decForm.format(totalCosts[numDay-1]));
            sb.append("\n");
        }
        sb.append("#ObjectiveTotalCostsEnd\n");
        out.write(sb.toString());
    }

    @SuppressWarnings("null")
    public static void formatTotalCostsSplitByFuelType(AMESMarket amesMarket,
            Writer out) throws IOException {
        // TODO Auto-generated method stub
        
        StringBuilder sb = new StringBuilder();

        sb.append("\\\\Costs per fuel type\n");
        sb.append("#GeneratorTotalCostsByFuelTypeStart\n");
        sb.append("FuelType\t Startup\t Shutdown\t Production\t NoLoad\t Total\n");

        final ArrayList<double[][]> startup, production, shutdown;
        
        startup = amesMarket.getActualStartupCostsByDay();
        production = amesMarket.getActualProductionCostsByDay();
        shutdown = amesMarket.getActualShutdownCostsByDay();
        
        final ArrayList<GenAgent> agents = amesMarket.getGenAgentList();

        ArrayList<String> fueltype = new ArrayList<String>(agents.size());
        
        for(int i = 0; i<agents.size(); i++ ) {
            GenAgent g =  agents.get(i);
                String fuelName = g.getFuelType();
                fueltype.add(fuelName);                
        }
        
        
        double[] dailyCostsByGenCo = new double[agents.size()];
        double[] dailyStartupCostsByGenCo = new double[agents.size()];
        double[] dailyShutdownCostsByGenCo = new double[agents.size()];
        double[] dailyProductionCostsByGenCo = new double[agents.size()];
        double[] dailyNoLoadCostsByGenCo = new double[agents.size()];

        for(int i = 0; i<dailyCostsByGenCo.length; i++) {
            dailyStartupCostsByGenCo[i] = 0.0;
            dailyShutdownCostsByGenCo[i] = 0.0;
            dailyProductionCostsByGenCo[i] = 0.0;
            dailyNoLoadCostsByGenCo[i] = 0.0;
            dailyCostsByGenCo[i] = 0.0;
        }
        
        Set<String> uniqueFuelType = new HashSet<String>(fueltype);
        
        List<String> uniqueFuelTypeNames = new ArrayList<String>(uniqueFuelType);

        double[] dailyCostsByFuelType = new double[uniqueFuelType.size()];
        double[] dailyStartupCostsByFuelType = new double[uniqueFuelType.size()];
        double[] dailyShutdownCostsByFuelType = new double[uniqueFuelType.size()];
        double[] dailyProductionCostsByFuelType = new double[uniqueFuelType.size()];
        double[] dailyNoLoadCostsByFuelType = new double[uniqueFuelType.size()];

        for(int i = 0; i<dailyCostsByFuelType.length; i++) {
            dailyStartupCostsByFuelType[i] = 0.0;
            dailyShutdownCostsByFuelType[i] = 0.0;
            dailyProductionCostsByFuelType[i] = 0.0;
            dailyNoLoadCostsByFuelType[i] = 0.0;
            dailyCostsByFuelType[i] = 0.0;
        }
        
        for(int i = 0; i<agents.size(); i++ ) {
            GenAgent g =  agents.get(i);
            Map<Integer, int[][]> commitments =g.getCommitmentDecisions();
            int hoursCommitted = 0;
            for(int[][] cs : commitments.values()) {
                for(int h = 0; h < cs.length; h++) {
                    
                    // hardcoded, update
                    if(cs[h][0] == 1){
                        hoursCommitted++;
                    }
                }
            }
            dailyCostsByGenCo[i] += hoursCommitted * g.getNoLoadCost();
            dailyNoLoadCostsByGenCo[i] += hoursCommitted * g.getNoLoadCost();
            dailyNoLoadCostsByFuelType[uniqueFuelTypeNames.indexOf(g.getFuelType())] += hoursCommitted * g.getNoLoadCost();
            dailyCostsByFuelType[uniqueFuelTypeNames.indexOf(g.getFuelType())] += hoursCommitted * g.getNoLoadCost();
            System.out.println(g.getID() + " was committed for " + hoursCommitted + " total hours");
        }

        System.out.println("Basic commitment costs by genco:");
        for(int i = 0; i < dailyCostsByGenCo.length; i++) {
            System.out.println(agents.get(i).getID() + " " + dailyCostsByGenCo[i]);
        }

        //TODO-X wrong place! A formatter has no business computing costs. But it is quick to put it here.
        for(double[][] c : startup){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                    dailyStartupCostsByGenCo[g] += c[h][g];
                    dailyStartupCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
                    dailyCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
               }
            }
        }

        for(double[][] c : production){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                    dailyProductionCostsByGenCo[g] += c[h][g];
                    dailyProductionCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
                    dailyCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
                }
            }
        }

        for(double[][] c : shutdown){
            for(int h = 0; h<c.length; h++){
                for(int g = 0; g<dailyCostsByGenCo.length; g++){
                    dailyCostsByGenCo[g] += c[h][g];
                    dailyShutdownCostsByGenCo[g] += c[h][g];
                    dailyShutdownCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
                    dailyCostsByFuelType[uniqueFuelTypeNames.indexOf(agents.get(g).getFuelType())] += c[h][g];
                }
            }
        }
        
        
        
        for(int f = 0; f < uniqueFuelType.size(); f++){
            sb.append(uniqueFuelTypeNames.get(f));sb.append("\t");
            sb.append(decForm.format(dailyStartupCostsByFuelType[f]));sb.append("\t\t");
            sb.append(decForm.format(dailyShutdownCostsByFuelType[f]));sb.append("\t\t");
            sb.append(decForm.format(dailyProductionCostsByFuelType[f]));sb.append("\t\t");
            sb.append(decForm.format(dailyNoLoadCostsByFuelType[f]));sb.append("\t\t");
            sb.append(decForm.format(dailyCostsByFuelType[f]));sb.append("\t\t");
            sb.append("\n");
        }

        sb.append("#GeneratorTotalCostsByFuelTypeEnd\n");

        out.write(sb.toString());

    }
    
}
