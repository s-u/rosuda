import java.io.*;

/** Portable Graphics SubSystem - PDF implementation, based on PS implementation
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

class PoGraSSPDF extends PoGraSS
{
    String fn;
    PrintStream outs;
    PDFconstructor p;
    StringBuffer cont;
    /** list of defined color definitions */
    String[] c;
    /** list of defined color names */
    String[] cn;
    /** # of defined colors (currently max. 128 supported) */
    int cs;
    
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

    public PoGraSSPDF(String fnn)
    {
	lineWidth=1; fillSt=0; inPath=false; outs=null; fn=fnn;
	curFill="0 g "; curPen="1 g "; title=null;
	ox=0; oy=1000; c=new String[128]; cn=new String[128]; cs=0;
	cont=new StringBuffer();
    };

    public PoGraSSPDF(PrintStream ps)
    {
	lineWidth=1; fillSt=0; inPath=false; outs=ps; fn=null;
	curFill="0 g "; curPen="1 g "; title=null;
	ox=0; oy=1000; c=new String[128]; cn=new String[128]; cs=0;
	cont=new StringBuffer();
    };

    String getColor(String nam) {
        int i=0; while(i<cs) { if (cn[i].compareTo(nam)==0) return c[i]; i++; };
        return "0.0 0.0 0.0";
    };

    public void defineColor(String nam, String cmd)
    {
        if (cs<128) {
            cn[cs]=new String(nam); c[cs]=new String(cmd); cs++;
        };
    };
    
    public void passVersionInfo(int ver, String verS) {
        xver=verS;
    }
    
    public void setBounds(int w, int h) {
	boundsWidth=w; boundsHeight=h; ox=0; oy=boundsHeight;
    };
    

    void outPS(String s) {
	cont.append(s);
    };

    public void addComment(String c) {
	outPS("%% -- "+c);
    };

    public void setTitle(String t) {
	title=t;
    };

    public void defineColor(String nam, int R, int G, int B) {
        defineColor(nam,""+(R/255.0)+" "+(G/255.0)+" "+(B/255.0));
    };

    public void setColor(int R, int G, int B) {
        if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
        outPS(curPen=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" RG "));
        if (jointColors) outPS(((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" rg "));	
    };

    public void setColor(String nam) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
        outPS(curPen=getColor(nam)+" RG ");
        if (jointColors) outPS(getColor(nam)+" rg ");
    };
    
    public void setFillColor(int R, int G, int B) {
        if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	if (R==G && G==B) // if R=G=B then use grayscale instead. this allows us to produce monochr. PS
	    outPS(curFill=((R/255.0)+" g "));		
	else
	    outPS(curFill=((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" rg "));
        if (jointColors) {
            if (R==G && G==B) // if R=G=B then use grayscale instead. this allows us to produce monochr. PS
                outPS(((R/255.0)+" G "));
            else
                outPS(((R/255.0)+" "+(G/255.0)+" "+(B/255.0)+" RG "));
        };
    };

    public void setFillColor(String nam) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
        outPS(curFill=getColor(nam)+" rg ");
        if (jointColors) outPS(getColor(nam)+" RG ");
    };

    public void drawLine(int x1, int y1, int x2, int y2) {
	moveTo(x1,y1); lineTo(x2,y2);
    };

    public void moveTo(int x, int y) {
	if (!inPath) { lastLT=false;}; inPath=true;
	if (!lastLT || ox+x!=lastX || oy+y!=lastY)
	    outPS((ox+x)+" "+(oy-y)+" m ");
	lastLT=true; lastX=ox+x; lastY=ox+y;
    };
    public void lineTo(int x, int y) {
	if (!inPath) { lastLT=false;}; inPath=true;
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
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	if (pts<2) return;
	moveTo(x[0],y[0]);
	int i=1;
	while(i<pts) {
	    lineTo(x[i],y[i]);
	    i++;
	}
	outPS(" f\n"); inPath=false; lastLT=false;
    };
    public void drawRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS(" S\n"); inPath=false;
    };
    public void fillRect(int x1, int y1, int x2, int y2) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	moveTo(x1,y1); lineTo(x1+x2,y1); lineTo(x1+x2,y1+y2); lineTo(x1,y1+y2); lineTo(x1,y1);
	outPS(" f\n"); inPath=false;
    };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	dx/=2; dy/=2;
	outPS(" "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " m "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" v ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" v ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" v ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" v S\n");
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	dx/=2; dy/=2;
	outPS(" "+(ox+x1+x2-dx)+" "+(oy-y1)+
	      " m "+(ox+x1+x2)+" "+(oy-y1)+" "+(ox+x1+x2)+" "+(oy-y1-y2+dy)+" v ");
	outPS((ox+x1+x2)+" "+(oy-y1-y2)+" "+(ox+x1+dx)+" "+(oy-y1-y2)+" v ");
	outPS((ox+x1)+" "+(oy-y1-y2)+" "+(ox+x1)+" "+(oy-y1-dy)+" v ");
	outPS((ox+x1)+" "+(oy-y1)+" "+(ox+x1+x2-dx)+" "+(oy-y1)+" v f\n");
    };
    public void drawOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	rx/=2; ry/=2;
        //outPS("np "+(ox+x+rx)+" "+(oy-y-ry)+
	//      " m "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc cp s\n");
    };
    public void fillOval(int x, int y, int rx, int ry) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	rx/=2; ry/=2;
	//	outPS("np "+(ox+x+rx)+" "+(oy-y-ry)+
	//" m "+(ox+x+rx)+" "+(oy-y-ry)+" "+rx+" 0 360 arc cp fill\n");
    };
    public void setLineWidth(int w) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	lineWidth=w;
        outPS(" "+w+" w ");
    };
    public void setFillStyle(int s) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	fillSt=s; 
    };
    public void drawString(String txt, int x, int y) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;	
	outPS("BT /F1 "+lastFontSize+" Tf "+(ox+x)+" "+(oy-y)+" Td ("+txt+") Tj ET\n");
    };
    public void drawString(String txt, int x, int y, int att) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;       
	String cmd="sw";
	if ((att&TA_MASK_Or)==TA_Right) cmd="swr";
	if ((att&TA_MASK_Or)==TA_Center) cmd="swc";
	//outPS("("+txt+") "+(ox+x)+" "+(oy-y)+" "+cmd+"\n");
    };

    // currently ignores rot
    public void drawString(String txt, int x, int y, double ax, double ay) {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
	//outPS(""+(x+ox)+" "+(oy-y)+" "+ax+" "+ay+" ("+txt+") xsw\n");
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
	p=new PDFconstructor(outs,1);
        p.addObject("<< /Type /Catalog\n/Outlines 2 0 R\n/Pages 3 0 R\n>>");
        p.addObject("<< /Type /Outlines\n/Count 0\n>>");
        p.addObject("<< /Type /Pages\n/Kids [4 0 R]\n/Count 1\n>>");
        p.addObject("<< /Type /Page\n/Parent 3 0 R\n/MediaBox [0 0 "+boundsWidth+" "+boundsHeight+"]\n/Contents 5 0 R\n/Resources << /ProcSet 6 0 R\n/Font << /F1 7 0 R >>\n>>\n>>");
	lastFontSize=10; lastFontAttr=0; lastFont=FF_SansSerif;
	lastBaseFont=lastFace="Helvetica";
	italAppendix="Oblique"; romAppendix="";
    };
    
    public void end() {
	if (inPath) outPS(" S\n"); inPath=false; lastLT=false;
        p.addObject(PDFconstructor.asStream(cont.toString()));
        p.addObject("[/PDF /Text]");
	p.addObject("<< /Type /Font\n/Subtype /Type1\n/Name /F1\n/BaseFont /Helvetica\n/Encoding /MacRomanEncoding\n>>");
	p.end(1);
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
	//outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");	
    };
    public void setOptionalFace(String name) {
	/* warning: keep in mind that setting style after setOptionalFace restores
	   the default font assigned by setFontFace, since in PS style of the font
	   is given by its name */
	if (inPath) outPS(" cp s\n"); inPath=false;
	lastFace=name;
	//outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
    public void setFontSize(int pt) {
	if (inPath) outPS(" cp s\n"); inPath=false;
        lastFontSize=pt;
	//outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
    void internal_setFontStyle(int attr) {
	lastFace=lastBaseFont+romAppendix; lastFontAttr=attr;
	if ((attr&FA_MASK_Type)==FA_Bold) lastFace=lastBaseFont+"-Bold";
	if ((attr&FA_MASK_Type)==FA_Ital) lastFace=lastBaseFont+"-"+italAppendix;
    };
    public void setFontStyle(int attr) {
	if (inPath) outPS(" cp s\n"); inPath=false;
	internal_setFontStyle(attr);
	//outPS("/"+lastFace+" findfont "+lastFontSize+" scalefont setfont\n");
    };
}
