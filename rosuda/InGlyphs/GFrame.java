package org.rosuda.InGlyphs;

//
//  GFrame.java
//  InGlyphs
//
//  Created by Daniela DiBenedetto on Tue Nov 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

import org.rosuda.ibase.toolkit.TFrame;

public class GFrame extends TFrame {
    public Dataset dataSet = null;
    public String chartType = null;
    public String scale =null;

    public GFrame(String tit,int wclass) { super(tit,true,wclass); }
}
