//
//  TreeLoader.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package org.rosuda.klimt;

import java.io.*;
import java.util.*;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class TreeLoader extends Loader {
    public static SNode LoadTree(BufferedReader r, DataRoot dr, String name) {
        return LoadTree(r, dr, name, true);
    }
    
    public static SNode LoadTree(BufferedReader r, DataRoot dr, String name, boolean registerIt) {
        try {
            return Load(r,name,dr,0,null,null,true,registerIt);
        } catch (Exception e) {
            System.out.println("TreeLoader.LoadTree: "+e.getMessage());
            if (Global.DEBUG>0)
                e.printStackTrace();
        }
        return null;
    }


    /* OLD LOADER !! remove as soon as the new loaders take over the functionality */

    public static void checkPolys(SVar psv, SVarSet vset) {
        // check for empty polygons (informative only)
        int i=0;
        while (i<psv.size()) {
            if (psv.at(i)==null)
                Common.addWarning("Case at position "+(i+1)+" has no associated polygons in variable \""+psv.getName()+"\". (This is ok if it was intentional.)");
            i++;
        }
        if (vset!=null && psv.size()<vset.length())
            Common.addWarning("There are "+vset.length()+" cases in the dataset, but only "+psv.size()+" polygons in \""+psv.getName()+"\". Maybe the attached polygons are incomplete.");
    }
    /** reads a tree and dataset froma text file
    the buffer may contain leading and trailing garbage; only the first tree is loaded
    after this method the buffer is either at EOF mark or behind first non-tree line
    this way you can load multiple trees from one file - separated e.g. by one empty line
    <p>
remark: this method can be used to load trees and data separately, but data must be
    present before a tree can be loaded - to load a separate tree for existing data
    pass valid vset to be used with the tree.

    @param r reader to read the data from
    @param vset dataset for storing the read variables and caes
    @return complete tree linked with the specified dataset */

    public static SNode Load(BufferedReader r, String tname, DataRoot dr) throws Exception { return Load(r,tname,dr,0,null,null,false,true); };
    public static SNode Load(BufferedReader r, String tname, DataRoot dr, long clen) throws Exception { return Load(r,tname,dr,0,null,null,false,true); };

    public static SNode Load(BufferedReader r, String tname, DataRoot dr, long clen, String smark, String emark, boolean dsReadOnly, boolean registerIt) throws Exception
    {
        SVarSet vset=dr.getDataSet();
        int nid=1;
        SNode ct=null, root=null;
        long cl=0, cpif=0; int pbg=5;
        boolean inside=false, isTree=false, isData=true, isPoly=false, gotData=false, gotTree=false, gotNames=false;
        boolean preMark=(smark!=null), rdsType=false;
        int presBase=2, oprog=-1, prog;
        ProgressDlg pd=null;
        Vector polyVars=new Vector();
        Vector polySVs=new Vector();
        Vector px=new Vector();
        Vector py=new Vector();
        int polyPt=0;
        int polyVar;
        String curPolyVar=null;
        SVar curPolySV=null;
        MapSegment mses[]=null;
        int msid=-1;

        Common.beginWorking("Loading dataset");
        if (clen>10000 || (vset!=null && vset.count()>0 && vset.at(0)!=null && vset.at(0).size()>2000)) {
            pd=new ProgressDlg("Loading..."); pd.setText("Initializing...");
            pd.show();
        };
        if (clen>100000) pbg=1;
        int prevars=(vset==null)?0:vset.count();
        Stopwatch sw=new Stopwatch(Global.DEBUG<2);

        String dataSep=" "; // default data separator is space

        while (r.ready()) {
            String s=r.readLine(); // read line
            if (preMark) {
                if (s.equals(smark)) preMark=false;
            } else {
                if (emark!=null && s.equals(emark)) break;
                cpif+=s.length();
                if (clen>0) {
                    prog=(((int)((double)cpif/(double)clen*100.0))/pbg)*pbg;
                    if (prog!=oprog) {
                        if (pd!=null) pd.setProgress(prog);
                        oprog=prog;
                    };
                };
                if (s.length()>2 && s.substring(0,2).compareTo("n=")==0) s=""; // n=... id added by rpart
                if (s==null) break;

                if (s.indexOf("node)")==0) {  // node) determines begin of tree part
                    if (isData) gotData=true;
                    isData=false;
                    isTree=true;
                    sw.profile();
                    if (pd!=null) pd.setText((tname==null)?"Loading tree ..":"Loading tree ("+tname+")..");
                    if (Global.DEBUG>0) System.out.println("tree begin found. gotData="+gotData);
	        };

                // we got data if this ain't no tree, s ne '' and data not read yet
                if (vset!=null && !gotData && !isTree && s.length()>0) isData=true;

                if (isData && dsReadOnly && s.length()==0) { // ok, we're outside the data table so it seems
                    gotData=true; isData=false;
                    if (gotTree) { // if we have tree then we're done
                        sw.profile();
                        if (pd!=null) pd.dispose();
                        if (registerIt && root!=null) dr.getTreeRegistry().addTree(root);
                        if (vset!=null) { int i=prevars; while (i<vset.count()) { vset.at(i++).getNotifier().endBatch(); } }
                        Common.endWorking();
                        return root;
                    };
                };

                /**=========== parsing of dataset contents ============*/
                if (isData && !dsReadOnly) {
                    if (s.indexOf("\t\t")>-1) { // we got empty cells here - force NA in those
                        int tti;
                        while ((tti=s.indexOf("\t\t"))>-1) {
                            s=s.substring(0,tti)+"\tNA"+s.substring(tti+1);
                        }
                    }
                    /**--------- HEADER ---------*/
                    if (!gotNames) { // didn't get names yet -> this is header line
                        if (s.length()>0) {
                            if (pd!=null) pd.setText("Loading dataset...");
                            if (s.indexOf("\t")>-1) { // we got tabs in the data, ergo TSV
                                dataSep="\t";
                            } else { // no tabs, ergo SSV, but possible RDS, check that
                                if (s.charAt(0)==' ') { // we used to condition on " too, but that's too dangerous if quoted TSV is used
                                    // RDS ergo add index as first var
                                    rdsType=true; SVar idx=new SVarObj("index",false);
                                    idx.setInternalType(SVar.IVT_Index);
                                    idx.getNotifier().beginBatch();
                                    vset.add(idx);
                                }
                            }
                            StringTokenizer st=new StringTokenizer(s,dataSep);
                            while (st.hasMoreTokens()) {
                                boolean forceCat=false;
                                boolean forceNum=false;
                                boolean hasPoly=false;
                                String nam=st.nextToken();
                                if (nam.length()>0 && s.charAt(0)=='"') // kill pre-&post-"
                                    nam=nam.substring(1,nam.length()-1);
                                int ca=0; boolean hadProbs=false; String on=nam;
                                if (nam.length()>1 && nam.charAt(0)=='/') { // mondiran's /x prefix
                                    char Mpref=nam.charAt(1);
                                    if (Mpref=='D') forceCat=true;
                                    if (Mpref=='C') forceNum=true;
                                    if (Mpref=='P') {
                                        hasPoly=true;
                                        polyVars.addElement(new String(nam));
                                    }
                                    nam=nam.substring(2);
                                };
                                while(ca<nam.length()) {
                                    char c=nam.charAt(ca);
                                    if ((c>='0'&&c<='9')||(c>='a'&&c<='z')||(c>='A'&&c<='Z')||c=='.') {} else {
                                        hadProbs=true;
                                        nam=nam.replace(c,'.');
                                    }
                                    ca++;
                                }
                                if (hadProbs) {
                                    ca=0;
                                    while(ca<nam.length() && nam.charAt(ca)=='.') ca++;
                                    if (ca>0) nam=nam.substring(ca);
                                    if (nam.length()<1 || (nam.charAt(0)>='0' && nam.charAt(0)<='9'))
                                        nam="v."+nam;
                                    Common.addWarning("RTree.Load: Variable name \""+on+"\" contains invalid characters and will be replaced by \""+nam+"\".");
                                }
                                SVar nv=null;
                                if (vset.add(nv=new SVarObj(nam))<0) {
                                    Common.addWarning("RTree.Load: Variable \""+nam+"\" occurs more than once in the header. Renaming to \""+nam+"."+vset.count()+"\".");
                                    Common.addWarning("            Please note that this variable won't be used in classifiers. You have to fix the problem in your dataset.");
                                    if (vset.add(nv=new SVarObj(nam+"."+vset.count()))<0) {
                                        Common.addWarning("RTree.Load: Failed to create the substitute variable.");
                                        Common.addWarning("            Trying to use \"unknown.var."+vset.count()+"\" instead.");
                                        if (vset.add(nv=new SVarObj("unknown.var."+vset.count()))<0) {
                                            Common.addWarning("RTree.Load: Failed to create even that variable. Somehting's terribly wrong. Expect more errors to follow.");
                                        };
                                    };
                                };
                                nv.getNotifier().beginBatch();
                                if (hasPoly) polySVs.add(nv);
                                // forceCat and forceNum is NOT used
			};
                            gotNames=true; // yeah, we got the variable names
		    };
		} else { // inside the table

                    /**----------- table contents - values ------------*/
                    if (s.length()==0 || isPoly) { // empty line .. hrmm ...
                        if (!isPoly && polyVars.size()>0) { // ok, we need polygons
                            isPoly=true;
                        };
                        if (isPoly) {
                            if (s.length()==0) {
                                // we should save any unfinished px/py entries
                                if (msid>=0) {
                                    double xx[]=new double[px.size()];
                                    double yy[]=new double[px.size()];
                                    int j=0;
                                    while (j<xx.length) {
                                        xx[j]=((Double)px.elementAt(j)).doubleValue();
                                        yy[j]=((Double)py.elementAt(j)).doubleValue();
                                        j++;
                                    };
                                    mses[msid].add(xx,yy,false);
                                    msid=-1;
                                    px.removeAllElements();
                                    py.removeAllElements();
                                }
                            } else {
                                int t1=s.indexOf("\t");
                                if (t1>0) {
                                    String p1=s.substring(0,t1);
                                    s=s.substring(t1+1);
                                    int t2=s.indexOf("\t");
                                    String p2=null;
                                    if (t2>-1 || (s.length()>1 && s.charAt(0)=='/')) { // polygon header line
                                        if (t2>-1) {
                                            p2=s.substring(0,t2);
                                            s=s.substring(t2+1);
                                        } else
                                            p2=s;
                                        // p1=value, p2=var name

                                        // we should save any unfinished px/py entries
                                        if (msid>=0) {
                                            double xx[]=new double[px.size()];
                                            double yy[]=new double[px.size()];
                                            int j=0;
                                            while (j<xx.length) {
                                                xx[j]=((Double)px.elementAt(j)).doubleValue();
                                                yy[j]=((Double)py.elementAt(j)).doubleValue();
                                                j++;
                                            };
                                            mses[msid].add(xx,yy,false);
                                            msid=-1;
                                        }
                                        if (!p2.equals(curPolyVar)) { // new poly line?
                                                                      // finish what's undone
                                            if (curPolySV!=null && mses!=null) {
                                                SVar psv=new SVarObj(curPolySV.getName()+".Map");
                                                psv.getNotifier().beginBatch();
                                                psv.setContentsType(SVar.CT_Map);
                                                int j=0;
                                                while (j<mses.length) {
                                                    psv.add(mses[j]);
                                                    j++;
                                                };
                                                if (vset!=null)
                                                    vset.add(psv);
                                                mses=null; px.removeAllElements(); py.removeAllElements();
                                                curPolySV=null; curPolyVar=null; msid=-1;
                                                checkPolys(psv,vset);
                                            };
                                            int i=0;
                                            while (i<polyVars.size()) {
                                                String pv=(String)polyVars.elementAt(i);
                                                if (pv!=null && pv.equals(p2)) { // ok, found new var
                                                    curPolySV=(SVar)polySVs.elementAt(i);
                                                    curPolyVar=p2;
                                                    mses=new MapSegment[curPolySV.size()];
                                                    px.removeAllElements(); py.removeAllElements();
                                                    break;
                                                }
                                                i++;
                                            };
                                            if (i>=polyVars.size()) {
                                                Common.addWarning("While processing polygons: Cannot find polygon variable \""+p2+"\". Ignoring further polygons.");
                                                isPoly=false;
                                            };
                                            // ok, found a new valid polygon variable
                                        };
                                        // existing header line
                                        //System.out.println("looking for \""+p1+"\" in "+p2+"/"+curPolySV);
                                        int j=0;
                                        double vv=Tools.parseDouble(p1);
                                        while (j<curPolySV.size()) {
                                            if ((curPolySV.isNum() && curPolySV.atD(j)==vv) || curPolySV.atS(j).compareTo(p1)==0) {
                                                msid=j;
                                                if (mses[j]==null) mses[j]=new MapSegment();
                                                break;
                                            };
                                            j++;
                                        };
                                        if (j>=curPolySV.size()) {
                                            Common.addWarning("While processing polygons: cannot find corresponding value \""+p1+"\" in variable \""+p2+"\". Ignoring this polygon.");
                                            msid=-1;
                                        };
                                        px.removeAllElements();
                                        py.removeAllElements();
                                        polyPt=0;
                                    } else { // t2=-1 -> p1=x, s=y entry
                                        double x=Tools.parseDouble(p1);
                                        double y=Tools.parseDouble(s);
                                        px.add(new Double(x));
                                        py.add(new Double(y));
                                        polyPt++;
                                    };
                                }
                            };
                        };
                        if (!isPoly) {
                            // ok, we're outside the data table so it seems
                            if (curPolySV!=null && mses!=null) {
                                SVar psv=new SVarObj(curPolySV.getName()+".Map");
                                psv.getNotifier().beginBatch();
                                int j=0;
                                while (j<mses.length) {
                                    psv.add(mses[j]);
                                    j++;
                                };
                                if (vset!=null)
                                    vset.add(psv);
                                mses=null; px.removeAllElements(); py.removeAllElements();
                                curPolySV=null; curPolyVar=null; msid=-1;
                                checkPolys(psv,vset);
                            };

                            gotData=true; isData=false;
                            if (gotTree) { // if we have tree then we're done
                                sw.profile();
                                if (pd!=null) pd.dispose();
                                if (registerIt && root!=null) dr.getTreeRegistry().addTree(root);
                                if (vset!=null) { int i=prevars; while (i<vset.count()) { vset.at(i++).getNotifier().endBatch(); } }
                                Common.endWorking();
                                return root;
                            };
                        };
                    } else {

                        /**---------- string/num data -----------*/

                        //System.out.println("Ok, case "+vset.at(0).size()+" found.");
                        if (dataSep==" ") { // space is separator so we need to protect "" enclosed strings
                            if (s.indexOf("\"")>-1) { // replace spaces by _ inside " - this is
                                                      // terribly inefficient!!
                                StringBuffer sb=new StringBuffer(s);
                                int j=s.indexOf("\"");
                                while (j>-1) {
                                    j++;
                                    while (j<s.length() && s.charAt(j)!='"') {
                                        if (s.charAt(j)==' ')
                                            sb.setCharAt(j,'_');
                                        j++;
                                    };
                                    if (j+1>=s.length())
                                        j=-1;
                                    else
                                        j=s.indexOf("\"",j+1);
                                };
                                s=sb.toString();
                            };
                        };
                        int i=0;
                        StringTokenizer st=new StringTokenizer(s,dataSep);
                        while (st.hasMoreTokens()) {
                            if (i>=vset.count()) {
                                Common.addWarning("RTree.Load: row "+(vset.at(i).size()+1)+" has more columns than the header. Ignoring exceeding column(s)."); break;
                            };
                            Object o=null;
                            String t=st.nextToken();
                            t=t.trim();
                            if (t.length()>0 && t.charAt(0)=='"') { // kill quoting "
                                t=t.substring(1,t.length()-1);
                                // for RDS files quotes denote factor variables - except for index
                                if (rdsType && i>0 && !vset.at(i).isCat()) vset.at(i).categorize();
                            }
                            boolean isnumber=false;
                            int a;

                            try {
                                Float f=null;
                                f=Float.valueOf(t);
                                if (t.indexOf('.')!=-1)
                                    vset.at(i).add(f);
                                else
                                    vset.at(i).add(new Integer(f.intValue()));
                                isnumber=true;
                            } catch (NumberFormatException E) {};
                            if (!isnumber) {
                                if (vset.at(i).size()==vset.at(i).getMissingCount()) // if the var contains more than missing values
                                    vset.at(i).categorize(); // string values are categorized by default
                                if ((t.length()==1 && (t.charAt(0)==0xa5 || t.charAt(0)==8226)) || t.compareTo("?")==0 || t.compareTo("NA")==0) // the 8226 may sound weird, but it's the unicode representation of the 0xA5 character in MacOS X environment
                                    vset.at(i).add(null);
                                else {
                                    if (!vset.at(i).add(t)) { // if the add failed then we have a non-numerical entry in a numerical var.
                                        Common.addWarning("RTree.Load: row "+(vset.at(i).size()+1)+", column "+(i+1)+" - non-numerical value ("+t+") encountered in a numerical variable ("+vset.at(i).getName()+"). Value will be treated as missing.");
                                        vset.at(i).add(null);
                                    };
                                };
                            };
                            i++;
                        };
                    };
                };
                };

                int pres=-1;

                if (isTree) {
                    //System.out.println(">> \""+s+"\"");

                    pres=s.indexOf(')');
                    //System.out.println("pres="+pres+", inside="+inside+", isTree="+isTree);
                    if ((inside)&&(pres<2)) { // we're "outside" again if we find no ')' or not at char 2 or higher
                        if (Global.DEBUG>0)
                            System.out.println("End of tree. gotData="+gotData);
                        sw.profile();
                        if (root!=null) root.getRootInfo().name=tname;
                        int vsp;
                        if ((vsp=s.indexOf(" ~ "))>-1) {
                            if (Global.DEBUG>0)
                                System.out.println("Formula found: "+s);
                            root.getRootInfo().formula=s;
                            if (pd!=null) pd.setText("Creating prediction variable...");
                            s=s.substring(0,vsp);
                            if (Global.DEBUG>0)
                                System.out.println("Predicted variable: "+s);
                            if (vset!=null) {
                                int iii=0; SVar clv=null;
                                while (iii<vset.count()) {
                                    if (vset.at(iii)!=null && vset.at(iii).getName().compareTo(s)==0) { clv=vset.at(iii); break; };
                                    iii++;
                                };
                                root.getRootInfo().response=clv;
                                if (Global.DEBUG>0)
                                    System.out.println((clv==null)?"Dependent variable not found in dataset!":"Dependent varaibe found in dataset.");
                                if (clv!=null) {
                                    root.calculateSampleDeviances(); // update deviances based on the sample dataset
                                    if (registerIt && !Common.noIntVar) { // in some cases we don't want the additional vars to be built
                                        SVar vvv;
                                        vvv=Klimt.getPredictionVar(root,clv);
                                        vset.add(vvv);
                                        root.getRootInfo().prediction=vvv;
                                        dr.getTreeRegistry().registerTree(root,root.getRootInfo().name);
                                        if (vvv!=null && clv.isCat()) {
                                            //vvv.name="tree-clsf-"+vset.classifierCounter;
                                            vset.classifierCounter++;
                                            if (vset.globalMisclassVarID!=-1)
                                                Klimt.manageMisclassVar(root,vset.at(vset.globalMisclassVarID));
                                            else {
                                                SVar vmc=Klimt.manageMisclassVar(root,null);
                                                vset.globalMisclassVarID=vset.add(vmc);
                                            }
                                        }
                                    }
                                };
                            };
                        };
                        if ((gotData)||(vset==null)) {
                            sw.profile();
                            if (pd!=null) pd.dispose();
                            if (registerIt && root!=null) dr.getTreeRegistry().addTree(root);
                            if (vset!=null) { int i=prevars; while (i<vset.count()) { vset.at(i++).getNotifier().endBatch(); } }
                            Common.endWorking();
                            return root;
                        };
                        gotTree=true; isTree=false;
                    };
                };

                if (isTree) {
                    if (!inside) // we're "inside" the tree block only when we encounter ' 1) ...' line
                        if ((pres>=1)&&(s.charAt(0)!='n')) {
                            presBase=pres; inside=true;
                            //System.out.println(" just got inside: base="+presBase);
                        };

                    if (inside) { // process only if we're for sure in the tree block
                                  //System.out.println("orig.pres="+pres+", base="+presBase);
                        pres-=presBase;
                        pres/=2;

                        SNode sn=new SNode();
                        sn.id=nid; nid++;

                        if (Global.DEBUG>0)
                            System.out.println("new node, pres="+pres+"/"+cl+", s=\""+s+"\"");

                        while ((ct!=null)&&(pres<cl)) {
                            ct=(SNode)ct.getParent(); cl--;
                        };

                        int vi1=s.lastIndexOf('(');
                        int vi2=s.lastIndexOf(')');
                        boolean isLast=(s.lastIndexOf('*')>vi2);

                        boolean isRegrTree=false;
                        String vs=null;
                        if (vi1>-1) { /* categ. response */
                            vs=s.substring(vi1+1,vi2).trim();
                            s=s.substring(0,vi1-1);
                        } else { /* regression */
                            vi2=s.lastIndexOf('*')-1; isRegrTree=true;
                            if (vi2<0) vi2=s.length()-1;
                            while(vi2>0&&s.charAt(vi2)==' ') vi2--;
                            if(vi2>0) s=s.substring(0,vi2+1);
                        };
                        if (Global.DEBUG>0)
                            System.out.println((!isRegrTree)?"class.tree, s=\""+s+"\", vs=\""+vs+"\"":"regression tree, s=\""+s+"\"");

                        int i=s.length()-1;
                        do { // if the class name contains spaces, we must be more careful as it's not quoted
                            while ((i>0)&&(s.charAt(i)!=' ')) i--;
                        } while (i>0 && (s.charAt(i-1)<'0' || s.charAt(i-1)>'9') && (i-->0));
                        sn.Name=s.substring(i+1);
                        sn.predValD=Tools.parseDouble(sn.Name);
                        while ((i>0)&&(s.charAt(i)==' ')) i--;
                        vi2=i+1;
                        while ((i>0)&&(s.charAt(i)!=' ')) i--;
                        sn.F1=Float.valueOf(s.substring(i+1,vi2)).doubleValue()/(isRegrTree?1:2);
                        while ((i>0)&&(s.charAt(i)==' ')) i--;
                        vi2=i+1;
                        while ((i>0)&&(s.charAt(i)!=' ')) i--;
                        sn.Cases=Integer.parseInt(s.substring(i+1,vi2));
                        vi2=i;
                        i=0;
                        while ((i<vi2)&&(s.charAt(i)!=')')) i++;
                        sn.Cond=s.substring(i+2,vi2);

                        /*
                         String vname=null;
                         if (sn.Cond.indexOf(' ')>0)
                         vname=sn.Cond.substring(0,sn.Cond.indexOf(' '));
                         if (vname!=null) {

                         };*/
                        //System.out.println("\""+vs+"\" fi="+vs.indexOf(' '));
                        if (vs!=null) {
                            int fi;
                            while ((fi=vs.indexOf(' '))>0) {
                                sn.V.addElement(new Float(vs.substring(0,fi)));
                                vs=vs.substring(fi+1);
                            };
                            sn.V.addElement(new Float(vs));
                        };

                        //System.out.println(", cond=\""+sn.Cond+"\", isLast="+isLast);
                        sn.setSource(vset);

                        if (gotData) { // create data index for the current node
                            if (ct==null) {
                                int I=0, L=(vset.at(0)!=null)?vset.at(0).size():0;
                                sn.data=new Vector((L<5)?5:L);
                                while (I<L) { sn.data.addElement(new Integer(I)); I++; };
                            } else {
                                sn.data=new Vector();
                                int split, cmp=0;
                                split=sn.Cond.indexOf(':');
                                if (split<0) { // problem with = is that it may be part of <= or >=
                                    int eqs=sn.Cond.indexOf('=');
                                    if (eqs>=0) {
                                        int lsg=sn.Cond.indexOf('<');
                                        int gsg=sn.Cond.indexOf('>');
                                        int d1=lsg-eqs; if (d1<0) d1=-d1;
                                        if (lsg<0) d1=9;
                                        int d2=gsg-eqs; if (d2<0) d2=-d2;
                                        if (gsg<0) d2=9;
                                        if (d1>1 && d2>1) split=eqs;
                                    }
                                }
                                if (split<0) { cmp=-1; split=sn.Cond.indexOf('<'); };
                                if (split<0) { cmp=1; split=sn.Cond.indexOf('>'); };
                                // rpart uses = instead of :
                                //if (split<0 && sn.Cond.indexOf('=')>0) { cmp=0; split=sn.Cond.indexOf('='); };

                                if (split>=0) {
                                    String varn=sn.Cond.substring(0,split).trim();
                                    String cond=sn.Cond.substring(split+1).trim();
                                    if (cond.charAt(0)=='=')
                                        cond=cond.substring(1);
                                    SVar V=vset.byName(varn);
                                    if (Global.DEBUG>0)
                                        System.out.println("  splt var=\""+varn+"\", cond=\""+cond+"\"");
                                    sn.splitVar=V;
                                    sn.splitIndex=vset.indexOf(varn);
                                    sn.splitComp=cmp;
                                    if (V!=null) {
                                        boolean lexi=!V.isNum();
                                        double limit=0;
                                        try {
                                            if (!lexi) limit=Float.valueOf(cond).doubleValue();
                                        } catch (NumberFormatException E) {
                                            if (Global.DEBUG>0)
                                                System.out.println("RTree.Load: failed to parse number ("+cond+") in split definition ("+E.getMessage()+") although lexi is not defined");
                                        };
                                        sn.splitVal=cond;
                                        sn.splitValF=limit;
                                        for (Enumeration e=ct.data.elements(); e.hasMoreElements();) {
                                            Object o=e.nextElement();
                                            int I=((Integer)o).intValue();
                                            if (I<V.size()) {
                                                if (lexi) {
                                                    Object oo=V.at(I);
                                                    if (oo==null) oo=SVar.missingCat;
                                                    if (cond.indexOf(",")>-1) { // more cats specified
                                                        StringTokenizer st=new StringTokenizer(cond,",");
                                                        while (st.hasMoreTokens()) {
                                                            String cd=st.nextToken();
                                                            int cr=oo.toString().compareTo(cd);
                                                            if (cr>0) cr=1;
                                                            if (cr<0) cr=-1;
                                                            if (cr==cmp)
                                                                sn.data.addElement(o);
                                                        };
                                                    } else {
                                                        int cr=oo.toString().compareTo(cond);
                                                        if (cr>0) cr=1;
                                                        if (cr<0) cr=-1;
                                                        if (cr==cmp) sn.data.addElement(o);
                                                    };
                                                } else {
                                                    if (!V.isMissingAt(I)) {
                                                        double F=V.atF(I);
                                                        if (((cmp==0)&&(F==limit))||
                                                            ((cmp==-1)&&(F<limit))||
                                                            ((cmp==1)&&(F>limit))) sn.data.addElement(o);
                                                    };
                                                };
                                            };
                                        };
                                    };
                                };
                            };
                        };


                        if (ct==null) {
                            root=ct=sn;
                            cl=1;
                        } else {
                            ct.add(sn);
                            if (!isLast) { ct=sn; cl++; };
                        };
                    };
                };
	    };
	};

        if (curPolySV!=null && mses!=null) {
            if (msid>=0) {
                double xx[]=new double[px.size()];
                double yy[]=new double[px.size()];
                int j=0;
                while (j<xx.length) {
                    xx[j]=((Double)px.elementAt(j)).doubleValue();
                    yy[j]=((Double)py.elementAt(j)).doubleValue();
                    j++;
                };
                mses[msid].add(xx,yy,false);
                msid=-1;
            }

            SVar psv=new SVarObj(curPolySV.getName()+".Map");
            psv.setContentsType(SVar.CT_Map);
            int j=0;
            while (j<mses.length) {
                psv.add(mses[j]);
                j++;
            };
            if (vset!=null)
                vset.add(psv);
            checkPolys(psv,vset);
        }

        sw.profile();
        if (pd!=null) pd.dispose();
        if (registerIt && root!=null) dr.getTreeRegistry().addTree(root);
        if (vset!=null) { int i=prevars; while (i<vset.count()) { vset.at(i++).getNotifier().endBatch(); } }
        Common.endWorking();
        return root;
    }
}
