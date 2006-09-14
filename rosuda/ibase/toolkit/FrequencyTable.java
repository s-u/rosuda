package org.rosuda.ibase.toolkit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.rosuda.ibase.SVar;

public final class FrequencyTable {
    
    private int vsize;
    private SVar[] vars;
    private CombinationEntry[] ceTable;
    private double[] table;
    private double[] exp;
    
    private double p;
    
    public FrequencyTable(final SVar[] vvs) {
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
        final int[] vc = new int[vsize];
        int pos=0;
        while(vc[0]<vars[0].getNumCats()){
            for(vc[vsize-1]=0; vc[vsize-1]<vars[vsize-1].getNumCats(); vc[vsize-1]++){
                for(int v=0; v<vsize; v++){
                    ceTable[pos+vc[vsize-1]].addCat(vars[v].getName(), vars[v].getCategories()[vars[v].mainSeq().catAtPos(vc[v])]);
                }
            }
            for(int i=vsize-2; i>=0; i--){
                vc[i+1]=0;
                if((++vc[i])<vars[i].getNumCats()) break;
            }
            pos += vars[vsize-1].getNumCats();
        }
        
        for (int i=0; i<ceTable.length; i++) {
            ceTable[i].cases = new ArrayList((int)(vars[0].size()/Math.pow(vars[0].getNumCats(),vars.length)));
        }
        final int[] chunks = new int[vars.length];
        chunks[chunks.length-1]=1;
        for(int i=chunks.length-2; i>=0; i--){
            chunks[i] = chunks[i+1]*vars[i+1].getNumCats();
        }
        TreeMap[] tm = new TreeMap[vars.length];
        for(int i=0; i<tm.length; i++) tm[i]=new TreeMap();
        for (int cs=0; cs < vars[0].size(); cs++){
            final int[] numOfCat = new int[vars.length];
            for(int i=vars.length-1; i>=0; i--){
                final String str = vars[i].at(cs).toString();
                final Object num = tm[i].get(str);
                if(num==null){
                    numOfCat[i]=-1;
                    for(int j=0; j<vars[i].getNumCats(); j++){
                        if(vars[i].at(cs).toString().equals(vars[i].getCategories()[j].toString())){
                            numOfCat[i]=j;
                            break;
                        }
                    }
                    tm[i].put(str,new Integer(numOfCat[i]));
                } else{
                    numOfCat[i] = ((Integer)num).intValue();
                }
            }
            //for(int i=0; i<chunks.length; i++) System.out.println("chunks " + i + " " + chunks[i]);
            //for(int i=0; i<numOfCat.length; i++) System.out.println("nOc " + i + " " + numOfCat[i]);
            
            int ind=0;
            for(int j=0; j<vars.length; j++) ind += chunks[j]*numOfCat[j];
            //System.out.println("ind: " + ind + ", len: " + ceTable.length);
            final CombinationEntry ce = ceTable[ind];
            ce.cases.add(new Integer(cs));
        }
        for (int i=0; i<ceTable.length; i++) {
            table[i] = ceTable[i].cases.size();
        }

        // init expected table assuming independence
        int maxNumCats=0;
        for(int v=0; v<vsize; v++){
            if(vars[v].getNumCats() > maxNumCats) maxNumCats=vars[v].getNumCats();
        }
         
        final int[][] counts;
        counts = new int[vsize][maxNumCats];
         
        for(int v=0; v<vsize; v++)
            for(int i=0; i<vars[v].getNumCats(); i++)
                counts[v][i]=0;
         
        for(int v=0; v<vsize; v++)
            for(int i=0; i<vars[v].size(); i++)
                counts[v][vars[v].getCatIndex(i)]++;
         
        final double denom = Math.pow(vars[0].size(), -vsize);
        for(int i=0; i<exp.length; i++){
            exp[i]=1;
            int j=i;
            for(int v=vsize-1; v>=0; v--){
                exp[i]*=counts[v][j%vars[v].getNumCats()];
                j = j/vars[v].getNumCats();
            }
            exp[i] *= denom;
        }
    }
    
    private final class CombinationEntry {
        
        Hashtable ccs = new Hashtable();
        
        List cases = new ArrayList(0);
        
        public CombinationEntry() {
            
        }
        
        public int[] getCases() {
            final int[] cs = new int[cases.size()];
            for (int i = 0, csize = cases.size(); i < cs.length && i < csize; i++)
                cs[i] = ((Integer) cases.get(i)).intValue();
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
            final Enumeration e = ccs.keys();
            while (e.hasMoreElements()) {
                final Object o = e.nextElement();
                comb += o+": "+ccs.get(o);
                if (e.hasMoreElements()) comb +="\n";
            }
            return comb;
        }
        
        public CombinationEntry(final CombinationEntry e) {
            this.ccs.putAll(e.ccs);
        }
        
        protected void addCat(final Object n, final Object c){
            ccs.put(n,c);
        }
        
        protected boolean CombinationEqualsCase(final int c) {
            for (int i = 0; i < vsize; i++) {
                final Comparable str = (Comparable)ccs.get(vars[i].getName());
                if(str.toString().compareTo(vars[i].at(c).toString())!=0)
                    return false;
            }
            return true;
        }
        
        
        public String toString() {
            String comb = "";
            final Enumeration e = ccs.keys();
            while (e.hasMoreElements()) {
                final Object o = e.nextElement();
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
                    lnames[i][j] = vars[i].getCatAt(vars[i].mainSeq().catAtPos(j)).toString();
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
    
    public int[] getMatchingCases(final int[] com, int maxLevel){
        //TODO make this faster
        for(int i=0; i<com.length && i<maxLevel; i++){
            if(com[i]==-1){
                maxLevel=i;
                break;
            }
        }
        
        final int[] factors = new int[vsize];
        factors[vsize-1]=1;
        for(int i=vsize-2; i>=0; i--){
            factors[i] = factors[i+1]*vars[i+1].getNumCats();
        }
        
        int n=0;
        for(int i=0; i<maxLevel; i++){
            n += factors[i]*com[i];
        }
        final List cases = new ArrayList(factors[maxLevel-1]);
        int arraySize=0;
        for(int i=0; i<factors[maxLevel-1]; i++){
            final int[] c = ceTable[n+i].getCases();
            cases.add(c);
            arraySize += c.length;
        }
        final int[] ret = new int[arraySize];
        int pos=0;
        for(final Iterator it = cases.listIterator(); it.hasNext();){
            final int[] c = (int[])it.next();
            System.arraycopy(c, 0, ret, pos, c.length);
            pos += c.length;
        }
        return ret;
    }
    
    public void permute(final int[] perm) {
        
        final int[]   plevels = new int[vsize];
        
        // permuted pendants
        final double[]   p_table  = new double[table.length];
        final CombinationEntry[]   p_cetable  = new CombinationEntry[table.length];
        final double[]   p_exp    = new double[table.length];
        final String[][] p_lnames = new String[vsize][];
        final SVar[] p_vars = new SVar[vsize];
        for (int i=0; i<vsize; i++)
            p_lnames[i] = new String[lnames[perm[i]].length];
        final int[]      p_levels = new int[vsize];
        
        plevels[vsize-1] = 0;
        plevels[vsize-2] = levels[vsize-1];		// calculate the number of cells covered by a
        // category in level vsize
        for (int i=vsize-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        final int[][] index;
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
