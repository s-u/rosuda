package org.rosuda.JGR.toolkit;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.FrameDevice;
import org.rosuda.ibase.toolkit.WTentry;
import org.rosuda.ibase.toolkit.WTentrySwing;
import org.rosuda.ibase.toolkit.WinTracker;

/**
 * 
 * 
 * @author Markus Helbig
 * 
 */
public class JGRFrame extends JFrame implements FrameDevice {

	private WTentry WTmyself;

	public JGRFrame() {
		this("<unnamed>", 0);
	}

	public JGRFrame(String title, int wclass) {
		String nativeLF = UIManager.getSystemLookAndFeelClassName();

		// Install the look and feel
		try {
			UIManager.setLookAndFeel(nativeLF);
		} catch (InstantiationException e) {
			/***/
		} catch (ClassNotFoundException e) {
			/***/
		} catch (UnsupportedLookAndFeelException e) {
			/***/
		} catch (IllegalAccessException e) {
			/***/
		}

		this.setTitle(title);
		this.getContentPane().setBackground(
				UIManager.getColor("Label.background"));

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		if (WinTracker.current == null)
			WinTracker.current = new WinTracker();
		WTmyself = new WTentrySwing(WinTracker.current, this, title, wclass);

		initPlacement();
	}

	private static int lastClass = -1;

	private static int lastPlaceX = 0, lastPlaceY = 0;

	private static int lastOffset = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#initPlacement()
	 */
	public void initPlacement() {
		if (WTmyself == null)
			return;
		if (lastClass != WTmyself.wclass) {
			lastClass = WTmyself.wclass;
			lastPlaceX = getWidth() + 10;
			lastPlaceY = 0;
			lastOffset = 0;
		} else {
			setLocation(lastPlaceX, lastPlaceY);
			lastPlaceX += getWidth() + 10;
			Common.getScreenRes();
			if (lastPlaceX + 100 > Common.screenRes.width) {
				lastPlaceY += getHeight() + 20;
				lastPlaceX = 0;
				if (lastPlaceY + 100 > Common.screenRes.height) {
					lastOffset += 30;
					lastPlaceY = lastOffset;
					lastPlaceX = lastOffset;
				}
			}
		}
	}

	public synchronized void setWorking(final boolean work) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (work)
					cursorWait();
				else
					cursorDefault();
			}
		});
	}

	/**
	 * Show waitcursor (speeningwheel or sandglass).
	 */
	void cursorWait() {
		Component gp = getRootPane().getGlassPane();
		gp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		gp.setVisible(true);
	}

	/**
	 * Show default cursor.
	 */
	void cursorDefault() {
		Component gp = getRootPane().getGlassPane();
		gp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		gp.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#getFrame()
	 */
	public Frame getFrame() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		if (b)
			super.setState(Frame.NORMAL);
		super.setVisible(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#addWindowListener(java.awt.event.WindowListener)
	 */
	public void addWindowListener(WindowListener l) {
		super.addWindowListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#setSize(java.awt.Dimension)
	 */
	public void setSize(Dimension d) {
		super.setSize(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#pack()
	 */
	public void pack() {
		super.pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rosuda.ibase.toolkit.FrameDevice#add(java.awt.Component)
	 */
	public Component add(Component c) {
		return getContentPane().add(c);
	}

}
