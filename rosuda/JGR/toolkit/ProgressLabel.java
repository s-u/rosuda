package org.rosuda.JGR.toolkit;

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
import javax.swing.*;


public class ProgressLabel extends Canvas implements Runnable {

    private Thread thread;
    private boolean next = false;
    private int angle = 0;
    private int x,length,a, gap = 16;
    private Image img = null;
    private Graphics g2 = null;
    private Color col = Color.darkGray;
    private int sleep = 150;

    public ProgressLabel(int g) {
        //super(true);
        this.setSize(g+10,g+10);
        this.x = g / 2;
        this.length = x - (x/10);
        this.x += 5;
        a = (length*3)/5;
        thread = new Thread(this);
        thread.start();
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
        for (int i = 0; i <=z; i += 2*gap) {
            g.fillArc(x - length, x - length, 2*length, 2*length,i-pos,gap);
        }
        g.fillArc(x-a,x-a,2*a,2*a,0,360);
    }

    public void start() {
        //if (this.isVisible()) next = true;
        this.setVisible(true);
        //this.repaint();
    }

    public void stop() {
        this.setVisible(false);
        //if (next) { next = false; this.start(); }
    }

    public void setVisible(boolean b) {
        System.out.println("setVisible "+b);
        super.setVisible(b);
    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(sleep);
                //System.out.println("isrunning");
                angle += 10;
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
        p.setVisible(true);
    }

}
