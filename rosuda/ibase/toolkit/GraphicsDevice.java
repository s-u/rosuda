package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

/** GraphicsDevice handles graphics-, mouse-, keyboard- and layer interactivity on lowest level 
 * @version $Id$
 */

public interface GraphicsDevice {

	int AWTGrDevID = 0;
	int SWINGGrDevID = 1;
	int JOGLGrDevID = 2;
	
	void paintLayer(int layer);
	void repaint();
	
	// intersection with PlotComponent
	void setPCOwner(PlotComponent pc);
	int getGrDevID();
	
	// layer manipulation methods
	void setUpdateRoot(int root);
	
	// component methods
	Component getComponent();
	Rectangle getBounds();
	void setSize(int w, int h);
	Dimension getSize();
	void setBackground(Color c);
	Point getLocation();
	void setCursor(Cursor c);
	void setSize(Dimension d);
	int getWidth();
	int getHeight();
	Container getParent();
	
	void addMouseListener(MouseListener l);
	void addMouseMotionListener(MouseMotionListener l);
	void addKeyListener(KeyListener l);
}
