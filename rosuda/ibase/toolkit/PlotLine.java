public class PlotLine extends PlotObject {
    double x1,y1,x2,y2;
    PlotColor c=null;
    
    public PlotLine(PlotManager p) { super(p);}

    public void set(int X1, int Y1, int X2, int Y2) {
	set((double)X1,(double)Y1,(double)X2,(double)Y2);
    }

    public void set(double X1, double Y1, double X2, double Y2) {
	x1=X1; y1=Y1; x2=X2; y2=Y2;
    }

    public void setColor(PlotColor C) { c=C; }

    public void drawLine(PoGraSS g, int x1, int y1, int x2, int y2) {
	g.drawLine(x1,y1,x2,y2);
	System.out.println("line ("+x1+":"+y1+"-"+x2+":"+y2+"), coord="+coordinates);
    }

    public void draw(PoGraSS g) {
	if (c!=null) c.use(g);
	if (coordinates==0)
	    drawLine(g,(int)x1,(int)y1,(int)x2,(int)y2);
	else {
	    Axis x=pm.getXAxis();
	    Axis y=pm.getYAxis();
	    drawLine(g,x.getValuePos(x1),y.getValuePos(y1),
		       x.getValuePos(x2),y.getValuePos(y2));
	}
    }
}
