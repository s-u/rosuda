/*
 * Main.java
 *
 * Created on 21. Februar 2005, 22:00
 */

package org.rosuda.JClaR;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public class Main {
    
    private static String last_directory;
    private static int lastGivenNumber=0;

    static final String VERSION = "0.5.1";
    
    /** Creates a new instance of Main */
    private Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        last_directory=System.getProperty("user.dir");
        
        try{
            if(!RserveConnection.evalB("require(e1071)")){
                ErrorDialog.show(null,"R package e1071 not found.");
                System.exit(-1);
            }
            if(!RserveConnection.evalB("require(GDD)")){
                if(!RserveConnection.evalB("capabilities('jpeg')")){
                    ErrorDialog.show(null,"R package GDD not found. Neither png nor jpeg device available. " +
                            "Plotting will be disabled");
                    Plot.setDevice(Plot.DEVICE_NO);
                } else Plot.setDevice(Plot.DEVICE_JPG);
            } else Plot.setDevice(Plot.DEVICE_GDD);
        } catch(RSrvException rse) {
            ErrorDialog.show(null,"Rserve exception in MainWindow(): "+rse.getMessage());
            System.exit(-1);
        }
        
        final MainWindow mw = new MainWindow();
        mw.setVisible(true);
        
        //WindowManager.newWindow();
    }
    
    
    static final String getLast_directory() {
        return last_directory;
    }
    
    static final void setLast_directory(final String aLast_directory) {
        last_directory = aLast_directory;
    }
    
    static final int getNewClassifierNumber(){
        return ++lastGivenNumber;
    }
    
}
