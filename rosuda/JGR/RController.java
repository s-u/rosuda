package org.rosuda.JGR;

/**
*  RController Class
 * 	nothing else should have access to R only this RController
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.io.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.util.Global;
import org.rosuda.JRI.*;
import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.toolkit.Editor;

public class RController {

    /**
    * dummy object
     */
    public static Object dummy = new Object();

    /**
    * get R_HOME
     * @return R_HOME path
     */
    public static String getRHome() {
        REXP x = JGR.R.eval("R.home()");
        if (x != null && x.asStringArray()!=null) return x.asStringArray()[0];
        return "";
    }

    /**
        * get R_LIBS
     * @return R_LIBS paths
     */
    public static String[] getRLibs() {
        REXP x = JGR.R.eval(".libPaths()");
        if (x != null && x.asStringArray()!=null) return x.asStringArray();
        return null;
    }

    /**
        * get R_DEFAULT_PACKAGE
     * @return default packages
     */
    public static String[] getDefaultPackages() {
        REXP x = JGR.R.eval("getOption(\"defaultPackages\")");
        if (x != null && x.asStringArray()!=null) return x.asStringArray();
        return new String[] {};
    }

    /**
        * show all posibilities to complete your given string
     * @param partOfCmd string which you want to complete
     * @return possible completions
     */
    public static String[] completeCommand(String partOfCmd) {
		if (!JGR.STARTED) return null;
        int s = partOfCmd.length()-1;
        if (partOfCmd.trim().length() == 0) return null;
        REXP cmds = JGR.R.eval(".completeCommand(\""+partOfCmd+"\")");
        String[] c = null;
        if (cmds != null && (c=cmds.asStringArray()) != null) return c;
        return c;
    }

    /**
        * completetion of a file (doesn't supports multiple fils yet)
     * @param part part of file to complete
     * @return file
     */
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

    /**
        * get all keywords for syntaxhighlighting
     * @return keywords
     */
    public static String[] getKeyWords() {
        REXP x = JGR.R.eval(".refreshKeyWords()");
        String[] r = null;
        if (x != null && (r=x.asStringArray()) != null) return r;
        return r;
    }

    /**
        * object names used for syntaxhighlighting
     * @return objects
     */
    public static String[] getObjects() {
        REXP x = JGR.R.eval(".refreshObjects()");
        String[] r = null;
        if (x != null && (r=x.asStringArray()) != null) return r;
        return r;
    }


    /**
        * select all Objects and put them in JGR.MODELS, JGR.DATA, JGR.OTHER and JGR.FUNCTIONS
     * currently only these things which are provided by ls(pos=1)
     *
     */
    public static void refreshObjects() {
        JGR.DATA.clear();
        JGR.OTHERS.clear();
        JGR.MODELS.clear();
        JGR.FUNCTIONS.clear();
        String models[];
        REXP x = JGR.R.eval(".getModels()");
        if (x != null && (models = x.asStringArray()) != null) {
            for (int i = 0; i < models.length; i++)
                JGR.MODELS.add(createRModel(models[i],models[++i]));
        }
        x = JGR.R.eval(".getDataObjects()");
        String[] data;
        if (x != null && (data = x.asStringArray()) != null) {
            int a = 1;
            for (int i = 0; i < data.length; i++) {
                boolean b = (data[i].equals("null")||data[i].trim().length()==0);
                String name = b?a+"":data[i];
                JGR.DATA.add(createRObject(name,data[++i],null,(!b)));
                a++;
            }
        }
        x = JGR.R.eval(".getOtherObjects()");
        String[] other;
        if (x != null && (other = x.asStringArray()) != null) {
            int a = 1;
            for (int i = 0; i < other.length; i++) {
                boolean b = (other[i].equals("null")||other[i].trim().length()==0);
                String name = b?a+"":other[i];
                JGR.OTHERS.add(createRObject(name,other[++i],null,(!b)));
                a++;
            }
        }
        x = JGR.R.eval(".getFunctionsInWS()");
        String[] functions;
        if (x != null && (functions = x.asStringArray()) != null) {
            int a = 1;
            for (int i = 0; i < functions.length; i++) {
                JGR.FUNCTIONS.add(createRObject(functions[i],"function",null,true));
                a++;
            }
        }
    }

    /**
        * get information about all packages (loaded, undloaded, defaults ...)
     * @return package information
     */
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

    /**
        * get the content of an RObject (list, data.frame, table, matrix)
     * @param o RObject
     * @param c all found objects are collected in c (currently disabled)
     * @return Vector of RObjects
     */
    public static Vector createContent(RObject o, Collection c) {
        Vector cont = new Vector();
        String p = "";
        if (o.getParent() != null && o.getParent().getType().equals("table"))
            p = ","+o.getParent().getRName();
        REXP x = JGR.R.eval("suppressWarnings(try(.getContent("+(o.getRName())+p+"),silent=TRUE))");
        String[] res;
        if (x != null && (res = x.asStringArray()) != null && !res[0].startsWith("Error")) {
            int a = 1;
            for (int i = 0; i < res.length; i++) {
                boolean b = (res[i].equals("null")||res[i].trim().length()==0);
                String name = b?a+"":res[i];
                RObject ro = createRObject(name,res[++i],o,(!b));
                //if (c != null) c.add(ro);
                if (ro != null) cont.add(ro);
                a++;
            }
        }
        return cont;
    }

    /**
        * creates an RObject (java-side) out of R
     * @param sx		name
     * @param type		type
     * @param parent	parent RObject
     * @param b			names(..) provides real names or not
     * @return 			new RObject
     */
    public static RObject createRObject(String sx, String type, RObject parent, boolean b)
    {
        RObject ro = new RObject(sx,type, parent, b);
        REXP y;
        if (type.equals("data.frame")) {
            y = JGR.R.eval("dim("+(ro.getRName())+")");
            if (y!=null && y.asIntArray()!=null) {
                ro.setInfo("dim("+y.asIntArray()[0]+":"+y.asIntArray()[1]+")");
            }
        }
        else if (type.equals("matrix")) {
            y = JGR.R.eval("dim("+(ro.getRName())+")");
            if (y!=null && y.asIntArray()!=null) {
                ro.setInfo("dim("+y.asIntArray()[0]+":"+y.asIntArray()[1]+")");
            }
        }
        else if (type.equals("factor")) {
            y = JGR.R.eval("length(levels("+(ro.getRName())+"))");
            if (y!=null && y.asIntArray() != null) ro.setInfo("levels: "+y.asIntArray()[0]);
        }
        else if (type.equals("list")) {
            y = JGR.R.eval("length("+(ro.getRName())+")");
            if (y!=null && y.asIntArray() != null) ro.setInfo("length: "+y.asIntArray()[0]);
        }
        else if (type.equals("table")) {
            y = JGR.R.eval("length(dim("+(ro.getRName())+"))");
            if (y!=null && y.asIntArray() != null) ro.setInfo("dim: "+y.asIntArray()[0]);
        }
        else if (type.equals("function")) {
            ro.setInfo("arguments: "+getFunHelp(ro.getRName()).replaceFirst(ro.getRName(),"").replaceAll("<br>",""));
        }
        else if (parent != null && parent.getType().equals("table")) {
            y = JGR.R.eval("length(dimnames("+parent.getRName()+")[[\""+ro.getName()+"\"]])");
            if (y!=null && y.asIntArray() != null) ro.setInfo("cats: "+y.asIntArray()[0]);
        }
        return ro;
    }

    /**
        * create RModel (java-side) out of R
     * @param sx	name
     * @param type	type (currently only lm and glm is supported
                         * @return	new RModel
                         */
    public static RModel createRModel(String sx, String type) {
        RModel m = new RModel(sx,type);
        REXP y = JGR.R.eval("summary("+sx+")[[\"r.squared\"]]");
        double[] res;
        if (y != null && (res = y.asDoubleArray()) != null) m.setRsquared(res[0]);
        y = JGR.R.eval("summary("+sx+")[[\"aic\"]]");
        if (y != null && (res = y.asDoubleArray()) != null) m.setAic(res[0]);
        y = JGR.R.eval("summary("+sx+")[[\"deviance\"]]");
        if (y != null && (res = y.asDoubleArray()) != null) m.setDeviance(res[0]);
        int[] res1;
        y = JGR.R.eval("summary("+sx+")[[\"df\"]]");
        if (y != null && (res1 = y.asIntArray()) != null) m.setDf(res1[0]);
        String[] res2;
        y = JGR.R.eval("summary("+sx+")[[\"family\"]][[\"family\"]]");
        if (y != null && (res2 = y.asStringArray()) != null) m.setFamily(res2[0]);
        y = JGR.R.eval("suppressWarnings(try(capture.output("+sx+"[[\"call\"]])))"); //as.character((cm$call))
        if (y != null && (res2 = y.asStringArray()) != null) {
            String call = "";
            for (int i = 0; i < res2.length; i++) {
                int z = -1;
                if ((z = res2[0].indexOf("data")) > 0) m.setData(res2[i].substring(z+6).replace(')',' ').trim());
                call += res2[i];
            }
            m.setCall(call);

        }
        return m;
    }
	
	public static String getFunHelpTip(String s) {
		String tip = getFunHelp(s);
		return tip!=null?"<html><pre>"+tip+"</pre></html>":null;
	}

    /**
        * show argumentes for function, tooltip
     * @param s function name
     * @return arguments
     */
    private static String getFunHelp(String s) {
        if (s==null) return null;
        String tip = null;
        String res[] = null;
        REXP x;
        try { x = JGR.R.eval("try(deparse(args("+s+")),silent=T)"); } catch (Exception e) { return null;}
        if (x!=null && (res = x.asStringArray()) != null) {
            tip = "";
            int l = -1;
            for (int i = 0; i < (l=res.length); i++) {
                if ((l-2)==i && !res[i].trim().equals("NULL")) tip += res[i].replaceFirst("function",s);
                else if (!res[i].trim().equals("NULL")) tip += res[i].replaceFirst("function",s)+ "<br>";
            }
            tip += "";
        }
        else return null;
        if (tip.trim().length() == 0) return null;
        return (tip.indexOf("Error")>=0)?null:tip;
    }

    /**
     * show summary of an RObject
     * @param o RObject
     * @return summary
     */
    public static String getSummary(RObject o) {
        if (o.getType().equals("function")) return "<html><pre>"+getFunHelp(o.getRName())+"</pre></html>";
        String tip = "";
        String res[] = null;
        REXP x;
        try { x = JGR.R.eval("suppressWarnings(try(capture.output(summary("+(o.getRName())+")),silent=TRUE))"); } catch (Exception e) { return null;}
        if (x!=null && (res = x.asStringArray()) != null && !res[0].startsWith("Error")) {
            //tip = "<html><pre>";
            int l = -1;
            for (int i = ((l = res.length) > 10?10:l)-1; i >= 0; i--) {
                if (i < l-1) tip = res[i] +"<br>"+ tip;
                else tip = res[i]+"       ";
            }
            tip = "<html><pre>"+tip+(l > 10?"...":"")+"</pre></html>";
        }
        else return null;
        return tip.startsWith("<html><pre>Error")?null:tip;
    }


    public static void newFunction(RObject o) {
        REXP x = JGR.R.eval("suppressWarnings(try(capture.output("+o.getRName()+"),silent=TRUE))");
        String[] res;
        if (x != null && (res = x.asStringArray()) != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < res.length; i++) {
                if (i==0) sb.append(o.getRName()+" <- "+res[i]+"\n");
                else sb.append(res[i]+"\n");
            }
            if (sb.length() > 0) new Editor().setText(sb);
        }
    }


    /** create a new dataset with the specified RObject.
        * @param o RObject which we want to have in a SVarSet
        * @return new dataset
        * */
    public static SVarSet newSet(RObject o) {
        SVarSet cvs=new SVarSet();
        cvs.setName(o.getRName());
        if (o.getType().equals("function")) {
            //thats not really the best way to do this but the easiest
            REXP x = JGR.R.eval("suppressWarnings(try(capture.output("+o.getRName()+"),silent=TRUE))");
            String[] res;
            if (x != null && (res = x.asStringArray()) != null) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < res.length; i++) {
                    if (i==0) sb.append(o.getRName()+" <- "+res[i]+"\n");
                    else sb.append(res[i]+"\n");
                }
                if (sb.length() > 0) new Editor().setText(sb);
            }
            return null;
        }
        else {
            Iterator i = createContent(o,null).iterator();
            if (!i.hasNext()) {
                cvs.add(createSVar(cvs,o));
            }
            while (i.hasNext()) {
                RObject o2 = (RObject) i.next();
                cvs.add(createSVar(cvs,o2));
            }
        }
        return cvs;
    }

    /**
     * create SVar out of an RObject
     * @param cvs	destination SVarSet
     * @param o		RObject
     * @return new SVar
     */
    private static SVar createSVar(SVarSet cvs, RObject o) {
        REXP x = JGR.R.eval("suppressWarnings(try("+o.getRName()+",silent=TRUE))");
        if (x !=null && x.asStringArray() != null && x.asStringArray().length > 0 && x.asStringArray()[0].startsWith("Error"))
            return null;
        SVar v = null;
        if (o.getType().equals("factor")) {
            REXP y = JGR.R.eval("suppressWarnings(try(levels("+o.getRName()+"),silent=TRUE))");
            if (y != null && x != null && y.asStringArray() != null && x.asIntArray() != null) {
                v = newVar(cvs,o.getName(),x.asIntArray(),y.asStringArray());
            }
        }
        else if (o.getType().equals("character")) {
            if (x != null && x.asStringArray() != null) {
                v = newVar(cvs,o.getName(),x.asStringArray());
            }
        }
        else if (x != null && x.asIntArray() != null) {
            v = newVar(cvs,o.getName(),x.asIntArray());
        }
        else if (x!= null && x.asDoubleArray() != null) {
            v = newVar(cvs,o.getName(),x.asDoubleArray());
        }
        return v;
    }

    /** construct a new numerical variable from supplied array of doubles. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of doubles
        @return SVar
        */
    public static SVar newVar(SVarSet cvs, String name, double[] d) {
        if (d==null) return null;
        if (Global.DEBUG>0)
            System.out.println("newVar: double["+d.length+"]");
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            double[] n = new double[cvs.at(0).size()];
            for (int i = 0; i < d.length && i < n.length; i++)
                n[i] = d[i];
            d = n;
        }
        SVar v=new SVarDouble(name,d);
        return v;
    }

    /** construct a new numerical variable from supplied array of integers. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of integers
        @return SVar
        */
    public static SVar newVar(SVarSet cvs, String name, int[] d) {
        if (d==null) return null;
        if (Global.DEBUG>0)
            System.out.println("newVar: int["+d.length+"]");
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            int[] n = new int[cvs.at(0).size()];
            for (int i = 0; i < d.length && i < n.length; i++)
                n[i] = d[i];
            d = n;
        }
        SVar v=new SVarInt(name,d);
        return v;
    };

    /** construct a new categorical variable from supplied array of strings. Unlike datasets variables cannot have
        the same name within a dataset.
        @param name variable name
        @param d array of strings
        @return SVar
        */
    public static SVar newVar(SVarSet cvs, String name, String[] d) {
        if (d==null) return null;
        if (Global.DEBUG>0)
            System.out.println("newVar: String[]");
        if (cvs.count()>0 && cvs.at(0).size()!=d.length) {
            String[] n = new String[cvs.at(0).size()];
            for (int i = 0; i < d.length && i < n.length; i++)
                n[i] = d[i];
            d = n;
        }
        SVar v=new SVarObj(name);
        int i=0; while(i<d.length) v.add(d[i++]);
        return v;
    }


    /** construct a new factor variable from supplied array of integers (cases) and strings (levels). Unlike datasets variables cannot have the same name within a dataset.
        @param name variable name
        @param ix array of level IDs. IDs out of range (<1 or >length(d)) are treated as missing values
        @param d levels (d[0]=ID 1, d[1]=ID 2, ...)
        @return SVar
        */
    public static SVar newVar(SVarSet cvs, String name, int[] ix, String[] d) {
        if (ix==null) return null;
        if (d==null) return newVar(cvs, name,ix);
        if (Global.DEBUG>0)
            System.out.println("newVar: int["+ix.length+"] + levels["+d.length+"]");
        if (cvs.count()>0 && cvs.at(0).size()!=ix.length) {
            int[] n = new int[cvs.at(0).size()];
            for (int i = 0; i < d.length && i < n.length; i++)
                n[i] = ix[i];
            ix = n;
        }
        int j=0;
        while (j<ix.length) { ix[j++]--; }; // reduce index by 1 since R is 1-based
        SVar v=new SVarFact(name, ix, d);
        return v;
    }

    /**
     * export an SVarSet to R
     * @param vs dataset
     * @return true if successful, false if not
     */
    public static boolean export(SVarSet vs,String type) {
        /*REXP x = JGR.R.eval("suppressWarnings(try(class("+vs.getName()+"),silent=TRUE))");
        String type = null;
        if (x != null && x.asString() != null && !x.asString().startsWith("Error"))
            type = x.asString();
        boolean success = false;
        System.out.println(type);*/
        boolean success = false;
        if (type == null || type.equals("data.frame"))
            success = exportDataFrame(vs);
        else if (type != null && type.equals("matrix"))
            success = exportMatrix(vs);
        else if (type != null && type.equals("list"))
            success = exportList(vs);
        else if (type != null && type.equals("numeric"))
            success = exportNumeric(vs);
        else if (type != null && type.equals("integer"))
            success = exportInteger(vs);
        else if (type != null && type.equals("character"))
            success = exportCharacter(vs);
        else if (type != null && type.equals("factor"))
            success = exportFactor(vs);
        return success;
    }


    private static boolean setName(String name) {
        try {
            JGR.R.eval(name+"<- jgrtemp; rm(jgrtemp)");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * export r-numeric
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportNumeric(SVarSet vs) {
        try {
            if (vs.count() > 1) return false;
            long v = JGR.R.rniPutDoubleArray(((SVarDouble) vs.at(0)).cont);
            System.out.println(vs.getName());
            JGR.R.rniAssign("jgrtemp",v,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * export r-integer
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportInteger(SVarSet vs) {
        try {
            if (vs.count() > 1) return false;
            long v = JGR.R.rniPutIntArray(((SVarInt) vs.at(0)).cont);
            JGR.R.rniAssign("jgrtemp",v,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * export r-factor
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportFactor(SVarSet vs) {
        try {
            if (vs.count() > 1) return false;
            int[] ids = new int[((SVarFact) vs.at(0)).cont.length];
            for (int z = 0; z < ids.length; z++)
                ids[z] = ((SVarFact) vs.at(0)).cont[z]+1;
            long v = JGR.R.rniPutIntArray(ids);
            long c = JGR.R.rniPutString("factor");
            JGR.R.rniSetAttr(v,"class",c);
            long levels = JGR.R.rniPutStringArray(((SVarFact) vs.at(0)).cats);
            JGR.R.rniSetAttr(v,"levels",levels);
            JGR.R.rniAssign("jgrtemp",v,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    

    /**
     * export r-character
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportCharacter(SVarSet vs) {
        try {
            if (vs.count() > 1) return false;
            String[] cont = new String[((SVarObj) vs.at(0)).size()];
            for (int i = 0; i < ((SVarObj) vs.at(0)).size(); i++)
                cont[i] = ((SVarObj) vs.at(0)).at(i).toString();
            long v = JGR.R.rniPutStringArray(cont);
            JGR.R.rniAssign("jgrtemp",v,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * export r-data.frame
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportDataFrame(SVarSet vs) {
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
                    int[] ids = new int[((SVarFact) vs.at(i)).cont.length];
                    for (int z = 0; z < ids.length; z++)
                        ids[z] = ((SVarFact) vs.at(i)).cont[z]+1;
                    long v = JGR.R.rniPutIntArray(ids);
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
            for (int i = 1; i <= rownames.length; i++) rownames[i-1] = i+"";
            long xp3 = JGR.R.rniPutStringArray(rownames);
            JGR.R.rniSetAttr(xp1,"row.names",xp3);

            long c = JGR.R.rniPutString("data.frame");
            JGR.R.rniSetAttr(xp1,"class",c);

            JGR.R.rniAssign("jgrtemp",xp1,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * export r-matrix
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportMatrix(SVarSet vs) {
        try {
            String[] names = new String[vs.count()];
            Object mcont;
            int vlength = vs.at(0).size();
            boolean isInt = false;
            if (vs.at(0).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
                mcont = new double[vs.count()*vlength];
            }
            else if (vs.at(0).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
                mcont = new int[vs.count()*vlength];
                isInt = true;
            }
            else return false;

            for (int i = 0; i< vs.count(); i++) {
                names[i] = vs.at(i).getName();
                if (isInt) System.arraycopy(((SVarInt) vs.at(i)).cont,0,mcont,i*vlength,vlength);
                else System.arraycopy(((SVarDouble) vs.at(i)).cont,0,mcont,i*vlength,vlength); 
            }

            long xp1;
            if (isInt) xp1 = JGR.R.rniPutIntArray((int[]) mcont);
            else xp1 = JGR.R.rniPutDoubleArray((double[]) mcont);

            long[] dimnames = new long[2];

            long xp2 = JGR.R.rniPutStringArray(names);
           

            String[] rownames = new String[vs.length()];
            for (int i = 1; i <= rownames.length; i++) rownames[i-1] = i+"";
            long xp3 = JGR.R.rniPutStringArray(rownames);

            dimnames[0] = xp3;
            dimnames[1] = xp2;

            long xp4 = JGR.R.rniPutVector(dimnames);

            double[] dim = new double[] {vlength,vs.count()};
            long xp5 = JGR.R.rniPutDoubleArray(dim);
            JGR.R.rniSetAttr(xp1,"dim",xp5);

            JGR.R.rniSetAttr(xp1,"dimnames",xp4);

            long c = JGR.R.rniPutString("matrix");
            JGR.R.rniSetAttr(xp1,"class",c);

            JGR.R.rniAssign("jgrtemp",xp1,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * export r-list
     * @param vs dataset
     * @return true if successful, false if not
     */
    private static boolean exportList(SVarSet vs) {
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
                    int[] ids = new int[((SVarFact) vs.at(i)).cont.length];
                    for (int z = 0; z < ids.length; z++)
                        ids[z] = ((SVarFact) vs.at(i)).cont[z]+1;
                    long v = JGR.R.rniPutIntArray(ids);
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

            JGR.R.rniAssign("jgrtemp",xp1,0);
            return setName(vs.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * compare to string and retrun the common prefix
     * @param str1 String 1
     * @param str2 String 2
     * @return common prefix
     */
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
