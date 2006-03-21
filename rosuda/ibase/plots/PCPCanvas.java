package org.rosuda.ibase.plots;

import java.awt.Frame;
import org.rosuda.ibase.SMarker;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.PlotComponent;

/** implementation of line plot
 * @version $Id$
 */
public class PCPCanvas extends ParallelAxesCanvas {
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc,f, yvs,mark);
        type=TYPE_PCP;
        updateMargins();
        dontPaint=false;
    }
};