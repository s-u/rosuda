/*
 * Created on Dec 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rosuda.JGR.toolkit;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
	
	public void setEditable(boolean b) {
		if (b && tf != null) {
			tf.requestFocus();
			tf.select(0,tf.getText().length());
		}
		super.setEditable(b);
	}
	
}