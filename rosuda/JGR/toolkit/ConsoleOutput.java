package org.rosuda.JGR.toolkit;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

/**
 * @author Markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConsoleOutput extends JTextPane {
	
	public ConsoleOutput() {
        if (FontTracker.current == null) FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        setDocument(new JGRStyledDocument());
    }

    public void append(String str, AttributeSet a) {
        Document doc = getDocument();
            if (doc != null) {
                try {
                    doc.insertString(doc.getLength(), str, a);
                } catch (BadLocationException e) {
                }
            }
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

    public void removeAllFrom(int index) throws BadLocationException {
        this.getDocument().remove(index,this.getDocument().getLength()-index);
    }

    public void setFont(Font f) {
        super.setFont(f);
        try {
            ((StyledDocument) this.getDocument()).setCharacterAttributes(0, this.getText().length(),JGRPrefs.SIZE, false);
        } catch (Exception e) {}
    }
}
