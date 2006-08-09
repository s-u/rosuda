package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.event.FocusEvent;

import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

/**
 * Caret implementation that doesn't blow away the selection when we lose focus.
 */
public class SelectionPreservingCaret extends DefaultCaret {

	private static SelectionPreservingCaret last = null;

	private static FocusEvent lastFocusEvent = null;

	public SelectionPreservingCaret() {
		int blinkRate = 500;
		Object o = UIManager.get("TextArea.caretBlinkRate");
		if ((o != null) && (o instanceof Integer)) {
			Integer rate = (Integer) o;
			blinkRate = rate.intValue();
		}
		setBlinkRate(blinkRate);
	}

	/**
	 * focusGainded: hanlde focus event.
	 */
	public void focusGained(FocusEvent evt) {
		super.focusGained(evt);
		if ((last != null) && (last != this))
			last.hide();
	}

	/**
	 * focusLost: hanlde focus event.
	 */
	public void focusLost(FocusEvent evt) {
		setVisible(false);
		last = this;
		lastFocusEvent = evt;
	}

	protected void hide() {
		if (last == this) {
			super.focusLost(lastFocusEvent);
			last = null;
			lastFocusEvent = null;
		}
	}
}
