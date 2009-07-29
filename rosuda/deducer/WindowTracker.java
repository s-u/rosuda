package org.rosuda.deducer;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.rosuda.deducer.data.DataFrameWindow;

public class WindowTracker{
	protected ArrayList activeWindows = new ArrayList();
	static WindowTracker tracker;
	public synchronized static void addWindow(JDialog d){
		if(tracker==null){
			tracker = new WindowTracker();
			tracker.activeWindows = new ArrayList();
		}
		tracker.activeWindows.add(d);
	}
	
	public synchronized static void addWindow(JFrame f){
		if(tracker==null){
			tracker = new WindowTracker();
			tracker.activeWindows = new ArrayList();
		}
		tracker.activeWindows.add(f);
	}
	
	public static void waitForAllClosed(){
    	while(tracker.activeWindows.size()>0){
    		try {
    			for(int i=0;i<tracker.activeWindows.size();i++){
    				Window win = (Window)tracker.activeWindows.get(i);
    				if(win==null || win.isDisplayable()==false){
    					tracker.activeWindows.remove(i);
    				}
    			}
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
    	}
	}


	
}
