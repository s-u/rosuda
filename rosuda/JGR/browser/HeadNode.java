package org.rosuda.JGR.browser;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.rosuda.JGR.JGR;

public class HeadNode implements BrowserNode {

	ArrayList children = new ArrayList();
	
	protected boolean expanded = true; 
	
	TreeCellRenderer renderer = new BlankCellRenderer();
	
	public HeadNode(){}
	
	public Enumeration children() {
		return Collections.enumeration(children);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) children.get(childIndex);
	}

	public int getChildCount() {
		return children.size();
	}

	public int getIndex(TreeNode node) {
		for(int i=0;i<children.size();i++)
			if(node==children.get(i))
				return i;
		return -1;
	}

	public TreeNode getParent() {
		return null;
	}

	public boolean isLeaf() {
		return false;
	}

	public String getExecuteableRObjectName() {
		return "globalenv()";
	}

	public String getChildExecuteableRObjectName(BrowserNode child) {
		return child.getRName();
	}

	public TreeCellRenderer getRenderer() {
		return renderer;
	}
	
	class BlankCellRenderer implements TreeCellRenderer{
		JLabel lab = new JLabel();
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			return lab;
		}
		
	}

	public String getRName() {
		return null;
	}

	public void addChild(BrowserNode node) {
		children.add(node);
	}

	public void insert(MutableTreeNode child, int index) {
		children.add(index, child);
	}

	public void remove(int index) {
		children.remove(index);
	}

	public void remove(MutableTreeNode node) {
		children.remove(this.getIndex(node));
	}

	public void removeFromParent() {
		
	}

	public void setParent(MutableTreeNode arg0) {
		
	}

	public void setUserObject(Object arg0) {

	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public void setExpanded(boolean expand){
		expanded=expand;
	}

	public void update(DefaultTreeModel mod) {
		if(!expanded)
			return;
		try {
			String[] objectNames = JGR.eval("ls()").asStrings();
			
			String[] objectClasses = new String[]{};
			if(objectNames.length>0)
				objectClasses = JGR.eval("sapply(ls(),function(a)class(get(a,envir=globalenv()))[1])").asStrings();
			if(objectNames.length<children.size())
				for(int i=children.size()-1;i>=objectNames.length;i--){
					//System.out.println("remove 1");
					mod.removeNodeFromParent((MutableTreeNode) children.get(i));
				}

			for(int i=0;i<objectNames.length;i++){
				if(i<objectClasses.length){
					BrowserNode node = BrowserController.createNode(this, objectNames[i], objectClasses[i]);
					if(children.size()>i && children.get(i).equals(node)){
						((BrowserNode)children.get(i)).update(mod);
					}else{
						//if(children.size()>i)
						//	System.out.println(children.get(i).equals(node));
						Object[] tmp = children.toArray();
						if(tmp.length>i){
							for(int ind=i;ind<tmp.length;ind++){
								//System.out.println("remove 2");
								mod.removeNodeFromParent((MutableTreeNode) tmp[ind]);
							}
						}
						//System.out.println("HeadNode add"+node.getRName());
						mod.insertNodeInto(node, this, children.size());
						node.update(mod);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	public boolean equals(Object obj){
		return this == obj;
	}

	public JPopupMenu getPopupMenu() {
		return null;
	}

	public void editObject() {}

	public void removeChildObjectFromR(BrowserNode child) {
		JGR.MAINRCONSOLE.execute("rm(\""+child.getRName()+"\",envir="+this.getExecuteableRObjectName()+")");
	}

}
