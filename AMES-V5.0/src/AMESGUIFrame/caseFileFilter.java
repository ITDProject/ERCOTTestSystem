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

package AMESGUIFrame;
/*
 * caseFileFilter.java
 *
 * Created on 2007 1 28, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



import java.io.File;
import javax.swing.filechooser.FileFilter;

 class caseFileFilter extends FileFilter {
 
  String[] extensions;

  String description;

  public caseFileFilter(String ext) {
    this(new String[] { ext }, null);
  }

  public caseFileFilter(String[] exts, String descr) {
    extensions = new String[exts.length];
    for (int i = exts.length - 1; i >= 0; i--) {
      extensions[i] = exts[i].toLowerCase();
    }
     
    description = (descr == null ? exts[0] + " files" : descr);
  }

  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String name = f.getName().toLowerCase();
    for (int i = extensions.length - 1; i >= 0; i--) {
      if (name.endsWith(extensions[i])) {
        return true;
      }
    }
    return false;
  }

  public String getDescription() {
    return description;
  }
    
 }
