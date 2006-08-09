package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.event.ActionListener;

import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * InsertRemoveUndoManager - undo only insertion and remove events.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2004
 */

public class InsertRemoveUndoManager extends UndoManager {

	/** Undo button from toobar */
	public IconButton undoButton;

	/** Redo button from toobar */
	public IconButton redoButton;

	public InsertRemoveUndoManager(ActionListener al) {
		this.setLimit(10000);
		undoButton = new IconButton("/icons/undo.png", "Undo", al, "undo");
		redoButton = new IconButton("/icons/redo.png", "Redo", al, "redo");
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);
	}

	public void undoableEditHappened(UndoableEditEvent e) {
		UndoableEdit ue = e.getEdit();
		addEdit(ue);
		undoButton.setEnabled(true);
	}

	/**
	 * Undo an insertion or remove event.
	 */
	public void undo() {
		while (this.editToBeUndone().getPresentationName().equals(
				UIManager.getString("AbstractDocument.styleChangeText")))
			super.undo();
		super.undo();
		if (this.editToBeUndone() == null)
			undoButton.setEnabled(false);
		redoButton.setEnabled(true);
	}

	/**
	 * Redo an insertion or remove event.
	 */
	public void redo() {
		super.redo();
		while (this.editToBeRedone() != null
				&& this
						.editToBeRedone()
						.getPresentationName()
						.equals(
								UIManager
										.getString("AbstractDocument.styleChangeText")))
			super.redo();
		if (this.editToBeRedone() == null)
			redoButton.setEnabled(false);
		undoButton.setEnabled(true);
	}
}