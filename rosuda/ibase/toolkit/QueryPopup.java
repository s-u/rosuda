package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

public interface QueryPopup {

	public abstract void setContent(String s);
	public abstract void setContent(String s, int cid);
	public abstract void setContent(String s, int[] cid);
	
	public abstract void setLocation(int x, int y);
	
	public abstract void show();
	public abstract void hide();
	
    public Component getQueryComponent();

}
