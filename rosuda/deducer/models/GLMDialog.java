package org.rosuda.deducer.models;


import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.menu.SubsetDialog;
import org.rosuda.deducer.menu.SubsetPanel;
import org.rosuda.deducer.toolkit.AddButton;
import org.rosuda.deducer.toolkit.DJList;
import org.rosuda.deducer.toolkit.OkayCancelPanel;
import org.rosuda.deducer.toolkit.RemoveButton;
import org.rosuda.deducer.toolkit.SingletonAddRemoveButton;
import org.rosuda.deducer.toolkit.SingletonDJList;
import org.rosuda.deducer.toolkit.VariableSelector;

public class GLMDialog extends ModelDialog implements ActionListener {
	private VariableSelector variableSelector;
	private JPanel contPanel;
	private JLabel typeLabel;
	private JComboBox type;
	private JButton addOutcome;
	private JPanel weightPanel;
	private SubsetPanel subset;
	private JPanel subsetPanel;
	private SingletonDJList weights;
	private JButton addWeight;
	private SingletonDJList outcome;
	private JPanel outcomePanel;
	private JButton help;
	private OkayCancelPanel okayCancelPanel;
	private RemoveButton removeFactor;
	private DJList factorVars;
	private DJList numericVars;
	private JScrollPane factScroller;
	private JScrollPane numericScroller;
	private JPanel factPanel;
	private static DefaultComboBoxModel families  = new DefaultComboBoxModel(
				new String[] { "gaussian()", "binomial()","poisson()",
						"Gamma()","inverse.guassian()","quasibinomial()",
						"quasipoisson()","other..." });
	private AddButton addFactor;
	private RemoveButton removeNumeric;
	private AddButton addNumeric;
	private GLMModel model= new GLMModel();
	private GLMModel modelOnOpen = new GLMModel();
	
	public GLMDialog(JDialog d,GLMModel mod) {
		super(d);
		mod.copyInto(modelOnOpen);
		initGUI();
		help.setVisible(false);
		setModel(mod);
	}
	public GLMDialog(JFrame frame,GLMModel mod) {
		super(frame);
		mod.copyInto(modelOnOpen);
		initGUI();
		help.setVisible(false);
		setModel(mod);
	}
	public GLMDialog(JFrame frame) {
		this(frame,new GLMModel());
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			variableSelector = new VariableSelector();
			{
				weightPanel = new JPanel();
				BorderLayout weightPanelLayout = new BorderLayout();
				weightPanel.setLayout(weightPanelLayout);
				getContentPane().add(weightPanel, new AnchorConstraint(644, 978, 730, 568, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				weightPanel.setBorder(BorderFactory.createTitledBorder("Weights"));
				weightPanel.setPreferredSize(new java.awt.Dimension(223, 52));
				{
					ListModel weightsModel = 
						new DefaultListModel();
					weights = new SingletonDJList();
					weightPanel.add(weights, BorderLayout.CENTER);
					weights.setModel(weightsModel);
				}
			}
			{
				addWeight =  new SingletonAddRemoveButton(
						new String[]{"Add Weighting Variable","Remove Weighting Variable"},
						new String[]{"Add Weighting Variable","Remove Weighting Variable"},
						weights,variableSelector);
				getContentPane().add(addWeight, new AnchorConstraint(664, 534, 724, 467, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				addWeight.setPreferredSize(new java.awt.Dimension(36, 36));
			}
			{
				subsetPanel = new JPanel();
				BorderLayout subsetPanelLayout = new BorderLayout();
				subsetPanel.setLayout(subsetPanelLayout);
				getContentPane().add(subsetPanel, new AnchorConstraint(740, 978, 872, 568, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				subsetPanel.setBorder(BorderFactory.createTitledBorder("Subset"));
				subsetPanel.setPreferredSize(new java.awt.Dimension(223, 79));
				{
					subset = new SubsetPanel(variableSelector.getJComboBox());
					subsetPanel.add(subset, BorderLayout.CENTER);
					subset.setPreferredSize(new java.awt.Dimension(213, 53));
				}
			}
			{
				{
					contPanel = new JPanel();
					BorderLayout contPanelLayout = new BorderLayout();
					contPanel.setLayout(contPanelLayout);
					getContentPane().add(contPanel, new AnchorConstraint(115, 978, 352, 568, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					contPanel.setBorder(BorderFactory.createTitledBorder(null, "As Numeric", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
					contPanel.setPreferredSize(new java.awt.Dimension(223, 142));
					{
						numericScroller = new JScrollPane();
						contPanel.add(numericScroller, BorderLayout.CENTER);
						{
							numericVars = new DJList();
							numericVars.setModel(new DefaultListModel());
							numericScroller.setViewportView(numericVars);
						}
					}
				}
				{
					factPanel = new JPanel();
					BorderLayout factPanelLayout = new BorderLayout();
					factPanel.setLayout(factPanelLayout);
					getContentPane().add(factPanel, new AnchorConstraint(362, 978, 634, 568, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					factPanel.setBorder(BorderFactory.createTitledBorder("As Factor"));
					factPanel.setPreferredSize(new java.awt.Dimension(223, 163));
					{
						factScroller = new JScrollPane();
						factPanel.add(factScroller, BorderLayout.CENTER);
						{
							factorVars = new DJList();
							factorVars.setModel(new DefaultListModel());
							factScroller.setViewportView(factorVars);
						}
					}
				}
				{
					type = new JComboBox();
					getContentPane().add(type, new AnchorConstraint(839, 431, 874, 64, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
					type.setModel(families);
					type.setPreferredSize(new java.awt.Dimension(170, 21));
					type.addActionListener(this);
				}
				{
					typeLabel = new JLabel();
					getContentPane().add(typeLabel, new AnchorConstraint(839, 487, 872, 11, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
					typeLabel.setText("Family:");
					typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
					typeLabel.setPreferredSize(new java.awt.Dimension(46, 20));
				}
				{
					addNumeric = new AddButton("Add Numeric Variables",
							variableSelector,numericVars);
					getContentPane().add(addNumeric, new AnchorConstraint(179, 534, 239, 467, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
					addNumeric.setPreferredSize(new java.awt.Dimension(36, 36));
				}
				{
					removeNumeric = new RemoveButton("Remove Numeric Variables",
							variableSelector,numericVars);
					getContentPane().add(removeNumeric, new AnchorConstraint(239, 534, 299, 467,
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
					removeNumeric.setPreferredSize(new java.awt.Dimension(36, 36));
				}
				{
					addFactor = new AddButton("Add Factor Variables",
							variableSelector,factorVars);
					getContentPane().add(addFactor, new AnchorConstraint(444, 534, 504, 467, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
					addFactor.setPreferredSize(new java.awt.Dimension(36, 36));
				}
				{
					removeFactor = new RemoveButton("Remove Factor Variables",
							variableSelector,factorVars);
					getContentPane().add(removeFactor, new AnchorConstraint(504, 534, 564, 467,
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
					removeFactor.setPreferredSize(new java.awt.Dimension(36, 36));
				}
				{
					okayCancelPanel = new OkayCancelPanel(true,true,this);
					getContentPane().add(okayCancelPanel, new AnchorConstraint(904, 978, 980, 300, 
							AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE));
					okayCancelPanel.setPreferredSize(new java.awt.Dimension(400, 46));
					okayCancelPanel.getApproveButton().setText("Continue");
					okayCancelPanel.getApproveButton().setActionCommand("Continue");
				}
				{
					help = new JButton();
					getContentPane().add(help, new AnchorConstraint(-15, 0, 0, -17, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					help.setText("Help");
					help.setPreferredSize(new java.awt.Dimension(10, 10));
				}
				{
					outcomePanel = new JPanel();
					BorderLayout outcomePanelLayout = new BorderLayout();
					outcomePanel.setLayout(outcomePanelLayout);
					getContentPane().add(outcomePanel, new AnchorConstraint(20, 978, 105, 568, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					outcomePanel.setBorder(BorderFactory.createTitledBorder("Outcome"));
					outcomePanel.setPreferredSize(new java.awt.Dimension(223, 51));
					{
						ListModel outcomeModel = new DefaultListModel();
						outcome = new SingletonDJList();
						outcomePanel.add(outcome, BorderLayout.CENTER);
						outcome.setModel(outcomeModel);
					}
				}
				{
					addOutcome = new SingletonAddRemoveButton(
							new String[]{"Add Outcome","Remove Outcome"},
							new String[]{"Add Outcome","Remove Outcome"},
							outcome,variableSelector);
					getContentPane().add(addOutcome, new AnchorConstraint(34, 534, 94, 467, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
							AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					addOutcome.setPreferredSize(new java.awt.Dimension(36, 36));
				}
			}
			{
				getContentPane().add(variableSelector, new AnchorConstraint(20, 431, 819, 22, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(222, 479));
			}
			this.setSize(552, 634);
			this.setTitle("Generalized Linear Model");
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	
	public void setModel(GLMModel mod){
		variableSelector.setSelectedData(mod.data);
		boolean valid = variableSelector.removeAll(mod.outcomes);
		if(!valid){
			setModel(new GLMModel());
			return;
		}
		outcome.setModel(mod.outcomes);
		
		valid = variableSelector.removeAll(mod.numericVars);
		if(!valid){
			setModel(new GLMModel());
			return;
		}
		numericVars.setModel(mod.numericVars);
		
		valid = variableSelector.removeAll(mod.factorVars);
		if(!valid){
			setModel(new GLMModel());
			return;
		}
		factorVars.setModel(mod.factorVars);
		
		valid = variableSelector.removeAll(mod.weights);
		if(!valid){
			setModel(new GLMModel());
			return;
		}
		weights.setModel(mod.weights);
		if(SubsetDialog.isValidSubsetExp(mod.subset, mod.data))
			subset.setText(mod.subset);
		if(families.getIndexOf(mod.family)>=0)
			families.setSelectedItem(mod.family);
		else
			families.insertElementAt(mod.family, families.getSize()-1);
		model = mod;
	}
	
	public void updateModel(){
		model.factorVars=(DefaultListModel)factorVars.getModel();
		model.numericVars=(DefaultListModel)numericVars.getModel();
		model.outcomes = (DefaultListModel) outcome.getModel();
		model.data = variableSelector.getSelectedData();
		model.subset = subset.getText();
		model.weights = (DefaultListModel) weights.getModel();
		model.family =(String)families.getSelectedItem();
		
		boolean allIn=true;
		for(int i=0;i<modelOnOpen.numericVars.size();i++){
			if(!model.numericVars.contains(modelOnOpen.numericVars.get(i)))
				allIn= false;
		}
		for(int i=0;i<modelOnOpen.factorVars.size();i++){
			if(!model.factorVars.contains(modelOnOpen.factorVars.get(i)))
				allIn= false;
		}
		if(!allIn)
			model.terms=new DefaultListModel();
	}
	
	public boolean valid(){
		if(outcome.getModel().getSize()==0){
			JOptionPane.showMessageDialog(this, "Please specify an outcome variable");
			return false;
		}
		if(factorVars.getModel().getSize()==0 && numericVars.getModel().getSize()==0){
			JOptionPane.showMessageDialog(this, "Please specify a predictor variable (numeric or factor).");
			return false;
		}
		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd == "Continue"){
			if(!valid())
				return;
			updateModel();
			ModelBuilder builder = new ModelBuilder(model,this);
			builder.setLocationRelativeTo(this);
			builder.setVisible(true);
			this.dispose();
		}else if(cmd == "Cancel"){
			this.dispose();
		}else if(cmd == "comboBoxChanged" && type.getSelectedItem().equals("other...")){
			String tmp = JOptionPane.showInputDialog(this, "Custom GLM Family");
			if(tmp==null || tmp == "")
				type.setSelectedIndex(0);
			else{
				families.insertElementAt(tmp, families.getSize()-1);
				type.setSelectedItem(tmp);
			}
		}
		
	}
	
	public void callBack(JDialog d,ModelModel mod){
		if(! (mod instanceof GLMModel)){
			JOptionPane.showMessageDialog(this, "Internal Error: Invalid ModelModel");
			mod=new GLMModel();
		}
		GLMDialog dia = new GLMDialog(new JDialog(),(GLMModel)mod);
		dia.setLocationRelativeTo(d);
		dia.setVisible(true);
	}

	
	

}
