package org.rosuda.pograss;

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

public class PoGraSSPS extends PoGraSS
{
    String fn;
    PrintStream outs;

    int lineWidth, fillSt, ox, oy;
    boolean inPath;
    /* color handling is quite spimlified here as we can define colors and use them directly in PS */

    String curFill, curPen;
    String title;
    String xver=null;

    String lastBaseFont;
    String italAppendix="Italic", romAppendix="";

    boolean lastLT=false; // true if lineTo was last command used
    int lastX, lastY; // coordinates of lst lineTo; used for optimizing output

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

    public void passVersionInfo(int ver, String verS) {
        xver=verS;
    }
    
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
    public void setColor(int R, int G, int B) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	
	outPS(curPen=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" setrgbcolor "));	
    };
    public void setColor(String nam) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	outPS(curPen=("color_"+nam+" "));
    };
    public void setFillColor(int R, int G, int B) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	if (R==G && G==B) // if R=G=B then use grayscale instead. this allows us to produce monochr. PS
	    outPS(curFill=((R/255.0)+" setgray "));		
	else
	    outPS(curFill=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" setrgbcolor "));	
    };
    public void setFillColor(String nam) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	outPS(curFill=("color_"+nam+" "));
    };
    public void drawLine(int x1, int y1, int x2, int y2) {
	moveTo(x1,y1); lineTo(x2,y2);
    };
    public void moveTo(int x, int y) {
	if (!inPath) { outPS("np "); lastLT=false;}; inPath=true;
	if (!lastLT || ox+x!=lastX || oy+y!=lastY)
	    outPS((ox+x)+" "+(oy-y)+" m ");
	lastLT=true; lastX=ox+x; lastY=ox+y;
    };
    public void lineTo(int x, int y) {
	if (!inPath) { outPS("np "); lastLT=false;}; inPath=true;
	if (!lastLT || ox+x!=lastX || oy+y!=lastY)
	    outPS((ox+x)+" "+(oy-y)+" l ");
	lastLT=true; lastX=ox+x; lastY=ox+y;
    };
    public void drawPolygon(int[] x, int[] y, int pts, boolean closed) {
	if (pts<2) return;
	moveTo(x[0],y[0]);
	int i=1;
	while(i<pts) {
	    lineTo(x[i],y[i]);
	    i++;
	}
	if (closed)
	    lineTo(x[0],y[0]);
    }
    public void fillPolygon(int[] x, int[] y, int pts) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	if (pts<2) return;
	moveTo(x[0],y[0]);
	int i=1;
	while(i<pts) {
	    lineTo(x[i],y[i]);
	    i++;
	}
	outPS("cp fill\n"); inPath=false; lastLT=false;
    };
    public void drawRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS("cp s\n"); inPath=false;
    };
    public void fillRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS("cp fill\n"); inPath=false;
    };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	dx/=2; dy/=2;
	outPS("np "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " m "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" "+dx+" arcto fp ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" "+dx+" arcto fp cp s\n");
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	dx/=2; dy/=2;
	outPS("np "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " m "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" "+dx+" arcto fp ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" "+dx+" arcto fp ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" "+dx+" arcto fp cp fill\n");
    };
    public void drawOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	rx/=2; ry/=2;
	outPS("np "+(ox+x+rx)+" "+(oy-y-ry)+
	      " m "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc cp s\n");
    };
    public void fillOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	rx/=2; ry/=2;
	outPS("np "+(ox+x+rx)+" "+(oy-y-ry)+
	      " m "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc cp fill\n");
    };
    public void setLineWidth(int w) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	lineWidth=w; 	
    };
    public void setFillStyle(int s) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	fillSt=s; 
    };
    public void drawString(String txt, int x, int y) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;	
	outPS("("+txt+") "+(ox+x)+" "+(oy-y)+" sw\n");
    };
    public void drawString(String txt, int x, int y, int att) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	String cmd="sw";
	if ((att&TA_MASK_Or)==TA_Right) cmd="swr";
	if ((att&TA_MASK_Or)==TA_Center) cmd="swc";
	outPS("("+txt+") "+(ox+x)+" "+(oy-y)+" "+cmd+"\n");
    };

    // currently ignores rot
    public void drawString(String txt, int x, int y, double ax, double ay) {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
	outPS(""+(x+ox)+" "+(oy-y)+" "+ax+" "+ay+" ("+txt+") swx\n");
    };

    public void nextLayer() {
        outPS("%% nextLayer\n");
    }
    
    public void begin() {
	if (fn!=null) {
	    try {
		outs=new PrintStream(new FileOutputStream(fn));
	    } catch(Exception E) { outs=null; };
	};
	outPS("%!PS-Adobe-2.0 EPSF-1.2\n");
	outPS("%%BoundingBox: 0 0 "+boundsWidth+" "+boundsHeight+"\n");
	if (title!=null) outPS("%%Title: "+title+"\n");
        outPS("%% Created by PoGraSSPS v"+versionString+((xver==null)?"":(", based on input of PoGraSS v"+xver))+"\n");
        outPS("/fp { 4 { pop } repeat } def\n/cp {closepath} def /s {stroke} def /m {moveto} def /l {lineto} def /np {newpath} def\n");
	outPS("/fe { -1 roll } def\n/xs { dup np 0 0 m false charpath pathbbox 4 -2 roll pop pop 4 fe mul exch 4 fe mul 5 fe add exch 4 fe add m show } def\n"); // x y ax ay txt <xs> (no rotation)
	outPS("/sw { m show } def /swr { 2 index np 0 0 m false charpath pathbbox 4 -2 roll pop pop pop -1 mul 3 fe add exch sw } def /swc { 2 index np 0 0 m false charpath pathbbox 4 -2 roll pop pop pop -0.5 mul 3 fe add exch sw } def \n"); // shortcuts for l/r/c text outs: txt x y sw[rc]
	outPS("/swx { 5 2 roll 3 index np 0 0 m false charpath pathbbox 4 -2 roll pop pop 7 fe mul 4 fe exch sub 3 1 roll mul 3 fe exch sub exch sw } def\n");
	outPS("/Helvetica findfont 10 scalefont setfont\n");
	lastFontSize=10; lastFontAttr=0; lastFont=FF_SansSerif;
	lastBaseFont=lastFace="Helvetica";
	italAppendix="Oblique"; romAppendix="";
    };
    
    public void end() {
	if (inPath) outPS(" cp s\n"); inPath=false; lastLT=false;
    };

    public void closePSoutput() {
	if (outs!=null) {
	    outs.close();
	    outs=null;
	};
    };
    
    public void setFontFace(int face) {
	lastBaseFont="Helvetica"; romAppendix=""; italAppendix="Oblique";
	if (face==FF_Serif) { lastBaseFont="Time"; romAppendix="-Roman"; italAppendix="Italic"; };
	if (face==FF_Mono) { lastBaseFont="Courier"; };
	lastFont=face;
	if (inPath) outPS(" cp s\n"); inPath=false;
	internal_setFontStyle(lastFontAttr);
	outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");	
    };
    public void setOptionalFace(String name) {
	/* warning: keep in mind that setting style after setOptionalFace restores
	   the default font assigned by setFontFace, since in PS style of the font
	   is given by its name */
	if (inPath) outPS(" cp s\n"); inPath=false;
	lastFace=name;
	outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
    public void setFontSize(int pt) {
	if (inPath) outPS(" cp s\n"); inPath=false;
	outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
    void internal_setFontStyle(int attr) {
	lastFace=lastBaseFont+romAppendix; lastFontAttr=attr;
	if ((attr&FA_MASK_Type)==FA_Bold) lastFace=lastBaseFont+"-Bold";
	if ((attr&FA_MASK_Type)==FA_Ital) lastFace=lastBaseFont+"-"+italAppendix;
    };
    public void setFontStyle(int attr) {
	if (inPath) outPS(" cp s\n"); inPath=false;
	internal_setFontStyle(attr);
	outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
}
