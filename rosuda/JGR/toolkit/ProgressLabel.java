package org.rosuda.JGR.toolkit;

//
//  ProgressIcon.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 22 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;


import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.JGR.*;




public class ProgressLabel extends JLabel implements Runnable {

    private String whatToDo = "Working";
    private String temp;
    private boolean next = false;
    private Thread thread;

    public ProgressLabel() {
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
    }

    public void run() {
        this.setText(whatToDo);
        try {
            while (true) {
                this.setText(whatToDo+" .");
                thread.sleep(500);
                this.setText(whatToDo+" . .");
                thread.sleep(500);
                this.setText(whatToDo+" . . .");
                thread.sleep(500);
            }
        } catch (Exception e) {

        }
    }

    public void stop() {
        this.setVisible(false);
        if (thread != null) thread.stop();
        if (next) {
            next = false;
            start(temp);
        }
    }
}