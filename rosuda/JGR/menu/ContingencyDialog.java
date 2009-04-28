package org.rosuda.JGR.menu;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.toolkit.DJList;
import org.rosuda.JGR.toolkit.IconButton;
import org.rosuda.JGR.toolkit.VariableSelector;
import org.rosuda.JGR.util.ErrorMsg;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


public class ContingencyDialog extends JDialog implements ActionListener {
	private VariableSelector variableSelector;
	private IconButton addStratum;
	private IconButton removeStratum;
	private IconButton removeColumn;
	private JButton help;
	private JTextArea subset;
	private JScrollPane subsetScroller;
	private JPanel subsetPanel;
	private DJList stratumList;
	private DJList columnList;
	private DJList rowList;
	private JButton postHoc;
	private JButton statistics;
	private JButton cells;
	private IconButton addColumn;
	private JButton results;
	private IconButton removeRow;
	private IconButton addRow;
	private JButton resetButton;
	private JButton cancelButton;
	private JButton runButton;
	private JPanel strataPanel;
	private JPanel columnPanel;
	private JPanel rowPanel;
	
	private CellOptions cellOpt;
	private StatisticsOptions statOpt;
	private ResultsOptions resultOpt;
	
	private static CellOptions lastCellOpt;
	private static StatisticsOptions lastStatOpt;
	private static ResultsOptions lastResultOpt;
	private static DefaultListModel lastRowModel;
	private static DefaultListModel lastColumnModel;
	private static DefaultListModel lastStratumModel;
	private static String lastDataName;
	private static String lastSubset;
	
	public ContingencyDialog(JFrame frame) {
		super(frame);
		initGUI();
		cellOpt = new CellOptions();
		statOpt = new StatisticsOptions();
		resultOpt = new ResultsOptions();
		setToLast();
		new Thread(new Refresher()).start();
	}
	
	public void saveToLast(){
		lastDataName = variableSelector.getSelectedData();
		lastRowModel = (DefaultListModel) rowList.getModel();
		lastColumnModel = (DefaultListModel) columnList.getModel();
		lastStratumModel = (DefaultListModel) stratumList.getModel();
		lastSubset = subset.getText();
		lastCellOpt = cellOpt;
		lastStatOpt = statOpt;
		lastResultOpt = resultOpt;
	}
	
	public void setToLast(){
		boolean allExist=false;
		if(lastDataName==null || lastStratumModel==null || lastColumnModel==null || lastRowModel==null || lastStatOpt==null
				|| lastResultOpt==null || lastCellOpt == null){
			reset(true);
			return;
		}
		variableSelector.setSelectedData(lastDataName);
		allExist=variableSelector.removeAll(lastRowModel);
		if(allExist)
			rowList.setModel(lastRowModel);
		else{
			reset(true);
			return;
		}
		allExist=variableSelector.removeAll(lastColumnModel);
		if(allExist)
			columnList.setModel(lastColumnModel);
		else{
			reset(true);
			return;
		}
		allExist=variableSelector.removeAll(lastStratumModel);
		if(allExist)
			stratumList.setModel(lastStratumModel);
		else{
			reset(true);
			return;
		}
		if(isValidSubset(lastSubset,lastDataName)){
			subset.setText(lastSubset);
		}
		cellOpt = lastCellOpt;
		statOpt = lastStatOpt;
		resultOpt = lastResultOpt;
		
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);

			{
				help = new JButton();
				getContentPane().add(help, new AnchorConstraint(910, 71, 977, 15, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				help.setText("Help");
				help.setPreferredSize(new java.awt.Dimension(41, 35));
				help.addActionListener(this);
			}
			{
				subsetPanel = new JPanel();
				BorderLayout subsetPanelLayout = new BorderLayout();
				subsetPanel.setLayout(subsetPanelLayout);
				subsetPanel.setBorder(BorderFactory.createTitledBorder("Subset"));
				getContentPane().add(subsetPanel, new AnchorConstraint(772, 776, 875, 469, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				subsetPanel.setPreferredSize(new java.awt.Dimension(226, 53));
				{
					subsetScroller = new JScrollPane();
					subsetPanel.add(subsetScroller, BorderLayout.CENTER);
					{
						subset = new JTextArea();
						subsetScroller.setViewportView(subset);
						subset.setText("");
					}
				}
			}
			{
				postHoc = new JButton();
				getContentPane().add(postHoc, new AnchorConstraint(217, 954, 269, 818, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				postHoc.setText("Post-Hoc");
				postHoc.setPreferredSize(new java.awt.Dimension(100, 27));
				postHoc.addActionListener(this);
			}
			{
				statistics = new JButton();
				getContentPane().add(statistics, new AnchorConstraint(142, 954, 194, 818, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				statistics.setText("Statistics");
				statistics.setPreferredSize(new java.awt.Dimension(100, 27));
				statistics.addActionListener(this);
			}
			{
				cells = new JButton();
				getContentPane().add(cells, new AnchorConstraint(62, 954, 118, 818, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cells.setText("Cells");
				cells.setPreferredSize(new java.awt.Dimension(100, 29));
				cells.addActionListener(this);
			}
			{
				addRow = new IconButton("/icons/1rightarrow_32.png","Add Row",this,"Add Row");
				getContentPane().add(addRow, new AnchorConstraint(76, 442, 153, 371, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				addRow.setPreferredSize(new java.awt.Dimension(42, 42));
			}
			{
				resetButton = new JButton();
				getContentPane().add(resetButton, new AnchorConstraint(898, 699, 960, 601, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				resetButton.setText("Reset");
				resetButton.addActionListener(this);
				resetButton.setPreferredSize(new java.awt.Dimension(72, 32));
			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton, new AnchorConstraint(898, 837, 960, 715, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				cancelButton.setPreferredSize(new java.awt.Dimension(90, 32));
				cancelButton.addActionListener(this);
			}
			{
				runButton = new JButton();
				getContentPane().add(runButton, new AnchorConstraint(884, 984, 977, 855, AnchorConstraint.ANCHOR_NONE,
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				runButton.setText("Run");
				runButton.setPreferredSize(new java.awt.Dimension(95, 48));
				runButton.addActionListener(this);
			}
			{
				strataPanel = new JPanel();
				BorderLayout strataPanelLayout = new BorderLayout();
				strataPanel.setLayout(strataPanelLayout);
				getContentPane().add(strataPanel, new AnchorConstraint(662, 776, 749, 469, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				strataPanel.setPreferredSize(new java.awt.Dimension(226, 45));
				strataPanel.setBorder(BorderFactory.createTitledBorder("Stratify By"));
				{
					ListModel stratumListModel = new DefaultListModel();
					stratumList = new SingletonDJList();
					strataPanel.add(stratumList, BorderLayout.CENTER);
					stratumList.setModel(stratumListModel);
				}
			}
			{
				columnPanel = new JPanel();
				BorderLayout columnPanelLayout = new BorderLayout();
				columnPanel.setLayout(columnPanelLayout);
				getContentPane().add(columnPanel, new AnchorConstraint(349, 776, 639, 469, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				columnPanel.setPreferredSize(new java.awt.Dimension(226, 150));
				columnPanel.setEnabled(false);
				columnPanel.setBorder(BorderFactory.createTitledBorder("Column"));
				{
					ListModel columnListModel = 
						new DefaultListModel();
					columnList = new DJList();
					columnPanel.add(columnList, BorderLayout.CENTER);
					columnList.setModel(columnListModel);
				}
			}
			{
				rowPanel = new JPanel();
				BorderLayout rowPanelLayout = new BorderLayout();
				rowPanel.setLayout(rowPanelLayout);
				getContentPane().add(rowPanel, new AnchorConstraint(24, 777, 325, 469, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				rowPanel.setPreferredSize(new java.awt.Dimension(227, 156));
				rowPanel.setBorder(BorderFactory.createTitledBorder("Row"));
				{
					ListModel rowListModel = 
						new DefaultListModel();
					rowList = new DJList();
					rowPanel.add(rowList, BorderLayout.CENTER);
					rowList.setModel(rowListModel);
				}
			}
			{
				variableSelector = new VariableSelector();
				getContentPane().add(variableSelector, new AnchorConstraint(24, 355, 834, 18, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(248, 419));
				variableSelector.getJComboBox().addActionListener(this);
			}
			{
				removeRow = new IconButton("/icons/1leftarrow_32.png","Remove Row",this,"Remove Row");
				getContentPane().add(removeRow, new AnchorConstraint(169, 442, 153, 371, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				removeRow.setPreferredSize(new java.awt.Dimension(42, 42));
			}
			{
				addColumn = new IconButton("/icons/1rightarrow_32.png","Add Column",this,"Add Column");
				getContentPane().add(addColumn, new AnchorConstraint(405, 442, 153, 371, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				addColumn.setPreferredSize(new java.awt.Dimension(42,42));
			}
			{
				removeColumn = new IconButton("/icons/1leftarrow_32.png","Remove Column",this,"Remove Column");
				getContentPane().add(removeColumn, new AnchorConstraint(498, 442, 153, 371, AnchorConstraint.ANCHOR_REL,
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				removeColumn.setPreferredSize(new java.awt.Dimension(42,42));
			}
			{
				addStratum = new IconButton("/icons/1rightarrow_32.png","Add Stratum",this,"Add Stratum");
				getContentPane().add(addStratum, new AnchorConstraint(674, 442, 153, 371, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				addStratum.setPreferredSize(new java.awt.Dimension(42, 42));
			}
			{
				removeStratum = new IconButton("/icons/1leftarrow_32.png","Remove Stratum",this,"Remove Stratum");
				getContentPane().add(removeStratum, new AnchorConstraint(674, 442, 153, 371, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				removeStratum.setPreferredSize(new java.awt.Dimension(42, 42));
				removeStratum.setVisible(false);
			}
			{
				results = new JButton();
				getContentPane().add(results, new AnchorConstraint(293, 954, 345, 818, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				results.setText("Results");
				results.setPreferredSize(new java.awt.Dimension(100, 27));
				results.addActionListener(this);
			}

			this.setTitle("Contingency Tables");
			this.setSize(736, 539);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	
	public static boolean isValidSubset(String subset,String dataName){
		//TODO: perform subset checks
		return true;
	}
	
	public void reset(boolean resetOptions){
		rowList.setModel(new DefaultListModel());
		columnList.setModel(new DefaultListModel());
		stratumList.setModel(new DefaultListModel());
		subset.setText("");
		if(resetOptions){
			cellOpt = new CellOptions();
			statOpt = new StatisticsOptions();
			resultOpt = new ResultsOptions();
		}
		variableSelector.reset();
	}
	
	public void executeTables(){
		String data = variableSelector.getSelectedData();
		String result = resultOpt.name;
		if(result==""){
			result="tables";
			result=RController.makeValidVariableName(result);
			result=JGR.MAINRCONSOLE.getUniqueName(result);
		}else
			result=JGR.MAINRCONSOLE.getUniqueName(result);
		
		JGR.MAINRCONSOLE.execute(result+"<-contingency.tables(\n\trow.vars="+RController.makeRStringVector(rowList)+
					",\n\tcol.vars="+RController.makeRStringVector(columnList)+
					(stratumList.getModel().getSize()>0 ? ",\n\tstratum.var="+RController.makeRStringVector(stratumList) : "") +
					",data="+data+")");
		statOpt.addStatistics(result);

		JGR.MAINRCONSOLE.execute("print("+result+
				(cellOpt.row ? ",prop.r=T" :",prop.r=F")+
				(cellOpt.col ? ",prop.c=T" :",prop.c=F")+
				(cellOpt.total ? ",prop.t=T" :",prop.t=F")+
				(cellOpt.expected ? ",expected.n=T" :"")+
				(cellOpt.residuals ? ",residuals=T" :"")+
				(cellOpt.stdResiduals ? ",std.residuals=T" :"")+
				(cellOpt.adjResiduals ? ",adj.residuals=T" :"")+
				(cellOpt.noTables ? ",no.tables=T" :"")+
				")");
		if(!resultOpt.keep)
			JGR.MAINRCONSOLE.execute("remove("+result+")");
	}

	public void actionPerformed(ActionEvent evnt) {
		String cmd = evnt.getActionCommand();
		
		if(cmd == "comboBoxChanged"){
			reset(false);
		}if(cmd == "Cancel"){
			this.dispose();
		}else if(cmd == "Run"){
			executeTables();
			saveToLast();
			this.dispose();
		}else if(cmd == "Reset"){
			reset(true);
		}else if(cmd == "Add Row"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)rowList.getModel()).addElement(objs[i]);
			}
		}else if(cmd == "Remove Row"){
			Object[] objs=rowList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)rowList.getModel()).removeElement(objs[i]);
			}			
		}else if(cmd == "Add Column"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)columnList.getModel()).addElement(objs[i]);
			}
		}else if(cmd == "Remove Column"){
			Object[] objs=columnList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)columnList.getModel()).removeElement(objs[i]);
			}			
		}else if(cmd == "Add Stratum"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			if(objs.length>1){
				variableSelector.getJList().setSelectedIndex(variableSelector.getJList().getSelectedIndex());
			}else if(objs.length==1 && stratumList.getModel().getSize()==0){
				variableSelector.remove(objs[0]);
				((DefaultListModel)stratumList.getModel()).addElement(objs[0]);
				addStratum.setVisible(false);
				removeStratum.setVisible(true);
			}
		}else if(cmd == "Remove Stratum"){
			DefaultListModel tmpModel =(DefaultListModel)stratumList.getModel();
			if(tmpModel.getSize()>0){
				variableSelector.add(tmpModel.remove(0));
				addStratum.setVisible(true);
				removeStratum.setVisible(false);				
			}
		}else if(cmd == "Post-Hoc"){
			
		}else if(cmd == "Statistics"){
			StatisticsDialog dia = new StatisticsDialog(this,statOpt);
			dia.setLocationRelativeTo(this);
			dia.setVisible(true);
		}else if(cmd == "Cells"){
			CellDialog cell = new CellDialog(this,cellOpt);
			cell.setLocationRelativeTo(this);
			cell.setVisible(true);
		}else if(cmd =="Results"){
			ResultsDialog res = new ResultsDialog(this,resultOpt);
			res.setLocationRelativeTo(this);
			res.setVisible(true);
		}
		
	}

	
	
	
	public class CellOptions{
		public boolean row=true;
		public boolean col=true;
		public boolean total=false;
		public boolean expected=false;
		public boolean residuals=false;
		public boolean stdResiduals=false;
		public boolean adjResiduals=false;
		public boolean noTables=false;
	}
	
	public class LikeOptions{
		public boolean conservative = false;
		
		public LikeOptions(){}
		
		public LikeOptions(boolean con){
			conservative = con;
		}
	}
	
	public class ChiOptions {
		public boolean asy = true;
		public boolean conservative = false;
		public boolean mc = false;
		public long b = 10000;
		
		public ChiOptions(){}
		
		public ChiOptions(boolean asymptotic,boolean conserv,boolean monteCarlo,long ss){
			asy=asymptotic;
			conservative = conserv;
			mc = monteCarlo;
			b=ss;
		}
		
		public boolean isValid(){
			if((b<1 && mc) || (asy==false && mc==false))
				return false;
			else
				return true;
		}
		
		public Object clone(){
			ChiOptions tmp = new ChiOptions();
			tmp.asy=asy;
			tmp.conservative=conservative;
			tmp.mc=mc;
			tmp.b=b;
			return tmp;
		}
	}
	
	public class StatisticsOptions{
		public boolean mantelHaen=false;
		public boolean kruskal=false;
		public boolean spearmans=false;
		public boolean kendall=false;
		public boolean liklihood=false;
		public boolean fishers=false;
		public boolean chisq=true;
		public ChiOptions chiSquared=new ChiOptions();
		public LikeOptions lrTest=new LikeOptions();
		
		public void addStatistics(String result){
			if(chisq==true)
				JGR.MAINRCONSOLE.execute(result+"<-add.chi.squared("+result+
						(chiSquared.conservative ? ",conservative=T": "")+
						(chiSquared.mc ? (",simulate.p.value=T,B="+chiSquared.b) : "") + ")");
			if(liklihood)
				JGR.MAINRCONSOLE.execute(result+"<-add.likelihood.ratio("+result+
						(lrTest.conservative ? ",conservative=T": "")+ ")");
			if(fishers)
				JGR.MAINRCONSOLE.execute(result+"<-add.fishers.exact("+result+ ")");
			if(spearmans)
				JGR.MAINRCONSOLE.execute(result+"<-add.correlation("+result+ ",method='spearman')");
			if(kendall)
				JGR.MAINRCONSOLE.execute(result+"<-add.correlation("+result+ ",method='kendall')");
			if(kruskal)
				JGR.MAINRCONSOLE.execute(result+"<-add.kruskal("+result+ ")");
			if(mantelHaen)
				JGR.MAINRCONSOLE.execute(result+"<-add.mantel.haenszel("+result+ ")");
		}
	}
	
	public class ResultsOptions{
		public boolean keep = false;
		public String name = "";
	}
	
	
	
	
	public class CellDialog extends JDialog implements ActionListener {
		private JPanel cellSumPanel;
		private JCheckBox rowPerc;
		private JCheckBox noTables;
		private JButton cancel;
		private JButton okay;
		private JCheckBox adjResid;
		private JCheckBox stdResid;
		private JCheckBox resid;
		private JCheckBox expected;
		private JPanel chiSumPanel;
		private JCheckBox totalPerc;
		private JCheckBox colPerc;
		

		public CellDialog(JDialog d,CellOptions opt) {
			super(d);
			initGUI();
			setOptions(opt);

		}
		
		public void setOptions(CellOptions opt){
			rowPerc.setSelected(opt.row);
			colPerc.setSelected(opt.col);
			totalPerc.setSelected(opt.total);
			expected.setSelected(opt.expected);
			resid.setSelected(opt.residuals);
			stdResid.setSelected(opt.stdResiduals);
			adjResid.setSelected(opt.adjResiduals);
			noTables.setSelected(opt.noTables);			
		}
		
		public CellOptions getOptions(){
			CellOptions opt = new CellOptions();
			opt.row=rowPerc.isSelected();
			opt.col=colPerc.isSelected();
			opt.total=totalPerc.isSelected();
			opt.expected=expected.isSelected();
			opt.residuals=resid.isSelected();
			opt.stdResiduals=stdResid.isSelected();
			opt.adjResiduals=adjResid.isSelected();
			opt.noTables=noTables.isSelected();
			return opt;
		}
		
		private void initGUI() {
			try {
				{
					getContentPane().setLayout(null);
				}
				{
					cellSumPanel = new JPanel();
					getContentPane().add(cellSumPanel);
					cellSumPanel.setBounds(12, 17, 147, 133);
					cellSumPanel.setBorder(BorderFactory.createTitledBorder("Percentages"));
					cellSumPanel.setLayout(null);
					{
						rowPerc = new JCheckBox();
						cellSumPanel.add(rowPerc);
						rowPerc.setText("Row");
						rowPerc.setBounds(17, 26, 125, 19);
					}
					{
						colPerc = new JCheckBox();
						cellSumPanel.add(colPerc);
						colPerc.setText("Column");
						colPerc.setBounds(17, 51, 125, 19);
					}
					{
						totalPerc = new JCheckBox();
						cellSumPanel.add(totalPerc);
						totalPerc.setText("Total");
						totalPerc.setBounds(17, 76, 125, 19);
					}
				}
				{
					chiSumPanel = new JPanel();
					getContentPane().add(chiSumPanel);
					chiSumPanel.setBounds(180, 17, 208, 133);
					chiSumPanel.setBorder(BorderFactory.createTitledBorder("Chi-Squared"));
					chiSumPanel.setLayout(null);
					{
						expected = new JCheckBox();
						chiSumPanel.add(expected);
						expected.setText("Expected");
						expected.setBounds(17, 26, 180, 19);
					}
					{
						resid = new JCheckBox();
						chiSumPanel.add(resid);
						resid.setText("Residuals");
						resid.setBounds(17, 51, 180, 19);
					}
					{
						stdResid = new JCheckBox();
						chiSumPanel.add(stdResid);
						stdResid.setText("Standardized Residuals");
						stdResid.setBounds(17, 72, 180, 19);
					}
					{
						adjResid = new JCheckBox();
						chiSumPanel.add(adjResid);
						adjResid.setText("Adjusted Residuals");
						adjResid.setBounds(17, 97, 180, 19);
					}
				}
				{
					okay = new JButton();
					getContentPane().add(okay);
					okay.setText("OK");
					okay.setBounds(308, 162, 80, 29);
					okay.addActionListener(this);
				}
				{
					cancel = new JButton();
					getContentPane().add(cancel);
					cancel.setText("Cancel");
					cancel.setBounds(220, 165, 76, 22);
					cancel.addActionListener(this);
				}
				{
					noTables = new JCheckBox();
					getContentPane().add(noTables);
					noTables.setText("Don't print tables");
					noTables.setBounds(12, 162, 147, 19);
				}
				this.setTitle("Table Cell Contents");
				this.setSize(400, 225);
			} catch (Exception e) {
				new ErrorMsg(e);
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();
			if(cmd=="OK"){
				cellOpt = this.getOptions();
				this.dispose();
			}else if(cmd == "Cancel"){
				this.dispose();
			}
			
		}

	}
	

	
	public class StatisticsDialog extends JDialog implements ActionListener {
		private JPanel nomByNomPanel;
		private JCheckBox mantelHaen;
		private JButton custom;
		private JButton helpButton;
		private JButton kruskalOptions;
		private JButton exchAssump;
		private IconButton lrgAssump;
		private JCheckBox kruskal;
		private JPanel nomByOrdPanel;
		private JButton spearmanOptions;
		private IconButton lrgAssump1;
		private IconButton lrgAssump2;
		private JSeparator jSeparator4;
		private IconButton lrgAssump3;
		private IconButton lrgAssump9;
		private JCheckBox spearmans;
		private JButton kendallOptions;
		private IconButton lrgAssump4;
		private JCheckBox kendall;
		private JPanel ordByOrdPanel;
		private JButton homoAssump;
		private IconButton lrgAssump5;
		private JSeparator jSeparator3;
		private JSeparator jSeparator2;
		private JLabel strataLabel;
		private JSeparator jSeparator1;
		private JSeparator sep;
		private IconButton lrgAssump6;
		private IconButton lrgAssump7;
		private JButton liklihoodOptions;
		private JCheckBox liklihood;
		private JButton mantelOptions;
		private JButton fishersOptions;
		private JCheckBox fishers;
		private JButton chisqOptions;
		private JCheckBox chisq;
		private JButton cancel;
		private JButton okay;
		private IconButton approxAssump;
		
		private ChiOptions chiSquared;
		private LikeOptions lrTest;


		
		public StatisticsDialog(JDialog d,StatisticsOptions so) {
			super(d);
			initGUI();
			setOptions(so);
			if(statOpt.chiSquared.mc){
				lrgAssump7.setVisible(false);
				approxAssump.setVisible(true);
			}else{
				lrgAssump7.setVisible(true);
				approxAssump.setVisible(false);					
			}
		}
		
		public void setOptions(StatisticsOptions so){
			mantelHaen.setSelected(so.mantelHaen);
			kruskal.setSelected(so.kruskal);
			spearmans.setSelected(so.spearmans);
			kendall.setSelected(so.kendall);
			liklihood.setSelected(so.liklihood);
			fishers.setSelected(so.fishers);
			chisq.setSelected(so.chisq);
			chiSquared = so.chiSquared;
			lrTest=so.lrTest;
		}
		
		public StatisticsOptions getOptions(){
			StatisticsOptions so = new StatisticsOptions();
			so.mantelHaen = mantelHaen.isSelected();
			so.kruskal = kruskal.isSelected();
			so.spearmans = spearmans.isSelected();
			so.kendall = kendall.isSelected();
			so.liklihood = liklihood.isSelected();
			so.fishers = fishers.isSelected();
			so.chisq = chisq.isSelected();
			so.chiSquared = chiSquared;
			so.lrTest = lrTest;
			return so;
		}
		
		private void initGUI() {
			try {
				getContentPane().setLayout(null);
				{
					cancel = new JButton();
					getContentPane().add(cancel);
					cancel.setText("Cancel");
					cancel.setBounds(253, 298, 71, 22);
					cancel.addActionListener(this);
				}
				{
					okay = new JButton();
					getContentPane().add(okay);
					okay.setText("OK");
					okay.setBounds(330, 292, 72, 35);
					okay.addActionListener(this);
				}
				{
					nomByNomPanel = new JPanel();
					getContentPane().add(nomByNomPanel);
					nomByNomPanel.setLayout(null);
					nomByNomPanel.setBorder(BorderFactory.createTitledBorder("Nominal By Nominal"));
					nomByNomPanel.setBounds(12, 13, 192, 246);
					{
						jSeparator3 = new JSeparator();
						nomByNomPanel.add(jSeparator3);
						jSeparator3.setBounds(105, 174, 83, 4);
					}
					{
						jSeparator2 = new JSeparator();
						nomByNomPanel.add(jSeparator2);
						jSeparator2.setPreferredSize(new java.awt.Dimension(8, 4));
						jSeparator2.setBounds(5, 174, 8, 4);
					}
					{
						strataLabel = new JLabel();
						nomByNomPanel.add(strataLabel);
						strataLabel.setText("Cross-Stratum");
						strataLabel.setPreferredSize(new java.awt.Dimension(95, 15));
						strataLabel.setHorizontalAlignment(SwingConstants.CENTER);
						strataLabel.setBounds(13, 169, 95, 15);
					}
					{
						sep = new JSeparator();
						nomByNomPanel.add(sep);
						sep.setPreferredSize(new java.awt.Dimension(79, 8));
						sep.setBounds(75, 68, 79, 8);
					}
					{
						lrgAssump7 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						nomByNomPanel.add(lrgAssump7);
						lrgAssump7.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
						lrgAssump7.setBounds(32, 38, 25, 21);
					}
					{
						approxAssump = new IconButton("/icons/mcapprox_assump.png","Monte Carlo Approximation",
								null,"Monte Carlo Approximation");
						nomByNomPanel.add(approxAssump);
						approxAssump.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
						approxAssump.setBounds(32, 38, 25, 21);
					}
					{
						lrgAssump9 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						nomByNomPanel.add(lrgAssump9);
						lrgAssump9.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
						lrgAssump9.setBounds(32, 95, 25, 21);
					}
					{
						liklihood = new JCheckBox();
						nomByNomPanel.add(liklihood);
						liklihood.setText("Likelihood");
						liklihood.setBounds(17, 76, 109, 19);
					}
					{
						mantelHaen = new JCheckBox();
						nomByNomPanel.add(mantelHaen);
						mantelHaen.setText("Mantel Haenszel");
						mantelHaen.setBounds(17, 190, 132, 19);
					}
					{
						fishers = new JCheckBox();
						nomByNomPanel.add(fishers);
						fishers.setText("Fisher's Exact");
						fishers.setBounds(17, 131, 114, 19);
					}
					{
						chisqOptions = new IconButton("/icons/advanced_21.png","Chi-Squared Options",this,"Chi-Squared Options");
						nomByNomPanel.add(chisqOptions);
						chisqOptions.setBounds(149, 27, 27, 21);
					}
					{
						chisq = new JCheckBox();
						nomByNomPanel.add(chisq);
						chisq.setText("Chi-Squared");
						chisq.setBounds(17, 19, 110, 19);
					}
					{
						fishersOptions = new JButton();
						nomByNomPanel.add(fishersOptions);
						fishersOptions.setBounds(149, 137, 27, 21);
						fishersOptions.addActionListener(this);
					}
					{
						mantelOptions = new JButton();
						nomByNomPanel.add(mantelOptions);
						mantelOptions.setBounds(149, 199, 27, 21);
						mantelOptions.addActionListener(this);
					}
					{
						liklihoodOptions =  new IconButton("/icons/advanced_21.png","Liklihood Ratio Options",
								this,"Liklihood Ratio Options");
						nomByNomPanel.add(liklihoodOptions);
						liklihoodOptions.setBounds(149, 82, 27, 21);
					}
					{
						lrgAssump6 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						nomByNomPanel.add(lrgAssump6);
						lrgAssump6.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
						lrgAssump6.setBounds(32, 94, 25, 21);
					}
					{
						jSeparator1 = new JSeparator();
						nomByNomPanel.add(jSeparator1);
						jSeparator1.setPreferredSize(new java.awt.Dimension(79, 3));
						jSeparator1.setBounds(75, 122, 79, 3);
					}
					{
						lrgAssump5 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						nomByNomPanel.add(lrgAssump5);
						lrgAssump5.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump5.setBounds(32, 215, 25, 21);
					}
					{
						homoAssump = new IconButton("/icons/homo_assump.png","Homogeneity Across Strata",null,"Homogeneity Across Strata");
						nomByNomPanel.add(homoAssump);
						homoAssump.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						homoAssump.setBounds(63, 215, 25, 21);
					}
				}
				{
					ordByOrdPanel = new JPanel();
					getContentPane().add(ordByOrdPanel);
					ordByOrdPanel.setLayout(null);
					ordByOrdPanel.setBounds(217, 12, 192, 136);
					ordByOrdPanel.setBorder(BorderFactory.createTitledBorder("Ordinal By Ordinal"));
					{
						kendall = new JCheckBox();
						ordByOrdPanel.add(kendall);
						kendall.setText("Kendall's Tau");
						kendall.setBounds(17, 20, 130, 19);
					}
					{
						lrgAssump4 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						ordByOrdPanel.add(lrgAssump4);
						lrgAssump4.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump4.setBounds(33, 39, 25, 21);
					}
					{
						kendallOptions = new JButton();
						ordByOrdPanel.add(kendallOptions);
						kendallOptions.setText("Kendall Options");
						kendallOptions.setBounds(147, 27, 27, 21);
						kendallOptions.addActionListener(this);
					}
					{
						spearmans = new JCheckBox();
						ordByOrdPanel.add(spearmans);
						spearmans.setText("Spearman's Rho");
						spearmans.setBounds(17, 78, 131, 19);
					}
					{
						lrgAssump3 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						ordByOrdPanel.add(lrgAssump3);
						lrgAssump3.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump3.setBounds(33, 39, 25, 21);
					}
					{
						jSeparator4 = new JSeparator();
						ordByOrdPanel.add(jSeparator4);
						jSeparator4.setBounds(70, 70, 77, 7);
					}
					{
						lrgAssump2 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						ordByOrdPanel.add(lrgAssump2);
						lrgAssump2.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump2.setBounds(33, 39, 25, 21);
					}
					{
						lrgAssump1 = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						ordByOrdPanel.add(lrgAssump1);
						lrgAssump1.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump1.setBounds(33, 103, 25, 21);
					}
					{
						spearmanOptions = new JButton();
						ordByOrdPanel.add(spearmanOptions);
						spearmanOptions.setBounds(148, 85, 27, 21);
						spearmanOptions.addActionListener(this);
					}
				}
				{
					nomByOrdPanel = new JPanel();
					getContentPane().add(nomByOrdPanel);
					nomByOrdPanel.setBounds(217, 160, 190, 99);
					nomByOrdPanel.setBorder(BorderFactory.createTitledBorder("Nominal By Ordinal"));
					nomByOrdPanel.setLayout(null);
					{
						kruskal = new JCheckBox();
						nomByOrdPanel.add(kruskal);
						kruskal.setText("Kruskal-Wallis");
						kruskal.setBounds(17, 20, 119, 19);
					}
					{
						lrgAssump = new IconButton("/icons/N_assump.png","Large Sample",null,"Large Sample");
						nomByOrdPanel.add(lrgAssump);
						lrgAssump.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						lrgAssump.setBounds(32, 39, 25, 21);
					}
					{
						exchAssump = new IconButton("/icons/eqvar_assump.png","Exchangability",null,"Exchangability");
						nomByOrdPanel.add(exchAssump);
						exchAssump.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
						exchAssump.setBounds(63, 39, 25, 21);
					}
					{
						kruskalOptions = new JButton();
						nomByOrdPanel.add(kruskalOptions);
						kruskalOptions.setText("Kruskal-Wallis Options");
						kruskalOptions.setBounds(146, 32, 27, 21);
						kruskalOptions.addActionListener(this);
					}
				}
				{
					helpButton = new JButton();
					getContentPane().add(helpButton);
					helpButton.setText("Help");
					helpButton.setBounds(12, 293, 41, 32);
				}
				{
					custom = new JButton();
					getContentPane().add(custom);
					custom.setText("Custom");
					custom.setBounds(171, 265, 82, 22);
					custom.addActionListener(this);
				}
				this.setSize(421, 361);
				this.setTitle("Table Statistics");
				chiSquared = new ChiOptions();
				lrTest = new LikeOptions();
				//Unimplemented options
				helpButton.setVisible(false);
				kruskalOptions.setVisible(false);
				spearmanOptions.setVisible(false);
				kendallOptions.setVisible(false);
				mantelOptions.setVisible(false);
				fishersOptions.setVisible(false);
				helpButton.setVisible(false);
				custom.setVisible(false);
			} catch (Exception e) {
				new ErrorMsg(e);
			}
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if(cmd == "Cancel"){
				this.dispose();
			}else if(cmd == "OK"){
				statOpt = this.getOptions();
				this.dispose();
			}else if(cmd == "Chi-Squared Options"){
				ChiOptionDialog chi = new ChiOptionDialog(this,chiSquared);
				chi.setLocationRelativeTo(this);
				chi.setVisible(true);
			}else if(cmd == "Liklihood Ratio Options"){
				LikeOptionDialog like = new LikeOptionDialog(this,lrTest);
				like.setLocationRelativeTo(this);
				like.setVisible(true);
			}
			
		}
		
		
		public class ChiOptionDialog extends JDialog implements ActionListener {
			private JSeparator sep;
			private JSeparator jSeparator3;
			private JSeparator jSeparator2;
			private JButton cancel;
			private JButton okay;
			private JLabel simSizeLabel;
			private JTextField simSize;
			private JSeparator jSeparator1;
			private JCheckBox monteCarlo;
			private JCheckBox asymptTest;
			private JCheckBox conservative;

			
			public ChiOptionDialog(JDialog d,ChiOptions chi) {
				super(d);
				initGUI();
				setOptions(chi);
			}
			
			public void setOptions(ChiOptions chi){
				monteCarlo.setSelected(chi.mc);
				asymptTest.setSelected(chi.asy);
				conservative.setSelected(chi.conservative);
				simSize.setText((new Long(chi.b)).toString());			
			}
			
			public ChiOptions getOptions(){
				ChiOptions chi = new ChiOptions();
				long ss;
				try{
					ss =Long.parseLong(simSize.getText());
				}catch(Exception exp){
					ss=-1;
				}
				chi.b=ss;
				chi.mc=monteCarlo.isSelected();
				chi.asy=asymptTest.isSelected();
				chi.conservative=conservative.isSelected();	
				return chi;
				
			}
			
			private void initGUI() {
				try {
					AnchorLayout thisLayout = new AnchorLayout();
					getContentPane().setLayout(thisLayout);
					{
						jSeparator3 = new JSeparator();
						getContentPane().add(jSeparator3, new AnchorConstraint(178, 237, 756, 205, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						jSeparator3.setPreferredSize(new java.awt.Dimension(10, 151));
						jSeparator3.setOrientation(SwingConstants.VERTICAL);
					}
					{
						cancel = new JButton();
						getContentPane().add(cancel, new AnchorConstraint(860, 693, 944, 444, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						cancel.setText("Cancel");
						cancel.setPreferredSize(new java.awt.Dimension(79, 22));
						cancel.addActionListener(this);
					}
					{
						okay = new JButton();
						getContentPane().add(okay, new AnchorConstraint(825, 944, 975, 712, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						okay.setText("OK");
						okay.setPreferredSize(new java.awt.Dimension(74, 39));
						okay.addActionListener(this);
					}
					{
						simSizeLabel = new JLabel();
						getContentPane().add(simSizeLabel, new AnchorConstraint(545, 583, 603, 309, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						simSizeLabel.setText("Sample Size: ");
						simSizeLabel.setPreferredSize(new java.awt.Dimension(87, 15));
					}
					{
						simSize = new JTextField();
						getContentPane().add(simSize, new AnchorConstraint(530, 825, 614, 602, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						simSize.setText("5000");
						simSize.setPreferredSize(new java.awt.Dimension(71, 22));
					}
					{
						monteCarlo = new JCheckBox();
						getContentPane().add(monteCarlo, new AnchorConstraint(434, 825, 507, 256, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						monteCarlo.setText("Monte Carlo Simulation");
						monteCarlo.setPreferredSize(new java.awt.Dimension(181, 19));
						monteCarlo.addActionListener(this);
					}
					{
						asymptTest = new JCheckBox();
						getContentPane().add(asymptTest, new AnchorConstraint(239, 570, 312, 256, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						asymptTest.setText("Asymptotic");
						asymptTest.setPreferredSize(new java.awt.Dimension(100, 19));
						asymptTest.addActionListener(this);
					}
					{
						conservative = new JCheckBox();
						getContentPane().add(conservative, new AnchorConstraint(82, 794, 155, 256, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						conservative.setText("Conservative");
						conservative.setPreferredSize(new java.awt.Dimension(171, 19));
						conservative.addActionListener(this);
					}
					{
						sep = new JSeparator();
						getContentPane().add(sep, new AnchorConstraint(178, 825, 216, 205, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						sep.setPreferredSize(new java.awt.Dimension(197, 10));
					}
					{
						jSeparator1 = new JSeparator();
						getContentPane().add(jSeparator1, new AnchorConstraint(381, 693, 411, 290, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						jSeparator1.setPreferredSize(new java.awt.Dimension(128, 8));
					}
					{
						jSeparator2 = new JSeparator();
						getContentPane().add(jSeparator2, new AnchorConstraint(660, 693, 706, 290, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						jSeparator2.setPreferredSize(new java.awt.Dimension(128, 12));
					}
					this.setSize(318, 283);
					this.setTitle("Chi Squared Test Options");
				} catch (Exception e) {
					new ErrorMsg(e);
				}
			}

			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				
				if(cmd == "Cancel"){
					this.dispose();
				}else if(cmd=="OK"){
					ChiOptions tmp = this.getOptions();
					if(tmp.b<1 && monteCarlo.isSelected()){
							JOptionPane.showMessageDialog(this, "Please enter a valid monte carlo sample size");
							simSize.setText("10000");
							return;
					}
					if(!tmp.isValid()){
						JOptionPane.showMessageDialog(this, "Please select the type of test you wish to perform." +
															"\n\t(Asymptotic and/or Monte Carlo)");
						return;
					}
					chiSquared = tmp;
					if(chiSquared.mc){
						lrgAssump7.setVisible(false);
						approxAssump.setVisible(true);
					}else{
						lrgAssump7.setVisible(true);
						approxAssump.setVisible(false);					
					}
					this.dispose();
				}
				
			}
			


		}
		

		
		public class LikeOptionDialog extends JDialog implements ActionListener{
			private JCheckBox conservative;
			private JButton cancel;
			private JButton okay;
			
			public LikeOptionDialog(JDialog d,LikeOptions lrt) {
				super(d);
				initGUI();
				setOptions(lrt);
			}	
			
			public void setOptions(LikeOptions lrt){
				conservative.setSelected(lrt.conservative);
			}
			
			public LikeOptions getOptions(){
				LikeOptions lrt = new LikeOptions();
				lrt.conservative= conservative.isSelected();
				return lrt;
			}
			
			private void initGUI() {
				try {
					this.setLayout(null);
					{
						conservative = new JCheckBox();
						getContentPane().add(conservative, BorderLayout.CENTER);
						conservative.setLayout(null);
						conservative.setText("Conservative");
						conservative.setBounds(100, 18, 145, 27);
					}
					{
						okay = new JButton();
						getContentPane().add(okay);
						okay.setText("OK");
						okay.setBounds(198, 71, 87, 29);
						okay.addActionListener(this);
					}
					{
						cancel = new JButton();
						getContentPane().add(cancel);
						cancel.setText("Cancel");
						cancel.setBounds(109, 74, 77, 22);
						cancel.addActionListener(this);
					}
					this.setTitle("Liklihood Ratio Options");
					this.setSize(305, 134);
				} catch (Exception e) {
					new ErrorMsg(e);
				}
			}

			public void actionPerformed(ActionEvent arg0) {
				String cmd = arg0.getActionCommand();
				if(cmd == "OK"){
					lrTest = this.getOptions();
					this.dispose();
				}else if(cmd=="Cancel")
					this.dispose();
				
			}

		}
		
	}
	
	public class ResultsDialog extends JDialog implements ActionListener{
		private JCheckBox keep;
		private JButton okay;
		private JButton cancel;
		private JTextField resultName;
		private JLabel name;
		
		public ResultsDialog(JDialog d,ResultsOptions opt) {
			super(d);
			initGUI();
			this.setOptions(opt);
		}
		
		public void setOptions(ResultsOptions opt){
			if(opt.name=="")
				resultName.setText("<auto>");
			else
				resultName.setText(opt.name);
			keep.setSelected(opt.keep);
		}
		
		public ResultsOptions getOptions(){
			ResultsOptions opt = new ResultsOptions();
			if(resultName.getText()!="<auto>")
				opt.name = resultName.getText();
			else
				opt.name="";
			opt.keep = keep.isSelected();
			return opt;
		}
		
		private void initGUI() {
			try {
				{
					getContentPane().setLayout(null);
					{
						keep = new JCheckBox();
						getContentPane().add(keep);
						keep.setText("Keep results in workspace");
						keep.setBounds(49, 53, 216, 19);
					}
					{
						name = new JLabel();
						getContentPane().add(name);
						name.setText("Result name:");
						name.setBounds(54, 23, 86, 15);
					}
					{
						resultName = new JTextField();
						getContentPane().add(resultName);
						resultName.setText("<auto>");
						resultName.setBounds(140, 19, 98, 22);
					}
					{
						okay = new JButton();
						getContentPane().add(okay);
						okay.setText("OK");
						okay.setBounds(187, 96, 78, 35);
						okay.addActionListener(this);
					}
					{
						cancel = new JButton();
						getContentPane().add(cancel);
						cancel.setText("Cancel");
						cancel.setBounds(97, 102, 74, 22);
						cancel.addActionListener(this);
					}
				}
				this.setTitle("Result Options");
				this.setSize(288, 165);
			} catch (Exception e) {
				new ErrorMsg(e);
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();
			if(cmd =="Cancel"){
				this.dispose();
			}else if(cmd == "OK"){
				resultOpt = this.getOptions();
				this.dispose();
			}
			
		}
	}
	
	private class SingletonDJList extends DJList{
		public void drop(DropTargetDropEvent dtde) {
			try{	
				dtde.acceptDrop(DnDConstants.ACTION_MOVE);
				boolean dropped =false;
				DefaultListModel curModel = (DefaultListModel) this.getModel();
				if(curModel.getSize()>0)
					dtde.rejectDrop();
				else{
					ArrayList ary = (ArrayList)dtde.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
						      + ";class=java.util.ArrayList"));
					if(ary.size()>1)
						dtde.rejectDrop();
					else{
					dropped = arrayListHandler.importData(this,dtde.getTransferable());
					DefaultListModel newModel = (DefaultListModel) this.getModel();
					if(newModel.getSize()>1){
						dtde.rejectDrop();
						this.setModel(curModel);
					}
					}
				}
				
				dtde.dropComplete(dropped);
				dragIndex=-1;
				isAtEnd=false;
				repaint();
				if(this.getModel().getSize()>0){
					addStratum.setVisible(false);
					removeStratum.setVisible(true);
				}else{
					addStratum.setVisible(true);
					removeStratum.setVisible(false);
				}
	        }catch (Exception e){
	        	new ErrorMsg(e);
	        }
		}
		
	}
	
	class Refresher implements Runnable {
		public Refresher() {
		}

		public void run() {
			while (true)
				try {
					Thread.sleep(500);
					Runnable doWorkRunnable = new Runnable() {
						public void run() { 
							if(stratumList.getModel().getSize()>0){
								addStratum.setVisible(false);
								removeStratum.setVisible(true);
							}else{
								addStratum.setVisible(true);
								removeStratum.setVisible(false);
							}
						}};
					SwingUtilities.invokeLater(doWorkRunnable);
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}
	
}
