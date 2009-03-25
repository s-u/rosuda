package org.rosuda.JGR.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.toolkit.DJList;
import org.rosuda.JGR.toolkit.VariableSelector;


public class FrequencyDialog extends javax.swing.JDialog implements ActionListener{
	private VariableSelector variableSelector;
	private JButton options;
	private JButton cancel;
	private JButton okay;
	private JButton remove;
	private JButton Add;
	private JList freqList;
	private JScrollPane freqScroller;
	private JPanel frequencyPanel;
	private int digits=1;

	
	public FrequencyDialog(JFrame frame) {
		super(frame);
		initGUI();
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				options = new JButton();
				getContentPane().add(options, new AnchorConstraint(829, 594, 966, 445, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				options.setText("Options");
				options.setPreferredSize(new java.awt.Dimension(78, 43));
				options.addActionListener(this);
			}
			{
				cancel = new JButton();
				getContentPane().add(cancel, new AnchorConstraint(867, 800, 937, 651, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancel.setText("Cancel");
				cancel.setPreferredSize(new java.awt.Dimension(78, 22));
				cancel.addActionListener(this);
			}
			{
				okay = new JButton();
				getContentPane().add(okay, new AnchorConstraint(829, 978, 963, 823, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okay.setText("OK");
				okay.setPreferredSize(new java.awt.Dimension(81, 42));
				okay.addActionListener(this);
			}
			{
				remove = new JButton();
				getContentPane().add(remove, new AnchorConstraint(397, 567, 535, 445, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				remove.setText("Remove");
				remove.setPreferredSize(new java.awt.Dimension(64, 43));
				remove.addActionListener(this);
			}
			{
				Add = new JButton();
				getContentPane().add(Add, new AnchorConstraint(202, 567, 343, 449, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				Add.setText("Add");
				Add.setPreferredSize(new java.awt.Dimension(62, 44));
				Add.addActionListener(this);
			}
			{
				frequencyPanel = new JPanel();
				BorderLayout frequencyPanelLayout = new BorderLayout();
				getContentPane().add(frequencyPanel, new AnchorConstraint(39, 978, 774, 579, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				frequencyPanel.setPreferredSize(new java.awt.Dimension(209, 230));
				frequencyPanel.setLayout(frequencyPanelLayout);
				frequencyPanel.setBorder(BorderFactory.createTitledBorder("Run Frequencies On:"));
				{
					freqScroller = new JScrollPane();
					frequencyPanel.add(freqScroller, BorderLayout.CENTER);
					{
						ListModel freqListModel = 
							new DefaultListModel();
						freqList = new DJList();
						freqScroller.setViewportView(freqList);
						freqList.setModel(freqListModel);
					}
				}
			}
			{
				variableSelector = new VariableSelector();
				getContentPane().add(variableSelector, new AnchorConstraint(39, 434, 963, 23, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(215, 289));
				variableSelector.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
				variableSelector.getJComboBox().addActionListener(this);
			}
			this.setSize(524, 335);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent event) {
		
		String cmd = event.getActionCommand();
		if(cmd == "comboBoxChanged"){
			freqList.setModel(new DefaultListModel());
		}else if(cmd=="Cancel")
			this.dispose();
		else if(cmd == "Add"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)freqList.getModel()).addElement(objs[i]);
			}
		}else if(cmd == "Remove"){
			Object[] objs=freqList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)freqList.getModel()).removeElement(objs[i]);
			}			
		}else if(cmd == "Options"){
			boolean valid = false;
			while(!valid){
				String result =JOptionPane.showInputDialog(this, "How many digits (Maximum: 7)\n should the percentages be rounded to?", "Option: Rounding", JOptionPane.INFORMATION_MESSAGE);
				try{
				digits =Integer.parseInt(result);
				valid = true;
				}catch(Exception e){}
			}
		}else if(cmd == "OK"){
			if(freqList.getModel().getSize()==0){
				JOptionPane.showMessageDialog(this, "Please select some variables to\nrun frequencies on.");
				return;
			}
			String dataName = variableSelector.getSelectedData();
			Object[] vars =freqList.getSelectedValues();
			ArrayList varList = new ArrayList();
			for(int i=0;i<freqList.getModel().getSize();i++)
				varList.add(freqList.getModel().getElementAt(i));
			this.dispose();
			JGR.MAINRCONSOLE.toFront();			
			JGR.MAINRCONSOLE.execute("frequencies("+dataName+
					"["+RController.makeRStringVector(varList)+"] , r.digits = "+digits+")");

		}
		
	}
}
