package org.rosuda.JGR;
//
//  RTalk.java
//  JGR
//
//  Created by Markus Helbig on Thu Mar 11 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.io.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.JRI.*;
import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.toolkit.*;

public class RController {

    public static Object dummy = new Object();


    public static void runCmd(String cmd) {
        JGR.R.eval(cmd);
    }

    public static String getRHome() {
        REXP x = JGR.R.eval("R.home()");
        if (x != null && x.asStringArray()!=null) return x.asStringArray()[0];
        return "";
    }

    public static String[] getRLIBS() {
        REXP x = JGR.R.eval(".Library");
        if (x != null && x.asStringArray()!=null) return x.asStringArray();
        return null;
    }
    
    public static String[] getDefaultPackages() {
    	REXP x = JGR.R.eval("getOption(\"defaultPackages\")");
    	if (x != null && x.asStringArray()!=null) return x.asStringArray();
        return new String[] {};
    }

    //code completion
    public static String[] completeCommand(String partOfCmd) {
    	int s = partOfCmd.length()-1;
        if (partOfCmd.trim().length() == 0) return null;
        REXP cmds = JGR.R.eval(".completeCommand(\""+partOfCmd+"\")");
        String[] c = null;
        if (cmds != null && (c=cmds.asStringArray()) != null) return c;
        return c;
    }

    //filecompletion i'm not really using R here, but why not put this function too in the RController
    public static String completeFile(String part) {
        part = part.replaceFirst("~",System.getProperty("user.home"));
        int tl = part.length();
        int ls = tl - 1, fb = 0;
        if (tl == 0) ls = 0;
        String dir = null;
        boolean working = false;
        String fn = null;
        if (System.getProperty("os.name").startsWith("Windows")) part = part.replaceAll(":/","://");
        while (ls > 0 && part.charAt(ls) != '/') {
            System.out.println(part.charAt(ls));
            ls--;
        }
        if (ls == 0 && (tl == 0 || part.charAt(ls) != '/'))
            working = true;
        dir = working ? "." : ( (ls == 0) ? "/" : (part.substring(0, ls)));
        fb = ls;
        if (fb < tl && part.charAt(fb) == '/')
            fb++;
        fn = (fb < tl) ? part.substring(fb) : "";
        File directory = null;
        String[] cont = null;
        if ( (directory = new File(dir)).exists())
            cont = directory.list();
        if (cont == null)
            return null;
        int firstMatch = -1, matches = 0;
        String common = null;
        for (int i = 0; i < cont.length; i++) {
            String sx = cont[i];
            if (sx.startsWith(fn)) {
                if (matches == 0) {
                    firstMatch = i;
                    common = sx;
                }
                else {
                    common = commonWithPrefix(common, sx);
                }
                matches++;
            }
        }
        if (common != null) {
            String fnp = common.replace('\\','/');
            File tfile = null;
            boolean isDir = false;
            fnp = ( (dir == ".") ? fnp :
                   ( (dir == "/") ? ("/" + fnp) :
                    (dir + "/") + fnp));
            if ( (tfile = new File(fnp)).exists() && (isDir = tfile.isDirectory()))
                fnp = fnp + "/";
            if (fnp.endsWith("//"))
                fnp = "";
            return fnp.replaceFirst(part, "");
        }
        return null;
    }

    /* get current available keywords*/
    public static String[] getKeyWords() {
        REXP x = JGR.R.eval(".refreshKeyWords()");
        String[] r = null;
        if (x != null && (r=x.asStringArray()) != null) return r;
        return r;
    }
    
    public static String[] getObjects() {
        REXP x = JGR.R.eval(".refreshObjects()");
        String[] r = null;
        if (x != null && (r=x.asStringArray()) != null) return r;
        return r;
    }


    /* get current available function names for help*/
    public static List getFunctionNames() {
        List fkt = new ArrayList();
        fkt.add("");
        REXP x = JGR.R.eval("length(search())");
        int pos = 2, maxpos = 2;
        if (x == null && x.asIntArray() != null) return fkt;
        if ((maxpos = x.asIntArray()[0])==0) return fkt;
        while (pos <= maxpos) {
            REXP y = JGR.R.eval("ls(pos="+pos+", all.names=TRUE, pattern=\"^\\\\w+\")");
            if (y==null) return fkt;
            String[] result = y.asStringArray();
            for (int i = 0; i < result.length; i++) {
                String sy = result[i];
                REXP z = JGR.R.eval("try(class("+sy+"),silent=TRUE)");
                if (z != null && z.asString() != null && z.asString().equals("function")) fkt.add(sy);
            }
            pos++;
        }
        Collections.sort(fkt);
        return fkt;
    }

    /* refresh r-objects which ar in the pos=0 environment*/
    public static void refreshObjects() {
        JGR.DATA.clear();
        JGR.OTHERS.clear();
        JGR.MODELS.clear();
        String[] res1;
        REXP x = JGR.R.eval("ls()");
        if (x!= null && (res1 = x.asStringArray()) != null) {
            for (int i = 0; i < res1.length; i++) {
                String sx = res1[i];
                    if (!sx.equals("last.warning")) {
                    if (isClass(sx,"data.frame")) JGR.DATA.add(createDataFrame(sx));
                    else if (isClass(sx,"table")) JGR.DATA.add(createTable(sx));
                    else if (isClass(sx,"lm")) JGRObjectManager.models.add(createModel(sx,RObject.LM));
                    else if (isClass(sx,"glm")) JGRObjectManager.models.add(createModel(sx,RObject.GLM));
                    //else if (isClass(sx,"aov")) RObjectManager.models.add(createModel(sx,RObject.ANOVA));
                    else if (isClass(sx,"list")) JGR.OTHERS.add(createList(sx));
                    else if (isClass(sx,"matrix")) JGR.OTHERS.add(createMatrix(sx));
                    else {
                    	RObject other = createOther(sx);
                    	if (other != null) JGR.OTHERS.add(other);
                    }
                }
            }
        }
        else return;
    }


    /* refresh packages (loaded and availables)*/
    public static Object[][] refreshPackages() {
        Object[][] pkg = null;
        Hashtable loadedP = new Hashtable();
        REXP x = JGR.R.eval("sort(.packages(all.available=T))");
        String[] res;
        if (x != null && x.asStringArray() != null) {
            REXP y = JGR.R.eval("(.packages())");
            if (y != null && (res=y.asStringArray()) != null) {
                for (int i = 0; i < res.length; i++) loadedP.put(res[i],dummy);
            }
            res = x.asStringArray();
            pkg = new Object[res.length][4];
            for (int i = 0; i < res.length; i++) {
                pkg[i][2]=new String(res[i]);
                try {
                    pkg[i][3]=JGR.R.eval("packageDescription(\""+res[i]+"\",fields=\"Title\")").asString();
                } catch (Exception e) { pkg[i][1] = "";}
                pkg[i][0]= loadedP.containsKey(res[i])?(new Boolean(true)):(new Boolean(false));
                pkg[i][1]= new Boolean(false);
                for (int d = 0; d < JGRPackageManager.defaultPackages.length; d++) {
                	
                	if (res[i].equals(JGRPackageManager.defaultPackages[d]) || JGRPackageManager.neededPackages.containsKey(res[i])) pkg[i][1] = new Boolean(true);
                }
            }
        }
        return pkg;
    }

    /* create r-dataframe as java-object*/
    public static dataframe createDataFrame(String sx) {
        dataframe d = new dataframe(sx);
        REXP y = JGR.R.eval("dim("+sx+")");
        if (y!=null && y.asIntArray()!=null) {
            d.setDim(y.asIntArray()[0],y.asIntArray()[1]);
        }
        REXP z = JGR.R.eval("names("+sx+")");
        String[] res;
        if (y != null && (res = z.asStringArray()) != null) {
            for (int i = 0; i < res.length; i++) {
                String sz = res[i];
                if (isMode(sx+"$"+sz,"numeric")) {
                    if (isClass(sx+"$"+sz,"factor")) {
                        factor f = createFactor(sx,sz);
                        f.setParent(d);
                        d.add(f);
                    }
                    else d.add(new numeric(sz,d));
                }
                else d.add(new unknown(sz,d));
            }
        }
        return d;
    }

    /* create r-factor as java-object*/
    public static factor createFactor(String sx, String sz) {
        factor f = new factor(sz);
        REXP x;
        if (sx != null) x = JGR.R.eval("length(levels("+sx+"$"+sz+"))");
        else  x = JGR.R.eval("length(levels("+sz+"))");
        if (x!=null && x.asIntArray() != null) f.setLevels(x.asIntArray()[0]);
        return f;
    }

    /*create r-list as java object*/
    public static list createList(String sx) {
        list l = new list(sx,null);
        REXP y = JGR.R.eval("length("+sx+")");
        if (y!=null && y.asIntArray()!=null) l.setLength(y.asIntArray()[0]);
        REXP z = JGR.R.eval("names("+sx+")");
        String[] res;
        if (y != null && (res = z.asStringArray()) != null) {
            for (int i = 0; i < res.length; i++) {
                String sz = res[i];
                REXP v = JGR.R.eval("class("+sx+"$\""+sz+"\")");
                if (v != null && v.asStringArray() != null) l.vars.add(new other(sz,v.asStringArray()[0],l));
                else l.vars.add(new other(sz,"",l));
            }
        }
        return l;
    }

    /* create r-matrix as java-object*/
    public static matrix createMatrix(String sx) {
        matrix m = new matrix(sx,null);
        REXP y = JGR.R.eval("dim("+sx+")");
        if (y!=null && y.asIntArray()!=null) {
            m.setDim(y.asIntArray()[0],y.asIntArray()[1]);
        }
        return m;
    }

    /* create other r-obj as java-obj*/
    public static RObject createOther(String sx) {
    	if (sx==null || sx.trim().length() == 0) return null;
        REXP y = JGR.R.eval("suppressWarnings(try(class("+sx+"),silent=TRUE))");
        String[] res;
        if (y!=null && (res = y.asStringArray())!=null) {
        	if (res[0].equals("factor")) return createFactor(null,sx);
            else return new other(sx,res[0],null);
        }
        return null;
    }

    /* create r-table as java-object*/
    public static table createTable(String sx) {
        table t = new table(sx);
        REXP y = JGR.R.eval("names(dimnames("+sx+"))");
        String[] res1;
        if (y != null && (res1 = y.asStringArray()) != null) {
            for (int i = 0; i < res1.length; i++) {
                String sy = res1[i];
                tableVar tv = new tableVar(sy,t);
                REXP v = JGR.R.eval("length(dimnames("+sx+")$"+sy+")");
                if (v!=null && v.asIntArray() != null) {
                    tv.setLevels(v.asIntArray()[0]);
                    t.add(tv);
                }
            }
        }
        return t;
    }

    /* create a r-model as java-object */
    public static model createModel(String sx, int type) {
        model m = new model(sx,type);
        REXP y = JGR.R.eval("summary("+sx+")$r.squared");
        double[] res;
        if (y != null && (res = y.asDoubleArray()) != null) m.setRsquared(res[0]);
        y = JGR.R.eval("summary("+sx+")$aic");
        if (y != null && (res = y.asDoubleArray()) != null) m.setAic(res[0]);
        y = JGR.R.eval("summary("+sx+")$deviance");
        if (y != null && (res = y.asDoubleArray()) != null) m.setDeviance(res[0]);
        int[] res1;
        y = JGR.R.eval("summary("+sx+")$df");
        if (y != null && (res1 = y.asIntArray()) != null) m.setDf(res1[0]);
        String[] res2;
        y = JGR.R.eval("summary("+sx+")$family$family");
        if (y != null && (res2 = y.asStringArray()) != null) m.setFamily(res2[0]);
        y = JGR.R.eval("as.character(("+sx+"$call))"); //as.character((cm$call))
        if (y != null && (res2 = y.asStringArray()) != null) {
            m.setCall(res2[1]+(res2.length==3?(", data = "+res2[2]):""));
            if (res2.length==3) m.setData(res2[2]);
        }
        return m;
    }

    /* get short usage of function*/
    public static String getFunHelp(String s) {
        System.out.println("s "+s); 
        if (s==null) return null;
        String tip = null;
        String res[] = null;
        REXP x;
        try { x = JGR.R.eval("try(deparse(args("+s+")),silent=T)"); } catch (Exception e) { return null;}
        if (x!=null && (res = x.asStringArray()) != null) {
            tip = "<html><pre>"; 
            int l = -1;
            for (int i = 0; i < (l=res.length); i++) {
                System.out.println(res[i]);
                if ((l-2)==i && !res[i].trim().equals("NULL")) tip += res[i].replaceFirst("function",s);
                else if (!res[i].trim().equals("NULL")) tip += res[i].replaceFirst("function",s)+ "<br>";
            }
            tip += "</pre></html>";
        }
        else return null;
        return (tip.indexOf("Error")>0)?null:tip;
    }


    public static String getSummary(RObject o) {
        if (isClass(o.getName(),"function")) return getFunHelp(o.getName());
        String tip = null;
        String res[] = null;
        REXP x;
        try { x = JGR.R.eval("suppressWarnings(try(capture.output(summary("+(o.getParent()==null?o.getName():((o.getParent()).getName()+"$"+o.getName()))+"))))"); } catch (Exception e) { return null;}
        if (x!=null && (res = x.asStringArray()) != null) {
            tip = "<html><pre>"; //<font size="+Preferences.FontSize/2+">"
            int l = -1;
            for (int i = 0; i < (l = res.length); i++) {
                if (i==10 && l > i) { tip += "..."; break; }
                else if ((--l)==i && !res[i].trim().equals("NULL")) tip += res[i];
                else if (!res[i].trim().equals("NULL")) tip += res[i]+"<br>";
            }
            //if (res.length > 0) tip = tip.substring(0,tip.length()-4); //cut last <br>
            tip += "</pre></html>";
        }
        else return null;
        return tip.startsWith("<html><pre>Error")?null:tip;
    }


    /* get levels for a factor */
    public static String getFactorLevels(factor f) {
        String levels = null;
        String res[];
        REXP x = JGR.R.eval("levels("+(f.getParent()==null?f.getName():((f.getParent()).getName()+"$"+f.getName()))+")");
        if (x != null && (res = x.asStringArray()) != null) {
            levels = "<html>";
            int l = -1;
            for (int i = 0; i < (l=res.length); i++) {
                if (i==10 && l > i) { levels += "..."; break; }
                else if (i==--l) levels+= res[i];
                else levels += res[i]+"<br>";
            }
            levels += "<html>";
        }
        return levels;
    }

    public static SVarSet getVarSet(dataframe d) {
        if (d == null) return null;
    	SVarSet vset = new SVarSet();
        vset.setName(d.getName());

        for (int i = 0; i < d.vars.size(); i++) {
            vset.add(getVar(d.getName(),((RObject) d.vars.elementAt(i)).getName()));
        }

        return vset;
    }

    public static SVarSet getVarSet(matrix m) {
        SVarSet vset = new SVarSet();
        String name = m.getName();
        JGR.R.eval("jgr_temp"+name+" <- as.data.frame("+name+")");
        dataframe d = createDataFrame("jgr_temp"+name);
        vset.setName(d.getName());

        for (int i = 0; i < d.vars.size(); i++) {
            vset.add(getVar(d.getName(),((RObject) d.vars.elementAt(i)).getName()));
        }
        JGR.R.eval("rm("+d.getName()+")");
        return vset;
    }


    public static SVar getVar(String p, String c) {
        REXP x = JGR.R.eval((p==null)?"":(p+"$")+c);
        if (x==null) return null;
        int[] res = x.asIntArray();
        if (res != null && isClass((p==null)?"":(p+"$")+c,"factor")) {

            for (int i = 0; i < res.length; i++) System.out.print(res[i]+" ");
            REXP y = JGR.R.eval("levels("+((p==null)?"":(p+"$")+c)+")");
            String[] s;
            if (y != null && (s = y.asStringArray()) != null) {
                //for (int i = 0; i < s.length; i++) System.out.print(s[i]);
                return new SVarFact(c,res,s);            }
        }
        else if (res != null) {
            SVar v = new SVarInt(c,res);
            for (int i = 0; i < res.length; i++) v.add(res[i]);
            return v;
        }
        else {
            double[]  res1 = x.asDoubleArray();
            if (res1 != null) {
                return new SVarDouble(c,res1);
            }
        }
        return null;
    }

    public static boolean putToR(SVarSet vs) {
        try {
            long contlist[] = new long[vs.count()];
            String[] names = new String[vs.count()];
            for (int i = 0; i< vs.count(); i++) {
                names[i] = vs.at(i).getName();
                if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
                    long v = JGR.R.rniPutDoubleArray(((SVarDouble) vs.at(i)).cont);
                    contlist[i] = v;
                                   }
                else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
                    long v = JGR.R.rniPutIntArray(((SVarInt) vs.at(i)).cont);
                    contlist[i] = v;
                                   }
                else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarFact")) {
                    long v = JGR.R.rniPutIntArray(((SVarFact) vs.at(i)).cont);
                    long c = JGR.R.rniPutString("factor");
                    JGR.R.rniSetAttr(v,"class",c);
                    long levels = JGR.R.rniPutStringArray(((SVarFact) vs.at(i)).cats);

                    JGR.R.rniSetAttr(v,"levels",levels);
                    contlist[i] = v;
                }
            }

            long xp1 = JGR.R.rniPutVector(contlist);
            long xp2 = JGR.R.rniPutStringArray(names);
            JGR.R.rniSetAttr(xp1,"names",xp2);

            String[] rownames = new String[vs.length()];
            for (int i = 0; i < rownames.length; i++) rownames[i] = i+"";
            long xp3 = JGR.R.rniPutStringArray(rownames);
            JGR.R.rniSetAttr(xp1,"row.names",xp3);

            long c = JGR.R.rniPutString("data.frame");
            JGR.R.rniSetAttr(xp1,"class",c);

            JGR.R.rniAssign(vs.getName(),xp1,0);
            if (vs.getName().startsWith("jgr_temp")) {
                String name = vs.getName();
                JGR.R.eval(name.replaceFirst("jgr_temp","")+" <- as.matrix("+vs.getName()+")");
                JGR.R.eval("rm("+name+")");
            }
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isClass(String v, String t) {
        REXP z = JGR.R.eval("suppressWarnings(try(class("+v+"),silent=TRUE))");
        if (z != null && z.asString() != null) return z.asString().equals(t);
        return false;
    }

    public static boolean isMode(String v, String t) {
        REXP z = JGR.R.eval("suppressWarnings(try(mode("+v+"),silent=TRUE))");
        if (z != null && z.asString() != null) return z.asString().equals(t);
        return false;
    }



    public static String commonWithPrefix(String str1, String str2) {
        int min = Math.min(str1.length(),str2.length());
        String result = "";
        String s = "";
        for (int i = 0; i < min; i++) {
            if ((s=str1.substring(i,i+1)).equals(str2.substring(i,i+1))) result += s;
            else break;
        }
        return result;
    }
}