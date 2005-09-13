/*
 * RadioButtonCellEditor.java
 *
 * Created on 27. Juli 2005, 11:28
 *
 */

package org.rosuda.JClaR;

import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;

/**
 *
 * @author tobias
 */
public final class RadioButtonEditor extends DefaultCellEditor implements ChangeListener {
    
    private JRadioButton jrb;
    private Vector listeners = new Vector();
    
    /** Creates a new instance of RadioButtonCellEditor */
    public RadioButtonEditor(final JRadioButton jrb) {
        super(new JCheckBox());
        this.jrb = jrb;
        jrb.addChangeListener(this);
    }
    
    /**
     * Implements the <code>TableCellEditor</code> interface.
     */
    public java.awt.Component getTableCellEditorComponent(final javax.swing.JTable table, final Object value, final boolean isSelected, final int row, final int column) {
        
        if(value instanceof Boolean){
            jrb.setSelected(((Boolean)value).booleanValue());
        }
        return jrb;
    }
    
    /**
     * Forwards the message from the <code>CellEditor</code> to
     * the <code>delegate</code>.
     * @see EditorDelegate#getCellEditorValue
     */
    public Object getCellEditorValue() {
        return new Boolean(jrb.isSelected());
    }

    public void stateChanged(final javax.swing.event.ChangeEvent e) {
        super.stopCellEditing();
    }
}
