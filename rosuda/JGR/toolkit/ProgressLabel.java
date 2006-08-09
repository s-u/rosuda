package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * !! Currently not used !! ProgressLabel - similar to Cocoa ProgressIcon
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2004
 */

public class ProgressLabel extends Canvas implements Runnable {

	private Thread thread;

	private boolean next = false;

	private int angle = 0;

	private int x, length, a;

	private final int gap = 16;

	private Image img = null;

	private Graphics g2 = null;

	private final Color col = Color.darkGray;

	private final int sleep = 240;

	public ProgressLabel(int g) {
		this.setSize(g + 10, g + 10);
		x = g / 2;
		length = x - (x / 10);
		x += 5;
		a = (length * 3) / 5;
	}

	public void update(Graphics g) {
		if (img == null) {
			img = createImage(this.getWidth(), this.getHeight());
			g2 = img.getGraphics();
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		drawProgress(g2);
		g.drawImage(img, 0, 0, this);
	}

	private void drawProgress(Graphics g) {
		for (int i = 0; i < 360; i += 2 * gap) {
			if (i >= angle * 6 && i <= angle * 6 + 90)
				g.setColor(new Color(col.getRed(), col.getGreen(), col
						.getBlue(), 200));
			else
				g.setColor(new Color(0, 0, 0,
						(360 - i + 100 - angle) / 10));
			g.fillArc(x - length, x - length, 2 * length, 2 * length, -i, gap);
		}
		g.setColor(this.getBackground());
		g.fillArc(x - a, x - a, 2 * a, 2 * a, 0, 360);
	}

	/**
	 * Start progess-label to show JGR is working.
	 */
	public void start() {
		if (this.isVisible())
			next = true;
		this.setVisible(true);
		thread = new Thread(this);
		if (thread != null)
			thread.start();
	}

	/**
	 * Stop progress-label to show JGR is ready.
	 * 
	 */
	public void stop() {
		this.setVisible(false);
		try {
			if (thread != null)
				thread.interrupt();
		} catch (Exception e) {
			new org.rosuda.JGR.util.ErrorMsg(e);
		}
		thread = null;
		if (next) {
			next = false;
			this.start();
		}
	}

	/**
	 * Run method.
	 */
	public void run() {
		try {
			while (isVisible()) {
				Thread.sleep(sleep);
				angle = (angle + 10) % 60;
				repaint();
			}
		} catch (Exception e) {
			new org.rosuda.JGR.util.ErrorMsg(e);
		}
	}
}
