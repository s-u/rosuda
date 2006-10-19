/*
 * ExtFileFilter.java
 *
 * Created on 13. Juni 2005, 15:08
 */

package org.rosuda.JClaR;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author tobias
 */
public final class ExtFileFilter extends javax.swing.filechooser.FileFilter {
    private String description="";
    private List extensions=new ArrayList();
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
        
        for(final ListIterator e = extensions.listIterator(); e.hasNext();){
            if(f.getName().toLowerCase().endsWith(((String)e.next()))) {
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