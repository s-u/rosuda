package org.rosuda.JGR.toolkit;

//
//  JGRListener.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.event.*;

import org.rosuda.JGR.*;

public class JGRListener implements ActionListener {

    public JGRListener() {
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        //JGR.RCSync.triggerNotification(cmd);
        JGR.MAINRCONSOLE.execute(cmd);
    }
}