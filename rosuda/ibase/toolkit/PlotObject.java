public class PlotObject {
    PlotManager pm;
    boolean visible=false;
    boolean clip=false;
    int coordX=0, coordY=0; // 0=abs-geom, 1=var.space, 2=relative geom. (-1=min, 1=max)

    public PlotObject(PlotManager p) { pm=p; p.add(this); }

    public void draw(PoGraSS g) {
    }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean vis) { visible=vis; }
    /** deptrecated! */
    public void useVarSpace(boolean vs) { coordX=coordY=vs?1:0; }
    public void setCoordinates(int ct) { coordX=coordY=ct; };
    public void setCoordinates(int cx, int cy) { coordX=cx; coordY=cy; };

    public void setClip(boolean cl) { clip=cl; }
}
