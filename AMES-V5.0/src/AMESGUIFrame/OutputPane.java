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
 * OutputPane.java
 *
 * Created on June 16, 2007, 4:36 PM
 */

package AMESGUIFrame;


import java.awt.*;
import java.awt.event.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import amesmarket.AMESMarket;

public class OutputPane extends javax.swing.JPanel {
    
    /** Creates new form OutputPane */
    public OutputPane() {
        initComponents();
     
        PrintStream print=new PrintStream(new TextAreaOutputStream(OutputTextArea));

        System.setOut(print);
        System.setErr(print);
        
        
        System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>\n");
        System.out.println("      Version 5.0 of the AMES Wholesale Power Market Test Bed (AMES) \n");
        System.out.println("         under development by S. Battula, L. Tesfatsion, and Z. Wang   \n");
        System.out.println("      Based on AMES V2.06 released in 2013 as open-source software \n");
        System.out.println("         by the copyright holders  (H. Li, J. Sun, L. Tesfatsion, S. Mooney) \n");
        System.out.println("         under the terms of the Modified BSD License \n");
        System.out.println("      Homepage: http://www.econ.iastate.edu/tesfatsi/AMESMarketHome.htm  ");
        System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>\n");
        
        Font font = new Font("Courier New", Font.PLAIN, 12);
        OutputTextArea.setFont(font);
        
        clearAllItem = popupMenu.add("Clear All");          
        clearAllItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  clearAllItemActionPerformed(evt);
                }
        });

        popupMenu.addSeparator();                      

        cutItem = popupMenu.add("Cut");          
        cutItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  cutItemActionPerformed(evt);
                }
        });

        copyItem = popupMenu.add("Copy");          
        copyItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  copyItemActionPerformed(evt);
                }
        });

        pasteItem = popupMenu.add("Paste");          
        pasteItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  pasteItemActionPerformed(evt);
                }
        });

        deleteItem = popupMenu.add("Delete");          
        deleteItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  deleteItemActionPerformed(evt);
                }
        });
       
        // Set the component to show the popup menu
        OutputTextArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
     
        CheckOutputPaneRunnable checkRunable=new CheckOutputPaneRunnable();
        checkRunable.setOutputPane(this);
        (new Thread(checkRunable)).start();
  }
    
  private void clearAllItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.selectAll();
    OutputTextArea.replaceSelection("");
  }
 
 private void cutItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.cut();
 }
 
 private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.copy();
 }
 
 private void pasteItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.paste();
 }
 
 private void deleteItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.replaceSelection("");   
 }
public class CheckOutputPaneRunnable implements Runnable {

    private OutputPane outputPane;
    
    public void setOutputPane(OutputPane pane){
        outputPane=pane;
    }
    
    public void run() {
        while(true){
                try {
                    Thread.sleep(1000);
                    //int iCount=OutputTextArea.getLineCount();
                    //System.out.println("iCount="+iCount);

                    if (OutputTextArea.getLineCount() > 10000) {
                        OutputTextArea.select(0, 0);
                        OutputTextArea.selectAll();
                        OutputTextArea.replaceSelection("");
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(OutputPane.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
}
 
   /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        OutputTextArea = new javax.swing.JTextArea();

        OutputTextArea.setColumns(20);
        OutputTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        OutputTextArea.setRows(5);
        jScrollPane1.setViewportView(OutputTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
public class TextAreaOutputStream extends OutputStream {
    private JTextArea output;
    
    public TextAreaOutputStream(JTextArea area) {
        output=area;
    }
    
    public void write(int b) throws IOException {
         output.append(String.valueOf((char)b));
    }   
}
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea OutputTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
     
    private javax.swing.JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem clearAllItem, cutItem, copyItem, pasteItem, deleteItem;
   
}
