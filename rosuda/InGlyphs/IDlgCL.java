package org.rosuda.InGlyphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.rosuda.ibase.toolkit.PGSCanvas;

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