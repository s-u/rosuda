package org.rosuda.JGR.toolkit;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * 
 * JComboBoxExt - add JTextField in a JComboBox.
 * 
 * @author Markus Helbig
 *
 * RoSuDA 2003 - 2005
 * 
 */

public class JComboBoxExt extends JComboBox {
	
	private JTextField tf;
	
	public class CBDocument extends PlainDocument {
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
			super.insertString(offset,str,a);
		}
	}
	
	public JComboBoxExt(String[] str) {
		super(str);
		if (getEditor() != null) {
			tf = (JTextField) getEditor().getEditorComponent();
			tf.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				}
				
				public void keyReleased(KeyEvent e) {
				}
			});
			if (tf != null) {
				tf.setDocument(new CBDocument());
			}
		}
	}
	
	/**
	 * Set the combobox editable.
	 */
	public void setEditable(boolean b) {
		if (b && tf != null) {
			tf.requestFocus();
			tf.select(0,tf.getText().length());
		}
		super.setEditable(b);
	}
	
}