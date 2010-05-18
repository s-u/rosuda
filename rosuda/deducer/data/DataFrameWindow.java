package org.rosuda.deducer.data;



import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.menu.*;
import org.rosuda.deducer.menu.twosample.TwoSampleDialog;
import org.rosuda.deducer.models.GLMDialog;
import org.rosuda.deducer.models.LinearDialog;
import org.rosuda.deducer.models.LogisticDialog;
import org.rosuda.JGR.editor.Editor;
import org.rosuda.deducer.toolkit.IconButton;
import org.rosuda.JGR.toolkit.AboutDialog;
import org.rosuda.JGR.toolkit.PrefDialog;
import org.rosuda.deducer.toolkit.VariableSelectionDialog;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.DataLoader;
import org.rosuda.JGR.JGRConsole;
import org.rosuda.JGR.SaveData;
import org.rosuda.JGR.toolkit.FileSelector;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

import org.rosuda.JRI.*;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;


import java.util.ArrayList;
import java.util.Vector;
import java.lang.Thread;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Component;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.lang.Thread;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotUndoException;
import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

import java.lang.Thread;



/**
 * A window for displaying data frames. Contains two tabs, one to view the raw data, and
 * another to view variable information
 * 
 * @author ifellows
 *
 */
public class DataFrameWindow extends TJFrame implements ActionListener {

	private ExScrollableTable dataScrollPane;
	private ExScrollableTable variableScrollPane;

	private IconButton jButton2;
	private JTabbedPane jTabbedPane1;
	private IconButton jButton1;
	private IconButton button1;
	private JSeparator separator2;
	private JSeparator separator1;
	private JLabel dataSelectorLabel;
	private JComboBox dataSelector;
	private JPanel dataSelectorPanel;

	
	private ExTable table;
	private DataFrameWindow theWindow = this; //this
	private String showData = null;    //shows this data at next opportunity
	
	public static ArrayList dataWindows;
	


	
	public DataFrameWindow() {
		super("Data Viewer", false, TJFrame.clsPackageUtil);
		initGUI(null);
		new Thread(new Refresher()).start();
		if(dataWindows==null)
			dataWindows = new ArrayList();
		dataWindows.add(0, this);
	}
	
	public DataFrameWindow(ExTable t) {
		super("Data Viewer", false, TJFrame.clsPackageUtil);
		initGUI(t);
		new Thread(new Refresher()).start();
		if(dataWindows==null)
			dataWindows = new ArrayList();
		dataWindows.add(0, this);
	}
	
	/**
	 * initiates GUI
	 * 
	 * @param t
	 * 				an ExTable to display
	 */
	private void initGUI(ExTable t) {
		try {
			this.setName("Data Viewer");
			RController.refreshObjects();
			
			
			BorderLayout thisLayout = new BorderLayout();
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setMinimumSize(new java.awt.Dimension(400, 400));
			{
				dataSelectorPanel = new JPanel();
				getContentPane().add(dataSelectorPanel, BorderLayout.NORTH);
				AnchorLayout dataSelectorPanelLayout = new AnchorLayout();
				dataSelectorPanel.setLayout(dataSelectorPanelLayout);
				dataSelectorPanel.setPreferredSize(new java.awt.Dimension(839, 52));
				dataSelectorPanel.setSize(10, 10);
				dataSelectorPanel.setMinimumSize(new java.awt.Dimension(100, 100));
				{
					button1 = new IconButton("/icons/kfloppy.png","Save Data",this,"Save Data");
					dataSelectorPanel.add(button1, new AnchorConstraint(12, 60, 805, 62, 
										AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, 
										AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
					button1.setFont(new java.awt.Font("Dialog",0,8));
					button1.setPreferredSize(new java.awt.Dimension(32,32));
					
				
				}
				{
					separator2 = new JSeparator();
					dataSelectorPanel.add(separator2, new AnchorConstraint(-76, 620, 1008, 608, 
										AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
										AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					separator2.setPreferredSize(new java.awt.Dimension(10, 64));
					separator2.setOrientation(SwingConstants.VERTICAL);
				}
				{
					separator1 = new JSeparator();
					dataSelectorPanel.add(separator1, new AnchorConstraint(8, 415, 1076, 402, 
										AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
										AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					separator1.setPreferredSize(new java.awt.Dimension(11, 63));
					separator1.setOrientation(SwingConstants.VERTICAL);
				}
				{
					dataSelectorLabel = new JLabel();
					dataSelectorPanel.add(dataSelectorLabel, new AnchorConstraint(48, 595, 432, 415, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					dataSelectorLabel.setText("Data Set");
					dataSelectorLabel.setPreferredSize(new java.awt.Dimension(151, 20));
					dataSelectorLabel.setFont(new java.awt.Font("Dialog",0,14));
					dataSelectorLabel.setHorizontalAlignment(SwingConstants.CENTER);
					dataSelectorLabel.setHorizontalTextPosition(SwingConstants.CENTER);
				}
				{
					DataFrameComboBoxModel dataSelectorModel = //new DataFrameComboBoxModel();
						new DataFrameComboBoxModel(JGR.DATA);
					dataSelector = new JComboBox();
					dataSelectorPanel.add(dataSelector, new AnchorConstraint(432, 594, 906, 415, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					dataSelector.setModel(dataSelectorModel);
					dataSelector.setPreferredSize(new java.awt.Dimension(149, 28));
					dataSelector.addActionListener(this);
				}
				{
					jButton1 = new IconButton("/icons/opendata_24.png","Open Data",this,"Open Data");
					dataSelectorPanel.add(jButton1, new AnchorConstraint(12, 60, 805, 12, AnchorConstraint.ANCHOR_ABS, 
											AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, 
											AnchorConstraint.ANCHOR_ABS));
					jButton1.setPreferredSize(new java.awt.Dimension(32,32));
				}
				{
					jButton2 = new IconButton("/icons/trashcan_remove_32.png","Clear",this,"Clear Data");
					dataSelectorPanel.add(jButton2, new AnchorConstraint(144, 12, 971, 863, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE));
					jButton2.setPreferredSize(new java.awt.Dimension(40,40));
				}
			}
			{
				jTabbedPane1 = new JTabbedPane();
				getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
				jTabbedPane1.setPreferredSize(new java.awt.Dimension(839, 395));
				jTabbedPane1.setTabPlacement(JTabbedPane.LEFT);
				{
					if(t==null){
						if(JGR.DATA==null || JGR.DATA.size()==0){
							dataScrollPane = null;
							variableScrollPane=null;
							jTabbedPane1.addTab("Data View", null, defaultPanel(), null);
							jTabbedPane1.addTab("Variable View", null, defaultPanel(), null);							
						}else{
							jTabbedPane1.addTab("Data View", null, defaultPanel(), null);
							jTabbedPane1.addTab("Variable View", null, defaultPanel(), null);
							setDataView(((RObject)JGR.DATA.elementAt(0)).getName());
							setVariableView(((RObject)JGR.DATA.elementAt(0)).getName());

						}
					}else{
						dataScrollPane = new ExScrollableTable(table = t);					
					}

				}
			}
			String[] Menu = { "+", "File", "@NNew Data","newdata", "@LOpen Data", "loaddata","@SSave Data", "Save Data", "-",
				 "-","@PPrint","print","~File.Quit", 
				"+","Edit","@CCopy","copy","@XCut","cut", "@VPaste","paste","-","Remove Data", "Clear Data",
				"~Window", "+","Help","R Help","help", "~About","0" };
			JMenuBar mb = EzMenuSwing.getEzMenu(this, this, Menu);
			
			//preference and about for non-mac systems
			if(!Common.isMac()){
				EzMenuSwing.addMenuSeparator(this, "Edit");
				EzMenuSwing.addJMenuItem(this, "Help", "About", "about", this);	
				jTabbedPane1.setTabPlacement(JTabbedPane.TOP);
				for(int i=0;i<mb.getMenuCount();i++){
					if(mb.getMenu(i).getText().equals("About")){
						mb.remove(i);
						i--;
					}
				}
			}
			
			
			pack();
			this.setSize(839, 839);
			
			final JFrame theFrame = this;
			this.addComponentListener(new java.awt.event.ComponentAdapter() {
				  public void componentResized(ComponentEvent event) {
					  theFrame.setSize(
				      Math.max(300, theFrame.getWidth()),
				      theFrame.getHeight());
				  }
			});
			
			this.addWindowFocusListener(new WindowAdapter() {
			    public void windowGainedFocus(WindowEvent e) {
			    	try{
				    	dataWindows.remove(theWindow);
				    	dataWindows.add(0,theWindow);
				    	REXP isBusy = Deducer.idleEval("2");
				    	int cnt=1;
				    	while(isBusy==null && cnt<=3){
				    		try{Thread.sleep(300);}catch(Exception ee){}
				    		isBusy = Deducer.idleEval("2");
				    		cnt++;
				    		
				    	}
				    	if(cnt==4){
				    		JGR.MAINRCONSOLE.toFront();
				    		JGR.MAINRCONSOLE.requestFocus();
				    		return;
				    	}
				    	RController.refreshObjects();
						((DataFrameComboBoxModel) dataSelector.getModel()).refresh(JGR.DATA);	
						if(JGR.DATA.size()==0){
							dataScrollPane = null;	
							jTabbedPane1.setComponentAt(0, defaultPanel());
							jTabbedPane1.setComponentAt(1, defaultPanel());
						} else if(dataScrollPane==null){
							RObject firstItem = (RObject) JGR.DATA.elementAt(0);
							dataSelector.setSelectedItem(firstItem);
							setDataView(firstItem.getName());
							setVariableView(firstItem.getName());
						}else{
							refresh();
						}
			    	}catch(Exception e1){new ErrorMsg(e1);}
			    }
			});

			jTabbedPane1.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent changeEvent) {
					int index = jTabbedPane1.getSelectedIndex();
					if(index==1){
						((RDataFrameVariableModel)variableScrollPane.getExTable().getModel()).refresh();
						variableScrollPane.getRowNamesModel().refresh();
					}else if(index==0){
						dataScrollPane.getRowNamesModel().refresh();
						((RDataFrameModel)dataScrollPane.getExTable().getModel()).refresh();
					}
				}
			});
			

		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	public void refresh(){
		boolean changed=false;
		if(dataScrollPane!=null){
			changed=((RDataFrameModel)dataScrollPane.getExTable().getModel()).refresh();
			if(changed){
				dataScrollPane.getRowNamesModel().refresh();  
				dataScrollPane.autoAdjustRowWidth();  
			}
		}
		if(variableScrollPane!=null){
			if(changed){
				((RDataFrameVariableModel)variableScrollPane.getExTable().getModel()).refresh();
				variableScrollPane.getRowNamesModel().refresh();
				variableScrollPane.autoAdjustRowWidth();
			}
		}
	}
	public synchronized void  showData(String dataName){
		showData = dataName;
	}
	
	public static void setTopDataWindow(String dataName){
		if(dataWindows==null)
			dataWindows=new ArrayList();
		if(dataWindows.size()>0){
			for(int i =0;i<dataWindows.size();i++){
				if(((DataFrameWindow)dataWindows.get(i)).isVisible()){
					((DataFrameWindow)dataWindows.get(i)).showData(dataName);
					((DataFrameWindow)dataWindows.get(i)).toFront();
					return;
				}else
					dataWindows.remove(i);
			}
			
		}
	}
	
	public void setVisibleDataFrame(String dataName){
		String name;
		for(int i=0;i<dataSelector.getModel().getSize();i++){
			name = ((DataFrameComboBoxModel)dataSelector.getModel()).getNameAt(i);
			if(name.equals(dataName)){
				dataSelector.setSelectedIndex(i);
				dataSelector.repaint();
			}
		}
	}
	
	/**
	 * Changes tab 0 (data view) to display a new data frame
	 * @param dataName
	 * 				the name of the data frame to be displayed
	 */
	public void setDataView(String dataName){
		if(dataScrollPane==null){
			RDataFrameModel dataModel = new RDataFrameModel(dataName);
			dataScrollPane = new ExScrollableTable(table =new ExTable(dataModel));
			dataScrollPane.setRowNamesModel(((RDataFrameModel) dataScrollPane.
					getExTable().getModel()).getRowNamesModel());
			dataScrollPane.getExTable().setDefaultRenderer(Object.class,
					dataModel.new RCellRenderer());
			if(jTabbedPane1.getTabCount()>0)
				jTabbedPane1.setComponentAt(0, dataScrollPane);
		}else{
			((RDataFrameModel) dataScrollPane.getExTable().getModel()).setDataName(dataName);
			dataScrollPane.getRowNamesModel().refresh();  
			dataScrollPane.autoAdjustRowWidth();
		}
	}
	
	/**
	 * Changes tab 1 (variable view) to display a new data frame's
	 * variable information
	 * 
	 * @param dataName
	 * 				the name of the data frame to be displayed
	 */
	public void setVariableView(String dataName){
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("String");
		comboBox.addItem("Factor");
		comboBox.addItem("Double");
		comboBox.addItem("Integer");
		comboBox.addItem("Logical");
		RDataFrameVariableModel varModel = new RDataFrameVariableModel(dataName);
		ExTable ex = new ExTable();	
		ex.setModel(varModel);			
		ex.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
		ex.getColumnModel().getColumn(0).setPreferredWidth(200);
		ex.getColumnModel().getColumn(1).setPreferredWidth(50);
		ex.getColumnModel().getColumn(2).setPreferredWidth(300);
		ex.setColumnSelectionAllowed(true);
		ex.setRowSelectionAllowed(true);		
		ex.getTableHeader().removeMouseListener(ex.getColumnListener());
		ex.addMouseListener(new MouseAdapter(){
		     public void mouseClicked(MouseEvent e){
		    	 ExTable extab = (ExTable)e.getSource();
		    	 if(extab.getSelectedColumn()==2){
		    		int row = extab.getSelectedRow();
		    		String varName = (String)extab.getModel().getValueAt(row, 0);
		    		String datName = ((RObject)dataSelector.getSelectedItem()).getName();
		    		REXPLogical tmp;
					tmp = (REXPLogical) Deducer.eval("is.factor("+datName+"$"+varName+")");
		    		if(tmp!=null && tmp.isTRUE()[0]){
		    			FactorDialog fact = new FactorDialog(null,datName+"$"+varName);
		    			fact.setLocation(e.getPoint());
		    			fact.setTitle("Factor Editor: "+varName);
		    			fact.setVisible(true);
		    		}
		    	 }
		      }
		     } );

		variableScrollPane = new ExScrollableTable(ex);			
		variableScrollPane.setRowNamesModel(varModel.new VariableNumberListModel());
		variableScrollPane.displayContextualMenu(false);
		if(jTabbedPane1.getTabCount()>0)
			jTabbedPane1.setComponentAt(1, variableScrollPane);	
	}
	
	
	
	public void actionPerformed(ActionEvent e) {
		//JGR.R.eval("print('"+e.getActionCommand()+"')");
		try{
			String cmd = e.getActionCommand();		
			if(cmd=="comboBoxChanged" ){
				if(dataScrollPane==null){
					setDataView(((RObject)dataSelector.getSelectedItem()).getName());
					setVariableView(((RObject)dataSelector.getSelectedItem()).getName());
					refresh();
				}else if(!((RObject)dataSelector.getSelectedItem()).getName().equals(
						((RDataFrameModel) dataScrollPane.getExTable().getModel()).getDataName())){
					setDataView(((RObject)dataSelector.getSelectedItem()).getName());
					setVariableView(((RObject)dataSelector.getSelectedItem()).getName());
					refresh();
				}
			}else if(cmd=="Open Data"){
				JGR.MAINRCONSOLE.toFront();
				JGR.MAINRCONSOLE.requestFocus();
				new DataLoader();	
			}else if(cmd=="Save Data"){

				new SaveData(((RObject)dataSelector.getSelectedItem()).getName());
			}else if(cmd=="Clear Data"){
				if(dataSelector.getSelectedItem()==null){
					JOptionPane.showMessageDialog(this, "Invalid selection: There is no data loaded.");
					return;
				}
				String data = ((RObject)dataSelector.getSelectedItem()).getName();
				int confirm = JOptionPane.showConfirmDialog(null, "Remove Data Frame "+
						data+" from enviornment?\n" +
								"Unsaved changes will be lost.",
						"Clear Data Frame", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(confirm == JOptionPane.NO_OPTION)
					return;
				JGR.MAINRCONSOLE.executeLater("rm("+data + ")");
				RController.refreshObjects();
			}else if (cmd == "about")
				new AboutDialog(this);
			else if (cmd == "cut"){
				if(jTabbedPane1.getSelectedComponent() instanceof org.rosuda.deducer.data.ExScrollableTable){
					((ExScrollableTable) jTabbedPane1.getSelectedComponent()).getExTable().cutSelection();
				}
			}else if (cmd == "copy") {
				if(jTabbedPane1.getSelectedComponent() instanceof org.rosuda.deducer.data.ExScrollableTable){
					((ExScrollableTable) jTabbedPane1.getSelectedComponent()).getExTable().copySelection();
				}
			} else if (cmd == "print"){
				try{
					((ExScrollableTable) jTabbedPane1.getSelectedComponent()).getExTable().print(JTable.PrintMode.NORMAL);
				}catch(Exception exc){}
			}else if (cmd == "editor")
				new Editor();
			else if (cmd == "exit")
				dispose();
			else if(cmd=="newdata"){
				String inputValue = JOptionPane.showInputDialog("Data Name: ");
				inputValue = JGR.MAINRCONSOLE.getUniqueName(inputValue);
				if(inputValue!=null){
					Deducer.eval(inputValue.trim()+"<-data.frame(Var1=NA)");
					RController.refreshObjects();
					((DataFrameComboBoxModel) dataSelector.getModel()).refresh(JGR.DATA);
					Deducer.eval(inputValue.trim());
					for(int i=0;i<dataSelector.getItemCount();i++)
						if(((RObject)dataSelector.getItemAt(i)).getName().equals(inputValue.trim()))
							dataSelector.setSelectedIndex(i);
				}
			}else if (cmd == "loaddata"){
				DataLoader dld= new DataLoader();
				DataFrameWindow.setTopDataWindow(dld.getDataName());
				((JFrame)DataFrameWindow.dataWindows.get(0)).toFront();
			}else if (cmd == "help")
				Deducer.execute("help.start()");
			else if (cmd == "table"){
				DataFrameWindow inst = new DataFrameWindow();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}else if (cmd == "save")
				new SaveData(((RObject)dataSelector.getSelectedItem()).getName());
			
		}catch(Exception e2){new ErrorMsg(e2);}
	}
	
	/**
	 * A panel to display when there are no data frames in the work space.
	 * 
	 * @return
	 */
	private JPanel defaultPanel(){
		JPanel panel = new JPanel();
		GridBagLayout panelLayout = new GridBagLayout();
		panelLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
		panel.setLayout(panelLayout);
		ActionListener lis = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if(cmd=="Open Data"){	
					new DataLoader();
					JGR.MAINRCONSOLE.toFront();					
				}else if(cmd=="New Data"){
					String inputValue = JOptionPane.showInputDialog("Data Name: ");
					if(inputValue!=null)
						Deducer.eval(inputValue.trim()+"<-data.frame()");
				}
			}
		};
		JButton newButton = new IconButton("/icons/newdata_128.png","New Data Frame",lis,"New Data");
		newButton.setPreferredSize(new java.awt.Dimension(128,128));
		panel.add(newButton, new GridBagConstraints(0, 0, 1,1,  0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		IconButton openButton = new IconButton("/icons/opendata_128.png","Open Data Frame",lis,"Open Data");
		openButton.setPreferredSize(new java.awt.Dimension(128,128));
		panel.add(openButton, new GridBagConstraints(0, 1, 1,1,  0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		
		return panel;
		
	}
	
	public void dispose(){
		dataWindows.remove(this);
		super.dispose();
		if(dataScrollPane!=null)
			((RDataFrameModel) dataScrollPane.getExTable().getModel()).removeCachedData();
	}
	
	public void setDataDependentMenusEnabled(boolean enabled){
		String[] dataRequiredFor = {"Edit Factor","Recode","rowReset","Sort","Merge",
									"transpose","Frequencies","Descriptives","Subset",
									"contin", "One Sample", "Two Sample",
									"ksample","corr"};
		ArrayList dataRequiredMenuItems = new ArrayList();
		JMenuItem temp;
		for(int i=0;i<dataRequiredFor.length;i++){
			temp = ((JMenuItem)EzMenuSwing.getItem(this,dataRequiredFor[i]));
			if(temp!=null)
				temp.setEnabled(enabled);
		}
	}
	/**
	 * 
	 * A model for the data selection combo box.
	 * 
	 * 
	 * @author ifellows
	 *
	 */
	public class DataFrameComboBoxModel extends DefaultComboBoxModel{

		private Vector items;
		private int selectedIndex=0;
		
		public Object getElementAt(int index){
			return items.elementAt(index);
		}
		public int getIndexOf(Object anObject){
			for(int i = 0;i<items.size();i++)
				if(items.elementAt(i)==anObject)
					return i;
			return -1;
		}
		public int 	getSize() {
			return items.size();
		}
		
		public Object getSelectedItem(){
			if(items.size()>0)
				return items.elementAt(selectedIndex);
			else
				return null;
		}
		
		public String getNameAt(int index){
			return ((RObject)items.elementAt(index)).getName();
		}
		
		public void setSelectedItem(Object obj){
			selectedIndex = getIndexOf(obj);
		}
		
		
		public DataFrameComboBoxModel(Vector v){
			items = new Vector(v);
		}
		public void refresh(Vector v){
			String dataName = null;
			this.removeAllElements();
			int prevSize = items.size();
			if(getSelectedItem()!=null)
				dataName = ((RObject)getSelectedItem()).getName();			
			items = new Vector(v);
			selectedIndex=0;			
			if(items.size()>0){
				for(int i = 0;i<items.size();i++)
					if(((RObject)items.elementAt(i)).getName().equals(dataName))
						selectedIndex =i;
				this.fireContentsChanged(this,0,prevSize);
			}

		}	
		
		
	}
	
	

	class Refresher implements Runnable {
		public Refresher() {
		}

		public void run() {
			final DataFrameWindow win = theWindow;
			boolean cont = true;
			while (cont)
				try {
					Thread.sleep(2000);
					Runnable doWorkRunnable = new Runnable() {
						public void run() { 
							refresh();
							if(Deducer.getData().size()==0){
								setDataDependentMenusEnabled(false);
							}else
								setDataDependentMenusEnabled(true);	
							
							((DataFrameComboBoxModel) dataSelector.getModel()).refresh(Deducer.getData());
							if(dataSelector.getModel().getSize()==0 && dataScrollPane!=null){
								jTabbedPane1.setComponentAt(0, defaultPanel());
								jTabbedPane1.setComponentAt(1, defaultPanel());
								dataScrollPane=null;
								variableScrollPane=null;
							}							
						}};
						
					if(win==null || !win.isDisplayable()){
						cont=false;
						if(dataScrollPane!=null)
							((RDataFrameModel) dataScrollPane.getExTable().getModel()).removeCachedData();
					}else{
						doWorkRunnable.run();
						if(showData!=null){
							setVisibleDataFrame(showData);
						if(((RObject)dataSelector.getSelectedItem()).getName().equals(showData))
							showData=null;
						}
					}
					
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}
}
