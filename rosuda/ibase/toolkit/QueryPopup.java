package org.rosuda.ibase.toolkit;

import java.awt.*;


public interface QueryPopup {

	void setContent(String s);
	void setContent(String s, int cid);
	void setContent(String s, int[] cid);
	
	void setLocation(int x, int y);
	
	void show();
	void hide();
	
    Component getQueryComponent();

}
