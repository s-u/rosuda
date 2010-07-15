package org.rosuda.deducer.widgets.param;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.toolkit.IconButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import javax.swing.WindowConstants;
import javax.swing.JFrame;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class ParamRFunctionWidget extends ParamWidget implements ActionListener{
	private JLabel label;
	private Param model;
	private JComboBox comboBox;
	private JButton options;
	
	private HashMap childDialogs;
	
	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Param p = new Param();
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_text","theme_blank"};
		p.name = "axis.line";
		p.title = "axis.line";
		HashMap hm = new HashMap();
		RFunction rf = new RFunction();
		rf.setName("theme_text");
		
		Param rfp = new Param("family");
		rfp.title = "font family";
		rfp.dataType = Param.DATA_CHARACTER;
		rfp.view = Param.VIEW_EDITABLE_COMBO;
		rfp.options = new String[] {"times","monoco","helvenica"};
		rf.add(rfp);
		
		rfp = new Param("face");
		rfp.dataType = Param.DATA_CHARACTER;
		rfp.view = Param.VIEW_COMBO;
		rfp.options = new String[] {"plain","italic","bold"};
		rfp.value = "plain";
		rfp.defaultValue = "plain";
		rf.add(rfp);
		
		rfp = new Param("colour");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rfp.value = Color.black;
		rfp.defaultValue = Color.black;
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "size";
		rfp.title = "size";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1);
		rfp.defaultValue = new Double(1);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "vjust";
		rfp.title = "vjust";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rfp.upperBound = new Double(1);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "hjust";
		rfp.title = "hjust";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rfp.upperBound = new Double(1);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "angle";
		rfp.title = "angle";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(0);
		rfp.defaultValue = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "linehieght";
		rfp.title = "linehieght";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1.1);
		rfp.defaultValue = new Double(1.1);
		rf.add(rfp);
		
		hm.put("theme_text", rf);
		
		rf = new RFunction();
		rf.setName("theme_blank");
		hm.put("theme_blank", rf);
		
		Vector v = new Vector();
		v.add("theme_text");
		v.add(hm);
		p.value = v;
		
		frame.getContentPane().add(new ParamRFunctionWidget(p));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public ParamRFunctionWidget() {
		super();
		initGUI();
	}
	public ParamRFunctionWidget(Param p) {
		super();
		initGUI();
		setModel(p);
	}
	
	private void initGUI(){
		this.removeAll();
		AnchorLayout thisLayout = new AnchorLayout();
		this.setLayout(thisLayout);
		{
			options = new IconButton("/icons/advanced_32.png","Options",this,"Options");
			this.add(options, new AnchorConstraint(16, 1, 1050, 867, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
			options.setPreferredSize(new java.awt.Dimension(32, 31));
		}
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
			this.add(comboBox, new AnchorConstraint(150, 39, 743, textPos, 
					AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, 
					AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
			comboBox.setModel(comboBoxModel);
			comboBox.setPreferredSize(new java.awt.Dimension(159, 22));
		}

		this.setPreferredSize(new Dimension(200,30));
		this.setMaximumSize(new Dimension(2000,30));
	}

	public void setModel(Param p) {
		model = p;
		initGUI();
		label.setText(p.title);
		if(p.value !=null){
			String val = (String) ((Vector)model.value).get(0);
			comboBox.setSelectedItem(val);
		}

	}

	public void updateModel() {
		Vector v = (Vector) model.value;
		v.set(0, comboBox.getSelectedItem());
	}

	public void actionPerformed(ActionEvent arg0) {
		Object f = comboBox.getSelectedItem();
		if(f!=null && f.toString().length()>0){
			String fun = f.toString();
			HashMap hm = (HashMap) ((Vector)model.value).get(1);
			RFunction rf = (RFunction) hm.get(fun);
			RFunctionDialog d = new RFunctionDialog(rf);
			d.setModal(true);
			d.setLocationRelativeTo(options);
			d.setSize(300, 300);
			d.setVisible(true);
		}
	}

}
