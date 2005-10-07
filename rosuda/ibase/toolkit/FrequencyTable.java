package org.rosuda.ibase.toolkit;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.rosuda.ibase.SVar;

public class FrequencyTable {
    
    private int vsize;
    private SVar[] vars;
    private CombinationEntry[] ceTable;
    private double[] table;
    private double[] exp;
    
    private double p;
    
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
        
        // init frequency table
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
    }
    
    private class CombinationEntry {
        
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
    
    private String[][] lnames;
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
    
    public int[] getMatchingCases(int[] com, int maxLevel){
        int[] factors = new int[vsize];
        factors[vsize-1]=1;
        for(int i=vsize-2; i>=0; i--){
            factors[i] = factors[i+1]*vars[i+1].getNumCats();
        }
        
        int n=0;
        for(int i=0; i<maxLevel; i++){
            n += factors[i]*com[i];
        }
        Vector cases = new Vector();
        int arraySize=0;
        for(int i=0; i<factors[maxLevel-1]; i++){
            int[] c = ceTable[n+i].getCases();
            cases.add(c);
            arraySize += c.length;
        }
        int[] ret = new int[arraySize];
        int pos=0;
        for(Enumeration en = cases.elements(); en.hasMoreElements();){
            int[] c = (int[])en.nextElement();
            System.arraycopy(c, 0, ret, pos, c.length);
            pos += c.length;
        }
        return ret;
    }
    
    public void permute(int[] perm) {
        
        int[]   plevels = new int[vsize];
        int[][] index;
        // permuted pendants
        double[]   p_table  = new double[table.length];
        CombinationEntry[]   p_cetable  = new CombinationEntry[table.length];
        double[]   p_exp    = new double[table.length];
        String[][] p_lnames = new String[vsize][];
        SVar[] p_vars = new SVar[vsize];
        for (int i=0; i<vsize; i++)
            p_lnames[i] = new String[lnames[perm[i]].length];
        int[]      p_levels = new int[vsize];
        
        plevels[vsize-1] = 0;
        plevels[vsize-2] = levels[vsize-1];		// calculate the number of cells covered by a
        // category in level vsize
        for (int i=vsize-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        index = new int[table.length][vsize];
        
        int decompose;
        
        for (int i=0; i<table.length; i++) {
            decompose = i;
            for (int j=0; j<vsize-1; j++) {
                index[i][j] = decompose / plevels[j];
                decompose -= index[i][j] *  plevels[j];
            }
            index[i][vsize-1] = decompose;
        }
        
        for (int i=0; i<vsize; i++) {                  // permute the names: this is easy
            p_vars[i] = vars[perm[i]];
            p_levels[i] = levels[perm[i]];
        }
        
        for (int i=0; i<vsize; i++) {                  // and the level names
            for (int j=0; j<(p_lnames[i].length); j++) {
                p_lnames[i][j] = lnames[perm[i]][j];
            }
        }
        
        levels = p_levels;
        this.lnames = p_lnames;
        
        plevels[vsize-2] = levels[vsize-1];		 // calculate the number of cells covered by a
        // category in level k
        for (int i=vsize-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        for (int i=0; i<table.length; i++) {
            decompose = 0;
            for (int j=0; j<vsize-1; j++) {
                decompose += index[i][perm[j]] * plevels[j];
            }
            decompose += index[i][perm[vsize-1]];
            p_table[decompose]  = table[i];
            p_cetable[decompose] = ceTable[i];
            p_exp[decompose]    = exp[i];
        }
        
        table = p_table;
        ceTable = p_cetable;
        exp = p_exp;
        vars = p_vars;
        
    } // end perm
    
    public SVar[] getVars() {
        return vars;
    }
    
    public double getP() {
        return p;
    }
    
}
