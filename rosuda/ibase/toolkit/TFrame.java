import java.awt.*;

/** enhanced {@link Frame} that uses {@link WinTracker} to keep track of
    open frames.
    @version $Id$
*/
public class TFrame extends Frame
{
    WTentry WTmyself;
    TFrame(String tit) {
	setTitle(tit);
	// add myself to WinTracker
	if (WinTracker.current==null) WinTracker.current=new WinTracker();
	WTmyself=new WTentry(this,tit);
	WinTracker.current.add(WTmyself);
    };

    TFrame() { this("<unnamed>"); };

    public void finalize() {
	if (Common.DEBUG>0)
	    System.out.println("Frame \""+getTitle()+"\" removed.");
	//WinTracker.current.rm(WTmyself);
    };
};
