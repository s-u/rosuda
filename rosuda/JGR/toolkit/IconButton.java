package org.rosuda.JGR.toolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *  IconButton - special button with an icon for JGR toolbars.
 *  
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2005
 */


public class IconButton extends JButton implements MouseListener {

   /** 
    * Create a button whith icon on it.
    * @param iconUrl url to icon
    * @param tooltip Tooltip
    * @param al ActionListener
    * @param cmd ActionCommand
    */
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
    
    /**
     * mouseClicked: handle mouse event.
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * mouseEntered: handle mouse event: show border when mouse is over this icon.
     */
    public void mouseEntered(MouseEvent e) {
        this.setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * mousePressed: handle mouse event.
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * mouseReleased: handle mouse event.
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * mouseExited: handle mouse event: border should now disappear when mouse isn't over icon.
     */
    public void mouseExited(MouseEvent e) {
        this.setBorder(BorderFactory.createEmptyBorder());
    }
}