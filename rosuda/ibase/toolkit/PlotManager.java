import java.util.*;

/** Plot manager is a container of all plot objects to be painted on the associated PoGraSS canvas.
*/
public class PlotManager {
    PGSCanvas c;
    Vector obj;

    public PlotManager(PGSCanvas cv) {
        c=cv;
	obj=new Vector();
    }

    public Axis getXAxis() { return c.getXAxis(); }
    public Axis getYAxis() { return c.getYAxis(); }

    public void drawLayer(PoGraSS g,int layer, int layers) {
	int i=0;
	while(i<obj.size()) {
	    PlotObject o=(PlotObject)obj.elementAt(i);
	    if (o!=null && o.isVisible() && (o.layer==layer || (o.layer==-1 && layer==layers-1)))
		o.draw(g);
	    i++;
	};
    }

    public void add(PlotObject po) {
	obj.addElement(po);
    }

    public void rm(PlotObject po) {
        obj.removeElement(po);
    }
    
    public void update(int layer) {
	if (layer>-1) c.setUpdateRoot(layer);
        c.repaint();
    }

    public void update() {
	update(0);
    }
}
