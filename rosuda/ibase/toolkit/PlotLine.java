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
        if (Common.DEBUG>0)
            System.out.println("line ("+x1+":"+y1+"-"+x2+":"+y2+"), coord="+coordX+"/"+coordY);
    }

    public void draw(PoGraSS g) {
	if (c!=null) c.use(g);
        int rx1=0,rx2=0,ry1=0,ry2=0;
        if (coordX==0) { rx1=(int)x1; rx2=(int)x2; };
        if (coordY==0) { ry1=(int)y1; ry2=(int)y2; };
        if (coordX>0) {
            Axis x=pm.getXAxis();
            if (coordX==1) { rx1=x.getValuePos(x1); rx2=x.getValuePos(x2); };
            if (coordX==2) {
                rx1=x.gBegin+(int)(((double)x.gLen)*x1);
                rx2=x.gBegin+(int)(((double)x.gLen)*x2);
            };
	}
        if (coordY>0) {
            Axis y=pm.getYAxis();
            if (coordY==1) { ry1=y.getValuePos(y1); ry2=y.getValuePos(y2); };
            if (coordY==2) {
                ry1=y.gBegin+(int)(((double)y.gLen)*y1);
                ry2=y.gBegin+(int)(((double)y.gLen)*y2);
            };
        }
        drawLine(g,rx1,ry1,rx2,ry2);
    }
}
