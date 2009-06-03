package org.rosuda.deducer.toolkit;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.rosuda.JGR.util.ErrorMsg;

public class SingletonAddRemoveButton extends IconButton{
	private SingletonDJList singList;
	private String[] cmdText;
	private String[] tooltipText;
	private SingletonAddRemoveButton theButton=this;
	
	public SingletonAddRemoveButton(String[] tooltip, ActionListener al,
			String[] cmd,SingletonDJList lis){
		super("/icons/1rightarrow_32.png", tooltip[0], al,cmd[0]);
		singList=lis;
		tooltipText=tooltip;
		cmdText=cmd;
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
	
}
