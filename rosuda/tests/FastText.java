/*===================================================================*\
|  FastText is an attempt to provide a fast read-only text component
|
|  (C)Copyright 2004 Simon Urbanek, All rights reserved.
|
|  Version: $Id$
|
\*===================================================================*/
 

import java.awt.*;

/** Representation of a single line. For efficiency lines are stored in a list-like structure with both next and prev pointers */
class Line {
    Line next;
    Line prev;
    String cont;
    
    public Line(Line prev, String s) {
        if (prev!=null) {
            if (prev.next!=null)
                next=prev.next;
            prev.next=this;
            this.prev=prev;
        }                    
        cont=s;
    }
}

/** this is just a test thread to simulate filling of the text component with huge data */
class TestThread extends Thread {
    FastText t;
    
    public TestThread(FastText t) {
        this.t=t;
    }
    
    public void run() {
        for (int i=0; i<100000; i++) {
            t.addLine("line "+i);
            Thread.yield();
        }
        System.out.println("thread done");
    }
}

/** Simple blank canvas, that is it calls parent's paint routine instead of providing its own */
class BlankCanvas extends Canvas {
    public void paint(Graphics g) {
        this.getParent().paint(g);
    }
}

/** Refresher runs as a thread and makes sure that the text component is properly flushed even if ansychronous repaint was used */
class Refresher extends Thread {
    FastText t;
    boolean active;
    
    public Refresher(FastText t) {
        this.t=t;
    }
    
    public void run() {
        active=true;
        while (active) {
            try {
                Thread.sleep(300);
            } catch (Exception e) {}
            
            if (t.repaintPending) {
                System.out.println("initiating pending repaint");
                t.asyncRepaint();
            }
        }
    }
}

/** This class attempts to implement a fast read-only text component with all bells and whisles like asynchronous repaint etc. */
public class FastText extends Panel {
    /** just a test code now ... */
    public static void main(String[] args) {
        Frame f=new Frame("FastText");
        FastText t=new FastText();
        f.add(t);
        f.setSize(600,400);
        f.show();

        try {
            Thread.sleep(1000);
        } catch(Exception e) {}
        
        TestThread tt = new TestThread(t);
        tt.start();
    }

    /*========= attributes */
    
    final int repaintDelay = 300;
    
    boolean repaintPending;
    
    Line text, lastLine, topLine;
    int lines;
    
    int last_x, last_y;
    int offset_x, offset_y;
    int canvas_width, canvas_height;
    
    Scrollbar vbar, hbar;
    Canvas canvas;
    
    Dimension lastSize;
    
    /*=========== methods */
    
    public FastText() {
        canvas = (Canvas) new BlankCanvas();
        hbar = new Scrollbar(Scrollbar.HORIZONTAL);
        vbar = new Scrollbar(Scrollbar.VERTICAL);
        this.setLayout(new BorderLayout(0, 0));
        this.add("Center", canvas);
        this.add("South", hbar);
        this.add("East", vbar);
        
        text=new Line(null, "Hello, World!");
        new Line(text, "Just a test");

        topLine=text;
        
        updateCachedLines();
        lastSize=getSize();

        Refresher r=new Refresher(this);
        r.start();
    }
    
    public void updateCachedLines() {
        lastLine=text;
        lines=0;
        while (lastLine!=null && lastLine.next!=null) { lastLine=lastLine.next; lines++; }
    }
    
    long lastRepaint=0;

    public synchronized void asyncRepaint() {
        if (System.currentTimeMillis()-lastRepaint>repaintDelay) {
            System.out.println("attempted repaint");
            this.update(canvas.getGraphics());
            System.out.println("returned from attempt");
            lastRepaint=System.currentTimeMillis();
        } else repaintPending=true;
    }
    
    public synchronized void addLine(String s) {
        lastLine=new Line(lastLine, s);
        lines++;
        if (lines>20) topLine=topLine.next;
        asyncRepaint();
    }
    
    public synchronized void reshape(int x, int y, int width, int height) {
        // do the real stuff
        super.reshape(x, y, width, height);
        // Update our scrollbar page size
        Dimension hbar_size = hbar.size();
        Dimension vbar_size = vbar.size();
        canvas_width = width - vbar_size.width;
        canvas_height = height - hbar_size.height;
        hbar.setValues(offset_x, canvas_width, 0, 1000-canvas_width);
        vbar.setValues(offset_y, canvas_height, 0, 1000-canvas_height);
        hbar.setPageIncrement(canvas_width/2);
        vbar.setPageIncrement(canvas_height/2);
        this.update(canvas.getGraphics());
    }

    /*
    public void update(Graphics g) {
        super.update(g);
        if (g==null) { System.out.println("null-graphics update"); return; };
        System.out.println("update");
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
    } */
    
    public boolean handleEvent(Event e) {
        if (e.target == hbar) {
            switch(e.id) {
                case Event.SCROLL_LINE_UP:  
                case Event.SCROLL_LINE_DOWN: 
                case Event.SCROLL_PAGE_UP:  
                case Event.SCROLL_PAGE_DOWN: 
                case Event.SCROLL_ABSOLUTE:  
                    offset_x = ((Integer)e.arg).intValue();    break;
            }
            //this.update(canvas.getGraphics());
            return true;
        }
        else if (e.target == vbar) {
            switch(e.id) {
                case Event.SCROLL_LINE_UP:  
                case Event.SCROLL_PAGE_UP:  
                case Event.SCROLL_LINE_DOWN: 
                case Event.SCROLL_PAGE_DOWN: 
                case Event.SCROLL_ABSOLUTE:  
                    offset_y = ((Integer)e.arg).intValue();    break;
            }
            //this.update(canvas.getGraphics());
            return true;
        }
        
        // If we didn't handle it above, pass it on to the superclass
        // handleEvent routine, which will check its type and call
        // the mouseDown(), mouseDrag(), and other methods.
        return super.handleEvent(e);
    }
    
    public void paint(Graphics g) {
        System.out.println("repaint");
        repaintPending=false;

        Dimension size = getSize();
        Line l=topLine;
        g.setColor(Color.BLACK);
        int y=10;
        while (l!=null) {
            g.drawString(l.cont, 10,y);
            y+=16;
            l=l.next;
        }
    }
}