package org.rosuda.JGR.toolkit;

/**
*  ObjectBrowserTree
 *
 * 	loads childs dynamically
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.Point;
import java.util.*;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.event.*;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;

import org.rosuda.JGR.*;
import org.rosuda.JGR.robjects.RObject;

import sun.security.x509.RDN;

public class ObjectBrowserTree extends JTree implements ActionListener, MouseListener, DragGestureListener, DragSourceListener, TreeWillExpandListener {

    private Collection data;
    private JGRObjectManager objmgr;
    private DragSource dragSource;
    private DataTreeModel objModel;
    private DefaultMutableTreeNode root;
    private RObject selectedObject = null;
    
    private String type;

    public ObjectBrowserTree(JGRObjectManager parent, Collection c, String name) {
        this.data = c;
        this.objmgr = parent;
        this.type = name;

        if (FontTracker.current == null)
            FontTracker.current = new FontTracker();
        FontTracker.current.add(this);

        root = new DefaultMutableTreeNode(name);
        objModel = new DataTreeModel(root);
        this.setModel(objModel);

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);

        this.setToggleClickCount(100);
        this.addMouseListener(this);
        this.addTreeWillExpandListener(this);
    }

    public void addNodes(DefaultMutableTreeNode node) {
        RObject o = (RObject) node.getUserObject();
        Iterator i = RController.createContent(o,data).iterator();
        while (i.hasNext()) {
            RObject ro = (RObject) i.next();
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(ro);
            if (!ro.isAtomar())
                child.add(new DefaultMutableTreeNode());
            node.add(child);
        }
    }

    public void refresh(Collection c) {
        this.data = c;
        ((DefaultMutableTreeNode)root).removeAllChildren();
        objModel = new DataTreeModel(root);
        this.setModel(null);
        this.setModel(objModel);
    }
    
    private void popUpSaveMenu(MouseEvent e, RObject o) {
        JPopupMenu menue = new JPopupMenu();
        JMenuItem saveData = new JMenuItem();
        JMenuItem titleItem = new JMenuItem();
        titleItem.setEnabled(false);
        
        saveData.setToolTipText("Save Data");
        saveData.setActionCommand("saveData-"+o.getRName()+"");
        saveData.setText("Save Data");
        saveData.addActionListener(this);
        
        menue.add(titleItem);
        menue.add(saveData);
        menue.show(e.getComponent(), e.getX(), e.getY());
    }
    
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if (cmd.startsWith("saveData")) new JGRDataFileSaveDialog(objmgr,cmd.substring(9), JGRConsole.directory);
    }
    
    public void saveData() {
        new JGRDataFileSaveDialog(objmgr,selectedObject.getRName(), JGRConsole.directory);
    }
    

    public void dragGestureRecognized(DragGestureEvent evt) {
        Point p = evt.getDragOrigin();
        RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();

        Transferable t = new java.awt.datatransfer.StringSelection(o.getRName());
        dragSource.startDrag (evt, DragSource.DefaultCopyDrop, t, this);
    }
    public void dragEnter(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and enters
        // the drop target.
    }
    public void dragOver(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and moves
        // over the drop target.
    }
    public void dragExit(DragSourceEvent evt) {
        // Called when the user is dragging this drag source and leaves
        // the drop target.
    }
    public void dropActionChanged(DragSourceDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void dragDropEnd(DragSourceDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    }

    public void mouseClicked(MouseEvent e) {
            Point p = e.getPoint();
            RObject o = null;
            try {
                o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();
                objmgr.savedata.setEnabled(o.isEditable());
                selectedObject = o;
            }
            catch (Exception ex) {
                objmgr.savedata.setEnabled(false);
                selectedObject = null;
            }
            if (e.getClickCount()==2 && o != null) {
                org.rosuda.ibase.SVarSet vs = RController.newSet(o);
                if (vs != null && vs.count() != 0) new DataTable(vs,o.getType(),o.isEditable());
            }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (objmgr.summary != null)	objmgr.summary.hide();
        if (e.isPopupTrigger()) {
            objmgr.cursorWait();
            Point p = e.getPoint();
            JToolTip call  = new JToolTip();
            RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();
            /*if (type == "data" && ((org.rosuda.JGR.toolkit.JGRPrefs.isMac && e.isMetaDown()) || (!org.rosuda.JGR.toolkit.JGRPrefs.isMac && e.isControlDown()))) {
                popUpSaveMenu(e,o);
                objmgr.cursorDefault();
            }
            else {*/
	            String tip = RController.getSummary(o);
	            if (tip==null) {
	                objmgr.cursorDefault();
	                return;
	            }
	            call.setTipText(tip);
	            SwingUtilities.convertPointToScreen(p,this);
	            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,call,p.x+20,p.y+25);
	            objmgr.summary.show();
	            objmgr.cursorDefault();
            //}
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            objmgr.cursorWait();
            Point p = e.getPoint();
            JToolTip call  = new JToolTip();
            RObject o = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();
            /*if (type == "data" && ((org.rosuda.JGR.toolkit.JGRPrefs.isMac && e.isMetaDown()) || (!org.rosuda.JGR.toolkit.JGRPrefs.isMac && e.isControlDown()))) {
                popUpSaveMenu(e,o);
                objmgr.cursorDefault();
            }
            else {*/
	            String tip = RController.getSummary(o);
	            if (tip==null) {
	                objmgr.cursorDefault();
	                return;
	            }
	            call.setTipText(tip);
	            SwingUtilities.convertPointToScreen(p,this);
	            objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,call,p.x+20,p.y+25);
	            objmgr.summary.show();
	            objmgr.cursorDefault();
            //}
        }
    }

    public void treeWillExpand(TreeExpansionEvent e) {
        TreePath p = e.getPath();
        if (!this.hasBeenExpanded(p)) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode)p.getLastPathComponent();
            n.removeAllChildren();
            objmgr.cursorWait();
            this.addNodes(n);
            objmgr.cursorDefault();
        }
    }

    public void treeWillCollapse(TreeExpansionEvent e) {
    }


    class DataTreeModel extends DefaultTreeModel{

        TreeNode root;

        public DataTreeModel(TreeNode node) {
            super(node);
            this.root = node;
            Iterator i = data.iterator();
            while(i.hasNext()) {
                RObject o = (RObject) i.next();
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(o);
                if (!o.isAtomar())
                    child.add(new DefaultMutableTreeNode());
                ((DefaultMutableTreeNode) root).add(child);
            }
        }
    }

}
