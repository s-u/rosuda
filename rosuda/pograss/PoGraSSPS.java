import java.io.*;

/** Portable Graphics SubSystem - PostScript implementation
 *
 * Bugs: in this PS implementation xxxOval, xxxRoundRectangle etc. require dx=dy because
 *       in PostScript arc resp. arcto is used where both of them require only one radius argument
 *       in fact spacing is done with dx/dy but arc is drawn with dx only, thus resulting in ugly drawing
 *
 * Changelog: 1.0.0  initial release
 *            1.0.1  added support for raw PrintStream constructor
 *                   added support for monochromatic PS: if equal RGB values are used, set grayscale
 * @version $Id$
 */

class PoGraSSPS extends PoGraSS
{
    String fn;
    PrintStream outs;

    int lineWidth, fillSt, ox, oy;
    boolean inPath;
    /* color handling is quite spimlified here as we can define colors and use them directly in PS */

    String curFill, curPen;
    String title;

    public PoGraSSPS(String fnn)
    {
	lineWidth=1; fillSt=0; inPath=false; outs=null; fn=fnn;
	curFill="0 gray "; curPen="1 gray "; title=null;
	ox=0; oy=1000;
    };

    public PoGraSSPS(PrintStream ps)
    {
	lineWidth=1; fillSt=0; inPath=false; outs=ps; fn=null;
	curFill="0 gray "; curPen="1 gray "; title=null;
	ox=0; oy=1000;
    };

    public void setBounds(int w, int h) {
	boundsWidth=w; boundsHeight=h; ox=0; oy=boundsHeight;
    };
    

    void outPS(String s) { if (outs!=null) outs.print(s); };

    public void addComment(String c) {
	outPS("%% -- "+c);
    };

    public void setTitle(String t) {
	title=t;
    };

    public void defineColor(String nam, int R, int G, int B) {
	if (R==G && G==B) // if R=G=B then use grayscale instead. this allows us to produce monochr. PS
	    outPS("/color_"+nam+"  { "+(R/255.0)+" setgray } def\n");
	else
	    outPS("/color_"+nam+"  { "+(R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" setrgbcolor } def\n");
    };
    public void setColor(int R, int B, int G) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	
	outPS(curPen=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" setrgbcolor "));	
    };
    public void setColor(String nam) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	outPS(curPen=("color_"+nam+" "));
    };
    public void setFillColor(int R, int G, int B) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	if (R==G && G==B) // if R=G=B then use grayscale instead. this allows us to produce monochr. PS
	    outPS(curFill=((R/255.0)+" setgray "));		
	else
	    outPS(curFill=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" setrgbcolor "));	
    };
    public void setFillColor(String nam) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	outPS(curFill=("color_"+nam+" "));
    };
    public void drawLine(int x1, int y1, int x2, int y2) {
	moveTo(x1,y1); lineTo(x2,y2);
    };
    public void moveTo(int x, int y) {
	if (!inPath) outPS("newpath "); inPath=true;
	outPS((ox+x)+" "+(oy-y)+" moveto ");
    };
    public void lineTo(int x, int y) {
	if (!inPath) outPS("newpath "); inPath=true;
	outPS((ox+x)+" "+(oy-y)+" lineto ");
    };
    public void drawRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS("closepath stroke\n"); inPath=false;
    };
    public void fillRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS("closepath fill\n"); inPath=false;
    };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	dx/=2; dy/=2;
	outPS("newpath "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " moveto "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" "+dx+" arcto fp ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" "+dx+" arcto fp closepath stroke\n");
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	dx/=2; dy/=2;
	outPS("newpath "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " moveto "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" "+dx+" arcto fp ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" "+dx+" arcto fp closepath fill\n");
    };
    public void drawOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	rx/=2; ry/=2;
	outPS("newpath "+(ox+x+rx)+" "+(oy-y-ry)+
	      " moveto "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc closepath stroke\n");
    };
    public void fillOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	rx/=2; ry/=2;
	outPS("newpath "+(ox+x+rx)+" "+(oy-y-ry)+
	      " moveto "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc closepath fill\n");
    };
    public void setLineWidth(int w) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	lineWidth=w; 	
    };
    public void setFillStyle(int s) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	fillSt=s; 
    };
    public void drawString(String txt, int x, int y) {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;	
	outPS((ox+x)+" "+(oy-y)+" moveto ("+txt+") show\n");
    };

    public void begin() {
	if (fn!=null) {
	    try {
		outs=new PrintStream(new FileOutputStream(fn));
	    } catch(Exception E) { outs=null; };
	};
	outPS("%!PS-Adobe-2.0 EPSF-1.2\n");
	outPS("%%BoundingBox: 0 0 "+boundsWidth+" "+boundsHeight+"\n");
	if (title!=null) outPS("%%Title: "+title+"\n");
	outPS("/fp { 4 { pop } repeat } def\n");
	outPS("/Helvetica findfont 10 scalefont setfont\n");
    };
    
    public void end() {
	if (inPath) outPS(" closepath stroke\n"); inPath=false;
	if (outs!=null) outs.close();
	outs=null;
    };
};
