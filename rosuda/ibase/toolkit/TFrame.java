package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

/** enhanced {@link Frame} that uses {@link WinTracker} to keep track of
    open frames.
    @version $Id$
*/
public class TFrame extends Frame
{
    WTentry WTmyself;

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

    public static final int clsUser     = 8192;
    
    public TFrame(String tit, boolean useCommonBg, int wclass) {
        if (useCommonBg) setBackground(Common.backgroundColor);
	setTitle(tit);
	// add myself to WinTracker
	if (WinTracker.current==null) WinTracker.current=new WinTracker();
	WTmyself=new WTentry(this,tit,wclass);
	WinTracker.current.add(WTmyself);
        if (Common.cur_arrow!=null) setCursor(Common.cur_arrow);
    };

    //public TFrame(String tit) { this(tit,true,0); }
    public TFrame(String tit,int wclass) { this(tit,true,wclass); }
    
    public TFrame() { this("<unnamed>",true,0); }

    public void finalize() {
	if (Global.DEBUG>0)
	    System.out.println("Frame \""+getTitle()+"\" removed.");
	WinTracker.current.rm(WTmyself);
    };

    static int lastClass=-1;
    static int lastPlaceX=0, lastPlaceY=0;
    static int lastOffset=0;
    
    public void initPlacement() { // initial frame placement
	if (WTmyself==null) return;
        if (lastClass!=WTmyself.wclass) {
            lastClass=WTmyself.wclass;
            lastPlaceX=getWidth()+10; lastPlaceY=0; lastOffset=0;
        } else {
            setLocation(lastPlaceX,lastPlaceY);
            lastPlaceX+=getWidth()+10;
            Common.getScreenRes();
            if (lastPlaceX+100>Common.screenRes.width) {
                lastPlaceY+=getHeight()+20;
                lastPlaceX=0;
                if (lastPlaceY+100>Common.screenRes.height) {
                    lastOffset+=30;
                    lastPlaceY=lastOffset; lastPlaceX=lastOffset;
                }
            }
        }
    };
};
