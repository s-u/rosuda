import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** SplitEditor */

public class SplitEditor extends TFrame implements ActionListener, ItemListener, MouseListener {
    SNode n,ln;
    SMarker m;
    SVarSet vs;
    SVar cv;
    SNode root;
    TextField st=null;
    String splitText; // contains the text in 'st' - it is used to check if used actually changed the content without pressing enter
    Choice vc;
    ScatterCanvas sc;
    LineCanvas lc;
    Label l1;
    Panel cp,sp,pp;
    PlotLine li,lrl, rrl;
    double spVal;
    
    public static int editSuffix=0;
    
    public SplitEditor(SNode nd) {
        super("Split Editor",TFrame.clsUser);
        n=nd; vs=n.getSource();
        m=vs.getMarker();
        root=(SNode)n.getRoot();
        ln=(SNode)n.at(0); if (ln!=null) cv=ln.splitVar;
        addWindowListener(Common.defaultWindowListener);
        if (n!=null && !n.isLeaf()) {
            setLayout(new BorderLayout());
            add(new SpacingPanel(),BorderLayout.WEST);
            add(new SpacingPanel(),BorderLayout.EAST);
            Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            Button b;
            //bp.add(b=new Button("Preview")); b.addActionListener(this);
            bp.add(b=new Button("OK")); b.addActionListener(this);
            bp.add(b=new Button("Cancel")); b.addActionListener(this);
            add(bp,BorderLayout.SOUTH);
            Panel vp=new Panel(); vp.setLayout(new FlowLayout());
            vp.add(new Label("Variable: "));
            vc=new Choice(); vc.addItemListener(this);
            int j=0;
            while(j<vs.count()) {
                if (!vs.at(j).isInternal())
                    vc.add(vs.at(j).getName());
                j++;
            }
            vc.select(cv.getName());
            vp.add(vc);
            pp=new Panel(); pp.setLayout(new GridLayout(2,1));
            add(vp,BorderLayout.NORTH);
            sp=new Panel(); // split panel
            cp=new Panel(); // canvas panel
            cp.setLayout(new BorderLayout());
            cp.add(sp,BorderLayout.NORTH);
            add(cp);
            constructInnerPlots();
            pack();
        };
    }

    void constructInnerPlots() {
        Dimension scd=null;
        if (sc!=null) scd=sc.getSize(); else scd=new Dimension(400,100);
        if (sc!=null) { pp.remove(sc); sc=null; };
        if (lc!=null) { pp.remove(lc); lc=null; };
        if (st!=null) { sp.remove(st); st=null; };
        if (l1!=null) { sp.remove(l1); l1=null; };
        if (cv.isCat()) { /** split variable is categorical */
        } else { /** split variabel is numerical */
            Stopwatch sw=new Stopwatch();
            sp.setLayout(new FlowLayout());
            sp.add(l1=new Label("split at "));
            spVal=ln.splitValF;
            sp.add(st=new TextField((ln==null)?"0":""+spVal,10));
	    splitText=st.getText();
            st.addActionListener(this);
            sc=new ScatterCanvas(this,cv,root.response,m);
            m.addDepend(sc);
	    //sc.removeMouseListener(sc);
	    sc.addMouseListener(this);
            sc.setFilter(n.data);
            sc.setSize(scd); sc.bgTopOnly=true;
            cp.add(pp); pp.add(sc);
            sw.profile("innerPlots.build graphics");
            double maxD=0, maxDP=0, optSP=0;
            
            /** build deviance plot
                known bugs:
                - uses entire data instead of in-node data
                - is not updated on var change
                todo?
                - plot also left/right deviance? */

            SVar sdv=new SVar("SplitDev",false);
            SVar rxv=new SVar("RankedXV",false);
            int []fullrks=cv.getRanked();
            int []rks=new int[n.data.size()];
            int rki=0;
            double D=0;
            SVar rsp=root.response;
            boolean isCat=rsp.isCat();
            double sumL=0, sumR=0; // regr: sum of y[i] left/right
            int trct=0;
            for (int ix=0;ix<fullrks.length;ix++)
                if (n.data.contains(new Integer(fullrks[ix]))) {
                    rks[rki++]=fullrks[ix];
                    if (!isCat && rsp.at(fullrks[ix])!=null) {
                        sumR+=rsp.atD(fullrks[ix]); trct++;
                    }
                };
            double mnL=0.0, mnR=0.0; // regr: mean left/right
            if (!isCat) {
                mnR=sumR/((double)trct);
                for(int ix=0;ix<rks.length;ix++)
                    if(rsp.at(rks[ix])!=null)
                        D+=(rsp.atD(rks[ix])-mnR)*(rsp.atD(rks[ix])-mnR);
            }
            sw.profile("innerPlots.init");
            if (Common.DEBUG>0)
                System.out.println("input consistency check: rks.len="+rks.length+", rki="+rki);
            int q=0;
            int []cls=null;
            int []tcls=null;
            if (isCat) {
                cls=new int[root.response.getNumCats()];
                tcls=new int[root.response.getNumCats()];
                if (Common.DEBUG>0)
                    System.out.println("ranked: "+rks.length+", classes="+tcls.length);
                q=0; while(q<tcls.length) tcls[q++]=0;
                q=0;
                while(q<rks.length) {
                    int ci=root.response.getCatIndex(rks[q]);
                    if (ci>-1) tcls[ci]++;
                    q++;
                };
                D=Tools.nlogn(rks.length);
            }
            int lct=0, eq=0;
            boolean isOpt=false;
            double lv=0, devL=0, devR=D;
            q=0;
            double XdevL=devL, XdevR=devR; // experimental
            while(q<rks.length) {
                lv=cv.atD(rks[q]);
                if (isOpt)
                    optSP=(lv+maxDP)/2;
                eq=0;
                double deltay=0, deltay2=0;
                while (q<rks.length && lv==cv.atD(rks[q])) {
                    if (isCat) {
                        int ci=rsp.getCatIndex(rks[q]);
                        if (ci>-1) cls[ci]++;
                    } else {
                        if (rsp.at(rks[q])!=null) {
                            double v=rsp.atD(rks[q]);
                            double osumL=sumL, osumR=sumR;
                            sumL+=v; sumR-=v;
                            double pvL=sumL/((double)(lct+1));
                            double pvR=(trct-lct<2)?0:sumR/((double)(trct-lct-1));
                            // mnL=\bar y_t, pvL=\bar y_t+1
                            // update formula for deviances
                            XdevL+=((double)lct)*(pvL*pvL-mnL*mnL)+pvL*pvL-2*osumL*(pvL-mnL)-2*pvL*v+v*v;
                            XdevR+=((double)(trct-lct-1))*pvR*pvR-((double)(trct-lct))*mnR*mnR-2*pvR*sumR+2*mnR*osumR-v*v;
                            mnL=pvL; mnR=pvR;
                        }
                    };
                    rxv.add(cv.at(rks[q]));
                    q++; eq++; lct++;
                };
                sumL+=deltay;
                sumR-=deltay;
                if (Common.DEBUG>0)
                    System.out.println("q="+q+", lv="+lv+", eq="+eq+", lct="+lct);

                double d=D;
                if (isCat) {
                    d-=Tools.nlogn(lct)+Tools.nlogn(rks.length-lct);
                    int cl=0;
                    while(cl<cls.length) {
                        d+=-Tools.nlogn(tcls[cl])+Tools.nlogn(cls[cl])+Tools.nlogn(tcls[cl]-cls[cl]);
                        cl++;
                    }
                } else {
                    d-=XdevL+XdevR;
                }
                isOpt=(d>maxD);
                if (isOpt) { maxD=d; maxDP=lv; optSP=lv; };
                while(eq>0) {
                    sdv.add(new Double(d));                    
                    eq--;
                };
            };
            sw.profile("innerPlots.calculate deviance");
            if (Common.DEBUG>0) {
                System.out.println("Consistency check:");
                System.out.println("sdv length="+sdv.size()+", rxv length="+rxv.size());
                if (isCat) {
                    q=0; while(q<tcls.length) {
                        System.out.println(" class "+q+", tcls="+tcls[q]+", cls="+cls[q]);
                        q++;
                    };
                }
                System.out.println("max.Dev="+maxD+", at "+maxDP+", ergo opt.split="+optSP);
            }
            spVal=optSP;
            st.setText(Tools.getDisplayableValue(spVal));
	    splitText=st.getText();
            SVar[] svl=new SVar[1]; svl[0]=sdv;
            lc=new LineCanvas(this,rxv,svl,m);
            lc.ignoreMarker=true;
            //pp.add(new ScatterCanvas(this,rxv,sdv,m));
            lc.setLineType(LineCanvas.LT_RECT);
            pp.add(lc);
            lc.getXAxis().setValueRange(cv.getMin(),cv.getMax()-cv.getMin());
            lc.getYAxis().setValueRange(0,(maxD>n.sampleDevGain)?maxD:n.sampleDevGain);
            PlotManager pm=sc.getPlotManager();
            if (pm!=null) {
                li=new PlotLine(pm);
                li.setCoordinates(1,2);
                li.setColor(new PlotColor(255,0,0));
                li.set(spVal,-1,spVal,1);
                li.setVisible(true);
                if (!isCat) {
                    lrl=new PlotLine(pm);
                    lrl.setCoordinates(1,1);
                    lrl.setColor(new PlotColor(160,160,160));
                    lrl.set(cv.getMin(),0,spVal,0);
                    lrl.setVisible(true);
                    rrl=new PlotLine(pm);
                    rrl.setCoordinates(1,1);
                    rrl.setColor(new PlotColor(160,160,160));
                    rrl.set(spVal,0,cv.getMax(),0);
                    rrl.setVisible(true);
                    setSplitValue(spVal);
                }
            }
            sw.profile("fixup , draw line");
        }
    }
    
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange()==ItemEvent.SELECTED) {
            String sv=vc.getSelectedItem();
            SVar v=vs.byName(sv);
            if (v!=null && v!=cv) {
                cv=v;
                setMenuBar(null);
                constructInnerPlots();
                pack(); repaint();
            };
        };
    };

    void setSplitValue(double v) {
        spVal=v;
        if (!root.response.isCat()) {
            double sumL=0, sumR=0;
            int ctl=0, ctr=0;
            int i=0,j;
            while(i<n.data.size()) {
                Integer iN=(Integer)n.data.elementAt(i);
                if (iN!=null) {
                    j=iN.intValue();
                    double pv=root.response.atD(j);
                    if (cv.atD(j)<=v) { sumL+=pv; ctl++; }
                    else { sumR+=pv; ctr++; }
                }
                i++;
            }
            double pvL=(ctl>0)?sumL/((double)ctl):0;
            double pvR=(ctr>0)?sumR/((double)ctr):0;
            //System.out.println("ctL="+ctl+", ctR="+ctr+", pvL="+Tools.getDisplayableValue(pvL)+", pvR="+Tools.getDisplayableValue(pvR));
            lrl.set(cv.getMin(),pvL,spVal,pvL);
            rrl.set(spVal,pvR,cv.getMax(),pvR);
        }
        if (li!=null) {
            li.set(spVal,-1,spVal,1);
        }
        sc.repaint();        
    }

    public void mouseClicked(MouseEvent ev) {
	double nsv=sc.getXAxis().getValueForPos(ev.getX());	
	setSplitValue(nsv);
	st.setText(Tools.getDisplayableValue(spVal));
    }

    public void mousePressed(MouseEvent ev) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        String cmd=e.getActionCommand();
        if (Common.DEBUG>0)
            System.out.println("SplitEditor.actionPerformed(\""+cmd+"\") ["+e.toString()+"]\n source="+e.getSource().toString());
        if (e.getSource()==st) {
            double v=0;
            v=Tools.parseDouble(cmd);
	    splitText=cmd;
	    setSplitValue(v);
        } else {
            if (cmd=="Cancel") {
                WinTracker.current.rm(this);
                sc=null; li=null; removeAll();
                dispose();
            }
            if (cmd=="OK") {
		if (splitText!=null && st!=null && splitText.compareTo(st.getText())!=0)
		    setSplitValue(Tools.parseDouble(st.getText())); // set split even if user didnt press enter
		
		if (!cv.isCat() && cv.isNum()) {
                    if (spVal<cv.getMin()||spVal>=cv.getMax()) {
                        new MsgDialog(this,"Invalid split value","The specified split value would result in a single son. No action will be performed.");
                    } else {
                        Vector cp=new Vector();
                        editSuffix++;
                        SNode nt=InTr.makePrunedCopy(root,true,n,true,cp,"Ed_"+root.name+"_"+editSuffix);
			nt.formula=root.formula;
                        
                        /* build the two other chunks here */
                        ProgressDlg pd=new ProgressDlg(null,"Running tree generation plugin ...");
                        pd.setText("Initializing plugin, loading R ...");
                        pd.show();
                        Plugin gt=PluginManager.loadPlugin("PluginGetTreeR");
                        if (gt==null || !gt.initPlugin()) {
                            pd.dispose();
                            new MsgDialog(this,"Plugin init failed","Cannot initialize plugin.\n"+((gt==null)?"Tree generation plugin not found":gt.getLastError()));
                            return;
                        }
                        gt.setParameter("dataset",vs);
			if (root.formula!=null)
			    gt.setParameter("formula",root.formula);
                        gt.checkParameters();
                        pd.setVisible(false);
                        if (!gt.pluginDlg(this)) {
                            pd.dispose();
                            if (gt.cancel) {
                                gt.donePlugin();
                                return;
                            };
                            new MsgDialog(this,"Parameter check failed","Some of your selections are invalid.\n"+gt.getLastError());
                            return;
                        }
                        pd.setProgress(40);
                        pd.setVisible(true);
                        /* we cannot use holdConnection yet, since the method used by the plugin assumes
                            that each time entire dataset must be loaded
                        gt.setParameter("holdConnection",Boolean.TRUE); */
                        gt.setParameter("selectedOnly",Boolean.TRUE);
                        gt.setParameter("registerTree",Boolean.FALSE);
                        SMarker bak=vs.getMarker();
                        SMarker ml=new SMarker(cv.size());
                        SMarker mr=new SMarker(cv.size());
                        int i=0;
                        Vector leftd=new Vector(), rightd=new Vector();
                        while(i<cv.size()) {
                            Object o=cv.at(i);
                            if (o!=null && n.data.contains(new Integer(i))) {
                                try {
                                    double v=((Number)o).doubleValue();
                                    if (v<=spVal) { ml.set(i,true); leftd.addElement(new Integer(i)); };
                                    if (v>spVal) { mr.set(i,true); rightd.addElement(new Integer(i)); };
                                } catch(Exception ex) {};
                            }
                            i++;
                        }
                        if (Common.DEBUG>0)
                            System.out.println("Markers: ml="+ml.marked()+", mr="+mr.marked());
                        vs.setMarker(ml);
                        if (!gt.execPlugin()) {
                            vs.setMarker(bak);
                            pd.dispose();
                            HelpFrame hf=new HelpFrame();
                            hf.t.setText("Left-branch generation failed.\n"+gt.getLastError()+"\n\nDump of R output (if any):\n"+gt.getParameter("lastdump"));
                            hf.setTitle("Plugin execution failed");
                            //hf.setModal(true);
                            hf.show();
                            return;
                        }
                        pd.setProgress(70);
                        vs.setMarker(mr);
                        SNode leftb=(SNode)gt.getParameter("root");
                        if (!gt.execPlugin()) {
                            vs.setMarker(bak);
                            pd.dispose();
                            HelpFrame hf=new HelpFrame();
                            hf.t.setText("Right-branch generation failed.\n"+gt.getLastError()+"\n\nDump of R output (if any):\n"+gt.getParameter("lastdump"));
                            hf.setTitle("Plugin execution failed");
                            //hf.setModal(true);
                            hf.show();
                            return;
                        }
                        SNode rightb=(SNode)gt.getParameter("root");
                        vs.setMarker(bak);

                        pd.setProgress(100);
                        gt.donePlugin();
                        pd.dispose();

                        SNode ntcp=(SNode)cp.elementAt(0);
                        ntcp.add(leftb);
                        ntcp.add(rightb);
                        leftb.splitVar=rightb.splitVar=cv;
                        leftb.splitValF=rightb.splitValF=spVal;
                        leftb.splitIndex=rightb.splitIndex=vs.indexOf(cv.getName());
                        leftb.Cond=cv.getName()+" < "+spVal;
                        rightb.Cond=cv.getName()+" > "+spVal;
                        //leftb.data=leftd; rightb.data=rightd;
                        leftb.splitComp=-1; rightb.splitComp=1;
                        RTree.passDownData(ntcp,leftb); RTree.passDownData(ntcp,rightb);
                        //---
                        SVar vvv;
                        vvv=RTree.getPredictionVar(nt,nt.response);
                        if (vvv!=null) {
                            vs.add(vvv);
                            nt.prediction=vvv;
                            if (vvv.isCat()) {
                                if (vs.globalMisclassVarID!=-1)
                                    RTree.manageMisclassVar(root,vs.at(vs.globalMisclassVarID));
                                else {
                                    SVar vmc=RTree.manageMisclassVar(nt,null);
                                    vs.globalMisclassVarID=vs.add(vmc);
                                }
                            }
                        };
                        TFrame f=new TFrame(nt.name,TFrame.clsTree);
                        TreeCanvas tc=InTr.newTreeDisplay(nt,f);
                        tc.repaint(); tc.redesignNodes();
                        RTree.getManager().addTree(nt);

                        WinTracker.current.rm(this);
                        sc=null; li=null; removeAll();
                        dispose();                        
                    }
                };
            };
        }        
    };
}
