public class PlotColor {
    int r,g,b;
    String nam;
    boolean RGB;

    public PlotColor(int R, int G, int B) {
	r=R; g=G; b=B; RGB=true;
    }

    public PlotColor(String n) {
	if (n.length()==7 && n.charAt(0)=='#') { // #RRGGBB notation
	    try {
		int ir=Integer.parseInt(n.substring(1,3),16);
		int ig=Integer.parseInt(n.substring(3,5),16);
		int ib=Integer.parseInt(n.substring(5,7),16);
		r=ir; g=ig; b=ib;
		RGB=true;
	    } catch(Exception e) {
		nam=n; RGB=false;
	    }	    
	} else {
	    nam=n; RGB=false;
	}
    }

    public void use(PoGraSS p) {
	if (RGB)
	    p.setColor(r,g,b);
	else
	    p.setColor(nam);
    }

    public String toString() {
        return "PlotColor("+((RGB)?""+r+"/"+g+"/"+b:nam)+")";
    }
}
