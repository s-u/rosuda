package org.rosuda.deducer.widgets.param;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;

public class ParamComboBoxWidget extends ParamWidget{

	private JComboBox comboBox;
	public ParamComboBoxWidget(){
		super();
	}
	
	public ParamComboBoxWidget(Param p){
		super();
		setModel(p);
	}
	
	public void setModel(Param p){
		model = p;
		initAsComboBox(p.view == Param.VIEW_EDITABLE_COMBO);
		label.setText(p.title);
		if(p.value !=null && (p.labels==null || p.view == Param.VIEW_EDITABLE_COMBO))
			comboBox.setSelectedItem(p.value.toString());
		else if(p.value !=null && p.labels!=null){
			for(int i=0;i<p.options.length;i++)
				if(p.value.toString() == p.options[i])
					comboBox.setSelectedIndex(i+1);
		}
	}
	
	public void updateModel(){
		String val = (String) comboBox.getSelectedItem();
		int ind = comboBox.getSelectedIndex();
		if(ind>0){
			val = model.options[ind-1];
		}
		if(val!=null && val.length()>0)
			model.value = val;
	}
	
	public Param getModel(){
		updateModel();
		return model;
	}
	
	private void initAsComboBox(boolean editable){
		this.removeAll();
		AnchorLayout thisLayout = new AnchorLayout();
		this.setLayout(thisLayout);
		this.setPreferredSize(new java.awt.Dimension(241, 37));
		int labelWidth = leftPos-22; 
		{
			label = new JLabel();
			this.add(label, new AnchorConstraint(202, 234, 689, 12, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			if(model!=null){
				label.setText(model.title);
				labelWidth = SwingUtilities.computeStringWidth(
						label.getFontMetrics(label.getFont()),
						model.title);
			}

		}	
		{
			int textPos = Math.max(labelWidth+22, leftPos);
			DefaultComboBoxModel comboBoxModel = 
				new DefaultComboBoxModel();
			comboBoxModel.addElement(null);
			if(model!=null){
				if(model.options!=null & model.labels!=null)
					for(int i=0;i<model.options.length;i++)
						comboBoxModel.addElement(model.options[i] + "  :  "+model.labels[i]);
				if(model.options!=null & model.labels==null)
					for(int i=0;i<model.options.length;i++)
						comboBoxModel.addElement(model.options[i]);
			}
			comboBox = new JComboBox();
			this.add(comboBox, new AnchorConstraint(148, 12, 743, textPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			comboBox.setEditable(editable);
			comboBox.setModel(comboBoxModel);
			comboBox.setPreferredSize(new java.awt.Dimension(122, 22));
		}

		this.setPreferredSize(new Dimension(200,30));
		this.setMaximumSize(new Dimension(2000,30));
	}
}
