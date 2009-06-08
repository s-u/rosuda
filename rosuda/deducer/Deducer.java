package org.rosuda.deducer;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


import org.rosuda.JGR.DataLoader;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.SaveData;
import org.rosuda.deducer.data.DataFrameSelector;
import org.rosuda.deducer.data.DataFrameWindow;

import org.rosuda.deducer.menu.*;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.PrefDialog;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.deducer.toolkit.DeducerPrefs;
import org.rosuda.deducer.toolkit.PrefPanel;
import org.rosuda.deducer.toolkit.VariableSelectionDialog;
import org.rosuda.deducer.menu.RecodeDialog;
import org.rosuda.deducer.menu.twosample.TwoSampleDialog;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;

public class Deducer {
	ConsoleListener cListener =  new ConsoleListener();
	static final int MENUMODIFIER = Common.isMac() ? Event.META_MASK : Event.CTRL_MASK;
	static int menuIndex=3;
	
	public Deducer(){
		String dataMenu = "Data";
		String analysisMenu = "Analysis";
		try{
			DeducerPrefs.initialize();
			if(DeducerPrefs.SHOWDATA){
				insertMenu(JGR.MAINRCONSOLE,dataMenu,menuIndex);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Edit Factor", "factor", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Recode Variables", "recode", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Reset Row Names", "reset rows", cListener);
				EzMenuSwing.getMenu(JGR.MAINRCONSOLE, dataMenu).addSeparator();
				//EzMenuSwing.addMenuSeparator(JGR.MAINRCONSOLE, dataMenu);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Sort", "sort", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Merge Data", "merge", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Transpose", "trans", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Subset", "subset", cListener);
				menuIndex++;
			}
			
			//EzMenuSwing.getMenu(JGR.MAINRCONSOLE, dataMenu).addSeparator();
			//EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Data Viewer", "table", cListener);
			if(DeducerPrefs.SHOWANALYSIS){
				insertMenu(JGR.MAINRCONSOLE,analysisMenu,menuIndex);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Frequencies", "frequency", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Descriptives", "descriptives", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Contingency Tables", "contingency", cListener);
				EzMenuSwing.getMenu(JGR.MAINRCONSOLE, analysisMenu).addSeparator();
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "One Sample Test", "onesample", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Two Sample Test", "two sample", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "K-Sample Test", "ksample", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Correlation", "corr", cListener);
				menuIndex++;
			}
			
			//Replace DataTable with Data Viewer
			JGR.MAINRCONSOLE.getJMenuBar().getMenu(menuIndex).remove(1);
			insertJMenuItem(JGR.MAINRCONSOLE, "Packages & Data", "Data Viewer", "table", cListener, 1);

			//Override New Data with Data Viewer enabled version
			JGR.MAINRCONSOLE.getJMenuBar().getMenu(0).remove(0);
			insertJMenuItem(JGR.MAINRCONSOLE, "File", "New Data", "New Data Set", cListener, 0);
			
			//Override Open Data with Data Viewer enabled version
			JGR.MAINRCONSOLE.getJMenuBar().getMenu(0).remove(1);
			insertJMenuItem(JGR.MAINRCONSOLE, "File", "Open Data", "Open Data Set", cListener, 1);
			JMenuItem open = (JMenuItem)JGR.MAINRCONSOLE.getJMenuBar().getMenu(0).getMenuComponent(1);
			open.setAccelerator(KeyStroke.getKeyStroke('L',MENUMODIFIER));
			
			//Save Data
			insertJMenuItem(JGR.MAINRCONSOLE, "File", "Save Data", "Save Data Set", cListener, 2);
			
			new Thread(new Runner()).start();		
		}catch(Exception e){new ErrorMsg(e);}
	}
	
	
	public void detach(){
		JMenuBar mb = JGR.MAINRCONSOLE.getJMenuBar();
		for(int i=0;i<mb.getMenuCount();i++){
			if(mb.getMenu(i).getText().equals("Data") ||
					mb.getMenu(i).getText().equals("Analysis")){
				mb.remove(i);
				i--;
			}
		}
	}

	class ConsoleListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();
			
			if(cmd=="New Data Set"){
				String inputValue = JOptionPane.showInputDialog("Data Name: ");
				if(inputValue!=null){
					String var = RController.makeValidVariableName(inputValue.trim());
					JGR.MAINRCONSOLE.execute(var+"<-data.frame()");
					DataFrameWindow.setTopDataWindow(var);
				}
			}else if (cmd == "Open Data Set"){
				DataLoader inst = new DataLoader();
				DataFrameWindow.setTopDataWindow(inst.getDataName());
			}else if(cmd == "Save Data Set"){
				RObject data = (new DataFrameSelector(JGR.MAINRCONSOLE)).getSelection();
				if(data!=null){
					new SaveData(data.getName());
				}
			}else if(cmd == "recode"){
				RecodeDialog recode =new RecodeDialog(JGR.MAINRCONSOLE); 
				recode.setLocationRelativeTo(null);
				recode.setVisible(true);
			}else if(cmd=="factor"){
				VariableSelectionDialog inst =new VariableSelectionDialog(JGR.MAINRCONSOLE);
				inst.SetSingleSelection(true);
				inst.setLocationRelativeTo(null);
				inst.setRFilter("is.factor");
				inst.setTitle("Select Factor to Edit");
				inst.setVisible(true);
				String variable = inst.getSelecteditem();
				if(variable==null)
					return;
				FactorDialog fact = new FactorDialog(JGR.MAINRCONSOLE,variable);
				fact.setLocationRelativeTo(null);
				fact.setVisible(true);
			}else if(cmd == "reset rows"){
				String name = null;
				RObject data = null;
				DataFrameSelector sel = new DataFrameSelector(JGR.MAINRCONSOLE);
				data = sel.getSelection();
				if(data!=null){
					name = data.getName();
					JGR.MAINRCONSOLE.executeLater("rownames("+name+") <-1:dim("+name+")[1]");
					DataFrameWindow.setTopDataWindow(name);
				}
				JGR.MAINRCONSOLE.toFront();
			}else if(cmd=="sort"){
				SortDialog sort = new SortDialog(JGR.MAINRCONSOLE);
				sort.setLocationRelativeTo(null);
				sort.setVisible(true);
			}else if(cmd == "merge"){
				MergeDialog merge =new MergeDialog(JGR.MAINRCONSOLE); 
				merge.setLocationRelativeTo(null);
				merge.setVisible(true);
			}else if (cmd == "trans"){
				String name = null;
				RObject data = null;
				DataFrameSelector sel = new DataFrameSelector(JGR.MAINRCONSOLE);
				data = sel.getSelection();
				if(data!=null){
					name = data.getName();
					JGR.MAINRCONSOLE.executeLater(name+"<-as.data.frame(t("+name+"))");
					DataFrameWindow.setTopDataWindow(name);
					JGR.MAINRCONSOLE.toFront();
				}
			}else if(cmd == "subset"){
				SubsetDialog sub = new SubsetDialog(JGR.MAINRCONSOLE);
				sub.setLocationRelativeTo(null);
				sub.setVisible(true);
				JGR.MAINRCONSOLE.toFront();
			}else if(cmd =="frequency"){
				FrequencyDialog freq = new FrequencyDialog(JGR.MAINRCONSOLE);
				freq.setLocationRelativeTo(null);
				freq.setVisible(true);
			}else if(cmd =="descriptives"){
				DescriptivesDialog desc = new DescriptivesDialog(JGR.MAINRCONSOLE);
				desc.setLocationRelativeTo(null);
				desc.setVisible(true);
			}else if(cmd =="contingency"){
				ContingencyDialog cont = new ContingencyDialog(JGR.MAINRCONSOLE);
				cont.setLocationRelativeTo(null);
				cont.setVisible(true);
			}else if (cmd == "table"){
				DataFrameWindow inst = new DataFrameWindow();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}else if(cmd=="onesample"){
				OneSampleDialog inst = new OneSampleDialog(JGR.MAINRCONSOLE);
				inst.setLocationRelativeTo(JGR.MAINRCONSOLE);
				inst.setVisible(true);
			}else if(cmd=="two sample"){
				TwoSampleDialog inst = new TwoSampleDialog(JGR.MAINRCONSOLE);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);				
			}else if(cmd=="ksample"){
				KSampleDialog inst = new KSampleDialog(JGR.MAINRCONSOLE);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}else if(cmd=="corr"){
				CorDialog inst = new CorDialog(JGR.MAINRCONSOLE);
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);				
			}
		}
	}
	
	//temporary until new version of ibase
	public static void insertMenu(JFrame f, String name,int index) {
		JMenuBar mb = f.getJMenuBar();
		JMenu m = EzMenuSwing.getMenu(f,name);
		if (m == null && index<mb.getMenuCount()){
			JMenuBar mb2 = new JMenuBar(); 
			int cnt = mb.getMenuCount();
			for(int i=0;i<cnt;i++){
				if(i==index)
					mb2.add(new JMenu(name));
				mb2.add(mb.getMenu(0));
			}
			f.setJMenuBar(mb2);			
		}else if(m==null && index==mb.getMenuCount())
			EzMenuSwing.addMenu(f,name);
	}
	public static void insertJMenuItem(JFrame f, String menu, String name,
			String command, ActionListener al,int index) {
		JMenu m = EzMenuSwing.getMenu(f, menu);
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(al);
		mi.setActionCommand(command);
		m.insert(mi,index);
	}
	
	class Runner implements Runnable {
		public Runner() {
		}

		public void run() {
			boolean flag=true;
			while (flag)
				try {
					Thread.sleep(50);
						Runnable doWorkRunnable = new Runnable() {
					    public void run() { 
					    	PrefPanel pref = new PrefPanel();
					    	PrefDialog.addPanel(pref, pref);					    	
						    if(DeducerPrefs.VIEWERATSTARTUP){
							   	DataFrameWindow inst = new DataFrameWindow();
						    	inst.setLocationRelativeTo(null);
						    	inst.setVisible(true);
						    	JGR.MAINRCONSOLE.toFront(); 
					    	}
					    	if(DeducerPrefs.USEQUAQUACHOOSER && Common.isMac())
					    		JGR.R.eval(".jChooserMacLAF()");
					    }
						};
					    if(JGR.R.idleEval("TRUE").asBool().isTRUE()){
					    		SwingUtilities.invokeLater(doWorkRunnable);
					    		flag=false;
					    }
					
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}
}
