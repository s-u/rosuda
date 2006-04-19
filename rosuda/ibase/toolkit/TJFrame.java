package org.rosuda.ibase.toolkit;
import java.awt.*;
import javax.swing.*;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

/** enhanced {@link JFrame} that uses {@link WinTracker} to keep track of
    open frames.
    @version $Id$
*/
public class TJFrame extends JFrame implements FrameDevice
{
    WTentry WTmyself;

    public TJFrame(final String tit, final boolean useCommonBg, final int wclass) {
        if (useCommonBg) setBackground(Common.backgroundColor);
	setTitle(tit);
	
	// add myself to WinTracker
	if (WinTracker.current==null) WinTracker.current=new WinTracker();
	WTmyself=new WTentryAWT(WinTracker.current,this,tit,wclass);
        if (Common.cur_arrow!=null) setCursor(Common.cur_arrow);
    };

    //public TFrame(String tit) { this(tit,true,0); }
    public TJFrame(final String tit,final int wclass) { this(tit,true,wclass); }
    
    public TJFrame() { this("<unnamed>",true,0); }

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
    
    public Frame getFrame() {
    	return this;
    }
    
    public Component add(Component c) {
    	return getContentPane().add(c);
    }
    
    // other FrameDevice methods are redirected to superclass
    
};
