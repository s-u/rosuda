package org.rosuda.JGR.toolkit;
//
//  ConsoleSync.java
//  JGR
//
//  Created by Markus Helbig on Wed Apr 14 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import org.rosuda.JGR.*;

public class ConsoleSync {

    public ConsoleSync() {
    }

    private boolean notificationArrived=false;
    private String lastNotificationMessage; // only synchronized methods are allowed to use this

    /** this internal method waits until {@link #triggerNotification} is called by another thread. It is implemented by using {@link wait()} and checking {@link notificationArrived}. */
    public synchronized String waitForNotification() {
        while (!notificationArrived) {
            try {
                //wait();
                wait(100);
                JGR.R.rniIdle();
            } catch (InterruptedException e) {
            }
        }
        notificationArrived=false;
        String s=lastNotificationMessage;
        //System.out.println("rSync "+s);
        lastNotificationMessage=null; // reset lastNM
        //System.out.println("msg cleared");
        return s;
    }

    /** this methods awakens {@link #waitForNotification}. It is implemented by setting {@link #notificationArrived} to <code>true</code>, setting {@link #lastNotificationMessage} to the passed message and finally calling {@link notifyAll()}. */
    public synchronized void triggerNotification(String msg) {
        //System.out.println("lastmsg "+lastNotificationMessage);
        notificationArrived=true;
        lastNotificationMessage=msg;
        //System.out.println("msg set");
        notifyAll();
    }
}
