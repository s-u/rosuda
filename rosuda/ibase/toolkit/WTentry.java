import java.awt.*;
import java.util.*;

// what we ought to add to WT are among others:
// - associated variables
// - class (other that just using java tools)

/** a {@link WinTracker} entry
    @version $Id$ 
*/
class WTentry extends Object
{
    public Window w;
    public String name;
    WTentry(Window win) { w=win; }
    WTentry(Window win,String nam) { this(win); name=nam; };
};
