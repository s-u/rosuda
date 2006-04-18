package org.rosuda.ibase;

import java.util.Date;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.awt.event.MouseEvent;

import org.rosuda.util.*;
// we should weed this out sometime ...
import org.rosuda.plugins.PluginManager;

/** Common constants and gereral static methods for the entire application and ibase toolkit
    @version $Id$
*/
public class Common
{
    /** ibase version */
    public static String Version="0.97";
    /** ibase release */
    public static String Release="D729";
    /** application identifier (the first thing to do in main is to modify this) */
    public static String appName="default";

    public static boolean useSwing=true;
    /** determines whether R-serve should be started if it's not running yet */
    public static boolean startRserv=false;

    /** AppType contstant: Klimt called by Omegahat SJava interface */
    public static final int AT_KOH        = 0x0020;
    /** AppType contstant: launched by interactive framework interface */
    public static final int AT_Framework  = 0x0030;
    
    /** buffer containing all warnings/errors */
    static StringBuffer warnings=null;
    /** number of warnings so far */
    static int warningsCount=0;
    /** max. # of warnings, any further will be dropped (0=no limit)  */
    static int maxWarnings=250;
    /** screen resolution as obtained from the toolkit.  */
    public static Dimension screenRes=null;
    /** common background color. TFrame uses this as default */
    public static Color backgroundColor=new Color(255,255,192);
    /** common query popup background color */
    public static Color popupColor=new Color(245,255,255);
    /** common background color when aqua-style background is used */
    public static Color aquaBgColor=new Color(230,230,240);
    /** common selection color (so far only BaseCanvas-based plots use this) */
    public static Color selectColor=new Color(255,0,0);
    //public static Color selectColor=new Color(128,255,128);
    /** common objects color */
    public static Color objectsColor=new Color(255,255,255);
    /** if <code>true</code> no internal variables are created */
    public static boolean noIntVar=false;

    public static Font defaultFont=new Font("SansSerif",Font.PLAIN,10);
    
	/** default plot component type (AWT/Swing/...) for automatically created plot components */
	public static int defaultPlotComponentType = org.rosuda.ibase.toolkit.PlotComponent.AWTGrDevID;
	
    /** Notify-Message constant: SMarker state changed */
    public static final int NM_MarkerChange       =0x001000;
    /** Notify-Message constant: SMarker state changed (secondary marks) */
    public static final int NM_SecMarkerChange    =0x001002;
    
    /** Notify-Message constant: Axis changed */
    public static final int NM_AxisChange         =0x002000;
    /** Notify-Message constant: geometry part of an Axis changed */
    public static final int NM_AxisGeometryChange =0x002001;
    /** Notify-Message constant: value/data part of an Axis changed */
    public static final int NM_AxisDataChange     =0x002002;
    
    /** Notify-Message constant: SVar changed */
    public static final int NM_VarChange          =0x003000;
    /** Notify-Message constant: SVar changed: content of a variable changed */
    public static final int NM_VarContentChange   =0x003001;
    /** Notify-Message constant: SVar changed: type (cat/num) changed */
    public static final int NM_VarTypeChange      =0x003002;
    /** Notify-Message constant: SVar changed: sequence of categories changed */
    public static final int NM_VarSeqChange       =0x003003;

    /** Notify-Message constant: SVarSet changed (e.g. # of vars...) */
    public static final int NM_VarSetChange       =0x004000;
    /** Notify-Message constant: current node changed */
    public static final int NM_NodeChange         =0x005000;

    /** Notify-Message constant: category sequence changed (fired by SCatSeqence)
        note that SCatSequence passes change events to SVar only if it is told to. This allows other classes to maintain private category sequences independent of the variable's main sequence. This event is sent to the private dependents. */
    public static final int NM_CatSeqChange       =0x006000;

    /** Notify-Message constant: preferences have changed */
    public static final int NM_PrefsChanged       =0x007000;
    
    /** Notify-Message constant: BREAK event - this one is usually not processed in Java but sent to the calling system. Usually this event is used to stop an external event loop, such as an iPlots event loop. */
    public static final int NM_BREAK              =0x700000;

    public static final int NM_ExtEvent           =0x800000;
    public static final int NM_ActionEvent        =0x800001;
    
    /** mask to apply in order to get the top-level event */
    public static final int NM_MASK               =0xfff000;

    /** this flag is set to <code>true</code> by an external application if the underlying system supports the BREAK event */
    public static boolean supportsBREAK = false;
    /** BREAK dispatcher for this application. check for <code>null</code> before use, since it is initialized only if BREAK event is supported *and* dispatched */
    public static Notifier breakDispatcher = null;
    
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

    /** main frame */
    public static Frame mainFrame;
    
    /** Default window listener used for handling global tasks
        *  (like closing windows etc.). Any window is free to use it. */
    static WindowListener defWinListener=null;

    public static WindowListener getDefaultWindowListener() {
        if (defWinListener==null)
            defWinListener=new org.rosuda.ibase.toolkit.WinListener();
        return defWinListener;
    }
    
    public static void initValuesFromConfigFile(PluginManager pm) {
        if (Global.DEBUG>0)
            System.out.println("Common.initValuesFromConfigFile: loading values");
        String s=GlobalConfig.getS("Common.color.background");
        if (Global.DEBUG>0)
            System.out.println("Common.color.background="+s);
        if (s!=null && s.length()>0 && s.charAt(0)=='#') {
            int c=Tools.parseHexInt(s.substring(1));
            backgroundColor=new Color((c>>16)&255,(c>>8)&255,c&255);            
        }
        s=GlobalConfig.getS("Common.color.select");
        if (Global.DEBUG>0)
            System.out.println("Common.color.select="+s);
        if (s!=null && s.length()>0 && s.charAt(0)=='#') {
            int c=Tools.parseHexInt(s.substring(1));
            selectColor=new Color((c>>16)&255,(c>>8)&255,c&255);
        }
        s=GlobalConfig.getS("Common.color.objects");
        if (Global.DEBUG>0)
            System.out.println("Common.color.objects="+s);
        if (s!=null && s.length()>0 && s.charAt(0)=='#') {
            int c=Tools.parseHexInt(s.substring(1));
            objectsColor=new Color((c>>16)&255,(c>>8)&255,c&255);
        }
    }

    /** returns <code>true</code> if ran on an Apple Macintosh computer */
    public static boolean isMac() {
        return Platform.isMac;
    }

    /** given mouse event this method determines whether pop-up sequence was triggered */ 
    public static boolean isPopupTrigger(MouseEvent ev) {
        return Platform.isMac?(ev.isControlDown() && !ev.isShiftDown() && !ev.isAltDown() && !ev.isMetaDown()):ev.isPopupTrigger();
    }

    /** given mouse event this method determines whether zoom sequence was triggered (mouse button 3 or META on a Mac) */ 
    public static boolean isZoomTrigger(MouseEvent ev) {
        return ev.getButton()==MouseEvent.BUTTON3;
    }
    
    /** returns true if the supplied event corresponds to popup query trigger. */
    public static boolean isQueryTrigger(MouseEvent ev) {
        // Query = <ALT> + BUTTON1; since mac emulates B2 we don't impose this on a Mac
    	return ev.isControlDown() && !ev.isAltDown();
    }

    /** returns true if the supplied event corresponds to object-move trigger */
    public static boolean isMoveTrigger(MouseEvent ev) {
        return ev.isAltDown() && !ev.isControlDown() && !ev.isShiftDown() && ev.getButton()==MouseEvent.BUTTON1;
    }
    
    public static boolean isExtQuery(MouseEvent ev) {
        return (ev.isShiftDown() && ev.isControlDown());
    }

    /** "select" trigger is left mouse button(1) - none of Alt, Meta or other mouse keys may be pressed.
        the only optional ones are Shift (XOR) and Shift+Ctrl (OR) (see {@link #getSelectMode})
        @return <code>true</code> if supplied event triggers selection trigger */
    public static boolean isSelectTrigger(MouseEvent ev) {
        return Platform.isMac?(!ev.isMetaDown() && ((!ev.isControlDown() && !ev.isAltDown()) || (ev.isShiftDown() && ev.isAltDown() && ev.isControlDown()))):(!ev.isAltDown() && !ev.isMetaDown() && (!ev.isControlDown() || ev.isShiftDown()) && ((ev.getModifiers()&MouseEvent.BUTTON3_MASK)!=MouseEvent.BUTTON3_MASK) && ((ev.getModifiers()&MouseEvent.BUTTON2_MASK)!=MouseEvent.BUTTON2_MASK) && ((ev.getModifiers()&MouseEvent.BUTTON1_MASK)==MouseEvent.BUTTON1_MASK));
    }

    /** get selection mode according to the modifiers. Make sure {@link #isSelectTrigger} returns <code>true</code> otherwise the result of this function is invalid.
        @return 0=replace, 1=XOR, 2=union */
    public static int getSelectMode(MouseEvent ev) {
        return ev.isShiftDown()?(ev.isControlDown()?2:1):0;
    }

    public static void printEvent(MouseEvent ev) {
        if (Global.DEBUG>0) {
            String mods="";
            if (ev.isShiftDown()) mods+=" SHIFT";
            if (ev.isAltDown()) mods+=" ALT";
            if (ev.isControlDown()) mods+=" CTRL";
            if (ev.isMetaDown()) mods+=" META";
            if (ev.isAltGraphDown()) mods+=" ALT.GR";
            if ((ev.getModifiers()&MouseEvent.BUTTON1_MASK)==MouseEvent.BUTTON1_MASK) mods+=" M1";
            if ((ev.getModifiers()&MouseEvent.BUTTON2_MASK)==MouseEvent.BUTTON2_MASK) mods+=" M2";
            if ((ev.getModifiers()&MouseEvent.BUTTON3_MASK)==MouseEvent.BUTTON3_MASK) mods+=" M3";
            if (ev.isPopupTrigger()) mods+=" POPUP";
            System.out.println("Event:"+ev+mods);
        }
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
        int lzs=0;
        int firstNonZeroNum=0;
        boolean isLz=true;

	StringBuffer cp=new StringBuffer("");
	StringBuffer nm=new StringBuffer("");

	while(i<s.length()) {
	    char c=s.charAt(i);
	    if (c>='A'&&c<='Z') { caps++; cp.append(c); };
            if (c>'0' && c<='9') isLz=false;
	    if (!isLz && c>='0' &&c <='9') { nums++; nm.append(c); };
            if (c=='0' && isLz) lzs++;
	    i++;
	}
        if (nums==1) { nums=2; nm=new StringBuffer("0"+nm.toString()); } // hack hack hack
	char lc=s.charAt(s.length()-1);
        //System.out.println("\""+s+"\" nums="+nums+", caps="+caps);
	if (nums>0) {
            // hack for imported variables which have the form X_i_...
            if (nums<3 && s.length()>4 && s.charAt(1)=='_' && s.charAt(2)=='i' && s.charAt(3)=='_')
                return s.charAt(0)+"i"+nm.toString();
	    if (caps+nums<5 && caps>0) // this is clumsy, but since 1.4.1 append fails
		return cp.toString()+nm.toString();
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
	String ignore="aeiouAEIOU ._\t\n\r������";
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

    static Frame workFrame;
    
    public static void beginWorking(String txt) {
        if (workFrame!=null) endWorking(); // we don't support nested calls
        workFrame=new Frame();
        workFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public static void endWorking() {
        if (workFrame!=null) {
            workFrame.dispose();
            workFrame=null;
        }
    }
    
    // HCL color scheme routines (ported from Ross Ihaka's R code)
    /** display gamma setting (used by color conversion functions such as {@link #getHCLcolor} */
    public static double displayGamma=2.2;

    /** adjusts RGB value according to the specified display gamma setting (see {@link #displayGamma}) */
    public static double gammaAdjust(double u) {
        return (u > 0.00304)?1.055 * Math.pow(u,(1 / displayGamma)) - 0.055: 12.92 * u;
    }

    /** transforms color defined in HCL space into RGB color
        @param hue - hue (in degrees, between 0.0 and 360.0). basic colors are at angles 0, 120, 240
        @param chroma - colorfullness of the color - unlike the saturation, chroma is an absolute value (default=35)
        @param luminance - brightness of the color relative to while (white=100; default=85)
        @return color object in RGB representation suitable for use in graphics */
    public static Color getHCLcolor(double hue, double chroma, double luminance) {
        //function(hue, chroma = 35, luminance = 85, correct = FALSE, gamma = 2.2)
        //  Assume a D65 whitepoint with luminance 100.
        //  Ultimately, this should be a parameter.
        //  These are the CIE XYZ values.

        double XN =  95.047;
        double YN = 100.000;
        double ZN = 108.883;

        //  uN and vN are the corresponding LUV chromaticities

        double tmp = XN + YN + ZN;
        double xN = XN / tmp;
        double yN = YN / tmp;
        double uN = 2 * xN /(6 * yN - xN + 1.5);
        double vN = 4.5 * yN / (6 * yN - xN + 1.5);

        //  Convert from polar coordinates to u and v.
        //  Hue is take to be in degrees and needs to be converted.

        double U = chroma * Math.cos(.01745329251994329576 * hue);
        double V = chroma * Math.sin(.01745329251994329576 * hue);

        // Convert from L*u*v* to CIE-XYZ

        double Y = YN * ((luminance > 7.999592)?Math.pow((luminance + 16)/116,3):luminance/903.3);
        double u = U / (13 * luminance) + uN;
        double v = V / (13 * luminance) + vN;
        double X = 9.0 * Y * u / (4 * v);
        double Z = - X / 3 - 5 * Y + 3 * Y / v;

        //  Map to ``gamma dependent'' RGB

        int r=(int)(255.0 * gammaAdjust(( 3.240479 * X - 1.537150 * Y - 0.498535 * Z) / YN));
        int g=(int)(255.0 * gammaAdjust((-0.969256 * X + 1.875992 * Y + 0.041556 * Z) / YN));
        int b=(int)(255.0 * gammaAdjust(( 0.055648 * X - 0.204043 * Y + 1.057311 * Z) / YN));

        if (r<0) r=0; if (r>255) r=255;
        if (g<0) g=0; if (g>255) g=255;
        if (b<0) b=0; if (b>255) b=255;
        return new Color(r,g,b);
    }

    public static Color getHCLcolor(double hue) { return getHCLcolor(hue,35.0,85.0); }
    public static Color getHCLcolor(double hue, double chroma) { return getHCLcolor(hue,chroma,85.0); }
};
