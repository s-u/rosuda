/*
 * ClassifyingDialog.java
 *
 * Created on 1. März 2006, 12:39
 *
 */

package org.rosuda.JClaR;

import java.awt.Frame;
import javax.swing.JEditorPane;

/**
 *
 * @author tobias
 */
public class ClassifyingDialog extends ListeningDialog implements SimpleChangeListener {
    
    private JEditorPane jepClassifyingResults;
    private Classifier classifier;
    
    /** Creates a new instance of ClassifyingDialog */
    public ClassifyingDialog(Frame parent, boolean modal, Classifier classifier) {
        super(parent,modal);
        
        setUpdateButton(false);
        setOkButtonText("Classify",'c');
        
        this.classifier = classifier;
        
        jepClassifyingResults = new JEditorPane();
        jepClassifyingResults.setEditable(false);
        addCenterComponent(jepClassifyingResults);
        
        updateInfo();
    }
    
    
    
    void ok() {
        classifier.reclassify();
        updateInfo();
    }
    
    public void stateChanged(SimpleChangeEvent e) {
        updateInfo();
    }
    
    private void updateInfo() {
        String info="";
        
        info += classifier.getAccuracyOfPrediction();
        
        jepClassifyingResults.setText(info);
    }
}
