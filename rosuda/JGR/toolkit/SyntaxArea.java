package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

import org.rosuda.JGR.util.*;

/**
 *  SyntaxArea - extends JTextPane and provides bracketmatching. 
 *
 *	@author Markus Helbig
 *
 * 	RoSuDa 2003 - 2004
 */

public class SyntaxArea extends JTextPane implements CaretListener, DropTargetListener {

    private HighlightPainter ParanthesisHighlightMissing = new HighlightPainter(JGRPrefs.ERRORColor);
    private HighlightPainter ParanthesisHighlight = new HighlightPainter(JGRPrefs.BRACKETHighLight);

    private boolean wrap=true;

    /** SyntaxArea, with highlighting matching brackets */
    public SyntaxArea() {
        this.setContentType("text/rtf");
        this.setDocument(new SyntaxDocument());
        if (FontTracker.current == null)
            FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        this.addCaretListener(this);
        this.setTransferHandler(new TextTransferHandler());
        this.setDragEnabled(true);
    }

    /**
     * Append text.
     */
    public void append(String str) {
        append(str,null);
    }

    /**
     * Append text with supplied attributeset.
     */
    public void append(String str, AttributeSet attr) {
        try {
            Document doc = this.getDocument();
            doc.insertString(doc.getLength(), str, attr);
        } catch (BadLocationException e) {
        }
    }

    /**
     * Insert text at position. 
     */
    public void insertAt(int offset, String str) {
        try {
            Document doc = this.getDocument();
            doc.insertString(offset, str, null);
        } catch (BadLocationException e) {
        }
    }

    /**
     * Get text.
     */
    public String getText() {
        try {
            Document doc = this.getDocument();
            return doc.getText(0,doc.getLength());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Get text from offset with supplied length.
     */
    public String getText(int offs, int len) {
		try {
            Document doc = this.getDocument();
			try {
				return doc.getText(offs,len+1);
			} catch (Exception e) {
				return doc.getText(offs,len);
			}
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Set text.
     */
    public void setText(String str) {
        try {
            Document doc = this.getDocument();
            doc.remove(0,doc.getLength());
            doc.insertString(0,str,null);
        } catch (BadLocationException e) {
        }
    }

    /**
     * Cut text.
     */
    public void cut() {
        this.removeCaretListener(this);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents((Transferable) new StringSelection(this.getSelectedText()),null);
        this.replaceSelection("");
        this.addCaretListener(this);
    }

    /**
     * Copy text.
     */
    public void copy() {
		this.removeCaretListener(this);
		super.copy();
        this.addCaretListener(this);
    }
	
    /**
     * Paste from clipboard.
     */
    public void paste() {
        this.removeCaretListener(this);
        try {
            SyntaxDocument doc = (SyntaxDocument) this.getDocument();
            if (isEditable() && isEnabled()) {
                int s = this.getSelectionStart();
                int e = this.getSelectionEnd();
				
                if (s != -1 && e != -1)
                    doc.remove(s,e-s);
				doc.insertStringWithoutWhiteSpace(this.getCaretPosition(),Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this).getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor).toString(),null);
			}
        } catch (Exception e) {
        }
        this.addCaretListener(this);
    }

    /**
     * Get amount of lines.
     */
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

    /**
     * Set word wrap behavior.
     * @param wrap true if wrap, false if not
     */
    public void setWordWrap(boolean wrap) {
        this.wrap=wrap;
    }

    /**
     * Set word wrap behavior.
     * @return true if wrap, false if not
     */
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

    /**
     * Checks wether character is escaped.
     * @param pos postion where to check
     * @return true if escaped, false if not
     */
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

    /**
     * Check wether last character matches cont.
     * @param pos postion
     * @param cont pattern
     * @return true if matches, fals if not
     */
    public boolean lastChar(int pos, String cont) {
        if (pos == 0) {
            return false;
        }
        if (this.getText(pos - 1, 1) != null && this.getText(pos - 1, 1).equals(cont)) {
            return true;
        }
        else {
            return false;
        }
    }

    /** 
     * Highlight the corresponding brackets (forward).
     * @param par String which bracket
     * @param pos int current position
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

    /** 
     * Highlight the corresponding brackets (backward).
     * @param par String which bracket
     * @param pos int current position
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

    /**
     * Highlight pattern at position.
     * @param textComp textcomponent
     * @param pattern pattern
     * @param pos position
     * @param hipainter highlightpainter
     */
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

    /**
     * Remove current highlights.
     */
    public void removeHighlights() {
        Highlighter hilite = this.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter()instanceof HighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    /**
     * caretUpdate: handle caret event: if it was a bracket, highlight the matching one if there is one.
     */
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

    /**
     * dragEnter: handle drag event.
     */
    public void dragEnter(DropTargetDragEvent evt) {
        //System.out.println("dragEnter");
        // Called when the user is dragging and enters this drop target.
    }

    /**
     * dragOver: handle drag event.
     */
    public void dragOver(DropTargetDragEvent evt) {
        //System.out.println("dragOver");
        // Called when the user is dragging and moves over this drop target.
    }

    /**
     * dragExit: handle drag event.
     */
    public void dragExit(DropTargetEvent evt) {
        //System.out.println("dragExit");
        // Called when the user is dragging and leaves this drop target.
    }

    /**
     * dropActionChanged: handle drop event.
     */
    public void dropActionChanged(DropTargetDragEvent evt) {
        //System.out.println("dropActionChanged");
        // Called when the user changes the drag action between copy or move.
    }

    /**
     * drop: handle drop target event: insert string representation of dragged object.
     */
    public void drop(DropTargetDropEvent evt) {
        //System.out.println("drop");
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
