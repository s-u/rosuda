package org.rosuda.ibase.toolkit;

import java.util.*;
import org.rosuda.ibase.*;

public class FrequencyTable {
    
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    
    private int vsize;
    private SVar[] vars;
    private CombinationEntry[] ceTable;
    private double[] table;
    private double[] exp;
    
    public FrequencyTable(SVar[] vvs) {
        this.vars = vvs;
        this.vsize = vvs.length;
        
        int tableLen = 1;
        for(int i=0; i< vsize; i++){
            tableLen *= vars[i].getNumCats();
        }
        ceTable = new CombinationEntry[tableLen];
        table = new double[tableLen];
        exp = new double[tableLen];
        
        init();
    }
    
    // init frequency table
    public void init() {
        
        for(int i=0; i<ceTable.length; i++) ceTable[i] = new CombinationEntry();
        int[] vc = new int[vsize];
        int pos=0;
        while(vc[0]<vars[0].getNumCats()){
            for(vc[vsize-1]=0; vc[vsize-1]<vars[vsize-1].getNumCats(); vc[vsize-1]++){
                for(int v=0; v<vsize; v++){
                    ceTable[pos+vc[vsize-1]].addCat(vars[v].getName(), vars[v].getCategories()[vc[v]]);
                }
            }
            for(int i=vsize-2; i>=0; i--){
                vc[i+1]=0;
                if((++vc[i])<vars[i].getNumCats()) break;
            }
            pos += vars[vsize-1].getNumCats();
        }
        
        for (int i=0; i<ceTable.length; i++) {
            CombinationEntry ce = ceTable[i];
            for (int cs = 0; cs < vars[0].size(); cs++) {
                if (ce.CombinationEqualsCase(cs))
                    ce.cases.add(new Integer(cs));
            }
            table[i] = (double)ce.cases.size();
        }
        
        // CHANGE!!!
        for (int i=0; i<exp.length; i++){
            exp[i] = table[i]/2;
        }
    }
    
    class CombinationEntry {
        
        Hashtable ccs = new Hashtable();
        
        Vector cases = new Vector();
        
        public CombinationEntry() {
            
        }
        
        public int[] getCases() {
            int[] cs = new int[cases.size()];
            for (int i = 0; i < cs.length && i < cases.size(); i++)
                cs[i] = ((Integer) cases.elementAt(i)).intValue();
            return cs;
        }
        
        public int getCombSize() {
            return cases.size();
        }
        
        public Hashtable getCats() {
            return ccs;
        }
        
        public String getCombInfo() {
            String comb = "";
            Enumeration e = ccs.keys();
            while (e.hasMoreElements()) {
                Object o = e.nextElement();
                comb += o+": "+ccs.get(o);
                if (e.hasMoreElements()) comb +="\n";
            }
            return comb;
        }
        
        public CombinationEntry(CombinationEntry e) {
            this.ccs.putAll(e.ccs);
        }
        
        protected void addCat(Object n, Object c){
            ccs.put(n,c);
        }
        
        protected boolean CombinationEqualsCase(int c) {
            for (int i = 0; i < vsize; i++) {
                String str = (String)ccs.get(vars[i].getName());
                if(str.compareTo(vars[i].at(c).toString())!=0)
                    return false;
            }
            return true;
        }
        
        
        public String toString() {
            String comb = "";
            Enumeration e = ccs.keys();
            while (e.hasMoreElements()) {
                Object o = e.nextElement();
                comb += o+": "+ccs.get(o);
                if (e.hasMoreElements()) comb +=", ";
            }
            return comb+" : "+cases.size();
        }
    }
    
    private int[] levels;
    public int[] getLevels(){
        if (levels==null){
            levels = new int[vars.length];
            for(int i=0; i<vars.length; i++){
                levels[i] = vars[i].getNumCats();
            }
        }
        return levels;
    }
    
    String[][] lnames;
    public String[][] getLnames(){
        if(lnames==null){
            lnames = new String[vars.length][];
            for(int i=0; i<vars.length; i++){
                lnames[i] = new String[getLevels()[i]];
                for(int j=0; j<getLevels()[i]; j++){
                    lnames[i][j] = (String)vars[i].getCatAt(j);
                }
            }
        }
        return lnames;
    }
    
    public double[] getTable(){
        return table;
    }
    
    public double[] getExp(){
        return exp;
    }
    
    public int[] getMatchingCases(int[] com){
        int[] factors = new int[vsize];
        factors[vsize-1]=1;
        for(int i=vsize-2; i>=0; i--){
            factors[i] = factors[i+1]*vars[i+1].getNumCats();
        }
        int n=0;
        for(int i=0; i<com.length; i++){
            n += factors[i]*com[i];
        }
        
        return ceTable[n].getCases();
    }
}
