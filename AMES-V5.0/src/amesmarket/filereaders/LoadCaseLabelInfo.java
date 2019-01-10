//TODO: LICENCE
package amesmarket.filereaders;

import java.util.BitSet;

import amesmarket.Support;

/**
 * Models all the different pieces of information that may show up in a label in
 * on of the master/aux/scenario load case files.
 *
 * @author Sean L. Mooney
 *
 */
public class LoadCaseLabelInfo {

    /////////////////////KEYWORDS IN LOAD CONTROL FILE////////////////////
    public static final String CASE_NAME = "CaseName";
    public static final String NUM_ZONES = "NumZones";
    public static final String NUM_LS = "NumScenarios";
    public static final String MAX_DAYS = "MaxDay";
    public static final String SCENARIO = "Scenario";
    public static final String AUX_CONTROL = "Aux_Control_File";
    public static final String DAY = "Day";
    public static final String PROB = "Prob";
    public static final String FILE = "File";
    public static final String EXP_LOAD = "ExpectedLoad";
    public static final String ACT_LOAD = "ActualLoad";
    public static final String PART_OF = "PartOf";
    public static final String ALL_OF = "AllOf";
    public static final String LOAD = "Load";
    public static final String WIND = "Wind";
    public static final String DELIM = ":";
    /////////////////////END KEYWORDS/////////////////////////////////////

    private static final int CASE_NAME_F = 0;
    private static final int NUM_ZONES_F = 1;
    private static final int NUM_LS_F = 2;
    private static final int MAX_DAYS_F = 3;
    private static final int SCENARIO_F = 4;
    private static final int AUX_CONTROL_F = 5;
    private static final int DAY_F = 6;
    private static final int PROB_F = 7;
    private static final int FILE_F = 8;
    private static final int EXP_LOAD_F = 9;
    private static final int ACT_LOAD_F = 10;
    private static final int PART_OF_F = 11;
    private static final int ALL_OF_F = 12;
    private static final int LOAD_F = 13;
    private static final int WIND_F = 14;

    private int dayNum = -1;
    private int scenNum = -1;
    private double probVal = -1;
    private String value=null;

    BitSet flags = new BitSet(15);

    /**
     * Convert a label string into something we can understand more easily.
     *
     * @param l
     * @throws BadDataFileFormatException
     */
    public LoadCaseLabelInfo(String l) throws BadDataFileFormatException {
        String[] lineelems = l.split(DELIM);
        if(lineelems.length != 2){
            throw new BadDataFileFormatException("Failed to find label and value in " + l);
        }
        Support.trimAllStrings(lineelems);

        //split the line into tokens by white space.
        String[] elems = lineelems[0].trim().split("\\s+");
        value = lineelems[1];

        //then while more tokens,
        for (int t = 0; t < elems.length; t++) {
            String cur = elems[t];
            if (CASE_NAME.equals(cur)) {
                flags.set(CASE_NAME_F);
            } else if (NUM_ZONES.equals(cur)) {
                flags.set(NUM_ZONES_F);
            } else if (NUM_LS.equals(cur)) {
                flags.set(NUM_LS_F);
            } else if (MAX_DAYS.equals(cur)) {
                flags.set(MAX_DAYS_F);
            } else if (SCENARIO.equals(cur)) {
                flags.set(SCENARIO_F);
                if(t < elems.length - 1) //make sure there's another element.
                    scenNum = Integer.parseInt(elems[++t]);
            } else if (AUX_CONTROL.equals(cur)) {
                flags.set(AUX_CONTROL_F);
            } else if (DAY.equals(cur)) {
                flags.set(DAY_F);
                if(t < elems.length - 1)
                    dayNum = Integer.parseInt(elems[++t]);
            } else if (PROB.equals(cur)) {
                flags.set(PROB_F);
            } else if (FILE.equals(cur)) {
                flags.set(FILE_F);
            } else if (LOAD.equals(cur)) {
                flags.set(LOAD_F);
            } else if (WIND.equals(cur)) {
                flags.set(WIND_F);
            }
            //Both Expected and Actual are sligtly special cases.
            //Since they don't need a prob, the value after the :
            //is implicitly a file path.
            else if (EXP_LOAD.equals(cur)) {
                flags.set(EXP_LOAD_F);
                flags.set(FILE_F);
            } else if (ACT_LOAD.equals(cur)) {
                flags.set(ACT_LOAD_F);
                flags.set(FILE_F);
            } else if (PART_OF.equals(cur)) {
                flags.set(PART_OF_F);
            } else if (ALL_OF.equals(cur)) {
                flags.set(ALL_OF_F);
            } else {
                throw new BadDataFileFormatException("Unknown Token " + cur);
            }
        }
    }

    public boolean isCaseName() {return flags.get(CASE_NAME_F);}
    public boolean isNumZones() {return flags.get(NUM_ZONES_F);}
    public boolean isNumLoadScen() {return flags.get(NUM_LS_F);}
    public boolean isMaxDay() {return flags.get(MAX_DAYS_F);}
    public boolean isScenario() {return flags.get(SCENARIO_F);}
    public boolean isAux() {return flags.get(AUX_CONTROL_F);}
    public boolean hasDay() {return flags.get(DAY_F);}
    public boolean hasProb() {return flags.get(PROB_F);}
    public boolean hasFile() {return flags.get(FILE_F);}
    public boolean isExpLoad() {return flags.get(EXP_LOAD_F);}
    public boolean isActualLoad() {return flags.get(ACT_LOAD_F);}
    public boolean isPartOfDecl() { return flags.get(PART_OF_F);}
    public boolean isAllOfDecl() { return flags.get(ALL_OF_F);}
    public boolean hasWindDecl() { return flags.get(WIND_F); }
    public boolean hasLoadDecl() { return flags.get(LOAD_F); }

    public int day() { return dayNum; }
    public int scenario() { return scenNum; }
    public double probability() {return probVal; }
    public String value() { return value; }

}
