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
FAC - factor
  TSP:
    <number of levels>
    <level 0>
	...
    <level n>
  the data consist of level indices (starting at 0); -1 denotes NA

The flag @ after a type denotes that there are missing values.

Any data beyond the last record of the last field are silently discarded.
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
