/*
 * RadioButtonRenderer.java
 *
 * Created on 27. Juli 2005, 11:46
 *
 */

package org.rosuda.JClaR;

import java.awt.Color;
import javax.swing.JRadioButton;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author tobias
 */
public final class RadioButtonRenderer implements TableCellRenderer {
    
    private final JRadioButton jrb;    
    
    /** Creates a new instance of RadioButtonRenderer */
    RadioButtonRenderer() {
        jrb = new JRadioButton();
        jrb.setBackground(Color.WHITE);
    }
    
    /**
     *  Returns the component used for drawing the cell.  This method is
     *  used to configure the renderer appropriately before drawing.
     *
     * @param	table		the <code>JTable</code> that is asking the
     * 				renderer to draw; can be <code>null</code>
     * @param	value		the value of the cell to be rendered.  It is
     * 				up to the specific renderer to interpret
     * 				and draw the value.  For example, if
     * 				<code>value</code>
     * 				is the string "true", it could be rendered as a
     * 				string or it could be rendered as a check
     * 				box that is checked.  <code>null</code> is a
     * 				valid value
     * @param	isSelected	true if the cell is to be rendered with the
     * 				selection highlighted; otherwise false
     * @param	hasFocus	if true, render cell appropriately.  For
     * 				example, put a special border on the cell, if
     * 				the cell can be edited, render in the color used
     * 				to indicate editing
     * @param	row	        the row index of the cell being drawn.  When
     * 				drawing the header, the value of
     * 				<code>row</code> is -1
     * @param	column	        the column index of the cell being drawn
     */
    public java.awt.Component getTableCellRendererComponent(final javax.swing.JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        if (value instanceof Boolean)  {
            jrb.setSelected(((Boolean)value).booleanValue());
        }
        
        return jrb;
    }
    
}
