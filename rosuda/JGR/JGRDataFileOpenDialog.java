package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.rosuda.JGR.toolkit.ExtensionFileFilter;
import org.rosuda.JGR.toolkit.JComboBoxExt;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JRI.REXP;

/**
 * JGRDataFileOpenDialog - implementation of a file-dialog which allows loading
 * datasets into R by choosing several options.
 * 
 * @author Markus Helbig
 * @deprecated use Data Loader RoSuDa 2003 - 2005
 */

public class JGRDataFileOpenDialog extends JFileChooser implements ActionListener, ItemListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2438253324279973086L;
	public static String extensions[][] = new String[][] { { "rda", "rdata" }, { "csv" }, { "txt" } };
	public static String extensionDescription[] = new String[] { "R (*.rda *.rdata)", "Comma seperated (*.csv)", "Text file (*.txt)" };

	private final JTextField dataName = new JTextField();

	private final JCheckBox header = new JCheckBox("Header", true);

	private final JCheckBox attach = new JCheckBox("Attach", false);

	private final JComboBoxExt sepsBox = new JComboBoxExt(new String[] { "\\t", "\\w", ",", ";", "|", "Others..." });

	private final String[] seps = new String[] { "\\t", "", ",", ";", "|" };

	private final JComboBoxExt quoteBox = new JComboBoxExt(new String[] { "None", "\\\"", "\\'", "Others..." });

	private final String[] quotes = new String[] { "", "\\\"", "\\'" };

	/**
	 * Create a new DataFileOpenDialog
	 * 
	 * @param f
	 *            parent frame
	 * @param directory
	 *            current directory
	 */
	public JGRDataFileOpenDialog(Frame f, String directory) {
		FileFilter extFilter;
		this.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);

		this.addActionListener(this);
		this.addPropertyChangeListener(this);
		if (directory != null && new File(directory).exists())
			this.setCurrentDirectory(new File(directory));
		for (int i = 0; i < extensionDescription.length; i++) {
			extFilter = new ExtensionFileFilter(extensionDescription[i], extensions[i]);
			this.addChoosableFileFilter(extFilter);
		}
		this.setFileFilter(this.getAcceptAllFileFilter());
		this.showOpenDialog(f);
		/*
		 * dataName.setMinimumSize(new Dimension(180, 22));
		 * dataName.setPreferredSize(new Dimension(180, 22));
		 * dataName.setMaximumSize(new Dimension(180, 22));
		 * quoteBox.setMinimumSize(new Dimension(90, 22));
		 * quoteBox.setPreferredSize(new Dimension(90, 22));
		 * quoteBox.setMaximumSize(new Dimension(90, 22));
		 * sepsBox.setMinimumSize(new Dimension(90, 22));
		 * sepsBox.setPreferredSize(new Dimension(90, 22));
		 * sepsBox.setMaximumSize(new Dimension(90, 22));
		 * quoteBox.addItemListener(this); sepsBox.addItemListener(this);
		 * this.addActionListener(this); this.addPropertyChangeListener(this);
		 * if (directory != null && new File(directory).exists())
		 * this.setCurrentDirectory(new File(directory)); JPanel options = new
		 * JPanel(); JPanel command = new JPanel(new
		 * FlowLayout(FlowLayout.LEFT)); command.add(new
		 * JLabel(" read.table(...) -> ")); command.add(dataName); JPanel
		 * command2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		 * command2.add(header); command2.add(new JLabel(", sep="));
		 * command2.add(sepsBox); command2.add(new JLabel(", quote="));
		 * command2.add(quoteBox); JPanel att = new JPanel(new
		 * FlowLayout(FlowLayout.LEFT)); att.add(attach); options.add(command);
		 * options.add(command2); options.add(att); if
		 * (System.getProperty("os.name").startsWith("Window")) { try { JPanel
		 * fileview = (JPanel) ((JComponent) ((JComponent) this
		 * .getComponent(2)).getComponent(2)).getComponent(2);
		 * fileview.add(command); fileview.add(command2); fileview.add(att);
		 * JPanel pp = (JPanel) ((JComponent) ((JComponent) this
		 * .getComponent(2)).getComponent(2)).getComponent(0); pp.add(new
		 * JPanel()); this.setPreferredSize(new Dimension(660, 450)); } catch
		 * (Exception e) { JPanel filename = (JPanel) this.getComponent(this
		 * .getComponentCount() - 1); JPanel buttons = (JPanel)
		 * filename.getComponent(filename .getComponentCount() - 1);
		 * this.setControlButtonsAreShown(false); filename.add(command);
		 * filename.add(command2); filename.add(att); filename.add(buttons);
		 * this.setPreferredSize(new Dimension(550, 450)); } } else { JPanel
		 * filename = (JPanel) this.getComponent(this .getComponentCount() - 1);
		 * JPanel buttons = (JPanel) filename.getComponent(filename
		 * .getComponentCount() - 1); this.setControlButtonsAreShown(false);
		 * filename.add(command); filename.add(command2); filename.add(att);
		 * filename.add(buttons); this.setPreferredSize(new Dimension(550,
		 * 450)); } this.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
		 * this.showOpenDialog(f);
		 */
	}

	/**
	 * Open selected datafile, with specified options, R-command:
	 * read.table(...)
	 */
	public void loadFile() {
		if (this.getSelectedFile() != null) {
			JGRPrefs.workingDirectory = this.getCurrentDirectory().getAbsolutePath() + File.separator;
			String file = this.getSelectedFile().toString();

			String useSep;
			if (sepsBox.getSelectedIndex() >= seps.length)
				useSep = sepsBox.getSelectedItem().toString();
			else
				useSep = seps[sepsBox.getSelectedIndex()];
			String useQuote;
			if (quoteBox.getSelectedIndex() >= quotes.length)
				useQuote = quoteBox.getSelectedItem().toString();
			else
				useQuote = quotes[quoteBox.getSelectedIndex()];

			String cmd = dataName.getText().trim().replaceAll("\\s", "") + " <- read.table(\"" + file.replace('\\', '/') + "\",header="
					+ (header.isSelected() ? "T" : "F") + ",sep=\"" + useSep + "\", quote=\"" + useQuote + "\")"
					+ (attach.isSelected() ? ";attach(" + dataName.getText().trim().replaceAll("\\s", "") + ")" : "") + "";
			JGR.MAINRCONSOLE.execute(cmd, true);
		}
	}

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		String fileName = this.getSelectedFile().toString();

		if (cmd == "ApproveSelection") {
			if (fileName.endsWith(".rda") || fileName.endsWith(".rdata"))
				loadRdaFile(fileName);
		}
		// loadFile();
		// else if (cmd == "CancelSelection") dispose();
	}

	public void loadRdaFile(String fileName) {
		String cmd = "dataset<-load(\"" + fileName.replace('\\', '/') + "\")";
		JGR.MAINRCONSOLE.execute(cmd, true);

	}

	/**
	 * itemStateChanged: handle itemStateChanged event, et separator and quote
	 * box enabled if "Others..." is choosen.
	 */
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		boolean edit = false;
		if (source == quoteBox) {
			edit = quoteBox.getSelectedIndex() == quoteBox.getItemCount() - 1 ? true : false;
			quoteBox.setEditable(edit);
		} else if (source == sepsBox) {
			edit = sepsBox.getSelectedIndex() == sepsBox.getItemCount() - 1 ? true : false;
			sepsBox.setEditable(edit);
		}
	}

	private void checkFile(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line1 = null;
			String line2 = null;
			if (reader.ready())
				line1 = reader.readLine();
			if (reader.ready())
				line2 = reader.readLine();
			reader.close();
			if (line2 != null) {
				int i = line2.indexOf("\"");
				if (i > -1 && line2.indexOf("\"", i + 1) > -1)
					quoteBox.setSelectedItem("\\\"");
				else {
					i = line2.indexOf("\'");
					if (i > -1 && line2.indexOf("\'", i + 1) > -1)
						quoteBox.setSelectedItem("\\\'");
					else
						quoteBox.setSelectedItem("None");
				}
				sepsBox.setSelectedItem("\\w"); // fallback
				i = line2.indexOf("\t");
				if (i > -1 && line2.indexOf("\t", i + 1) > -1)
					sepsBox.setSelectedItem("\\t");
				i = line2.indexOf(";");
				if (i > -1 && line2.indexOf(";", i + 1) > -1)
					sepsBox.setSelectedItem(";");
				i = line2.indexOf(",");
				if (i > -1 && line2.indexOf(",", i + 1) > -1)
					sepsBox.setSelectedItem(",");
				i = line2.indexOf("|");
				if (i > -1 && line2.indexOf("|", i + 1) > -1)
					sepsBox.setSelectedItem("|");
				i = line2.indexOf("\\w");
				if (i == line2.indexOf("\n"))
					sepsBox.setSelectedItem("\\t");

			}
			if (line1 != null && line2 != null) {
				String sep = seps[sepsBox.getSelectedIndex()];
				sep = sep == "\\t" ? "\t" : sep;
				int z1 = 0, z2 = 0;
				if (sep.length() == 0) {
					z1 = new StringTokenizer(line1).countTokens();
					z2 = new StringTokenizer(line2).countTokens();
				} else {
					int i = -1;
					while ((i = line1.trim().indexOf(sep, i + 1)) > -1)
						z1++;
					i = -1;
					while ((i = line2.trim().indexOf(sep, i + 1)) > -1)
						z2++;
				}
				if (z1 + 1 == z2 || (z1 == z2 && line1.matches("^[a-zA-Z\"].*")))
					header.setSelected(true);
				else
					header.setSelected(false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean nameAccepted = false;

	/**
	 * propertyChange: handle propertyChange, used for setting the name where
	 * the file should be assigned to.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		File file = this.getSelectedFile();
		if (file != null && !file.isDirectory() && !nameAccepted) {
			String name = file.getName().replaceAll("\\..*", "");
			name = name.replaceAll("^[0-9]+|[^a-zA-Z|^0-9|^_]", ".");

			REXP x = ((org.rosuda.REngine.JRI.JRIEngine) JGR.getREngine()).getRni().idleEval("try(.refreshObjects(),silent=TRUE)");
			String[] r = null;
			if (x != null && (r = x.asStringArray()) != null)
				JGR.setObjects(r);
			while (JGR.OBJECTS.contains(name) && !nameAccepted) {
				String val = (String) JOptionPane.showInputDialog(new JTextField(), "Object name already used!", "Object " + name + " exists!",
						JOptionPane.PLAIN_MESSAGE, null, null, name);
				if (val != null)
					name = val;
				nameAccepted = true;
			}
			dataName.setText(name);
			checkFile(file);
		} else
			dataName.setText(null);
	}
}
