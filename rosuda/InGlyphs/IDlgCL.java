package org.rosuda.InGlyphs;

import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

class IDlgCL implements ActionListener {
	PGSCanvas c;
	IDlgCL(PGSCanvas cc) {
		c=cc;
	}

	/** activated if a button was pressed. It determines whether "cancel" was pressed or OK" */
	public void actionPerformed(ActionEvent e) {
            /*
		c.cancel=!e.getActionCommand().equals("OK");
		c.intDlg.setVisible(false);
             */
	}
}