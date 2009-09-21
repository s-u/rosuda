package org.rosuda.JGR.toolkit;

/**
 * PlatformMac mac-specific handlers
 */

import java.io.File;

import org.rosuda.JGR.JGR;

/**
 * This is just an example of what individaul implementations may want to do in
 * order to make the handlers work. Clearly we cannot implement
 * {@link #handleOpenFile} - that's up to the individual application.
 */
public class PlatformMac extends org.rosuda.util.PlatformMac {
	public PlatformMac() {
		super();
		JGRPrefs.isMac = true;
	}

	public void handleQuit() {
		JGR.MAINRCONSOLE.exit();
	}

	public void handleAbout() {
		new AboutDialog();
	}

	public void handleOpenFile(File fileName) {
	}

	public void handlePrefs() {
		PrefDialog inst = PrefDialog.showPreferences(null);
		inst.setLocationRelativeTo(null);
		inst.setVisible(true);
	}

}
