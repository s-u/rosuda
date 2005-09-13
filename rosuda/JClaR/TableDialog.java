/*
 * TableDialog.java
 *
 * Created on 23. Juni 2005, 13:51
 */

package org.rosuda.JClaR;

import java.awt.Component;
import java.awt.Dimension;
import java.util.EventListener;
import java.util.Vector;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author  tobias
 */
public abstract class TableDialog extends javax.swing.JDialog {
    
    protected boolean success=false;
    protected EventListenerList listeners=new EventListenerList();
    
    /** Creates new form TableDialog */
    public TableDialog(final java.awt.Frame parent) {
        this(parent, false);
    }
    
    public TableDialog(final java.awt.Frame parent, final boolean modal){
        super(parent,modal);
        initComponents();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //resetSize();
    }
    
    //TODO: resetSize necessary?
    protected final void resetSize(){
        final Dimension dim = table.getPreferredSize();
        double w = dim.getWidth();
        double h = dim.getHeight();
        if (h>200)  {
            h=200;
        }
        
        if (w>400)  {
            w=400;
        }
        
        dim.setSize(w,h);
        table.setPreferredScrollableViewportSize(dim);
        pack();
    }
    
    protected final void addListSelectionListener(final ListSelectionListener lSelListener){
        table.getSelectionModel().addListSelectionListener(lSelListener);
    }
    
    public final boolean getSuccess(){
        return success;
    }
    
    protected final void setTableModel(final AbstractTableModel tm){
        table.setModel(tm);
    }
    
    public final void addSimpleChangeListener(final SimpleChangeListener l){
        listeners.add(SimpleChangeListener.class, l);
    }
    
    public final void removeSimpleChangeListener(final SimpleChangeListener l){
        listeners.remove(SimpleChangeListener.class, l);
    }
    
    protected final int getSelectedRow(){
        return table.getSelectedRow();
    }
    
    protected class TableModel extends javax.swing.table.DefaultTableModel {
        
        private Class[] columnClasses;
        private boolean[] columnEditable;
        
        protected final void setColumnClasses(final Class[] columnClasses){
            this.columnClasses = columnClasses;
        }
        
        protected final void setColumnEditable(final boolean[] columnEditable){
            this.columnEditable = columnEditable;
        }
        
        
        public final Class getColumnClass(final int columnIndex){
            if (columnClasses!=null){
                try{
                    return columnClasses[columnIndex];
                } catch(ArrayIndexOutOfBoundsException e){                    /* CAUTION: empty block! */

                }
            }
            return super.getColumnClass(columnIndex);
        }
        
        public final boolean isCellEditable(final int rowIndex, final int columnIndex) {
            if (columnEditable!=null){
                try{
                    return columnEditable[columnIndex];
                } catch(ArrayIndexOutOfBoundsException e){                    /* CAUTION: empty block! */

                }
            }
            return super.isCellEditable(rowIndex, columnIndex);
        }
        
        public final void setDataVector(final Vector newDataVector, final Object[] newColumnIdentifiers){
            setDataVector(newDataVector, convertToVector(newColumnIdentifiers));
        }
        
        public final Object[] getColumnData(final int column){
            final Vector data = getDataVector();
            final Object ret[] = new Object[getRowCount()];
            for(int i=0; i< data.size(); i++){
                ret[i] = ((Vector)data.elementAt(i)).elementAt(column);
            }
            return ret;
        }
        
        public final void setColumnData(final int column, final Object[] data){
            for(int i=0; i<data.length; i++)  {
                ((Vector)dataVector.elementAt(i)).setElementAt(data[i], column);
            }
            
            fireTableDataChanged();
        }
        
        /**
         * Inverts the boolean values in the given column.
         * @param column Column to be inverted.
         */
        public final void invertSelection(final int column){
            if(!getColumnClass(column).equals(Boolean.class)) return; // can only invert booleans
            Object[] colData = getColumnData(column);
            for(int i=0; i<colData.length; i++)
                colData[i] = ((Boolean)colData[i]).booleanValue()?Boolean.FALSE:Boolean.TRUE;
            setColumnData(column, colData);
        }
        
        /**
         * Sets all boolean values to true.
         * @param column Column to be selected.
         */
        public final void selectAll(final int column){
            if(!getColumnClass(column).equals(Boolean.class)) return; // can only be applied to booleans
            Object[] colData = getColumnData(column);
            for(int i=0; i<colData.length; i++)
                colData[i] = Boolean.TRUE;
            setColumnData(column, colData);
        }
        
        /**
         * Sets all boolean values to false.
         * @param column Column to be unselected.
         */
        public final void selectNothing(final int column){
            if(!getColumnClass(column).equals(Boolean.class)) return; // can only be applied to booleans
            Object[] colData = getColumnData(column);
            for(int i=0; i<colData.length; i++)
                colData[i] = Boolean.FALSE;
            setColumnData(column, colData);
        }
    }
    
    protected final void setUpdateButton(final boolean hasUpdateButton){
        butUpdate.setVisible(hasUpdateButton);
    }
    
    protected final void setOkButtonText(String text, char mnemonic){
        butOK.setText(text);
        butOK.setMnemonic(mnemonic);
    }
    
    protected abstract void ok();

    protected void update(){        /* CAUTION: empty block! */

    }
    protected final void cancel(){
        success=false;
        dispose();
    }
    
    protected final void addComponent(final Component component){
        panSouth.add(component,0);
    }
    
    protected final void fireSimpleChange(final int message){
        final EventListener[] els = listeners.getListeners(SimpleChangeListener.class);
        for(int i=0; i<els.length; i++){
            ((SimpleChangeListener)els[i]).stateChanged(new SimpleChangeEvent(this,message));
        }
    }
    
    public final TableColumn getColumn(final int col){
        return table.getColumnModel().getColumn(col);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        panSouth = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        butOK = new javax.swing.JButton();
        butUpdate = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();

        jScrollPane1.setMaximumSize(new java.awt.Dimension(300, 150));
        table.setMaximumSize(new java.awt.Dimension(3000, 3000));
        table.setMinimumSize(new java.awt.Dimension(400, 50));
        jScrollPane1.setViewportView(table);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panSouth.setLayout(new javax.swing.BoxLayout(panSouth, javax.swing.BoxLayout.Y_AXIS));

        butOK.setMnemonic('o');
        butOK.setText("OK");
        butOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butOKActionPerformed(evt);
            }
        });

        jPanel2.add(butOK);

        butUpdate.setMnemonic('u');
        butUpdate.setText("Update");
        butUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butUpdateActionPerformed(evt);
            }
        });

        jPanel2.add(butUpdate);

        butCancel.setMnemonic('c');
        butCancel.setText("Cancel");
        butCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCancelActionPerformed(evt);
            }
        });

        jPanel2.add(butCancel);

        panSouth.add(jPanel2);

        getContentPane().add(panSouth, java.awt.BorderLayout.SOUTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    
    private final void butUpdateActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butUpdateActionPerformed
        update();
    }//GEN-LAST:event_butUpdateActionPerformed
    
    private final void butCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCancelActionPerformed
        cancel();
    }//GEN-LAST:event_butCancelActionPerformed
    
    private final void butOKActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOKActionPerformed
        ok();
    }//GEN-LAST:event_butOKActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOK;
    private javax.swing.JButton butUpdate;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel panSouth;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
    
}
