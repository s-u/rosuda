package org.rosuda.JGR.toolkit;

//
//  IconButton.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class IconButton extends JButton implements MouseListener {

   /** create a button whith icon
     * @param iconUrl url to icon
     * @param tooltip Tooltip
     * @param al ActionListener
     * @param cmd ActionCommand*/
    public IconButton(String iconUrl, String tooltip, ActionListener al, String cmd) {
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource(iconUrl));
            this.setIcon(icon);
            this.setMinimumSize(new Dimension(26,26));
            this.setPreferredSize(new Dimension(26,26));
            this.setMaximumSize(new Dimension(26,26));
        }
        catch (Exception e) {
            this.setText(tooltip);
        }
        this.setActionCommand(cmd);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setToolTipText(tooltip);
        this.addActionListener(al);
        this.addMouseListener(this);
    }
        
    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        this.setBorder(BorderFactory.createEtchedBorder());
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}