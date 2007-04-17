/*
 * DatasetManager.java
 *
 * Created on 12. September 2005, 11:15
 *
 */

package org.rosuda.JClaR;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author tobias
 */
public final class DatasetManager {
    
    // TODO: check if dataset on disc has changed
    
    private static DataClassifierListenerIF listener;
    
    /**
     * maps file names to data objects
     */
    private static Hashtable<String,Data> datasets = new Hashtable<String,Data>(4);
    
    /** Creates a new instance of DatasetManager */
    private DatasetManager() {
    }
    
    /**
     * Adds data to hashtable with file name as key.
     */
    static void addDataset(final Data data){
        datasets.put(data.getPath(), data);
        if(listener!=null) listener.datasetsChanged();
    }
    
    /**
     * Get dataset to given file
     * @return Corresponding data object. Returns null if file hasn't been opened.
     */
    static Data getDataset(final String file){
        return (Data)datasets.get(file);
    }
    
    static int getNumberOfDatasets(){
        return datasets.size();
    }
    
    static Enumeration getElements(){
        return datasets.elements();
    }
    
    static List<Data> getDataVector(){
        return new ArrayList<Data>(datasets.values());
    }

    static void setListener(final DataClassifierListenerIF aListener) {
        listener = aListener;
    }
}
