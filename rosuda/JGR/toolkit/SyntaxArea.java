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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

import org.rosuda.JGR.util.*;


public class SyntaxArea extends JTextPane implements CaretListener, DropTargetListener {

    private HighlightPainter ParanthesisHighlightMissing = new HighlightPainter(JGRPrefs.ERRORColor);
    private HighlightPainter ParanthesisHighlight = new HighlightPainter(JGRPrefs.BRACKETHighLight);

    private boolean wrap=true;

    /** SyntaxArea, with highlighting matching brackets
     * @param highlight should we do coloring and highlighting brackets */
    public SyntaxArea() {
        this.setContentType("text/rtf");
        this.setDocument(new SyntaxDocument());
        if (FontTracker.current == null) 
        	FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        this.addCaretListener(this);
    }

    public void append(String str) {
        append(str,null);
    }

    public void append(String str, AttributeSet attr) {
        try {
            Document doc = this.getDocument();
            doc.insertString(doc.getLength(), str, attr);
        } catch (BadLocationException e) {
        }
    }


    public void insertAt(int offset, String str) {
        try {
            Document doc = this.getDocument();
            doc.insertString(offset, str, null);
        } catch (BadLocationException e) {
        }
    }

    public String getText() {
        try {
            Document doc = this.getDocument();
            return doc.getText(0,doc.getLength());
        } catch (BadLocationException e) {
            return null;
        }   
    }

    public void setText(String str) {
        try {
            Document doc = this.getDocument();
            doc.remove(0,doc.getLength());
            doc.insertString(0,str,null);
        } catch (BadLocationException e) {
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


    public void setFont(Font f) {
        super.setFont(f);
        try {
            ((StyledDocument) this.getDocument()).setCharacterAttributes(0, this.getText().length(),JGRPrefs.DEFAULTFONT, false);
        } catch (Exception e) {}
    }


    /** highlight the corresponding brackets (forward)
     * @param par String which bracket
     * @param pos int current position
     * @return index int where the matching bracket is
     */

    public void highlightParanthesisForward(String par, int pos) throws BadLocationException {
        //System.out.println(par);
        int open = pos;
        int cend = this.getText().length();
        //try { System.out.println(pos+" "+getText(pos-1,1)); } catch(Exception e) {}

        String end = null;

        if (par.equals("{")) {
            end = "}";
        }
        if (par.equals("(")) {
            end = ")";
        }
        if (par.equals("[")) {
            end = "]";
        }

        if (end==null) return;

        String cchar = null;

        int pcount = 1;

        int line = this.getLineOfOffset(open);
        int lstart = this.getLineStartOffset(line);
        int lend = this.getLineEndOffset(line);


        while(++pos <= cend) {
            cchar = this.getText(pos - 1, 1);
            if (cchar.matches("\"") && !isEscaped(pos)) {
                boolean found = true;
                int i = pos;
                while(++i <= lend) {
                    found = false;
                    String schar = this.getText(i - 1, 1);
                    //System.out.print(schar);
                    if (schar.equals("\"") && !isEscaped(i)) {
                        pos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) return;
            }
            else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos)) pcount++;
            else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos)) {
                pcount--;
                if (pcount == 0) {
                    if (cchar.equals(end)) {
                        highlight(this, par, open, ParanthesisHighlight);
                        highlight(this, end, pos,
                                  ParanthesisHighlight);
                    }
                    else {
                        highlight(this, par, open,
                                  ParanthesisHighlightMissing);
                        highlight(this, end, pos,
                                  ParanthesisHighlightMissing);
                    }
                    return;
                }
            }
        }
    }

    /** highlight the corresponding brackets (backward)
     * @param par String which bracket
     * @param pos int current position
     * @return index int where the matching bracket is
     */

    public  void highlightParanthesisBackward(String par, int pos) throws BadLocationException{

        int end = pos;

        String open = null;

        if (par.equals("}")) {
            open = "{";
        }
        if (par.equals(")")) {
            open = "(";
        }
        if (par.equals("]")) {
            open = "[";
        }

        if (open==null) return;

        String cchar = null;

        int pcount = 1;

        int line = this.getLineOfOffset(end);
        int lstart = this.getLineStartOffset(line);
        int lend = this.getLineEndOffset(line);


        while(--pos > 0) {
            cchar = this.getText(pos - 1, 1);
            if (cchar.matches("\"") && !isEscaped(pos)) {
                boolean found = true;
                int i = pos;
                while(--i > lstart) {
                    found = false;
                    String schar = this.getText(i - 1, 1);
                    if (schar.equals("\"") && !isEscaped(i)) {
                        pos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) return;
            }
            else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos)) pcount++;
            else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos)) {
                pcount--;
                if (pcount == 0) {
                    if (cchar.equals(open)) {
                        highlight(this, par, end, ParanthesisHighlight);
                        highlight(this, open, pos,
                                  ParanthesisHighlight);
                    }
                    else {
                        highlight(this, par, end,
                                  ParanthesisHighlightMissing);
                        highlight(this, open, pos,
                                  ParanthesisHighlightMissing);
                    }
                    return;
                }
            }
        }
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

    public void removeHighlights() {
        Highlighter hilite = this.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter()instanceof HighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }


    public void caretUpdate(final CaretEvent e) {
        final SyntaxArea sa = this;
        removeHighlights();
        try {
            if (e.getDot()==0) return;
            if (getText(e.getDot()-1,1).matches("[(]|[\\[]|[{]|[)]|[\\]]|[}]")) /*t.start();*/ {

                removeCaretListener(sa);
                String c; int pos;
                try {
                    pos = e.getDot();
                    c = getText(pos-1,1);
                    if (sa.isEscaped(pos)) {
                        addCaretListener(sa);
                        return;
                    }
                }
                catch (Exception ex1) {
                    new ErrorMsg(ex1);
                    addCaretListener(sa);
                    return;
                }
                try {
                    if (c.matches("[(]|[\\[]|[{]"))
                        highlightParanthesisForward(c, pos);
                    else if (c.matches("[)]|[\\]]|[}]"))
                        highlightParanthesisBackward(c, pos);
                }
                catch (Exception ex2) { new ErrorMsg(ex2);}
                addCaretListener(sa);
            }
        } catch (Exception ex3) { new ErrorMsg(ex3);}
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

            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                String s = (String) t.getTransferData(DataFlavor.stringFlavor);
                evt.getDropTargetContext().dropComplete(true);
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