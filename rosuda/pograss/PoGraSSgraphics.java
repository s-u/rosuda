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

    Color curFillC;
    Color curPenC;

    /** construct an instance of {@link PoGraSS} associated with a {@link Graphics}.
	@param g associated Graphics */
    public PoGraSSgraphics(Graphics G) { 
	g=G;
	c=new Color[128]; cn=new String[128]; cs=0; fillSt=0; lineWidth=1;
	curFillC=Color.white; curPenC=Color.black;
    };
    
    Color getColor(String nam) {
	int i=0; while(i<cs) { if (cn[i].compareTo(nam)==0) return c[i]; i++; };
	return Color.black;
    };    
    
    /** In this defineColor implementation we're bad, because we ignore any colors that
	exceed our initial 128 colors limit. it would be fairly easy to use Vector for
	storage instead of fixed arrays, but well, there was no need for bigger color
	tables (yet).<p>
	Keep in mind, that the user can use any number of colors by using
	{@link setColor(int,int,int)}. the defineColor principle is meant just to
	define a fixed set of few colors used frequently throughout the application.
    */	
    public void defineColor(String nam, int R, int G, int B) 
    {
	if (cs<128) {
	    cn[cs]=new String(nam); c[cs]=new Color(R,G,B); cs++;
	}; 
    };
    public void setColor(int R, int B, int G) { g.setColor(curPenC=new Color(R,G,B)); };
    public void setColor(String nam) { g.setColor(curPenC=getColor(nam)); };
    public void drawLine(int x1, int y1, int x2, int y2) { g.drawLine(x1,y1,x2,y2); };
    public void moveTo(int x, int y) { cx=x; cy=y; };
    public void lineTo(int x, int y) { g.drawLine(cx,cy,x,y); cx=x; cy=y; };
    public void drawRect(int x1, int y1, int x2, int y2) { g.drawRect(x1,y1,x2,y2); };
    public void fillRect(int x1, int y1, int x2, int y2) { g.fillRect(x1,y1,x2,y2); };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	g.drawRoundRect(x1,y1,x2,y2,dx,dy);
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	g.fillRoundRect(x1,y1,x2,y2,dx,dy);
    };
    public void drawOval(int x, int y, int rx, int ry) { g.drawOval(x,y,rx,ry); };
    public void fillOval(int x, int y, int rx, int ry) { g.fillOval(x,y,rx,ry); };
    public void drawString(String txt, int x, int y) { g.drawString(txt,x,y); };

    public void begin() {};
    public void end() {};
};
