/*
 * HelpDialog.java
 *
 * Created on 29. April 2005, 10:00
 */

package org.rosuda.JClaR;

import java.awt.Component;

/**
 *
 * @author tobias
 */
public final class HelpDialog extends MessageDialog {
    
    /** Creates a new instance of HelpDialog */
    public HelpDialog() {
    }
    
    public static void show(final Component parent, final String text){
        show(parent, text, "Help", INFORMATION_MESSAGE);
    }
    
}
