//
//  TreeEntry.java
//  Klimt
//
//  Created by Simon Urbanek on Tue Jul 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

public class TreeEntry {
    public SNode root;
    public String name;
    
    public TreeEntry(SNode t, String n) {
        root=t; name=n;
    }
}
