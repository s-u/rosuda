package org.rosuda.JGR.toolkit.*;

/**
 *  ProgressLabel
 *
 *  similar to Cocoa ProgressIcon
 *
 *  @author Markus Helbig
 *
 *  RoSuDA 2003 - 2004
 */


import java.awt.*;


public class ProgressLabel extends Canvas implements Runnable {
	
	private Thread thread;
	private boolean next = false;
	private int angle = 0;
	private int x,length,a;
	private Image img = null;
	private Graphics g2 = null;
	private Color col = Color.darkGray;
	
	public ProgressLabel(int g) {
		this.setSize(g,g);
		this.x = g / 2;
		this.length = x - (x/10); 
		a = (length*3)/4;
	}
	
	public void update(Graphics g) {
		if (img == null) {
			img = createImage(this.getWidth(),this.getHeight());
			g2 = img.getGraphics();
		}
		g2.setColor(this.getBackground());
		g2.fillRect(0,0,this.getWidth(),this.getHeight());
		g2.setColor(col);
		g2.fillArc(x - length, x - length, 2*length,2*length,0,360);
		drawProgress(g2,angle % 60);
		g.drawImage(img,0,0,this);
	}
	
	private void drawProgress(Graphics g, int pos) {
		g.setColor(this.getBackground());
		int z = 360;
		for (int i = 0; i <=z; i += 20) {
			g.fillArc(x - length, x - length, 2*length, 2*length,i+pos,10);
		}
		g.fillArc(x-a,x-a,2*a,2*a,0,360);
	}
	
    public void start() {
        thread = new Thread(this);
        if (this.isVisible()) 
            next = true;
        this.setVisible(true);
        thread.start();
    }

    public void stop() {
        this.setVisible(false);
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        if (next) {
            next = false;
            start();
        }
    }
	
	public void run() {
		try {
			while (true) {
				Thread.sleep(50);
				angle += 5;
				repaint();
			}
		}
		catch(Exception e){
			
		}
	}
	
	public static void main(String[] args) {
		Frame f = new Frame();
		ProgressLabel p = new ProgressLabel(28);
		f.add(p);
		f.pack();
		f.setVisible(true);
		p.start();
	}

}
