package org.rosuda.ibase.toolkit;
import java.awt.*;
import java.awt.event.*;
import net.java.games.jogl.*;

public interface PlotComponent {
	
	int AWT = 0;
	int SWING = 1;
	int OPENGL = 2;
	
	Component getComponent();
	/* FIXME: the two methods below destroy the independence and should be removed */
	void initializeLayerCanvas(LayerCanvas l);
	void initializeGraphics(Window w);
	
	int getGraphicsEngine();
	
	// we redirect only necessary methods to the component
	void repaint();
	void setCursor(Cursor cursor);
	void setBackground(Color c);
	void addMouseMotionListener(MouseMotionListener l);
	void addMouseListener(MouseListener l);
	void addKeyListener(KeyListener l);
	Rectangle getBounds();
	Point getLocation();
	void setSize(int width, int height);
	void setSize(Dimension d);
	Dimension getSize();
	Image createImage(int width, int height);
	Color getForeground();
	Color getBackground();
	/* this should be removed! a PC doesn't have to be Graphics based (e.g. OpenGL is not) */
	Graphics getGraphics();
	Container getParent();
	Window getParentWindow();
	int getWidth();
	int getHeight();
	
	// additions to SWING
	void setPreferredSize(Dimension d);
	void setMinimumSize(Dimension d);
	void setMaximumSize(Dimension d);
	void setToolTipText(String s);
	
	// additions to JOGL:
	public void addGLEventListener(GLEventListener l);

}
