import java.awt.*;
import java.awt.event.*;

/** Common constants and gereral static methods for the entire application
    @version $Id$
*/
public class Common
{
    /** application version */
    public static String Version="0.96p";
    /** application release */
    public static String Release="CC13";

    /** Debug flag. When set to >0 verbose debug messages are generated.
        parameter equivalent: --debug / --nodebug */
    public static int DEBUG=0;
    /** profiler flag. When set to >0 profile information (timings) are generated; >1 then memory info is added
        parameter equivalent: --profile */
    public static int PROFILE=0;
    /** Frame of the main window. Used by close-window-handler
     *  in {@link DefWinL} for exiting application if this window is closed. */
    public static Frame mainFrame=null;
    /** Default window listener used for handling global tasks 
     *  (like closing windows etc.). Any window is free to use it. */
    public static WindowListener defaultWindowListener=null;

    /** use Swing classes */
    public static boolean useSwing=true;
    /** use Aqua-style background
        parameter equivalent: --with-aqua */
    public static boolean useAquaBg=false;
    /** if <code>true</code> then special messages for a loader are printed
        parameter equivalent: --with-loader */
    public static boolean informLoader=false;
    /** determines whether R-serve should be started if it's not running yet */
    public static boolean startRserv=false;

    /** AppType contstant: stand-alone application */    
    public static final int AT_standalone = 0x0000;
    /** AppType contstant: applet (set by applet wrapper) */    
    public static final int AT_applet     = 0x0001;
    /** AppType contstant: Klimt called by Omegahat SJava interface */    
    public static final int AT_KOH        = 0x0020;
    /** AppType contstant: launched by interactive framework interface */    
    public static final int AT_Framework  = 0x0030;
    
    /** application type. so far 0=stand-alone, other types are set by wrappers. See AT_xxx */
    public static int AppType=AT_standalone;   
    
    /** buffer containing all warnings/errors */
    static StringBuffer warnings=null;
    /** number of warnings so far */
    static int warningsCount=0;
    /** max. # of warnings, any further will be dropped (0=no limit)  */
    static int maxWarnings=64;
    /** screen resolution as obtained from the toolkit.  */
    public static Dimension screenRes=null;
    /** common background color. TFrame uses this as default */
    public static Color backgroundColor=new Color(255,255,192);
    /** common query popup background color */
    public static Color popupColor=new Color(245,255,255);
    /** common background color when aqua-style background is used */
    public static Color aquaBgColor=new Color(230,230,240);
    /** common selection color (so far only BaseCanvas-based plots use this) */
    //public static Color selectColor=new Color(255,0,0);
    public static Color selectColor=new Color(128,255,128);
    /** if <code>true</code> no internal variables are created */
    public static boolean noIntVar=false;


    /** Notify-Message constant: SMarker state changed */
    public static final int NM_MarkerChange     =0x100;

    /** Notify-Message constant: Axis changed */
    public static final int NM_AxisChange       =0x200;
    /** Notify-Message constant: geometry part of an Axis changed */
    public static final int NM_AxisGeometryChange=0x201;
    /** Notify-Message constant: value/data part of an Axis changed */
    public static final int NM_AxisDataChange   =0x0202;
    
    /** Notify-Message constant: SVar changed */
    public static final int NM_VarChange        =0x300;
    /** Notify-Message constant: SVar changed: content of a variable changed */
    public static final int NM_VarContentChange =0x301;
    /** Notify-Message constant: SVar changed: type (cat/num) changed */
    public static final int NM_VarTypeChange    =0x302;

    /** Notify-Message constant: SVarSet changed (e.g. # of vars...) */
    public static final int NM_VarSetChange     =0x400;
    /** Notify-Message constant: current node changed */
    public static final int NM_NodeChange       =0x500;

    /** mask to apply in order to get the top-level event */
    public static final int NM_MASK             =0xf00;

    /** Cursor: arrow (all Common.cur_xxx variables are set by Platform class upon init) */
    public static Cursor cur_arrow;
    /** Cursor: query (usually arrow with a question mark) */
    public static Cursor cur_query;
    /** Cursor: tick hint/cue (usually arrow with a separator resize symbol) */
    public static Cursor cur_tick;
    /** Cursor: hand (either pointing or dragging - not specified yet - for general use) */
    public static Cursor cur_hand;
    /** Cursor: zoom (usually magnifying glass; should NOT contain + or -) */
    public static Cursor cur_zoom;
    /** Cursor: move (usually 4 arrows but may as well be other symbol, e.g. dragging hand) */
    public static Cursor cur_move;
    /** Cursor: aim or cross-hair (used for targeting exact point(s)) */ 
    public static Cursor cur_aim;

    static boolean initializedStatic=false;
    
    /** is set to <code>true</code> if this app is run on an Apple Macintosh computer.
        Main reason is the different handling of mouse events: Macs have only one mouse button
        and other buttons are emulated. Also META key is guaranteed to be present and will be used. */
    static boolean isMac=false;

    /** static platform initialization. Should be performed as soon as possible upon startup.
        Code relying on platform dependent code should call this method to make sure the
        platform dependent code is initialized (initialization is done only once even if this method is called multiple times) */
    static void initStatic() {
        if (initializedStatic) return; // prevent loops
        initializedStatic=true;
        if (System.getProperty("java.vendor").indexOf("Apple")>-1) {
            isMac=true;
            try {
                Class c=Class.forName("PlatformMac");
                c.newInstance();
            } catch (Exception e) {
                if (DEBUG>0) System.out.println("Common.initStatic[Mac platform] failed to create platform-dependent class PlatformMac: "+e.getMessage());
            }
        } else {
            new Platform();
        };
    }

    /** returns <code>true</code> if ran on an Apple Macintosh computer */
    public static boolean isMac() {
        if (!initializedStatic) initStatic();
        return isMac;
    }

    /** given mouse event this method determines whether pop-up sequence was triggered */ 
    public static boolean isPopupTrigger(MouseEvent ev) {
        if (!initializedStatic) initStatic();
        return isMac?(ev.isControlDown() && !ev.isShiftDown() && !ev.isAltDown() && !ev.isMetaDown()):ev.isPopupTrigger();
    }

    /** given mouse event this method determines whether zoom sequence was triggered (mouse button 3 or META on a Mac) */ 
    public static boolean isZoomTrigger(MouseEvent ev) {
        // no need to check for Mac since button3 is emulated by Meta
        return (ev.getModifiers()&MouseEvent.BUTTON3_MASK)==MouseEvent.BUTTON3_MASK;
    }
    
    /** returns true if the supplied event corresponds to popup query trigger. */
    public static boolean isQueryTrigger(MouseEvent ev) {
        // Query = <ALT> + BUTTON1; since mac emulates B2 we don't impose this on a Mac
        if (!initializedStatic) initStatic();
        return (ev.isAltDown() && !ev.isControlDown() && (isMac || ((ev.getModifiers()&MouseEvent.BUTTON1_MASK)==MouseEvent.BUTTON1_MASK)));
    }

    public static boolean isExtQuery(MouseEvent ev) {
        return (ev.isShiftDown());
    }

    /** "select" trigger is left mouse button(1) - none of Alt, Meta or other mouse keys may be pressed.
        the only optional ones are Shift (XOR) and Shift+Ctrl (OR) (see {@link #getSelectMode})
        @return <code>true</code> if supplied event triggers selection trigger */
    public static boolean isSelectTrigger(MouseEvent ev) {
        if (!initializedStatic) initStatic();
        return isMac?(!ev.isMetaDown() && ((!ev.isControlDown() && !ev.isAltDown()) || (ev.isShiftDown() && ev.isAltDown() && ev.isControlDown()))):(!ev.isAltDown() && !ev.isMetaDown() && (!ev.isControlDown() || ev.isShiftDown()) && ((ev.getModifiers()&MouseEvent.BUTTON3_MASK)!=MouseEvent.BUTTON3_MASK) && ((ev.getModifiers()&MouseEvent.BUTTON2_MASK)!=MouseEvent.BUTTON2_MASK) && ((ev.getModifiers()&MouseEvent.BUTTON1_MASK)==MouseEvent.BUTTON1_MASK));
    }

    /** get selection mode according to the modifiers. Make sure {@link #isSelectTrigger} returns <code>true</code> otherwise the result of this function is invalid.
        @return 0=replace, 1=XOR, 2=union */
    public static int getSelectMode(MouseEvent ev) {
        return ev.isShiftDown()?(ev.isControlDown()?2:1):0;
    }

    /** add an application warning/error */
    public static void addWarning(String war) {
        if (maxWarnings>0 && warningsCount==maxWarnings) {
            warnings.append("** Too many warnings. No further warnings will be recoreded. **"); warningsCount++;
        };
        if (maxWarnings>0 && warningsCount>maxWarnings) return;
        if (warnings==null)
            warnings=new StringBuffer(war);
        else
            warnings.append(war);
        warnings.append("\n");
        warningsCount++;
    };

    /** get warnings/errors reported so far
        @return <code>null</code> if there are no warnings or string containing all warnings */        
    public static String getWarnings() { return (warnings==null)?null:warnings.toString(); };

    /** clear all warnings */
    public static void flushWarnings() { warnings=null; warningsCount=0; };
    
    /** returns a short form of the string given in s. it is more complex but
	tries to get a half-way sensible combination of letters from the word.
	first approach is to use capitals and numbers only. If that doesnt work
	then first letter+first consonant+last letter is used.
    @param s string to be abbreviated 
    @return abbreviated string (mostly trigraph, but exceptions may include
    4 letters) */
    public static String getTriGraph(String s) {
	if (s.length()<4) return s;
	int i=0;
	int caps=0;
	int nums=0;

	StringBuffer cp=new StringBuffer("");
	StringBuffer nm=new StringBuffer("");

	while(i<s.length()) {
	    char c=s.charAt(i);
	    if (c>='A'&&c<='Z') { caps++; cp.append(c); };
	    if (c>='0'&&c<='9') { nums++; nm.append(c); };
	    i++;
	};
	char lc=s.charAt(s.length()-1);
	if (nums>0) {
	    if (caps+nums<5 && caps>0)
		return cp.append(nm).toString();
	    if (nums<4 && caps>0)
		return cp.toString().substring(0,4-nums)+nm.toString();
	    if (nums>1 && nums<4 && caps==0 && s.charAt(0)>'9')
		return s.charAt(0)+nm.toString();
            if (nums==1 && s.charAt(0)>'9' && s.charAt(s.length()-1)>'9')
                return s.charAt(0)+nm.toString()+s.charAt(s.length()-1);
            if (nums==1 && s.charAt(0)>'9')
                lc=nm.toString().charAt(0);
	};
	if (caps==3||caps==4) return cp.toString();
	if (caps==2&&(lc<'A'||lc>'Z'))
	    return cp.append(lc).toString();
	i=1;
	char mid=' ';
	String ignore="aeiouAEIOU ._\t\n\röäüÖÄÜ";
	while (i<s.length()-1) {
	    char c=s.charAt(i);
	    if (ignore.indexOf(c)==-1) {
		mid=c; break;
	    };
	    i++;
	};
	if (mid==' ') mid=s.charAt(1);
	return ""+s.charAt(0)+mid+lc;
    };

    /** returns screen resolution. the value is cached after first successful retrival
        @return screen resolution */
    public static Dimension getScreenRes() {
        if (Common.screenRes==null) Common.screenRes=Toolkit.getDefaultToolkit().getScreenSize();
        return Common.screenRes;
    };
};
