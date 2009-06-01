package org.rosuda.JGR;




import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.toolkit.ExtensionFileFilter;
import org.rosuda.JGR.toolkit.FileSelector;
import org.rosuda.JGR.editor.Editor;
import org.rosuda.JGR.util.ErrorMsg;


public class DataLoader extends JFrame implements PropertyChangeListener {
	
	private static String extensions[][] = new String[][]{	{"rda","rdata"},
	{"robj"},
	{"csv"},
	{"txt"},
	{"sav"},
	{"xpt"},
	{"dbf"},
	{"dta"},
	{"syd","sys"},
	{"arff"},
	{"rec"},
	{"mtp"},
	{"s3"} 
	};
	private static String extensionDescription[] = new String[]{	"R (*.rda *.rdata)",
	"R dput() (*.robj)",
	"Comma seperated (*.csv)",
	"Text file (*.txt)",
	"SPSS (*.sav)",
	"SAS export (*.xpt)",
	"DBase (*.dbf)",
	"Stata (*.dta)",
	"Systat (*.sys *.syd)",
	"ARFF (*.arff)",
	"Epiinfo (*.rec)",
	"Minitab (*.mtp)",
	"S data dump (*.s3)"};
	private JTextField rDataNameField;
	private String rName;
	private FileSelector fileDialog;
	private boolean nameAccepted = false;
	
	public DataLoader(){
		try{
			FileFilter extFilter;
			fileDialog = new FileSelector(this,"Load Data",FileSelector.LOAD,null,true);
			JFileChooser chooser = fileDialog.getJFileChooser();
			for(int i=0;i<extensionDescription.length;i++){
				extFilter= new ExtensionFileFilter(extensionDescription[i], extensions[i]);
				chooser.addChoosableFileFilter(extFilter);
			}
			chooser.setFileFilter(chooser.getAcceptAllFileFilter());
			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			namePanel.add(new JLabel("Set name: "));
			rDataNameField = new JTextField(20);
			namePanel.add(rDataNameField);
			fileDialog.addFooterPanel(namePanel);
			fileDialog.getJFileChooser().addPropertyChangeListener(this);
			fileDialog.setVisible(true);
			if(fileDialog.getFile()==null)
				return;
			String fileName = fileDialog.getDirectory()+fileDialog.getFile();
			rName = rDataNameField.getText();
			if(rName.length()==0)
				rName = (fileDialog.getFile().indexOf(".")<=0 ?JGR.MAINRCONSOLE.getUniqueName(fileDialog.getFile()):
						 JGR.MAINRCONSOLE.getUniqueName(fileDialog.getFile().substring(0, fileDialog.getFile().indexOf("."))) );
			rName = RController.makeValidVariableName(rName);
			loadData(fileDialog.getFile(),fileDialog.getDirectory(),rName);
		}catch(Exception er){new ErrorMsg(er);}
		
	}
	public void loadData(String fileName,String directory,String var){
		
		//String var = (fileName.indexOf(".")<=0 ?JGR.MAINRCONSOLE.getUniqueName(fileName):
		//	JGR.MAINRCONSOLE.getUniqueName(fileName.substring(0, fileName.indexOf("."))) );
		
		
		if(fileName.toLowerCase().endsWith(".rda")|| fileName.toLowerCase().endsWith(".rdata"))
			loadRdaFile(fileName,directory);
		else if(fileName.toLowerCase().endsWith(".robj"))
			loadDputFile(fileName,directory);
		else if(fileName.toLowerCase().endsWith(".r")){
			Editor temp = new Editor((directory+fileName).replace('\\', '/'),true);
			temp.dispose();
		}
		else if(fileName.toLowerCase().endsWith(".txt")|fileName.toLowerCase().endsWith(".csv"))
			loadTxtFile(fileName,directory,var);
		else {
			try{
				RController.loadPackage("foreign");
				if(fileName.toLowerCase().endsWith(".sav"))
					JGR.MAINRCONSOLE.executeLater(var+" <- as.data.frame(read.spss('"+(directory+fileName).replace('\\', '/')+"'))",true);
				else if(fileName.toLowerCase().endsWith(".xpt") |fileName.toLowerCase().endsWith(".xport"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.xport('"+(directory+fileName).replace('\\', '/')+"')",true);
				else if(fileName.toLowerCase().endsWith(".dta"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.dta('"+(directory+fileName).replace('\\', '/')+"')",true);
				else if(fileName.toLowerCase().endsWith(".arff"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.arff('"+(directory+fileName).replace('\\', '/')+"')",true);
				else if(fileName.toLowerCase().endsWith(".rec"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.epiinfo('"+(directory+fileName).replace('\\', '/')+"')",true);
				else if(fileName.toLowerCase().endsWith(".mtp"))
					JGR.MAINRCONSOLE.executeLater(var+" <- as.data.frame(read.mtp('"+(directory+fileName).replace('\\', '/')+"'))",true);
				else if(fileName.toLowerCase().endsWith(".s3"))
					JGR.MAINRCONSOLE.executeLater("data.restore('"+(directory+fileName).replace('\\', '/')+"',print=TRUE)",true);
				else if(fileName.toLowerCase().endsWith(".syd") || fileName.toLowerCase().endsWith(".sys"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.systat('"+(directory+fileName).replace('\\', '/')+"')",true);
				else if(fileName.toLowerCase().endsWith(".dbf"))
					JGR.MAINRCONSOLE.executeLater(var+" <- read.dbf('"+(directory+fileName).replace('\\', '/')+"')",true);
				else{
					int opt =JOptionPane.showConfirmDialog(this, "Unknown File Type.\nWould you like to try to open it as a text data file?");
					if(opt==JOptionPane.OK_OPTION)
						loadTxtFile(fileName,directory,var);
				}
			}catch(Exception e){new ErrorMsg(e);}
		}
		
		
		
	}
	
	public void loadRdaFile(String fileName,String directory){
		String cmd = "print(load(\""+(directory+fileName).replace('\\', '/')+"\"))";
		JGR.R.eval("cat('The following data objects have been loaded:\\\n')",false);		
		JGR.MAINRCONSOLE.executeLater(cmd, true);		
	}
	
	public void loadDputFile(String fileName,String directory){
		String var = (fileName.indexOf(".")<=0 ?JGR.MAINRCONSOLE.getUniqueName(fileName):
					  JGR.MAINRCONSOLE.getUniqueName(fileName.substring(0, fileName.indexOf("."))) );
		JGR.MAINRCONSOLE.executeLater(var+" <- dget('"+(directory+fileName).replace('\\', '/')+"')",true);
	}
	
	public void loadTxtFile(String fileName,String directory, String rName){
		TxtTableLoader.run(directory+fileName,rName);
	}
	
	public String getDataName(){return rName;}
	

	/**
	 * propertyChange: handle propertyChange, used for setting the name where
	 * the set should be assigned to.
	 */
		public void propertyChange(PropertyChangeEvent e) {
			File file = fileDialog.getSelectedFile();
			if(e.getPropertyName()=="SelectedFileChangedProperty"){
				if(file!=null && !file.isDirectory() && !(file.getName().toLowerCase().endsWith(".rdata")||
					file.getName().toLowerCase().endsWith(".rda"))){
					String name = file.getName().replaceAll("\\..*", "");
					name =JGR.MAINRCONSOLE.getUniqueName(name);
					rDataNameField.setText(name);
				}else
					rDataNameField.setText("");
			}
		}
	
}
