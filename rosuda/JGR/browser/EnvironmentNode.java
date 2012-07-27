package org.rosuda.JGR.browser;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.browser.DefaultBrowserNode.PopupListener;
import org.rosuda.REngine.REXP;

public class EnvironmentNode extends DefaultBrowserNode {

	public EnvironmentNode(){}	
	
	public EnvironmentNode(BrowserNode par, String rObjectName, String rClass){
		parent = par;
		//parent.addChild(this);
		rName = rObjectName;
		cls = rClass;		
		icon = findIcon();
	}
	
	public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
		return new EnvironmentNode(parent,rName,rClass);
	}
	
	public boolean getAllowsChildren() {
		return true;
	}
	public boolean isLeaf() {
		return false;
	}

	public String getChildExecuteableRObjectName(BrowserNode child) {
		return getExecuteableRObjectName() + "$" + child.getRName();
	}

	
	class BlankCellRenderer implements TreeCellRenderer{
		JLabel lab = new JLabel();
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			return lab;
		}
		
	}
	
	public JPopupMenu getPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		ActionListener lis = new PopupListener();
		JMenuItem item = new JMenuItem ("Edit");
		item.addActionListener(lis);
		//menu.add( item );
		//menu.add(new JSeparator());
		item = new JMenuItem ("Print");
		item.addActionListener(lis);
		menu.add( item );
		item = new JMenuItem ("Summary");
		item.addActionListener(lis);
		//menu.add( item );
		item = new JMenuItem ("Plot");
		item.addActionListener(lis);
		//menu.add( item );
		//menu.add(new JSeparator());
		item = new JMenuItem ("Remove");
		item.addActionListener(lis);
		menu.add( item );
		
		return menu;
	}

	public void update(DefaultTreeModel mod) {
		if(!expanded)
			return;
		REXP rexp;
		try {
			rexp = JGR.idleEval("ls(envir="+getExecuteableRObjectName()+")");
			if(rexp==null)
				return;
			String[] objectNames = rexp.asStrings();
			String[] objectClasses = new String[]{};
			if(objectNames.length>0){
				rexp = JGR.idleEval("sapply(ls(envir="+getExecuteableRObjectName()+
						"),function(a)class(get(a,envir="+getExecuteableRObjectName()+"))[1])");
				if(rexp==null)
					return;
				objectClasses = rexp.asStrings();
			}
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
	

	public void editObject() {}

	public void removeChildObjectFromR(BrowserNode child) {
		JGR.MAINRCONSOLE.execute("rm(\""+child.getRName()+"\",envir="+this.getExecuteableRObjectName()+")");
	}
}
