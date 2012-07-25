package org.rosuda.JGR.browser;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;



public class BrowserTree extends JTree {

	HeadNode head;
	DefaultTreeModel mod;
	Refresher ref;
	public BrowserTree(){
		super();
		setModel(mod=new DefaultTreeModel(head=new HeadNode()));
		//this.setEditable(true);
		this.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.putClientProperty("JTree.lineStyle", "None");
		this.setCellRenderer(new BrowserCellRenderer());
		head.update(mod);
		this.repaint();
		ExpandListener lis = new ExpandListener();
		this.addTreeWillExpandListener(lis);
		this.addTreeExpansionListener(lis);
		TreeMouseListener mlis = new TreeMouseListener();
		this.addMouseListener(mlis);
		//this.setRootVisible(false);
		this.setToggleClickCount(1000);
	}
	
	public void startRefresher(){
		if(ref!=null)
			return;
		ref = new Refresher(mod);
		new Thread(ref).start();
	}
	
	public void stopRefresher(){
		if(ref!=null){
			ref.stopRunning();
			ref=null;
		}
	}
	
	
	class BrowserCellRenderer implements TreeCellRenderer{

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			BrowserNode node = (BrowserNode) value;
			return node.getRenderer().getTreeCellRendererComponent(
					tree,value,selected,expanded,leaf,row,hasFocus);
		}
		
	}

	
	class ExpandListener implements TreeWillExpandListener, TreeExpansionListener{

		public void treeWillCollapse(TreeExpansionEvent event)
				throws ExpandVetoException {
		}

		public void treeWillExpand(TreeExpansionEvent event)
				throws ExpandVetoException {
			BrowserNode node = (BrowserNode) event.getPath().getLastPathComponent();
			node.setExpanded(true);
			node.update(mod);
			mod.reload(node);
		}

		public void treeCollapsed(TreeExpansionEvent event) {
			//System.out.println("collapsing");
			BrowserNode node = (BrowserNode) event.getPath().getLastPathComponent();
			node.setExpanded(false);
			node.update(mod);
			mod.reload(node);
		}

		public void treeExpanded(TreeExpansionEvent event) {}
		
	}
	
	class TreeMouseListener implements MouseListener{

		public void mouseClicked(MouseEvent e) {
		    if (SwingUtilities.isRightMouseButton(e)) {

		        int row = BrowserTree.this.getClosestRowForLocation(e.getX(), e.getY());
		        BrowserTree.this.setSelectionRow(row);
		        JPopupMenu popupMenu= ((BrowserNode)BrowserTree.this.getSelectionPath().getLastPathComponent()).getPopupMenu();
		        if(popupMenu!=null)
		        	popupMenu.show(e.getComponent(), e.getX(), e.getY());
		    }else if(e.getClickCount() == 2){
		        int row = BrowserTree.this.getClosestRowForLocation(e.getX(), e.getY());
		        BrowserTree.this.setSelectionRow(row);
		        ((BrowserNode)BrowserTree.this.getSelectionPath().getLastPathComponent()).editObject();
		    }
		}

		public void mouseEntered(MouseEvent arg0) {}

		public void mouseExited(MouseEvent arg0) {}

		public void mousePressed(MouseEvent arg0) {}

		public void mouseReleased(MouseEvent arg0) {}
		
	}
	
}


class Refresher implements Runnable{
	public boolean keepRunning = true;
	DefaultTreeModel model;
	public Refresher(DefaultTreeModel mod){
		model=mod;
	}
	
	public void stopRunning(){
		keepRunning = false;
	}
	
	public void run(){
		while(keepRunning){
			//System.out.println("refresh");
			try {	
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				keepRunning=false;
			}
			((BrowserNode)model.getRoot()).update(model);
		}
	}
	

}


