package org.rosuda.JGR.toolkit;

//
//  RStyledDocument.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import javax.swing.text.*;

public class RStyledDocument extends DefaultStyledDocument implements StyledDocument{

    public RStyledDocument() {
    }

    /** because of a bug in the apple jre we need to tell java everytime to use the current fontsize */
    public void insertString(int offset, String str, AttributeSet a) throws
        BadLocationException {
        if (a == null)
            a = Preferences.SIZE;
        else
            StyleConstants.setFontSize( (MutableAttributeSet) a,
                                       Preferences.FontSize);
        super.insertString(offset, str, a);
    }
}