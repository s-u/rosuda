//
//  GDContainer.java
//  Java Graphics Device
//
//  Created by Simon Urbanek on Thu Aug 05 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation;
//  version 2.1 of the License.
//  
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
