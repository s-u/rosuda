import java.awt.*;
import java.awt.event.*;

/** Common constants and gereral static methods for the entire application
    @version $Id$
*/
public class Common
{
    /** application version */
    public static String Version="0.96h";
    /** application release */
    public static String Release="C731";

    /** Debug flag. When set to >0 verbose debug messages are generated.
        parameter equivalend: --debug / --nodebug */
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
    /** */
    public static boolean startRserv=false;
    

    public static final int AT_standalone = 0x0000;
    public static final int AT_applet     = 0x0001;
    public static final int AT_KOH        = 0x0020;
    public static final int AT_Framework  = 0x0030;
    
    /** application type. so far 0=stand-alone, other types are set by wrappers */
    public static int AppType=AT_standalone;   
    
    /** buffer containing all warnings/errors */
    static StringBuffer warnings=null;
    /** number of warnings so far */
    static int warningsCount=0;
    /** max. # of warnings, any further will be dropped (0=no limit)  */
    static int maxWarnings=20;
    /** screen resolution as obtained from the toolkit.  */
    public static Dimension screenRes=null;
    /** common background color. TFrame uses this as default */
    public static Color backgroundColor=new Color(255,255,192);
    /** common query popup background color */
    public static Color popupColor=new Color(245,255,255);
    /** common background color when aqua-style background is used */
    public static Color aquaBgColor=new Color(230,230,240);
    /** if <code>true</code> no internal variables are created */
    public static boolean noIntVar=false;
    /** SMarker state changed */
    public static final int NM_MarkerChange     =0x100;
    /** Axis changed */
    public static final int NM_AxisChange       =0x200;
    /** SVar changed */
    public static final int NM_VarChange        =0x300;
    /** SVar changed: content of a variable changed */
    public static final int NM_VarContentChange =0x301;
    /** SVar changed: type (cat/num) changed */
    public static final int NM_VarTypeChange    =0x302;
    /** SVarSet changed (e.g. # of vars...) */
    public static final int NM_VarSetChange     =0x400;
    /** current node changed */
    public static final int NM_NodeChange       =0x500;

    /** mask to apply in order to become top-level event */
    public static final int NM_MASK             =0xf00;

    /** returns true if the supplied event corresponds to popup query trigger. */
    public static boolean isQueryTrigger(MouseEvent ev) {
        /* this one prevents the use of <ctrl><shift>-select on Mac, so we drop the other button
           return ev.isAltDown() || ev.isPopupTrigger() || (ev.getModifiers()&MouseEvent.BUTTON3_MASK)>0; */
        return ev.isAltDown() /*|| ev.isPopupTrigger()*/;
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
	tries to get a half-way sensible combination of letter from the word.
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
