package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRDataFileSaveDialog;
import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.robjects.RObject;

/**
 * ObjectBrowserTree - show {@see RObject}s and supports dynamically loading of
 * childs.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2004
 */

public class ObjectBrowserTree extends JTree implements ActionListener,
		KeyListener, MouseListener, DragGestureListener, DragSourceListener,
		TreeWillExpandListener {

	private Collection data;

	private JGRObjectManager objmgr;

	private DragSource dragSource;

	private DataTreeModel objModel;

	private DefaultMutableTreeNode root;

	private RObject selectedObject = null;

	public ObjectBrowserTree(JGRObjectManager parent, Collection c, String name) {
		data = c;
		objmgr = parent;
		ToolTipManager.sharedInstance().registerComponent(this);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		ToolTipManager.sharedInstance().setReshowDelay(0);

		if (FontTracker.current == null)
			FontTracker.current = new FontTracker();
		FontTracker.current.add(this);

		root = new DefaultMutableTreeNode(name);
		objModel = new DataTreeModel(root);
		this.setModel(objModel);

		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY, this);

		this.setToggleClickCount(100);
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addTreeWillExpandListener(this);
	}

	private void addNodes(DefaultMutableTreeNode node) {
		RObject o = (RObject) node.getUserObject();
		Iterator i = RController.createContent(o, data).iterator();
		while (i.hasNext()) {
			RObject ro = (RObject) i.next();
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(ro);

			if (!ro.isAtomar())
				child.add(new DefaultMutableTreeNode());
			node.add(child);
		}
	}

	/**
	 * Refresh current objects.
	 * 
	 * @param c
	 *            collection of objects
	 */
	public void refresh(Collection c) {
		data = c;
		(root).removeAllChildren();
		objModel = new DataTreeModel(root);
		this.setModel(null);
		this.setModel(objModel);
	}

	public String getToolTipText(MouseEvent e) {
		if (objmgr.summary != null)
			objmgr.summary.hide();
		if (e.isAltDown()) {
			objmgr.setWorking(true);
			Point p = e.getPoint();
			JToolTip call = new JToolTip();
			RObject o = (RObject) ((DefaultMutableTreeNode) getUI()
					.getClosestPathForLocation(this, p.x, p.y)
					.getLastPathComponent()).getUserObject();
			String tip = RController.getSummary(o);
			if (tip != null) {
				objmgr.setWorking(false);
				return tip;
			}
			return null;
		}
		return null;
	}

	/**
	 * actionPerformed: handle action event: buttons.
	 */
	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		if (cmd.startsWith("saveData"))
			new JGRDataFileSaveDialog(objmgr, cmd.substring(9),
					JGRPrefs.workingDirectory);
	}

	/**
	 * Save selected object to a file.
	 */
	public void saveData() {
		new JGRDataFileSaveDialog(objmgr, selectedObject.getRName(),
				JGRPrefs.workingDirectory);
	}

	/**
	 * dragGestureRecognized: handle drag gesture event: transfer the name of
	 * dragged object.
	 */
	public void dragGestureRecognized(DragGestureEvent evt) {
		Point p = evt.getDragOrigin();
		RObject o = (RObject) ((DefaultMutableTreeNode) getUI()
				.getClosestPathForLocation(this, p.x, p.y)
				.getLastPathComponent()).getUserObject();

		Transferable t = new java.awt.datatransfer.StringSelection(o.getRName());
		if (t == null)
			return;
		dragSource.startDrag(evt, DragSource.DefaultCopyDrop, t, this);
	}

	/**
	 * dragEnter: handel drag source drag event.
	 */
	public void dragEnter(DragSourceDragEvent evt) {
		// Called when the user is dragging this drag source and enters
		// the drop target.
	}

	/**
	 * dragOver: handle drag source drag event.
	 */
	public void dragOver(DragSourceDragEvent evt) {
		// Called when the user is dragging this drag source and moves
		// over the drop target.
	}

	/**
	 * dragExit: handle drag source event.
	 */
	public void dragExit(DragSourceEvent evt) {
		// Called when the user is dragging this drag source and leaves
		// the drop target.
	}

	/**
	 * dropActionChanged: handle drag source drag event.
	 */
	public void dropActionChanged(DragSourceDragEvent evt) {
		// Called when the user changes the drag action between copy or move.
	}

	/**
	 * dragDropEnd: handle drag source drop event.
	 */
	public void dragDropEnd(DragSourceDropEvent evt) {
		// Called when the user finishes or cancels the drag operation.
	}

	/**
	 * keyTyped: handle key typed.
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * keyPressed: handle key event.
	 */
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * keyReleased: handle key event.
	 */
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE
				|| e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			TreePath[] sel = this.getSelectionPaths();
			for (int i = 0; i < sel.length; i++) {
				TreePath p = sel[i];
				try {
					if (((DefaultMutableTreeNode) p.getLastPathComponent())
							.getLevel() == 1) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) p
								.getPathComponent(1);
						((org.rosuda.REngine.JRI.JRIEngine)JGR.getREngine()).getRni().eval("rm(" + ((RObject) n.getUserObject()).getRName()	+ ")");
						objModel.removeNodeFromParent(n);
					}
				} catch (Exception ex) {
				}
			}
		}
	}

	/**
	 * mouseClicked: handle mouse event.
	 */
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		RObject o = null;
		try {
			o = (RObject) ((DefaultMutableTreeNode) getUI()
					.getClosestPathForLocation(this, p.x, p.y)
					.getLastPathComponent()).getUserObject();
			objmgr.savedata.setEnabled(o.isEditable());
			selectedObject = o;
		} catch (Exception ex) {
			objmgr.savedata.setEnabled(false);
			selectedObject = null;
		}
		if (e.getClickCount() == 2 && o != null) {
			org.rosuda.ibase.SVarSet vs = RController.newSet(o);
			if (vs != null && vs.count() != 0)
				new DataTable(vs, o.getType(), o.isEditable());
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
	 * mousePressed: handle mouse event: show summary of selected object.
	 */
	public void mousePressed(MouseEvent e) {
		if (objmgr.summary != null)
			objmgr.summary.hide();
		if (e.isPopupTrigger()) {
			objmgr.setWorking(true);
			Point p = e.getPoint();
			JToolTip call = new JToolTip();
			RObject o = (RObject) ((DefaultMutableTreeNode) getUI()
					.getClosestPathForLocation(this, p.x, p.y)
					.getLastPathComponent()).getUserObject();
			String tip = RController.getSummary(o);
			if (tip == null) {
				objmgr.setWorking(false);
				return;
			}
			call.setTipText(tip);
			SwingUtilities.convertPointToScreen(p, this);
			objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,
					call, p.x + 20, p.y + 25);
			objmgr.summary.show();
			objmgr.setWorking(false);
		}
	}

	/**
	 * mouseReleased: hanlde mouse event: show summary of selected object.
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			objmgr.setWorking(true);
			Point p = e.getPoint();
			JToolTip call = new JToolTip();
			RObject o = (RObject) ((DefaultMutableTreeNode) getUI()
					.getClosestPathForLocation(this, p.x, p.y)
					.getLastPathComponent()).getUserObject();
			String tip = RController.getSummary(o);
			if (tip == null) {
				objmgr.setWorking(false);
				return;
			}
			call.setTipText(tip);
			SwingUtilities.convertPointToScreen(p, this);
			objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,
					call, p.x + 20, p.y + 25);
			objmgr.summary.show();
			objmgr.setWorking(false);
		}
	}

	/**
	 * treeWillExpand: handle tree expansion event: add childs to object if
	 * there is a content.
	 */
	public void treeWillExpand(TreeExpansionEvent e) {
		TreePath p = e.getPath();
		if (!this.hasBeenExpanded(p)) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) p
					.getLastPathComponent();
			n.removeAllChildren();
			objmgr.setWorking(true);
			this.addNodes(n);
			objmgr.setWorking(false);
		}
	}

	/**
	 * treeWillCollapse: handle tree expansion event.
	 */
	public void treeWillCollapse(TreeExpansionEvent e) {
	}

	class DataTreeModel extends DefaultTreeModel {

		TreeNode root;

		public DataTreeModel(TreeNode node) {
			super(node);
			root = node;
			Iterator i = data.iterator();
			while (i.hasNext()) {
				RObject o = (RObject) i.next();
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(o);
				if (!o.isAtomar())
					child.add(new DefaultMutableTreeNode());
				((DefaultMutableTreeNode) root).add(child);
			}
		}
	}
}
