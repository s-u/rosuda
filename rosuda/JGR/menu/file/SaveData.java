
package org.rosuda.JGR;

import org.rosuda.JGR.toolkit.ExtensionFileFilter;
import org.rosuda.JGR.toolkit.FileSelector;

import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class SaveData extends JFrame {
	
	private static String extensions[][] = new String[][]{	{"rda","rdata"},
		{"robj"},
		{"csv"},
		{"txt"},
		{"xpt"},
		{"dta"},
		{"arff"}
	  };
private static String extensionDescription[] = new String[]{	"R (*.rda *.rdata)",
				"R dput() (*.robj)",
				"Comma seperated (*.csv)",
				"Tab (*.txt)",
				"SAS export (*.xpt)",
				"Stata (*.dta)",
				"ARFF (*.arff)"};
	
	public SaveData(String dataName){
		FileFilter extFilter=null;
		FileSelector fileDialog = new FileSelector(this,"Load Data File",FileSelector.SAVE);
		if(fileDialog.isSwing()){
			JFileChooser chooser = fileDialog.getJFileChooser();
			for(int i=0;i<extensionDescription.length;i++)
			{
				extFilter= new ExtensionFileFilter(extensionDescription[i], extensions[i]);
				chooser.addChoosableFileFilter(extFilter);
			}
			chooser.setFileFilter(chooser.getAcceptAllFileFilter());
		}
		fileDialog.setVisible(true);
		String fileName = fileDialog.getDirectory()+fileDialog.getFile();
		fileName=fileName.replace('\\', '/');
		if(fileDialog.getFile()==null)
			return;
		if(!fileDialog.isSwing()){
			if(!fileName.toLowerCase().endsWith(".robj"))
				fileName = fileName.concat(".robj");
			JGR.MAINRCONSOLE.execute("dput("+dataName+",'"+fileName+"')",true);
		}
		//JGR.R.eval("print('"+fileName+"')");
	}
	

}
