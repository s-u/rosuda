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

/** DataRoot is an object associated with each data set. It contains meta information of the data */
public class DataRoot {
    /** Underlying dataset */
    SVarSet dataset;
    /** Associated tree registry with trees which are based on this data */
    TreeRegistry tr;
    /** Data set type */
    int type;

    /** Dataset type constant: regular dataset */
    public static final int DT_Regular = 0;
    /** Dataset type constant: forest data. this changes linking behavior */
    public static final int DT_Forest  = 0x100;
    
    public DataRoot(SVarSet ds, TreeRegistry tr, int dataType) {
        dataset=ds;
        this.tr=tr;
        type=dataType;
    }

    /** create a new data root with a new (empty) tree resistry and data type DT_Regular */
    public DataRoot(SVarSet ds) {
        this(ds,new TreeRegistry(),DT_Regular);
    }

    /** retrieves associated tree registry
        @return tree registry */
    public TreeRegistry getTreeRegistry() {
        return tr;
    }

    /** retrieves associated dataset
        @return dataset */
    public SVarSet getDataSet() {
        return dataset;
    }

    /** returns data type of this data set */
    public int getDataType() {
        return type;
    }

    /** override data type (e.g. when new meta data was added)
        @param dataType new data type */
    public void setDataType(int dataType) {
        type=dataType;
    }
}
