public class PlotObject {
    PlotManager pm;
    boolean visible=false;
    boolean clip=false;
    int coordinates=0; // 0=abs-geom, 1=var.space

    public PlotObject(PlotManager p) { pm=p; p.add(this); }

    public void draw(PoGraSS g) {
    }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean vis) { visible=vis; }
    public void useVarSpace(boolean vs) { coordinates=vs?1:0; }
    public void setClip(boolean cl) { clip=cl; }
}
