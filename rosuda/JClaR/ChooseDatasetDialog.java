/*
 * ChooseDatasetDialog.java
 *
 * Created on 12. September 2005, 11:45
 *
 */

package org.rosuda.JClaR;

import java.awt.Frame;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author tobias
 */
public class ChooseDatasetDialog extends TableDialog {
    
    private TableModel tm;
    private int selectedIndex=-1;
    
    /** Creates a new instance of ChooseDatasetDialog */
    ChooseDatasetDialog(Frame parent) {
        super(parent,true);
        
        setUpdateButton(false);
        setOkButtonText("Classify", 'f');
        
        tm = new TableModel();
        // create data vector
        final Vector dataVector = new Vector(DatasetManager.getNumberOfDatasets());
        // each element is <file name>|<length>|<#variables>
        for(Enumeration en = DatasetManager.getElements(); en.hasMoreElements();){
            final Data data = (Data)en.nextElement();
            if (data!=null){
                final Vector row = new Vector(3);
                row.add(data.getPath());
                row.add(new Integer(data.getLength()));
                row.add(new Integer(data.getNumberOfVariables()));
                dataVector.add(row);
            }
        }
        final Object columnIdentifiers[]={"File", "Length", "#Variables"};
        tm.setDataVector(dataVector, columnIdentifiers);
        setTableModel(tm);
        
        resetSize();
    }
    
    protected void ok() {
        if((selectedIndex=getSelectedRow())==-1) return;
        dispose();
    }
    
    Data getSelectedDataset(){
        if(selectedIndex==-1) return null;
        
        final String fileName = (String)tm.getValueAt(selectedIndex,0);
        return DatasetManager.getDataset(fileName);
    }
    
    private final class TableModel extends TableDialog.TableModel {
        
        private Vector variables;
        
        public TableModel(){
            final Class[] colClasses = {String.class, Integer.class, Integer.class};
            final boolean[] colEditable = {false, false, false};
            
            setColumnClasses(colClasses);
            setColumnEditable(colEditable);
        }
    }
    
}
