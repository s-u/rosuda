package org.rosuda.JGR.toolkit;

//
//  RSyntaxDocument.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class RSyntaxDocument extends RStyledDocument {

    private DefaultStyledDocument doc;
    private Element rootElement;
    private boolean multiLineComment;
    private Hashtable keywords = Preferences.KEYWORDS;
    private Hashtable objects = Preferences.KEYWORDSOBJECTS;

    private static int startLine = 0;


    /** we're doing a lot here, but it is enough to know that we are coloring the text*/
    public RSyntaxDocument() {
        doc = this;
        rootElement = doc.getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }

    public void insertPage(final String str) throws BadLocationException {
        super.insertString(0,str,null);
    }

    /*
         * Override to apply syntax highlighting after the document has been updated
     */
    public void insertString(final int offset, String str, AttributeSet a) throws
        BadLocationException {
        //if (str.equals("{")) str = addMatchingBrace(offset);
        boolean whitespace = false;
        try { whitespace = getText(offset-1,1).matches("[\\s|#]"); } catch (Exception e) {}
        if (!whitespace && offset != 0) str = str.replaceAll("\t","");
        if (str.matches("_")) str = "<-";
        super.insertString(offset, str, a);
        final int len = str.length();
        SwingUtilities.invokeLater(new Runnable() {public void run() { try { processChangedLines(offset, len);} catch (Exception e) {new iError(e);}}});
    }

    public void insertWithoutHighlight(final int offset, final String str, AttributeSet a) throws
        BadLocationException {
        super.insertString(offset, str, a);
    }


    /*
         * Override to apply syntax highlighting after the document has been updated
     */
    public void remove(final int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        SwingUtilities.invokeLater(new Runnable() {public void run() { try { processChangedLines(offset, 0);} catch (Exception e) {new iError(e);}}});
    }

    /*
     * Determine how many lines have been changed,
     * then apply highlighting to each line
     */
    public void processChangedLines(int offset, int length) throws
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
    private void applyHighlighting(String content, int line) throws
        BadLocationException {
        int startOffset = rootElement.getElement(line).getStartOffset();
        int endOffset = rootElement.getElement(line).getEndOffset() - 1;
        int lineLength = endOffset - startOffset;
        if (lineLength < 0) lineLength = 0;
        int contentLength = content.length();
        if (endOffset >= contentLength)
            endOffset = contentLength - 1;

        // set normal attributes for the line
        doc.setCharacterAttributes(startOffset, lineLength, Preferences.NORMAL, true);
        // check for single line comment
        int index = content.indexOf(getSingleLineDelimiter(), startOffset);
        if ( (index > -1) && (index < endOffset)) {
            doc.setCharacterAttributes(index, endOffset - index + 1,
                                       Preferences.COMMENT, false);
            endOffset = index - 1;
        }
        // check for tokens*/
        checkForTokens(content, startOffset, endOffset);
    }

    /*
     * Parse the line for tokens to highlight
     */
    private void checkForTokens(String content, int startOffset, int endOffset) {
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
    private int getQuoteToken(String content, int startOffset, int endOffset) {
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
                                   Preferences.QUOTE, false);
        return endOfQuote + 1;
    }

    private int getOtherToken(String content, int startOffset, int endOffset) {
        int endOfToken = startOffset + 1;
        while (endOfToken <= endOffset) {
            if (isDelimiter(content.substring(endOfToken, endOfToken + 1)))
                break;
            endOfToken++;
        }
        String token = content.substring(startOffset, endOfToken);
        if (isKeyword(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       Preferences.KEYWORD, false);
        else if (isObject(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       Preferences.OBJECT, false);
        else if (isNumber(token))
            doc.setCharacterAttributes(startOffset, endOfToken - startOffset,
                                       Preferences.NUMBER, false);
        return endOfToken + 1;
    }

    /*
     * Assume the needle will the found at the start/end of the line
     */
    private int indexOf(String content, String needle, int offset) {
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
    private int lastIndexOf(String content, String needle, int offset) {
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
    protected boolean isDelimiter(String character) {
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
    protected boolean isQuoteDelimiter(String character) {
        String quoteDelimiters = "\"'";
        if (quoteDelimiters.indexOf(character) < 0)
            return false;
        else
            return true;
    }

    /*
     * Override for other languages
     */
    protected boolean isKeyword(String token) {
        Object o = keywords.get(token);
        return o == null ? false : true;
    }
    /*
     * Override for other languages
     */
    protected boolean isNumber(String token) {
        return token.matches("[[0-9]+.[0-9]+]*[0-9]+");
    }
    
    protected boolean isObject(String token) {
        Object o = objects.get(token);
        return o == null ? false : true;
    }

    /*
     * Override for other languages
     */

    protected String getSingleLineDelimiter() {
        return "#";
    }

    /*
     * Override for other languages
     */
    protected String getEscapeString(String quoteDelimiter) {
        return "\\" + quoteDelimiter;
    }

    /*
     *
     */
    protected String addMatchingBrace(int offset) throws BadLocationException {
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