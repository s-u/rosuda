//
//  PlotComponentFactory.java
//  
//  Factory for creating PlotComponents and associated widgets automatically
//
//  Created by Simon Urbanek on 12/4/05.
//  $Id$
//

package org.rosuda.ibase.toolkit;

import org.rosuda.ibase.*;
import java.awt.Window;

/** Factory for creating PlotComponents and associated widgets automatically. All methods are static, thus creating an instance of this class doesn't make any sense. */
public class PlotComponentFactory {
	/** create an empty PlotComponent using the specified engine (AWT/SWING/...) */
	public static PlotComponent createPlotComponent(final int engine) {
		if (engine == PlotComponent.AWT)
			return new AwtPlotComponent();
		else if (engine == PlotComponent.SWING)
			return new SwingPlotComponent();
		else throw(new RuntimeException("PlotComponentFactory: unsupported plot component engine ("+engine+")"));
	}
	
	/** create an empty PlotComponent using the default engine (see Common.defaultPlotComponentType) */
	public static PlotComponent createPlotComponent() {
		return createPlotComponent(Common.defaultPlotComponentType);
	}

	/** create a QueryPopup that fits the supplied PlotComponent. If the component has a parent window already, then the win argument can be null, otherwise win is used as the parent frame for the popup. */
	public static QueryPopup createQueryPopup(final PlotComponent pc, final Window win, final SVarSet vs, final String ct, final int w, final int cid) {
		final int engine = pc.getGraphicsEngine();
		if (engine == PlotComponent.AWT)
			return new AwtQueryPopup(pc, win, vs, ct, w, cid);
		else if (engine == PlotComponent.SWING)
			return new SwingQueryPopup(pc, win, vs, ct, w, cid);
		else throw(new RuntimeException("PlotComponentFactory.createQueryPopup: unsupported plot component engine ("+engine+")"));
	}

	/** create a QueryPopup that fits the supplied PlotComponent. If the component has a parent window already, then the win argument can be null, otherwise win is used as the parent frame for the popup. */
	public static QueryPopup createQueryPopup(final PlotComponent pc, final Window win, final SVarSet vs, final String ct) {
		return createQueryPopup(pc, win, vs, ct, -1, -1);
	}
}
