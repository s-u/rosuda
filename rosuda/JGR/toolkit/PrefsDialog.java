package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.rosuda.ibase.Common;

/**
 * PrefsDialog - dialog for setting preferences in JGR
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2004
 */

public class PrefsDialog extends JDialog implements ActionListener,
		ItemListener {

	private final Dimension screenSize = Common.getScreenRes();

	private final String[] sizes = { "2", "4", "6", "8", "9", "10", "11", "12", "14",
			"16", "18", "20", "22", "24" }; // ,"30","36","48","72" };

	private String[] fonts;

	private final JComboBox font = new JComboBox();

	private final JComboBox size = new JComboBox();

	private DefaultComboBoxModel mf;

	private DefaultComboBoxModel ms;

	private final JSpinner helptabs = new JSpinner();

	private final JSpinner tabWidth = new JSpinner();

	private final JCheckBox useHelpAgent = new JCheckBox("Use Help Agent",
			JGRPrefs.useHelpAgent);

	private final JCheckBox useHelpAgentConsole = new JCheckBox("in Console",
			JGRPrefs.useHelpAgentConsole);

	private final JCheckBox useHelpAgentEditor = new JCheckBox("in Editor",
			false/*JGRPrefs.useHelpAgentEditor*/);

	private final JCheckBox useEmacsKeyBindings = new JCheckBox(
			"Use Emacs Key Bindings", JGRPrefs.useEmacsKeyBindings);

	private final JCheckBox showHiddenFiles = new JCheckBox("Show hidden files",
			JGRPrefs.showHiddenFiles);

	private final JTextField workinDirectory = new JTextField(
			JGRPrefs.workingDirectory);

	private final JButton cancel = new JButton("Cancel");

	private final JButton apply = new JButton("Apply");

	private final JButton save = new JButton("Save");

	public PrefsDialog() {
		this(null);
	}

	/**
	 * Create PrefsDialog.
	 * 
	 * @param f
	 *            parent JFrame
	 */
	public PrefsDialog(JFrame f) {
		super(f, "Preferences", false);

		helptabs.setMinimumSize(new Dimension(40, 24));
		helptabs.setPreferredSize(new Dimension(40, 24));
		helptabs.setMaximumSize(new Dimension(40, 24));
		helptabs.setValue(new Integer(JGRPrefs.maxHelpTabs));

		tabWidth.setMinimumSize(new Dimension(40, 24));
		tabWidth.setPreferredSize(new Dimension(40, 24));
		tabWidth.setMaximumSize(new Dimension(40, 24));
		tabWidth.setValue(new Integer(JGRPrefs.tabWidth));

		fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames();
		font.setModel(mf = new DefaultComboBoxModel(fonts));
		size.setModel(ms = new DefaultComboBoxModel(sizes));
		mf.setSelectedItem(JGRPrefs.FontName);
		ms.setSelectedItem(new Integer(JGRPrefs.FontSize));

		font.setMinimumSize(new Dimension(200, 22));
		font.setPreferredSize(new Dimension(200, 22));
		font.setMaximumSize(new Dimension(200, 22));
		size.setMinimumSize(new Dimension(50, 22));
		size.setPreferredSize(new Dimension(50, 22));
		size.setMaximumSize(new Dimension(50, 22));
		size.setEditable(true);

		workinDirectory.setMinimumSize(new Dimension(300, 22));
		workinDirectory.setPreferredSize(new Dimension(300, 22));
		workinDirectory.setMaximumSize(new Dimension(300, 22));

		JPanel prefs = new JPanel();
		prefs.setLayout(new GridBagLayout());

		JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fontPanel.add(new JLabel("Font: "));
		fontPanel.add(font);
		fontPanel.add(new JLabel(" Size: "));
		fontPanel.add(size);

		JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		helpPanel.add(new JLabel("#Help Pages: "));
		helpPanel.add(helptabs);

		JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tabPanel.add(new JLabel("Tabwidth:"));
		tabPanel.add(tabWidth);

		useHelpAgentConsole.setEnabled(useHelpAgent.isSelected());
		useHelpAgentEditor.setEnabled(false);
//		useHelpAgentEditor.setEnabled(useHelpAgent.isSelected());

		JPanel consoleAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		consoleAgentPanel.add(new JLabel("  "));
		consoleAgentPanel.add(useHelpAgentConsole);

		JPanel editorAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		editorAgentPanel.add(new JLabel("  "));
		editorAgentPanel.add(useHelpAgentEditor);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		prefs.add(fontPanel, gbc);
		gbc.gridy = 1;
		prefs.add(helpPanel, gbc);
		gbc.gridy = 2;
		prefs.add(useHelpAgent, gbc);
		gbc.gridy = 3;
		prefs.add(consoleAgentPanel, gbc);
		gbc.gridy = 4;
		prefs.add(editorAgentPanel, gbc);
		gbc.gridy = 5;
		prefs.add(useEmacsKeyBindings, gbc);
		gbc.gridy = 6;
		prefs.add(new JLabel(
				"* Emacs Keybindings are only advisable for Mac OS X!"), gbc);

		gbc.gridy = 7;
		prefs.add(showHiddenFiles, gbc);

		// gbc.gridy = 7;
		// prefs.add(new JLabel(" "),gbc);
		gbc.gridy = 8;
		prefs.add(tabPanel, gbc);

		gbc.gridy = 9;
		prefs.add(new JLabel(" Default working directory:"), gbc);
		gbc.gridy = 10;

		JButton choose = new JButton("...");
		choose.setMinimumSize(new Dimension(40, 22));
		choose.setPreferredSize(new Dimension(40, 22));
		choose.setMaximumSize(new Dimension(40, 22));
		choose.setActionCommand("chooseWD");
		choose.addActionListener(this);

		JPanel wd = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wd.add(workinDirectory);
		wd.add(choose);
		prefs.add(wd, gbc);

		cancel.setActionCommand("cancel");
		apply.setActionCommand("apply");
		save.setActionCommand("save");

		cancel.addActionListener(this);
		apply.addActionListener(this);
		save.addActionListener(this);

		cancel.setToolTipText("Cancel");
		apply.setToolTipText("Apply changes to current session");
		save.setToolTipText("Save changes for future sessions and quit");

		useHelpAgent.addItemListener(this);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(cancel);
		buttons.add(apply);
		buttons.add(save);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(prefs, BorderLayout.NORTH);
		this.getContentPane().add(buttons, BorderLayout.SOUTH);

		this.getRootPane().setDefaultButton(save);
		this.setResizable(false);
		this.setSize(new Dimension(420, 470));
		this.setLocation((screenSize.width - this.getSize().width) / 2,
				(screenSize.height - this.getSize().height) / 2);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				dispose();
			}
		});
		this.setVisible(true);
	}

	/**
	 * Apply changes the user made.
	 */
	public void applyChanges() {
		JGRPrefs.FontName = font.getSelectedItem().toString();
		JGRPrefs.FontSize = new Integer(size.getSelectedItem().toString())
				.intValue();
		JGRPrefs.maxHelpTabs = ((Integer) helptabs.getValue()).intValue();
		JGRPrefs.useHelpAgent = useHelpAgent.isSelected();
		JGRPrefs.useHelpAgentConsole = useHelpAgentConsole.isSelected();
		JGRPrefs.useHelpAgentEditor = false; //useHelpAgentEditor.isSelected();
		JGRPrefs.useEmacsKeyBindings = useEmacsKeyBindings.isSelected();
		JGRPrefs.showHiddenFiles = showHiddenFiles.isSelected();
		JGRPrefs.workingDirectory = workinDirectory.getText().trim().length() == 0 ? System
				.getProperty("user.home")
				: workinDirectory.getText().trim();
		JGRPrefs.tabWidth = ((Integer) tabWidth.getValue()).intValue();
		JGRPrefs.apply();
	}

	/**
	 * actionPerformed: handle action event: buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "apply")
			applyChanges();
		else if (cmd == "cancel")
			dispose();
		else if (cmd == "chooseWD") {
			JFileChooser chooser = new JFileChooser(JGRPrefs.workingDirectory);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Choose Working Directory");
			chooser.setApproveButtonText("Choose");
			int r = chooser.showOpenDialog(this);
			if (r == JFileChooser.CANCEL_OPTION)
				return;
			if (chooser.getSelectedFile() != null)
				workinDirectory.setText(chooser.getSelectedFile().toString());
		} else if (cmd == "save") {
			applyChanges();
			JGRPrefs.writePrefs(false);
			this.dispose();
		}
	}

	/**
	 * itemStateChanged: handle item events: enable/ disable
	 * useHelpAgentConsole/Editor
	 */
	public void itemStateChanged(ItemEvent e) {
		useHelpAgentConsole.setEnabled(useHelpAgent.isSelected());
		useHelpAgentEditor.setEnabled(useHelpAgent.isSelected());
		if (!useHelpAgent.isSelected()) {
			useHelpAgentEditor.setSelected(false);
			useHelpAgentConsole.setSelected(false);
		}
	}
}
