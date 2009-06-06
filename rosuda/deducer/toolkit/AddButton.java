package org.rosuda.deducer.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;

public class AddButton extends IconButton implements ActionListener{
	VariableSelector varSel;
	DJList lis;
	public AddButton(String name,VariableSelector var,DJList list){
		super("/icons/1rightarrow_32.png", name, null,name);
		this.addActionListener(this);
		varSel=var;
		lis=list;
	}
	public void actionPerformed(ActionEvent arg0) {
		Object[] objs=varSel.getJList().getSelectedValues();
		for(int i=0;i<objs.length;i++){
			varSel.remove(objs[i]);
			((DefaultListModel)lis.getModel()).addElement(objs[i]);
		}
		
	}

}
