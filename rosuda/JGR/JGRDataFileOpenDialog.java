package org.rosuda.JGR;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.StringTokenizer;

import javax.swing.*;

import org.rosuda.ibase.*;

import org.rosuda.JGR.toolkit.*;

/**
 *  JGRDataFileOpenDialog - implementation of a file-dialog which allows loading datasets into R by choosing several options.
 *
 *	@author Markus Helbig
 *
 * 	RoSuDa 2003 - 2005
 */

public class JGRDataFileOpenDialog extends JFileChooser implements ActionListener, ItemListener, PropertyChangeListener {
	
    private JTextField dataName = new JTextField();
    private JTextField otherSeps = new JTextField();
    private JCheckBox header = new JCheckBox("Header",true);
    private JCheckBox attach = new JCheckBox("Attach",false);
	
	private JComboBoxExt sepsBox = new JComboBoxExt(new String[] {"\\t","\\w",",",";","|","Others..."});
    private String[] seps = new String[] {"\\t","",",",";","|"};
	
    private JComboBoxExt quoteBox = new JComboBoxExt(new String[] {"None","\\\"","\\'","Others..."});
	private String[] quotes = new String[] {"","\\\"","\\'"};

	
	/**
	 * Create a new DataFileOpenDialog
	 * @param f parent frame
	 * @param directory current directory
	 */
	public JGRDataFileOpenDialog(Frame f,String directory) {
		
		dataName.setMinimumSize(new Dimension(180,22));
		dataName.setPreferredSize(new Dimension(180,22));
		dataName.setMaximumSize(new Dimension(180,22));
		
		quoteBox.setMinimumSize(new Dimension(90,22));
		quoteBox.setPreferredSize(new Dimension(90,22));
		quoteBox.setMaximumSize(new Dimension(90,22));
		
		sepsBox.setMinimumSize(new Dimension(90,22));
		sepsBox.setPreferredSize(new Dimension(90,22));
		sepsBox.setMaximumSize(new Dimension(90,22));
				
		quoteBox.addItemListener(this);
		sepsBox.addItemListener(this);
		
		this.addActionListener(this);
		this.addPropertyChangeListener(this);
		if (directory != null && new File(directory).exists()) this.setCurrentDirectory(new File(directory));
		
		
		
		JPanel options = new JPanel();
		BoxLayout box = new BoxLayout(options,BoxLayout.Y_AXIS);
		JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
		command.add(new JLabel(" read.table(...) -> "));
		command.add(dataName);
		
		JPanel command2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		command2.add(header);
		command2.add(new JLabel(", sep="));
		command2.add(sepsBox);
		command2.add(new JLabel(", quote="));
		command2.add(quoteBox);
		
		JPanel att = new JPanel(new FlowLayout(FlowLayout.LEFT));
		att.add(attach);
		
		
		options.add(command); 
		options.add(command2);
		options.add(att); 
		
		if (System.getProperty("os.name").startsWith("Window")) {
			JPanel fileview = (JPanel)((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(2);
			fileview.add(command);
			fileview.add(command2);
			fileview.add(att);
			JPanel pp = (JPanel) ((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(0);
			pp.add(new JPanel());
			this.setPreferredSize(new Dimension(650,450));
		}
		else {
			JPanel filename = (JPanel) this.getComponent(this.getComponentCount()-1);
			JPanel buttons = (JPanel) filename.getComponent(filename.getComponentCount()-1);
			this.setControlButtonsAreShown(false);
			filename.add(command);
			filename.add(command2);
			filename.add(att);
			filename.add(buttons);
			this.setPreferredSize(new Dimension(550,450));
		}
		this.showOpenDialog(f);
	}
	
	/**
	 * Open selected datafile, with specified options, R-command: read.table(...)
	 */
	public void loadFile() {
		if (this.getSelectedFile() != null) {
			JGR.directory = this.getCurrentDirectory().getAbsolutePath()+File.separator;
			String file = this.getSelectedFile().toString();
			
			String useSep;
			if (sepsBox.getSelectedIndex() >= seps.length) useSep = sepsBox.getSelectedItem().toString();
			else useSep = seps[sepsBox.getSelectedIndex()];
			String useQuote;
			if (quoteBox.getSelectedIndex() >= quotes.length) useQuote = quoteBox.getSelectedItem().toString();
			else useQuote = quotes[quoteBox.getSelectedIndex()];
			
			String cmd = dataName.getText().trim().replaceAll("\\s","")+ " <- read.table(\""+file.replace('\\','/')+"\",header="+(header.isSelected()?"T":"F")+",sep=\""+useSep+"\", quote=\""+useQuote+"\")"+(attach.isSelected()?";attach("+dataName.getText().trim().replaceAll("\\s","")+")":"")+"";
			JGR.MAINRCONSOLE.execute(cmd,true);
		}
	}

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "ApproveSelection") loadFile();
		//else if (cmd == "CancelSelection") dispose();
	}

	/**
	 * itemStateChanged: handle itemStateChanged event, et separator and quote box enabled if "Others..." is choosen.
	 */
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		boolean edit = false;
		if (source == quoteBox) {
			edit = quoteBox.getSelectedIndex() == quoteBox.getItemCount()-1?true:false;
			quoteBox.setEditable(edit);
		}
		else if (source == sepsBox) {
			edit = sepsBox.getSelectedIndex() == sepsBox.getItemCount()-1?true:false;
			sepsBox.setEditable(edit);
		}
	}
	
	private void checkFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line1 = null;
			String line2 = null;
			if (reader.ready()) line1 = reader.readLine();
			if (reader.ready()) line2 = reader.readLine();
			reader.close();
			if (line2 != null) {
				int i = line2.indexOf("\"");
				if (i > -1 && line2.indexOf("\"",i+1) > -1) quoteBox.setSelectedItem("\\\"");
				else {
					i = line2.indexOf("\'");
					if (i > -1 && line2.indexOf("\'",i+1) > -1) quoteBox.setSelectedItem("\\\'");
					else quoteBox.setSelectedItem("None");
				}
				sepsBox.setSelectedItem("\\w"); //fallback
				i = line2.indexOf("\t");
				if (i > -1 && line2.indexOf("\t",i+1) > -1) sepsBox.setSelectedItem("\\t");
				i = line2.indexOf(";");
				if (i > -1 && line2.indexOf(";",i+1) > -1) sepsBox.setSelectedItem(";");
				i = line2.indexOf(",");
				if (i > -1 && line2.indexOf(",",i+1) > -1) sepsBox.setSelectedItem(",");
				i = line2.indexOf("|");
				if (i > -1 && line2.indexOf("|",i+1) > -1) sepsBox.setSelectedItem("|");
			}
			if (line1 != null && line2 != null) {
				String sep = seps[sepsBox.getSelectedIndex()];
				sep = sep=="\\t"?"\t":sep;
				int z1 = 0, z2 = 0;
				if (sep.length() == 0) {
					z1 = new StringTokenizer(line1).countTokens();
					z2 = new StringTokenizer(line2).countTokens();
				}
				else {
					int i = -1; 
					while ((i = line1.trim().indexOf(sep,i+1)) > -1) z1++;
					i = -1;
					while ((i = line2.trim().indexOf(sep,i+1)) > -1) z2++;
				}
				if (z1+1==z2 || (z1==z2 && line1.matches("^[a-zA-Z\"].*"))) header.setSelected(true);
				else header.setSelected(false);
			}
			
		} catch (Exception e) { e.printStackTrace();}
	}

	/**
	 * propertyChange: handle propertyChange, used for setting the name where the file should be assigned to.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		File file = this.getSelectedFile();
		if(file!=null && !file.isDirectory()) {
			String name = file.getName().replaceAll("\\..*", "");
			name = name.replaceAll("^[0-9]+|[^a-zA-Z|^0-9|^_]",".");
			dataName.setText(name);
			checkFile(file);
		}
		else {
			dataName.setText(null);
		}
	}
}
