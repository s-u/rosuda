package org.rosuda.JGR.toolkit;

//
//  SyntaxArea.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.text.*;


public class SyntaxArea extends JTextPane implements CaretListener, DropTargetListener {

    private HighlightPainter ParanthesisHighlightMissing = new HighlightPainter(Preferences.ERRORColor);
    private HighlightPainter ParanthesisHighlight = new HighlightPainter(Preferences.BRACKETHighLight);

    private boolean syntaxHighlightOn = true;


    private boolean wrap=true;

    public SyntaxArea() {
        this(true);
    }

    /** SyntaxArea, with highlighting matching brackets
     * @param highlight should we do coloring and highlighting brackets */
    public SyntaxArea(boolean highlight) {
        this.syntaxHighlightOn = highlight;
        if (syntaxHighlightOn) this.setDocument(new RSyntaxDocument());
        else this.setDocument(new RStyledDocument());
        //this.setDocument(new PlainDocument());
        this.setContentType("text/plain");
        if (FontTracker.current == null)
            FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        this.addCaretListener(this);
        //new DropTarget(this,this);
    }

    public void append(String str) {
        append(str,null);
    }

    public void append(String str, AttributeSet attr) {
        try {RSyntaxDocument doc = (RSyntaxDocument) getDocument();
            if (doc != null) {
                try {
                    doc.insertString(doc.getLength(),str,attr);
                } catch (BadLocationException e) {
                }
            }
        }
        catch (ClassCastException ex) {
            Document doc = getDocument();
            if (doc != null) {
                try {
                    doc.insertString(doc.getLength(), str, attr);
                } catch (BadLocationException e) {
                }
            }
        }
    }


    public void insertAt(int offset, String str) {
        new JTextArea();
        try {RSyntaxDocument doc = (RSyntaxDocument) getDocument();
            if (doc != null) {
                try {
                    doc.insertString(offset,str,null);
                } catch (BadLocationException e) {
                }
            }
        }
        catch (ClassCastException ex) {
            Document doc = getDocument();
            if (doc != null) {
                try {
                    doc.insertString(offset, str, null);
                } catch (BadLocationException e) {
                }
            }
        }
    }

    public void cut() {
        this.removeCaretListener(this);
        super.cut();
        this.addCaretListener(this);
    }

    public void copy() {
        this.removeCaretListener(this);
        super.copy();
        this.addCaretListener(this);
    }

    public void paste() {
        this.removeCaretListener(this);
        super.paste();
        this.addCaretListener(this);
    }


    /*public void setTextWhileLoading(String str) {
        try { RSyntaxDocument doc = (RSyntaxDocument) getDocument();
            if (doc != null) {
                try {
                    doc.insertPage(str);
                } catch (BadLocationException e) {
                }
            }
        }
        catch (ClassCastException ex) {
            try { RStyledDocument doc = (RStyledDocument) getDocument();
                if (doc != null) {
                    try {
                        doc.insertString(doc.getLength(), str, null);
                    } catch (BadLocationException e) {
                    }
                }
            }
            catch (ClassCastException ex2) {
                super.setText(str);
            }
        }
    }*/


    public int getLineCount() {
        Element map = getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    public int getLineStartOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", getDocument().getLength()+1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    public int getLineEndOffset(int line) throws BadLocationException {
        int lineCount = getLineCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line >= lineCount) {
            throw new BadLocationException("No such line", getDocument().getLength()+1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            Element lineElem = map.getElement(line);
            int endOffset = lineElem.getEndOffset();
            // hide the implicit break at the end of the document
            return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
        }
    }


    public int getLineOfOffset(int offset) throws BadLocationException {
        Document doc = getDocument();
        if (offset < 0) {
            throw new BadLocationException("Can't translate offset to line", -1);
        } else if (offset > doc.getLength()) {
            throw new BadLocationException("Can't translate offset to line",
                                           doc.getLength() + 1);
        } else {
            Element map = getDocument().getDefaultRootElement();
            return map.getElementIndex(offset);
        }
    }

    public void setWordWrap(boolean wrap) {
        this.wrap=wrap;
    }

    public boolean getWordWrap() {
        return this.wrap;
    }

    public boolean getScrollableTracksViewportWidth() {
        if (!wrap)
        {
            Component parent=this.getParent();
            ComponentUI ui=this.getUI();
            int uiWidth=ui.getPreferredSize(this).width;
            int parentWidth=parent.getSize().width;
            boolean bool= (parent !=null)
                ? (ui.getPreferredSize(this).width < parent.getSize().width)
                : true;

            return bool;
        }
        else return super.getScrollableTracksViewportWidth();
    }

    public void setBounds(int x, int y, int width, int height) {
        if (wrap)
            super.setBounds(x, y, width, height);
        else
        {
            Dimension size = this.getPreferredSize();
            super.setBounds(x,y,Math.max(size.width, width),Math.max(size.height, height));
        }
    }



    public void setSyntaxHighlightEnabled(boolean on) {
        this.syntaxHighlightOn = on;
        if (syntaxHighlightOn) this.setDocument(new RSyntaxDocument());
        else this.setDocument(new RStyledDocument());
    }

    public boolean isEscaped(int pos) {
        boolean escaped = false;
        try {
            escaped = lastChar(pos - 1, "\\");
        }
        catch (Exception e) {
            escaped = false;
        }

        return escaped;
    }

    public boolean lastChar(int pos, String cont) {
        if (pos == 0) {
            return false;
        }
        try {
            if (this.getText(pos - 1, 1).equals(cont)) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (BadLocationException e) {}
        return false;
    }


    /** highlight the corresponding brackets (forward)
     * @param par String which bracket
     * @param pos int current position
     * @return index int where the matching bracket is
     */

    public int highlightParanthesisForward(String par, int pos) {
        String end = "";
        if (par.equals("{")) {
            end = "}";
        }
        if (par.equals("(")) {
            end = ")";
        }
        if (par.equals("[")) {
            end = "]";
        }
        int index = pos;
        int indexend = -1;
        int len = this.getText().length();
        try {
            while (index <= len) {
                String s = this.getText(index, 1);
                if (s.equals("\"")) {
                    index = endQuote(index);
                    if (index == -1) {
                        break;
                    }
                }
                else if (!isEscaped(index + 1)) {
                    if (s.equals("{") || s.equals("[") || s.equals("(")) {
                        index = highlightParanthesisForward(s, index + 1);
                        if (index == -1) {
                            break;
                        }
                    }
                    else if (s.equals("}") || s.equals("]") || s.equals(")")) {
                        indexend = index;
                        if (pos == this.getCaretPosition()) {
                            if (s.equals(end)) {
                                highlight(this, par, pos, ParanthesisHighlight);
                                highlight(this, end, indexend + 1,
                                          ParanthesisHighlight);
                            }
                            else {
                                highlight(this, par, pos,
                                          ParanthesisHighlightMissing);
                                highlight(this, end, indexend + 1,
                                          ParanthesisHighlightMissing);
                            }
                        }
                        break;
                    }
                }
                else {
                    index++;
                }
                index++;
            }
        }
        catch (Exception e) {
            new iError(e);
        }
        return indexend;
    }

    /** highlight the corresponding brackets (backward)
     * @param par String which bracket
     * @param pos int current position
     * @return index int where the matching bracket is
     */

    public int highlightParanthesisBackward(String par, int pos) {
        String open = "";
        if (par.equals("}")) {
            open = "{";
        }
        if (par.equals(")")) {
            open = "(";
        }
        if (par.equals("]")) {
            open = "[";
        }
        int index = pos - 2;
        int indexopen = -1;
        try {
            while (index >= 0) {
                String s = this.getText(index, 1);
                if (s.equals("\"")) {
                    index = openQuote(index);
                    if (index == -1) {
                        break;
                    }
                }
                else if (!isEscaped(index) || index == 0) {
                    if (s.equals("}") || s.equals("]") || s.equals(")")) {
                        index = highlightParanthesisBackward(s, index + 1);
                        if (index == -1) {
                            break;
                        }
                    }
                    if (s.equals("{") || s.equals("[") || s.equals("(")) {
                        indexopen = index;
                        if (pos == this.getCaretPosition()) {
                            if (s.equals(open)) {
                                highlight(this, par, pos, ParanthesisHighlight);
                                highlight(this, open, indexopen + 1,
                                          ParanthesisHighlight);
                            }
                            else {
                                highlight(this, par, pos,
                                          ParanthesisHighlightMissing);
                                highlight(this, open, indexopen + 1,
                                          ParanthesisHighlightMissing);
                            }
                        }
                        break;
                    }
                }
                else {
                    index--;
                }
                index--;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return indexopen;
    }

    public int openQuote(int index) {
        index--;
        int open = -1;
        int i = -1;
        int linebegin = this.getText().lastIndexOf("\n", index + 1);
        linebegin = linebegin == -1 ? 0 : linebegin;
        try {
            while ( (i = this.getText().lastIndexOf("\"", index)) != -1 &&
                   i > linebegin) {
                if (!isEscaped(i + 1)) {
                    open = i;
                    break;
                }
                index = i;
            }
        }
        catch (Exception e) {
            new iError(e);
        }
        return open;
    }

    public int endQuote(int index) {
        index++;
        int end = -1;
        int i = -1;
        int lineend = this.getText().indexOf("\n", index);
        lineend = lineend == -1 ? this.getText().length() : lineend;
        try {
            while ( (i = this.getText().indexOf("\"", index)) != -1 &&
                   i <= lineend) {
                if (!isEscaped(i + 1)) {
                    end = i;
                    break;
                }
                index = i + 1;
            }
        }
        catch (Exception e) {
            new iError(e);
        }
        return end;
    }

    public String getCurrentWord() {
        String word = null;
        String text = this.getText();
        int pos = this.getCaretPosition();
        if (pos < 0) return null;
        int offset = pos-1, end = pos; pos--;
        int l = text.length();
        while (offset > -1 && pos > -1) {
            char c = text.charAt(pos);
            if (((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z'))||c=='.') offset--;
            else break;
            pos--;
        }
        offset = offset==-1?0:++offset;
        pos = end;
        while (end < l && pos < l) {
            char c = text.charAt(pos);
            if (((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z'))||c=='.') end++;
            else break;
            pos++;
        }
        end = end==-1?l:end;
        return (offset!=end)?text.substring(offset,end).trim():null;
    }

    public String getLastPart() {
        String word = "";
        int pos = this.getCaretPosition();
        String text = null;
        try { text = this.getText(0,pos); } catch (Exception e) {return null;}
        return text;
    }

    public void highlight(JTextComponent textComp, String pattern, int pos,
                          HighlightPainter hipainter) {
        try {
            Highlighter hilite = textComp.getHighlighter();
            if (pos == 0) {
                pos++;
            }
            hilite.addHighlight(pos - 1, pos, hipainter);
        }
        catch (BadLocationException e) {
        }
    }

    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter()instanceof HighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    public void caretUpdate(CaretEvent e) {
        if (syntaxHighlightOn) {
            try {
            removeHighlights(this);
            final Document doc = getDocument();
            final int pos = this.getCaretPosition();
            final String currentStr = this.getText(pos - 1, 1);
            /*SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                        try {
                        int length = doc.getLength();
                        if (!isEscaped(pos)) {
                            if (lastChar(pos, "{") || lastChar(pos, "(") || lastChar(pos, "[")) {
                                    highlightParanthesisForward(currentStr,pos);
                            }
                            if (lastChar(pos, "}") || lastChar(pos, ")") || lastChar(pos, "]")) {
                                highlightParanthesisBackward(currentStr,pos);
                            }
                        }
                        }
                        catch (Exception ex1) { ex1.printStackTrace();}
                }
            });*/
            } catch (Exception ex) {}
        }
    }

    public void dragEnter(DropTargetDragEvent evt) {
    // Called when the user is dragging and enters this drop target.
    }

    public void dragOver(DropTargetDragEvent evt) {
    // Called when the user is dragging and moves over this drop target.
    }

    public void dragExit(DropTargetEvent evt) {
    // Called when the user is dragging and leaves this drop target.
    }

    public void dropActionChanged(DropTargetDragEvent evt) {
    // Called when the user changes the drag action between copy or move.
    }

    public void drop(DropTargetDropEvent evt) {
        try {
            Transferable t = evt.getTransferable();

            System.out.println(t);

            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String s = (String) t.getTransferData(DataFlavor.stringFlavor);
                evt.getDropTargetContext().dropComplete(true);
                System.out.println(s);
            }
            else {
                evt.rejectDrop();
            }
        }
        catch (Exception e) {
            evt.rejectDrop();
        }
    }


    class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public HighlightPainter(Color color) {
            super(color);
        }
    }
}