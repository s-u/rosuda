package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import javax.swing.text.*;

/**
 *  JGRStyledDocument - apply styled when inserting text.
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDa 2003 - 2004 
 */

public class JGRStyledDocument extends DefaultStyledDocument implements StyledDocument{

    public static final String tabSizeAttribute = "tabSize";
    
    /** because of a bug in the apple jre_1.4.1 we need to tell java everytime to use the current fontsize */
    public void insertString(int offset, String str, AttributeSet a) throws
        BadLocationException {
        if (a == null) a = JGRPrefs.SIZE;
        else
            StyleConstants.setFontSize( (MutableAttributeSet) a, JGRPrefs.FontSize);
        super.insertString(offset, str, a);
    }
}