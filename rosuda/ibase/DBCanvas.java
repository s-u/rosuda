import java.awt.*;

/**
 * Extends the {@link Canvas} class by adding double-buffering support.
 * @version $Id$
 */
public abstract class DBCanvas extends Canvas
{
    /** off-screen buffer descriptor */
    Graphics offgc;
    /** off-screen image */
    Image offscreen = null;
    /** geometry of the off-screen buffer */
    Dimension offsd = null;
    
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

	// sanity check (sounds wierd, but JDK really delivers negative sizes sometimes)
	if (d.width<1 || d.height<1) return;
	// we will re-create the off-screen object only if the canvas was resized
	if ((offsd==null)||(offsd.width!=d.width)||(offsd.height!=d.height)) {
	    // create the offscreen buffer and associated Graphics
	    curimg = createImage(d.width, d.height);
	    offgc = curimg.getGraphics();
	    offsd=d;
	};
	
        // clear the image
        offgc.setColor(getBackground());
        offgc.fillRect(0, 0, d.width, d.height);
        offgc.setColor(getForeground());
        // do normal redraw
        paintBuffer(offgc);
        // transfer offscreen to window	
        g.drawImage((curimg!=null)?curimg:offscreen, 0, 0, this);
	// if curimg is newly created one, replace the previous (ergo free it)
	if (curimg!=null)
	    offscreen=curimg;
    };

    /** normally we would not use paint at all, but sometimes paint is called
	instead of update, so we want to force {@link update} instead */
    public void paint(Graphics g)
    {
	update(g);
    };

    /**
     * Override with your own paint method.
     *
     * @param g is <code>Graphics</code> context you can use for painting.
     */
    abstract public void paintBuffer(Graphics g);
};

