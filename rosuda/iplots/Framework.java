import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** basic framework interface for bulding interactive
    statistical programs */

public class Framework {
    Vector dataset;
    SVarSet cvs;
    int tvctr;

    /** initialize framework, create and select a dataset which id called "default".
        one framework supports multiple datasets, plots etc. so there should be no
        need for multiple frameworks usually.
        */
    public Framework() {
        Common.AppType=Common.AT_Framework;
        Common.useAquaBg=true;
        Common.initStatic();
	cvs=new SVarSet();
	cvs.setName("default");
	dataset=new Vector();
	dataset.addElement(cvs);
    }

    public String getNewTmpVar() {
        tvctr++;
        return "temp."+tvctr;
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

    public static String[] toStringArray(Object[] o) {
        String[] s=new String[o.length];
        int i=0; while(i<o.length) { if (o[i]!=null) s[i]=o[i].toString(); i++; }
        return s;
    }

    /** replaces the content of a variable. it is meant for modification ONLY. note that the length of
        the new content cannot exceed the original size of the variable, no cases are added.
        @param vi ID of the variable
        @param d new content
        @return variable ID (same as vi) */
    public int replaceVar(int vi, double[] d) {
	SVar v=cvs.at(vi);
	if (v==null) return -1;
	v.notify.beginBatch();
	int i=0; while(i<d.length) { v.replace(i,new Double(d[i])); i++; };
	v.notify.endBatch();
	return vi;
    };

    /** replaces the content of a variable. it is meant for modification ONLY. note that the length of
        the new content cannot exceed the original size of the variable, no cases are added.
        @param vi ID of the variable
        @param d new content
        @return variable ID (same as vi) */
    public int replaceVar(int vi, int[] d) {
	SVar v=cvs.at(vi);
	if (v==null) return -1;
	v.notify.beginBatch();
	int i=0; while(i<d.length) { v.replace(i,new Integer(d[i])); i++; };
	v.notify.endBatch();
	return vi;
    };

    /** updates any plots associated with the current dataset by sending NM_VarContentChange message */
    public void update() {
	SMarker m=cvs.getMarker();
	if (m!=null) m.NotifyAll(new NotifyMsg(m,Common.NM_VarContentChange));
    };

    /** get variable object associated with an ID in current dataset
        @param i variable ID
        @return variable object or <code>null</code> if ID is invalid */
    public SVar getVar(int i) { return cvs.at(i); };

    /** get first variable object associated with a name in current dataset
        @param i variable name
        @return variable object or <code>null</code> if var of that name doesn't exist */
    public SVar getVar(String name) { return cvs.byName(name); };

    /** display a new scatterplot of two variables from current dataset
        @param v1 X-axis variable
        @param v2 Y-axis variable
        @return scatterplot canvas object */
    public ScatterCanvas newScatterplot(int v1, int v2) { return newScatterplot(cvs,v1,v2); }
    public ScatterCanvas newScatterplot(SVarSet vs, int v1, int v2) {
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(v1).size()));
	TFrame f=new TFrame("Scatterplot ("+
			    vs.at(v2).getName()+" vs "+
			    vs.at(v1).getName()+")",TFrame.clsScatter);	
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
	ScatterCanvas sc=new ScatterCanvas(f,vs.at(v1),vs.at(v2),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
	sc.setSize(new Dimension(400,300));
	f.add(sc); f.pack(); f.show();
	return sc;
    };

    public BarCanvas newBarchart(int v) { return newBarchart(cvs,v,-1); }
    public BarCanvas newBarchart(int v, int wgt) { return newBarchart(cvs,v,wgt); }
    public BarCanvas newBarchart(SVarSet vs, int v, int wgt) {
        if (vs.getMarker()==null)
            vs.setMarker(new SMarker(vs.at(v).size()));
        SVar theCat=vs.at(v), theNum=(wgt<0)?null:vs.at(wgt);
        if (theCat==null) return null;
        TFrame f=new TFrame(
                            (theNum!=null)?
                            "w.Barchart ("+theCat.getName()+"*"+theNum.getName()+")":
                            "Barchart ("+theCat.getName()+")"
                            ,TFrame.clsBar);
        f.addWindowListener(Common.defaultWindowListener);
        BarCanvas bc=new BarCanvas(f,theCat,vs.getMarker(),theNum);
        if (vs.getMarker()!=null) vs.getMarker().addDepend(bc);
        bc.setSize(new Dimension(400,300));
        f.add(bc); f.pack(); f.show();
        f.initPlacement();
        return bc;
    }
    
    public LineCanvas newLineplot(int[] v) { return newLineplot(cvs,-1,v); }
    public LineCanvas newLineplot(int rv, int[] v) { return newLineplot(cvs,rv,v); }
    public LineCanvas newLineplot(int rv, int v) { int vv[]=new int[1]; vv[0]=v; return newLineplot(cvs,rv,vv); }
    public LineCanvas newLineplot(SVarSet vs, int rv, int[] v) {
	if (v.length==0) return null;
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(v[0]).size()));
	TFrame f=new TFrame("Lineplot",TFrame.clsLine);	
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

    /** display a new histogram of a variables from current dataset
        @param v variable ID
        @return histogram canvas object */
    public HistCanvasNew newHistogram(int v) { return newHistogram(cvs,v); };
    public HistCanvasNew newHistogram(SVarSet vs, int i) {
	if (vs.getMarker()==null)
	    vs.setMarker(new SMarker(vs.at(i).size()));
	TFrame f=new TFrame("Histogram ("+vs.at(i).getName()+")",TFrame.clsHist);
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	f.addWindowListener(Common.defaultWindowListener);
	HistCanvasNew hc=new HistCanvasNew(f,vs.at(i),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(hc);
	hc.setSize(new Dimension(400,300));
	f.add(hc); f.pack(); f.show();
	return hc;
    };

    /** display a new variables frame
        @return variable frame object */
    public VarFrame newVarFrame() { return newVarFrame(cvs); };
    public VarFrame newVarFrame(SVarSet v) {
	VarFrame vf=new VarFrame(v,10,10,150,400);
	return vf;
    };

    public double[] getDoubleContent(int vid) {
        SVar v=cvs.at(vid);
        if (v==null) return null;
        double[] d=new double[v.size()];
        int i=0;
        while(i<v.size()) { d[i]=v.atD(i); i++; };
        return d;
    };

    public String[] getStringContent(int vid) {
        SVar v=cvs.at(vid);
        if (v==null) return null;
        String[] d=new String[v.size()];
        int i=0;
        while(i<v.size()) { d[i]=v.atS(i); i++; };
        return d;
    };

    public int varIsNum(int vid) {
        SVar v=cvs.at(vid);
        if (v==null) return -1;
        return (v.isNum())?1:0;
    }

    public void setSecMark(int[] ml) { setSecMark(ml,true); }
    public void setSecMark(int[] ml, boolean circular) {
        if (cvs==null) return;
        int i=0, j=0;
        SMarker m=cvs.getMarker();
        while(i<ml.length && j<m.size()) {
            m.setSec(j,ml[i]);
            i++; j++;
	    if (circular && i>=ml.length) i=0;
        }
        m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
    }
    
    public void updateMarker() {
        if (cvs!=null) cvs.getMarker().NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
    }

    public void updateVars() {
        if (cvs!=null) cvs.getMarker().NotifyAll(new NotifyMsg(this,Common.NM_VarChange));
    }
}
