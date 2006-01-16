package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

/**
 * Extends the {@link Canvas} class by adding multi-buffering support based on layers.
 * @version $Id$
 */
public abstract class LayerCanvas {
    public PlotComponent pc;
    public int graphicsEngine;
    
    /** off-screen buffer descriptor */
    Graphics offgc;
    /** off-screen images. they're not overlaid but hierarchical. that means that the top layer is actually drawn
     * others are used for caching if only top layers are modified. Keep this in mind when deginging your layer layout,
     * modifications in bottom layers cause redraw of the top ones. */
    Image offscreen[] = null;
    /** geometry of the off-screen buffer */
    Dimension offsd = null;
    /** layers */
    int layers=0;
    /** layer to start update with */
    int updateRoot;
    /** previous update root */
    int prevUpdateRoot=0;
    
    /** creates a Graphics-based driver consisting of layers to draw on, using ps plot component as its target, specified number of layers. If pc is set to <code>null</code> then a plot component of the default type (see Common.defaultPlotComponentType) is created automatically. */
    public LayerCanvas(PlotComponent pc, final int Layers) {
        if (pc==null) pc=PlotComponentFactory.createPlotComponent();
        pc.initializeLayerCanvas(this);
        this.pc = pc;
        graphicsEngine = pc.getGraphicsEngine();
        layers=Layers;
        offscreen=new Image[layers];
        for(int i=0;i<layers;i++) offscreen[i]=null;
        updateRoot=0;
        if (Global.DEBUG>0) System.out.println("LayerCanvas: layers="+layers);
    }
    
    /** creates 1 layer as default */
    public LayerCanvas(final PlotComponent pc) {
        this(pc, 1);
    }
    
    /** creates default plot component and initializes layers */
    public LayerCanvas(final int layers) {
        this(null, layers);
    }
    
    /** set update root layer, i.e. the first layer that has to be updated via {@link #paintLayer}.
     * Note that after resize full repaint of all layers is done but updateRoot is not changed.
     * It is safe (and sensible) to set the update root higher than the last layer; this will cause full repaint only
     * on resize. such behavior is useful when the underlying paint doesn't change except for resizes.
     * common practice is to the update root to # of layers at the end of the paintLayer function and use
     * setUpdateRoot in the remaining program only where content of the painted are has to be explicitely changed.
     * also note that changing update root during the update itself has no effect on the current update.
     * @param ur root layer, i.e. the layer to start repaining from */
    public void setUpdateRoot(final int ur) {
        prevUpdateRoot=updateRoot; updateRoot=ur;
    }
    
    /** restore update root to previous setting. usual procedure is to used following sequence: setUpdateRoot; repaint; restoreUpdateRoot; */
    public void restoreUpdateRoot() {
        updateRoot=prevUpdateRoot; prevUpdateRoot=0; // two subsequent calls to restore will cause updateRoot to be set to 0
    }
    
    /**
     * Like <code>Canvas</code>' Update, but implements double-buffering.
     * we use update instead of paint to prevent automatic background clearing.
     *
     * @param <code>Graphics</code> context used for update
     */
    public void update(final Graphics g) {
        if(g==null) return;
        final Dimension d = pc.getSize();
        
        int firstPaintLayer=updateRoot;
        if (Global.forceAntiAliasing) {
            final Graphics2D g2=(Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        if (Global.DEBUG>0) System.out.println("LayerCanvas: update, layers="+layers+", root="+updateRoot);
        
        // sanity check (sounds wierd, but JDK really delivers negative sizes sometimes)
        if (d.width<1 || d.height<1) return;
        // yet another sanity check - some systems (e.g. X with Xinerama enabled) wrap around
        // the 0 boundary resulting in huge numbers;
        if (d.width>2000 || d.height>2000) {
            d.width=(d.width>2000)?640:d.width;
            d.height=(d.height>2000)?600:d.height;
        };
        
        final Stopwatch sw=new Stopwatch();
        // we will re-create the off-screen object only if the canvas was resized
        if ((offsd==null)||(offsd.width!=d.width)||(offsd.height!=d.height)) {
            if (Global.DEBUG>0) System.out.println("LayerCanvas: update, need to re-create offscreen buffers ("+d.width+":"+d.height+")");
            // draw the old image - after resize the background is cleared automatically
            // so in order to reduce flickering draw the old image until the new one is generated
            if (offscreen[layers-1]!=null) g.drawImage(offscreen[layers-1], 0, 0, pc.getComponent());
            // create the offscreen buffer
            for(int i=0;i<layers;i++) offscreen[i] = pc.createImage(d.width, d.height);
            offsd=d;
            firstPaintLayer=0; // after resize we need to repaint them all
            setUpdateRoot(0);
            if (Global.PROFILE>0) sw.profile("LayerCanvas.update.recreateOffscreen");
        };
        
        // clear the image
        if (firstPaintLayer==0) { // total repaint, i.e. clear the layer 0 also
            offgc = offscreen[0].getGraphics();
            
            if (offgc!=null) { /* insane sanity checks, because sometimes it happens that
                                  the graphics subsystem returns null */
                offgc.setFont(Common.defaultFont);
                if (Global.forceAntiAliasing) {
                    final Graphics2D g2=(Graphics2D) offgc;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                }
                if (Global.useAquaBg) {
                    offgc.setColor(Color.white);
                    offgc.fillRect(0, 0, d.width, d.height);
                    
                    
                    offgc.setColor(Common.aquaBgColor);
                    int y = 0;
                    while (y<d.height-2) {
                        offgc.fillRect(0,y,d.width,2); y+=4;
                    }
                } else {
                    final Color bg=Common.backgroundColor;
                    //Color bg=getBackground();
                    offgc.setColor(bg==null?Color.white:bg);
                    offgc.fillRect(0, 0, d.width, d.height);
                }
                
                final Color fg=pc.getForeground();
                if (offgc!=null) offgc.setColor(fg==null?Color.black:fg);
            }
            if (Global.PROFILE>0) sw.profile("LayerCanvas.update.clearLayer0");
        }
        
        int l=firstPaintLayer;
        while(l<layers) {
            offgc=offscreen[l].getGraphics();
            if (l>0) offgc.drawImage(offscreen[l-1],0,0,pc.getComponent());
            paintLayer(offgc,l);
            l++;
        }
        if (Global.PROFILE>0) sw.profile("LayerCanvas.update.constructLayers");
        
        // do normal redraw
        // transfer offscreen to window, last layer should have the correct image
        g.drawImage(offscreen[l-1],0,0,pc.getComponent());
        if (Global.PROFILE>0) sw.profile("LayerCanvas.update.paintLayers");
    };
    
    /** normally we would not use paint at all, but sometimes paint is called
     * instead of update, so we want to force {@link #update} instead */
    public void paint(final Graphics g) {
        update(g);
    };
    
    public void repaint() {
        update(pc.getGraphics());
    }
    
    /**
     * Override with your own paint method.
     *
     * @param g is <code>Graphics</code> context you can use for painting.
     * @param layer is the currently processed layer
     */
    public abstract void paintLayer(Graphics g, int layer);
    
    public Component getComponent() { return pc.getComponent(); }
    
    public void setSize(final int w, final int h) {
        pc.setSize(w, h);
    }
    
    public void setSize(final Dimension d) {
        pc.setSize(d.width, d.height);
    }
};

