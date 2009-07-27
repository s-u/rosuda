package org.rosuda.deducer;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rosuda.deducer.data.DataFrameWindow;

public class WindowTracker implements WindowListener{

	public static int numRDependentWindows;
	static WindowTracker tracker;
	public static synchronized void addWindow(JDialog d){
		if(tracker==null){
			tracker = new WindowTracker();
			numRDependentWindows=0;
		}
		numRDependentWindows++;
		d.addWindowListener(tracker);
	}
	
	public static synchronized void addWindow(JFrame f){
		if(tracker==null){
			tracker = new WindowTracker();
			numRDependentWindows=0;
		}
		numRDependentWindows++;
		f.addWindowListener(tracker);
	}
	
	public static void waitForAllClosed(){
    	while(numRDependentWindows>0 ||DataFrameWindow.dataWindows.size()>0){
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
    	}
	}

	public void windowActivated(WindowEvent arg0) {}


	public synchronized void windowClosed(WindowEvent e) {
		if(numRDependentWindows>0)
			numRDependentWindows--;
	}

	public void windowClosing(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

	
}
