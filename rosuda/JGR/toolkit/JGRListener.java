package org.rosuda.JGR.toolkit;

import java.awt.event.*;

import org.rosuda.JGR.*;

/**
 *  JGRListener - an actionlistener listening menuitems which the user added to the console on-the-fly.
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

public class JGRListener implements ActionListener {

    
    /**
     * actionPerformed: handle action event: on-the-fly added menuitems.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        JGR.MAINRCONSOLE.execute(cmd);
    }
}