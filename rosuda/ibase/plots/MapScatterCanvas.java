//
//  MapScatterPlot.java
//  Klimt
//
//  Created by Simon Urbanek on 22.02.05.
//  Copyright 2005 Simon Urbanek. All rights reserved.
//

package org.rosuda.ibase.plots;

import java.awt.*;
import org.rosuda.pograss.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

/** Scatter plot overlaying a map */
public class MapScatterCanvas extends ScatterCanvas {
	// map variable
	SVar map;
    public MapScatterCanvas(int gd, Frame f, SVar v1, SVar v2, SVar map, SMarker mark) {
		super(gd,f, v1, v2, mark);
		this.map = map;
	}
	
	public void paintBackground(PoGraSS g) {
		int i=0;
		g.setColor("black");
		while (i<map.size()) {
			MapSegment ms=(MapSegment) map.at(i);
            if (ms!=null) {
                int j=0;
                while (j<ms.count()) {
					g.drawPolygon(MapSegmentTools.transViaAxisX(ms,j,ax),MapSegmentTools.transViaAxisY(ms,j,ay),ms.getSizeAt(j));
                    j++;
                }
            }
			i++;
		}			
	}
}
