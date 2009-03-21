
package org.rosuda.JGR.toolkit;


import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.rosuda.JGR.*;
import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.util.*;



/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class VariableSelector extends JPanel implements ActionListener, KeyListener {
	private JComboBox dataComboBox;
	private DefaultComboBoxModel dataComboBoxModel;
	private JList variableList;
	private JTextField filter;
	private JLabel filterText;
	
	public VariableSelector() {
		super();
		initGUI();
		RController.refreshObjects();
		dataComboBoxModel.removeAllElements();
		for(int i=0;i<JGR.DATA.size();i++){
			dataComboBoxModel.addElement(((RObject) JGR.DATA.elementAt(i)).getName());
		}

		String dataName = (String)dataComboBox.getSelectedItem();
		variableList.setModel(new FilteringModel(JGR.R.eval("names("+dataName+")").asStringArray()));

	}
	
	private void initGUI() {
		try {
			this.setName("Variable Selector");
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(164, 290));
			dataComboBoxModel = 
				new DefaultComboBoxModel();
			dataComboBox = new JComboBox();
			dataComboBox.setModel(dataComboBoxModel);
			dataComboBox.addActionListener(this);
			
			filterText = new JLabel();
			filterText.setText("  Filter:");
			
			
			filter = new JTextField();
			this.add(filter, new AnchorConstraint(98, 966, 170, 338, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			this.add(filterText, new AnchorConstraint(108, 338, 160, 3, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			this.add(dataComboBox, new AnchorConstraint(1, 1003, 77, 3, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			dataComboBox.setPreferredSize(new java.awt.Dimension(164, 22));
			filterText.setPreferredSize(new java.awt.Dimension(55, 15));
			filter.setPreferredSize(new java.awt.Dimension(103, 21));
			filter.addKeyListener(this);

			ListModel variableListModel = new FilteringModel(new String[] { "Item One", "Item Two","A","b","lalala la","the" });
			variableList = new JList();
			variableList.setModel(variableListModel);
			JScrollPane listScroller = new JScrollPane(variableList,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			this.add(listScroller, new AnchorConstraint(191, 1003, 1001, 3, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
			listScroller.setPreferredSize(new java.awt.Dimension(164, 235));

		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	


	public void add(Object variable){
		((FilteringModel) variableList.getModel()).addElement(variable);
	}
	
	public void remove(Object variable){
		((FilteringModel) variableList.getModel()).removeElement(variable);
	}
	
	public JList getJList(){
		return variableList;
	}
	public JComboBox getJComboBox(){
		return dataComboBox;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		//JGR.R.eval("print('"+arg0.toString()+"')");
		if(cmd=="comboBoxChanged"){
			String dataName = (String)dataComboBox.getSelectedItem();
			variableList.setModel(new FilteringModel(JGR.R.eval("names("+dataName+")").asStringArray()));
		}
		
	}

	public void keyTyped(KeyEvent arg0) {
		Runnable doWorkRunnable = new Runnable() {
		    public void run() { ((FilteringModel) variableList.getModel()).filter(filter.getText()); }
		};
		SwingUtilities.invokeLater(doWorkRunnable);
		
	}
	private class FilteringModel extends AbstractListModel{
		List list;
		List filteredList;
		String lastFilter = "";

		public FilteringModel() {
			list = new ArrayList();
			filteredList = new ArrayList();
		}
		public FilteringModel(String[] items){
			list = new ArrayList();
			filteredList = new ArrayList();
			for(int i=0;i<items.length;i++){
				list.add(items[i]);
				filteredList.add(items[i]);
			}
		}

		public void addElement(Object element) {
			list.add(element);
			filter(lastFilter);
		}
		public void removeElement(Object element) {
			list.remove(element);
			filter(lastFilter);
		}
		public Object remove(int index) {
			Object obj =list.remove(index);
			filter(lastFilter);
			return obj;
		}

		public int getSize() {
			return filteredList.size();
		}

		public Object getElementAt(int index) {
			Object returnValue;
			if (index < filteredList.size()) {
				returnValue = filteredList.get(index);
			} else {
				returnValue = null;
			}
			return returnValue;
		}

		void filter(String search) {
			filteredList.clear();
			Object element;
			for (int i=0;i<list.size();i++) {
				element = list.get(i);
				if (element.toString().toLowerCase().indexOf(search.toLowerCase(), 0) != -1) {
					filteredList.add(element);
				}
			}
			lastFilter=search;
			fireContentsChanged(this, 0, getSize());
		}
	}
	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

}

