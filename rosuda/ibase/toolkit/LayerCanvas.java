import java.awt.*;

/**
 * Extends the {@link Canvas} class by adding multi-buffering support based on layers.
 * @version $Id$
 */
public abstract class LayerCanvas extends Canvas
{
    /** off-screen buffer descriptor */
    Graphics offgc;
    /** off-screen images. they're not overlaid but hierarchical. that means that the top layer is actually drawn
        others are used for caching if only top layers are modified. Keep this in mind when deginging your layer layout,
        modifications in bottom layers cause redraw of the top ones. */
    Image offscreen[] = null;
    /** geometry of the off-screen buffer */
    Dimension offsd = null;
    /** layers */
    int layers=0;
    /** layer to start update with */
    int updateRoot;
    /** previous update root */
    int prevUpdateRoot=0;
    
    /** creates Layers layers */
    public LayerCanvas(int Layers) {
        layers=Layers;
        offscreen=new Image[layers];
        for(int i=0;i<layers;i++) offscreen[i]=null;
        updateRoot=0;
        if (Common.DEBUG>0) System.out.println("LayerCanvas: layers="+layers);
    }

    /** creates 1 layer as default */
    public LayerCanvas() {
        this(1);
    }

    /** set update root layer. note that after resize full repaint of all layers is done but updateRoot is not changed 
        @param ur root layer, i.e. the layer to start repaining from */
    public void setUpdateRoot(int ur) {
        prevUpdateRoot=updateRoot; updateRoot=ur;
        if (Common.DEBUG>0) System.out.println("LayerCanvas: setUpdateRoot("+ur+")");
    }

    /** restore update root to previous setting. usual procedure is to used following sequence: setUpdateRoot; repaint; restoreUpdateRoot; */
    public void restoreUpdateRoot() {
        updateRoot=prevUpdateRoot; prevUpdateRoot=0; // two subsequent calls to restore will cause updateRoot to be set to 0
        if (Common.DEBUG>0) System.out.println("LayerCanvas: restoreUpdateRoot to "+updateRoot);
    }
    
    /**
     * Like <code>Canvas</code>' Update, but implements double-buffering.
     * we use update instead of paint to prevent automatic background clearing.
     *
     * @param <code>Graphics</code> context used for update
     */
    public void update(Graphics g)
    {
        Dimension d = getSize();
	Image curimg=null;
        int firstPaintLayer=updateRoot;

        if (Common.DEBUG>0) System.out.println("LayerCanvas: update, layers="+layers+", root="+updateRoot);        
        
	// sanity check (sounds wierd, but JDK really delivers negative sizes sometimes)
	if (d.width<1 || d.height<1) return;
	// yet another sanity check - some systems (e.g. X with Xinerama enabled) wrap around
	// the 0 boundary resulting in huge numbers;
	if (d.width>2000 || d.height>2000) {
	    d.width=(d.width>2000)?640:d.width;
	    d.height=(d.height>2000)?600:d.height;
	};

        Stopwatch sw=new Stopwatch();
        // we will re-create the off-screen object only if the canvas was resized
	if ((offsd==null)||(offsd.width!=d.width)||(offsd.height!=d.height)) {
            if (Common.DEBUG>0) System.out.println("LayerCanvas: update, need to re-create offscreen buffers ("+d.width+":"+d.height+")");
            // draw the old image - after resize the background is cleared automatically
            // so in order to reduce flickering draw the old image until the new one is generated
            if (offscreen[layers-1]!=null) g.drawImage(offscreen[layers-1], 0, 0, this);
	    // create the offscreen buffer
            for(int i=0;i<layers;i++)
                offscreen[i] = createImage(d.width, d.height);
            offsd=d;
            firstPaintLayer=0; // after resize we need to repaint them all
            if (Common.DEBUG>1) sw.profile("LayerCanvas.update.recreateOffscreen");
        };
	
        // clear the image
        if (firstPaintLayer==0) { // total repaint, i.e. clear the layer 0 also
            offgc = offscreen[0].getGraphics();
            offgc.setColor(getBackground());
            offgc.fillRect(0, 0, d.width, d.height);
            offgc.setColor(getForeground());
            if (Common.DEBUG>1) sw.profile("LayerCanvas.update.clearLayer0");
        }

        int l=firstPaintLayer;
        while(l<layers) {
            offgc=offscreen[l].getGraphics();
            if (l>0) offgc.drawImage(offscreen[l-1],0,0,this);
            paintLayer(offgc,l);
            l++;
        }
        if (Common.DEBUG>1) sw.profile("LayerCanvas.update.constructLayers");

        // do normal redraw
        // transfer offscreen to window, last layer should have the correct image
        g.drawImage(offscreen[l-1], 0, 0, this);
        if (Common.DEBUG>1) sw.profile("LayerCanvas.update.paintLayers");
    };

    /** normally we would not use paint at all, but sometimes paint is called
        instead of update, so we want to force {@link #update} instead */
    public void paint(Graphics g)
    {
	update(g);
    };

    /**
     * Override with your own paint method.
     *
     * @param g is <code>Graphics</code> context you can use for painting.
     * @param layer is the currently processed layer
     */
    abstract public void paintLayer(Graphics g, int layer);
};

