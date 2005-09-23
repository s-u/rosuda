package org.rosuda.ibase.toolkit;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.rosuda.ibase.SVar;

public class FrequencyTable {
    
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    
    private int vsize;
    private int k; // same as vsize
    private SVar[] vars;
    private CombinationEntry[] ceTable;
    private double[] table;
    private double[] exp;
    
    private double G2, X2;
    private int df;
    private double p;
    
    public InteractionSet Interactions;
    
    public FrequencyTable(SVar[] vvs) {
        this.vars = vvs;
        this.vsize = vvs.length;
        k=vsize;
        
        int tableLen = 1;
        for(int i=0; i< vsize; i++){
            tableLen *= vars[i].getNumCats();
        }
        ceTable = new CombinationEntry[tableLen];
        table = new double[tableLen];
        exp = new double[tableLen];
        Interactions = new InteractionSet(vsize);
        
        df = tableLen-1;
        
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
        
        // init exp
        for( int i=0; i<vsize-1; i++ ) {
            addInteraction( new int[] { i }, false );
        }
        addInteraction( new int[] { vsize-1 } , true  );
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
    
    public boolean deleteInteraction( int[] delInteraction ) {
        boolean in = Interactions.isMember( delInteraction );
        if( in && delInteraction.length > 1 ) {
            Interactions.deleteMember( delInteraction );
            this.logLinear();
        }
        return in;
    }
    
    
    /**
     * From org.rosuda.Mondrian.Table.
     */
    public void logLinear() {
        
        int[][] interact = new int[Interactions.Ssize()][];
        for( int i=0; i<Interactions.Ssize(); i++ ) {
            interact[i] = (int[])Interactions.SmemberAt(i);
        }
        int n = this.table.length;
        int[][] permArray = new int[interact.length][this.k];
        int[][] ipermArray = new int[interact.length][this.k];
        int[] sumInd = new int[interact.length];
        for( int i=0; i<interact.length; i++ ) {
            for( int j=0; j<k; j++ ) {
                if( j<interact[i].length )
                    permArray[i][j] = interact[i][j];
                else
                    permArray[i][j] = -1;
            }
        }
        for( int i=0; i<interact.length; i++ ) {
            for( int j=0; j<this.k; j++ ) {
                int l=0;
                while( permArray[i][l] != j )
                    if( permArray[i][l] == -1 )
                        permArray[i][l] = j;
                    else
                        l++;
            }
        }
        for( int i=0; i<interact.length; i++ ) {
            sumInd[i] = 1;
            for( int j=0; j<this.k; j++ ) {
                ipermArray[i][permArray[i][j]] = j;
                if( ! (j<interact[i].length) )
                    sumInd[i] *= getLevels()[permArray[i][j]];
            }
        }
        double[] lastIteration = new double[n];
        for( int i=0; i<n; i++ )
            this.exp[i] = 1;
        boolean converge = false;
        while( !converge ) {
            System.out.println("");
            System.out.println("###########################");
            for( int i=interact.length-1; i>=0; i-- ) {
                System.out.println("Interaction No: "+i);
                //this.permute(permArray[i]);
                double obs = 0.0;
                double exp = 0.0;
                double scale;
                for( int l=0; l<n; l++ ) {
                    obs += this.table[l];
                    exp += this.exp[l];
                    if( ((l+1) % sumInd[i]) == 0 ) {
                        if( exp < 0.0001 )
                            scale = 0;
                        else
                            scale = obs / exp;
                        obs = 0;
                        exp = 0;
                        for( int m=l-sumInd[i]+1; m<=l; m++ )
                            this.exp[m] *= scale;
                    }
                }
                //this.permute(ipermArray[i]);
            }
            converge = true;
            for( int l=0; l<n; l++ )
                converge = converge && (Math.abs(lastIteration[l] - this.exp[l]) < 0.01);
            System.arraycopy(this.exp, 0, lastIteration, 0, n);
        }
        G2 = 0; X2 = 0;
        for( int l=0; l<n; l++ ) {
            if( table[l] > 0 ) {
                G2 += 2 * table[l] * Math.log( table[l] / exp[l] );
                X2 += Math.pow( table[l] - exp[l], 2 ) / exp[l];
            }
        }
        this.df = table.length - 1;
        for( int i=0; i<Interactions.size(); i++ ) {
            int mul=1;
            for( int j=0; j<(Interactions.memberAt(i)).length; j++ )
                mul *= getLevels()[(Interactions.memberAt(i))[j]] - 1;
            df -= mul;
        }
        
        this.p = 1-pchisq( G2, df);
        
    }
    
    private double pchisq( double q, int df ) {
        
        double up=0.9999999;
        double lp=0.0000001;
        while( Math.abs( up - lp ) > 0.0001 )
            if(df * Math.pow( 1 - 2/(9*(double)df) + qnorm((up+lp)/2)*Math.pow(2/(9*(double)df), 0.5), 3) <= q)
                lp = (up+lp)/2;
            else
                up = (up+lp)/2;
        return up;
    }
    
    private double qnorm( double p ) {
        
        double a0 = 2.515517;
        double a1 = 0.802853;
        double a2 = 0.010328;
        
        double b1 = 1.432788;
        double b2 = 0.189269;
        double b3 = 0.001308;
        
        double  t = Math.pow(-2*Math.log(1-p), 0.5);
        
        return t - (a0 + a1*t + a2*t*t) / (1 + b1*t + b2*t*t + b3*t*t*t);
    }
    
    public void permute(int[] perm) {
        
        Interactions.permute( perm );
        
        int[]   plevels = new int[k];
        int[][] index;
        // permuted pendants
        double[]   p_table  = new double[table.length];
        double[]   p_exp    = new double[table.length];
        String[][] p_lnames = new String[k][];
        SVar[] p_vars = new SVar[k];
        for (int i=0; i<k; i++)
            p_lnames[i] = new String[lnames[perm[i]].length];
        int[]      p_levels = new int[k];
        
        plevels[k-1] = 0;
        plevels[k-2] = levels[k-1];		// calculate the number of cells covered by a
        // category in level k
        for (int i=k-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        index = new int[table.length][k];
        
        int decompose;
        
        for (int i=0; i<table.length; i++) {
            decompose = i;
            for (int j=0; j<k-1; j++) {
                index[i][j] = decompose / plevels[j];
                decompose -= index[i][j] *  plevels[j];
            }
            index[i][k-1] = decompose;
        }
        
        for (int i=0; i<k; i++) {                  // permute the names: this is easy
            p_vars[i] = vars[perm[i]];
            p_levels[i] = levels[perm[i]];
        }
        
        for (int i=0; i<k; i++) {                  // and the level names
            for (int j=0; j<(p_lnames[i].length); j++) {
                p_lnames[i][j] = lnames[perm[i]][j];
            }
        }
        
        levels = p_levels;
        this.lnames = p_lnames;
        
        plevels[k-2] = levels[k-1];		 // calculate the number of cells covered by a
        // category in level k
        for (int i=k-3; i>=0; i--) {
            plevels[i] = plevels[i+1] * levels[i+1];
        }
        
        for (int i=0; i<table.length; i++) {
            decompose = 0;
            for (int j=0; j<k-1; j++) {
                decompose += index[i][perm[j]] * plevels[j];
            }
            decompose += index[i][perm[k-1]];
            p_table[decompose]  = table[i];
            p_exp[decompose]    = exp[i];
        }
        
        table = p_table;
        exp = p_exp;
        vars = p_vars;
        
    } // end perm
    
    
    
    public boolean addInteraction( int[] newInteraction, boolean update ) {
        if( !Interactions.isMember( newInteraction ) ) {
            Interactions.newMember( newInteraction );
            if( update ) {
                if( this.k > 1 )
                    this.logLinear();
                else
                    System.arraycopy(this.table, 0, this.exp, 0, table.length);
            }
            return true;
        } else
            return false;
    }
    
    class InteractionSet implements Cloneable {
        
        private Vector Set = new Vector(256,0);
        private Vector Strip;
        private int k;
        private int[] insertAt;
        private final int IN = 1;
        private final int OUT = 0;
        
        public InteractionSet(int k) {
            this.k = k;
            insertAt = new int[k];
        }
        
        public Object clone() {
            InteractionSet clone = new InteractionSet(this.k);
            clone.Set = (Vector)Set.clone();
            clone.Strip = (Vector)Strip.clone();
            clone.k = this.k;
            System.arraycopy(this.insertAt, 0, clone.insertAt, 0, this.insertAt.length);
            
            return clone;
        }
        
        public boolean isMember(int[] k) {
            
            int l = Set.size();
            BitSet test = new BitSet(k.length);
            
            for( int i=0; i<k.length; i++ )
                test.set(k[i]);
            
            for( int i=0; i<l; i++) {
                if( test.equals((BitSet)(Set.elementAt(i))) )
                    return true;
            }
            return false;
        }
        
        boolean isMember(BitSet test) {
            
            for( int i=0; i<Set.size(); i++) {
                if( test.equals((BitSet)(Set.elementAt(i))) )
                    return true;
            }
            return false;
        }
        
        public void newMember(int[] inters) {
            
            if( !isMember(inters) ) {
                BitSet newI = new BitSet(inters.length);
                
                for( int i=0; i<inters.length; i++ )
                    newI.set(inters[i]);
                
                int pos = nBits(newI)-1;
                if( insertAt[pos] < Set.size() )
                    Set.insertElementAt(newI, insertAt[pos]);
                else
                    Set.addElement(newI);
                
                for( int i=pos; i<k; i++ )
                    insertAt[i]++;
                
                maintainHirarchie( newI, IN );
                Strip = (Vector)Set.clone();
                strip();
            }
        }
        
        void newMember(BitSet incl) {
            
            BitSet newI = (BitSet)incl.clone();
            
            int pos = nBits(newI)-1;
            if( insertAt[pos] < Set.size() )
                Set.insertElementAt(newI, insertAt[pos]);
            else
                Set.addElement(newI);
            for( int i=pos; i<k; i++ )
                insertAt[i]++;
        }
        
        void setMember(int[] k, int l) {
            
            BitSet newI = new BitSet(k.length);
            
            for( int i=0; i<k.length; i++ )
                newI.set(k[i]);
            
            Set.setElementAt(newI, l);
        }
        
        public void deleteMember(int[] inter) {
            
            BitSet test = new BitSet(inter.length);
            
            for( int i=0; i<inter.length; i++ )
                test.set(inter[i]);
            
            for( int i=0; i<Set.size(); i++)
                if( test.equals((BitSet)(Set.elementAt(i))) ) {
                Set.removeElementAt(i);
                for( int j=0; j<k; j++ )
                    if( insertAt[j] > i )
                        insertAt[j]--;
                }
            maintainHirarchie( test, OUT );
            Strip = (Vector)Set.clone();
            strip();
        }
        
        public void deleteMember(BitSet rem) {
            
            for( int i=0; i<Set.size(); i++)
                if( rem.equals((BitSet)(Set.elementAt(i))) ) {
                Set.removeElementAt(i);
                for( int j=0; j<k; j++ )
                    if( insertAt[j] > i )
                        insertAt[j]--;
                }
        }
        
        public int[] memberAt(int k) {
            
            BitSet out = (BitSet)(Set.elementAt(k));
            int[] Inter = new int[nBits(out)];
            int count = 0;
            for( int i=0; i< out.size(); i++ )
                if( out.get(i) )
                    Inter[count++] = i;
            return Inter;
        }
        
        public int[] SmemberAt(int k) {
            
            BitSet out = (BitSet)(Strip.elementAt(k));
            int[] Inter = new int[nBits(out)];
            int count = 0;
            for( int i=0; i< out.size(); i++ )
                if( out.get(i) )
                    Inter[count++] = i;
            return Inter;
        }
        
        public int size() {
            return Set.size();
        }
        
        public int Ssize() {
            return Strip.size();
        }
        
        public void permute( int[] perm ) {
            
            for( int i=0; i<this.size(); i++ ) {
                int[] interPerm = (int[])this.memberAt(i);
                for( int j=0; j<interPerm.length; j++ ) {
                    int k=0;
                    while( perm[k] != interPerm[j] )
                        k++;
                    interPerm[j] = k;
                }
                this.setMember(interPerm, i);
            }
        }
        
        void maintainHirarchie(BitSet InOut, int dir) {
            
            //System.out.println("Maintain: "+InOut.toString());
            BitSet Inter = (BitSet)InOut.clone();
            if( dir == IN ) {
                if( nBits(Inter) > 2 ) {
                    for( int i=0; i<Inter.size(); i++)
                        if( Inter.get(i) ) {
                        Inter.clear(i);
                        if( !isMember( Inter ) ) {
                            newMember( Inter );
                            maintainHirarchie(Inter, IN);
                        }
                        Inter.set(i);
                        }
                }
            } else {
                for( int i=0; i<Set.size(); i++ ) {
                    BitSet Inter1 = (BitSet)Inter.clone();
                    Inter1.and( ((BitSet)Set.elementAt(i)) );
                    if( Inter.equals( Inter1 ) )
                        deleteMember( (BitSet)Set.elementAt(i) );
                }
            }
        }
        
        void strip() {
            
            int i=Set.size();
            while( i>0 ) {
                i--;
                BitSet Inter = ((BitSet)Strip.elementAt(i));
                int j=i;
                while( j>0 ) {
                    j--;
                    BitSet inStrip = (BitSet)Strip.elementAt(j);
                    if( !Inter.equals(inStrip) ) {
                        BitSet Inter1 = (BitSet)Inter.clone();
                        Inter1.and( inStrip );
                        if( inStrip.equals( Inter1 ) ) {
                            Strip.removeElementAt(j);
                            i--;
                        }
                    }
                }
            }
        }
        
        int nBits(BitSet b) {
            
            int count = 0;
            for( int i=0; i< b.size(); i++ )
                if( b.get(i) )
                    count++;
            return count;
        }
    }

    public SVar[] getVars() {
        return vars;
    }
    
}
