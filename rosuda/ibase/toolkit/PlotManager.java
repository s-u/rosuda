import java.util.*;

/** Plot manager is a container of all plot objects to be painted on the associated PoGraSS canvas.
*/
public class PlotManager {
    PGSCanvas c;
    Axis ax, ay;
    Vector obj;

    // Warning: we need some some more generic approach: a1/a2 may have changed in the plot after we registered with it
    public PlotManager(PGSCanvas cv, Axis a1, Axis a2) {
        c=cv; ax=a1; ay=a2;
	obj=new Vector();
    }

    public Axis getXAxis() { return ax; }
    public Axis getYAxis() { return ay; }

    public void draw(PoGraSS g) {
	int i=0;
	while(i<obj.size()) {
	    PlotObject o=(PlotObject)obj.elementAt(i);
	    if (o!=null && o.isVisible()) o.draw(g);
	    i++;
	};
    }

    public void add(PlotObject po) {
	obj.addElement(po);
    }

    public void rm(PlotObject po) {
        obj.removeElement(po);
    }
    
    public void update() {
        c.repaint();
    }
}
