//
//  LoaderFLF.java
//  Klimt
//
//  Created by Simon Urbanek on 1/24/05.
//  Copyright 2005 Simon Urbanek. All rights reserved.
//

package org.rosuda.ibase;

import java.io.*;
import org.rosuda.util.*;

/** Loader for FLF (Fast Loading Files) data format.
FLF is a simple (text-based) format that is optimized to allow fast loading. In addition to regualar ASCII text formats it contains metainformation that allow the program to pre-allocate buffers and there is no need for type guessing. The actual format is line-based and starts with FLF1 magic line followed by headers. Those are terminated by an empty line which is followed byt he actual data.

FLF1
<headers>

<data>

** headers **
<property>:<value>

mandatory properties:
fs:<number of fields=variables>
rec:<number of records>

** data **
<variable name>
<variable type (3 upper-case chars)>[@][...]
[<type-specific properties (TSP)>]
<data> (as many lines as there are records)

** types **
INT - integer, no TSP
FLT - float, no TSP
PLN - polygons, no TSP (for data format see below)
FAC - factor
  TSP:
    <number of levels>
    <level 0>
	...
    <level n>
  the data consist of level indices (starting at 0); -1 denotes NA

The flag @ after a type denotes that there are missing values.

Any data beyond the last record of the last field are silently discarded.

PLN - polygons, no TSP
data are more tricky:
<# of segments (polygons) for this case> (-1 = NA, structures below are not stored then; currently 0 is silently converted to -1)
<# of points in this poly>
<flags> (currently ignored and should be always "L" = land; later may be "W" for water etc. )
<x>
..
<y>
..
[<next poly>]

*/

public class LoaderFLF {
	public int load(File f, SVarSet set) throws IOException {
		SVar[] vars=null;
		int recs=0, cv=0, fs=0;
		
		BufferedReader r = new BufferedReader(new FileReader(f));
		String s = r.readLine();
		if (s==null || !s.equals("FLF1")) throw new IOException("Unsupported file format (expecting FLF1)");
		while ((s=r.readLine())!=null && !s.equals("")) {
			if (s.substring(0,3).equals("fs:")) {
				fs = Tools.parseInt(s.substring(3));
				vars = new SVar[fs];
			}
			if (s.substring(0,4).equals("rec:"))
				recs = Tools.parseInt(s.substring(4));
		}
		if (fs<1 || recs<1) throw new IOException("File contains no data (at least 1x1 required)");
		
		while (cv<fs) {
			SVar v=null;
			String fieldName = r.readLine();
			s = r.readLine();
			if (s==null || s.length()<3) throw new IOException("Corrupted file");
			String ft = s.substring(0,3);
			if (ft.equals("INT")) {
				int ia[] = new int[recs];
				int k=0;
				while (k<recs) {
					s=r.readLine();
					if (s==null) throw new IOException("Corrupted file");
					ia[k++]=s.equals("NA")?SVar.int_NA:Tools.parseInt(s);
				}
				v = new SVarFixInt(fieldName, ia, false);
			} else if (ft.equals("FLT")) {
				double da[] = new double[recs];
				int k=0;
				while (k<recs) {
					s=r.readLine();
					if (s==null) throw new IOException("Corrupted file");
					da[k++]=s.equals("NA")?SVar.double_NA:Tools.parseDouble(s);
				}
				v = new SVarFixDouble(fieldName, da, false);
			} else if (ft.equals("PLN")) {
				v= new SVarObj(fieldName, false);
				v.setContentsType(SVar.CT_Map);
				int k=0;
				while (k<recs) {
					s=r.readLine();
					if (s==null) throw new IOException("Corrupted file");
					int ps = Tools.parseInt(s);
					if (ps<1) {
						v.add(null);
					} else {
						MapSegment ms = new MapSegment();
						while (ps>0) {
							double x[], y[];
							s=r.readLine();
							if (s==null) throw new IOException("Corrupted file");
							int pts = Tools.parseInt(s);
							if (pts<0) throw new IOException("Corrupted file (PLN.points<0)");
							x=new double[pts];
							y=new double[pts];
							int i=0;
							s=r.readLine(); // flags - currently ignored
							while (i<pts) {
								s=r.readLine();
								if (s==null) throw new IOException("Corrupted file");
								x[i]=Tools.parseDouble(s);
								i++;
							}
							i=0;
							while (i<pts) {
								s=r.readLine();
								if (s==null) throw new IOException("Corrupted file");
								y[i]=Tools.parseDouble(s);
								i++;
							}
							ms.add(x,y);
							ps--;
						}
						v.add(ms);
					}
					k++;
				}
			} else if (ft.equals("FAC")) {
				s = r.readLine();
				if (s==null) throw new IOException("Corrupted file");
				int ls = Tools.parseInt(s);
				if (ls<1) throw new IOException("Invalid file (variable "+fieldName+" has no levels anthough being a factor)");
				String facn[] = new String[ls];
				int k=0;
				while (k<ls) {
					s=r.readLine();
					if (s==null) throw new IOException("Corrupted file");
					facn[k++]=s;
				}
				int ix[] = new int[recs];
				k=0;
				while (k<recs) {
					s = r.readLine();
					if (s==null) throw new IOException("Corrupted file");
					try {
						ix[k]=Integer.parseInt(s);
					} catch(NumberFormatException dce) {
						ix[k]=-1;
					}
					k++;
				}
				v = new SVarFixFact(fieldName, ix, facn);
			}
			if (v==null) throw new IOException("Unsupported field type ("+ft+").");
			vars[cv] = v;
			cv++;
		}
		
		cv = 0;
		while (cv<fs)
			set.add(vars[cv++]);

		r.close();
		
		return fs;
	}	
}
