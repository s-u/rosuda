/*
 * FixVariablesDialog.java
 *
 * Created on 23. Juni 2005, 16:29
 */

package org.rosuda.JClaR;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JSlider;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public final class FixVariablesDialog extends TableDialog implements SelectionModIF {
    
    private String formula;
    private String fixedVariables;
    private String subsetExpression;
    
    private double[] sds;
    private double[] medians;
    
    /**
     * shift caused by hidden variables
     */
    private int lastSelectedRow=0;
    
    private TableModel tm;
    
    /**
     * True if a property has been changed, that requires recalculation
     * of prediction values. Is set to false by update().
     */
    private boolean hardChange;
    private FixVariablesSliderPanel fvsp;
    /**
     * If false, don't recalculate deviation when slider is changed.
     * Should be true unless the selected row is changing.
     */
    private boolean updateDevVal=true;
    
    /** Creates new form FixVariablesDialog */
    public FixVariablesDialog(final java.awt.Frame parent, final Data data, final int varPos) {
        super(parent);
        
        tm = new TableModel();
        setTableModel(tm);
        
        tm.addTableModelListener(new TableModelListener(){
            public final void tableChanged(final TableModelEvent e){
                if(e.getFirstRow() <= lastSelectedRow && e.getLastRow() >= lastSelectedRow) {
                    setSliderDeviation(tm.getDeviations()[lastSelectedRow]);
                }
                
            }
        });
        
        SelectionPanel selp = new SelectionPanel(this);
        addComponent(selp);
        
        fvsp = new FixVariablesSliderPanel();
        fvsp.addSliderListener(new javax.swing.event.ChangeListener() {
            public final void stateChanged(final javax.swing.event.ChangeEvent evt) {
                if(updateDevVal){
                    if (fvsp.whichSlider(evt.getSource())==1){
                        hardChange=true;
                        final double newValue = 8*sds[lastSelectedRow]*(double)(fvsp.getValue()-50)/100+medians[lastSelectedRow];
                        tm.setValue(newValue,lastSelectedRow);
                    } else {
                        final double newDeviation = 4*sds[lastSelectedRow]*(double)fvsp.getDeviation()/100;
                        tm.setDeviation(newDeviation, lastSelectedRow);
                    }
                    tm.fireTableDataChanged();
                    
                    if(!((JSlider)evt.getSource()).getValueIsAdjusting() && fvsp.getAutoUpdate())  {
                        update();
                    }
                    
                }
            }
        });
        addComponent(fvsp);
        
        setData(data,varPos);
        resetSize();
        
        addListSelectionListener(new ListSelectionListener() {
            public final void valueChanged(final ListSelectionEvent e) {
                final int row = getSelectedRow();
                if(row!=-1)  {
                    lastSelectedRow = row;
                }
                
                setSliderDeviation(tm.getDeviations()[lastSelectedRow]);
            }
        });
        addListSelectionListener(new ListSelectionListener() {
            public final void valueChanged(final ListSelectionEvent e) {
                final int row = getSelectedRow();
                if(row!=-1)  {
                    lastSelectedRow = row;
                }
                
                setSliderValue(tm.getValues()[lastSelectedRow]);
            }
        });
        
        hardChange=false;
        pack();
    }
    
    public void setData(final Data data, final int varPos){
        // estimating default values for FixVariablesDialog
        // uses median and 1/3*standard deviation
        final int numVars = data.getNumberOfVariables()-1;
        final double[] values = new double[numVars];
        final double[] deviations = new double[numVars];
        for (int i=0; i<numVars;i++){
            int j=i+1;
            if (i>=varPos)  {
                j++;
            }
            
            try{
                final RserveConnection rcon = RserveConnection.getRconnection();
                values[i] = rcon.eval("as.double(median(" + data.getRname() + "[," + j + "]))").asDouble();
                deviations[i] = rcon.eval("as.double(sd(" + data.getRname() + "[," + j + "])/3)").asDouble();
            } catch (RSrvException rse){
                ErrorDialog.show(this,"Rserve exception in SVM.createPlot(Frame): "+rse.getMessage());
            }
        }
        
        final Vector variables = new Vector(data.getVariables());
        variables.remove(varPos);
        
        final Vector dataVector = new Vector(variables.size());
        for(int i=0; i<variables.size(); i++){
            final Vector row = new Vector(4);
            row.add(new Boolean(false));
            row.add(variables.elementAt(i));
            row.add(new Double(values[i]));
            row.add(new Double(deviations[i]));
            dataVector.add(row);
        }
        final Object[] columnIdentifiers = {"Fixed", "Variable", "Value", "Deviation"};
        tm.setDataVector(dataVector, columnIdentifiers);
        
        medians = new double[values.length];
        System.arraycopy(values, 0, medians, 0,values.length);
        
        sds = new double[deviations.length];
        System.arraycopy(deviations, 0, sds, 0,deviations.length);
        setSliderDeviation(deviations[lastSelectedRow]);
        
        //set fix-checkBoxes to true
        final boolean trueArray[] = new boolean[tm.getRowCount()];
        for(int i=0; i< trueArray.length; i++)  {
            trueArray[i] = true;
        }
        
        tm.setFixed(trueArray);
    }
    
    private void setSliderDeviation(final double deviation){
        updateDevVal=false;
        fvsp.setDeviation((int)(deviation/sds[lastSelectedRow]*(double)100/4));
        updateDevVal=true;
    }
    
    private void setSliderValue(final double value){
        updateDevVal=false;
        fvsp.setValue((int)((value-medians[lastSelectedRow])/sds[lastSelectedRow]*(double)100/8)+50);
        updateDevVal=true;
    }
    
    public String getFormula() {
        return formula;
    }
    
    public String getFixedVariables(){
        return fixedVariables;
    }
    
    public String getSubsetExpression(){
        return subsetExpression;
    }
    
    private boolean evaluate(){
        final Vector variables=tm.getVariables();
        if(variables.size()<3)  {
            return true;
        }
        
        
        final Vector fixedVariablesVector = new Vector();
        final int formulaVariables[] = new int[2];
        
        final boolean fixed[]=tm.getFixed();
        final double values[]=tm.getValues();
        final double deviations[] = tm.getDeviations();
        
        int j=0;
        for(int i=0; i<variables.size() && j<3; i++){
            if(fixed[i])  {
                fixedVariablesVector.add(new Integer(i));
            }
            
            else {
                if (j<2)  {
                    formulaVariables[j]=i;
                }
                
                j++;
            }
        }
        
        if (j!=2) {
            ErrorDialog.show(this,"There must be exactly two non-fixed variables.");
            return false;
        } else {
            formula=variables.elementAt(formulaVariables[0]) + "~" + variables.elementAt(formulaVariables[1]);
            fixedVariables="";
            subsetExpression="";
            boolean comma = false;
            for(final Enumeration e = fixedVariablesVector.elements(); e.hasMoreElements();){
                if(comma) {
                    fixedVariables += ",";
                    subsetExpression += " & ";
                } else  {
                    comma=true;
                }
                
                
                final int i=((Integer)e.nextElement()).intValue();
                fixedVariables += variables.elementAt(i) + "=" + values[i];
                subsetExpression += "abs(" + variables.elementAt(i) + "-" + values[i] + ")<=" + deviations[i];
            }
            
            return true;
        }
    }
    
    protected void ok() {
        if(evaluate()){
            success = true;
            int message=0;
            if (hardChange)  {
                message = SimpleChangeEvent.HARD_CHANGE;
            }
            
            fireSimpleChange(message);
            hardChange=false;
            dispose();
        }
    }
    
    private final class TableModel extends TableDialog.TableModel {
        
        public TableModel(){
            final Class[] colClasses = {Boolean.class, String.class, Double.class, Double.class};
            final boolean[] colEditable = {true, false, true, true};
            
            setColumnClasses(colClasses);
            setColumnEditable(colEditable);
        }
        
        public void setVariables(final Vector vars){
            setColumnData(1, vars.toArray());
        }
        
        
        public boolean[] getFixed() {
            final Object data[] = getColumnData(COL_FIXED);
            final boolean fixed[] = new boolean[data.length];
            for(int i=0; i<data.length; i++)  {
                fixed[i] = ((Boolean)data[i]).booleanValue();
            }
            
            return fixed;
        }
        
        public double[] getValues() {
            final Object data[] = getColumnData(COL_VALUE);
            final double values[] = new double[data.length];
            for(int i=0; i<data.length; i++)  {
                values[i] = ((Double)data[i]).doubleValue();
            }
            
            return values;
        }
        
        public void setValues(final double[] values){
            final Object data[] = new Object[values.length];
            for(int i=0; i<data.length; i++)  {
                data[i] = new Double(values[i]);
            }
            
            setColumnData(COL_VALUE, data);
        }
        
        public void setDeviations(final double[] deviations){
            final Object data[] = new Object[deviations.length];
            for(int i=0; i<data.length; i++)  {
                data[i] = new Double(deviations[i]);
            }
            
            setColumnData(COL_DEVIATION, data);
        }
        
        public void setFixed(final boolean[] fixed){
            final Object data[] = new Object[fixed.length];
            for(int i=0; i<data.length; i++)  {
                data[i] = new Boolean(fixed[i]);
            }
            
            setColumnData(COL_FIXED, data);
        }
        
        public double[] getDeviations(){
            final Object data[] = getColumnData(COL_DEVIATION);
            final double deviations[] = new double[data.length];
            for(int i=0; i<data.length; i++)  {
                deviations[i] = ((Double)data[i]).doubleValue();
            }
            
            return deviations;
        }
        
        public Vector getVariables() {
            return convertToVector(getColumnData(COL_VARIABLE));
        }
        
        public void setDeviation(final double deviation, final int row){
            setValueAt(new Double(deviation), row, COL_DEVIATION);
        }
        
        public void setValue(final double value, final int row){
            setValueAt(new Double(value), row, COL_VALUE);
        }
        
        /**
         * Sets the object value for the cell at <code>column</code> and
         * <code>row</code>.  <code>aValue</code> is the new value.  This method
         * will generate a <code>tableChanged</code> notification.
         *
         * This method was changed in order to indicate a hard change when the user
         * changes which fariables are fixed.
         *
         * @param   aValue          the new value; this can be null
         * @param   row             the row whose value is to be changed
         * @param   column          the column whose value is to be changed
         * @exception  ArrayIndexOutOfBoundsException  if an invalid row or
         *               column was given
         */
        public void setValueAt(final Object aValue, final int row, final int column) {
            if (column==0)  {
                hardChange=true;
            }
            
            super.setValueAt(aValue, row, column);
        }
        
        
    }
    
    protected void update() {
        if(evaluate()){
            int message=0;
            if (hardChange)  {
                message = SimpleChangeEvent.HARD_CHANGE;
            }
            
            fireSimpleChange(message);
        }
        hardChange=false;
    }
    
    public boolean getHardChange(){
        return hardChange;
    }
    
    public FixVariablesDialogSnapshotIF createSnapshot(){
        final Snapshot snapshot = new Snapshot();
        snapshot.setDeviations(tm.getDeviations());
        snapshot.setFixed(tm.getFixed());
        snapshot.setValues(tm.getValues());
        snapshot.setMedians(medians);
        snapshot.setSds(sds);
        
        return snapshot;
    }
    
    public void restoreSnapshot(final FixVariablesDialogSnapshotIF fvdSnapIF){
        final Snapshot snap = (Snapshot)fvdSnapIF;
        
        tm.setValues(snap.getValues());
        tm.setDeviations(snap.getDeviations());
        tm.setFixed(snap.getFixed());
        medians = snap.getMedians();
        sds = snap.getSds();
        
        setSliderDeviation(tm.getDeviations()[lastSelectedRow]);
        setSliderValue(tm.getValues()[lastSelectedRow]);
        
        tm.fireTableDataChanged();
        update();
    }
    
    private static final class Snapshot implements FixVariablesDialogSnapshotIF {
        public String Rname;
        public boolean fixed[];
        public double values[];
        public double deviations[];
        public double medians[];
        public double sds[];
        
        public Snapshot(){
            Rname = "snap" + hashCode();
        }
        
        
        public double[] getDeviations() {
            return (double[])this.deviations.clone();
        }
        
        public void setDeviations(final double[] deviations) {
            this.deviations = (double[])deviations.clone();
        }
        
        public boolean[] getFixed() {
            return (boolean[])this.fixed.clone();
        }
        
        public void setFixed(final boolean[] fixed) {
            this.fixed = (boolean[])fixed.clone();
        }
        
        public double[] getValues() {
            return (double[])this.values.clone();
        }
        
        public void setValues(final double[] values) {
            this.values = (double[])values.clone();
        }
        
        public double[] getMedians() {
            return (double[])this.medians.clone();
        }
        
        public void setMedians(final double[] medians) {
            this.medians = (double[])medians.clone();
        }
        
        public double[] getSds() {
            return (double[])this.sds.clone();
        }
        
        public void setSds(final double[] sds) {
            this.sds = (double[])sds.clone();
        }
    }
    
    private static final int COL_FIXED = 0;
    private static final int COL_VARIABLE = 1;
    private static final int COL_VALUE = 2;
    private static final int COL_DEVIATION = 3;
    
    public void selectNothing() {
        hardChange=true;
        tm.selectNothing(COL_FIXED);
    }
    
    public void selectAll() {
        hardChange=true;
        tm.selectAll(COL_FIXED);
    }
    
    public void invertSelection() {
        hardChange=true;
        tm.invertSelection(COL_FIXED);
    }
    
}
