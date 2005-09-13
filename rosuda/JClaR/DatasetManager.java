/*
 * DatasetManager.java
 *
 * Created on 12. September 2005, 11:15
 *
 */

package org.rosuda.JClaR;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author tobias
 */
public class DatasetManager {
    
    // TODO: check if dataset on disc has changed
    
    /**
     * maps file names to data objects
     */
    private static Hashtable datasets = new Hashtable(4);
    
    /** Creates a new instance of DatasetManager */
    public DatasetManager() {
    }
    
    /**
     * Adds data to hashtable with file name as key.
     */
    public static void addDataset(Data data){
        datasets.put(data.getPath(), data);
    }
    
    /**
     * Get dataset to given file
     * @return Corresponding data object. Returns null if file hasn't been opened.
     */
    public static Data getDataset(String file){
        return (Data)datasets.get(file);
    }
    
    public static int getNumberOfDatasets(){
        return datasets.size();
    }
    
    public static Enumeration getElements(){
        return datasets.elements();
    }
}
