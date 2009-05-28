package org.rosuda.deducer.menu;

import java.awt.BorderLayout;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.toolkit.DJList;
import org.rosuda.deducer.toolkit.VariableSelector;
import org.rosuda.deducer.toolkit.IconButton;
import org.rosuda.JGR.util.ErrorMsg;


public class SortDialog extends javax.swing.JDialog implements ActionListener{
	private VariableSelector variableSelector;
	private JButton cancel;
	private JButton okay;
	private IconButton remove;
	private IconButton Add;
	private IconButton increasing;
	private IconButton decreasing;
	private DJList sortList;
	private JScrollPane sortScroller;
	private JPanel sortPanel;
	
	private static String lastDataName;
	private static DefaultListModel lastListModel;

	
	public SortDialog(JFrame frame) {
		super(frame);
		initGUI();
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				cancel = new JButton();
				getContentPane().add(cancel, new AnchorConstraint(867, 800, 937, 651, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancel.setText("Cancel");
				cancel.setPreferredSize(new java.awt.Dimension(78, 22));
				cancel.addActionListener(this);
			}
			{
				okay = new JButton();
				getContentPane().add(okay, new AnchorConstraint(829, 978, 963, 823, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okay.setText("OK");
				okay.setPreferredSize(new java.awt.Dimension(81, 42));
				okay.addActionListener(this);
			}
			{
				remove = new IconButton("/icons/1leftarrow_32.png","Remove",this,"Remove");
				getContentPane().add(remove, new AnchorConstraint(397, 550, 535, 470, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				remove.setPreferredSize(new java.awt.Dimension(40, 41));
			}
			{
				Add = new IconButton("/icons/1rightarrow_32.png","Add",this,"Add");
				getContentPane().add(Add, new AnchorConstraint(250, 550, 390, 470, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				Add.setPreferredSize(new java.awt.Dimension(40, 41));
			}
			{
				increasing = new IconButton("/icons/sort_az_32.png","Increasing: Descending",this,"Increasing");
				getContentPane().add(increasing, new AnchorConstraint(615, 865, 755, 785, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				increasing.setPreferredSize(new java.awt.Dimension(40, 41));
			}
			{
				decreasing = new IconButton("/icons/sort_za_32.png","Decreasing: Ascending",this,"Decreasing");
				getContentPane().add(decreasing, new AnchorConstraint(615, 780, 755, 700, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				decreasing.setPreferredSize(new java.awt.Dimension(40, 41));
			}
			{
				sortPanel = new JPanel();
				BorderLayout sortPanelLayout = new BorderLayout();
				getContentPane().add(sortPanel, new AnchorConstraint(39, 978, 600, 579, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				sortPanel.setPreferredSize(new java.awt.Dimension(209, 230));
				sortPanel.setLayout(sortPanelLayout);
				sortPanel.setBorder(BorderFactory.createTitledBorder("Sort data by:"));
				{
					sortScroller = new JScrollPane();
					sortPanel.add(sortScroller, BorderLayout.CENTER);
					{
						ListModel sortListModel= new DefaultListModel();
						sortList = new SortDJList();
						sortScroller.setViewportView(sortList);
						sortList.setModel(sortListModel);
					}
				}
			}
			{
				variableSelector = new VariableSelector();
				getContentPane().add(variableSelector, new AnchorConstraint(39, 434, 963, 23, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableSelector.setPreferredSize(new java.awt.Dimension(215, 289));
				variableSelector.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
				variableSelector.getJComboBox().addActionListener(this);
				variableSelector.setDropStringSplitter(" -- ");
			}
			if(lastDataName!=null)
				variableSelector.setSelectedData(lastDataName);
			if(lastListModel!=null && lastDataName!=null){
				sortList.setModel(lastListModel);
			}
			boolean allExist=variableSelector.removeAll((DefaultListModel) sortList.getModel());
			if(!allExist)
				sortList.setModel(new DefaultListModel());
			this.setTitle("Sort Data Frame");
			this.setSize(524, 335);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	
	public void setDataName(String dataName){
		if(!dataName.equals(variableSelector.getSelectedData()))
			variableSelector.setSelectedData(dataName);
	}
	
	public void actionPerformed(ActionEvent event) {
		
		String cmd = event.getActionCommand();
		if(cmd == "comboBoxChanged"){
			sortList.setModel(new DefaultListModel());
		}else if(cmd=="Cancel")
			this.dispose();
		else if(cmd == "Add"){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.remove(objs[i]);
				((DefaultListModel)sortList.getModel()).addElement(((String)objs[i])+" -- "+"Increasing");
			}
		}else if(cmd == "Remove"){
			Object[] objs=sortList.getSelectedValues();
			for(int i=0;i<objs.length;i++){
				variableSelector.add(objs[i]);
				((DefaultListModel)sortList.getModel()).removeElement(objs[i]);
			}			
		}else if(cmd == "Increasing"){
			int[] inds = sortList.getSelectedIndices();
			if(inds.length==0){
				JOptionPane.showMessageDialog(this, "Please select a variable in the above list.");	
				return;
			}
			DefaultListModel listModel = (DefaultListModel)sortList.getModel();
			for(int i=0;i<inds.length;i++){
				listModel.set(inds[i], ((String)listModel.get(inds[i])).substring(0, 
						((String)listModel.get(inds[i])).indexOf(" -- "))+" -- Increasing");
			}
		}else if(cmd == "Decreasing"){
			int[] inds = sortList.getSelectedIndices();
			if(inds.length==0){
				JOptionPane.showMessageDialog(this, "Please select a variable in the above list.");	
				return;
			}
			DefaultListModel listModel = (DefaultListModel)sortList.getModel();
			for(int i=0;i<inds.length;i++){
				listModel.set(inds[i], ((String)listModel.get(inds[i])).substring(0, 
						((String)listModel.get(inds[i])).indexOf(" -- "))+" -- Decreasing");
			}			
		}else if(cmd == "OK"){
			if(sortList.getModel().getSize()==0){
				JOptionPane.showMessageDialog(this, "Please select some variables to\nrun frequencies on.");
				return;
			}
			String dataName = variableSelector.getSelectedData();
			Object[] vars =sortList.getSelectedValues();
			ArrayList varList = new ArrayList();
			String[] temp;
			String sortingDirections = "c(";
			for(int i=0;i<sortList.getModel().getSize();i++){
				temp = ((String)sortList.getModel().getElementAt(i)).split(" -- ");
				varList.add(temp[0]);
				if(temp[1].startsWith("I"))
					sortingDirections+="TRUE";
				else
					sortingDirections+="FALSE";
				if(i<sortList.getModel().getSize()-1)
					sortingDirections+=",";
			}
			sortingDirections+=")";
			this.dispose();
			JGR.MAINRCONSOLE.toFront();			
			JGR.MAINRCONSOLE.executeLater(dataName+"<- sortData("+dataName+
					","+RController.makeRStringVector(varList)+",increasing= "+
					sortingDirections+")");
			lastDataName=dataName;
			lastListModel = (DefaultListModel) sortList.getModel();

		}
		
	}
	
	
	private class SortDJList extends DJList{
		public void drop(DropTargetDropEvent dtde) {
			super.drop(dtde);
			int len = this.getModel().getSize();
			String temporary;
			for(int i=0;i<len;i++){
				temporary = (String)this.getModel().getElementAt(i);
				if(temporary.indexOf(" -- ")<0){
					((DefaultListModel)this.getModel()).removeElementAt(i);
					((DefaultListModel)this.getModel()).add(i, temporary+" -- Increasing");
				}
			}
		}
		
	}
}
