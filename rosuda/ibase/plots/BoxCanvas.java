package org.rosuda.ibase.plots;

import java.awt.Frame;
import org.rosuda.ibase.SMarker;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.PlotComponent;


/** BoxCanvas - implementation of the boxplots
 * @version $Id$
 */
public class BoxCanvas extends ParallelAxesCanvas {
    
    /** create a boxplot canvas for a single boxplot
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent pc, final Frame f, final SVar var, final SMarker mark) {
        super(pc,f,var,mark,TYPE_BOX);
    }
    
    /** create a boxplot canvas for multiple boxplots
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variables
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar[] var, final SMarker mark) {
        super(ppc,f,var,mark,TYPE_BOX);
    };
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar var, final SVar cvar, final SMarker mark) { // multiple box vs cat
        super(ppc,f,var,cvar,mark);
    }
}
