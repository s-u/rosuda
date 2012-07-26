package org.rosuda.JGR.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRConsole;
import org.rosuda.JGR.editor.Editor;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

public class DefaultBrowserNode implements BrowserNode, BrowserNodeFactory{

	protected String cls;
	protected String rName;
	protected BrowserNode parent;
	protected boolean isList = false;
	volatile protected ArrayList children = new ArrayList();
	protected boolean expanded;	
	protected ImageIcon icon;
	
	TreeCellRenderer renderer = new DefaultBrowserCellRenderer();

	
	public DefaultBrowserNode(){}
	
	public DefaultBrowserNode(BrowserNode par, String rObjectName, String rClass){
		parent = par;
		//parent.addChild(this);
		rName = rObjectName;
		cls = rClass;		
		icon = findIcon();
	}
	
	protected ImageIcon findIcon(){
		URL url = getClass().getResource("/icons/tree_"+cls+".png");
		Image img = null;
		ImageIcon ic = null;
		try {
			img = ImageIO.read(url);
			ic = new ImageIcon(img);
		} catch (Exception e) {
			url = getClass().getResource("/icons/tree_default.png");
			img = null;
			try {
				img = ImageIO.read(url);
				ic = new ImageIcon(img);
			} catch (Exception ex) {
				ex.printStackTrace();
			}			
		}	
		return ic;
	}

	public Enumeration children() {
		return Collections.enumeration(children);
	}

	public boolean getAllowsChildren() {
		return isList;
	}

	public TreeNode getChildAt(int i) {
		return (TreeNode) children.get(i);
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
		return parent;
	}

	public boolean isLeaf() {
		return !isList;
	}
	
	public String getRName(){
		return rName;
	}

	public String getExecuteableRObjectName() {
		if(parent==null)
			return rName;
		return parent.getChildExecuteableRObjectName(this);
	}

	public String getChildExecuteableRObjectName(BrowserNode child) {
		//if(child.getRName()!=null)
		//	return getExecuteableRObjectName() + "[[\"" + child.getRName() + "\"]]";
		//else{
			return getExecuteableRObjectName() + "[[" + (this.getIndex(child)+1) + "]]";
		//}
	}

	public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
		return new DefaultBrowserNode(parent,rName,rClass);
	}

	public TreeCellRenderer getRenderer() {
		return renderer;
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
		parent.remove(this);
		parent=null;
	}

	public void setParent(MutableTreeNode arg0) {
		parent=(BrowserNode) arg0;
	}

	public void setUserObject(Object arg0) {

	}
	
	public boolean isExpanded(){
		return expanded;
	}
	
	public void setExpanded(boolean expand){
		expanded=expand;
	}
	
	synchronized public void update(DefaultTreeModel mod) {
		REXP rexp;
		String fullName = parent.getChildExecuteableRObjectName(this);
		//System.out.println(fullName);
		try {
			rexp = JGR.idleEval("is.list(" + fullName + ")");
			if(rexp==null)
				return;
			isList = ((REXPLogical)rexp).isTRUE()[0];
			if(!expanded){
				this.children.clear();		
				return;
			}
			if(isList){
				rexp = JGR.idleEval("length(" + fullName + ")>0");
				if(rexp==null)
					return;
				boolean hasChildren = ((REXPLogical)rexp).isTRUE()[0];			
				if(!hasChildren){
					Object[] tmp = children.toArray();
					for(int i=0;i<tmp.length;i++)
						mod.removeNodeFromParent((MutableTreeNode) tmp[i]);
					return;
				}
				REXP nrexp = JGR.idleEval("names(" + fullName + ")");
				if(nrexp==null)
					return;
				rexp = JGR.idleEval("sapply(" + fullName + ",function(a)class(a)[1])");
				if(rexp==null)
					return;
				String[] objectClasses = rexp.asStrings();
				String[] names;
				boolean[] isNA;
				if(nrexp==null || nrexp.isNull()){
					names = new String[objectClasses.length];
					isNA = new boolean[objectClasses.length];
					for(int i=0;i<objectClasses.length;i++){
						names[i]=null;
						isNA[i]=true;
					}
				}else{
					names = nrexp.asStrings();
					isNA = nrexp.isNA();
				}
				if(names.length<children.size())
					for(int i=children.size()-1;i>=names.length;i--)
						mod.removeNodeFromParent((MutableTreeNode) children.get(i));
				for(int i=0;i<names.length;i++){
					BrowserNode node = BrowserController.createNode(this,isNA[i] ? null : names[i],
							objectClasses[i]);
					if(children.size()>i && children.get(i).equals(node)){
						((BrowserNode)children.get(i)).update(mod);
					}else{
						//System.out.println("DefaultBrowserNode add: " + node.getRName());
						Object[] tmp = children.toArray();
						if(tmp.length>i){
							for(int ind=i;ind<tmp.length;ind++)
								mod.removeNodeFromParent((MutableTreeNode) tmp[ind]);
						}
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
		if(!(obj instanceof DefaultBrowserNode))
			return false;
		DefaultBrowserNode tmp = (DefaultBrowserNode) obj;
		return (cls==null?tmp.cls==null:cls.equals(tmp.cls)) &&  (rName==null?tmp.rName==null:rName.equals(tmp.rName));
	}
	
	class DefaultBrowserCellRenderer extends DefaultTreeCellRenderer{
		
		
		public DefaultBrowserCellRenderer(){
			super();		
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(
                    tree, value, selected,
                    expanded, leaf, row,
                    hasFocus);
			if(rName!=null)
				this.setText(rName);
			else
				this.setText("" + (parent.getIndex(DefaultBrowserNode.this) + 1));
			this.setIcon(icon);
			this.setVerticalAlignment(JLabel.CENTER);
			this.setVerticalTextPosition(JLabel.CENTER);
			this.setSize(new Dimension(26,tree.getWidth()));
			this.setToolTipText(cls);
			return this;
		}
	}

	public JPopupMenu getPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		ActionListener lis = new PopupListener();
		JMenuItem item = new JMenuItem ("Edit");
		item.addActionListener(lis);
		menu.add( item );
		menu.add(new JSeparator());
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

	class PopupListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			final String cmd = arg0.getActionCommand();
			new Thread(new Runnable(){

				public void run() {
					runCmd(cmd);
				}
				
			}).start();
		}
		
		public void runCmd(String cmd){
			if(cmd.equals("Edit")){
				editObject();
			}else if(cmd.equals("Print")){
				printObject();
			}else if(cmd.equals("Summary")){
				summaryObject();
			}else if(cmd.equals("Plot")){
				plotObject();
			}else if(cmd.equals("Remove")){
				parent.removeChildObjectFromR(DefaultBrowserNode.this);
			}
		}
		
	}
	
	public void editObject(){
		try {
			REXP x = JGR.eval("suppressWarnings(try(paste(capture.output(dput("+ 
					this.getExecuteableRObjectName()+")),collapse=\"\n\"),silent=TRUE))");
			if(x!=null){
				StringBuffer sb = new StringBuffer();
				sb.append(this.getExecuteableRObjectName() + "<-");
				sb.append(x.asString());
				Editor ed = new Editor();
				ed.setText(sb);
			}
		} catch (REngineException e) {
		} catch (REXPMismatchException e) {
		}
	}
	
	public void printObject(){
		JGR.MAINRCONSOLE.execute("print("+getExecuteableRObjectName()+")");
	}
	
	public void summaryObject(){
		JGR.MAINRCONSOLE.execute("summary("+getExecuteableRObjectName()+")");
	}
	
	public void plotObject(){
		JGR.MAINRCONSOLE.execute("plot("+getExecuteableRObjectName()+")");
	}
	
	public void removeChildObjectFromR(BrowserNode node){
		JGR.MAINRCONSOLE.execute(node.getExecuteableRObjectName() + " <- NULL");
	}
	
}
