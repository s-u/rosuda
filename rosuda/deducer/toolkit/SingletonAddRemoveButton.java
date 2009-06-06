package org.rosuda.deducer.toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.rosuda.JGR.util.ErrorMsg;

public class SingletonAddRemoveButton extends IconButton implements ActionListener{
	private SingletonDJList singList;
	private String[] cmdText;
	private String[] tooltipText;
	private SingletonAddRemoveButton theButton=this;
	private VariableSelector variableSelector;
	public SingletonAddRemoveButton(String[] tooltip, ActionListener al,
			String[] cmd,SingletonDJList lis){
		super("/icons/1rightarrow_32.png", tooltip[0], al,cmd[0]);
		singList=lis;
		tooltipText=tooltip;
		cmdText=cmd;
		new Thread(new Refresher()).start();
	}
	
	public SingletonAddRemoveButton(String[] tooltip,String[] cmd,SingletonDJList lis,VariableSelector var){
		super("/icons/1rightarrow_32.png", tooltip[0], null,cmd[0]);
		this.addActionListener(this);
		singList=lis;
		tooltipText=tooltip;
		cmdText=cmd;
		variableSelector = var;
		new Thread(new Refresher()).start();
	}
	
	
	class Refresher implements Runnable {
		public Refresher() {
		}

		public void run() {
			while (true)
				try {
					Thread.sleep(500);
					Runnable doWorkRunnable = new Runnable() {
						public void run() { 
							if(singList.getModel().getSize()>0){
								theButton.setToolTipText(tooltipText[1]);
								theButton.setActionCommand(cmdText[1]);
								ImageIcon icon = new ImageIcon(getClass().getResource("/icons/1leftarrow_32.png"));
								theButton.setIcon(icon);
							}else{
								theButton.setToolTipText(tooltipText[0]);
								theButton.setActionCommand(cmdText[0]);
								ImageIcon icon = new ImageIcon(getClass().getResource("/icons/1rightarrow_32.png"));
								theButton.setIcon(icon);
							}
						}};
					SwingUtilities.invokeLater(doWorkRunnable);
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}


	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals(cmdText[0])){
			Object[] objs=variableSelector.getJList().getSelectedValues();
			if(objs.length>1){
				variableSelector.getJList().setSelectedIndex(variableSelector.getJList().getSelectedIndex());
			}else if(objs.length==1 && singList.getModel().getSize()==0){
					variableSelector.remove(objs[0]);
					((DefaultListModel)singList.getModel()).addElement(objs[0]);
			}
		}else{
			DefaultListModel tmpModel =(DefaultListModel)singList.getModel();
			if(tmpModel.getSize()>0){
				variableSelector.add(tmpModel.remove(0));	
			}
		}
		
	}
	
}
