/*
 * ClassifyingDialog.java
 *
 * Created on 1. März 2006, 12:39
 *
 */

package org.rosuda.JClaR;

import java.awt.Frame;
import java.text.NumberFormat;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;

/**
 *
 * @author tobias
 */
public class ClassifyingDialog extends ListeningDialog implements SimpleChangeListener {
    
    private JEditorPane jepClassifyingResults;
    private JCheckBox jcbAutoReclassify;
    private Classifier classifier;
    
    private NumberFormat numberFormat;
    
    /** Creates a new instance of ClassifyingDialog */
    public ClassifyingDialog(Frame parent, boolean modal, Classifier classifier) {
        super(parent,modal);
        
        setUpdateButton(false);
        setOkButtonText("Classify",'c');
        
        this.classifier = classifier;
        
        jepClassifyingResults = new JEditorPane();
        jepClassifyingResults.setEditable(false);
        addCenterComponent(jepClassifyingResults);
        
        jcbAutoReclassify = new JCheckBox("reclassify automatically",true);
        addComponent(jcbAutoReclassify);
        
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(5);
        
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
        
        if(classifier.hasAccuracyOfPrediction()){
            info += "Accuracy: " + numberFormat.format(classifier.getAccuracyOfPrediction());
        }
        
        jepClassifyingResults.setText(info);
    }
    
    boolean autoReclassify() {
        return jcbAutoReclassify.isSelected();
    }
}
