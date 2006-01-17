package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;

public class PlotColor {
    int r,g,b,a;
    String nam;
    boolean RGB;
    boolean alpha;

    public PlotColor(final int R, final int G, final int B) {
        r=R; g=G; b=B; RGB=true; alpha=false;
    }

    public PlotColor(final String n) {
	if ((n.length()==7 || n.length()==9) && n.charAt(0)=='#') { // #RRGGBB notation
	    try {
		final int ir=Integer.parseInt(n.substring(1,3),16);
		final int ig=Integer.parseInt(n.substring(3,5),16);
		final int ib=Integer.parseInt(n.substring(5,7),16);
                if (n.length()==9) {
                    a=Integer.parseInt(n.substring(7,9),16);
                    alpha=true;
                }
		r=ir; g=ig; b=ib;
		RGB=true;
	    } catch(Exception e) {
		nam=n; RGB=false;
	    }	    
	} else {
	    nam=n; RGB=false;
	}
    }

    public void use(final PoGraSS p) {
        if (RGB) {
            if (alpha)
                p.setColor((float)r/255.0F,(float)g/255.0F,(float)b/255.0F,(float)a/255.0F);
            else
                p.setColor(r,g,b);
        } else
	    p.setColor(nam);
    }

    public String toString() {
        return "PlotColor("+((RGB)?(""+r+"/"+g+"/"+b+(alpha?"-"+a:"")):nam)+")";
    }
}
