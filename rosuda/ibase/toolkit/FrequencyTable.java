package org.rosuda.ibase.toolkit;

import java.util.*;
import org.rosuda.ibase.*;

public class FrequencyTable {

	private static final int HORIZONTAL = 0;
	private static final int VERTICAL = 1;
	
	private int rows,cols,vsize;
	private SVar[] vars;
	private Object[][] ftable;
	
	public FrequencyTable(SVar[] vvs, int cols, int rows) {
		this.vars = vvs;
		this.vsize = vvs.length;
		this.cols = cols;
		this.rows = rows;
		this.ftable = new Object[rows][cols];
		for (int c = 0; c < cols; c++)
			for (int r = 0; r < rows; r++)
				ftable[r][c] = new CombinationEntry();
		init();
		calculate();
	}
	
	// init frequency table doing splits
	public void init() {
		int mode,r=0,c=0,ca;
		for (int i = 0; i < vsize; i++) {
			if (i%2==0) mode = HORIZONTAL;
			else mode = VERTICAL;
			
			int nextSplitC = -1;
			if (i+2 < vsize) nextSplitC = vars[i+2].getNumCats();
			Object[] cats = vars[i].getCategories();
			
			if (mode==HORIZONTAL){
				for (ca = 0; ca < cats.length; ca++) {
					for (c = (nextSplitC==-1?ca:nextSplitC*ca); c < cols; c+= (nextSplitC==-1?cats.length:1)) {
						for (r = 0; r < rows; r++) {
							((CombinationEntry) ftable[r][c]).addCat(vars[i].getName(),cats[ca]);
						}
						if (nextSplitC != -1 && (c+1)%nextSplitC==0)
							c += cats.length*(nextSplitC==-1?1:nextSplitC);
					}
				}
			}
			if (mode==VERTICAL) {
					for (ca = 0; ca < cats.length; ca++) {
						for (r = (nextSplitC==-1?ca:nextSplitC*ca); r < rows; r += (nextSplitC==-1?cats.length:1)) {
							for (c = 0; c < cols; c++)
								((CombinationEntry) ftable[r][c]).addCat(vars[i].getName(),cats[ca]);
							if (nextSplitC != -1 && (r+1)%nextSplitC==0)
								r += cats.length*(nextSplitC==-1?1:nextSplitC);
						}
					}
			}
		}
	}
	
	//calculate counts for each cell
	public void calculate() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				for (int cs = 0; cs < vars[0].size(); cs++) {
					CombinationEntry ce = (CombinationEntry) ftable[r][c];
					if (ce.CombinationEqualsCase(cs)) 
						ce.cases.add(new Integer(cs));
				}
			}
		}

	}
	
	// get amount of cases
	public int getCasesSize() {
		return vars[0].size();
	}
	
	//get amount of cases in this row
	public int getRowCases(int r) {
		//System.out.println(r);
		int cnt = 0;
		for (int i = 0; i < cols; i++) 
			cnt +=  ((CombinationEntry) ftable[r][i]).getCombSize();
		return cnt;
	}
	
	//get max amout of cases in this row
	public int getMaxRowCases(int r) {
		int tmp,cnt = 0;
		for (int i = 0; i < cols; i++) { 
			tmp = ((CombinationEntry) ftable[r][i]).getCombSize();
			cnt = tmp < cnt ? cnt : tmp;
		}
		return cnt;
	}
	
	//get amount of cases in this col
	public int getColCases(int c) {
		int cnt = 0;
		for (int i = 0; i < rows; i++)
			cnt +=  ((CombinationEntry) ftable[i][c]).getCombSize();
		return cnt;
	}
	
	//get max amount of this cases in this col
	public int getMaxColCases(int c) {
		int tmp,cnt = 0;
		for (int i = 0; i < rows; i++) { 
			tmp = ((CombinationEntry) ftable[i][c]).getCombSize();
			cnt = tmp < cnt ? cnt : tmp;
		}
		return cnt;
	}
	
	//get max count in all combinations
	public int getMax() {
		int tmp,cnt = 0;
		for (int i = 0; i < cols; i++) { 
			tmp = this.getMaxColCases(i);
			cnt = tmp < cnt ? cnt : tmp;
		}
		return cnt;
	}

	
	//get counts of one specific combination
	public int getCountsAt(int r, int c) {
		return ((CombinationEntry) ftable[r][c]).getCombSize();
	}
	
	//get Case IDs of this combination
	public int[] getCasesAt(int r, int c) {
		return ((CombinationEntry) ftable[r][c]).getCases();
	}
	
	//get Info of combination
	public String getInfo(int r, int c) {
		return ((CombinationEntry) ftable[r][c]).getCombInfo();
	}
	
	public void print() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++)
				System.out.println(ftable[r][c]+"\n");
			System.out.println();
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
}
