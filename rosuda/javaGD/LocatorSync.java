package org.rosuda.javaGD;

public class LocatorSync {
    private double[] locResult=null;
    private boolean notificationArrived=false;

    /** this internal method waits until {@link #triggerNotification} is called by another thread. It is implemented by using {@link wait()} and checking {@link notificationArrived}. */
    public synchronized double[] waitForAction() {
        while (!notificationArrived) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
	notificationArrived=false;
	return locResult;
    }

    /** this methods awakens {@link #waitForNotification}. It is implemented by setting {@link #notificationArrived} to <code>true</code>, setting {@link #lastNotificationMessage} to the passed message and finally calling {@link notifyAll()}. */
    public synchronized void triggerAction(double[] result) {
	locResult=result;
        notificationArrived=true;
        notifyAll();
    }
}
