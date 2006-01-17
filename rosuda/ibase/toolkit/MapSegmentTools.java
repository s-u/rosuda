//
//  MapSegmentEx.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import org.rosuda.ibase.*;

/** this class provides a few toolkit-specific tools for handling {@link MapSegment}s */
public class MapSegmentTools {
    /** transform X coordinates of a {@link MapSegment} according to the specified {@link Axis} and return the result.
    @param ms map segment to transform
    @param id which segment entry to transform
    @param axis to be used for transformation
    @return transformed X axis as an array of integers */
    public static int[] transViaAxisX(final MapSegment ms, final int id, final Axis a) {
        final MapSegmentEntry mse=ms.at(id);
        if (mse==null) return null;
        final int[] xs=new int[mse.xp.length];
        int i=0;
        while (i<mse.xp.length) { xs[i]=a.getValuePos(mse.xp[i]); i++; };
        return xs;
    }

    /** transform Y coordinates of a {@link MapSegment} according to the specified {@link Axis} and return the result.
    @param ms map segment to transform
    @param id which segment entry to transform
    @param axis to be used for transformation
    @return transformed Y axis as an array of integers */
    public static int[] transViaAxisY(final MapSegment ms, final int id, final Axis a) {
        final MapSegmentEntry mse=ms.at(id);
        if (mse==null) return null;
        final int[] ys=new int[mse.yp.length];
        int i=0;
        while (i<mse.yp.length) { ys[i]=a.getValuePos(mse.yp[i]); i++; };
        return ys;
    }    
}
