/*
 * ErrorDialog.java
 *
 * Created on 26. April 2005, 14:56
 */

package org.rosuda.JClaR;

import java.awt.Component;
import java.awt.Frame;
import org.rosuda.JRclient.RSrvException;

/**
 *
 * @author tobias
 */
public final class ErrorDialog extends MessageDialog {
    
    private static final boolean DEBUG = false;
    
    private static final String RSERVE_OUTPUT_HINT = "Check Rserve output for more information.";
    
    /** Creates a new instance of ErrorDialog */
    private ErrorDialog() {
    }
    
    static void show(Component parent, final String message){
        show(parent,message,DEBUG);
    }
    
    private static void show(Component parent, final String message, boolean show_hint){
        if(parent==null){
            parent = new Frame();
        }
        if(show_hint) show(parent, message + "\n"
                + RSERVE_OUTPUT_HINT + "\nLast R call: " + RserveConnection.getLastRcall(), "Error", ERROR_MESSAGE);
        else show(parent, message, "Error", ERROR_MESSAGE);
    }
    
    static void show(Component parent, final RSrvException rse, final String method){
        show(parent, rse, method,DEBUG);
    }
    
    private static void show(Component parent, final RSrvException rse, final String method, boolean show_hint){
        if(parent==null){
            parent = new Frame();
        }
        if(show_hint) show(parent, "Rserve error in " + method + ":\n"
                + rse.getMessage() + "\n" + RSERVE_OUTPUT_HINT, "Error", ERROR_MESSAGE);
        else show(parent, "Rserve error in " + method + ":\n"
                + rse.getMessage() + "\nLast R call: " + RserveConnection.getLastRcall(), "Error", ERROR_MESSAGE);
    }
}
