package org.rosuda.JGR.toolkit;

//
//  RSyntaxDocument.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import javax.swing.text.*;

import org.rosuda.JGR.*;

public class SyntaxDocument extends JGRStyledDocument {

    private DefaultStyledDocument doc;
    private Element rootElement;
    private boolean multiLineComment;
    
    private static int startLine = 0;

    private static MutableAttributeSet BOLD = new SimpleAttributeSet();


    /** we're doing a lot here, but it is enough to know that we are coloring the text*/
    public SyntaxDocument() {
        doc = this;
        rootElement = doc.getDefaultRootElement();

        StyleConstants.setBold(BOLD, true);

        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }

    /*
         * Override to apply syntax highlighting after the document has been updated
     */
    public void insertString(final int offset, String str, AttributeSet a) throws
        BadLocationException {
        boolean whitespace = false;
        if (str.equals("\t")) {
            try {
                whitespace = getText(offset - 1, 1).matches("[\\s|#]");
            }
            catch (Exception e) {}
            if (!whitespace && offset != 0)
                str = str.replaceAll("\t", "");
        }
        else if (str.equals("\n")) {
            int line = rootElement.getElementIndex(offset);
                int off = rootElement.getElement(line).getStartOffset();
                int i = off;
                try {
                    while (getText(i++,1).matches("[\\s]"));
                } catch (Exception ex) {}
                try {
                    str ="\n"+ getText(off,i-off-1).replaceAll("\n","");
                }
                catch (Exception ex2) {}
        }
        super.insertString(offset, str, a);
        int len = str.length();
        processChangedLines(offset, len);
    }
    
    public void insertStringWithoutWhiteSpace(final int offset, String str, AttributeSet a) throws BadLocationException {
    	super.insertString(offset, str, a);
    	int len = str.length();
    	processChangedLines(offset, len);
    }    


    /*
         * Override to apply syntax highlighting after the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {
        if (offset==-1) return;
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }

    /*
     * Determine how many lines have been changed,
     * then apply highlighting to each line
     */
    public synchronized void processChangedLines(int offset, int length) throws
        BadLocationException {
        String content = doc.getText(0, doc.getLength());
        // The lines affected by the latest document update
        int startLine = rootElement.getElementIndex(offset);
        int endLine = rootElement.getElementIndex(offset + length);
        // Do the actual highlighting
         for (int i = startLine; i <= endLine; i++)
             applyHighlighting(content, i);
    }



    /*
     * Parse the line to determine the appropriate highlighting
     */
    private synchronized void applyHighlighting(String content, int line) throws
        BadLocationException {
        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;
        int lineLength = endOffset - startOffset;
        if (lineLength < 0) lineLength = 0;
        int contentLength = content.length();
        if (endOffset >= contentLength)
            endOffset = contentLength - 1;

        // set normal attributes for the line
        doc.setCharacterAttributes(startOffset, lineLength, JGRPrefs.NORMAL, true);
        // check for single line comment
        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ( (index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1,
                                       JGRPrefs.COMMENT, false);
            endOffset = index - 1;
        }
        // check for tokens*/
        checkForTokens(content, startOffset, endOffset);
    }

    /*
     * Parse the line for tokens to highlight
     */
    private synchronized void checkForTokens(String content, int startOffset, int endOffset) {
        while (startOffset <= endOffset) {
            // skip the delimiters to find the start of a new token
            while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
                if (startOffset < endOffset)
                    startOffset++;
                else
                    return;
            }
            // Extract and process the entire token
            if (isQuoteDelimiter(content.substring(startOffset, startOffset + 1)))
                startOffset = getQuoteToken(content, startOffset, endOffset);
            else
                startOffset = getOtherToken(content, startOffset, endOffset);
        }
    }

    /*
     *
     */
    private synchronized int getQuoteToken(String content, int startOffset, int endOffset) {
        String quoteDelimiter = content.substring(startOffset, startOffset + 1);
        String escapeString = getEscapeString(quoteDelimiter);
        int index;
        int endOfQuote = startOffset;
        // skip over the escape quotes in this quote
        index = content.indexOf(escapeString, endOfQuote + 1);
        while ( (index > -1) && (index < endOffset)) {
            endOfQuote = index + 1;
            index = content.indexOf(escapeString, endOfQuote);
        }
        // now find the matching delimiter
        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
        if ( (index < 0) || (index > endOffset))
            endOfQuote = endOffset;
        else
            endOfQuote = index;
        doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1,
                                   JGRPrefs.QUOTE, false);
        return endOfQuote + 1;
    }

    private synchronized int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1)))
                break;
            endOfToken++;
        }
        String token = content.substring(startOffset, endOfToken);
        if (isKeyword(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       JGRPrefs.KEYWORD, false);
        else if (isObject(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       JGRPrefs.OBJECT, false);
        else if (isNumber(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       JGRPrefs.NUMBER, false);
        return endOfToken + 1;
    }

    /*
     * Assume the needle will the found at the start/end of the line
     */
    private synchronized int indexOf(String content, String needle, int offset) {
        int index;
        while ( (index = content.indexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();
            if (text.startsWith(needle) || text.endsWith(needle))
                break;
            else
                offset = index + 1;
        }
        return index;
    }

    /*
     * Assume the needle will the found at the start/end of the line
     */
    private synchronized int lastIndexOf(String content, String needle, int offset) {
        int index;
        while ( (index = content.lastIndexOf(needle, offset)) != -1) {
            String text = getLine(content, index).trim();
            if (text.startsWith(needle) || text.endsWith(needle))
                break;
            else
                offset = index - 1;
        }
        return index;
    }

    private String getLine(String content, int offset) {
        int line = rootElement.getElementIndex(offset);
        Element lineElement = rootElement.getElement(line);
        int start = lineElement.getStartOffset();
        int end = lineElement.getEndOffset();
        return content.substring(start, end - 1);
    }

    /*
     * Override for other languages
     */
    protected synchronized boolean isDelimiter(String character) {
        String operands = ",;:{}()[]+-/%<=>!&|^~*$";
        if (Character.isWhitespace(character.charAt(0)) ||
            operands.indexOf(character) != -1)
            return true;
        else
            return false;
    }

    /*
     * Override for other languages
     */
    protected synchronized boolean isQuoteDelimiter(String character) {
        String quoteDelimiters = "\"'";
        if (quoteDelimiters.indexOf(character) < 0)
            return false;
        else
            return true;
    }

    /*
     * Override for other languages
     */
    protected synchronized boolean isKeyword(String token) {
        Object o = JGR.KEYWORDS.get(token);
        return o == null ? false : true;
    }
    /*
     * Override for other languages
     */
    protected synchronized boolean isNumber(String token) {
        return token.matches("[[0-9]+.[0-9]+]*[0-9]+");
    }

    protected synchronized boolean isObject(String token) {
        Object o = JGR.KEYWORDS_OBJECTS.get(token);
        return o == null ? false : true;
    }

    /*
     * Override for other languages
     */

    protected synchronized String getSingleLineDelimiter() {
        return "#";
    }

    /*
     * Override for other languages
     */
    protected synchronized String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

    /*
     *
     */
    protected synchronized String addMatchingBrace(int offset) throws BadLocationException {
        StringBuffer whiteSpace = new StringBuffer();
        int line = rootElement.getElementIndex(offset);
        int i = rootElement.getElement(line).getStartOffset();
        while (true) {
            String temp = doc.getText(i, 1);
            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            } else
                break;
        }
        return "{\n" + whiteSpace.toString() + whiteSpace.toString() + "\n" +
            whiteSpace.toString() + "}";
    }
}
