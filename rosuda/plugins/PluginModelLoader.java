//
//  PluginModelLoader.java
//  Klimt
//
//  Created by Simon Urbanek on Sun Aug 01 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.plugins;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import org.rosuda.JRclient.*;
import org.rosuda.ibase.*;
import org.rosuda.klimt.*;

public class PluginModelLoader extends Plugin implements ActionListener {
    String imagePath;
    Rconnection rc;
    Dialog d;
    SVarSet vset;
    DataRoot dr;
    boolean reg=true;
    
    public boolean initPlugin() {
        name="Model loader";
        desc="Allows the user to select a model from a saved image";
        return true;
    }

    public void setParameter(String par, Object val) {
        if (par.equals("image"))
            imagePath=(String)val;
        if (par.equals("dataset"))
            vset=(SVarSet)val;
        if (par.equals("dataroot")) {
            dr=(DataRoot)val;
            vset=dr.getDataSet();
        }
        if (par.equals("register"))
            reg=((Boolean)val).booleanValue();
    }

    public Object getParameter(String par) {
        if (par.equals("image"))
            return imagePath;
        return super.getParameter(par);
    }

    public boolean checkParameters() {
        if (imagePath==null || vset==null) return false;
        return true;
    }
    
    public boolean pluginDlg(Frame f) {
        org.rosuda.util.ProgressDlg pd=new org.rosuda.util.ProgressDlg("Loading model image ...");
        pd.setText("Initializing plugin, loading R ...");
        pd.show();
        try {
            rc=new Rconnection();
        } catch (Exception e) {
            System.out.println("Attempt to connect Rserve failed: "+e);
            try {
                System.out.println("Starting Rserve ...");
                Runtime.getRuntime().exec("R CMD Rserve");
                int attp=0;
                while (attp<6) {
                    Thread.sleep(500);
                    try {
                        rc=new Rconnection();
                        break;
                    } catch (Exception e2) {
                        System.out.println("Attempt to connect Rserve failed (but still trying): "+e2);
                        err="Cannot connect to R, "+e2;
                        rc=null;
                    }
                    attp++;
                }
                if (rc==null) System.out.println("Giving up.");
            } catch (Exception e3) {
                System.out.println("Cannot start Rserve: "+e3);
                err="Cannot start Rserve, "+e3;
                rc=null;
            }
        }
        if (rc==null) {
            System.out.println("Failed to connect Rserve :(");
            pd.dispose();
            return false;
        }
        try {
            pd.setText("Loading image ...");
            REXP x=rc.eval("load(\""+imagePath+"\")");
            String s=x.asString();
            String sa[]=null;
            if (s!=null) {
                sa=new String[1]; sa[0]=s;
            } else {
                Vector v=x.asVector();
                if (v!=null) {
                    sa=new String[v.size()];
                    int lv=0;
                    while (lv<sa.length) {
                        sa[lv]=((REXP) v.elementAt(lv)).asString();
                        lv++;
                    }
                }
            }
            System.out.println("Found "+sa.length+" objects.");
            
            Vector pm=new Vector();
            Vector md=new Vector();
            if (sa!=null && sa.length>0) {
                int i=0;
                while (i<sa.length) {
                    pd.setText("Checking object "+sa[i]+" ...");
                    pd.setProgress(100*i/sa.length);
                    pd.repaint();
                    Thread.currentThread().yield();
                    REXP x2=rc.eval("class("+sa[i]+")[1]");
                    String c=x2.asString();
                    System.out.println("Object "+sa[i]+", class "+c);
                    if (c!=null) {
                        if (c.equals("lm") || c.equals("glm")) {
                            pm.addElement(sa[i]);
                            md.addElement(sa[i]+" ("+c+")");
                        } else if (c.equals("list")) {
                            // ok, let's check the first element
                            REXP x3=rc.eval("class("+sa[i]+"[[1]])[1]");
                            c=x3.asString();
                            if (c!=null && (c.equals("lm") || c.equals("glm"))) {
                                REXP x4=rc.eval("as.integer(length("+sa[i]+"))");
                                System.out.println("length="+x4);
                                int l=x4.asInt();
                                pm.addElement("*"+sa[i]);
                                md.addElement(sa[i]+" (list of "+l+" "+c+" models)");
                            }
                        }
                    }
                    i++;
                }
                System.out.println("User can select from "+pm.size()+" models/lists.");
                if (pm.size()<1) {
                    pd.dispose();
                    err="Image contained no models or model lists.";
                    return false;
                }

                Button b,b2;
                d=new Dialog(f,"Select models to load",true);
                d.setBackground(Color.white);
                d.setLayout(new BorderLayout());
                d.add(new org.rosuda.util.SpacingPanel(),BorderLayout.WEST);
                d.add(new org.rosuda.util.SpacingPanel(),BorderLayout.EAST);
                Panel bp=new Panel(); bp.setLayout(new FlowLayout());
                bp.add(b=new Button("OK"));bp.add(b2=new Button("Cancel"));
                d.add(bp,BorderLayout.SOUTH);
                int mts=pm.size();
                List l=new List((mts>10)?10:mts,true);
                int j=0;
                while (j<mts) {
                    l.add((String)md.elementAt(j));
                    l.select(j);
                    j++;
                }
                d.add(l);
                d.pack();
                b.addActionListener(this);b2.addActionListener(this);
                pd.setVisible(false);
                d.setVisible(true);
                if (cancel) {
                    d.dispose();
                    pd.dispose();
                    err="Cancelled by user.";
                    return false;
                }
                pd.setProgress(0);
                pd.setText("Loading selected models ...");
                Thread.currentThread().yield();
                System.out.println("back for good ... ");
                j=0;
                while (j<mts) {
                    if (l.isIndexSelected(j)) {
                        String m=(String) pm.elementAt(j);
                        pd.setText("Loading object "+m+" ...");
                        pd.setProgress(100*j/mts);
                        Thread.currentThread().yield();
                        if (m.charAt(0)=='*') {
                            m=m.substring(1);
                            REXP x5=rc.eval("as.integer(length("+m+"))");
                            int l2=x5.asInt();
                            int i2=0;
                            while (i2<l2) {
                                i2++;
                                pd.setText("Loading selected models ...");
                                Thread.currentThread().yield();
                                pd.setText("Loading "+m+", model "+i2+" of "+l2);
                                addModel(m+"[["+i2+"]]",m+"_"+i2);
                            }
                        } else
                            addModel(m,m);
                    }
                    j++;
                }
                pd.dispose();
            } else {
                err="Image contained no objects.";
                return false;
            }
        } catch (Exception e) {
            err="Cannot load image, "+e;
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void addModel(String m, String name) {
        try {
            REXP x=rc.eval("predict("+m+", type=\"response\")");
            double[] d= x.asDoubleArray();
            if (d!=null) {
                SVar v = new SVarFixDouble("P_i_"+name, d, false);
                v.setInternalType(SVar.IVT_Prediction);
                vset.add(v);
            }
            {
                x=rc.eval("as.character(as.list(attr(terms("+m+"),\"variables\"))[[attr(terms("+m+"),\"response\")+1]])");
                String rvn=x.asString();
                SVar resp=vset.byName(rvn);
                String c0=(resp==null)?"0":resp.getCatAt(0).toString();
                String c1=(resp==null)?"1":resp.getCatAt(1).toString();
                SVar v=new SVarObj("C_i_"+name, true);
                int i=0;
                while (i<d.length) {
                    v.add((d[i]<0.5)?c0:c1);
                    i++;
                }
                v.setInternalType(SVar.IVT_Prediction);
                vset.add(v);
                if (reg && dr!=null) {
                    SNode n=new SNode();
                    n.getRootInfo().name="im_"+name;
                    n.getRootInfo().prediction=v;
                    n.getRootInfo().response=resp;
                    dr.getTreeRegistry().addTree(n);
                    System.out.println("Registered "+m+": "+n.getRootInfo().prediction+", "+n.getRootInfo().response);
                }
            }
            x=rc.eval("resid("+m+", type=\"response\")");
            d= x.asDoubleArray();
            if (d!=null) {
                SVar v = new SVarFixDouble("R_i_"+name, d, false);
                v.setInternalType(SVar.IVT_Resid);
                vset.add(v);
            }
        } catch (Exception e) {
            System.out.println("Loading model \""+m+"\" failed: "+e);
            e.printStackTrace();
        }
    }
    
    public boolean execPlugin() {
        return true;
    }

    /** activated if a button was pressed. It determines whether "cancer" was pressed or OK" */
    public void actionPerformed(ActionEvent e) {
        cancel=!e.getActionCommand().equals("OK");
        d.setVisible(false);
    }
}
