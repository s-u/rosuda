
package org.rosuda.JGR.menu;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.rosuda.JGR.toolkit.VariableSelector;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.JGR;


public class RecodeDialog extends javax.swing.JDialog implements ActionListener {
	private SetRecodingsDialog codes;
	private VariableSelector variableSelector;
	private JList recodeVariableList;
	private JButton intoButton;
	private JButton removeButton;
	private JButton cancelButton;
	private JButton runButton;
	private JButton defineButton;
	private JButton addButton;


	
	public RecodeDialog(JFrame frame) {
		super(frame);
		initGUI();
	}
	
	private void initGUI() {
		try {
			this.setTitle("Recode Variables");
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);

			{
				removeButton = new JButton();
				getContentPane().add(removeButton, new AnchorConstraint(502, 459, 583, 334, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				removeButton.setText("Remove");
				removeButton.setPreferredSize(new java.awt.Dimension(79, 27));
				removeButton.setFont(new java.awt.Font("Tahoma",0,10));
				removeButton.addActionListener(this);
			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton, new AnchorConstraint(767, 934, 835, 801, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				cancelButton.setPreferredSize(new java.awt.Dimension(84, 23));
				cancelButton.addActionListener(this);
			}
			{
				runButton = new JButton();
				getContentPane().add(runButton, new AnchorConstraint(867, 934, 968, 801, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				runButton.setText("OK");
				runButton.setPreferredSize(new java.awt.Dimension(84, 34));
				runButton.addActionListener(this);
			}
			{
				defineButton = new JButton();
				getContentPane().add(defineButton, new AnchorConstraint(870, 728, 965, 496, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				defineButton.setText("Define Recode");
				defineButton.setPreferredSize(new java.awt.Dimension(147, 32));
				defineButton.addActionListener(this);
			}
			{
				addButton = new JButton();
				getContentPane().add(addButton, new AnchorConstraint(375, 459, 449, 334, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				addButton.setText("Add");
				addButton.setPreferredSize(new java.awt.Dimension(79, 25));
				addButton.setFont(new java.awt.Font("Tahoma",0,10));
				addButton.addActionListener(this);
			}
			{
				intoButton = new JButton();
				getContentPane().add(intoButton, new AnchorConstraint(218, 934, 295, 782, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				intoButton.setText("New Variable");
				intoButton.setPreferredSize(new java.awt.Dimension(96, 26));
				intoButton.setFont(new java.awt.Font("Tahoma",0,10));
				intoButton.addActionListener(this);
			}
			{
				JPanel recodePanel = new JPanel();
				BorderLayout recodePanelLayout = new BorderLayout();
				recodePanel.setLayout(recodePanelLayout);
				ListModel recodeVariableListModel = 
					new DefaultComboBoxModel();
				recodeVariableList = new JList();
				JScrollPane recodeScroller = new JScrollPane(recodeVariableList,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				recodePanel.add(recodeScroller);
				getContentPane().add(recodePanel, new AnchorConstraint(123, 758, 835, 467, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				recodeVariableList.setModel(recodeVariableListModel);
				recodePanel.setPreferredSize(new java.awt.Dimension(184, 240));
				recodePanel.setBorder(BorderFactory.createTitledBorder("Variables to Recode"));
			}
			{
				variableSelector = new VariableSelector();
				getContentPane().add(variableSelector, new AnchorConstraint(74, 331, 846, 28, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				getContentPane().add(variableSelector, new AnchorConstraint(72, 325, 965, 21, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(192, 301));
				variableSelector.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
				variableSelector.getJComboBox().addActionListener(this);
			}
			this.setSize(640, 371);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.toString());
		String cmd=e.getActionCommand();
		if(cmd=="Cancel")
			this.dispose();
		else if(cmd=="Add"){
			JList list = variableSelector.getJList();
			Object[] elements=list.getSelectedValues();
			String temp;
			for(int i=0;i<elements.length;i++){
				if(elements[i]!=null){
					temp = (String) elements[i];
					((DefaultComboBoxModel)recodeVariableList.getModel()).addElement(
							temp.concat("\u2192".concat(temp)));
					variableSelector.remove(elements[i]);
				}
			}
		}else if(cmd=="Remove"){
			Object[] elements=recodeVariableList.getSelectedValues();
			String temp;
			for(int i=0;i<elements.length;i++){
				if(elements[i]!=null){
					temp=(String) elements[i];
					((DefaultComboBoxModel)recodeVariableList.getModel()).removeElement(elements[i]);
					variableSelector.add(temp.substring(0,temp.indexOf("\u2192")));
				}
			}			
		}else if(cmd=="New Variable"){
			int selectedIndex = recodeVariableList.getSelectedIndex();
			if(selectedIndex==-1)
				return;
			String entry = (String) recodeVariableList.getSelectedValue();
			entry = entry.substring(0,entry.indexOf("\u2192"));
			String newVar = (String) JOptionPane.showInputDialog(this,"Recode "+entry+" into:");
			newVar = RController.makeValidVariableName(newVar);
			((DefaultComboBoxModel) recodeVariableList.getModel()).removeElementAt(selectedIndex);
			((DefaultComboBoxModel) recodeVariableList.getModel()).addElement(entry+"\u2192"+newVar);
		}else if(cmd=="Define Recode"){
			String[] recodes = new String[recodeVariableList.getModel().getSize()];
			for(int i=0;i<recodeVariableList.getModel().getSize();i++)
				recodes[i]=(String)recodeVariableList.getModel().getElementAt(i);
			codes = new SetRecodingsDialog(this,recodes,(String)variableSelector.getJComboBox().getSelectedItem());
			codes.setVisible(true);
		}else if(cmd == "comboBoxChanged"){
			((DefaultComboBoxModel)recodeVariableList.getModel()).removeAllElements();
		}else if(cmd=="OK"){
			if(codes == null || codes.getCodes()==""){
				JOptionPane.showMessageDialog(this, "No Recodings Have been defined.\nClick on the 'Define Recode' button to specify...");
				return;
			}
			String data = (String)variableSelector.getJComboBox().getSelectedItem();
			String fromVars;
			String toVars;
			ArrayList fromList = new ArrayList();
			ArrayList toList = new ArrayList();
			DefaultComboBoxModel model = ((DefaultComboBoxModel) recodeVariableList.getModel());
			String[]temp;
			for(int i=0;i<model.getSize();i++){
				temp = ((String)model.getElementAt(i)).split("\u2192");
				fromList.add(temp[0]);
				toList.add(temp[1]);
			}
			toVars = RController.makeRStringVector(toList);
			fromVars = RController.makeRStringVector(fromList);
			
			JGR.MAINRCONSOLE.executeLater(data+"["+toVars+"] <- recode.variables("+data+
						"["+fromVars+"] , "+codes.getCodes()+")");
			this.dispose();
		}
	}

}
