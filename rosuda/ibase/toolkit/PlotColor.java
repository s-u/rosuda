public class PlotColor {
    int r,g,b;
    String nam;
    boolean RGB;

    public PlotColor(int R, int G, int B) {
	r=R; g=G; b=B; RGB=true;
    }

    public PlotColor(String n) {
	nam=n; RGB=false;
    }

    public void use(PoGraSS p) {
	if (RGB)
	    p.setColor(r,g,b);
	else
	    p.setColor(nam);
    }
}
