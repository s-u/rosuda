package org.rosuda.JGR.toolkit;

/**
 *  ProgressLabel
 * 
 * 	represents working status
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import javax.swing.*;

public class ProgressLabel extends JLabel implements Runnable {

    private String whatToDo = "Working";
    private String temp;
    private boolean next = false;
    private Thread thread;

    public ProgressLabel() {
    	this.setFont(new Font("Dialog",Font.BOLD,12));
        setMinimumSize(new Dimension(90, 15));
        setPreferredSize(new Dimension(90, 15));
        //setMaximumSize(new Dimension(90, 15));
        setVisible(false);
    }


    public void start(String str) {
        thread = new Thread(this);
        if (this.isVisible()) {
            temp = str;
            next = true;
        }
        else this.whatToDo = str;
        this.setVisible(true);
        thread.start();
        //run();
    }

    public void run() {
        this.setText(whatToDo);
        try {
            while (true) {
                this.setText(whatToDo+" .");
                Thread.sleep(500);
                this.setText(whatToDo+" . .");
                Thread.sleep(500);
                this.setText(whatToDo+" . . .");
                Thread.sleep(500);
            }
        } catch (Exception e) {

        }
    }

    public void stop() {
        this.setVisible(false);
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        if (next) {
            next = false;
            start(temp);
        }
    }
}