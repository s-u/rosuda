//
//  RespDialog.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 07 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import java.awt.*;
import java.awt.event.*;

public class RespDialog extends Dialog implements ActionListener {
    public boolean cancel=false;
    public String  lastAction;

    Panel centerPanel;
    
    public static final String okCancel[] = { "OK", "Cancel" };
    
    public RespDialog(Frame owner, String title, boolean modal, String[] buttons) {
        super(owner, title, modal);
        setBackground(Color.white);
        setLayout(new BorderLayout());
        add(new SpacingPanel(),BorderLayout.WEST);
        add(new SpacingPanel(),BorderLayout.EAST);
        Panel bp=new Panel();
        bp.setLayout(new FlowLayout());
        int i=0;
        while (i<buttons.length) {
            Button b;
            bp.add(b=new Button(buttons[i]));
            b.addActionListener(this);
            i++;
        }
        add(bp,BorderLayout.SOUTH);
        add(new Label(" "),BorderLayout.NORTH);
        centerPanel=new Panel(); centerPanel.setLayout(new FlowLayout());
        add(centerPanel);
    }
    
    public Panel getContentPanel() {
        return centerPanel;
    }
    
    /** activated if a button was pressed. It determines whether "cancel" was pressed or OK" */
    public void actionPerformed(ActionEvent e) {
        lastAction=e.getActionCommand();
        cancel=lastAction.equals("Cancel");
        setVisible(false);
    }   
}
