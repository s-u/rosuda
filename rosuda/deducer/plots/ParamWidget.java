package org.rosuda.deducer.plots;
import java.awt.Dimension;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.widgets.VectorBuilderWidget;

public class ParamWidget extends javax.swing.JPanel{
	private JCheckBox checkBox;
	private JComboBox comboBox;
	private JTextField textField;
	private JLabel label;
	
	private Param model;
	private VectorBuilderWidget vectorBuilder;
	
	private static final int leftPos = 80;

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new ParamWidget());
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public ParamWidget() {
		super();
		initAsComboBox(true);
		this.setAlignmentX(CENTER_ALIGNMENT);
	}
	
	public ParamWidget(Param p){
		super();
		setModel(p);
		this.setAlignmentX(CENTER_ALIGNMENT);
	}
	
	public void setModel(Param p) {
		model = p;
		if(p.view == Param.VIEW_CHECK_BOX){
			initAsCheckBox();
			checkBox.setText(p.title);
			if(p.value !=null)
				checkBox.setSelected(((Boolean) p.value).booleanValue());
		}else if(p.view == Param.VIEW_COMBO || p.view == Param.VIEW_EDITABLE_COMBO){
			initAsComboBox(p.view == Param.VIEW_EDITABLE_COMBO);
			label.setText(p.title);
			if(p.value !=null)
				comboBox.setSelectedItem(p.value);
		}else if(p.view == Param.VIEW_ENTER){
			initAsShortTextField();
			label.setText(p.title);
			if(p.value !=null)
				textField.setText(p.value.toString());
		}else if(p.view == Param.VIEW_ENTER_LONG){
			initAsLongTextField();
			label.setText(p.title);
			if(p.value !=null)
				textField.setText(p.value.toString());
		}else if(p.view == Param.VIEW_VECTOR_BUILDER){
			initAsVectorBuilder();
			vectorBuilder.removeAllItems();
			vectorBuilder.addItems((String[]) (model.value!=null ? model.value : new String[]{}));
		}
	}
	
	
	public void updateModel(){
		if(model.view == Param.VIEW_CHECK_BOX){
			model.value = new Boolean(checkBox.isSelected());
		}else if(model.view == Param.VIEW_COMBO || model.view == Param.VIEW_EDITABLE_COMBO){
			String val = (String) comboBox.getSelectedItem();
			int ind = comboBox.getSelectedIndex();
			if(ind>0){
				val = model.options[ind-1];
			}
			if(val!=null && val.length()>0)
				model.value = val;
		}else if(model.view == Param.VIEW_ENTER || model.view == Param.VIEW_ENTER_LONG){
			model.value = textField.getText();
		}else if(model.view == Param.VIEW_VECTOR_BUILDER){
			DefaultListModel lm = vectorBuilder.getListModel();
			if(lm.size()==0)
				model.value =null;
			else{
				String[] s = new String[lm.size()];
				for(int i=0;i<s.length;i++)
					s[i] = lm.get(i).toString();
				model.value = s;
			}
		}
	}
	
	public Param getModel(){
		updateModel();
		return model;
	}
	
	
	private void initAsCheckBox(){
		this.removeAll();
		AnchorLayout thisLayout = new AnchorLayout();
		this.setLayout(thisLayout);
		this.setPreferredSize(new java.awt.Dimension(241, 37));
		{
			checkBox = new JCheckBox();
			this.add(checkBox, new AnchorConstraint(175, 1002, 689, leftPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			checkBox.setText("option");
			checkBox.setPreferredSize(new java.awt.Dimension(179, 19));
		}	
	}
	
	private void initAsShortTextField(){
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
			textField = new JTextField();
			this.add(textField, new AnchorConstraint(148, 529, 743, textPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE,
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			textField.setPreferredSize(new java.awt.Dimension(71, 22));
			textField.setHorizontalAlignment(SwingConstants.CENTER);
		}
	
	}
	
	private void initAsLongTextField(){
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
			textField = new JTextField();
			this.add(textField, new AnchorConstraint(148, 12, 743, textPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			textField.setPreferredSize(new java.awt.Dimension(161, 22));
			textField.setHorizontalAlignment(SwingConstants.CENTER);
		}
		this.setPreferredSize(new Dimension(200,30));
		this.setMaximumSize(new Dimension(2000,30));
	
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
	
	private void initAsVectorBuilder() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(291, 166));
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
				vectorBuilder = new VectorBuilderWidget();
				this.add(vectorBuilder, new AnchorConstraint(3, 750, 1003, textPos, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				vectorBuilder.setPreferredSize(new java.awt.Dimension(113, 166));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setPreferredSize(new Dimension(200,100));
		this.setMaximumSize(new Dimension(2000,100));
	}


	//interface
	public void setModel(Object model) {
		this.setModel((Param) model);
	}
}
