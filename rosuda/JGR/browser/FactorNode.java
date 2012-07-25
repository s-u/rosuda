package org.rosuda.JGR.browser;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.browser.DefaultBrowserNode.PopupListener;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.DataTable;

public class FactorNode extends DefaultBrowserNode{
	public FactorNode(){}
	
	public FactorNode(BrowserNode parent, String rName, String rClass) {
		super(parent,rName,rClass);
	}

	public BrowserNode generate(BrowserNode parent, String rName, String rClass) {
		return new FactorNode(parent,rName,rClass);
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
		menu.add( item );
		item = new JMenuItem ("Plot");
		item.addActionListener(lis);
		menu.add( item );
		menu.add(new JSeparator());
		item = new JMenuItem ("Remove");
		item.addActionListener(lis);
		menu.add( item );
		
		return menu;
	}
	
	public void editObject(){
		RObject o = new RObject(this.getExecuteableRObjectName(), cls, true);
		org.rosuda.ibase.SVarSet vs = RController.newSet(o);
		if (vs != null && vs.count() != 0)
			new DataTable(vs, o.getType(), o.isEditable());
	}
	
	public void plotObject(){
		JGR.MAINRCONSOLE.execute("barchart("+getExecuteableRObjectName()+")");
	}

}
