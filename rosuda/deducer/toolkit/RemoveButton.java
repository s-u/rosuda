package org.rosuda.deducer.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;

public class RemoveButton extends IconButton implements ActionListener{
	VariableSelector varSel;
	DJList lis;
	public RemoveButton(String name,VariableSelector var,DJList list){
		super("/icons/1leftarrow_32.png", name, null,name);
		this.addActionListener(this);
		varSel=var;
		lis=list;
	}
	public void actionPerformed(ActionEvent arg0) {
		Object[] objs=lis.getSelectedValues();
		for(int i=0;i<objs.length;i++){
			varSel.add(objs[i]);
			((DefaultListModel)lis.getModel()).removeElement(objs[i]);
		}
		
	}

}
