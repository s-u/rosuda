package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;

/**
 * FunctionList - implemenation of a JList with some JGR specific features.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class FunctionList extends JList implements KeyListener, MouseListener {

	private JGRObjectManager objmgr;

	private final DefaultListModel fmodel = new DefaultListModel();

	public FunctionList(JGRObjectManager obm, Collection functions) {
		this.setModel(fmodel);
		fmodel.removeAllElements();
		Iterator i = functions.iterator();
		while (i.hasNext())
			fmodel.addElement(i.next());
		objmgr = obm;
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/**
	 * Refresh list and set supplied functions.
	 * 
	 * @param functions
	 *            new functions
	 */
	public void refresh(Collection functions) {
		fmodel.removeAllElements();
		Iterator i = functions.iterator();
		while (i.hasNext())
			fmodel.addElement(i.next());

	}

	/**
	 * mouseClicked: handle mouse event: double-click -> show function.
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			RObject o = null;
			try {
				o = (RObject) this.getSelectedValue();
			} catch (Exception ex) {
			}
			if (o != null)
				RController.newFunction(o);
		}
	}

	/**
	 * mouseEntered: handle mouse event.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * mouseExited: handle mouse event.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * mousePressed: handle mouse event.
	 */
	public void mousePressed(MouseEvent e) {
		if (objmgr.summary != null)
			objmgr.summary.hide();
	}

	/**
	 * mouseReleased: handle mouse event.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * keyTyped: handle key event.
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * keyPressed: handle key event.
	 */
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * keyReleased: handle key event: remove functions.
	 */
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE
				|| e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			Object[] sfunctions = this.getSelectedValues();
			for (int i = 0; i < sfunctions.length; i++) {
				RObject o = null;
				try {
					o = (RObject) sfunctions[i];
				} catch (Exception ex) {
				}
				if (o != null) {
					JGR.R.eval("rm(" + o.getRName() + ")");
					fmodel.removeElement(sfunctions[i]);
				}
			}
		}
	}

}
