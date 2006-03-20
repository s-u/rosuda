package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Tools;

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
    
    public void rotate(final int amount) {
        switch((orientation+amount)&1){
            case 0:
                mBottom=bigMBottom;
                mTop=bigMTop;
                mLeft=smallMLeft;
                mRight=smallMRight;
                break;
            case 1:
                mBottom=commonScale?bigMBottom:smallMBottom;
                mTop=smallMTop;
                mLeft = bigMLeft;
                mRight = smallMRight;
        }
        super.rotate(amount);
    }
    
    protected void initFlagsAndFields() {
        super.initFlagsAndFields();
        
        useRegularPositioning=true;
        bigMLeft=bigMRight=50;
    }
};