//
//  Framework.java - "glue" between R and iplots
//  (C)2003 Simon Urbanek
//
//  $Id$

package org.rosuda.iplots;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.plots.*;
import org.rosuda.util.*;

/** basic framework interface for bulding interactive
    statistical programs */

public class Framework implements Dependent, ActionListener {
    Vector dataset;
    SVarSet cvs;
    int tvctr;
    int dtctr;

    /** initialize framework, create and select a dataset which id called "default".
        one framework supports multiple datasets, plots etc. so there should be no
        need for multiple frameworks usually.
        */
    public Framework() {
        Global.AppType=Common.AT_Framework;
        Common.supportsBREAK=true;
        Global.useAquaBg=true; // use aqua look
	Common.backgroundColor=Common.aquaBgColor; // use aqua bg color
        Platform.initPlatform();
        if (Common.breakDispatcher==null) Common.breakDispatcher=new Notifier();
        Common.breakDispatcher.addDepend(this);
	cvs=new SVarSet();
	cvs.setName("default");
	dataset=new Vector();
	dataset.addElement(cvs);
    }
    
    public String getNewTmpVar(String t) {
        int i=cvs.indexOf(t);
        if (i==-1) return t;
        tvctr++;
        i=cvs.indexOf(t+"."+tvctr);
        if (i==-1) return t+"."+tvctr;
        return "temp."+tvctr;
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
	return (i<0||i>=dataset.size())?null:(cvs=(SVarSet)dataset.elementAt(i));
    }

    public SVarSet getSet(int i) {
        return (i<0||i>=dataset.size())?null:(SVarSet)dataset.elementAt(i);
    }

    public int countSets() {
        return dataset.size();
    }
    
    public int curSetId() {
        return dataset.indexOf(cvs);
    }

    public String getSetName(int i) {
        try {
            SVarSet vs=(SVarSet) dataset.elementAt(i);
            return vs.getName();
        } catch (Exception e) {}
        return null;
    }

    public String getSetName() {
        return cvs.getName();
    }
    
    /** create and select a new dataset with the specified name. please note that it is possible to create
        multiple datasets of the same name but then only the first of these will be retrieved by name, others
        have to be selected by ID
        @param name name of the new dataset
        @return new dataset */
    public int newSet(String name) {
	cvs=new SVarSet();
        if (name==null) {
            dtctr++;
            name="data."+dtctr;
        }
	cvs.setName(name);
	dataset.addElement(cvs);
	return dataset.indexOf(cvs);
    }

    public int getLength() {
        return (cvs==null || cvs.at(0)==null)?0:cvs.at(0).size();
    }
    
    /** add a variable to the current dataset. Note that many plots assume that all variables of a dataset
        have the same size.
        @param v the variable
        @return index of the variable within the dataset. In order to prevent usage of variables across datasets,
        most plots take the ID of a variable as parameter and NOT the {@link SVar} object itself.
        */
    public int addVar(SVar v) {
        if (cvs.getMarker()==null) {
            SMarker m;
            cvs.setMarker(m=new SMarker(v.size()));
            m.addDepend(this);
        }
        SMarker m=cvs.getMarker();
        if (v.size()>m.size()) m.resize(v.size());
	return cvs.add(v);
    }

    int mmDlg(String name, int d) {
        int res=0;
        Frame f=new Frame("dummy");
        MsgDialog md=new MsgDialog(f,"Data length mismatch","Variable \""+name+"\" consists of "+d+" cases, but your current iSet has "+cvs.at(0).size()+" cases. Do you want to create a new iSet?",MsgDialog.yesNoCancel);
        if (md.lastResult=="Cancel") res=-2;
        if (md.lastResult=="Yes") res=-3;
        md.dispose();
        md=null;
        f.dispose();
        f=null;
        return res;
    }

    public static String msgDlg(String caption, String msg, String[] buttons) {
        String res;
        Frame f=new Frame("dummy");
        MsgDialog md=new MsgDialog(f,caption,msg,buttons);
        res=md.lastResult;
        md.dispose();
        md=null;
        f.dispose();
        f=null;
        return res;
    }
    
    /** construct a new numerical variable from supplied array of doubles. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of doubles
        @return ID of the new variable or -1 if error occured (variable name already exists etc.)
        */
    public int newVar(String name, double[] d) {
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            int i=mmDlg(name,d.length);
            if (i<0) return i;
        }
	SVar v=new SVar(name);
        int i=0; while(i<d.length) {
            if (d[i]==Double.NaN)
                v.add(null);
            else
                v.add(new Double(d[i++]));
        }
	return addVar(v);
    };

    /** construct a new numerical variable from supplied array of integers. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of integers
        @return ID of the new variable or -1 if error occured (variable name already exists etc.)
        */   
    public int newVar(String name, int[] d) {
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            int i=mmDlg(name,d.length);
            if (i<0) return i;
        }
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
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            int i=mmDlg(name,d.length);
            if (i<0) return i;
        }
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

    /** beware!! this updateMerker has nothing to do with updateMarker() !!!!! bad thing! */
    public void updateMarker(SVarSet vs, int vid) {
        if (vs.getMarker()==null) {
            SMarker m=new SMarker(vs.at(vid).size());
            vs.setMarker(m);
            m.addDepend(this);
        }
    }
    
    /** display a new scatterplot of two variables from current dataset
        @param v1 X-axis variable
        @param v2 Y-axis variable
        @return scatterplot canvas object */
    public ScatterCanvas newScatterplot(int v1, int v2) { return newScatterplot(cvs,v1,v2); }
    public ScatterCanvas newScatterplot(SVarSet vs, int v1, int v2) {
        updateMarker(vs,v1);
        TFrame f=new TFrame("Scatterplot ("+
			    vs.at(v2).getName()+" vs "+
			    vs.at(v1).getName()+")",TFrame.clsScatter);	
	f.addWindowListener(Common.getDefaultWindowListener());
	ScatterCanvas sc=new ScatterCanvas(f,vs.at(v1),vs.at(v2),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
	sc.setSize(new Dimension(400,300));
	f.add(sc); f.pack(); f.show();
	return sc;
    };

    public BarCanvas newBarchart(int v) { return newBarchart(cvs,v,-1); }
    public BarCanvas newBarchart(int v, int wgt) { return newBarchart(cvs,v,wgt); }
    public BarCanvas newBarchart(SVarSet vs, int v, int wgt) {
        updateMarker(vs,v);
        SVar theCat=vs.at(v), theNum=(wgt<0)?null:vs.at(wgt);
        if (theCat==null) return null;
	if (!theCat.isCat()) theCat.categorize();
        TFrame f=new TFrame(
                            (theNum!=null)?
                            "w.Barchart ("+theCat.getName()+"*"+theNum.getName()+")":
                            "Barchart ("+theCat.getName()+")"
                            ,TFrame.clsBar);
        f.addWindowListener(Common.getDefaultWindowListener());
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
        updateMarker(vs,v[0]);
	TFrame f=new TFrame("Lineplot",TFrame.clsLine);	
	f.addWindowListener(Common.getDefaultWindowListener());
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
        updateMarker(vs,i);
	TFrame f=new TFrame("Histogram ("+vs.at(i).getName()+")",TFrame.clsHist);
	f.addWindowListener(Common.getDefaultWindowListener());
	HistCanvasNew hc=new HistCanvasNew(f,vs.at(i),vs.getMarker());
	if (vs.getMarker()!=null) vs.getMarker().addDepend(hc);
	hc.setSize(new Dimension(400,300));
	f.add(hc); f.pack(); f.show();
	return hc;
    };

    public BoxCanvas newBoxplot(int i) { return newBoxplot(cvs,i,-1); }
    public BoxCanvas newBoxplot(int i, int ic) { return newBoxplot(cvs,i,ic); }
    public BoxCanvas newBoxplot(SVarSet vs, int i, int ic) {
        SVar catVar=(ic<0)?null:vs.at(ic);
        updateMarker(vs,i);
        TFrame f=new TFrame("Boxplot ("+vs.at(i).getName()+")"+((catVar!=null)?" by "+catVar.getName():""),
                            TFrame.clsBox);
        f.addWindowListener(Common.getDefaultWindowListener());
        BoxCanvas sc=(catVar==null)?new BoxCanvas(f,vs.at(i),vs.getMarker()):new BoxCanvas(f,vs.at(i),catVar,vs.getMarker());
        if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);
        sc.setSize(new Dimension(80,300));
        f.add(sc); f.pack(); f.show();
        f.initPlacement();
        return sc;
    };

    // no VarFRame since that's Klimt-specific
    /** display a new variables frame
        @return variable frame object
    public VarFrame newVarFrame() { return newVarFrame(cvs); };
    public VarFrame newVarFrame(SVarSet v) {
	VarFrame vf=new VarFrame(v,10,10,150,400);
	return vf;
    };
    */
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

    public double[] getDoubleContent(SVar v) {
        if (v==null) return null;
        double[] d=new double[v.size()];
        int i=0;
        while(i<v.size()) { d[i]=v.atD(i); i++; };
        return d;
    }

    public String[] getStringContent(SVar v) {
        if (v==null) return null;
        String[] d=new String[v.size()];
        int i=0;
        while(i<v.size()) { d[i]=v.atS(i); i++; };
        return d;
    }

    public int varIsNum(int vid) {
        SVar v=cvs.at(vid);
        if (v==null) return -1;
        return (v.isNum())?1:0;
    }

    public int varIsNum(SVar v) {
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

    public void actionPerformed(ActionEvent e) {
	Common.breakDispatcher.NotifyAll(new NotifyMsg(this,Common.NM_ActionEvent,e.getActionCommand()));
    }

    public void setDebugLevel(int df) {
        if (Global.DEBUG>0) System.out.println("Setting DEBUG level to "+df);
        Global.DEBUG=df;
        if (Global.DEBUG>0) System.out.println("DEBUG level set to "+Global.DEBUG);
    }
    
    //=============================== EVENT LOOP STUFF =========================================

    private boolean notificationArrived=false;
    private NotifyMsg lastNotificationMessage; // only synchronized methods are allowed to use this

    /** this internal method waits until {@link #triggerNotification} is called by another thread. It is implemented by using {@link wait()} and checking {@link notificationArrived}. */
    private synchronized NotifyMsg waitForNotification() {
        while (!notificationArrived) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        notificationArrived=false;
        NotifyMsg m=lastNotificationMessage;
        lastNotificationMessage=null; // reset lastNM
        return m;
    }

    /** this methods awakens {@link #waitForNotification}. It is implemented by setting {@link #notificationArrived} to <code>true</code>, setting {@link #lastNotificationMessage} to the passed message and finally calling {@link notifyAll()}. */
    private synchronized void triggerNotification(NotifyMsg msg) {
        notificationArrived=true;
        lastNotificationMessage=msg;
        notifyAll();
    }

    /** is a message arrives we'll simply use {@link #triggerNotification} to inform any sleeping calls to {@link #waitForNotification} */
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        triggerNotification(msg);
    }

    /** this methods is called from R by ievent.wait and uses {@link #waitForNotification} to wait for an event. */
    public NotifyMsg eventWait() {
        NotifyMsg m=waitForNotification();
        return (m==null || m.getMessageID()==Common.NM_BREAK)?null:m;
    }
}
