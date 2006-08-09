package org.rosuda.JGR.rhelp;

/*============================================================================

 Project:

 JAVA Source file for the class Tracer

 COPYRIGHT (C), 1998-2000, Thomas Baier, R Core development Team

 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ============================================================================*/

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;

/**
 * Tracer.java from R-project
 */

public class Tracer extends Frame {
	public static void write(String string) {
		// create a trace window
		/*
		 * if (cTracer == null) { cTracer = new Tracer (); }
		 * 
		 * cTracer._write (string);
		 */
		return;
	}

	public static void start() {
		/*
		 * if (cTracer != null) { cTracer.show (); }
		 */
		return;
	}

	public static void stop() {
		// if (cTracer != null) {
		// cTracer.setVisible (false);
		// }

		return;
	}

	private Tracer() {
		super("Java Applet Tracer");

		// construct a layout manager
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(gridbag);
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1.0;
		constraints.weightx = 1.0;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.gridheight = GridBagConstraints.REMAINDER;

		iOutput = new TextArea();

		gridbag.setConstraints(iOutput, constraints);
		add(iOutput);

		show();
		pack();

		return;
	}

	private TextArea iOutput;
}

// Local Variables:
// mode: Java
// mode: font-lock
// End:
