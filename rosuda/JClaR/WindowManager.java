/*
 * WindowManager.java
 *
 * Created on 27. Juli 2005, 15:49
 *
 */

package org.rosuda.JClaR;

import java.awt.Frame;
import java.io.File;
import java.util.Vector;
import org.rosuda.JRclient.RSrvException;



/**
 *
 * @author tobias
 */
public final class WindowManager {
    
    private static Vector windows = new Vector();
    
    /** Creates a new instance of WindowManager */
    public WindowManager() {
    }
    
    public static void newWindow(Data data){
        
        
        final SVM svm;
        final ChooseVariableDialog cvd = new ChooseVariableDialog(null,data.getVariables());
        cvd.setText("Please select the variable that contains the classes.");
        cvd.show();
        final int variablePos = cvd.getVariable();
        if (variablePos>-1){
            int[] unused = cvd.getUnusedVariables();
            final Data preparedData;
            if (unused!=null && unused.length>0){
                preparedData = data.getRestrictedData(cvd.getUnusedVariables());
            } else {
                preparedData = data;
            }
            preparedData.unclass(variablePos);
            preparedData.refactor(variablePos);
            svm=new SVM(preparedData,variablePos);
            if (svm!=null){
                final ClassificationWindow window = new SVMWindow(svm);
                windows.add(window);
                window.show();
            }
        } else  {
            probablyExit();
        }
    }
    
    public static void newWindow(){
        
        final Data data = new Data();
        
        DataFileOpenDialog dfod = new DataFileOpenDialog(new Frame(), Main.getLast_directory(), data.getRname());
        switch(dfod.getStatus()){
            case DataFileOpenDialog.STATUS_SUCCESS:
                
                File file = dfod.getSelectedFile();
                data.setFile(file);
                //TODO: check if dataset has been opened already and ask what to do.
                DatasetManager.addDataset(data);
                
                data.removeNAs();
                try{
                    data.update();
                } catch (NullPointerException ex){
                    ErrorDialog.show(null, "Error reading file " + data.getPath() + ".");
                    probablyExit();
                }
                if(data.getNumberOfVariables()<2){
                    ErrorDialog.show(null,"Too few variables in dataset " + data.getPath() + ".");
                    probablyExit();
                } else{
                    newWindow(data);
                }
                break;
            case DataFileOpenDialog.STATUS_ERROR:
                ErrorDialog.show(null, "Error reading file " + dfod.getSelectedFile().getAbsolutePath() + ".");
                probablyExit();
                break;
            case DataFileOpenDialog.STATUS_CANCELED:
            default: // dialog closed or canceled
                if (windows.size()==0){
                    System.exit(0);
                }
                break;
        }
        
    }
    
    public static void closeWindow(final ClassificationWindow window){
        if (window!=null && window.isShowing())  {
            window.dispose(false);
        }
        
        windows.remove(window);
        probablyExit();
    }
    
    public static void probablyExit(){
        if (windows.size()==0)  {
            //System.exit(0);
            newWindow();
        }
    }
    
    public static int getNumberOfWindows(){
        return windows.size();
    }
}
