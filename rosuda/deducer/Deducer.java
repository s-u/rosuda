
package org.rosuda.deducer;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.UIManager;


import org.rosuda.JGR.DataLoader;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.SaveData;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.FileSelector;
import org.rosuda.JGR.toolkit.PrefDialog;
import org.rosuda.JGR.util.ErrorMsg;


import org.rosuda.deducer.menu.*;
import org.rosuda.deducer.menu.twosample.TwoSampleDialog;
import org.rosuda.deducer.models.*;
import org.rosuda.deducer.plots.*;
import org.rosuda.deducer.toolkit.DeducerPrefs;
import org.rosuda.deducer.toolkit.HelpButton;
import org.rosuda.deducer.toolkit.PrefPanel;
import org.rosuda.deducer.toolkit.VariableSelectionDialog;
import org.rosuda.deducer.data.DataFrameSelector;
import org.rosuda.deducer.data.DataFrameWindow;


import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.JRI.JRIEngine;


import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;

public class Deducer {
	ConsoleListener cListener =  new ConsoleListener();
	static final int MENUMODIFIER = Common.isMac() ? Event.META_MASK : Event.CTRL_MASK;
	static int menuIndex=3;
	static String recentActiveData = "";
	static final String Version= "0.4-0";
	public static String guiEnv = "gui.working.env";
	public static boolean insideJGR;
	public static boolean started;
	public static JRIEngine engine; 
	public Deducer(boolean jgr){
		started=false;
		try{
			if(jgr && ((JRIEngine)JGR.getREngine()).getRni()!=null){
				startWithJGR();
				(new Thread() {
					public void run() {
					    if(DeducerPrefs.VIEWERATSTARTUP){
						   	DataFrameWindow inst = new DataFrameWindow();
					    	inst.setLocationRelativeTo(null);
					    	inst.setVisible(true);
					    	JGR.MAINRCONSOLE.toFront(); 
				    	}
						new Thread(new DataRefresher()).start();
					}
				}).start();

			}
		}catch(Exception e){
			new ErrorMsg(e);
		}	
	}
	
	public void startNoJGR(){
		try{
			insideJGR=false;
		    String nativeLF = UIManager.getSystemLookAndFeelClassName();
		    try {
		        UIManager.setLookAndFeel(nativeLF);
		    } catch (InstantiationException e) {
		    } catch (ClassNotFoundException e) {
		    }  catch (IllegalAccessException e) {
		    }
			org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
			
			try {
				engine = new JRIEngine(org.rosuda.JRI.Rengine.getMainEngine());
			} catch (REngineException e) {
				new ErrorMsg(e);
			}
			Common.getScreenRes();
			JGR.setREngine(engine);
			JGR.MAINRCONSOLE=new JGRConsolePlaceholder(engine);
			JGR.MAINRCONSOLE.setVisible(false);
			JGR.STARTED=true;
			
			
			DeducerPrefs.initialize();
			
			started=true;
			eval(".javaGD.set.class.path(\"org/rosuda/JGR/JavaGD\")");
			new Thread(new DataRefresher()).start();
		}catch(Exception e){new ErrorMsg(e);}
	}
	
	public void startWithJGR(){
		insideJGR=true;
		engine = (JRIEngine)JGR.getREngine();
		String dataMenu = "Data";
		String analysisMenu = "Analysis";
		try{
			DeducerPrefs.initialize();
			if(DeducerPrefs.SHOWDATA){
				insertMenu(JGR.MAINRCONSOLE,dataMenu,menuIndex);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Edit Factor", "factor", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Recode Variables", "recode", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Transform", "transform", cListener);
				EzMenuSwing.getMenu(JGR.MAINRCONSOLE, dataMenu).addSeparator();				
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Reset Row Names", "reset rows", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Sort", "sort", cListener);				
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Transpose", "trans", cListener);
				EzMenuSwing.getMenu(JGR.MAINRCONSOLE, dataMenu).addSeparator();						
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Merge Data", "merge", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, dataMenu, "Subset", "subset", cListener);
				menuIndex++;
			}
			
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
				EzMenuSwing.getMenu(JGR.MAINRCONSOLE, analysisMenu).addSeparator();

				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Linear Model", "linear", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Logistic Model", "logistic", cListener);
				EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, analysisMenu, "Generalized Linear Model", "glm", cListener);
				menuIndex++;
			}
			

		    
		    insertMenu(JGR.MAINRCONSOLE,"Plots",menuIndex);
		    insertJMenuItem(JGR.MAINRCONSOLE, "File", "Open plot", "openplot", cListener, 3);
		    //EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, "Plots", "Open plot", "openplot", cListener);
		    EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, "Plots", "Plot builder", "plotbuilder", cListener);
		    EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, "Plots", "Import template", "Import template", cListener);		    
		    menuIndex++;
			
	    	if(DeducerPrefs.USEQUAQUACHOOSER && Common.isMac())
				Deducer.eval(".jChooserMacLAF()");
	    	
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
			
			//help
			EzMenuSwing.addJMenuItem(JGR.MAINRCONSOLE, "Help", "Deducer Help", "dhelp", cListener);
			
			//preferences
			PrefPanel prefs = new PrefPanel();
			PrefDialog.addPanel(prefs, prefs);
			started=true;
		}catch(Exception e){
			e.printStackTrace();
			new ErrorMsg(e);}		
	}
	
	public static boolean isJGR(){
		
		return insideJGR;
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
	
	public static void startViewerAndWait(){
	   	DataFrameWindow inst = new DataFrameWindow();
    	inst.setLocationRelativeTo(null);
    	inst.setVisible(true);
    	while(DataFrameWindow.dataWindows.size()>0){
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
    	}
	}

	public static String addSlashes(String str){
		if(str==null) return "";

		StringBuffer s = new StringBuffer(str);
		for(int i = 0; i < s.length(); i++){
			if(s.charAt (i) == '\\')
				s.insert(i++, '\\');
			else if(s.charAt (i) == '\"')
				s.insert(i++, '\\');
			else if(s.charAt (i) == '\'')
				s.insert(i++, '\\');
		}
		
		return s.toString();
	}

	class ConsoleListener implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();
			runCmdThreaded(cmd);
		}
	}
	
	public static void runCmdThreaded(String cmd){
		final String c = cmd;
		(new Thread() {
			public void run() {runCmd(c,false);					}
		}).start();
	}
		
	public static void runCmd(String cmd,boolean fromConsole){
		boolean needsRLocked=false;
		if(cmd.equals("dhelp")){
			HelpButton.showInBrowser(HelpButton.baseUrl+"pmwiki.php?n=Main.DeducerManual");
		} else if(cmd.equals("New Data Set")){
			String inputValue = JOptionPane.showInputDialog("Data Name: ");
			if(inputValue!=null){
				String var = RController.makeValidVariableName(inputValue.trim());
				JGR.MAINRCONSOLE.execute(var+"<-data.frame()");
				DataFrameWindow.setTopDataWindow(var);
			}
		}else if (cmd.equals("Open Data Set")){
			//needsRLocked=true;
			DataLoader inst = new DataLoader();
			DataFrameWindow.setTopDataWindow(inst.getDataName());
			Deducer.setRecentData(inst.getDataName());
		}else if(cmd.equals("Save Data Set")){
			//needsRLocked=true;
			RObject data = (new DataFrameSelector(JGR.MAINRCONSOLE)).getSelection();
			if(data!=null){
				SaveData inst = new SaveData(data.getName());
				//WindowTracker.addWindow(inst);
			}
		}else if(cmd.equals("recode")){
			needsRLocked=true;
			RecodeDialog recode =new RecodeDialog(JGR.MAINRCONSOLE); 
			recode.setLocationRelativeTo(null);
			recode.setVisible(true);
			WindowTracker.addWindow(recode);
		}else if(cmd.equals("transform")){
			needsRLocked=true;
			TransformDialog trans =new TransformDialog(JGR.MAINRCONSOLE); 
			trans.setLocationRelativeTo(null);
			trans.setVisible(true);
			WindowTracker.addWindow(trans);
		}else if(cmd.equals("factor")){
			needsRLocked=true;
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
			WindowTracker.addWindow(fact);
		}else if(cmd.equals("reset rows")){
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
		}else if(cmd.equals("sort")){
			needsRLocked=true;
			SortDialog sort = new SortDialog(JGR.MAINRCONSOLE);
			sort.setLocationRelativeTo(null);
			sort.setVisible(true);
			WindowTracker.addWindow(sort);
		}else if(cmd.equals("merge")){
			needsRLocked=true;
			MergeDialog merge =new MergeDialog(JGR.MAINRCONSOLE); 
			merge.setLocationRelativeTo(null);
			merge.setVisible(true);
			WindowTracker.addWindow(merge);
		}else if (cmd.equals("trans")){
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
		}else if(cmd.equals("subset")){
			needsRLocked=true;
			SubsetDialog sub = new SubsetDialog(JGR.MAINRCONSOLE);
			sub.setLocationRelativeTo(null);
			sub.setVisible(true);
			JGR.MAINRCONSOLE.toFront();
			WindowTracker.addWindow(sub);
		}else if(cmd.equals("frequency")){
			needsRLocked=true;
			FrequencyDialog freq = new FrequencyDialog(JGR.MAINRCONSOLE);
			WindowTracker.addWindow(freq);
			freq.setLocationRelativeTo(null);
			freq.setVisible(true);
		}else if(cmd.equals("descriptives")){
			needsRLocked=true;
			DescriptivesDialog desc = new DescriptivesDialog(JGR.MAINRCONSOLE);
			desc.setLocationRelativeTo(null);
			desc.setVisible(true);
			WindowTracker.addWindow(desc);
		}else if(cmd.equals("contingency")){
			needsRLocked=true;
			ContingencyDialog cont = new ContingencyDialog(JGR.MAINRCONSOLE);
			cont.setLocationRelativeTo(null);
			cont.setVisible(true);
			WindowTracker.addWindow(cont);
		}else if (cmd.equals("table")){
			needsRLocked=true;
			DataFrameWindow inst = new DataFrameWindow();
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
			WindowTracker.addWindow(inst);
		}else if(cmd.equals("onesample")){
			needsRLocked=true;
			OneSampleDialog inst = new OneSampleDialog(JGR.MAINRCONSOLE);
			inst.setLocationRelativeTo(JGR.MAINRCONSOLE);
			inst.setVisible(true);
			WindowTracker.addWindow(inst);
		}else if(cmd.equals("two sample")){
			needsRLocked=true;
			TwoSampleDialog inst = new TwoSampleDialog(JGR.MAINRCONSOLE);
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);	
			WindowTracker.addWindow(inst);
		}else if(cmd.equals("ksample")){
			needsRLocked=true;
			KSampleDialog inst = new KSampleDialog(JGR.MAINRCONSOLE);
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
			WindowTracker.addWindow(inst);
		}else if(cmd.equals("corr")){
			needsRLocked=true;
			CorDialog inst = new CorDialog(JGR.MAINRCONSOLE);
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);			
			WindowTracker.addWindow(inst);
		}else if(cmd.equals("glm")){
			needsRLocked=true;
			GLMDialog d = new GLMDialog(JGR.MAINRCONSOLE);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			WindowTracker.addWindow(d);
		}else if(cmd.equals("logistic")){
			needsRLocked=true;
			LogisticDialog d = new LogisticDialog(JGR.MAINRCONSOLE);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			WindowTracker.addWindow(d);
		}else if(cmd.equals("linear")){
			needsRLocked=true;
			LinearDialog d = new LinearDialog(JGR.MAINRCONSOLE);
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			WindowTracker.addWindow(d);
		}else if(cmd.equals("plotbuilder")){
			needsRLocked=true;
			PlotBuilder d = new PlotBuilder();
			d.setLocationRelativeTo(null);
			d.setVisible(true);
			WindowTracker.addWindow(d);			
		}else if(cmd.equals("openplot")){
			needsRLocked=true;
			FileSelector c = new FileSelector(null, "Open plot", FileSelector.LOAD, null, true);
			File f = c.getSelectedFile();
			if(f!=null){
				if(!f.getName().endsWith(".ggp")){
					JOptionPane.showMessageDialog(null, "This does not appear to be a"
							+" ggplot2 PlotBuilder file (extension .ggp)");
					return;
				}
				if(f!=null && f.exists()){
					PlotBuilderModel newModel = new PlotBuilderModel();
					newModel.setFromFile(f);
					PlotBuilder d = new PlotBuilder(newModel);
					d.setLocationRelativeTo(null);
					d.setVisible(true);
					WindowTracker.addWindow(d);	
				}
			}
		}else if(cmd.equals("Import template")){
			FileSelector fileDialog = new FileSelector(null, 
					"Import template", FileSelector.LOAD, null, true);
			fileDialog.setVisible(true);
			
			if(fileDialog.getFile() !=null){
				File f = fileDialog.getSelectedFile();
				if(!f.getName().endsWith(".ggtmpl")){
					JOptionPane.showMessageDialog(null, "This does not appear to be a"
							+" ggplot2 template file (extension .ggtmpl)");
					return;
				}
				if(f!=null && f.exists()){
					PlottingElement el = new PlottingElement();
					el.setFromFile(f);
					PlotController.addTemplate(el);
				}
			}
		}
		
		if(needsRLocked && fromConsole && !isJGR()){
			WindowTracker.waitForAllClosed();
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
	
	public static String getRecentData(){
		return recentActiveData;
	}
	
	public static void setRecentData(String data){
		recentActiveData=data;
	}
	
	/**
	 * DEPRICATED: Do not use
	 */
	public static org.rosuda.JRI.REXP rniEval(String cmd){
		org.rosuda.JRI.REXP result;
		boolean obtainedLock = engine.getRni().getRsync().safeLock();
		try {
			result = engine.getRni().eval(cmd);
		}finally {
				if (obtainedLock) 
					engine.getRni().getRsync().unlock();
		}
		return result;
	}
	
	/**
	 * DEPRICATED: Do not use
	 * @param cmd
	 * @return
	 */
	public static org.rosuda.JRI.REXP rniIdleEval(String cmd){
		return engine.getRni().idleEval(cmd);
	}
	
	public static REXP eval(String cmd){
		if(engine==null){
			try {
				engine = new JRIEngine(org.rosuda.JRI.Rengine.getMainEngine());
			} catch (REngineException e) {
				e.printStackTrace();
			}
		}
		try {
			return engine.parseAndEval(cmd);
		} catch (REngineException e) {
			new ErrorMsg(e);
			return null;
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
			return null;
		}
	}
	
	public static REXP idleEval(String cmd){
		if(engine==null){
			try {
				engine = new JRIEngine(org.rosuda.JRI.Rengine.getMainEngine());
			} catch (REngineException e) {
				e.printStackTrace();
			}
		}
		try {
			int lock = engine.tryLock();
			if(lock==0)
				return null;
			else{
				REXP e = engine.parseAndEval(cmd);
				engine.unlock(lock);
				return e;
			}
				
		} catch (REngineException e) {
			new ErrorMsg(e);
			return null;
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
			return null;
		}
	}
	
	public static void execute(String cmd){
		JGR.MAINRCONSOLE.execute(cmd);
	}
	
	public static void execute(String cmd, boolean hist){
		JGR.MAINRCONSOLE.execute(cmd,hist);
	}
	
	public static void executeAndContinue(String cmd){
		final String c = cmd;
		(new Thread() {
			public void run() {
		JGR.MAINRCONSOLE.execute(c);
			}
			}).start();
	}
				

	
	public static String makeFormula(DefaultListModel outcomes,DefaultListModel terms){
		String formula = "";
		if(outcomes.getSize()==1){
			formula+=outcomes.get(0)+" ~ ";
		}else{
			formula+="cbind(";
			for(int i=0;i<outcomes.getSize();i++){
				formula+=outcomes.get(i);
				if(i<outcomes.getSize()-1)
					formula+=",";
			}
			formula+=") ~ ";
		}
		for(int i=0;i<terms.getSize();i++){
			formula+=terms.get(i);
			if(i<terms.getSize()-1)
				formula+=" + ";
		}
		return formula;
	}
	
	public static String makeRCollection(Collection lis,String func, boolean quotes) {
		String q = "";
		if(quotes){
			q = "\"";
		}
		if (lis.size() == 0)
			return func+"()";
		String result = func+"(";
		Iterator it = lis.iterator();
		int ins = 1;
		while(it.hasNext()){
			String s = it.next().toString();
			result+= q + s + q;
			if(it.hasNext())
				result += ",";
			if (ins % 10 == 9)
				result += "\n";	
			ins++;
		}
		return result+")";
	}
	
	public static String makeRCollection(ListModel lis,String func, boolean quotes) {
		ArrayList a = new ArrayList();
		for(int i=0;i<lis.getSize();i++)
			a.add(lis.getElementAt(i));
		return makeRCollection(a,func,quotes);
	}
	
	public static String makeRCollection(String[] lis,String func, boolean quotes) {
		ArrayList a = new ArrayList();
		for(int i=0;i<lis.length;i++)
			a.add(lis[i]);
		return makeRCollection(a,func,quotes);
	}
	
	/**
	 * Gets a unique name based on a starting string
	 * 
	 * @param var
	 * @param envName
	 *            The name of the enviroment in which to look
	 * @return the value of var concatinated with a number
	 */
	public static String getUniqueName(String var, String envName) {
		return JGR.MAINRCONSOLE.getUniqueName(var, envName);
	}
	
	/**
	 * is package installed
	 * @param packageName
	 * @return
	 */
	public static boolean isInstalled(String packageName){
		REXP installed = Deducer.eval("'" + packageName +"' %in% installed.packages()[,1]");
		if(installed!=null && installed instanceof REXPLogical){
			return ((REXPLogical) installed).isTRUE()[0];
		}
		return false;
	}
	
	/**
	 * is package loaded
	 * @param packageName
	 * @return
	 */
	public static boolean isLoaded(String packageName){
		REXP loaded = Deducer.eval("'" + packageName +"' %in% c(names(sessionInfo()$otherPkgs), sessionInfo()$base)");
		if(loaded!=null && loaded instanceof REXPLogical){
			return ((REXPLogical) loaded).isTRUE()[0];
		}
		return false;
	}
	
	/**
	 * Checks if package is installed, and asks to install if not.
	 * @param packageName 
	 * @return "loaded", "installed" or "not-installed"
	 */
	public static String requirePackage(String packageName){
		if(isLoaded(packageName))
			return "loaded";
		if(isInstalled(packageName))
			return "installed";
		int inst = JOptionPane.showOptionDialog(null, "Package " + packageName + " not installed. \nWould you like to install it now?",
												"Install", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,
												new String[]{"Yes","No"}, "Yes");
		if(inst==JOptionPane.OK_OPTION){
			Deducer.eval("install.packages('" + packageName + "')");
			if(isInstalled(packageName))
				return "installed";
		}
		return "not-installed";
	}
	
	/**
	 * Gets a unique name based on a starting string
	 * 
	 * @param var
	 * @return the value of var concatinated with a number
	 */
	public static String getUniqueName(String var) {
		return JGR.MAINRCONSOLE.getUniqueName(var);
	}

	public static void refreshData(){
		REXP x = Deducer.eval(".getDataObjects()");
		String[] data;
		if (x != null)
			JGR.DATA.clear();
		try {
			if (x != null && !x.isNull() && (data = x.asStrings()) != null) {	
				int a = 1;
				for (int i = 0; i < data.length; i++) {
					boolean b = (data[i].equals("null") || data[i].trim().length() == 0);
					String name = b ? a + "" : data[i];
					JGR.DATA.add(RController.createRObject(name, data[++i], null, (!b)));
					a++;
				}

			}
		} catch (REXPMismatchException e) {}
	}
	
	public static Vector getData(){
		return JGR.DATA;
	}
	
	/**
	 * Refreshes objects and keywords if JGR is not doing so.
	 */
	class DataRefresher implements Runnable {

		public DataRefresher() {}

		public void run() {
			while (true)
				try {
					Thread.sleep(5000);
					refreshData();
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}
}
