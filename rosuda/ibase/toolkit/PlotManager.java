import java.util.*;

public class PlotManager {
    int leftm=10, rightm=10, topm=10, botm=10;
    PGSCanvas c;
    Axis ax, ay;
    Vector obj;

    public PlotManager(PGSCanvas cv, Axis a1, Axis a2, int lm, int tm, int rm, int bm) {
	ax=a1; ay=a2; leftm=lm; rightm=rm; topm=tm; botm=bm;
	obj=new Vector();
    }

    public void setMargins(int lm, int tm, int rm, int bm) {
	leftm=lm; rightm=rm; topm=tm; botm=bm;
    };

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
}
