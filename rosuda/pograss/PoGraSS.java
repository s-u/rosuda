import java.awt.Rectangle;

/** Portable Graphics SubSystem - first draft of the abstract interface definition.
    May change (or be extended) in the future.
    1.1: addComment, setTitle
    1.2: support for layers (nextLayer)
    @version $Id$
*/
public class PoGraSS
{
    int boundsWidth, boundsHeight, boundsX, boundsY;
    int fillStyle, lineWidth;
    
    public PoGraSS() { boundsX=0; boundsY=0; };

    public void setBounds(int x, int y, int w, int h) {
	boundsX=x; boundsY=y; boundsWidth=w; boundsHeight=h; 
    };
    public void setBounds(int w, int h) { boundsX=0; boundsY=0; boundsWidth=w; boundsHeight=h; };
    public Rectangle getBounds() { 
	return new Rectangle(boundsX,boundsY,boundsWidth,boundsHeight); 
    };

    public void addComment(String c) {};
    public void setTitle(String t) {};
    public void defineColor(String nam, int R, int G, int B) {};
    public void setColor(int R, int B, int G) {};
    public void setColor(String nam) {};
    public void drawLine(int x1, int y1, int x2, int y2) {};
    public void moveTo(int x, int y) {};
    public void lineTo(int x, int y) {};
    public void drawRect(int x1, int y1, int x2, int y2) {};
    public void fillRect(int x1, int y1, int x2, int y2) {};
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {};
    public void drawOval(int x, int y, int rx, int ry) {};
    public void fillOval(int x, int y, int rx, int ry) {};
    public void setLineWidth(int w) { lineWidth=w; };
    public void setFillStyle(int s) { fillStyle=s; };
    public void drawString(String txt, int x, int y) {};

    public void nextLayer() {};
    
    public void begin() {};
    public void end() {};
};
