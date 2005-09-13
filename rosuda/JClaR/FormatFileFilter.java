/*
 * FormatFileFilter.java
 *
 * Created on 27. Juli 2005, 15:54
 *
 */

package org.rosuda.JClaR;

import java.io.File;
import java.util.Vector;

/**
 *
 * @author tobias
 */
public final  class FormatFileFilter extends javax.swing.filechooser.FileFilter {
    private String description="";
    private Vector extensions=new Vector();
    private static final String STD_EXTENSION=null;
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public boolean accept(final File f){
        return true;
    }
    
    
}
