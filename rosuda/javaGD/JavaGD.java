package org.rosuda.javaGD;

import java.awt.*;

public class JavaGD {
    Frame f;
    GDCanvas c;
    
    public JavaGD() {
        super();
    }
    
    /*---- external API: those methods are called via JNI from the GD C code
    
    public void     gdOpen(double w, double h);
    public void     gdActivate();
    public void     gdCircle(double x, double y, double r);
    public void     gdClip(double x0, double x1, double y0, double y1);
    public void     gdClose();
    public void     gdDeactivate();
    public void     gdHold();
    public double[] gdLocator();
    public void     gdLine(double x1, double y1, double x2, double y2);
    public double[] gdMetricInfo(int ch);
    public void     gdMode(int mode);
    public void     gdNewPage();
    public void     gdPolygon(int n, double[] x, double[] y);
    public void     gdPolyline(int n, double[] x, double[] y);
    public void     gdRect(double x0, double y0, double x1, double y1);
    public double[] gdSize();
    public double   gdStrWidth(String str);
    public void     gdText(double x, double y, String str, double rot, double hadj);
    
    -- GDC - manipulation of the current graphics state
    public void gdcSetColor(int cc);
    public void gdcSetFill(int cc);
    public void gdcSetLine(double lwd, int lty);
    public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily);

    -- implementation --*/
    
    public void     gdOpen(double w, double h) {
        if (f!=null) gdClose();
            
        f=new Frame("JavaGD");
        c=new GDCanvas(w, h);
        f.add(c);
        f.pack();
        f.setVisible(true);
    }
    
    public void     gdActivate() {
    }

    public void     gdCircle(double x, double y, double r) {
        if (c==null) return;
        c.add(new GDCircle(x,y,r));
    }
    
    public void     gdClip(double x0, double x1, double y0, double y1) {
        // FixME
    }

    public void     gdClose() {
        if (f!=null) {
            c=null;
            f.removeAll();
            f.dispose();
            f=null;
        }
    }
    
    public void     gdDeactivate() {
    }
    
    public void     gdHold() {
    }
    
    public double[] gdLocator() {
        double[] res=new double[2];
        // FixME
        res[0]=0.0; res[1]=0.0;
        return res;
    }
    
    public void     gdLine(double x1, double y1, double x2, double y2) {
        if (c==null) return;
        c.add(new GDLine(x1, y1, x2, y2));
    }
    
    public double[] gdMetricInfo(int ch) {
        double[] res=new double[3];
        double ascent=0.0, descent=0.0, width=8.0;
        if (c!=null) {
            Graphics g=c.getGraphics();
            if (g!=null) {
                FontMetrics fm=g.getFontMetrics(c.f);
                if (fm!=null) {
                    ascent=(double) fm.getAscent();
                    descent=(double) fm.getDescent();
                    width=(double) fm.charWidth((ch==0)?77:ch);
                }
            }
        }
        res[0]=ascent; res[1]=descent; res[2]=width;
        return res;
    }
    
    public void     gdMode(int mode) {
        if (mode==0 || c!=null) c.repaint();
    }
    
    public void     gdNewPage() {
        if (c!=null) c.reset();
    }
    
    public void     gdPolygon(int n, double[] x, double[] y) {
        if (c==null) return;
        c.add(new GDPolygon(n, x, y, true));
    }
    
    public void     gdPolyline(int n, double[] x, double[] y) {
        if (c==null) return;
        c.add(new GDPolygon(n, x, y, false));
    }
    
    public void     gdRect(double x0, double y0, double x1, double y1) {
        if (c==null) return;
        c.add(new GDRect(x0, y0, x1, y1));
    }

    public double[] gdSize() {
        double[] res=new double[4];
        double width=0d, height=0d;
        if (c != null) {
            Dimension d = c.getSize();
            width = d.getWidth();
            height = d.getHeight();
        }
        res[0]=0d;
        res[1]=width;
        res[2]=height;
        res[3]=0;
        return res;
    }
    
    public double   gdStrWidth(String str) {
        double width=(double)(8*str.length()); // rough estimate
        if (c!=null) { // if canvas is active, we can do better
            Graphics g=c.getGraphics();
            if (g!=null) {
                FontMetrics fm=g.getFontMetrics(c.f);
                if (fm!=null) width=(double)fm.stringWidth(str);
            }
        }
        return width;
    }
    
    public void     gdText(double x, double y, String str, double rot, double hadj) {
        if (c==null) return;
        c.add(new GDText(x, y, rot, hadj, str));
    }
    
    /*-- GDC - manipulation of the current graphics state */
    public void gdcSetColor(int cc) {
        if (c==null) return;
        c.add(new GDColor(cc));
    }
    
    public void gdcSetFill(int cc) {
        if (c==null) return;
        c.add(new GDFill(cc));
    }
    
    public void gdcSetLine(double lwd, int lty) {
        if (c==null) return;
        c.add(new GDLinePar(lwd, lty));
    }
    
    public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
        if (c==null) return;
        c.add(new GDFont(cex, ps, lineheight, fontface, fontfamily));
    }
    
}
