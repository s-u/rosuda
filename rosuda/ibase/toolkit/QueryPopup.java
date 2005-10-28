package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.SVarSet;

public interface QueryPopup {

	public abstract void setContent(PlotComponent pc, String s);
	public abstract void setContent(PlotComponent pc, String s, int cid);
	public abstract void setContent(PlotComponent pc, String s, int[] cid);
	
	public abstract void setLocation(int x, int y);
	
	public abstract void show();
	public abstract void hide();
	
    public Component getQueryComponent();
    
//    public Window getOwnerWindow();
    
//    public Component getOwnerComponent();
	
}
