package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;
import net.java.games.jogl.*;

public class JOGLGraphicsDevice implements GraphicsDevice, GLEventListener {

    /** layers */
    int layers=0;
    /** layer to start update with */
    int updateRoot;
    /** previous update root */
    int prevUpdateRoot=0;
	private GLCanvas comp;
	private PlotComponent pcowner;
	
	boolean inProgress=false;
	
	boolean usingIPlots = false;
	
	private int viewportWidth, viewportHeight;
    private int[] canvasTexture;
    private boolean[] layerDisplayed;
	
	public JOGLGraphicsDevice(int _layers, boolean useIPlots) {
		usingIPlots=useIPlots;
		if(usingIPlots) System.out.println("using IPlots");
		GLCapabilities glcaps=new GLCapabilities();
		glcaps.setHardwareAccelerated(true);
		glcaps.setDoubleBuffered(true);
		comp=GLDrawableFactory.getFactory().createGLCanvas(glcaps);
		comp.addGLEventListener(this);
		layers=_layers;
		
		for(int i=0; i<layers; i++) {
			canvasTexture = new int[layers];
			layerDisplayed = new boolean[layers];
		}
        updateRoot=0;
        if (Global.DEBUG>0) System.out.println("JOGLGraphicsDevice: layers="+layers);
	}
	
	public JOGLGraphicsDevice(int _layers) {
		this(_layers,false);
	}
	
	public JOGLGraphicsDevice(boolean useIPlots) {
		this(1,useIPlots);
	}
	
	public JOGLGraphicsDevice() {
		this(1,false);
	}
	
    /** set update root layer, i.e. the first layer that has to be updated via {@link #paintLayer}.
     * Note that after resize full repaint of all layers is done but updateRoot is not changed.
     * It is safe (and sensible) to set the update root higher than the last layer; this will cause full repaint only
     * on resize. such behavior is useful when the underlying paint doesn't change except for resizes.
     * common practice is to the update root to # of layers at the end of the paintLayer function and use
     * setUpdateRoot in the remaining program only where content of the painted are has to be explicitely changed.
     * also note that changing update root during the update itself has no effect on the current update.
     * @param ur root layer, i.e. the layer to start repaining from */
    public void setUpdateRoot(int ur) {
        prevUpdateRoot=updateRoot; updateRoot=ur;
    }
    
    /** restore update root to previous setting. usual procedure is to used following sequence: setUpdateRoot; repaint; restoreUpdateRoot; */
    public void restoreUpdateRoot() {
        updateRoot=prevUpdateRoot; prevUpdateRoot=0; // two subsequent calls to restore will cause updateRoot to be set to 0
    }
	
    public void paintLayer(GLDrawable drawable, int layer) {
    	if(pcowner==null) return;
    	GL gl=drawable.getGL();
    	GLU glu=drawable.getGLU();
        if (inProgress) return; /* avoid recursions */
        inProgress=true;
        final PoGraSSjogl p=new PoGraSSjogl(gl, glu, comp, layer);
//        p.setTitle(pcowner.desc);
        pcowner.beginPaint(p);
        pcowner.paintPoGraSS(p);
        pcowner.endPaint(p);
        inProgress=false;
    }
    
    public void paintLayer(int layer) {
    	paintLayer(comp,layer);
    }
    
	public void repaint() {
//		System.out.println("GraphicsDevice: repaint()");
//        update(comp.getGraphics());
		comp.repaint();
	}
	
    public void paint(Graphics g) {
	// what should we do here?
    }

    public void update(Graphics g) {
	// what should we do here?
    }

	public int createTexture(int w, int h, GL gl) {
		int[] textureHandles = new int[1];
		gl.glGenTextures(1,textureHandles);
		int textureHandle = textureHandles[0];
		
		int textureData[] = new int[w*h*3];
		
		gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
		gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT,textureHandle);
		// this one is funny: internalformat RGB16 works faster then RGB
        gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, 3, w, h, 0, GL.GL_RGB16, GL.GL_UNSIGNED_INT, textureData);
        gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_EXT, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_EXT, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glDisable(GL.GL_TEXTURE_RECTANGLE_EXT);
		
		return textureHandle;
	}

	public void copyCanvas(int textureHandle, int width, int height, GL gl) {
		gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
	    gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, textureHandle);
//	    gl.glCopyTexSubImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, 0, 0, 0, 0, width, height);
	    // for speed reasons we need internalformat to be only RGB
	    gl.glCopyTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGB, 0, 0, width, height, 0);
	    gl.glDisable(GL.GL_TEXTURE_RECTANGLE_EXT);
	   }
	
	public void restoreCanvas(int textureHandle, int width, int height, GL gl) {
	    gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
	    gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, textureHandle);
        viewOrtho(gl);
        gl.glColor3f(1,1,1);
        gl.glBegin(GL.GL_QUADS);
        {
        	gl.glTexCoord2f(width, 0);
	        gl.glVertex2f(0, 0);
	        gl.glTexCoord2f(width, height);
	        gl.glVertex2f(0, 1.0f);
	        gl.glTexCoord2f(0, height);
	        gl.glVertex2f(1.0f, 1.0f);
	        gl.glTexCoord2f(0, 0);
	        gl.glVertex2f(1.0f, 0);
	    }
	    gl.glEnd();
	    viewPerspective(gl);
	    gl.glDisable(GL.GL_TEXTURE_RECTANGLE_EXT);
	}
	 
	private void viewOrtho(GL gl) {
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	    gl.glOrtho(1, 0, 0, 1, -1, 1000000);
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();
	}
	
	private void viewPerspective(GL gl) {
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPopMatrix();
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glPopMatrix();
	}
	
	
	public void init (GLDrawable drawable) {
	    System.out.println ("init()");
	    GL gl = drawable.getGL(); 

//	    int status;
//	    String str;
//	    status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
//	    switch(status) {
//	    	case GL.GL_FRAMEBUFFER_COMPLETE_EXT : str = "OK"; 
//	    	case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT : str = "not OK";
//	    	default : str = "maybe";
//	    }
//	    System.out.println(str);
	    
		gl.glEnable (GL.GL_LINE_SMOOTH);
		gl.glHint (GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
			
		gl.glEnable ( GL.GL_BLEND );
		gl.glBlendFunc ( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );		
		
//	    gl.glEnable(GL.GL_DEPTH_TEST);
//	    gl.glDepthFunc(GL.GL_LEQUAL);
	    
	    float[] rgba=comp.getBackground().getRGBComponents(null);
		gl.glClearColor( rgba[0], rgba[1], rgba[2], rgba[3] ); 
		gl.glColor3f( 0.0f, 0.0f, 0.0f );
	}
	    
	/** Called to indicate the drawing surface has been moved and/or resized
	*/
	public void reshape (GLDrawable drawable,int x,int y,int width,int height) {
	    GL gl = drawable.getGL(); 
	    GLU glu = drawable.getGLU(); 
	    long start, stop;
		start = System.currentTimeMillis();
//		System.out.println("reshape (gl="+gl+", w="+width+", h="+height+")");
			
	    // save size for viewport reset
	    viewportWidth = width;
	    viewportHeight = height;
		
	    float[] rgba=comp.getBackground().getRGBComponents(null);
		gl.glClearColor( rgba[0], rgba[1], rgba[2], rgba[3] ); 
		
	    gl.glMatrixMode( GL.GL_PROJECTION );  
	    gl.glLoadIdentity(); 
//	    glu.gluOrtho2D (0, width, 0, height);
	    gl.glOrtho(0,width,0,height,0,10);
	    gl.glViewport( 0, 0, viewportWidth, viewportHeight ); 
		float mmx[] = { 1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f };
		gl.glTranslatef(0f, (float)(height), 0f);
		gl.glMultMatrixf(mmx);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
	    
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		for(int i=0;i<layers;i++) {
			canvasTexture[i] = createTexture(width,height,gl);
			layerDisplayed[i] = false;
		}
		stop = System.currentTimeMillis();
		System.out.println("Time for reshape(): " + (stop-start));
    }

	    
    /** Called by drawable to initiate drawing 
	*/
    public void display (GLDrawable drawable) {
//    	System.out.println("updateRoot: " + updateRoot);
    	if(!usingIPlots) standardDisplay(drawable);
    		else iplotsDisplay(drawable);
    }
	    
//	 working in more cases then iplotsDisplay, but a little bit slower
    public void standardDisplay(GLDrawable drawable) {
    	GL gl = drawable.getGL();
		long start,stop;
		start = System.currentTimeMillis();
		if(updateRoot==0) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        // this one isn't really fast, but initializes all layers with lowerst layer
			for(int k=0; k<layers; k++) {
				paintLayer(drawable,k);
				copyCanvas(canvasTexture[k],viewportWidth,viewportHeight,gl);
			}
		} else {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
        	restoreCanvas(canvasTexture[updateRoot-1],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,updateRoot);
			copyCanvas(canvasTexture[updateRoot],viewportWidth,viewportHeight,gl);
		}
		stop = System.currentTimeMillis();
//		System.out.println("Time for standardDisplay(): "+(stop-start));
    }
    
    
//	 iplotsDisplay is tuned for iPlots
    public void iplotsDisplay(GLDrawable drawable) {
    	GL gl = drawable.getGL();
    	long start, stop;
    	start = System.currentTimeMillis();
		if(updateRoot ==0) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        for(int k=0; k<4; k++) {
				paintLayer(drawable,k);
				copyCanvas(canvasTexture[k],viewportWidth,viewportHeight,gl);
			}
			layerDisplayed[0] = true;
		} else if(updateRoot == 1) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
			paintLayer(drawable,1);
			copyCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
			layerDisplayed[1] = true;
		} else if(updateRoot == 2) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        if(layerDisplayed[1])
	        	restoreCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
	        else
	        	restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,2);
		} else if(updateRoot == 3) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        if(layerDisplayed[1])
	        	restoreCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
	        else
	        	restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,3);
			copyCanvas(canvasTexture[3],viewportWidth,viewportHeight,gl);
			
			/** what usage has the following layer? */
		} else if(updateRoot == 4) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        if(layerDisplayed[1])
	        	restoreCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
	        else if(layerDisplayed[3]) 
	        	restoreCanvas(canvasTexture[3],viewportWidth,viewportHeight,gl);
	        else
	        	restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,4);
			copyCanvas(canvasTexture[4],viewportWidth,viewportHeight,gl);
		}
		stop = System.currentTimeMillis();
//		if(stop-start!=0) System.out.println("Time for iplotsDisplay(): "+(stop-start));
    }
    
    /** Called by drawable to indicate mode or device has changed
	*/
   public void displayChanged (GLDrawable drawable,boolean modeChanged,boolean deviceChanged) {
       System.out.println ("displayChanged()");
    }



	
	// intersection with PlotComponent
	public void setPCOwner(PlotComponent pc) {pcowner=pc;}
	public int getGrDevID() {return AWTGrDevID;}
	
	// component methods
	public Component getComponent() {return comp;}
	public Rectangle getBounds() {return comp.getBounds();}
	public void setSize(int w, int h) {comp.setSize(w,h);}
	public Dimension getSize() {return comp.getSize();}
	public void setBackground(Color c) {comp.setBackground(c);}
	public void addMouseListener(MouseListener l) {comp.addMouseListener(l);}
	public void addMouseMotionListener(MouseMotionListener l) {comp.addMouseMotionListener(l);}
	public void addKeyListener(KeyListener l) {comp.addKeyListener(l);}
	public Point getLocation() {return comp.getLocation();}
	public void setCursor(Cursor c) {comp.setCursor(c);}
	public void setSize(Dimension d) {comp.setSize(d);}
	public int getWidth() {return comp.getWidth();}
	public int getHeight() {return comp.getHeight();}
	public Container getParent() {return comp.getParent();}
}
