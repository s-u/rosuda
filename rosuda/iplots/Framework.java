import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** basic framework interface for bulding interactive
    statistical programs */

public class Framework {
    Vector dataset;
    SVarSet cvs;

    public Framework() {
	cvs=new SVarSet();
	cvs.setName("new");
	dataset=new Vector();
	dataset.addElement(cvs);
    }

    public SVarSet getCurrentSet() { return cvs; };
    public SVarSet selectSet(String name) {
	int i=0;
	while (i<dataset.size()) {
	    SVarSet s=(SVarSet)dataset.elementAt(i);
	    if (s.getName().equals(name)) {
		cvs=s; return s;
	    };
	    i++;
	};
	return null;
    };
    public SVarSet selectSet(int i) {
	return (i<0||i>=dataset.size())?null:(SVarSet)dataset.elementAt(i);
    };
    public SVarSet newSet(String name) {
	cvs=new SVarSet();
	cvs.setName(name);
	dataset.addElement(cvs);
	return cvs;
    };
    
    public int addVar(SVar v) {
	return cvs.add(v);
    }

    public int newVar(String name, double[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(new Double(d[i++]));
	return addVar(v);
    };

    public int newVar(String name, int[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(new Integer(d[i++]));
	return addVar(v);
    };

    public int newVar(String name, String[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(d[i++]);
	return addVar(v);
    };

    public int replaceVar(int vi, double[] d) {
	SVar v=cvs.at(vi);
	if (v==null) return -1;
	v.notify.beginBatch();
	int i=0; while(i<d.length) { v.replace(i,new Double(d[i])); i++; };
	v.notify.endBatch();
	return vi;
    };

    public int replaceVar(int vi, int[] d) {
	SVar v=cvs.at(vi);
	if (v==null) return -1;
	v.notify.beginBatch();
	int i=0; while(i<d.length) { v.replace(i,new Integer(d[i])); i++; };
	v.notify.endBatch();
	return vi;
    };

    public void update() {
	SMarker m=cvs.getMarker();
	if (m!=null) m.NotifyAll(new NotifyMsg(m,Common.NM_VarContentChange));
    };

    public SVar getVar(int i) { return cvs.at(i); };
    public SVar getVar(String name) { return cvs.byName(name); };

    public ScatterCanvas newScatterplot(int v1, int v2) { return newScatterplot(cvs,v1,v2); }
    public ScatterCanvas newScatterplot(SVarSet vs, int v1, int v2) {
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(v1).size()));
	TFrame f=new TFrame("Scatterplot ("+
			    vs.at(v2).getName()+" vs "+
			    vs.at(v1).getName()+")");	
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
	ScatterCanvas sc=new ScatterCanvas(f,vs.at(v1),vs.at(v2),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
	sc.setSize(new Dimension(400,300));
	f.add(sc); f.pack(); f.show();
	return sc;
    };

    public LineCanvas newLineplot(int[] v) { return newLineplot(cvs,-1,v); }
    public LineCanvas newLineplot(int rv, int[] v) { return newLineplot(cvs,rv,v); }
    public LineCanvas newLineplot(int rv, int v) { int vv[]=new int[1]; vv[0]=v; return newLineplot(cvs,rv,vv); }
    public LineCanvas newLineplot(SVarSet vs, int rv, int[] v) {
	if (v.length==0) return null;
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(v[0]).size()));
	TFrame f=new TFrame("Lineplot");	
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
	SVar[] vl=new SVar[v.length];
	int i=0;
	while(i<v.length) { vl[i]=vs.at(v[i]); i++; };
	LineCanvas sc=new LineCanvas(f,vs.at(rv),vl,vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
	sc.setSize(new Dimension(400,300));
	f.add(sc); f.pack(); f.show();
	return sc;
    };

    public HistCanvas newHistogram(int v) { return newHistogram(cvs,v); };
    public HistCanvas newHistogram(SVarSet vs, int i) {
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(i).size()));
	TFrame f=new TFrame("Histogram ("+vs.at(i).getName()+")");
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
	HistCanvas hc=new HistCanvas(f,vs.at(i),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(hc);
	hc.setSize(new Dimension(400,300));
	f.add(hc); f.pack(); f.show();
	return hc;
    };
    
    public VarFrame newVarFrame() { return newVarFrame(cvs); };
    public VarFrame newVarFrame(SVarSet v) {
	VarFrame vf=new VarFrame(v,10,10,150,400);
	return vf;
    };
}
