import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** basic framework interface for bulding interactive
    statistical programs */

public class Framework {
    Vector dataset;
    SVarSet cvs;

    /** initialize framework, create and select a dataset which id called "default".
        one framework supports multiple datasets, plots etc. so there should be no
        need for multiple frameworks usually.
        */
    public Framework() {
        Common.AppType=Common.AT_Framework;
	cvs=new SVarSet();
	cvs.setName("default");
	dataset=new Vector();
	dataset.addElement(cvs);
    }

    /** get current dataset */
    public SVarSet getCurrentSet() { return cvs; };
    
    /** select dataset by name. the initial dataset created during framework initialization is called "default".
        @param name name of the dataset
        @return selected dataset or <code>null</code> if no such dataset was found */
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

    /** select dataset based on its ID (initial dataset has ID 0)/
        @param i the ID
        @return selected dataset or <code>null</code> if ID out of range */
    public SVarSet selectSet(int i) {
	return (i<0||i>=dataset.size())?null:(SVarSet)dataset.elementAt(i);
    };

    /** create and select a new dataset with the specified name. please note that it is possible to create
        multiple datasets of the same name but then only the first of these will be retrieved by name, others
        have to be selected by ID
        @param name name of the new dataset
        @return new dataset */
    public SVarSet newSet(String name) {
	cvs=new SVarSet();
	cvs.setName(name);
	dataset.addElement(cvs);
	return cvs;
    };

    /** add a variable to the current dataset. Note that many plots assume that all variables of a dataset
        have the same size.
        @param v the variable
        @return index of the variable within the dataset. In order to prevent usage of variables across datasets,
        most plots take the ID of a variable as parameter and NOT the {@link SVar} object itself.
        */
    public int addVar(SVar v) {
	return cvs.add(v);
    }

    /** construct a new numerical variable from supplied array of doubles. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of doubles
        @return ID of the new variable or -1 if error occured (variable name already exists etc.)
        */
    public int newVar(String name, double[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(new Double(d[i++]));
	return addVar(v);
    };

    /** construct a new numerical variable from supplied array of integers. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of integers
        @return ID of the new variable or -1 if error occured (variable name already exists etc.)
        */   
    public int newVar(String name, int[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(new Integer(d[i++]));
	return addVar(v);
    };

    /** construct a new categorical variable from supplied array of strings. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of strings
        @return ID of the new variable or -1 if error occured (variable name already exists etc.)
        */    
    public int newVar(String name, String[] d) {
	SVar v=new SVar(name);
	int i=0; while(i<d.length) v.add(d[i++]);
	return addVar(v);
    };

    /** replaces the content of a variable. it is meant for modification ONLY. note that the length of
        the new content cannot exceed the original size of the variable, no cases are added.
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
