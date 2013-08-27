package org.rosuda.JGR.browser;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public interface BrowserNode extends MutableTreeNode{

	public String getExecuteableRObjectName();
	
	public String getChildExecuteableRObjectName(BrowserNode child);
	
	public TreeCellRenderer getRenderer();
	
	public String getRName();
	
	public void addChild(BrowserNode child);
	
	public void update(DefaultTreeModel mod);
	
	public boolean isExpanded();
	
	public void setExpanded(boolean expand);
	
	public boolean equals(Object obj);
	
	public void editObject();
	
	public JPopupMenu getPopupMenu();
	
	public void removeChildObjectFromR(BrowserNode child);
	
	public void setShowSep(boolean show);
	
}
