package org.rosuda.JGR.toolkit;

/**
 *  TextFinder
 * 
 *  find specified pattern in attached textcomponent
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.rosuda.ibase.*;

public class TextFinder extends JDialog implements ActionListener, KeyListener {

    private GridBagLayout layout = new GridBagLayout();

    private Dimension screenSize = Common.screenRes;

    private JTextField keyWordField = new JTextField();
    private JTextComponent searchArea = null;
    private JButton searchButton = new JButton("Find");
    private JButton cancelButton = new JButton("Cancel");

    Highlighter.HighlightPainter highLighter = new FoundHighlighter(JGRPrefs.HighLightColor);

    private String keyWord = null;
    private int position = -1;
    private boolean found = false;

    private TextFinder last = null;

    public TextFinder() {
        this(null);
    }

    /** find a specified text in a JTextComponent
     * @param searchArea where should we search your input*/
    public TextFinder(JTextComponent searchArea) {
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

    private void exit() {
        removeHighlights(searchArea);
        setVisible(false);
    }
    
    public void setSearchArea(JTextComponent comp) {
        this.searchArea = comp;
    }

    private void find() {
        if (searchArea == null) return;
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


    private void highlight(JTextComponent textComp, int off, int end) {
        removeHighlights(textComp);
        try {
            Highlighter hilite = textComp.getHighlighter();
            hilite.addHighlight(off, end, highLighter);
        } catch (BadLocationException e) {
        }
    }

    private void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i=0; i<hilites.length; i++) {
            if (hilites[i].getPainter() instanceof FoundHighlighter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    private void showFinder() {
        keyWordField.requestFocus();
        this.setLocation((screenSize.width - 400 )/ 2, (screenSize.height - 100) / 2);
        super.setVisible(true);
        super.toFront();
    }


    public TextFinder showFind(boolean next) {
        if (!next) {
            keyWordField.setText(null);
            keyWord = null;
            position = -1;
            found = false;
            showFinder();
        }
        else {
            keyWordField.setText(keyWord);
            showFinder();
            find();
        }
        return last;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="cancel") this.exit();
        else if (cmd=="search") this.find();
    }


    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }


    class FoundHighlighter extends DefaultHighlighter.DefaultHighlightPainter {
        public FoundHighlighter(Color color) {
            super(color);
        }
    }




}