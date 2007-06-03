package org.rosuda.JGR.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.rosuda.JGR.toolkit.IconButton;

public class EditToolbar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8598025061970727075L;
	
	public EditToolbar(Frame f, ActionListener al) {
		this.add(new IconButton("/icons/new.png", "New", al, "new"));
		this.add(new IconButton("/icons/open.png", "Open", al, "open"));
		this.add(new IconButton("/icons/save.png", "Save", al, "save"));
		this.add(new Spacer(15));
		this.add(new IconButton("/icons/undo.png", "Undo", al, "undo"));
		this.add(new IconButton("/icons/redo.png", "Redo", al, "redo"));
		this.add(new Spacer(15));
		this.add(new IconButton("/icons/cut.png", "Cut", al, "cut"));
		this.add(new IconButton("/icons/copy.png", "Copy", al, "copy"));
		this.add(new IconButton("/icons/paste.png", "Paste", al, "paste"));
		this.add(new Spacer(15));
		this.add(new IconButton("/icons/find.png", "Find", al, "find"));
		this.add(new Spacer(15));
		this.add(new IconButton("/icons/help.png", "Help", al, "help"));
		f.add(this,BorderLayout.NORTH);
	}
	
	class Spacer extends JPanel {

		private static final long serialVersionUID = 4515920574842835717L;

		public Spacer(int width) {
			this.setMinimumSize(new Dimension(width, 0));
			this.setMaximumSize(new Dimension(width, 0));
			this.setPreferredSize(new Dimension(width, 0));
		}
	}

}

