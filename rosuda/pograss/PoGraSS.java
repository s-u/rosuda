import java.awt.Rectangle;

/** Portable Graphics SubSystem - first draft of the abstract interface definition.
    May change (or be extended) in the future.
    0.90: initial release
    0.91: (CVS 1.1) addComment, setTitle
    0.92: (CVS 1.2) support for layers (nextLayer)
    0.93: (CVS 1.3) support for version information (in the API; versionString, version and passVersionInfo)
    0.94: (CVS 1.5) support for extended drawString and font handling
    0.95: (CVS 1.6) support for polygons
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

    public String versionString="0.95";
    public int    version=0x0095;
        
    public PoGraSS() { boundsX=0; boundsY=0; };

    public void setBounds(int x, int y, int w, int h) {
	boundsX=x; boundsY=y; boundsWidth=w; boundsHeight=h; 
    };
    public void setBounds(int w, int h) { boundsX=0; boundsY=0; boundsWidth=w; boundsHeight=h; };
    public Rectangle getBounds() { 
	return new Rectangle(boundsX,boundsY,boundsWidth,boundsHeight); 
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
    public void drawRect(int x1, int y1, int x2, int y2) {};
    public void fillRect(int x1, int y1, int x2, int y2) {};
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void drawPolygon(int[] x, int[] y, int pts) { drawPolygon(x,y,pts,true); };
    public void drawPolyline(int[] x, int[] y, int pts) { drawPolygon(x,y,pts,false); };
    public void drawPolygon(int[] x, int[] y, int pts, boolean closed) {};
    public void fillPolygon(int[] x, int[] y, int pts) {};
    public void drawOval(int x, int y, int rx, int ry) {};
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
}
