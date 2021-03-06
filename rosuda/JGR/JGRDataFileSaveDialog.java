package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rosuda.JGR.toolkit.JComboBoxExt;
import org.rosuda.JGR.toolkit.JGRPrefs;

/**
 * JGRDataFileSaveDialog - implementation of a file-dialog which allows saving
 * datasets by choosing several options.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 */

public class JGRDataFileSaveDialog extends JFileChooser implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1217232299652353695L;

	private final JCheckBox append = new JCheckBox("append", false);

	private final JCheckBox quote = new JCheckBox("quote", false);

	private final JCheckBox rownames = new JCheckBox("row.names", false);

	private final JComboBoxExt sepsBox = new JComboBoxExt(new String[] { "\\t", "blank", ",", ";", "|", "Others..." });

	private final String[] seps = new String[] { "\\t", " ", ",", ";", "|" };

	private String data;

	/**
	 * Create a new Save-filedialog.
	 * 
	 * @param f
	 *            parent frame
	 * @param data
	 *            name of dataset which should be saved
	 * @param directory
	 *            current directory
	 */
	public JGRDataFileSaveDialog(Frame f, String data, String directory) {
		this.setDialogTitle("Save DatFile - " + data);
		if (directory != null && new File(directory).exists())
			this.setCurrentDirectory(new File(directory));
		this.data = data;
		this.addActionListener(this);

		sepsBox.setMinimumSize(new Dimension(90, 22));
		sepsBox.setPreferredSize(new Dimension(90, 22));
		sepsBox.setMaximumSize(new Dimension(90, 22));

		sepsBox.addItemListener(this);

		if (System.getProperty("os.name").startsWith("Window")) {
			JPanel fileview = (JPanel) ((JComponent) ((JComponent) this.getComponent(2)).getComponent(2)).getComponent(2);
			JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
			command.add(append);
			command.add(new JLabel("seps="));
			command.add(sepsBox);
			command.add(rownames);
			command.add(quote);

			fileview.add(command);
			JPanel pp = (JPanel) ((JComponent) ((JComponent) this.getComponent(2)).getComponent(2)).getComponent(0);
			pp.add(new JPanel());
			this.setPreferredSize(new Dimension(655, 450));
		} else {
			JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
			command.add(append);
			command.add(new JLabel("seps="));
			command.add(sepsBox);
			command.add(rownames);
			command.add(quote);

			JPanel filename = (JPanel) this.getComponent(this.getComponentCount() - 1);
			filename.add(command, filename.getComponentCount() - 1);
			this.setPreferredSize(new Dimension(550, 450));
		}
		this.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
		this.showSaveDialog(f);
	}

	/**
	 * Save dataset to choosen file, with specified options.
	 */
	public void saveFile() {
		if (this.getSelectedFile() != null) {
			JGRPrefs.workingDirectory = this.getCurrentDirectory().getAbsolutePath() + File.separator;
			String file = this.getSelectedFile().toString();

			String useSep;
			if (sepsBox.getSelectedIndex() >= seps.length)
				useSep = sepsBox.getSelectedItem().toString();
			else
				useSep = seps[sepsBox.getSelectedIndex()];

			String cmd = "write.table(" + data + ",\"" + file.replace('\\', '/') + "\",append=" + (append.isSelected() ? "T" : "F") + ",quote="
					+ (quote.isSelected() ? "T" : "F") + ",sep=\"" + useSep + "\"" + ",row.names=" + (rownames.isSelected() ? "T" : "F") + ")";
			JGR.MAINRCONSOLE.execute(cmd, true);
		}
	}

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "ApproveSelection")
			saveFile();
	}

	/**
	 * itemStateChanged: handle item state changed, set separator box editable
	 * if "Others..." is choosen.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == sepsBox)
			sepsBox.setEditable((sepsBox.getSelectedIndex() == sepsBox.getItemCount() - 1 ? true : false));
	}
}
