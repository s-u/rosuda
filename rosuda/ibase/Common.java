import java.awt.*;
import java.awt.event.*;

/** Common constants and gereral static methods for the entire application
    @version $Id$
*/
public class Common
{
    /** Debug flag. When set to >0 verbose debug messages are generated. */
    public static int DEBUG=0;
    /** Frame of the main window. Used by close-window-handler
     *  in {@link DefWinL} for exiting application if this window is closed. */
    public static Frame mainFrame=null;
    /** Default window listener used for handling global tasks 
     *  (like closing windows etc.). Any window is free to use it. */
    public static WindowListener defaultWindowListener=null;

    /** application version */
    public static String Version="0.95b";
    /** application release */
    public static String Release="C210";
    /** application type. so far 0=stand-alone (default, 1=applet - set by Wrapper) */
    public static int AppType=0;

    /** returns a short form of the string given in s. it is more complex but
	tries to get a half-way sensible combination of letter from the word.
	first approach is to use capitals and numbers only. If that doesnt work
	then first letter+first consonant+last letter is used.
    @param s string to be abbreviated 
    @returns abbreviated string (mostly trigraph, but exceptions may include
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
	};
	if (caps==3||caps==4) return cp.toString();
	if (caps==2&&(lc<'A'||lc>'Z'))
	    return cp.append(lc).toString();
	i=1;
	char mid=' ';
	String ignore="aeiouyAEIOUY \t\n\röäüÖÄÜ";
	while (i<s.length()-1) {
	    char c=s.charAt(i);
	    if (ignore.indexOf(c)==-1) {
		mid=c; break;
	    };
	    i++;
	};
	if (mid==' ') mid=s.charAt(1);
	return ""+s.charAt(0)+mid+s.charAt(s.length()-1);
    };
};
