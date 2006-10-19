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
    /**
     * FIXME: serialVersionUID field auto-generated by RefactorIT
     */
    protected static final long serialVersionUID = 200602271310L;
    
    /** Creates a new instance of MessageDialog */
    protected MessageDialog() {
    }
    
    private static final void show(final Component parent,final String message){
        showMessageDialog(parent, message);
    }
    
    static final void show(final Component parent,final String message, final String title){
        showMessageDialog(parent,message,title,INFORMATION_MESSAGE);
    }
    
    static final void show(final Component parent,final String message, final String title, final int messageType){
        showMessageDialog(parent, message, title, messageType);
    }
    
}
