package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.Frame;

/**
 * JGRDataFileOpenDialog - implementation of a file-dialog which allows loading
 * datasets into R by choosing several options.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 * @deprecated use JGRDataFileOpenDialog
 */

public class JGRDataFileDialog {

	/**
	 * Create a new DataFileDialog (Open)
	 * 
	 * @param f
	 *            parent frame
	 * @param directory
	 *            current directory
	 */
	public JGRDataFileDialog(Frame f, String directory) {
		new JGRDataFileOpenDialog(f, directory);
	}
}
