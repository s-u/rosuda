/*
 * SVMWindow.java
 *
 * Created on 9. August 2005, 13:51
 *
 */

package org.rosuda.JClaR;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;

/**
 *
 * @author tobias
 */
public final class SVMWindow extends ClassificationWindow {
    
    //TODO: add cross and tolerance spinners. fitted jcb?
    
    private double nu, cost, gamma, coef0;
    private int degree;
    
    private int grid;
    
    /** these variables have to be calculated according to the dataset
     *  since there is a feasibility check for nu
     */
    private double NU_MULTIPLYER, NU_SHIFT, MIN_NU, MAX_NU;
    
    private FixVariablesDialog fvd;
    private SVMPreferencesPanel spp;
    
    private SVM svm;
    private SVMClassificationPlot plot;
    private boolean restoring = false;
    private JCheckBoxMenuItem m_markSupportVectors;
    private JCheckBoxMenuItem m_markMisclassifiedPoints;
    
    /** Creates a new instance of SVMWindow */
    SVMWindow(final SVM svm) {
        super(svm);
        this.classifier=svm;
        this.svm=svm;
        
        m_markSupportVectors = new JCheckBoxMenuItem("mark support vectors in plot", true);
        m_markSupportVectors.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(plot!=null){
                    final boolean markSupportVectors;
                    if((markSupportVectors=m_markSupportVectors.getState())!=plot.isMarkSupportVectors()){
                        plot.setMarkSupportVectors(markSupportVectors);
                        updatePlot(false, CHANGE_TYPE_SOFT);
                    }
                }
            }
        });
        addDisplayMenuItem(m_markSupportVectors);
        
        m_markMisclassifiedPoints = new JCheckBoxMenuItem("mark misclassified points in plot", false);
        m_markMisclassifiedPoints.addActionListener(new ActionListener(){
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(plot!=null){
                    final boolean markMisclassifiedPoints;
                    if((markMisclassifiedPoints=m_markMisclassifiedPoints.getState())!=plot.isMarkMisclassifiedPoints()){
                        plot.setMarkMisclassifiedPoints(markMisclassifiedPoints);
                        updatePlot(false, CHANGE_TYPE_SOFT);
                    }
                }
            }
        });
        addDisplayMenuItem(m_markMisclassifiedPoints);
        
        plot = (SVMClassificationPlot)getPlot();
        updateCheckBoxMenus();
        
        setTitle("SVM #" + svm.getNumber());
        
        final SVMPanel svmPanel = new SVMPanel();
        svmPanel.setSVM(svm);
        svmPanel.setParent(this);
        svmPanel.initFields();
        svmPanel.addSimpleChangeListener(this);
        setSidePanel(svmPanel);
        
        createFixVariablesDialog();
        
        PreferencesDialog prefd = new PreferencesDialog(this);
        spp = new SVMPreferencesPanel();
        //prefd.addPreferencesPanel("SVM", spp);
        setPreferencesDialog(prefd);
    }
    
    private void createFixVariablesDialog(){
        fvd = new FixVariablesDialog(this, data, svm.getVariablePos());
        fvd.addSimpleChangeListener(new SimpleChangeListener(){
            public final void stateChanged(final SimpleChangeEvent evt){
                fvd = (FixVariablesDialog)evt.getSource();
                plotSVM(evt.getMessage()==SimpleChangeEvent.HARD_CHANGE);
            }
        });
    }
    
    void updatePlot(final boolean doSnapshot, final int changeType){
        if(plot!=null){
            if (changeType!=CHANGE_TYPE_RESIZE) {
                plot.createPlotCall(changeType==CHANGE_TYPE_HARD);
            }
            
            setPlotGraphic(new ImageIcon(plot.plot(getPreferredPlotSize())));
            if (doSnapshot)  {
                probablyDoSnapshot();
            }
            
        }
    }
    
    void probablyDoSnapshot(){
        if(getDoSnapshots() && !restoring){
            addSnaphot(new SnapshotContainer(classifier.createSnapshot(), fvd.createSnapshot()));
        }
    }
    
    private void retrain(){
        svm.train();
        updateConfusionMatrix();
    }
    
    void restore(final SnapshotContainer snapC){
        if(snapC!=null){
            restoring = true;
            
            svm.restoreSnapshot(snapC.getSvmSnap());
            fvd.restoreSnapshot(snapC.getFvdSnap());
            
            SVMClassificationPlot newPlot = (SVMClassificationPlot)svm.getPlot();
            adjustPlotToCheckBoxMenus(newPlot);
            setPlot(newPlot);
            plot.setFixVariablesDialog(fvd);
            updatePlot(false, CHANGE_TYPE_HARD);
            
            //Scaled + Tolerance!!
            restoring = false;
        }
    }
    
    void plotClassifier(final boolean hardChange){
        plotSVM(hardChange);
    }
    
    private void plotSVM(final boolean hardChange){
        if(svm.getTrained()){
            switch(svm.getType()){
                case SVM.TYPE_C_CLASS:
                case SVM.TYPE_NU_CLASS:
                    if (!fvd.isShowing() && svm.getData().getNumberOfVariables()-1>2 && !restoring){
                        fvd.show();
                        return;
                    }
                    if (plot==null) {
                        SVMClassificationPlot newPlot = new SVMClassificationPlot(svm, this, fvd);
                        adjustPlotToCheckBoxMenus(newPlot);
                        setPlot(newPlot);
                        newPlot.createPlotCall();
                    } else{
                        if (fvd.isShowing()) {
                            fvd.show();
                        }
                        
                        
                        plot.setClassifier(svm);
                        plot.setFixVariablesDialog(fvd);
                        plot.createPlotCall(hardChange);
                    }
                    svm.setPlot(plot);
                    break;
                case SVM.TYPE_ONE_CLASS:
                    //TODO: not really implemented
                    //setPlot(new SVMOneClassPlot(this));
                    break;
                default:
                    break;
                    //TODO: not really implemented
                    //setPlot(new SVMRegressionPlot(this));
            }
            final java.awt.image.BufferedImage bi = plot.plot(getPreferredPlotSize());
            if (bi!=null) {
                setPlotGraphic(new ImageIcon(bi));
                probablyDoSnapshot();
            }
        } else  {
            ErrorDialog.show(this,"SVM not trained yet.");
        }
        
    }
    
    public void stateChanged(final SimpleChangeEvent e) {
        switch (e.getMessage()){
            case SidePanel.EVT_TRAIN:
                retrain();
                break;
            case SidePanel.EVT_UPDATE_PLOT:
                updatePlot();
                break;
            default:
                break;
        }
    }
    
    void setPlot(final Plot newPlot) {
        this.plot = (SVMClassificationPlot)newPlot;
        updateCheckBoxMenus();
        super.setPlot(newPlot);
    }
    
    /**
     * Sets the checkBoxMenus' states according to plot.
     */
    void updateCheckBoxMenus(){
        if(plot!=null){
            m_markMisclassifiedPoints.setState(plot.isMarkMisclassifiedPoints());
            m_markSupportVectors.setState(plot.isMarkSupportVectors());
        }
        super.updateCheckBoxMenus();
    }
    
    /**
     * Sets plot fields according to checkBoxMenus' states.
     */
    void adjustPlotToCheckBoxMenus(Plot newPlot) {
        ((SVMClassificationPlot)newPlot).setMarkMisclassifiedPoints(m_markMisclassifiedPoints.getState());
        ((SVMClassificationPlot)newPlot).setMarkSupportVectors(m_markSupportVectors.getState());
        super.adjustPlotToCheckBoxMenus(newPlot);
    }
    
    public void preferenceChange(java.util.prefs.PreferenceChangeEvent evt) {
        super.preferenceChange(evt);
    }
    
    
    
}
