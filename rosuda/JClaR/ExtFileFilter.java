/*
 * ExtFileFilter.java
 *
 * Created on 13. Juni 2005, 15:08
 */

package org.rosuda.JClaR;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author tobias
 */
public final class ExtFileFilter extends javax.swing.filechooser.FileFilter {
    private String description="";
    private Vector extensions=new Vector();
    private String stdExtension=null;
    
    public String getDescription() {
        return description;
    }
    
    void setDescription(final String description) {
        this.description = description;
    }
    
    public boolean accept(final File f){
        if(f.isDirectory())  {
            return true;
        }
        
        for(final Enumeration e = extensions.elements(); e.hasMoreElements();){
            if(f.getName().toLowerCase().endsWith(((String)e.nextElement()))) {
                return true;
            }
            
        }
        return false;
    }
    
    void addExtension(final String ext){
        extensions.add(ext.toLowerCase());
        if(stdExtension==null)  {
            stdExtension=ext.toLowerCase();
        }
        
    }
    private String getExtension(){
        return stdExtension;
    }
}