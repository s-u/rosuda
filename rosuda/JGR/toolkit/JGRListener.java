package org.rosuda.JGR.toolkit;

/**
 *  JGRListener
 * 
 * 	execute commands from add menuitems at runtime
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */


import java.awt.event.*;

import org.rosuda.JGR.*;

public class JGRListener implements ActionListener {

    public JGRListener() {
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        JGR.MAINRCONSOLE.execute(cmd);
    }
}