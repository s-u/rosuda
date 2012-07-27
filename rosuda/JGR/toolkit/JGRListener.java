package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.rosuda.JGR.JGR;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * JGRListener - an actionlistener listening menuitems which the user added to
 * the console on-the-fly.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2004
 */

public class JGRListener implements ActionListener {
	boolean silent = true;
	public JGRListener(boolean silent){
		super();
		this.silent = silent;
	}
	/**
	 * actionPerformed: handle action event: on-the-fly added menuitems.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(silent){
			try {
				JGR.threadedEval(cmd);
			} catch (Exception e1) {
				e1.printStackTrace();
			} 
		}else{
			JGR.MAINRCONSOLE.execute(cmd, true);
		}
	}
}