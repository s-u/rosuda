package org.rosuda.JGR.toolkit;

/**
 *  JGRStyledDocument
 * 
 * 	is needed because of bug in apples java runtime
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */


import javax.swing.text.*;

public class JGRStyledDocument extends DefaultStyledDocument implements StyledDocument{

    public static final String tabSizeAttribute = "tabSize";
    
    public JGRStyledDocument() {
    }

    /** because of a bug in the apple jre we need to tell java everytime to use the current fontsize */
    public void insertString(int offset, String str, AttributeSet a) throws
        BadLocationException {
        if (a == null)
            a = JGRPrefs.SIZE;
        else
            StyleConstants.setFontSize( (MutableAttributeSet) a,
                                       JGRPrefs.FontSize);
           //System.out.println(a);
        super.insertString(offset, str, a);
    }
    
    public String getText(int offs, int len) throws BadLocationException {
    	return super.getText(offs,len);
    }
}