package org.rosuda.JClaR;

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;


public abstract class SidePanel extends JPanel {

    /** Autogenerated proxy constructor. */
    public SidePanel() {
        super();
    }

    protected JFrame parent;    
    protected boolean noRecalc;
    protected NumberFormat numberFormat;
    
    private Vector listeners = new Vector();
    
    public final void setParent(final JFrame parent){
        this.parent=parent;
    }
    
    protected final void fire(final int message){
        for(final Enumeration en = listeners.elements(); en.hasMoreElements();) {
            ((SimpleChangeListener)en.nextElement()).stateChanged(new SimpleChangeEvent(this,message));
        }
        
    }
    
    public final void addSimpleChangeListener(final SimpleChangeListener scl){
        listeners.add(scl);
    }
    
    public abstract boolean getAutoRecalc();
    
    public static final int EVT_TRAIN = 0;
    public static final int EVT_UPDATE_PLOT = 1;

}