package org.rosuda.JGR.toolkit;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import org.rosuda.ibase.Common;

/**
 * TextFinder - find specified pattern in attached textcomponent
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 */

public class TextFinder extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4833068209028584303L;

	private final Dimension screenSize = Common.screenRes;

	private final JTextField keyWordField = new JTextField();

	private JTextComponent searchArea = null;

	private final JButton searchButton = new JButton("Find");

	private final JButton cancelButton = new JButton("Cancel");

	private final JLabel status = new JLabel("                       ");

	Highlighter.HighlightPainter highLighter = new FoundHighlighter(SystemColor.textHighlight);

	private String keyWord = null;

	private int position = -1;

	private boolean found = false;

	private final TextFinder last = null;

	public TextFinder() {
		this(null);
	}

	public TextFinder(JTextComponent searchArea) {
		this.setTitle("Find");

		this.searchArea = searchArea;

		Dimension d = new Dimension(80, 25);
		searchButton.setActionCommand("search");
		searchButton.addActionListener(this);
		searchButton.setMaximumSize(d);
		searchButton.setMinimumSize(d);
		searchButton.setPreferredSize(d);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		cancelButton.setMaximumSize(d);
		cancelButton.setPreferredSize(d);
		cancelButton.setMinimumSize(d);

		FontTracker.current.add(keyWordField);
		keyWordField.setFont(JGRPrefs.DefaultFont);
		keyWordField.setMaximumSize(new Dimension(300, 25));
		keyWordField.setMinimumSize(new Dimension(300, 25));
		keyWordField.setPreferredSize(new Dimension(300, 25));

		JPanel top = new JPanel();
		top.add(keyWordField);

		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.add(status);
		bottom.add(searchButton);
		bottom.add(cancelButton);

		this.getContentPane().setLayout(new BorderLayout());

		this.getContentPane().add(top, BorderLayout.CENTER);
		this.getContentPane().add(bottom, BorderLayout.SOUTH);

		this.getRootPane().setDefaultButton(searchButton);

		this.setSize(new Dimension(320, 95));
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exit();
			}
		});
		this.setResizable(false);
	}

	private void exit() {
		removeHighlights(searchArea);
		setVisible(false);
	}

	/**
	 * Attach textcomponent to finder.
	 * 
	 * @param comp
	 *            textcomponent
	 */
	public void setSearchArea(JTextComponent comp) {
		searchArea = comp;
	}

	private void find() {
		if (searchArea == null)
			return;
		searchArea.requestFocus();
		if (keyWord != null && !keyWord.equals(keyWordField.getText().toLowerCase().trim())) {
			position = -1;
			found = false;
		}
		keyWord = keyWordField.getText().toLowerCase().trim();
		searchArea.selectAll();
		String cleanDoc = searchArea.getSelectedText();
		// System.out.println(cleanDoc);
		if (!keyWord.equals("")) {
			position = cleanDoc.toLowerCase().indexOf(keyWord, position + 1);
			if (position == -1) {
				if (!found)
					status.setText("No found!              ");
				else
					status.setText("No more results!       ");
				found = false;
			} else {
				status.setText("                       ");
				highlight(searchArea, position, position + keyWord.length());
				searchArea.select(position, position);
				found = true;
			}

		}
		this.toFront();
		this.requestFocus();
	}

	private void highlight(JTextComponent textComp, int off, int end) {
		removeHighlights(textComp);
		try {
			Highlighter hilite = textComp.getHighlighter();
			hilite.addHighlight(off, end, highLighter);
		} catch (BadLocationException e) {
		}
	}

	private void removeHighlights(JTextComponent textComp) {
		Highlighter hilite = textComp.getHighlighter();
		Highlighter.Highlight[] hilites = hilite.getHighlights();

		for (int i = 0; i < hilites.length; i++)
			if (hilites[i].getPainter() instanceof FoundHighlighter)
				hilite.removeHighlight(hilites[i]);
	}

	private void showFinder() {
		keyWordField.requestFocus();
		this.setLocation((screenSize.width - 400) / 2, (screenSize.height - 100) / 2);
		super.setVisible(true);
		super.toFront();
	}

	/**
	 * Show the textfinder.
	 * 
	 * @param next
	 *            false if show a new one, true if searching for the same
	 *            keyword as before.
	 * @return TextFinder
	 */
	public TextFinder showFind(boolean next) {
		if (!next) {
			keyWordField.setText(null);
			keyWord = null;
			position = -1;
			found = false;
			showFinder();
		} else {
			keyWordField.setText(keyWord);
			showFinder();
			find();
		}
		return last;
	}

	/**
	 * actionPerformed: handle action event: buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "cancel")
			this.exit();
		else if (cmd == "search")
			this.find();
	}

	class FoundHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
		public FoundHighlighter(Color color) {
			super(color);
		}
	}
}