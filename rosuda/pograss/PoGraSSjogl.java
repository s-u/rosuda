package org.rosuda.pograss;

import java.awt.*;

import net.java.games.jogl.*;
import net.java.games.jogl.util.*;

/** Portable Graphics SubSystem - OpenGL implementation using jogl classes
@version $Id$
*/
public class PoGraSSjogl extends PoGraSS
{
    /** associated graphics context */
    GL gl;
	GLU glu;
	GLUT glut;
	GLCanvas canvas;
	
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
    Font currentFont;
	float globalAlpha=1f;
	
	GLUquadric quad;
	
    public PoGraSSjogl(GL gl, GLU glu, GLCanvas canvas) { 
		this.gl=gl;
		this.glu=glu;
		this.canvas=canvas;
		this.glut=new GLUT();
		c=new Color[128]; cn=new String[128]; cs=0; fillSt=0; lineWidth=1;
        curFillC=Color.white; curPenC=Color.black; localLayerCache=paintLayer=-1;
        setFontStyle(lastFontAttr); // implicitly sets the font
        setColor(curPenC);
    }

	public void begin() {
		if (glu!=null)
			quad=glu.gluNewQuadric();
        curLayer=0;
	}

    public void end() {
		if (quad!=null && glu!=null) {
			glu.gluDeleteQuadric(quad);
			quad=null;
		}
	}

    Color getColor(String nam) {
		int i=0; while(i<cs) { if (cn[i].compareTo(nam)==0) return c[i]; i++; };
		return Color.black;
    }    
    
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
		}
    }
	
    public void defineColor(String nam, float r, float g, float b, float a) {
        if (cs<128) {
            cn[cs]=new String(nam);
            if (a<0f) a=0f;
            if (a>0.99f)
                c[cs]=new Color(r,g,b);
            else
                c[cs]=new Color(r,g,b,a);
            cs++;
        }
    }
	
    public void setColor(String nam, float alpha) {
        if (paintLayer==-1 || paintLayer==curLayer) {
            if (alpha>0.99f) { setColor(nam); return; } // if alpha is high, fall back to regular setColor for efficiency
            if (alpha<0f) alpha=0f;
            float[] rgba=getColor(nam).getRGBComponents(null);
            gl.glColor4f(rgba[0],rgba[1],rgba[2],alpha*globalAlpha);
        }
    }
	
	public void setColor(Color c) {
		curPenC=c;
		float[] rgba=c.getRGBComponents(null);
		gl.glColor4f(rgba[0],rgba[1],rgba[2],rgba[3]*globalAlpha);		
	}
	
    public void setColor(float r, float gr, float b, float a) {
        if (paintLayer==-1 || paintLayer==curLayer) {
			curPenC=new Color(r,gr,b,a*globalAlpha);
			gl.glColor4f(r,gr,b,a*globalAlpha);
		}
    }
    public void setColor(int R, int G, int B) {
		if (paintLayer==-1 || paintLayer==curLayer) setColor(curPenC=new Color(R,G,B));
	}
    public void setColor(String nam) { if (paintLayer==-1 || paintLayer==curLayer) setColor(getColor(nam)); };
    public void drawLine(int x1, int y1, int x2, int y2) {
		if (paintLayer==-1 || paintLayer==curLayer) {
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2i(x1,y1);
			gl.glVertex2i(x2,y2);
			gl.glEnd();
		}
	}
    public void moveTo(int x, int y) { if (paintLayer==-1 || paintLayer==curLayer)  { cx=x; cy=y; } };
    public void lineTo(int x, int y) { if (paintLayer==-1 || paintLayer==curLayer) {
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2i(cx,cy);
		gl.glVertex2i(x,y);
		gl.glEnd();
		cx=x; cy=y;
	} }
    public void drawRect(int x1, int y1, int x2, int y2) { if (paintLayer==-1 || paintLayer==curLayer) {
		gl.glBegin (GL.GL_LINE_LOOP);
        gl.glVertex2i (x1,y1);
        gl.glVertex2i (x1+x2,y1);
        gl.glVertex2i (x1+x2,y1+y2);
        gl.glVertex2i (x1,y1+y2);
        gl.glEnd();
	} }
    public void fillRect(int x1, int y1, int x2, int y2) { if (paintLayer==-1 || paintLayer==curLayer) {
		gl.glBegin (GL.GL_QUADS);
        gl.glVertex2i (x1,y1);
        gl.glVertex2i (x1+x2,y1);
        gl.glVertex2i (x1+x2,y1+y2);
        gl.glVertex2i (x1,y1+y2);
        gl.glEnd();
	} }
	
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
		// unsupported - i'm too lazy
		drawRect(x1,y1,x2,y2);
		//if (paintLayer==-1 || paintLayer==curLayer) g.drawRoundRect(x1,y1,x2,y2,dx,dy);
    }
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
		// unsupported - i'm too lazy
		fillRect(x1,y1,x2,y2);		
		//if (paintLayer==-1 || paintLayer==curLayer) g.fillRoundRect(x1,y1,x2,y2,dx,dy);
    }
    public void drawOval(int x, int y, int rx, int ry) { if (paintLayer==-1 || paintLayer==curLayer) {
	} }
    public void fillOval(int x, int y, int rx, int ry) { if (paintLayer==-1 || paintLayer==curLayer) {
		if (x<0) rx=-rx; if (ry<0) ry=-ry;
		if (rx==0 || ry==0) return;
		if (quad!=null) {
			gl.glPushMatrix();
			gl.glTranslatef((float)x+(((float)rx)*0.5f),(float)y+(((float)ry)*0.5f),0f);
			if (rx!=ry)
				gl.glScalef(1f,((float)ry)/((float)rx),1f);
			glu.gluDisk(quad, 0, ((float)rx)*0.5f, 3+rx, 1);
			gl.glPopMatrix();
		}
	} }
    public void drawString(String txt, int x, int y) { if (paintLayer==-1 || paintLayer==curLayer) {
		//int fsize = f.getSize();
		//float scale = (float)(1.*fsize/fh);
		//if (f.isBold())
		//	gl.glLineWidth(2);

		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		//gl.glScalef(scale,-scale,scale);
		gl.glScalef(.07f,-.08f,.1f);
		glut.glutStrokeString(gl,GLUT.STROKE_ROMAN,txt);
		gl.glPopMatrix();
		//gl.glLineWidth(1);
		
		//canvas.getGraphics().drawString(txt, x, y);
	} }
	
    public void drawString(String txt, int x, int y, int a) {
		if (paintLayer==-1 || paintLayer==curLayer) {
			if ((a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Right || (a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Center) {
				//FontMetrics fm=canvas.getGraphics().getFontMetrics();
				//int sw=fm.stringWidth(txt);
				int sw=5*txt.length();
				if ((a&PoGraSS.TA_MASK_Or)==PoGraSS.TA_Center)
					sw/=2;
				drawString(txt,x-sw,y);
			} else
				drawString(txt,x,y);
		}
    }
    public void drawString(String txt, int x, int y, double ax, double ay) {
		if (paintLayer==-1 || paintLayer==curLayer) {
			//FontMetrics fm=g.getFontMetrics();
			int dx=txt.length()*5; //fm.stringWidth(txt);
			int dy=10; //fm.getHeight();
			dx=(int)(((double)dx)*ax);
			dy=(int)(((double)dy)*ay);
			drawString(txt,x-dx,y+dy);
		}
    }
		
	public void setLineWidth(int w) {
		super.setLineWidth(w);
		gl.glLineWidth(w);
	}

    public void drawPolygon(int[] x, int[] y, int pts, boolean closed) {
		if (paintLayer==-1 || paintLayer==curLayer) {
			gl.glBegin (GL.GL_LINE_LOOP);
			int i=0;
			while (i<pts) {
				gl.glVertex2f (x[i],y[i]);
				i++;
			}
			gl.glEnd();
		}	
    }
	
    public void fillPolygon(int[] x, int[] y, int pts) {
		if (paintLayer==-1 || paintLayer==curLayer) {
		}
    }
	
    public void setFontFace(int face) {
		lastFace="SansSerif";
		if (face==PoGraSS.FF_Serif) lastFace="Serif";
		if (face==PoGraSS.FF_Mono) lastFace="Monospaced";
		lastFont=face;
		//g.setFont(currentFont=new Font(lastFace,lastFontAttr,(int)(0.5+lastFontSize)));
    }
    public void setOptionalFace(String name) {
		lastFace=name;
		//g.setFont(currentFont=new Font(lastFace,lastFontAttr,(int)(0.5+lastFontSize)));
    }
    public void setFontSize(double pt) {
        lastFontSize=pt;
		//g.setFont(currentFont=new Font(lastFace,lastFontAttr,(int)(0.5+lastFontSize)));
    }
    public void setFontStyle(int attr) {
		lastFontAttr=Font.PLAIN;
		if ((attr&PoGraSS.FA_Ital)>0) lastFontAttr|=Font.ITALIC;
		if ((attr&PoGraSS.FA_Bold)>0) lastFontAttr|=Font.BOLD;
		//g.setFont(currentFont=new Font(lastFace,lastFontAttr,(int)(0.5+lastFontSize)));
    }
	
    public void nextLayer() {
        curLayer++;
    }
		
    public int getWidthEstimate(String s) {
        //Rectangle r=g.getFontMetrics().getStringBounds(s,g).getBounds();
        //return r.width;
		return s.length()*10;
    }
    public int getHeightEstimate(String s) {
        //Rectangle r=g.getFontMetrics().getStringBounds(s,g).getBounds();
        //return r.height;
		return 14;
    }
	
    public void setGlobalAlpha(float alpha) {
		globalAlpha=alpha;
        //((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    
    public void resetGlobalAlpha() {
		globalAlpha=1f;
        //((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}
