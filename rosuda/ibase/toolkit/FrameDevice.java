package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

public interface FrameDevice {

    public static final int clsMain     = 1;
    public static final int clsSplash   = 2;
    public static final int clsVars     = 8;
    public static final int clsHelp     = 16;
    public static final int clsPlot     = 128;
    public static final int clsTree     = 129;
    public static final int clsMCP      = 130;
    public static final int clsDevPlot  = 131;
    public static final int clsTreeMap  = 132;

    public static final int clsBar      = 135;
    public static final int clsHist     = 136;
    public static final int clsScatter  = 137;
    public static final int clsBox      = 138;
    public static final int clsPCP      = 139;
    public static final int clsLine     = 140;
    public static final int clsMap      = 141;
    public static final int clsFD       = 142;
    public static final int clsTable    = 143;
    public static final int clsCustom   = 144;
    
    public static final int clsEditor   = 150;
    public static final int clsAbout    = 151;
    public static final int clsPrefs    = 152;
    public static final int clsObjBrowser = 153;
    public static final int clsPackageUtil = 154;
    
    public static final int clsJavaGD   = 160;
    
    public static final int clsUser     = 8192;
	
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