package org.rosuda.deducer.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class AddButton extends IconButton implements ActionListener{

	JList leftList;
	JList rightList;
	public AddButton(String name,VariableSelector var,JList list){
		super("/icons/1rightarrow_32.png", name, null,name);
		this.addActionListener(this);
		rightList=list;
		leftList = var.getJList();
	}
	public AddButton(String name,JList left,JList right){
		super("/icons/1rightarrow_32.png", name, null,name);
		this.addActionListener(this);
		rightList=right;
		leftList = left;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		Object[] objs=leftList.getSelectedValues();
		for(int i=0;i<objs.length;i++){
			((DefaultListModel)leftList.getModel()).removeElement(objs[i]);
			if(objs[i]!=null)
				((DefaultListModel)rightList.getModel()).addElement(objs[i]);
		}
		
	}

}
