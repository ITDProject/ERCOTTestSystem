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
// LSEAgent.java
// Storage Unit Agent (wholesale power buyer)
package amesmarket;

import fncs.JNIfncs;
import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.sql.*;

import amesmarket.CaseFileData.StorageInputData;
/**
 * Example showing what lseData[i] contains LSEData //ID	atBus	block	LP	block	LP
 * block	LP	block	LP 1	1	6	22	6	30	6	34	6	27
 *
 * // block: load block for the next how many hours // LP: load profile (fixed
 * demand)
 */
public class StorageAgent {

    private StorageInputData data;
    private int atBus;
    private int ID;
    
    // Constructor
    public StorageAgent(StorageInputData inputdata) {
        this.data = inputdata;
        this.ID = inputdata.getID();
        this.atBus = inputdata.getatBus();
    }

    public StorageInputData getData(){
        return this.data;
    }
    
    public void setData(StorageInputData inputdata){
        this.data = inputdata;
    }
    
}
