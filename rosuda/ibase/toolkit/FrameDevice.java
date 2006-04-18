package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

public interface FrameDevice {

	void initPlacement();
	Frame getFrame();
	
	// these ones are forwarded to superclasses in T(J)Frame
	void setVisible(boolean b);
	void addWindowListener(WindowListener l);
	void setSize(Dimension d);
	void pack();
	
	// inconsistence problem between adding components in AWT and SWING
	Component add(Component c);
}