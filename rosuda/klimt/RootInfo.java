//
//  RootInfo.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

/** this class encapsulates information associated with a tree which was formerly stored in the root node */
public class RootInfo {
    /** classification or regression variable */
    public SVar response=null;
    /** classifier or predicted response */
    public SVar prediction=null;

    /** root-only: name of the tree */
    public String name=null;
    /** formula used to grow the tree */
    public String formula;

    /** associated frame */
    public TFrame frame;

    /** tree registry containing this tree (in general a tree should be in one registry only) */
    public TreeRegistry home;
}
