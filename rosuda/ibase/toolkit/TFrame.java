import java.awt.*;

/** enhanced {@link Frame} that uses {@link WinTracker} to keep track of
    open frames.
    @version $Id$
*/
public class TFrame extends Frame
{
    WTentry WTmyself;
    
    public TFrame(String tit, boolean useCommonBg) {
        if (useCommonBg) setBackground(Common.backgroundColor);
	setTitle(tit);
	// add myself to WinTracker
	if (WinTracker.current==null) WinTracker.current=new WinTracker();
	WTmyself=new WTentry(this,tit);
	WinTracker.current.add(WTmyself);
    };

    public TFrame(String tit) { this(tit,true); }
    
    public TFrame() { this("<unnamed>",true); }

    public void finalize() {
	if (Common.DEBUG>0)
	    System.out.println("Frame \""+getTitle()+"\" removed.");
	WinTracker.current.rm(WTmyself);
    };
};
