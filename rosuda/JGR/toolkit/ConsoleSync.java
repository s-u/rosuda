package org.rosuda.JGR.toolkit;

/**
 *  TextFinder
 * 
 * 	take care about readconsole
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import org.rosuda.JGR.*;
import java.util.Vector;

public class ConsoleSync {
    Vector msgs;
    
    public ConsoleSync() {
        msgs=new Vector();
    }

    private boolean notificationArrived=false;

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
        String s=null;
        if (msgs.size()>0) {
            s=(String)msgs.elementAt(0);
            msgs.removeElementAt(0);
        }
        if (msgs.size()==0)
            notificationArrived=false;
        return s;
    }

    /** this methods awakens {@link #waitForNotification}. It is implemented by setting {@link #notificationArrived} to <code>true</code>, setting {@link #lastNotificationMessage} to the passed message and finally calling {@link notifyAll()}. */
    public synchronized void triggerNotification(String msg) {
        //System.out.println("lastmsg "+lastNotificationMessage);
        notificationArrived=true;
        msgs.addElement(msg);
        notifyAll();
    }
}
