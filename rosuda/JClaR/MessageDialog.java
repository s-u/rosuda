/*
 * MessageDialog.java
 *
 * Created on 29. April 2005, 10:06
 */

package org.rosuda.JClaR;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author tobias
 */
public class MessageDialog extends JOptionPane {
    
    /** Creates a new instance of MessageDialog */
    public MessageDialog() {
    }
    
    public static void show(final Component parent,final String message){
        showMessageDialog(parent, message);
    }
    
    protected static final void show(final Component parent,final String message, final String title, final int messageType){
        showMessageDialog(parent, message, title, messageType);
    }
    
}
