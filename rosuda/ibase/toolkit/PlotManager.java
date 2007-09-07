package org.rosuda.ibase.toolkit;

import java.util.*;
import org.rosuda.pograss.*;

/** Plot manager is a container of all plot objects to be painted on the associated PoGraSS canvas.
*/
public class PlotManager {
    /** parent plot of this manager */
    PGSCanvas c;
    /** list of PlotObjects managed by this manager */
    Vector obj;
    /** pointer to the current object int the list */
    int ptr;

    /** construct a new plot manager for the given plot
        @param cv parent plot */
    public PlotManager(final PGSCanvas cv) {
        c=cv;
	obj=new Vector();
        ptr=-1;
    }

    /** get axis object of the plot - simply calls {@link PGSCanvas.getXAxis} */
    public Axis getXAxis() { return c.getXAxis(); }

    /** get axis object of the plot - simply calls {@link PGSCanvas.getYAxis} */
    public Axis getYAxis() { return c.getYAxis(); }

    /** actual painting method */
    public void drawLayer(final PoGraSS g,final int layer, final int layers) {
	int i=0;
	while(i<obj.size()) {
	    final PlotObject o=(PlotObject)obj.elementAt(i);
	    if (o!=null && o.isVisible() && (o.layer==layer || (o.layer==-1 && layer==layers-1)))
		o.draw(g);
	    i++;
	}
    }

    public void dispose() {
	ptr=-1;
	while (obj.size()>0) {
	    PlotObject po = (PlotObject)obj.elementAt(0);
	    if (po != null) po.dispose(); // dispose should include a call to rm, but better safe than sorry ...
	    try { obj.removeElement(po); } catch (Exception e) {};
	}
	obj.clear();
	obj=null;
	c=null;
    }

    public void add(final PlotObject po) {
	obj.addElement(po);
        ptr=obj.size()-1; // set current pointer to the newly created object
    }

    public void rm(final PlotObject po) {
        if (ptr<0) ptr=obj.size()-1;
        if (ptr>=0) {
            final PlotObject cpo=(PlotObject) obj.elementAt(ptr);
            obj.removeElement(po);
            if (cpo==po || ptr>=obj.size() || cpo!=obj.elementAt(ptr))
                ptr=obj.size()-1;
        } else {
            obj.removeElement(po);
            ptr=obj.size()-1;
        }
    }

    public int getCurrentID() { return ptr; }

    public void setCurrentID(final int newID) { ptr=newID; }
    
    public PlotObject getCurrentObject() {
        return (ptr>=0 && ptr<obj.size())?(PlotObject)obj.elementAt(ptr):null;
    }

    public int setCurrentObject(final PlotObject po) {
        if (obj.size()<1) return -1;
        int i=0;
        while(i<obj.size()) {
            if (obj.elementAt(i)==po)
                return ptr=i;
            i++;
        }
        return -1;                
    }
    
    public void update(final int layer) {
	if (layer>-1) c.setUpdateRoot(layer);
        else c.setUpdateRoot(c.layers-1);
        c.repaint();
    }

    public void update() {
	update(0);
    }

    public PlotObject get(final int id) {
	return (PlotObject) obj.elementAt(id);
    }

    public int count() {
	return obj.size();
    }
}
