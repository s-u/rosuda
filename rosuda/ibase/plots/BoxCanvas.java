package org.rosuda.ibase.plots;

import java.awt.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.plots.OrdStats;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Tools;

/** BoxCanvas - implementation of the boxplots
 * @version $Id$
 */
public class BoxCanvas extends ParallelAxesCanvas {
    
    /** create a boxplot canvas for a single boxplot
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent pc, final Frame f, final SVar var, final SMarker mark) {
        this(pc,f,new SVar[]{var},mark);
    }
    
    /** create a boxplot canvas for multiple boxplots
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variables
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar[] var, final SMarker mark) {
        super(ppc,f,var,mark);
        type=TYPE_BOX;
        updateMargins();
        
        String variables = v[0].getName();
        for(int i=1; i<v.length; i++) variables+=", " + v[i].getName();
        setTitle("Boxplot ("+ variables + ")");
        
        
        if(var.length==1){
            if (v[0]!=null && !v[0].isCat() && v[0].isNum())
                valid=true; // valid are only numerical vars non-cat'd
            else valid=false;
            if (valid) {
                OSdata=new OrdStats();
                final int dr[]=v[0].getRanked();
                OSdata.update(v[0],dr);
                //updateObjects();
            }
        } else{
            oss = new OrdStats[v.length];
            for(int i=0; i<v.length; i++){
                if (v[i]!=null && !v[i].isCat() && v[i].isNum())
                    valid=true; // valid are only numerical vars non-cat'd
                if (valid) {
                    oss[i]=new OrdStats();
                    final int dr[]=v[i].getRanked();
                    oss[i].update(v[i],dr);
                }
            }
        }
        dontPaint=false;
    };
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar var, final SVar cvar, final SMarker mark) { // multiple box vs cat
        super(ppc,f,var,cvar,mark);
        type=TYPE_BOX;
        vsCat=true;
        updateMargins();
        
        setTitle("Boxplot ("+v[0].getName()+" grouped by "+cv.getName()+")");
        
        if (var!=null && !var.isCat() && var.isNum() && cvar.isCat())
            valid=true; // valid are only numerical vars non-cat'd, cvar is cat
        if (valid) { // split into ranked chunks by cat.
            cs=cv.getNumCats();
            cats=cv.getCategories();
            final int[] r=v[0].getRanked();
            oss=new OrdStats[cs*2+2];
            rk=new int[cs*2+2][];
            rs=new int[cs*2+2];
            int i=0;
            while (i<cs) {
                rs[i]=0;
                final int j=cv.getSizeCatAt(i);
                rk[i]=new int[j];
                rk[cs+1+i]=new int[j];
                oss[i]=new OrdStats();
                oss[cs+1+i]=new OrdStats();
                i++;
            }
            i=0;
            while(i<r.length) {
                int x=cv.getCatIndex(cv.at(r[i]));
                if (x<0) x=cs;
                rk[x][rs[x]]=r[i];
                rs[x]++;
                i++;
            }
            i=0;
            while(i<cs) {
                oss[i].update(v[0],rk[i],rs[i]);
                i++;
            }
            boolean[] validOss = new boolean[cs];
            int invalid=0;
            for(i=0; i<cs; i++){
                if(oss[i].lastR==null){
                    validOss[i]=false;
                    invalid++;
                } else validOss[i]=true;
            }
            if(invalid>0){
                OrdStats[] newOss = new OrdStats[2*(cs-invalid)+2];
                int j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[j++]=oss[i];
                }
                newOss[cs]=oss[cs];
                j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[cs-invalid+1+j++]=oss[cs+1+i];
                }
                oss=newOss;
                cs-=invalid;
            }
            updateObjects();
        }
        objectClipping=true;
        dontPaint=false;
    };
    
    public SVar getData(final int id) {
        if(cv==null) return super.getData(id);
        else return (id==0)?v[0]:((id==1)?cv:null);
    }
}
