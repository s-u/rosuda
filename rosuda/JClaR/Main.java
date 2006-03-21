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

    static String VERSION = "0.5.1";
    
    /** Creates a new instance of Main */
    private Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final RserveConnection rcon = RserveConnection.getRconnection();
        last_directory=System.getProperty("user.dir");
        
        try{
            if(rcon.eval("require(e1071)").asBool().isFALSE()){
                ErrorDialog.show(null,"R package e1071 not found.");
                System.exit(-1);
            }
            if(rcon.eval("require(GDD)").asBool().isFALSE()){
                if(rcon.eval("capabilities('jpeg')").asBool().isFALSE()){
                    ErrorDialog.show(null,"R package GDD not found. Neither png nor jpeg device available. " +
                            "Plotting will be disabled");
                    Plot.setDevice(Plot.DEVICE_NO);
                } else Plot.setDevice(Plot.DEVICE_JPG);
            } else Plot.setDevice(Plot.DEVICE_GDD);
        } catch(RSrvException rse) {
            ErrorDialog.show(null,"Rserve exception in MainWindow(): "+rse.getMessage());
            System.exit(-1);
        }
        
        MainWindow mw = new MainWindow();
        mw.show();
        
        //WindowManager.newWindow();
    }
    
    
    static String getLast_directory() {
        return last_directory;
    }
    
    static void setLast_directory(String aLast_directory) {
        last_directory = aLast_directory;
    }
    
    static int getNewClassifierNumber(){
        return ++lastGivenNumber;
    }
    
}
