package org.rosuda.deducer.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.rosuda.JGR.DataLoader;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.SaveData;
import org.rosuda.JGR.editor.Editor;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.AboutDialog;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.RDataFrameModel.RCellRenderer;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;

public class DataView extends DataViewerTab implements ActionListener {

	private ExTable table;
	private ExScrollableTable dataScrollPane;
	private String dataName;

	DataView(String dataName){
		super();
		init(dataName);
	}
	
	private void init(String dataName){
		this.dataName = dataName;		
		RDataFrameModel dataModel = new RDataFrameModel(dataName);
		dataScrollPane = new ExScrollableTable(table =new ExTable(dataModel));
		dataScrollPane.setRowNamesModel(((RDataFrameModel) dataScrollPane.
				getExTable().getModel()).getRowNamesModel());
		dataScrollPane.getExTable().setDefaultRenderer(Object.class,
				dataModel.new RCellRenderer());
		
		this.setLayout(new BorderLayout());
		this.add(dataScrollPane);
	}

	public void setData(String data) {
		dataName = data;
		RDataFrameModel dataModel = new RDataFrameModel(dataName);
		((RDataFrameModel)table.getModel()).removeCachedData();
		table.setModel(dataModel);
		dataScrollPane.getExTable().setDefaultRenderer(Object.class,
				dataModel.new RCellRenderer());
	}

	public void refresh() {
		//boolean changed=((RDataFrameModel)dataScrollPane.getExTable().getModel()).refresh();
		//if(changed){
		//	dataScrollPane.getRowNamesModel().refresh();  
		//	dataScrollPane.autoAdjustRowWidth();  
		//}
	}

	public JMenuBar generateMenuBar() {
		JFrame f = new JFrame();
		String[] Menu = { "+", "File", "@NNew Data","newdata", "@LOpen Data", "loaddata","@SSave Data", "Save Data", "-",
				 "-","@PPrint","print","~File.Quit", 
				"+","Edit","@CCopy","copy","@XCut","cut", "@VPaste","paste","-","Remove Data from Workspace", "Clear Data",
				"~Window", "+","Help","R Help","help", "~About","0" };
			JMenuBar mb = EzMenuSwing.getEzMenu(f, this, Menu);
			
			//preference and about for non-mac systems
			if(!Common.isMac()){
				EzMenuSwing.addMenuSeparator(f, "Edit");
				EzMenuSwing.addJMenuItem(f, "Help", "About", "about", this);	
				for(int i=0;i<mb.getMenuCount();i++){
					if(mb.getMenu(i).getText().equals("About")){
						mb.remove(i);
						i--;
					}
				}
			}
		return f.getJMenuBar();
	}

	public void actionPerformed(ActionEvent e) {
		//JGR.R.eval("print('"+e.getActionCommand()+"')");
		try{
			String cmd = e.getActionCommand();		
			if(cmd=="Open Data"){
				new DataLoader();	
			}else if(cmd=="Save Data"){

				new SaveData(dataName);
			}else if(cmd=="Clear Data"){
				if(dataName==null){
					JOptionPane.showMessageDialog(this, "Invalid selection: There is no data loaded.");
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(null, "Remove Data Frame "+
						dataName+" from enviornment?\n" +
								"Unsaved changes will be lost.",
						"Clear Data Frame", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(confirm == JOptionPane.NO_OPTION)
					return;
				Deducer.eval("rm("+dataName + ")");
				RController.refreshObjects();
			}else if (cmd == "about")
				new AboutDialog(null);
			else if (cmd == "cut"){
					table.cutSelection();
			}else if (cmd == "copy") {
					table.copySelection();
			}else if (cmd == "paste") {
					table.pasteSelection();
			} else if (cmd == "print"){
				try{
					table.print(JTable.PrintMode.NORMAL);
				}catch(Exception exc){}
			}else if (cmd == "editor")
				new Editor();
			else if (cmd == "exit")
				((JFrame)this.getTopLevelAncestor()).dispose();
			else if(cmd=="newdata"){
				String inputValue = JOptionPane.showInputDialog("Data Name: ");
				inputValue = Deducer.getUniqueName(inputValue);
				if(inputValue!=null){
					Deducer.eval(inputValue.trim()+"<-data.frame(Var1=NA)");
					RController.refreshObjects();
				}
			}else if (cmd == "loaddata"){
				DataLoader dld= new DataLoader();
			}else if (cmd == "help")
				Deducer.execute("help.start()");
			else if (cmd == "table"){
				DataViewer inst = new DataViewer();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}else if (cmd == "save")
				new SaveData(dataName);
			
		}catch(Exception e2){new ErrorMsg(e2);}
	}

	public void cleanUp() {
		((RDataFrameModel)table.getModel()).removeCachedData();
	}
	

}
