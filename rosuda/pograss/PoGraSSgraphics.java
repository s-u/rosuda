import java.awt.*;

/** Portable Graphics SubSystem - Graphics implementation
    @version $Id$
*/
class PoGraSSgraphics extends PoGraSS
{
    /** associated graphics context */
    Graphics g;
    /** list of defined color definitions */
    Color[] c;
    /** list of defined color names */
    String[] cn;
    /** # of defined colors (currently max. 128 supported) */
    int cs;

    int fillSt;
    int lineWidth;
    int cx,cy;
    int curLayer=0, paintLayer=0;

    Color curFillC;
    Color curPenC;

    /** construct an instance of {@link PoGraSS} associated with a {@link Graphics}.
	@param G associated Graphics
        @param layer layer to paint or -1 if all */
    public PoGraSSgraphics(Graphics G, int layer) { 
	g=G;
	c=new Color[128]; cn=new String[128]; cs=0; fillSt=0; lineWidth=1;
        curFillC=Color.white; curPenC=Color.black; paintLayer=layer;
	lastFace="SansSerif"; lastFontSize=10; lastFontAttr=Font.PLAIN;
	lastFont=PoGraSS.FF_SansSerif;
    };

    /** construct an instance of {@link PoGraSS} associated with a {@link Graphics}, paint all layers
        @param G associated Graphics */	
    public PoGraSSgraphics(Graphics G) {
        this(G,-1);
    }
    
    Color getColor(String nam) {
	int i=0; while(i<cs) { if (cn[i].compareTo(nam)==0) return c[i]; i++; };
	return Color.black;
    };    
    
    /** In this defineColor implementation we're bad, because we ignore any colors that
	exceed our initial 128 colors limit. it would be fairly easy to use Vector for
	storage instead of fixed arrays, but well, there was no need for bigger color
	tables (yet).<p>
	Keep in mind, that the user can use any number of colors by using
        {@link #setColor(int,int,int)}. the defineColor principle is meant just to
	define a fixed set of few colors used frequently throughout the application.
    */	
    public void defineColor(String nam, int R, int G, int B) 
    {
	if (cs<128) {
	    cn[cs]=new String(nam); c[cs]=new Color(R,G,B); cs++;
	}; 
    };
    public void setColor(int R, int G, int B) { if (paintLayer==-1 || paintLayer==curLayer) g.setColor(curPenC=new Color(R,G,B)); };
    public void setColor(String nam) { if (paintLayer==-1 || paintLayer==curLayer) g.setColor(curPenC=getColor(nam)); };
    public void drawLine(int x1, int y1, int x2, int y2) { if (paintLayer==-1 || paintLayer==curLayer) g.drawLine(x1,y1,x2,y2); };
    public void moveTo(int x, int y) { if (paintLayer==-1 || paintLayer==curLayer)  { cx=x; cy=y; } };
    public void lineTo(int x, int y) { if (paintLayer==-1 || paintLayer==curLayer) { g.drawLine(cx,cy,x,y); cx=x; cy=y; } };
    public void drawRect(int x1, int y1, int x2, int y2) { if (paintLayer==-1 || paintLayer==curLayer) g.drawRect(x1,y1,x2,y2); };
    public void fillRect(int x1, int y1, int x2, int y2) { if (paintLayer==-1 || paintLayer==curLayer) g.fillRect(x1,y1,x2,y2); };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (paintLayer==-1 || paintLayer==curLayer) g.drawRoundRect(x1,y1,x2,y2,dx,dy);
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (paintLayer==-1 || paintLayer==curLayer) g.fillRoundRect(x1,y1,x2,y2,dx,dy);
    };
    public void drawOval(int x, int y, int rx, int ry) { if (paintLayer==-1 || paintLayer==curLayer) g.drawOval(x,y,rx,ry); };
    public void fillOval(int x, int y, int rx, int ry) { if (paintLayer==-1 || paintLayer==curLayer) g.fillOval(x,y,rx,ry); };
    public void drawString(String txt, int x, int y) { if (paintLayer==-1 || paintLayer==curLayer) g.drawString(txt,x,y); };
    public void drawString(String txt, int x, int y, int a) {
	if (paintLayer==-1 || paintLayer==curLayer) {
	    if ((a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Right || (a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Center) {
		FontMetrics fm=g.getFontMetrics();
		int sw=fm.stringWidth(txt);
		if ((a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Center)
		    sw/=2;
		g.drawString(txt,x-sw,y);
	    } else
		g.drawString(txt,x,y);
	};
    };
    public void drawString(String txt, int x, int y, double ax, double ay) {
	if (paintLayer==-1 || paintLayer==curLayer) {
	    FontMetrics fm=g.getFontMetrics();
	    int dx=fm.stringWidth(txt);
	    int dy=fm.getHeight();
	    dx=(int)(((double)dx)*ax);
	    dy=(int)(((double)dy)*ay);
	    g.drawString(txt,x-dx,y+dy);
	};
    }

    public void drawPolygon(int[] x, int[] y, int pts, boolean closed) {
	if (paintLayer==-1 || paintLayer==curLayer) {
	    if (closed)
		g.drawPolygon(x,y,pts);
	    else
		g.drawPolyline(x,y,pts);
	}	
    }

    public void fillPolygon(int[] x, int[] y, int pts) {
	if (paintLayer==-1 || paintLayer==curLayer)
	    g.fillPolygon(x,y,pts);	
    }

    public void setFontFace(int face) {
	lastFace="SansSerif";
	if (face==PoGraSS.FF_Serif) lastFace="Serif";
	if (face==PoGraSS.FF_Mono) lastFace="Monospaced";
	lastFont=face;
	g.setFont(new Font(lastFace,lastFontSize,lastFontAttr));
    };
    public void setOptionalFace(String name) {
	lastFace=name;
	g.setFont(new Font(lastFace,lastFontSize,lastFontAttr));
    };
    public void setFontSize(int pt) {
	lastFontSize=pt;
	g.setFont(new Font(lastFace,lastFontSize,lastFontAttr));
    }
    public void setFontStyle(int attr) {
	lastFontAttr=Font.PLAIN;
	if ((attr&PoGraSS.FA_Ital)>0) lastFontAttr|=Font.ITALIC;
	if ((attr&PoGraSS.FA_Bold)>0) lastFontAttr|=Font.BOLD;
	g.setFont(new Font(lastFace,lastFontSize,lastFontAttr));
    }

    public void nextLayer() {
        curLayer++;
    }

    public void begin() { curLayer=0; }
    public void end() {}
}
