//
//  Loader.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package org.rosuda.ibase;

import java.io.*;
import java.util.StringTokenizer;

public class Loader {
    public static int LoadTSV(BufferedReader r, SVarSet vset) {
        try {
            int vsb=vset.count();
            String s=r.readLine(); // read line
            StringTokenizer st=new StringTokenizer(s,"\t");
            while (st.hasMoreTokens()) {
                String t=st.nextToken();
                SVar v=new SVar(t);
                vset.add(v);
            }
            int j=0;
            while (r.ready()) {
                String ls=r.readLine(); // read line
                if (ls==null || ls.length()==0) break;
                StringTokenizer lst=new StringTokenizer(ls,"\t");
                int i=0;
                while (lst.hasMoreTokens()) {
                    String t=lst.nextToken();
                    SVar v=vset.at(vsb+i);
                    v.add(t);
                    i++;
                }
                j++;
            }
            return j;
        } catch(IOException e) {
            System.out.println("Loader.LoadTSV[IOException]: "+e.getMessage());
        }
        return 0;
    }

    public static int LoadPolygons(BufferedReader r) {
        return 0;
    }
}
