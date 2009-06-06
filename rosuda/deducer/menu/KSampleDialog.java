package org.rosuda.deducer.menu;
//import com.cloudgarden.layout.AnchorConstraint;
//import com.cloudgarden.layout.AnchorLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.util.ErrorMsg;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.rosuda.deducer.toolkit.AddButton;
import org.rosuda.deducer.toolkit.DJList;
import org.rosuda.deducer.toolkit.IconButton;
import org.rosuda.deducer.toolkit.OkayCancelPanel;
import org.rosuda.deducer.toolkit.RemoveButton;
import org.rosuda.deducer.toolkit.SingletonAddRemoveButton;
import org.rosuda.deducer.toolkit.SingletonDJList;
import org.rosuda.deducer.toolkit.VariableSelector;


public class KSampleDialog extends javax.swing.JDialog implements ActionListener{
	private VariableSelector variableSelector;
	private SingletonDJList factor;
	private JPanel meanPanel;
	private JButton exchAssump;
	private JButton largeAssump2;
	private JSeparator sep;
	private JCheckBox kwTest;
	private JButton eqVarAssump;
	private JPanel okayCancelPanel;
	private JSeparator sep1;
	private JButton help;
	private JButton largeAssump3;
	private JCheckBox median;
	private JButton outliersAssump2;
	private JButton nOrNormAssump;
	private JCheckBox anova;
	private JButton outlierAssump;
	private JButton largeAssump;
	private JCheckBox welch;
	private JPanel medianPanel;
	private JButton plots;
	private SubsetPanel subset;
	private JPanel subsetPanel;
	private JButton addFactor;
	private JPanel factorPanel;
	private JButton remove;
	private JButton add;
	private DJList outcomes;
	private JScrollPane outcomeScroller;
	private JPanel outcomePanel;
	
	private KSampleModel model; 
	private static KSampleModel lastModel;


	
	public KSampleDialog(JFrame frame) {
		super(frame);
		initGUI();
		help.setVisible(false);
		median.setEnabled(false);
		largeAssump3.setEnabled(false);
		plots.setVisible(false);
		reset();
		if(lastModel!=null)
			setModel(lastModel);
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				variableSelector = new VariableSelector();
				getContentPane().add(variableSelector, new AnchorConstraint(12, 435, 556, 4, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				variableSelector.setPreferredSize(new java.awt.Dimension(234, 315));
			}
			{
				okayCancelPanel = new OkayCancelPanel(true,true,this);
				getContentPane().add(okayCancelPanel, new AnchorConstraint(910, 979, 980, 469, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okayCancelPanel.setPreferredSize(new java.awt.Dimension(279, 42));
			}
			{
				help = new JButton();
				getContentPane().add(help, new AnchorConstraint(943, 97, 980, 22, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				help.setText("Help");
				help.setPreferredSize(new java.awt.Dimension(32, 32));
			}
			{
				medianPanel = new JPanel();
				getContentPane().add(medianPanel, new AnchorConstraint(665, 12, 910, 528, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				medianPanel.setPreferredSize(new java.awt.Dimension(247, 144));
				medianPanel.setBorder(BorderFactory.createTitledBorder("Median"));
				medianPanel.setLayout(null);
				{
					kwTest = new JCheckBox();
					medianPanel.add(kwTest);
					kwTest.setText("Kruskal-Wallis");
					kwTest.setBounds(17, 27, 157, 19);
				}
				{
					largeAssump2 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
					medianPanel.add(largeAssump2);
					largeAssump2.setBounds(36, 46, 27, 27);
				}
				{
					exchAssump = new IconButton("/icons/eqvar_assump.png","Exchangablility",null,"Exchangablility");
					medianPanel.add(exchAssump);
					exchAssump.setBounds(63, 46, 27, 27);
				}
				{
					median = new JCheckBox();
					medianPanel.add(median);
					median.setText("Median Test");
					median.setBounds(17, 85, 157, 19);
				}
				{
					largeAssump3 =new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
					medianPanel.add(largeAssump3);
					largeAssump3.setBounds(36, 105, 27, 27);
				}
				{
					sep1 = new JSeparator();
					medianPanel.add(sep1);
					sep1.setBounds(51, 79, 118, 5);
				}
			}
			{
				meanPanel = new JPanel();
				getContentPane().add(meanPanel, new AnchorConstraint(665, 469, 910, 22, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				meanPanel.setPreferredSize(new java.awt.Dimension(245, 144));
				meanPanel.setBorder(BorderFactory.createTitledBorder("Mean"));
				meanPanel.setLayout(null);
				{
					welch = new JCheckBox();
					meanPanel.add(welch);
					welch.setText("One-Way ANOVA (Welch) ");
					welch.setBounds(17, 27, 211, 19);
				}
				{
					largeAssump = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
					meanPanel.add(largeAssump);
					largeAssump.setBounds(34, 46, 27, 27);
				}
				{
					outlierAssump = new IconButton("/icons/outlier_assump.png","No Outliers",this,"No Outliers");
					meanPanel.add(outlierAssump);
					outlierAssump.setBounds(61, 46, 27, 27);
				}
				{
					anova = new JCheckBox();
					meanPanel.add(anova);
					anova.setText("One-Way ANOVA");
					anova.setBounds(17, 85, 211, 19);
				}
				{
					nOrNormAssump = new IconButton("/icons/N_or_norm_assump.png","Large Sample or Normal",this,"Large Sample or Normal");
					meanPanel.add(nOrNormAssump);
					nOrNormAssump.setBounds(34, 105, 47, 27);
				}
				{
					outliersAssump2 = new IconButton("/icons/outlier_assump.png","No Outliers",this,"No Outliers");
					meanPanel.add(outliersAssump2);
					outliersAssump2.setBounds(81, 105, 27, 27);
				}
				{
					eqVarAssump = new IconButton("/icons/eqvar_assump.png","Equal Variances",null,"Equal Variances");
					meanPanel.add(eqVarAssump);
					eqVarAssump.setBounds(106, 105, 27, 27);
				}
				{
					sep = new JSeparator();
					meanPanel.add(sep);
					sep.setBounds(53, 79, 118, 5);
				}
			}
			{
				plots = new JButton();
				getContentPane().add(plots, new AnchorConstraint(565, 303, 737, 148, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				plots.setText("Plots");
				plots.setPreferredSize(new java.awt.Dimension(84, 22));
			}
			{
				subsetPanel = new JPanel();
				BorderLayout subsetPanelLayout = new BorderLayout();
				subsetPanel.setLayout(subsetPanelLayout);
				getContentPane().add(subsetPanel, new AnchorConstraint(480, 979, 638, 570, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				subsetPanel.setPreferredSize(new java.awt.Dimension(224, 93));
				subsetPanel.setBorder(BorderFactory.createTitledBorder("Subset"));
				{
					subset = new SubsetPanel(variableSelector.getJComboBox());
					subsetPanel.add(subset, BorderLayout.CENTER);
				}
			}

			{
				factorPanel = new JPanel();
				BorderLayout factorPanelLayout = new BorderLayout();
				factorPanel.setLayout(factorPanelLayout);
				getContentPane().add(factorPanel, new AnchorConstraint(375, 979, 471, 570, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				factorPanel.setPreferredSize(new java.awt.Dimension(224, 57));
				factorPanel.setBorder(BorderFactory.createTitledBorder("Factor"));
				{
					
					factor = new SingletonDJList();
					factorPanel.add(factor, BorderLayout.CENTER);
					factor.setModel(new DefaultListModel());
				}
			}
			{
				addFactor = new SingletonAddRemoveButton(new String[]{"Add Factor","Remove Factor"},
						new String[]{"Add Factor","Remove Factor"},factor,variableSelector);
				getContentPane().add(addFactor, new AnchorConstraint(400, 586, 529, 479, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				addFactor.setPreferredSize(new java.awt.Dimension(34, 34));
			}
			{
				outcomePanel = new JPanel();
				BorderLayout outcomePanelLayout = new BorderLayout();
				outcomePanel.setLayout(outcomePanelLayout);
				getContentPane().add(outcomePanel, new AnchorConstraint(12, 13, 375, 570, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				outcomePanel.setPreferredSize(new java.awt.Dimension(223, 208));
				outcomePanel.setBorder(BorderFactory.createTitledBorder("Outcomes"));
				{
					outcomeScroller = new JScrollPane();
					outcomePanel.add(outcomeScroller, BorderLayout.CENTER);
					{
						outcomes = new DJList();
						outcomeScroller.setViewportView(outcomes);
						outcomes.setModel(new DefaultListModel());
					}
				}
			}
			{
				remove = new RemoveButton("Remove",variableSelector,outcomes);
				getContentPane().add(remove, new AnchorConstraint(199, 570, 304, 479, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				remove.setPreferredSize(new java.awt.Dimension(34, 34));
			}
			{
				add = new AddButton("Add",variableSelector,outcomes);
				getContentPane().add(add, new AnchorConstraint(142, 537, 192, 479, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				add.setPreferredSize(new java.awt.Dimension(34, 34));
			}		
			this.setTitle("Multiple Independent Samples");
			this.setSize(548, 610);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	public void reset(){
		setModel(new KSampleModel());
	}
	
	public void setModel(KSampleModel mod){
		boolean allExist;
		model=mod;
		factor.setModel(new DefaultListModel());
		outcomes.setModel(new DefaultListModel());
		if(mod.dataName!=null){
			variableSelector.setSelectedData(mod.dataName);
			allExist=variableSelector.removeAll(mod.variables);
			if(allExist)
				outcomes.setModel(mod.variables);
			else{
				reset();
				return;
			}
			allExist=variableSelector.removeAll(mod.factorName);
			if(allExist)
				factor.setModel(mod.factorName);
			else{
				reset();
				return;
			}
			if(mod.subset=="" || RController.isValidSubsetExp(mod.subset,mod.dataName)){
				subset.setText(mod.subset);
			}
		}
		welch.setSelected(model.doWelch);
		anova.setSelected(model.doAnova);
		kwTest.setSelected(model.doKW);
		median.setSelected(model.doMedian);
		
	}
	public void setDataName(String dataName){
		if(!dataName.equals(variableSelector.getSelectedData())){
			variableSelector.setSelectedData(dataName);
		}
	}
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();
		
		if(cmd=="Cancel"){
			this.dispose();
		}else if(cmd == "Reset"){
			
		}else if(cmd == "Run"){
			model.doWelch=welch.isSelected();
			model.doAnova=anova.isSelected();
			model.doKW=kwTest.isSelected();
			model.doMedian=median.isSelected();	
			model.subset=subset.getText();
			model.variables=(DefaultListModel) outcomes.getModel();
			model.factorName=(DefaultListModel) factor.getModel();
			model.dataName=variableSelector.getSelectedData();
			boolean valid = model.run();
			if(valid){
				lastModel=model;
				SubsetDialog.addToHistory(model.dataName, model.subset);
				this.dispose();
			}
		}
		
	}
	
	class KSampleModel{
		public boolean doWelch=true;
		public boolean doAnova=false;
		public boolean doKW=false;
		public boolean doMedian=false;
		public DefaultListModel variables = new DefaultListModel();
		public DefaultListModel factorName = new DefaultListModel();
		public String subset="";
		public String dataName="";
		
		public boolean run(){
			if(dataName==null)
				return false;
			if(variables.size()==0){
				JOptionPane.showMessageDialog(null, "Please select one or more outcome variables.");
				return false;
			}
			if(factorName.size()==0){
				JOptionPane.showMessageDialog(null, "Please select a factor.");
				return false;			
			}
			subset = subset.trim();
			String cmd="";
			String subn;
			String outcomes = RController.makeRVector(variables);
			String factor = (String) factorName.get(0);
			if(dataName=="")
				return false;
			boolean isSubset=false;
			if(!subset.equals("") ){
				if(!SubsetDialog.isValidSubsetExp(subset,dataName)){
					JOptionPane.showMessageDialog(null, "Sorry, the subset expression seems to be invalid.");
					return false;
				}
				subn = JGR.MAINRCONSOLE.getUniqueName(dataName+".sub");
				cmd=subn+"<-subset("+dataName+","+subset+")"+"\n";
				isSubset=true;
			}else
				subn=dataName;
			
			if(doWelch){
				cmd += "k.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
					",\n\ttest=oneway.test)"+"\n";
			}
			if(doAnova){
				cmd += "k.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
					",\n\ttest=oneway.test,var.equal=TRUE)"+"\n";
			}
			if(doKW){
				cmd += "k.sample.test(variables="+outcomes+",\n\t\tfactor.var="+factor+",\n\t\tdata="+subn+
					",\n\ttest=kruskal.test)"+"\n";
			}
			
			if(isSubset)
				cmd+="rm("+subn+")\n";
			JGR.MAINRCONSOLE.executeLater(cmd);
			return true;
		}
	}

}
