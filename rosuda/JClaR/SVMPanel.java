/*
 * SVMPanel.java
 *
 * Created on 8. August 2005, 22:03
 */

package org.rosuda.JClaR;

import java.awt.Dimension;
import java.text.NumberFormat;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author  tobias
 */
public final class SVMPanel extends SidePanel {
    
    private SVM svm;
    
    private double NU_SHIFT, NU_MULTIPLYER, MIN_NU, MAX_NU;
    
    /** Creates new form SVMPanel */
    SVMPanel() {
        initComponents();
        setMaximumSize(new Dimension(getMinimumSize().width, Integer.MAX_VALUE));
        setPreferredSize(getMinimumSize());
    }
    
    void setSVM(final SVM newSVM){
        this.svm=newSVM;
    }
    
    void initFields(){
        
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(5);
        
        setSliderCoef0(svm.getCoef0());
        setSliderCost(svm.getCost());
        setSliderNu(svm.getNu());
        setSliderDegree(svm.getDegree());
        setSliderGamma(svm.getGamma());
        
        //calculate NU_MULTIPLYER and NU_SHIFT
        final int minGroupSize = svm.getMinGroupSize();
        final int maxGroupSize = svm.getMaxGroupSize();
        if (minGroupSize==-1 || maxGroupSize==-1){
            NU_MULTIPLYER = 0.000001;
            NU_SHIFT = -1;
        } else{
            final double minNu = 0.01; // change this to allow smaller values for nu to be chosen with the slider
            final double maxNu = 0.99; // change this to allow bigger values for nu to be chosen with the slider (if feasible)
            NU_SHIFT = Math.log(minNu)/Math.log(2);
            final double valueAtMax = Math.min(maxNu, 0.99 * (double)2*minGroupSize/(minGroupSize + maxGroupSize));
            NU_MULTIPLYER = Math.log(valueAtMax/minNu)/Math.log(2)/100.0;
        }
        MIN_NU = Math.pow(2, NU_SHIFT);
        MAX_NU = Math.pow(2, 100*NU_MULTIPLYER + NU_SHIFT);
        
        // test if default value for nu is feasible
        if (svm.getNu() < MIN_NU || svm.getNu() > MAX_NU) {
            svm.setNu((MAX_NU+MIN_NU)/2);
        }
        
        
        
        ((SpinnerNumberModel)spinCross.getModel()).setMinimum(new Integer(0));
        
        //TODO: Regression?
        if(svm.getType()==SVM.TYPE_NU_CLASS){
            lblNu.setEnabled(true);
            sldNu.setEnabled(true);
            lblNuValue.setEnabled(true);
        }
        
//        jtaSVMInfo.setLineWrap(true);
        
        updateSVMInfo();
    }
    
    private double getCoef0(){
        return ((double)sldCoef0.getValue()-50)/20;
    }
    
    private double getCost(){
        return java.lang.Math.pow(2,(double)sldCost.getValue()/25);
    }
    
    private double getNu(){
        return java.lang.Math.pow(2,(double)sldNu.getValue()*NU_MULTIPLYER+NU_SHIFT);
    }
    
    private double getGamma(){
        return java.lang.Math.pow(2,-(double)(sldGamma.getMaximum()+sldGamma.getMinimum()-sldGamma.getValue())/10);
    }
    
    private int getDegree(){
        return sldDegree.getValue();
    }
    
    private void setSliderGamma(final double gamma){
        sldGamma.setValue((int)Math.round(-10*Math.log(gamma)/Math.log(2))+sldGamma.getMaximum()+sldGamma.getMinimum());
        setLabelGamma(gamma);
    }
    
    private void setSliderCost(final double cost){
        sldCost.setValue((int)Math.round(25*Math.log(cost)/Math.log(2)));
        setLabelCost(cost);
    }
    
    private void setSliderNu(final double nu){
        sldNu.setValue((int)Math.round((Math.log(nu)/Math.log(2)-NU_SHIFT)/NU_MULTIPLYER));
        setLabelNu(nu);
    }
    
    private void setSliderCoef0(final double coef0){
        sldCoef0.setValue((int)Math.round(20*coef0+50));
        setLabelCoef0(coef0);
    }
    
    private void setSliderDegree(final int degree){
        sldDegree.setValue(degree);
        setLabelDegree(degree);
    }
    
    private void setLabelGamma(final double gamma){
        lblGammaValue.setText(numberFormat.format(gamma));
    }
    
    private void setLabelCost(final double cost){
        lblCostValue.setText(numberFormat.format(cost));
    }
    
    private void setLabelNu(final double nu){
        lblNuValue.setText(numberFormat.format(nu));
    }
    
    private void setLabelCoef0(final double coef0){
        lblCoef0Value.setText(numberFormat.format(coef0));
    }
    
    private void setLabelDegree(final int degree){
        lblDegreeValue.setText(String.valueOf(degree));
    }
    
    private void updateSVMInfo(){
        String info ="<html>";
        info += "<b>Data</b>: " + svm.getData().getPath();
        info += "<br>length: " + svm.getData().getLength();
        info += "<br>#variables: " + svm.getData().getNumberOfVariables();
        
        if(svm.getTrained()){
            info += "<br>#Support vectors: " + svm.getNumberOfSupportVectors();
            info += "<br>Accuracy rate: " + numberFormat.format(svm.getAccuracy());
        } else {
            info += "<br>SVM not trained yet. Please adjust parameters and press train button.";
        }
        info +="</html>";
        
        jepSVMInfo.setText(info);
    }
    
    private void changeType(final int type){
        svm.setType(type);
        if(jcbAutoRecalc.isEnabled() && !noRecalc && svm.getTrained()){
            fire(EVT_TRAIN);
            fire(EVT_UPDATE_PLOT);
        }
        updateSVMInfo();
    }
    
    private void changeKernel(final int kernel){
        svm.setKernel(kernel);
        if(jcbAutoRecalc.isEnabled() && !noRecalc && svm.getTrained()){
            fire(EVT_TRAIN);
            fire(EVT_UPDATE_PLOT);
        }
        updateSVMInfo();
    }
    
    private void update(){
        noRecalc=true;
        updateSVMInfo();
        
        // set sliders to new values
        setSliderCoef0(svm.getCoef0());
        setSliderCost(svm.getCost());
        setSliderDegree(svm.getDegree());
        setSliderGamma(svm.getGamma());
        setSliderNu(svm.getNu());
        spinCross.setValue(new Integer(svm.getCross()));
        setJRBKernel(svm.getKernel());
        setJRBType(svm.getType());
        noRecalc=false;
    }
    
    private void setJRBKernel(final int kernel){
        switch(kernel){
            case SVM.KERNEL_LINEAR:
                jrbLinear.setSelected(true);
                break;
            case SVM.KERNEL_POLYNOMIAL:
                jrbPolynomial.setSelected(true);
                break;
            case SVM.KERNEL_RADIAL:
                jrbRadial.setSelected(true);
                break;
            default:
                jrbSigmoid.setSelected(true);
                break;
        }
    }
    
    private void setJRBType(final int type){
        switch (type){
            case SVM.TYPE_C_CLASS:
                jrbCClass.setSelected(true);
                break;
            default:
                jrbNuClass.setSelected(true);
                break;
        }
    }
    
    public boolean getAutoRecalc() {
        return jcbAutoRecalc.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgrTypes = new javax.swing.ButtonGroup();
        brgKernels = new javax.swing.ButtonGroup();
        jepSVMInfo = new javax.swing.JEditorPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        lblCross = new javax.swing.JLabel();
        spinCross = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        jrbLinear = new javax.swing.JRadioButton();
        jrbPolynomial = new javax.swing.JRadioButton();
        jrbRadial = new javax.swing.JRadioButton();
        jrbSigmoid = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jrbCClass = new javax.swing.JRadioButton();
        jrbNuClass = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        lblGamma = new javax.swing.JLabel();
        sldGamma = new javax.swing.JSlider();
        lblGammaValue = new javax.swing.JLabel();
        lblDegree = new javax.swing.JLabel();
        sldDegree = new javax.swing.JSlider();
        lblDegreeValue = new javax.swing.JLabel();
        lblCoef0 = new javax.swing.JLabel();
        sldCoef0 = new javax.swing.JSlider();
        lblCoef0Value = new javax.swing.JLabel();
        lblCost = new javax.swing.JLabel();
        sldCost = new javax.swing.JSlider();
        lblCostValue = new javax.swing.JLabel();
        lblNu = new javax.swing.JLabel();
        sldNu = new javax.swing.JSlider();
        lblNuValue = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        butTune = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jcbAutoRecalc = new javax.swing.JCheckBox();
        butTrain = new javax.swing.JButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        setBorder(new javax.swing.border.TitledBorder("SVM"));
        jepSVMInfo.setEditable(false);
        jepSVMInfo.setContentType("text/html");
        jepSVMInfo.setMaximumSize(new java.awt.Dimension(300, 100));
        jepSVMInfo.setPreferredSize(new java.awt.Dimension(300, 100));
        add(jepSVMInfo);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));

        jPanel3.setBorder(new javax.swing.border.TitledBorder("Other Options"));
        lblCross.setText("Cross validation:");
        jPanel3.add(lblCross);

        spinCross.setMinimumSize(new java.awt.Dimension(50, 20));
        spinCross.setPreferredSize(new java.awt.Dimension(50, 20));
        spinCross.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinCrossStateChanged(evt);
            }
        });

        jPanel3.add(spinCross);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jPanel3, gridBagConstraints);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBorder(new javax.swing.border.TitledBorder("Kernel"));
        brgKernels.add(jrbLinear);
        jrbLinear.setText("linear");
        jrbLinear.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbLinearStateChanged(evt);
            }
        });

        jPanel1.add(jrbLinear);

        brgKernels.add(jrbPolynomial);
        jrbPolynomial.setText("polynomial");
        jrbPolynomial.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbPolynomialStateChanged(evt);
            }
        });

        jPanel1.add(jrbPolynomial);

        brgKernels.add(jrbRadial);
        jrbRadial.setSelected(true);
        jrbRadial.setText("radial");
        jrbRadial.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbRadialStateChanged(evt);
            }
        });

        jPanel1.add(jrbRadial);

        brgKernels.add(jrbSigmoid);
        jrbSigmoid.setText("sigmoid");
        jrbSigmoid.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbSigmoidStateChanged(evt);
            }
        });

        jPanel1.add(jrbSigmoid);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel2.setBorder(new javax.swing.border.TitledBorder("Type"));
        bgrTypes.add(jrbCClass);
        jrbCClass.setSelected(true);
        jrbCClass.setText("C-classification");
        jrbCClass.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbCClassStateChanged(evt);
            }
        });

        jPanel2.add(jrbCClass);

        bgrTypes.add(jrbNuClass);
        jrbNuClass.setText("nu-classification");
        jrbNuClass.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jrbNuClassStateChanged(evt);
            }
        });

        jPanel2.add(jrbNuClass);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel4.add(jPanel2, gridBagConstraints);

        add(jPanel4);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(new javax.swing.border.TitledBorder("Parameters"));
        jPanel6.setLayout(new java.awt.GridLayout(5, 0));

        lblGamma.setText("Gamma:");
        jPanel6.add(lblGamma);

        sldGamma.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldGammaStateChanged(evt);
            }
        });

        jPanel6.add(sldGamma);

        jPanel6.add(lblGammaValue);

        lblDegree.setText("Degree:");
        lblDegree.setEnabled(false);
        jPanel6.add(lblDegree);

        sldDegree.setMaximum(11);
        sldDegree.setMinimum(1);
        sldDegree.setSnapToTicks(true);
        sldDegree.setEnabled(false);
        sldDegree.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldDegreeStateChanged(evt);
            }
        });

        jPanel6.add(sldDegree);

        lblDegreeValue.setEnabled(false);
        jPanel6.add(lblDegreeValue);

        lblCoef0.setText("Coef0:");
        lblCoef0.setEnabled(false);
        jPanel6.add(lblCoef0);

        sldCoef0.setEnabled(false);
        sldCoef0.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldCoef0StateChanged(evt);
            }
        });

        jPanel6.add(sldCoef0);

        lblCoef0Value.setEnabled(false);
        jPanel6.add(lblCoef0Value);

        lblCost.setText("Cost:");
        jPanel6.add(lblCost);

        sldCost.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldCostStateChanged(evt);
            }
        });

        jPanel6.add(sldCost);

        jPanel6.add(lblCostValue);

        lblNu.setText("Nu:");
        lblNu.setEnabled(false);
        jPanel6.add(lblNu);

        sldNu.setEnabled(false);
        sldNu.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldNuStateChanged(evt);
            }
        });

        jPanel6.add(sldNu);

        lblNuValue.setEnabled(false);
        jPanel6.add(lblNuValue);

        jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

        butTune.setText("Tune");
        butTune.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTuneActionPerformed(evt);
            }
        });

        jPanel7.add(butTune);

        jPanel5.add(jPanel7, java.awt.BorderLayout.SOUTH);

        add(jPanel5);

        jcbAutoRecalc.setSelected(true);
        jcbAutoRecalc.setText("recalculate automatically");
        jPanel8.add(jcbAutoRecalc);

        butTrain.setText("Train");
        butTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTrainActionPerformed(evt);
            }
        });

        jPanel8.add(butTrain);

        add(jPanel8);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void butTrainActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butTrainActionPerformed
        fire(EVT_TRAIN);
        updateSVMInfo();
    }//GEN-LAST:event_butTrainActionPerformed
    
    private void butTuneActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butTuneActionPerformed
        final TuningDialog tuned = new TuningDialog(parent,svm.getType(),svm.getKernel(),MIN_NU,MAX_NU);
        tuned.setData(svm.getData());
        tuned.setVariable(svm.getVariableName());
        tuned.setFixCoef0(getCoef0());
        tuned.setFixCost(getCost());
        tuned.setFixDegree(getDegree());
        tuned.setFixGamma(getGamma());
        tuned.setFixNu(getNu());
        tuned.setDefaultRanges();
        tuned.show();
        if (tuned.getSetvalues()){
            noRecalc=true;
            setSliderGamma(tuned.getTune().getBestGamma());
            setSliderCost(tuned.getTune().getBestCost());
            setSliderNu(tuned.getTune().getBestNu());
            setSliderCoef0(tuned.getTune().getBestCoef0());
            setSliderDegree(tuned.getTune().getBestDegree());
            noRecalc=false;
            if(jcbAutoRecalc.isEnabled() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
            }
        }
        updateSVMInfo();
    }//GEN-LAST:event_butTuneActionPerformed
    
    private void sldNuStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldNuStateChanged
        //TODO: Grenzen?
        final double value=java.lang.Math.pow(2,(double)sldNu.getValue()*NU_MULTIPLYER+NU_SHIFT);
        setLabelNu(value);
        if(!sldNu.getValueIsAdjusting()){
            svm.setNu(value);
            if(getAutoRecalc() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
                updateSVMInfo();
            }
        }
    }//GEN-LAST:event_sldNuStateChanged
    
    private void sldCostStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldCostStateChanged
        final double value=java.lang.Math.pow(2,(double)sldCost.getValue()/25);
        setLabelCost(value);
        if(!sldCost.getValueIsAdjusting()){
            svm.setCost(value);
            if(getAutoRecalc() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
                updateSVMInfo();
            }
        }
    }//GEN-LAST:event_sldCostStateChanged
    
    private void sldCoef0StateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldCoef0StateChanged
        final double value=((double)sldCoef0.getValue()-50)/20;
        setLabelCoef0(value);
        if(!sldCoef0.getValueIsAdjusting()){
            svm.setCoef0(value);
            if(getAutoRecalc() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
                updateSVMInfo();
            }
        }
    }//GEN-LAST:event_sldCoef0StateChanged
    
    private void sldDegreeStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldDegreeStateChanged
        final int value=sldDegree.getValue();
        setLabelDegree(value);
        if(!sldDegree.getValueIsAdjusting()){
            svm.setDegree(value);
            if(getAutoRecalc() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
                updateSVMInfo();
            }
        }
    }//GEN-LAST:event_sldDegreeStateChanged
    
    private void sldGammaStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldGammaStateChanged
        final double value=java.lang.Math.pow(2,-(double)(sldGamma.getMaximum()+sldGamma.getMinimum()-sldGamma.getValue())/10);
        setLabelGamma(value);
        if(!sldGamma.getValueIsAdjusting()){
            svm.setGamma(value);
            if(getAutoRecalc() && !noRecalc && svm.getTrained()){
                fire(EVT_TRAIN);
                fire(EVT_UPDATE_PLOT);
                updateSVMInfo();
            }
        }
    }//GEN-LAST:event_sldGammaStateChanged
    
    private void spinCrossStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinCrossStateChanged
        svm.setCross(((Integer)spinCross.getValue()).intValue());
        if(jcbAutoRecalc.isSelected() && !noRecalc && svm.getTrained()){
            fire(EVT_TRAIN);
            fire(EVT_UPDATE_PLOT);
        }
        updateSVMInfo();
    }//GEN-LAST:event_spinCrossStateChanged
    
    private void jrbNuClassStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbNuClassStateChanged
        if(jrbNuClass.isSelected() && svm.getType()!=SVM.TYPE_NU_CLASS){
            lblNu.setEnabled(true);
            sldNu.setEnabled(true);
            lblNuValue.setEnabled(true);
            changeType(SVM.TYPE_NU_CLASS);
        }
    }//GEN-LAST:event_jrbNuClassStateChanged
    
    private void jrbCClassStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbCClassStateChanged
        if(jrbCClass.isSelected() && svm.getType()!= SVM.TYPE_C_CLASS){
            lblNu.setEnabled(false);
            sldNu.setEnabled(false);
            lblNuValue.setEnabled(false);
            changeType(SVM.TYPE_C_CLASS);
        }
    }//GEN-LAST:event_jrbCClassStateChanged
    
    private void jrbSigmoidStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbSigmoidStateChanged
        if(jrbSigmoid.isSelected() && svm.getKernel()!=SVM.KERNEL_SIGMOID) {
            lblGamma.setEnabled(true);
            sldGamma.setEnabled(true);
            lblGammaValue.setEnabled(true);
            lblCoef0.setEnabled(true);
            sldCoef0.setEnabled(true);
            lblCoef0Value.setEnabled(true);
            lblDegree.setEnabled(false);
            sldDegree.setEnabled(false);
            lblDegreeValue.setEnabled(false);
            changeKernel(SVM.KERNEL_SIGMOID);
        }
    }//GEN-LAST:event_jrbSigmoidStateChanged
    
    private void jrbRadialStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbRadialStateChanged
        if(jrbRadial.isSelected() && svm.getKernel()!=SVM.KERNEL_RADIAL) {
            lblGamma.setEnabled(true);
            sldGamma.setEnabled(true);
            lblGammaValue.setEnabled(true);
            lblCoef0.setEnabled(false);
            sldCoef0.setEnabled(false);
            lblCoef0Value.setEnabled(false);
            lblDegree.setEnabled(false);
            sldDegree.setEnabled(false);
            lblDegreeValue.setEnabled(false);
            changeKernel(SVM.KERNEL_RADIAL);
        }
    }//GEN-LAST:event_jrbRadialStateChanged
    
    private void jrbPolynomialStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbPolynomialStateChanged
        if(jrbPolynomial.isSelected() && svm.getKernel()!=SVM.KERNEL_POLYNOMIAL) {
            lblGamma.setEnabled(true);
            sldGamma.setEnabled(true);
            lblGammaValue.setEnabled(true);
            lblCoef0.setEnabled(true);
            sldCoef0.setEnabled(true);
            lblCoef0Value.setEnabled(true);
            lblDegree.setEnabled(true);
            sldDegree.setEnabled(true);
            lblDegreeValue.setEnabled(true);
            changeKernel(SVM.KERNEL_POLYNOMIAL);
        }
    }//GEN-LAST:event_jrbPolynomialStateChanged
    
    private void jrbLinearStateChanged(final javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jrbLinearStateChanged
        if(jrbLinear.isSelected() && svm.getKernel()!=SVM.KERNEL_LINEAR) {
            lblGamma.setEnabled(false);
            sldGamma.setEnabled(false);
            lblGammaValue.setEnabled(false);
            lblCoef0.setEnabled(false);
            sldCoef0.setEnabled(false);
            lblCoef0Value.setEnabled(false);
            lblDegree.setEnabled(false);
            sldDegree.setEnabled(false);
            lblDegreeValue.setEnabled(false);
            changeKernel(SVM.KERNEL_LINEAR);
        }
    }//GEN-LAST:event_jrbLinearStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgrTypes;
    private javax.swing.ButtonGroup brgKernels;
    private javax.swing.JButton butTrain;
    private javax.swing.JButton butTune;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JCheckBox jcbAutoRecalc;
    private javax.swing.JEditorPane jepSVMInfo;
    private javax.swing.JRadioButton jrbCClass;
    private javax.swing.JRadioButton jrbLinear;
    private javax.swing.JRadioButton jrbNuClass;
    private javax.swing.JRadioButton jrbPolynomial;
    private javax.swing.JRadioButton jrbRadial;
    private javax.swing.JRadioButton jrbSigmoid;
    private javax.swing.JLabel lblCoef0;
    private javax.swing.JLabel lblCoef0Value;
    private javax.swing.JLabel lblCost;
    private javax.swing.JLabel lblCostValue;
    private javax.swing.JLabel lblCross;
    private javax.swing.JLabel lblDegree;
    private javax.swing.JLabel lblDegreeValue;
    private javax.swing.JLabel lblGamma;
    private javax.swing.JLabel lblGammaValue;
    private javax.swing.JLabel lblNu;
    private javax.swing.JLabel lblNuValue;
    private javax.swing.JSlider sldCoef0;
    private javax.swing.JSlider sldCost;
    private javax.swing.JSlider sldDegree;
    private javax.swing.JSlider sldGamma;
    private javax.swing.JSlider sldNu;
    private javax.swing.JSpinner spinCross;
    // End of variables declaration//GEN-END:variables
    
}
