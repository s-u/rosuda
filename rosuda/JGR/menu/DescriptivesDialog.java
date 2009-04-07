package org.rosuda.JGR.menu;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;


public class DescriptivesDialog extends javax.swing.JDialog implements ActionListener {
	private JPanel firstPanel;
	private JList strataList;
	private JButton cont;
	private JButton addStrata;
	private JButton reset;
	private JButton cancel;
	private JButton removeStrata;
	private JButton removeDesc;
	private JButton addDesc;
	private JScrollPane strataScroller;
	private JPanel strataPanel;
	private JPanel descPanel;
	private JList descrList;
	private JScrollPane descScroller;
	private VariableSelector variableSelector;
	
	private JPanel secondPanel;
	private JPanel functionPanel;
	private JButton addFunc;
	private JList runFuncList;
	private JList functionList;
	private JScrollPane runFuncScroller;
	private JButton custom;
	private JPanel runFuncPanel;
	private JButton removeFunc;
	private JScrollPane functionScroller;
	
	private static String[] otherFunctions = new String[] {"Median","25th Percentile",
													"75th Percentile","Minimum","Maximum","Skew",
													"Kurtosis"};

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				DescriptivesDialog inst = new DescriptivesDialog(frame);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public DescriptivesDialog(JFrame frame) {
		super(frame);
		initGUI();
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			this.setTitle("Descriptives");
			{
				reset = new JButton();
				getContentPane().add(reset, new AnchorConstraint(869, 559, 920, 419, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				reset.setText("Reset");
				reset.setPreferredSize(new java.awt.Dimension(72, 21));
				reset.addActionListener(this);
			}
			{
				cancel = new JButton();
				getContentPane().add(cancel, new AnchorConstraint(869, 750, 920, 568, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancel.setText("Cancel");
				cancel.setPreferredSize(new java.awt.Dimension(94, 21));
				cancel.addActionListener(this);
			}
			{
				cont = new JButton();
				getContentPane().add(cont, new AnchorConstraint(844, 942, 947, 760, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cont.setText("Continue");
				cont.setPreferredSize(new java.awt.Dimension(94, 42));
				cont.addActionListener(this);
			}
			initFirstPanel();
			initSecondPanel();
			secondPanel.setVisible(false);
			this.setSize(524, 443);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void initFirstPanel(){
		{
			firstPanel = new JPanel();
			AnchorLayout firstPanelLayout = new AnchorLayout();
			getContentPane().add(firstPanel, new AnchorConstraint(-1, 1000, 812, 0, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			firstPanel.setLayout(firstPanelLayout);
			firstPanel.setPreferredSize(new java.awt.Dimension(516, 333));
			{
				addDesc = new JButton();
				firstPanel.add(addDesc, new AnchorConstraint(187, 543, 286, 462, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				addDesc.setText("Add");
				addDesc.setPreferredSize(new java.awt.Dimension(42, 33));
				addDesc.addActionListener(this);
			}
			{
				strataPanel = new JPanel();
				BorderLayout strataPanelLayout = new BorderLayout();
				strataPanel.setLayout(strataPanelLayout);
				firstPanel.add(strataPanel, new AnchorConstraint(623, 950, 920, 564, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				strataPanel.setPreferredSize(new java.awt.Dimension(199, 99));
				strataPanel.setBorder(BorderFactory.createTitledBorder("Stratify By:"));
				{
					strataScroller = new JScrollPane();
					strataPanel.add(strataScroller, BorderLayout.CENTER);
					{
						ListModel strataListModel = 
							new DefaultListModel();
						strataList = new JList();
						strataScroller.setViewportView(strataList);
						strataList.setModel(strataListModel);
					}
				}
			}
			{
				descPanel = new JPanel();
				BorderLayout descLayout = new BorderLayout();
				descPanel.setLayout(descLayout);
				firstPanel.add(descPanel, new AnchorConstraint(37, 956, 605, 564, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				descPanel.setPreferredSize(new java.awt.Dimension(202, 189));
				descPanel.setBorder(BorderFactory.createTitledBorder("Descriptives of:"));
				{
					descScroller = new JScrollPane();
					descPanel.add(descScroller);
					ListModel descrListModel = 
						new DefaultListModel();
					descrList = new JList();
					descScroller.setViewportView(descrList);
					descrList.setModel(descrListModel);
				}
			}
			{
				variableSelector = new VariableSelector();
				firstPanel.add(variableSelector, new AnchorConstraint(37, 429, 920, 24, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(209, 294));
				variableSelector.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
			}
			{
				removeDesc = new JButton();
				firstPanel.add(removeDesc, new AnchorConstraint(301, 543, 400, 462, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				removeDesc.setText("Remove");
				removeDesc.setPreferredSize(new java.awt.Dimension(42, 33));
				removeDesc.addActionListener(this);
			}
			{
				addStrata = new JButton();
				firstPanel.add(addStrata, new AnchorConstraint(692, 543, 791, 462, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				addStrata.setText("Add Strata");
				addStrata.setPreferredSize(new java.awt.Dimension(42, 33));
				addStrata.addActionListener(this);
			}
			{
				removeStrata = new JButton();
				firstPanel.add(removeStrata, new AnchorConstraint(806, 543, 905, 462, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				removeStrata.setText("Remove Strata");
				removeStrata.setPreferredSize(new java.awt.Dimension(42,33));
				removeStrata.addActionListener(this);
			}
		}
	}
	
	private void initSecondPanel(){
		secondPanel = new JPanel();
		AnchorLayout firstPanelLayout = new AnchorLayout();
		getContentPane().add(secondPanel, new AnchorConstraint(-1, 1000, 812, 0, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
		secondPanel.setLayout(firstPanelLayout);
		secondPanel.setPreferredSize(new java.awt.Dimension(516, 333));
		{
			custom = new JButton();
			secondPanel.add(custom, new AnchorConstraint(811, 901, 886, 695, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			custom.setText("Custom");
			custom.setPreferredSize(new java.awt.Dimension(93, 21));
			custom.addActionListener(this);
		}
		{
			runFuncPanel = new JPanel();
			BorderLayout runFuncPanelLayout = new BorderLayout();
			runFuncPanel.setLayout(runFuncPanelLayout);
			secondPanel.add(runFuncPanel, new AnchorConstraint(44, 974, 767, 629, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			runFuncPanel.setPreferredSize(new java.awt.Dimension(156, 201));
			runFuncPanel.setBorder(BorderFactory.createTitledBorder("Run Descriptives"));
			{
				runFuncScroller = new JScrollPane();
				runFuncPanel.add(runFuncScroller, BorderLayout.CENTER);
				{
					DefaultListModel runFuncListModel = new DefaultListModel();
					runFuncListModel.addElement("Mean");
					runFuncListModel.addElement("St. Deviation");
					runFuncListModel.addElement("Valid N");
					runFuncList = new JList();
					runFuncScroller.setViewportView(runFuncList);
					runFuncList.setModel(runFuncListModel);
				}
			}
		}
		{
			removeFunc = new JButton();
			secondPanel.add(removeFunc, new AnchorConstraint(321, 574, 437, 478, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			removeFunc.setText("Remove Function");
			removeFunc.setPreferredSize(new java.awt.Dimension(43, 32));
			removeFunc.addActionListener(this);
		}
		{
			addFunc = new JButton();
			secondPanel.add(addFunc, new AnchorConstraint(188, 574, 303, 478, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			addFunc.setText("Add Function");
			addFunc.setPreferredSize(new java.awt.Dimension(43, 32));
			addFunc.addActionListener(this);
		}
		{
			functionPanel = new JPanel();
			BorderLayout functionPanelLayout = new BorderLayout();
			secondPanel.add(functionPanel, new AnchorConstraint(44, 421, 886, 27, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			functionPanel.setLayout(functionPanelLayout);
			functionPanel.setPreferredSize(new java.awt.Dimension(178, 234));
			functionPanel.setBorder(BorderFactory.createTitledBorder("Functions"));
			{
				functionScroller = new JScrollPane();
				functionPanel.add(functionScroller, BorderLayout.CENTER);
				{
					DefaultListModel functionListModel = 
						new DefaultListModel();
					for(int i=0;i<otherFunctions.length;i++)
						functionListModel.addElement(otherFunctions[i]);
					functionList = new JList();
					functionScroller.setViewportView(functionList);
					functionList.setModel(functionListModel);
				}
			}
		}
	}
	
	

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd == "Continue"){
			firstPanel.setVisible(false);
			secondPanel.setVisible(true);
			cont.setText("Run");
		}else if(cmd == "Cancel"){
			this.dispose();
		}else if(cmd == "Add"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)descrList.getModel()).addElement(objs[i]);
			}
		}else if(cmd == "Remove"){
			Object[] objs=descrList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)descrList.getModel()).removeElement(objs[i]);
			}		
		}else if(cmd == "Add Strata"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)strataList.getModel()).addElement(objs[i]);
			}			
		}else if(cmd == "Remove Strata"){
			Object[] objs=strataList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)strataList.getModel()).removeElement(objs[i]);
			}
		}else if(cmd == "Add Function"){
			Object[] objs=functionList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				((DefaultListModel)functionList.getModel()).removeElement(objs[i]);
				((DefaultListModel)runFuncList.getModel()).addElement(objs[i]);
			}
		}else if(cmd == "Remove Function"){
			Object[] objs=runFuncList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				((DefaultListModel)runFuncList.getModel()).removeElement(objs[i]);
				((DefaultListModel)functionList.getModel()).addElement(objs[i]);
			}			
		}else if(cmd == "Reset"){
			DefaultListModel runFuncListModel = new DefaultListModel();
			runFuncList.setModel(runFuncListModel);
			runFuncListModel.addElement("Mean");
			runFuncListModel.addElement("St. Deviation");
			runFuncListModel.addElement("Valid N");
			DefaultListModel functionListModel = 
				new DefaultListModel();
			functionList.setModel(functionListModel);
			for(int i=0;i<otherFunctions.length;i++)
				functionListModel.addElement(otherFunctions[i]);
			((DefaultListModel)strataList.getModel()).removeAllElements();
			((DefaultListModel)descrList.getModel()).removeAllElements();
			variableSelector.reset();
			secondPanel.setVisible(false);
			cont.setText("Continue");
			firstPanel.setVisible(true);
		}else if(cmd == "Run"){
			String dataName = variableSelector.getSelectedData();
			ArrayList vars = new ArrayList();
			for(int i=0;i<descrList.getModel().getSize();i++)
				vars.add(descrList.getModel().getElementAt(i));
			ArrayList strata = new ArrayList();
			for(int i=0;i<strataList.getModel().getSize();i++)
				strata.add(strataList.getModel().getElementAt(i));	
			ArrayList functions = new ArrayList();
			for(int i=0;i<runFuncList.getModel().getSize();i++)
				functions.add(runFuncList.getModel().getElementAt(i));	
			vars.addAll(strata);
			this.dispose();
			JGR.MAINRCONSOLE.toFront();			
			JGR.MAINRCONSOLE.execute("descriptive.table("+dataName+
					"["+RController.makeRStringVector(vars)+"] ,\n\tfunc.names ="+
					RController.makeRStringVector(functions)+" ,\n\tstrata = "+
					RController.makeRStringVector(strata)+")");
		}
	}
}

