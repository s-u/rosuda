//
//  DataRoot.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.klimt;

import java.util.*;
import org.rosuda.ibase.*;

public class DataRoot {
    SVarSet dataset;
    TreeRegistry tr;
    int type;

    public static final int DT_Regular = 0;
    public static final int DT_Forest  = 0x100;
    
    public DataRoot(SVarSet ds, TreeRegistry tr, int dataType) {
        dataset=ds;
        this.tr=tr;
        type=dataType;
    }

    public DataRoot(SVarSet ds) {
        this(ds,new TreeRegistry(),DT_Regular);
    }

    public TreeRegistry getTreeRegistry() {
        return tr;
    }

    public SVarSet getDataSet() {
        return dataset;
    }

    public int getDataType() {
        return type;
    }

    public void setDataType(int dataType) {
        type=dataType;
    }
}
