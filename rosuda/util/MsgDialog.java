package org.rosuda.util;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** Implements a simple modal message dialog with an OK button.
    @version $Id$
*/
public class MsgDialog extends Dialog implements ActionListener
{
    public String lastResult;
    public static final String[] okCancel = {"OK","Cancel"};
    public static final String[] yesNoCancel = {"Yes","No","Cancel"};
    public static final String[] yesNo = {"Yes","No"};

    // this is taken from org.rosuda.ibase.toolkit but we want to be independent
    public class SpacingPanel extends Panel {
        int spacex, spacey;
        SpacingPanel() { spacex=spacey=15; };
        SpacingPanel(int sz) { spacex=spacey=sz; };
        SpacingPanel(int x, int y) { spacex=x; spacey=y; };
        public Dimension getMinimumSize() { return new Dimension(spacex,spacey); }
        public Dimension getMaximumSize() { return new Dimension(spacex,spacey); }
        public Dimension getPreferredSize() { return new Dimension(spacex,spacey); }
    }

    /*
     ZLabel 1.01 (for Java 1.1)   97/4/4
     This class handles a multi-line label.
     Copyright (c) 1997, Andre Pinheiro (adlp@camoes.rnl.ist.utl.pt)
     All rights reserved.

     Modified for JDK 1.1+ by Simon Urbanek
     
     Published by JavaZine - Your Java webzine
     Links to JavaZine's websites
     - http://camoes.rnl.ist.utl.pt/~adlp/JavaZine/Links/JavaZine.html

     Permission to use, copy, modify, and distribute this software
     and its documentation for NON-COMMERCIAL or COMMERCIAL purposes
     and without fee is hereby granted provided that the copyright
     and "published by" messages above appear in all copies.
     We will not be held responsible for any unwanted effects due to
     the usage of this software or any derivative.
     No warrantees for usability for any specific application are
     given or implied.
     */


    public class ZLabel extends Canvas
    {
        public static final int
        // alignment constants
        LEFT = 0,
        CENTER = 1,
        RIGHT = 2;
        protected String[] lines; // lines of text to display
        protected int
            numLines,     // number of lines
            marginWidth,  // left and right margins
            marginHeight, // top and bottom margins
            lineHeight,   // total height of the font
            lineAscent,   // font height above baseline
            maxWidth,     // width of the widest line
            alignment = LEFT; // alignment of the text
        protected int [] lineWidths; // how wide each line is


        // break a label up into an array of lines
        protected void newLabel(String label)
        {
            StringTokenizer t = new StringTokenizer(label, "\n");
            numLines = t.countTokens();
            lines = new String[numLines];
            lineWidths = new int[numLines];

            for(int i = 0; i < numLines; i++)
                lines[i] = t.nextToken();
        }


        /* figure out how the font is, and how wide each line of the label is
            and how wide the widest line is */
        protected void measure()
        {
            FontMetrics fm = this.getFontMetrics(this.getFont());

            if (fm == null) // don't have font metrics yet
                return;

            lineHeight = fm.getHeight();
            lineAscent = fm.getAscent();
            maxWidth = 0;

            for(int i = 0; i < numLines; i++)
            {
                lineWidths[i] = fm.stringWidth(lines[i]);
                if (lineWidths[i] > maxWidth)
                    maxWidth = lineWidths[i];
            }
        }


        // there are four versions of the constructor

        // break the label up into separate lines, and save the other info
        public ZLabel(String label, int marginWidth, int marginHeight, int alignment)
        {
            newLabel(label);
            this.marginWidth = marginWidth;
            this.marginHeight = marginHeight;
            this.alignment = alignment;
        }


        public ZLabel(String label, int marginWidth, int marginHeight)
        {
            this(label, marginWidth, marginHeight, LEFT);
        }


        public ZLabel(String label, int alignment)
        {
            this(label, 10, 10, alignment);
        }


        public ZLabel(String label)
        {
            this(label, 10, 10, LEFT);
        }


        // methods that set the various attributes of the component

        public void setLabel(String label)
        {
            newLabel(label);
            measure();
            repaint();
        }


        public void setFont(Font f)
        {
            super.setFont(f);
            measure();
            repaint();
        }


        public void setForeground(Color c)
        {
            super.setForeground(c);
            repaint();
        }


        public void setAlignment(int a)
        {
            alignment = a;
            repaint();
        }


        public void setMarginWidth(int mw)
        {
            marginWidth = mw;
            repaint();
        }


        public void setMarginHeight(int mh)
        {
            marginHeight = mh; repaint();
        }


        // methods that get the various attributes of the component

        public int getAlignment()
        {
            return alignment;
        }


        public int getMarginWidth()
        {
            return marginWidth;
        }


        public int getMarginHeight()
        {
            return marginHeight;
        }


        /* this method is invoked after our Canvas is first created
            but before it can actually be displayed
            after we've invoked our superclass's addNotify() method,
            we have font metrics and can successfully call measure()
            to figure out how big the label is */
        public void addNotify()
        {
            super.addNotify();
            measure();
        }


        /* this method is called by a layout manager when it wants to
            know how big we'd like to be */
        public Dimension getPreferredSize()
        {
            return new Dimension(maxWidth + 2 * marginWidth,
                                 numLines * lineHeight + 2 * marginHeight);
        }


        /* this method is called when the layout manager wants to know
            the bare minimum amount of space we need to get by */
        public Dimension getMinimumSize()
        {
            return new Dimension(maxWidth, numLines * lineHeight);
        }


        /* this method draws the label (applets use the same method)
            note that it handles the margins and the alignment, but that
            it doesn't have to worry about the color or font - the
            superclass takes care of setting those in the Graphics object */
        public void paint(Graphics g)
        {
            int x,
            y;
            Dimension d = this.getSize();

            y = lineAscent + (d.height - numLines * lineHeight) / 2;

            for(int i = 0; i < numLines; i++, y += lineHeight)
            {
                switch(alignment)
                {
                    case LEFT:
                        x = marginWidth;
                        break;
                    case RIGHT:
                        x = d.width - marginWidth - lineWidths[i];
                        break;
                    case CENTER:
                    default:
                        x = (d.width - lineWidths[i])/2;
                        break;
                }

                g.drawString(lines[i], x, y);
            }
        }
    }
    
    /** Creates and displays the dialog box
	@param par parent frame (or <code>null</code> if none)
 	@param Cap caption title of the dialog box
	@param Msg message to be displayed */
    public MsgDialog(Frame par, String Cap, String Msg) {
        this(par,Cap,Msg,null);
    }

    public MsgDialog(Frame par, String Cap, String Msg, String[] buts)
    {
	super(par,true);
	setLayout(new BorderLayout());
	setTitle(Cap);
	add(new ZLabel(Msg));
	Panel p=new Panel();	
	p.setLayout(new FlowLayout());
	Button b;
        if (buts==null || buts.length==0) {
            p.add(b=new Button("OK"));
            b.addActionListener(this);
        } else {
            int i=0;
            while (i<buts.length) {
                p.add(b=new Button(buts[i++]));
                b.addActionListener(this);
            }
        }
	add(p,"South");
	add(new SpacingPanel(),"West"); 
	add(new SpacingPanel(),"East");
        add(new SpacingPanel(50,10),"North");
	pack();
	setResizable(false);
	//b.setSize(new Dimension(60,20));
	show();
    };

    public void actionPerformed(ActionEvent e)
    {
        lastResult=e.getActionCommand();
        dispose();
    };
};
