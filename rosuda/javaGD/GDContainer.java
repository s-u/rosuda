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
    public boolean prepareLocator(LocatorSync ls); // this method is called to notify the contained that a locator request is pending; the container must either return @code{false} and ignore the ls parameter *or* return @code{true} and call @link{LocatorSync.triggerAction} method at some point in the future (which may well be after returning from this method)
    public void syncDisplay(boolean finish); // true=batch finished; false=batch begins
    public void setDeviceNumber(int dn);
    public void closeDisplay();
    public int getDeviceNumber();
    public Dimension getSize();
}
