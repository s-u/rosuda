package org.rosuda.JGR.menu;



import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.toolkit.DJList;
import java.awt.BorderLayout;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import javax.swing.WindowConstants;

import org.rosuda.JGR.*;
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
public class MergeData extends javax.swing.JFrame implements ActionListener {
	private JPanel varPanel1;
	private JPanel varPanel2;
	private JCheckBox key1;
	private JList pairedList;
	private JButton mergeButton;
	private JButton useSecondary;
	private JButton usePrimary;
	private JButton pairAllButton;
	private JButton removeButton1;
	private JButton unpairButton;
	private JButton cancelButton;
	private JButton jButton1;
	private JList mergeByList;
	private JButton useBoth;
	private JPanel mergeByPanel;
	private JPanel pairedPanel;
	private JCheckBox key2;
	private JCheckBox includeCheckBox2;
	private JCheckBox includeCheckBox1;
	private JList dataList2;
	private JList dataList1;
	private JButton pairButton;
	
	private static DefaultListModel mergeByListModel;
	private static DefaultListModel pairedListModel;
	private static DefaultComboBoxModel  dataList1Model;
	private static DefaultComboBoxModel  dataList2Model;
	private static String lastDataName1;
	private static String lastDataName2;
	private static String lastDataSetName;

	public MergeData(String newDataSetName,String dataName1,String dataName2) {
		super();
		initGUI(newDataSetName,dataName1,dataName2);
	}
	
	private void initGUI(String newDataSetName,String dataName1,String dataName2) {
		try {
			boolean findPairs=false;
			boolean sameData = dataName1.equals(lastDataName1) && dataName2.equals(lastDataName2);
			lastDataName1=dataName1;
			lastDataName2=dataName2;
			lastDataSetName=newDataSetName;
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				useBoth = new JButton();
				getContentPane().add(useBoth, new AnchorConstraint(599, 326, 633, 200, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				useBoth.setText("[Both]");
				useBoth.setPreferredSize(new java.awt.Dimension(86, 22));
				useBoth.addActionListener(this);
			}
			{
				useSecondary = new JButton();
				getContentPane().add(useSecondary, new AnchorConstraint(556, 326, 590, 264, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				useSecondary.setText("[2]");
				useSecondary.setPreferredSize(new java.awt.Dimension(42, 22));
				useSecondary.setFont(new java.awt.Font("Dialog",0,10));
				useSecondary.addActionListener(this);
			}
			{
				usePrimary = new JButton();
				getContentPane().add(usePrimary, new AnchorConstraint(556, 264, 590, 200, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				usePrimary.setText("[1]");
				usePrimary.setPreferredSize(new java.awt.Dimension(44, 22));
				usePrimary.setFont(new java.awt.Font("Dialog",0,10));
				usePrimary.addActionListener(this);
			}
			{
				pairAllButton = new JButton();
				getContentPane().add(pairAllButton, new AnchorConstraint(230, 566, 281, 436, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				pairAllButton.setText("Pair Match");
				pairAllButton.setPreferredSize(new java.awt.Dimension(89, 33));
				pairAllButton.setFont(new java.awt.Font("Dialog",0,10));
				pairAllButton.addActionListener(this);
			}
			{
				removeButton1 = new JButton();
				getContentPane().add(removeButton1, new AnchorConstraint(895, 326, 930, 200, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				removeButton1.setText("Remove");
				removeButton1.setPreferredSize(new java.awt.Dimension(86, 23));
				removeButton1.addActionListener(this);
			}
			{
				unpairButton = new JButton();
				getContentPane().add(unpairButton, new AnchorConstraint(722, 326, 755, 200, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				unpairButton.setText("Unpair");
				unpairButton.setPreferredSize(new java.awt.Dimension(86, 22));
				unpairButton.addActionListener(this);
			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton, new AnchorConstraint(904, 833, 956, 720, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				cancelButton.setPreferredSize(new java.awt.Dimension(77, 34));
				cancelButton.addActionListener(this);
			}
			{
				jButton1 = new JButton();
				getContentPane().add(jButton1, new AnchorConstraint(895, 971, 965, 861, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				jButton1.setText("Merge");
				jButton1.setPreferredSize(new java.awt.Dimension(75, 46));
				jButton1.addActionListener(this);
			}
			{
				mergeByPanel = new JPanel();
				BorderLayout mergeByPanelLayout = new BorderLayout();
				mergeByPanel.setLayout(mergeByPanelLayout);
				getContentPane().add(mergeByPanel, new AnchorConstraint(829, 675, 956, 335, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				mergeByPanel.setPreferredSize(new java.awt.Dimension(232, 83));
				mergeByPanel.setBorder(BorderFactory.createTitledBorder("Merge By Identifier"));
				{
					if(mergeByListModel==null || !sameData)
						mergeByListModel = new DefaultListModel();

					mergeByList = new MergeDJList();
					JScrollPane scrollPane = new JScrollPane(mergeByList,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);					
					mergeByPanel.add(scrollPane, BorderLayout.CENTER);
					mergeByList.setModel(mergeByListModel);
				}
			}
			{
				mergeButton = new JButton();
				getContentPane().add(mergeButton, new AnchorConstraint(763, 598, 809, 408, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				mergeButton.setText("Case Identifier");
				mergeButton.setPreferredSize(new java.awt.Dimension(130, 30));
				mergeButton.addActionListener(this);
			}
			{
				pairedPanel = new JPanel();
				BorderLayout pairedPanelLayout = new BorderLayout();
				pairedPanel.setLayout(pairedPanelLayout);
				getContentPane().add(pairedPanel, new AnchorConstraint(383, 675, 763, 335, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				pairedPanel.setPreferredSize(new java.awt.Dimension(232, 248));
				pairedPanel.setBorder(BorderFactory.createTitledBorder("Paired Variables"));
				{
					if(pairedListModel==null || !sameData)
						pairedListModel = new DefaultListModel();
					pairedList = new PairDJList();
					JScrollPane sPane = new JScrollPane(pairedList,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);					
					pairedPanel.add(sPane, BorderLayout.CENTER);
					pairedList.setModel(pairedListModel);
					pairedList.setPreferredSize(new java.awt.Dimension(222, 121));
				}
			}
			{
				key2 = new JCheckBox();
				getContentPane().add(key2, new AnchorConstraint(423, 971, 457, 707, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				key2.setText("Unique Identifiers");
				key2.setPreferredSize(new java.awt.Dimension(180, 22));
				key2.addActionListener(this);
			}
			{
				key1 = new JCheckBox();
				getContentPane().add(key1, new AnchorConstraint(423, 258, 457, 18, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				key1.setText("Unique Identifiers");
				key1.setPreferredSize(new java.awt.Dimension(164, 22));
				key1.addActionListener(this);
			}
			{
				includeCheckBox2 = new JCheckBox();
				getContentPane().add(includeCheckBox2, new AnchorConstraint(392, 971, 423, 707, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				includeCheckBox2.setText("Drop Unmatched Cases");
				includeCheckBox2.setPreferredSize(new java.awt.Dimension(180, 20));
				includeCheckBox2.addActionListener(this);
			}
			{
				includeCheckBox1 = new JCheckBox();
				getContentPane().add(includeCheckBox1, new AnchorConstraint(383, 326, 414, 18, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				includeCheckBox1.setText("Drop Unmatched Cases");
				includeCheckBox1.setPreferredSize(new java.awt.Dimension(210, 20));
				includeCheckBox1.addActionListener(this);
			}
			{
				pairButton = new JButton();
				getContentPane().add(pairButton, new AnchorConstraint(325, 566, 374, 436, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				pairButton.setText("Pair");
				pairButton.setPreferredSize(new java.awt.Dimension(89, 32));
				pairButton.addActionListener(this);
			}
			{
				varPanel1 = new JPanel();
				BorderLayout VarPanel1Layout = new BorderLayout();
				getContentPane().add(varPanel1, new AnchorConstraint(12, 427, 374, 12, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				varPanel1.setPreferredSize(new java.awt.Dimension(279, 232));
				varPanel1.setLayout(VarPanel1Layout);
				varPanel1.setBorder(BorderFactory.createTitledBorder("Primary Data: "+dataName1));
				{
					
					if(dataList1Model==null || !sameData){
						String[] data1Names = JGR.R.eval("colnames("+dataName1+")").asStringArray();
						dataList1Model = new DefaultComboBoxModel(data1Names);
						findPairs=true;
					}
					dataList1 = new JList();
					dataList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					JScrollPane scroll1 = new JScrollPane(dataList1,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);					
					varPanel1.add(scroll1, BorderLayout.CENTER);
					dataList1.setModel(dataList1Model);
				}
			}
			{
				varPanel2 = new JPanel();
				getContentPane().add(varPanel2, new AnchorConstraint(12, 20, 374, 584, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				BorderLayout jPanel1Layout = new BorderLayout();
				varPanel2.setPreferredSize(new java.awt.Dimension(264, 232));
				varPanel2.setBorder(BorderFactory.createTitledBorder("Secondary Data: "+dataName2));
				varPanel2.setLayout(jPanel1Layout);
				{
					if(dataList2Model==null || !sameData){
						String[] data2Names = JGR.R.eval("colnames("+dataName2+")").asStringArray();
						dataList2Model = new DefaultComboBoxModel(data2Names);
						findPairs=true;
					}
					dataList2 = new JList();
					JScrollPane scroll2 = new JScrollPane(dataList2,
                            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					varPanel2.add(scroll2, BorderLayout.CENTER);
					dataList2.setModel(dataList2Model);
					dataList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				}
			}
			if(findPairs)
				findPairs();
			pack();
			this.setSize(682, 675);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	
	public void findPairs(){
		for(int i=0;i<dataList1.getModel().getSize();i++){
			for(int j=0;j<dataList2.getModel().getSize();j++)
				if(dataList1.getModel().getElementAt(i).equals(dataList2.getModel().getElementAt(j))){
					((DefaultListModel)pairedList.getModel()).addElement("[1] "+dataList1.getModel().getElementAt(i));
					((DefaultComboBoxModel)dataList1.getModel()).removeElementAt(i);
					((DefaultComboBoxModel)dataList2.getModel()).removeElementAt(j);
					i--;
					break;
				}
		}
	}

	public void actionPerformed(ActionEvent act) {
		String cmd = act.getActionCommand();
		if(cmd =="Cancel"){
			this.dispose();
		}else if(cmd == "Case Identifier"){
			int[] ind = pairedList.getSelectedIndices();	
			for(int i=0;i<ind.length;i++)
				mergeByListModel.addElement(((String)
						pairedListModel.getElementAt(ind[i])).substring(4));
			for(int i= (ind.length-1);i>=0;i--)
				pairedListModel.removeElementAt(ind[i]);
			
		}else if(cmd == "Remove"){
				int[] ind = mergeByList.getSelectedIndices();
				for(int i= (ind.length-1);i>=0;i--){
					pairedListModel.addElement("[1] "+
							mergeByListModel.getElementAt(ind[i]));					
					mergeByListModel.removeElementAt(ind[i]);
				}

		}else if(cmd =="Unpair"){
			int[] ind = pairedList.getSelectedIndices();
			for(int i= (ind.length-1);i>=0;i--){
				String temp = (String) pairedListModel.getElementAt(ind[i]);
				if(temp.indexOf("<==>")==(-1)){
					((DefaultComboBoxModel)dataList2.getModel()).addElement(temp.substring(4));
					((DefaultComboBoxModel)dataList1.getModel()).addElement(temp.substring(4));
				}else{
					String[] t = temp.split("<==>");	
					((DefaultComboBoxModel)dataList2.getModel()).addElement(t[1]);
					((DefaultComboBoxModel)dataList1.getModel()).addElement(t[0].substring(4));
				}
				pairedListModel.removeElementAt(ind[i]);
			}				
		}else if(cmd == "Pair"){
			String var1 = (String)dataList1.getSelectedValue();
			String var2 = (String)dataList2.getSelectedValue();
			if(var1==null || var2==null)
				return;
			((DefaultComboBoxModel)dataList1.getModel()).removeElement(var1);
			((DefaultComboBoxModel)dataList2.getModel()).removeElement(var2);
			if(var1.equals(var2)){
				pairedListModel.addElement("[1] "+var1);
			}else
				pairedListModel.addElement("[1] "+var1+"<==>"+var2);
		}else if(cmd == "Pair Match"){
			findPairs();
		}else if(cmd == "[Both]"){
			String temporary;
			int[] ind = pairedList.getSelectedIndices();	
			for(int i=0;i<ind.length;i++){
				temporary = (String)pairedList.getModel().getElementAt(ind[i]);
				pairedListModel.removeElementAt(ind[i]);
				((DefaultListModel)pairedList.getModel()).add(ind[i], "[b] "+temporary.substring(4));
			}
			pairedList.setSelectedIndices(ind);
		}else if(cmd == "[1]"){
			String temporary;
			int[] ind = pairedList.getSelectedIndices();	
			for(int i=0;i<ind.length;i++){
				temporary = (String)pairedList.getModel().getElementAt(ind[i]);
				pairedListModel.removeElementAt(ind[i]);
				((DefaultListModel)pairedList.getModel()).add(ind[i], "[1] "+temporary.substring(4));
			}
			pairedList.setSelectedIndices(ind);
		}else if(cmd == "[2]"){
			String temporary;
			int[] ind = pairedList.getSelectedIndices();	
			for(int i=0;i<ind.length;i++){
				temporary = (String)pairedList.getModel().getElementAt(ind[i]);
				pairedListModel.removeElementAt(ind[i]);
				((DefaultListModel)pairedList.getModel()).add(ind[i], "[2] "+temporary.substring(4));
			}
			pairedList.setSelectedIndices(ind);
		}else if(cmd == "Merge"){
			merge();
			this.dispose();
			
		}
		
	}
	public void merge(){
		String temp;
		String byX="";
		String byY="";
		if(mergeByListModel.getSize()>0){
			byX+="c(";
			byY+="c(";
			for(int i=0;i<mergeByListModel.getSize();i++){
				temp = mergeByListModel.elementAt(i).toString();
				if(temp.indexOf("<==>")==(-1)){
					byX+="\""+temp+"\"";
					byY+="\""+temp+"\"";

				}else{
					String[] t = temp.split("<==>");	
					byX+="\""+t[0]+"\"";
					byY+="\""+t[1]+"\"";
				}
				if(i<mergeByListModel.getSize()-1){
					byX+=",";
					byY+=",";
				}				
			}
			byX+=")";
			byY+=")";			
		}else{
			byX="\"row.names\"";
			byY="\"row.names\"";
		}
		JGR.MAINRCONSOLE.executeLater(byX);
		JGR.MAINRCONSOLE.executeLater(byY);
		
		ArrayList excludeX = new ArrayList();
		ArrayList excludeY = new ArrayList();
		String[] varNames = {"",""};
		char code;
		int pairedSize = pairedListModel.getSize();
		for(int i=0;i<pairedSize;i++){
			temp = (String) pairedListModel.getElementAt(i);
			code = temp.charAt(1);
			temp = temp.substring(4);
			if(temp.indexOf("<==>")==(-1)){
				varNames[0]=temp;
				varNames[1]=temp;

			}else{
				varNames = temp.split("<==>");	
			}
			if(code=='1'){
				excludeY.add(varNames[1]);
			}else if(code=='2'){
				excludeX.add(varNames[0]);
			}
		}
		String temp1 = JGR.MAINRCONSOLE.getUniqueName(lastDataName1+".temp");
		String temp2 = JGR.MAINRCONSOLE.getUniqueName(lastDataName2+".temp");
		JGR.MAINRCONSOLE.executeLater(
				temp1+"<-"+lastDataName1+"[setdiff(colnames("+lastDataName1+"),"+
								makeRStringVector(excludeX)+")]"	
		);
		JGR.MAINRCONSOLE.executeLater(
				temp2+"<-"+lastDataName2+"[setdiff(colnames("+lastDataName2+"),"+
								makeRStringVector(excludeY)+")]"	
		);
		JGR.MAINRCONSOLE.executeLater(
				lastDataSetName+"<-merge("+temp1+","+temp2+",by.x="+byX+",by.y="+byY+",incomparables = NA"+
						",all.x =" + (!includeCheckBox1.getModel().isSelected()?"T":"F")+
						",all.y =" + (!includeCheckBox2.getModel().isSelected()?"T":"F")+
						")"
		);
		JGR.MAINRCONSOLE.executeLater(
				"rm(list=c(\""+temp1+"\",\""+temp2+"\"))"
		);
	}
	
	public String makeRStringVector(ArrayList lis){
		if(lis.size()==0)
			return "c()";
		String result = "c(";
		for(int i=0;i<lis.size();i++){
			result+="\""+lis.get(i).toString()+"\"";
			if(i<lis.size()-1)
				result+=",";
		}
		result+=")";
		return result;
	}

	private class PairDJList extends DJList{
		public void drop(DropTargetDropEvent dtde) {
			super.drop(dtde);
			int len = this.getModel().getSize();
			String temporary;
			for(int i=0;i<len;i++){
				temporary = (String)this.getModel().getElementAt(i);
				if(!temporary.startsWith("[")){
					((DefaultListModel)this.getModel()).removeElementAt(i);
					((DefaultListModel)this.getModel()).add(i, "[1] "+temporary);
				}
			}
		}
		
	}
	private class MergeDJList extends DJList{
		public void drop(DropTargetDropEvent dtde) {
			super.drop(dtde);
			int len = this.getModel().getSize();
			String temporary;
			for(int i=0;i<len;i++){
				temporary = (String)this.getModel().getElementAt(i);
				if(temporary.startsWith("[")){
					((DefaultListModel)this.getModel()).removeElementAt(i);
					((DefaultListModel)this.getModel()).add(i, temporary.substring(4));
				}
			}
		}
		
	}
}
