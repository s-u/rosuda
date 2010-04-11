package org.rosuda.deducer.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class RemoveButton extends IconButton implements ActionListener{
	JList rightList;
	JList leftList;
	
	public RemoveButton(String name,VariableSelector var,JList list){
		super("/icons/1leftarrow_32.png", name, null,name);
		this.addActionListener(this);
		leftList = var.getJList();
		rightList=list;
	}
	
	public RemoveButton(String name,JList left,JList right){
		super("/icons/1leftarrow_32.png", name, null,name);
		this.addActionListener(this);
		leftList = left;
		rightList=right;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object[] objs=rightList.getSelectedValues();
		for(int i=0;i<objs.length;i++){
			if(objs[i]!=null){
				((DefaultListModel)leftList.getModel()).addElement(objs[i]);
				((DefaultListModel)rightList.getModel()).removeElement(objs[i]);
			}
		}
		
	}

}
