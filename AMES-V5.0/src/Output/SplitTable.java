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

/*
 * SplitTable.java
 *
 * Created on 2007 3 11, 7:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Output;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import AMESGUIFrame.*;


public class SplitTable extends JFrame       {

    public SplitTable() {
        super("Output Table View");
    }

     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public  void createAndShowGUI() {
        //Create and set up the window.
        //JFrame frame = new SplitTable();
        
        TableView tableView = new TableView(amesFrame);
 
        SelectPanel selectPanel = new SelectPanel(amesFrame,true,tableView,null);
        
        //Provide minimum sizes for the two components in the split pane
        selectPanel.setMinimumSize(new Dimension(300, 50));
        tableView.setMinimumSize(new Dimension(50, 30));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(selectPanel);
        splitPane.setRightComponent(tableView);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(430);


        //Add the split pane to this frame
        getContentPane().add(splitPane);

        //Display the window.
        pack();
        setVisible(true);
    }

    public void setAMESFrame(AMESFrame frame) {
        amesFrame=frame;
    }
    
    public void setIfTableView(boolean bView) {
        bTableView=bView;
    }
        private AMESFrame amesFrame;
        private boolean bTableView;

}
