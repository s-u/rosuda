import java.awt.Rectangle;
import java.io.PrintStream;

/** Portable Graphics SubSystem - first draft of the abstract interface definition.
    May change (or be extended) in the future.
    0.90: initial release
    0.91: (CVS 1.1) addComment, setTitle
    0.92: (CVS 1.2) support for layers (nextLayer)
    0.93: (CVS 1.3) support for version information (in the API; versionString, version and passVersionInfo)
    0.94: (CVS 1.5) support for extended drawString and font handling
    0.95: (CVS 1.6) support for polygons
    0.96: (CVS 1.7) support for separate fill/pen colors and regular commands (rect, oval,...)
                    backwards compatibility is provided by the {@link #jointColors} flag. 
    @version $Id$
*/
public class PoGraSS
{
    int boundsWidth, boundsHeight, boundsX, boundsY;
    int fillStyle, lineWidth;
    int lastFont;
    int lastFontSize;
    int lastFontAttr;
    String lastFace;
    
    /** if set to <code>true</code> the pen and brush colors are not distinguished,
        i.e. setColor and setFillColor always set both colors to the same value.
        Such use is deprecated, but it is necessary for programs ported from Graphics.
        Programs using joint color are NOT allowed to use the regular commands which
        rely on both colors, such as rect, oval etc. Current default is <code>true</code>
        to simplify transition from old code, but in next release it will change.
    */
    boolean jointColors=true;
    
    public static final int TA_Left    = 0;
    public static final int TA_Right   = 1;
    public static final int TA_Center  = 2;

    public static final int TA_MASK_Or = 3;

    public static final int FA_Normal = 0;
    public static final int FA_Ital   = 1;
    public static final int FA_Bold   = 2;

    public static final int FA_MASK_Type = 7;

    // font face styles
    public static final int FF_SansSerif = 1; // usually Helvetica/Arial
    public static final int FF_Serif     = 2; // usually Times
    public static final int FF_Mono      = 3; // usually Courier or system font

    public String versionString="0.96";
    public int    version=0x0096;

    /** this variable is used only by PoGraSS implementations which support layer caching.
        the program using such PoGraSS can check this value to perform some speed optimizations
        and skip steps that are not necessary for the specified layer. If this value is -1 then
        no caching is used and all layers are to be fully painted (which is the default)
        [since 0.97beta]
    */
    public int    localLayerCache=-1;

    /** if set to <code>true</code> then calling any PoGraSS method should have no effect.
        it is used to make certain parts of PoGraSS code conditional - e.g. layers or alpha-support
        [since 0.97beta]
    */
    boolean nullEffect=false;

    PrintStream ps; // can be used by PoGraSS instances with file output
    
    public PoGraSS() { boundsX=0; boundsY=0; };

    public void setOutPrintStream(PrintStream pstr) { ps=pstr; };

    public void setBounds(int x, int y, int w, int h) {
	boundsX=x; boundsY=y; boundsWidth=w; boundsHeight=h; 
    };
    public void setBounds(int w, int h) { boundsX=0; boundsY=0; boundsWidth=w; boundsHeight=h; };
    public Rectangle getBounds() { 
	return new Rectangle(boundsX,boundsY,boundsWidth,boundsHeight); 
    };

    /** sets the {@link #jointColors} flag. Should be used after begin but before the first graphical command.
        The behavior is undefined if used elsewhere. */
    public void useJointColors(boolean jc) {
        jointColors=jc;
    };
    
    public void passVersionInfo(int ver, String verString) {}; // should not be called directly by programs, is meant for external interfaces, such as parser to allow unterlying PoGraSS implementations to do version check and refuse unsupported versions
    
    public void addComment(String c) {};
    public void setTitle(String t) {};
    public void defineColor(String nam, int R, int G, int B) {};
    public void setColor(int R, int G, int B) {};
    public void setColor(String nam) {};
    public void drawLine(int x1, int y1, int x2, int y2) {};
    public void moveTo(int x, int y) {};
    public void lineTo(int x, int y) {};
    public void rect(int x1, int y1, int x2, int y2) {}; // since 0.96
    public void drawRect(int x1, int y1, int x2, int y2) {};
    public void fillRect(int x1, int y1, int x2, int y2) {};
    public void roundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void polygon(int[] x, int[] y, int pts, boolean close) {}; // since 0.96
    public void drawPolygon(int[] x, int[] y, int pts) { drawPolygon(x,y,pts,true); };
    public void drawPolyline(int[] x, int[] y, int pts) { drawPolygon(x,y,pts,false); };
    public void drawPolygon(int[] x, int[] y, int pts, boolean closed) {};
    public void fillPolygon(int[] x, int[] y, int pts) {};
    public void drawOval(int x, int y, int rx, int ry) {};
    public void oval(int x, int y, int rx, int ry) {}; // since 0.96
    public void fillOval(int x, int y, int rx, int ry) {};
    public void setLineWidth(int w) { lineWidth=w; };
    public void setFillStyle(int s) { fillStyle=s; };
    public void drawString(String txt, int x, int y) { drawString(txt,x,y,0); };

    public void nextLayer() {};
    
    public void begin() {};
    public void end() {};

    // new since 0.94
    /** draw string with special attributes set (e.g. alignment etc.)
	default is left alignment, horizontal */
    public void drawString(String txt, int x, int y, int attr) {}

    /** draw string with anchor point x,y where ax and ay translate the anchor relative to
        text extension. For example ax=0.5, ay=0.5 center the text in both directions relative to the
	anchor point. */
    public void drawString(String txt, int x, int y, double ax, double ay) {}

    /** addition: rot specifies rotation angle around the anchor. negative values additionally
        flip the text on the horizontal axis of the text.
	Note that there are implementation of PoGraSS that don't support this function, so it should
	never be used for rot=0.
	In fact we're currently evaluating the possibility of removing this function for compatibility
	reasons (e.g. AWT 1.1 has no support for text rotation at all). This function is classified
	as experimental for now.
    */
    public void drawString(String txt, int x, int y, double ax, double ay, double rot) {
	if(rot==0) drawString(txt,x,y,ax,ay);
    }

    // preliminary font support .. user definable fonts may follow later

    /** set font face by style */
    public void setFontFace(int face) {};
    /** set optional font face. the underlying system will use the face only if it's available.
	you should specify the desired font type by {@link #setFontFace} and refine this selection by
	setting optional face. It is not guaranteed that this face will be used, keep it in mind.
	example: setFontFace(FF_SansSerif); setOptionalFont("Helvetica"); setOptionalFont("Myriad");
    */
    public void setOptionalFace(String name) {};
    /** just a shortcut for a sequence of setFontFace(face); setOptionalFace(name); */
    public void setFontFace(int face, String name) { setFontFace(face); setOptionalFace(name); }
    public void setFontSize(int pt) {}
    public void setFontStyle(int attr) {}

    // 0.97-beta experimental methods - they are NOT in the standard yet
    // these commands are NOT translated into meta. They are also ESTIMATES only, this is
    // why they cannot be used for exact centering etc. Main purpose is to implement roughly
    // boxed text - a feature which should come in final 0.97 or 0.98 once we get rid of
    // single-color
    public int getWidthEstimate(String s) { return (s==null)?0:s.length()*8; }
    public int getHeightEstimate(String s) { return 12; };
    // also beta: support for alpha channel
    // beware: alpha-channels are not supported by all PoGraSS implementations
    // (that's why it wasn't included in the first PoGraSS draft) - e.g. PoseScript
    // doesn't support transparency (except for PS 3.0+)
    public void defineColor(String nam, float r, float g, float b, float a) {}
    public void setColor(String nam, float alpha) {}
    public void setColor(float r, float g, float b, float a) {}
    // this construct should help to produce independent graphics code
    // any app that uses alpha only for specific tasks should enclose such code
    // by those control block commands and supply alternative methods without alpha

    public boolean internalSupportsAlpha() { return false; }

    public void beginAlphaBlock() { nullEffect=!internalSupportsAlpha(); }
    public void fallbackAlpha() { nullEffect=!internalSupportsAlpha(); }
    public void endAlphaBlock() { nullEffect=false; }
    // and finally separate colors
    public void setBrushColor(String nam) {}
    public void setBrushColor(float r, float g, float b) {}
    public void setBrushColor(String nam, float alpha) {}
    public void setBrushColor(float r, float g, float b, float a) {}
    public void setPenColor(String nam) {}
    public void setPenColor(float r, float g, float b) {}
    public void setPenColor(String nam, float alpha) {}
    public void setPenColor(float r, float g, float b, float a) {}
    
}
