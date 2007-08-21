/*
 * SVMWindow.java
 *
 * Created on 9. August 2005, 13:51
 *
 */

package org.rosuda.JClaR;

import java.awt.Image;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;

/**
 *
 * @author tobias
 */
public final class SVMWindow extends ClassificationWindow {
    static final long serialVersionUID = 200708211247L;
    
    //TODO: add cross and tolerance spinners. fitted jcb?
    
    private double nu, cost, gamma, coef0;
    private int degree;
    
    private int grid;
    
    /** these variables have to be calculated according to the dataset
     *  since there is a feasibility check for nu
     */
    private double NU_MULTIPLYER, NU_SHIFT, MIN_NU, MAX_NU;
    
    private FixVariablesDialog fvd;
    private final SVMPreferencesPanel spp;
    
    private SVM svm;
    //private SVMClassificationPlot plot;
    private boolean restoring = false;
    private final JCheckBoxMenuItem m_markSupportVectors;
    private final JCheckBoxMenuItem m_markMisclassifiedPoints;
    
    /** Creates a new instance of SVMWindow */
    SVMWindow(final SVM svm) {
	super(svm);
	this.classifier=svm;
	this.svm=svm;
	
	m_markSupportVectors = new JCheckBoxMenuItem("mark support vectors in plot", true);
	m_markSupportVectors.addActionListener(new ActionListener(){
	    public final void actionPerformed(final java.awt.event.ActionEvent e) {
		if(plot!=null){
		    final boolean markSupportVectors;
		    if((markSupportVectors=m_markSupportVectors.getState())!=((SVMClassificationPlot)plot).isMarkSupportVectors()){
			((SVMClassificationPlot)plot).setMarkSupportVectors(markSupportVectors);
			updatePlot(false, CHANGE_TYPE_SOFT);
		    }
		}
	    }
	});
	addDisplayMenuItem(m_markSupportVectors);
	
	m_markMisclassifiedPoints = new JCheckBoxMenuItem("mark misclassified points in plot", false);
	m_markMisclassifiedPoints.addActionListener(new ActionListener(){
	    public final void actionPerformed(final java.awt.event.ActionEvent e) {
		if(plot!=null){
		    final boolean markMisclassifiedPoints;
		    if((markMisclassifiedPoints=m_markMisclassifiedPoints.getState())!=((SVMClassificationPlot)plot).isMarkMisclassifiedPoints()){
			((SVMClassificationPlot)plot).setMarkMisclassifiedPoints(markMisclassifiedPoints);
			updatePlot(false, CHANGE_TYPE_SOFT);
		    }
		}
	    }
	});
	addDisplayMenuItem(m_markMisclassifiedPoints);
	
	//plot = (SVMClassificationPlot)getPlot();
	updateCheckBoxMenus();
	
	setTitle("SVM #" + svm.getNumber());
	
	final SVMPanel svmPanel = new SVMPanel();
	svmPanel.setSVM(svm);
	svmPanel.setParent(this);
	svmPanel.initFields();
	svmPanel.addSimpleChangeListener(this);
	setSidePanel(svmPanel);
	
	createFixVariablesDialog();
	
	
	spp = new SVMPreferencesPanel();
	//prefd.addPreferencesPanel("SVM", spp);
	final PreferencesDialog prefd = new PreferencesDialog(this);
	setPreferencesDialog(prefd);
    }
    
    private void createFixVariablesDialog(){
	fvd = new FixVariablesDialog(this, data, svm.getVariablePos());
	fvd.addSimpleChangeListener(new SimpleChangeListener(){
	    public final void stateChanged(final SimpleChangeEvent evt){
		fvd = (FixVariablesDialog)evt.getSource();
		if(showClassifiedPlot) plotClassifiedModel(evt.getMessage()==SimpleChangeEvent.HARD_CHANGE);
		else plotSVM(evt.getMessage()==SimpleChangeEvent.HARD_CHANGE);
	    }
	});
    }
    
    void updatePlot(final boolean doSnapshot, final int changeType){
	if(plot!=null){
	    if(showClassifiedPlot) svm.reclassify();
	    
	    if (changeType!=CHANGE_TYPE_RESIZE) {
		((SVMClassificationPlot)plot).createPlotCall(changeType==CHANGE_TYPE_HARD);
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
	if(cd!=null && cd.autoReclassify()){
	    svm.reclassify();
	    cd.stateChanged(null); //TODO: do this with listeners
	}
    }
    
    void restore(final SnapshotContainer snapC){
	if(snapC!=null){
	    restoring = true;
	    
	    svm.restoreSnapshot(snapC.getSvmSnap());
	    fvd.restoreSnapshot(snapC.getFvdSnap());
	    sidePanel.update();
	    
	    final SVMClassificationPlot newPlot = (SVMClassificationPlot)svm.getPlot();
	    adjustPlotToCheckBoxMenus(newPlot);
	    setPlot(newPlot);
	    ((SVMClassificationPlot)plot).setFixVariablesDialog(fvd);
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
			fvd.setVisible(true);
			return;
		    }
		    if (plot==null) {
			final SVMClassificationPlot newPlot = new SVMClassificationPlot(svm, this, fvd);
			adjustPlotToCheckBoxMenus(newPlot);
			setPlot(newPlot);
			newPlot.createPlotCall();
		    } else{
			if (fvd.isShowing()) {
			    fvd.setVisible(true);
			}
			
			
			plot.setClassifier(svm);
			((SVMClassificationPlot)plot).setFixVariablesDialog(fvd);
			((SVMClassificationPlot)plot).createPlotCall(hardChange);
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
	    final Image img = plot.plot(getPreferredPlotSize());
	    if (img!=null) {
		setPlotGraphic(new ImageIcon(img));
		probablyDoSnapshot();
	    }
	} else  {
	    ErrorDialog.show(this,"SVM not trained yet.");
	}
	
    }
    
    void plotClassifiedModel(final boolean hardChange){
	if(svm.hasClassifiedData()){
	    if (!fvd.isShowing() && svm.getData().getNumberOfVariables()-1>2 && !restoring){
		fvd.setVisible(true);
		return;
	    }
	    final SVMClassificationPlot newPlot = new SVMClassificationPlot(svm, this, fvd, true);
	    adjustPlotToCheckBoxMenus(newPlot);
	    setPlot(newPlot);
	    newPlot.createPlotCall();
	    svm.setPlot(newPlot);
	    final Image img = plot.plot(getPreferredPlotSize());
	    if (img!=null) {
		setPlotGraphic(new ImageIcon(img));
		probablyDoSnapshot();
	    }
	} else  {
	    ErrorDialog.show(this,"No classified data available.");
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
	    m_markMisclassifiedPoints.setState(((SVMClassificationPlot)plot).isMarkMisclassifiedPoints());
	    m_markSupportVectors.setState(((SVMClassificationPlot)plot).isMarkSupportVectors());
	}
	super.updateCheckBoxMenus();
    }
    
    /**
     * Sets plot fields according to checkBoxMenus' states.
     */
    void adjustPlotToCheckBoxMenus(final Plot newPlot) {
	((SVMClassificationPlot)newPlot).setMarkMisclassifiedPoints(m_markMisclassifiedPoints.getState());
	((SVMClassificationPlot)newPlot).setMarkSupportVectors(m_markSupportVectors.getState());
	super.adjustPlotToCheckBoxMenus(newPlot);
    }
}
