//
//  GDContainer.java
//  Java Graphics Device
//
//  Created by Simon Urbanek on Thu Aug 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.javaGD;

import java.awt.*;

public interface GDContainer {
    public void add(GDObject o);
    public void reset();
    public GDState getGState();
    public Graphics getGraphics(); // implementation is free to return null
    //public void repaint();
    //public void repaint(long tm);
    public void syncDisplay(boolean finish); // true=batch finished; false=batch begins
    public void setDeviceNumber(int dn);
    public void closeDisplay();
    public int getDeviceNumber();
    public Dimension getSize();
}
