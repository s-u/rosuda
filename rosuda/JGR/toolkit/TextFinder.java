package org.rosuda.JGR.toolkit;

//
//  TextFinder.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.rosuda.ibase.*;

public class TextFinder extends JDialog implements ActionListener, KeyListener {

    private GridBagLayout layout = new GridBagLayout();

    private Dimension screenSize = Common.screenRes;

    private static JTextField keyWordField = new JTextField();
    private JTextComponent searchArea = null;
    private JButton searchButton = new JButton("Find");
    private JButton cancelButton = new JButton("Cancel");

    Highlighter.HighlightPainter highLighter = new FoundHighlighter(JGRPrefs.HighLightColor);

    private static String keyWord = null;
    private static int position = -1;
    private static boolean found = false;

    private static TextFinder last = null;


    /** find a specified text in a JTextComponent
     * @param searchArea where should we search your input
     * @param keyWord and what is much more important what do you want to search*/
    public TextFinder(JTextComponent searchArea, String keyWord) {
        this.setTitle("Find");

        this.searchArea = searchArea;

        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        this.getRootPane().setDefaultButton(searchButton);

        FontTracker.current.add(keyWordField);
        keyWordField.setFont(JGRPrefs.DefaultFont);
        keyWordField.setPreferredSize(new Dimension(300,25));

        this.getContentPane().setLayout(layout);
        this.getContentPane().add(keyWordField,
                                  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(searchButton,
                                  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 180, 2, 5), 0, 0));
        this.getContentPane().add(cancelButton,
                                  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));

        this.setSize(new Dimension(400, 100));
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.setResizable(false);
        keyWordField.addKeyListener(this);
    }

    public void exit() {
        removeHighlights(searchArea);
        setVisible(false);
    }

    public void find() {
        searchArea.requestFocus();
        if (keyWord != null && !keyWord.equals(keyWordField.getText().trim())) {
            position = -1;
            found = false;
        }
        keyWord = keyWordField.getText().trim().toLowerCase();
        if (!keyWord.equals("")) {
            position = searchArea.getText().toLowerCase().indexOf(keyWord, position + 1);
            if (position == -1) {
                if (!found) JOptionPane.showMessageDialog(this, "Not found!",
                                              "Search failure",
                                              JOptionPane.WARNING_MESSAGE);
                else JOptionPane.showMessageDialog(this, "No more results!",
                                              "Search failure",
                                               JOptionPane.WARNING_MESSAGE);

                found = false;
            }
            else {
                highlight(searchArea,position, position + keyWord.length());
                searchArea.select(position,position);
                found = true;
            }

        }
    }


    public void highlight(JTextComponent textComp, int off, int end) {
        removeHighlights(textComp);
        try {
            Highlighter hilite = textComp.getHighlighter();
            hilite.addHighlight(off, end, highLighter);
        } catch (BadLocationException e) {
        }
    }

    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i=0; i<hilites.length; i++) {
            if (hilites[i].getPainter() instanceof FoundHighlighter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    public static TextFinder showFind(JTextComponent searchArea) {
        return showFind(searchArea,0);
    }

    public void showFinder() {
        keyWordField.requestFocus();
        this.setLocation((screenSize.width - 400 )/ 2, (screenSize.height - 100) / 2);
        super.setVisible(true);
        super.toFront();
    }


    public static TextFinder showFind(JTextComponent searchArea,int next) {
        if (last == null) {
            last = new TextFinder(searchArea,null);
            last.showFinder();
        }
        else if (next==0) {
            keyWordField.setText(null);
            keyWord = null;
            position = -1;
            found = false;
            last.showFinder();
        }
        else {
            keyWordField.setText(keyWord);
            last.showFinder();
            last.find();
        }
        return last;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        if (cmd=="cancel") this.exit();
        else if (cmd=="search") this.find();
    }


    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        //if (ke.getKeyCode() == KeyEvent.VK_ENTER) last.find();
    }

    public void keyReleased(KeyEvent ke) {
    }


    class FoundHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
        public FoundHighlighter(Color color) {
            super(color);
        }
    }




}