package org.rosuda.deducer.plots;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.ExDefaultTableModel;
import org.rosuda.deducer.widgets.VectorBuilderWidget;

public class ParamWidget extends javax.swing.JPanel implements ActionListener, FocusListener{
	private JCheckBox checkBox;
	private JComboBox comboBox;
	private JTextField textField;
	private JLabel label;
	private LegendPanel legendPanel;
	
	JButton colourButton;
	Color colourValue;
	
	private Param model;
	private VectorBuilderWidget vectorBuilder;
	private JTextField textField1;
	
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
			if(p.value !=null && (p.labels==null || p.view == Param.VIEW_EDITABLE_COMBO))
				comboBox.setSelectedItem(p.value.toString());
			else if(p.value !=null && p.labels!=null){
				for(int i=0;i<p.options.length;i++)
					if(p.value.toString() == p.options[i])
						comboBox.setSelectedIndex(i+1);
			}
		}else if(p.view == Param.VIEW_ENTER){
			initAsShortTextField();
			label.setText(p.title);
			if(p.value !=null)
				textField.setText(p.value.toString());
			if(p.dataType == Param.DATA_NUMERIC)
				textField.addFocusListener(this);
		}else if(p.view == Param.VIEW_ENTER_LONG){
			initAsLongTextField();
			label.setText(p.title);
			if(p.value !=null)
				textField.setText(p.value.toString());
			if(p.dataType == Param.DATA_NUMERIC)
				textField.addFocusListener(this);
		}else if(p.view == Param.VIEW_VECTOR_BUILDER){
			initAsVectorBuilder();
			vectorBuilder.removeAllItems();
			vectorBuilder.addItems((String[]) (model.value!=null ? model.value : new String[]{}));
			vectorBuilder.setNumeric(p.dataType == Param.DATA_NUMERIC_VECTOR);
		}else if(p.view == Param.VIEW_COLOUR){
			initAsColour();
		}else if(p.view == Param.VIEW_SCALE){
			initAsScale();
			if(model.value!=null){
				Vector v = (Vector) model.value;
				String text = (String) v.get(0);
				Boolean show = (Boolean) v.get(1);
				ExDefaultTableModel tm = (ExDefaultTableModel) v.get(2);
				legendPanel.setName(text);
				legendPanel.setShowLegend(show.booleanValue());
				legendPanel.setNumeric(p.dataType == Param.DATA_SCALE_NUMERIC);
				legendPanel.setTableModel(tm);
			}
		}else if(p.view == Param.VIEW_TWO_VALUE_ENTER){
			initAsTwoTextFields();
			String[] val = (String[]) p.value;
			if(val.length>1){
				textField.setText(val[0]);
				textField1.setText(val[1]);
			}
			if(p.dataType == Param.DATA_NUMERIC_VECTOR)
				textField.addFocusListener(this);
			if(p.dataType == Param.DATA_NUMERIC_VECTOR)
				textField1.addFocusListener(this);
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
		}else if(model.view == Param.VIEW_COLOUR){
			if(colourValue!=null && !colourValue.equals(model.defaultValue)){
				model.value = colourValue;
			}
		}else if(model.view == Param.VIEW_SCALE){
			Vector newValue = new Vector();
			newValue.add(legendPanel.getName());
			newValue.add(new Boolean(legendPanel.getShowLegend()));
			ExDefaultTableModel tm = legendPanel.getTableModel();
			if(model.dataType == Param.DATA_SCALE_NUMERIC)
				for(int j=0;j<tm.getColumnCount();j++){
					String val = (String) tm.getValueAt(0, j);
					try{
						Double.parseDouble(val);
					}catch(Exception e){
						tm.setValueAt("", 0, j);
					}
				}
			legendPanel.setTableModel(tm);
			newValue.add(tm);
			model.value = newValue;
		}else if(model.view == Param.VIEW_TWO_VALUE_ENTER){
			String a = textField.getText();
			String b = textField1.getText();
			if(model.dataType == Param.DATA_CHARACTER_VECTOR){
				if(a.length()>0 && b.length()>0)
					model.value = new String[]{"'"+Deducer.addSlashes(a)+"'","'"+Deducer.addSlashes(b)+"'"};
				else
					model.value = new String[]{};
			}
			if(model.dataType == Param.DATA_NUMERIC_VECTOR){
				if(a.length()>0 && b.length()>0)
					model.value = new String[]{a,b};
				else
					model.value = new String[]{};
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
	
	private void initAsTwoTextFields(){
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
		{
			int textPos = Math.max(labelWidth+22, leftPos);
			textField1 = new JTextField();
			this.add(textField1, new AnchorConstraint(148, 529, 743, textPos+81, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE,
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			textField1.setPreferredSize(new java.awt.Dimension(71, 22));
			textField1.setHorizontalAlignment(SwingConstants.CENTER);
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
	
	private void initAsScale() {
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
				legendPanel = new LegendPanel();
				this.add(legendPanel, new AnchorConstraint(3, 750, 1003, textPos, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				legendPanel.setPreferredSize(new java.awt.Dimension(255, 255));
				legendPanel.setMaximumSize(new java.awt.Dimension(255, 1000));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setPreferredSize(new Dimension(300,255));
		this.setMaximumSize(new Dimension(500,400));
	}
	
	private void initAsColour(){
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
			colourButton = new JButton("Set Colour");
			this.add(colourButton, new AnchorConstraint(148, 12, 743, textPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			colourButton.setPreferredSize(new java.awt.Dimension(122, 22));
			if(model.value!=null){
				colourButton.setForeground((Color) model.value);
				colourValue = (Color) model.value;
			}
			colourButton.removeActionListener(this);
			colourButton.addActionListener(this);
		}

		this.setPreferredSize(new Dimension(200,30));
		this.setMaximumSize(new Dimension(2000,30));
	}


	//interface
	public void setModel(Object model) {
		this.setModel((Param) model);
	}

	public void actionPerformed(ActionEvent arg0) {
		Color c =JColorChooser.showDialog(this, "Choose Colour", colourButton.getForeground());
		if(c!=null){
			colourButton.setForeground(c);
			colourValue = c;
		}
	}

	public void focusGained(FocusEvent fe) {}

	public void focusLost(FocusEvent fe) {
		JTextField field = (JTextField) fe.getSource();
		String s = field.getText();
		try{
			double d = Double.parseDouble(s);
			if(model.lowerBound!=null && d<model.lowerBound.doubleValue())
				field.setText(model.lowerBound.toString());
			if(model.upperBound!=null && d>model.upperBound.doubleValue())
				field.setText(model.upperBound.toString());
		}catch(Exception e){
			field.setText("");
		}
		
	}
}
