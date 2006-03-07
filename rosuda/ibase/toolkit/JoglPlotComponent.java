package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.rosuda.ibase.Common;
import org.rosuda.pograss.*;
import org.rosuda.util.Global;
import org.rosuda.util.Stopwatch;

import net.java.games.jogl.*;

public class JoglPlotComponent implements PlotComponent, GLEventListener {

	private GLCanvas comp;
	private GLCapabilities glcaps;
	protected PGSCanvas pgsCanvas;
    
    int[] canvasTexture = new int[5]; // mach das allgemeiner
    boolean[] layerDisplayed = new boolean[5];
    boolean usingIPlots = false; 
	
	public JoglPlotComponent(boolean useIPlots) {
		this.usingIPlots = useIPlots;
		glcaps = new GLCapabilities();
		glcaps.setHardwareAccelerated(true);
		glcaps.setDoubleBuffered(true);
//		glcaps.setOffscreenRenderToTexture(true);
		comp = GLDrawableFactory.getFactory().createGLCanvas(glcaps);
		comp.addGLEventListener(this);
		comp.canCreateOffscreenDrawable();
	}
	
	public JoglPlotComponent() {
		this(false);
	}
	
	// do not use this method
	public Component getComponent() {
		return comp;
	}
	
	public void initializeGraphics(Window w) {
	}
	
	public void initializeLayerCanvas(LayerCanvas l) {
		pgsCanvas = (PGSCanvas)l;
	}

	public int getGraphicsEngine() {
		return OPENGL;
	}
	
	public PlotComponent getAssociatedPlotComponent() {
		return new AwtPlotComponent();
	}
	
	// redirected methods
	public void repaint() {
		comp.repaint();
    	
	}
	public void paint(PoGraSS p) {
		update(p);
	}
	public void update(PoGraSS p) {

	}
	public void setCursor(Cursor cursor) {
		comp.setCursor(cursor);
	}
	public void setBackground(Color c) {
		comp.setBackground(c);
	}
	public void addMouseMotionListener(MouseMotionListener l) {
		comp.addMouseMotionListener(l);
	}
	public void addMouseListener(MouseListener l) {
		comp.addMouseListener(l);
	}
	public void addKeyListener(KeyListener l) {
		comp.addKeyListener(l);
	}
	public Rectangle getBounds(){
		return comp.getBounds();
	}
	public Point getLocation() {
		return comp.getLocation();
	}
	public void setSize(int width, int height) {
		comp.setSize(width,height);
	}
	public void setSize(Dimension d) {
		comp.setSize(d);
	}
	public Dimension getSize() {
		return comp.getSize();
	}
	public Image createImage(int width, int height) {
		return comp.createImage(width,height);
	}
	public Color getForeground() {
		return comp.getForeground();
	}
	public Color getBackground() {
		return comp.getBackground();
	}
	public Graphics getGraphics() {
		return comp.getGraphics();
	}
	public Container getParent() {
		return comp.getParent();
	}
	public int getWidth() {
		return comp.getWidth();
	}
	public int getHeight() {
		return comp.getHeight();
	}
	// additions to SWING: do nothing in AWT
	public void setPreferredSize(Dimension d) {}
	public void setMinimumSize(Dimension d) {}
	public void setMaximumSize(Dimension d) {}
	public void setToolTipText(String s) {}
	
	// additions to JOGL: do nothing in AWT and SWING
	public void addGLEventListener(GLEventListener l) {
		comp.addGLEventListener(this);
	}
	
	int viewportWidth;
    int viewportHeight;
		
	public int createTexture(int w, int h, GL gl) {
		int[] textureHandles = new int[1];
		gl.glGenTextures(1,textureHandles);
		int textureHandle = textureHandles[0];
		
		int textureData[] = new int[w*h*3];
		
		gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
		gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT,textureHandle);
        gl.glTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, 3, w, h, 0, GL.GL_RGB, GL.GL_UNSIGNED_INT, textureData);
        gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_EXT, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_RECTANGLE_EXT, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glDisable(GL.GL_TEXTURE_RECTANGLE_EXT);
		
		return textureHandle;
	}
	
	public void copyCanvas(int textureHandle, int width, int height, GL gl)
	   {
	       gl.glEnable(GL.GL_TEXTURE_RECTANGLE_EXT);
	       gl.glBindTexture(GL.GL_TEXTURE_RECTANGLE_EXT, textureHandle);
//	       gl.glCopyTexSubImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, 0, 0, 0, 0, width, height);
	       gl.glCopyTexImage2D(GL.GL_TEXTURE_RECTANGLE_EXT, 0, GL.GL_RGBA16, 0, 0, width, height, 0);
	       gl.glDisable(GL.GL_TEXTURE_RECTANGLE_EXT);
	   }
	
	 public void restoreCanvas(int textureHandle, int width, int height, GL gl)
	   {
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

	   private void viewOrtho(GL gl)
	   {
	       gl.glMatrixMode(GL.GL_PROJECTION);
	       gl.glPushMatrix();
	       gl.glLoadIdentity();
	       gl.glOrtho(1, 0, 0, 1, -1, 1000000);
	       gl.glMatrixMode(GL.GL_MODELVIEW);
	       gl.glPushMatrix();
	       gl.glLoadIdentity();
	   }

	   private void viewPerspective(GL gl)
	   {
	       gl.glMatrixMode(GL.GL_PROJECTION);
	       gl.glPopMatrix();
	       gl.glMatrixMode(GL.GL_MODELVIEW);
	       gl.glPopMatrix();
	   }

    
    
	public void init (GLDrawable drawable) {
//        System.out.println ("init()");
        GL gl = drawable.getGL(); 
		
//		gl.glEnable (GL.GL_LINE_SMOOTH);
//		gl.glHint (GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		
		gl.glEnable ( GL.GL_BLEND );
		gl.glBlendFunc ( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );		
		
//    	gl.glEnable(GL.GL_DEPTH_TEST);
//    	gl.glDepthFunc(GL.GL_LEQUAL);

        
		gl.glClearColor( 1.0f, 1.0f, 0f, 1.0f ); 
        gl.glColor3f( 0.0f, 0.0f, 0.0f ); 
       
	}
    
    /** Called to indicate the drawing surface has been moved and/or resized
		*/
    public void reshape (GLDrawable drawable,
                         int x,
                         int y,
                         int width,
                         int height) {
        GL gl = drawable.getGL(); 
        GLU glu = drawable.getGLU(); 
		//System.out.println("reshape (gl="+gl+", w="+width+", h="+height+")");
		
        // save size for viewport reset
        viewportWidth = width;
        viewportHeight = height;
		
		float[] rgba=getBackground().getRGBComponents(null);
		gl.glClearColor( rgba[0], rgba[1], rgba[2], rgba[3] ); 

        gl.glMatrixMode( GL.GL_PROJECTION );  
        gl.glLoadIdentity(); 
//        glu.gluOrtho2D (0, width, 0, height);
        gl.glOrtho(0,width,0,height,0,10);
        gl.glViewport( 0, 0, viewportWidth, viewportHeight ); 
		float mmx[] = { 1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f };
		gl.glTranslatef(0f, (float)(height), 0f);
		gl.glMultMatrixf(mmx);

		gl.glMatrixMode(GL.GL_MODELVIEW);
    	gl.glLoadIdentity();
    	
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		for(int i=0;i<5;i++) {
			canvasTexture[i] = createTexture(width,height,gl);
			layerDisplayed[i] = false;
		}


    }

    
    /** Called by drawable to initiate drawing 
		*/
    public void display (GLDrawable drawable) {
    	
    	if(!usingIPlots) standardDisplay(drawable);
    		else iplotsDisplay(drawable);

    }
    
// working in more cases then iplotsDisplay, but a little bit slower
    public void standardDisplay(GLDrawable drawable) {
    	GL gl = drawable.getGL();
		long start,stop;
		if(pgsCanvas.updateRoot ==0) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        // this one isn't really fast, but initializes all layers with lowerst layer
			for(int k=0; k<pgsCanvas.layers; k++) {
				paintLayer(drawable,k);
				copyCanvas(canvasTexture[k],viewportWidth,viewportHeight,gl);
			}
		} else {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
        	restoreCanvas(canvasTexture[pgsCanvas.updateRoot-1],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,pgsCanvas.updateRoot);
			copyCanvas(canvasTexture[pgsCanvas.updateRoot],viewportWidth,viewportHeight,gl);
		}
    }
    
    
// iplotsDisplay is tuned for iPlots
    public void iplotsDisplay(GLDrawable drawable) {
    	GL gl = drawable.getGL();
		if(pgsCanvas.updateRoot ==0) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
			paintLayer(drawable,0);
			copyCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
			for(int k=1; k<4; k++) {
				paintLayer(drawable,k);
				// the following line is not necessary
				// there are just some little problems while marking
				// have to be solved
//				copyCanvas(canvasTexture[k],viewportWidth,viewportHeight,gl);
			}
			layerDisplayed[0] = true;
		} else if(pgsCanvas.updateRoot == 1) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
			paintLayer(drawable,1);
			copyCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
			layerDisplayed[1] = true;
		} else if(pgsCanvas.updateRoot == 2) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        if(layerDisplayed[1])
	        	restoreCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
	        else
	        	restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,2);
		} else if(pgsCanvas.updateRoot == 3) {
	        gl.glClear (GL.GL_COLOR_BUFFER_BIT);
	        if(layerDisplayed[1])
	        	restoreCanvas(canvasTexture[1],viewportWidth,viewportHeight,gl);
	        else
	        	restoreCanvas(canvasTexture[0],viewportWidth,viewportHeight,gl);
	        paintLayer(drawable,3);
			copyCanvas(canvasTexture[3],viewportWidth,viewportHeight,gl);
			
			/** what usage has the following layer? */
		} else if(pgsCanvas.updateRoot == 4) {
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
    }
 // should draw offline   
    public void paintLayer(GLDrawable drawable, int layer) {
        GL gl = drawable.getGL();
        GLU glu = drawable.getGLU();

        gl.glMatrixMode (GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glColor3f( 0.0f, 0.0f, 0.0f );

        pgsCanvas.inProgress=true;
		PoGraSSjogl p=new PoGraSSjogl(gl, glu, (GLCanvas)getComponent(),layer);
		//PoGraSSgraphics p=new PoGraSSgraphics(new render.jogl.JoglGraphics(gl, glu));
		p.setTitle(pgsCanvas.desc);
		pgsCanvas.beginPaint(p);
		pgsCanvas.paintPoGraSS(p);
		pgsCanvas.endPaint(p);
		p=null;
		pgsCanvas.inProgress=false;
		
    }
    
    /** Called by drawable to indicate mode or device has changed
		*/
    public void displayChanged (GLDrawable drawable,
                                boolean modeChanged,
                                boolean deviceChanged) {
        System.out.println ("displayChanged()");
    }
    

    public Window getParentWindow() {
		//System.out.println("JoglPlotComponent["+this+"].getParentWindow().comp="+comp);
		Container p = getParent();
		//System.out.println("  container: "+p);
		while (p!=null && !(p instanceof Window)) {
			//System.out.println("  container: "+p);
			p=p.getParent();
		}
		return (Window)p;
	}
    

}