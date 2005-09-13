/*
 * TunePlot.java
 *
 * Created on 22. Mai 2005, 17:53
 */

package org.rosuda.JClaR;


/**
 *
 * @author tobias
 */
public final class TunePlot extends Plot {
    
    private Tune tune;
    
    /** Creates a new instance of TunePlot */
    public TunePlot(final Tune tune) {
        super();
        this.tune=tune;
        setPlotCall("plot(" + tune.getRname() + ")");
    }

    public void setVerticalShift(final double shift) {        /* CAUTION: empty block! */

    }

    public void setHorizontalShift(final double shift) {        /* CAUTION: empty block! */

    }

    public void setShowDataInPlot(final boolean showDataInPlot) {        /* CAUTION: empty block! */

    }

    public boolean getShowDataInPlot() {
        return true;
    }
    
}
