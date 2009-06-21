package org.rosuda.JGR.editor;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Markus Helbig
 */

public class SyntaxDocument extends DefaultStyledDocument {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6843788078390350530L;
	private DefaultStyledDocument doc;
	private Element rootElement;

	private static MutableAttributeSet BOLD = new SimpleAttributeSet();

	public SyntaxDocument() {
		doc = this;
		rootElement = doc.getDefaultRootElement();

		StyleConstants.setBold(BOLD, true);

		putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
	}

	/**
	 * Insert text and apply coloring.
	 */
	public void insertString(final int offset, String str, AttributeSet a) throws BadLocationException {
		boolean whitespace = false;
		if (str.equals("\t")) {
			try {
				whitespace = getText(offset - 1, 1).matches("[\\s|#]");
			} catch (Exception e) {
				/***/
			}
			if (!whitespace && offset != 0)
				str = str.replaceAll("\t", "");
			else {
				String tab = "";
				for (int i = 0; i < EditorPreferences.tabWidth; i++)
					tab += " ";
				str = tab;
			}
		} else if (str.equals("\n")) {

			int line = rootElement.getElementIndex(offset);

			int off = rootElement.getElement(line).getStartOffset();

			int i = off;
			try {
				while (getText(i++, 1).matches("[\\t\\x0B\\f]"))
					; // matches("[\\s]"));
			} catch (Exception ex) {
				/***/
			}
			try {
				str = "\n" + getText(off, i - off - 1).replaceAll("\n", "");
			} catch (Exception ex2) {
				/***/
			}
		}
		super.insertString(offset, str, a);
	}

	/**
	 * Remove text and apply again coloring.
	 */
	public void remove(int offset, int length) throws BadLocationException {
		if (offset == -1)
			return;
		super.remove(offset, length);
		// processChangedLines(offset, 0);
	}

	/**
	 * Determine which lines have changed and apply highlighting to them.
	 */
	private synchronized void processChangedLines(int offset, int length) throws BadLocationException {
		String content = doc.getText(0, doc.getLength());
		// The lines affected by the latest document update
		int startLine = rootElement.getElementIndex(offset);
		int endLine = rootElement.getElementIndex(offset + length);
		// Do the actual highlighting
		for (int i = startLine; i <= endLine; i++)
			applyHighlighting(content, i);
	}

	/**
	 * Parse the line to determine the appropriate highlighting.
	 */
	private synchronized void applyHighlighting(String content, int line) {
		int startOffset = rootElement.getElement(line).getStartOffset();
		int endOffset = rootElement.getElement(line).getEndOffset() - 1;
		int lineLength = endOffset - startOffset;
		if (lineLength < 0)
			lineLength = 0;
		int contentLength = content.length();
		if (endOffset >= contentLength)
			endOffset = contentLength - 1;

		// set normal attributes for the line
		doc.setCharacterAttributes(startOffset, lineLength, EditorPreferences.NORMAL, true);
		// check for single line comment
		int index = content.indexOf(getSingleLineDelimiter(), startOffset);
		if ((index > -1) && (index < endOffset)) {
			doc.setCharacterAttributes(index, endOffset - index + 1, EditorPreferences.COMMENT, false);
			endOffset = index - 1;
		}
		// check for tokens*/
		checkForTokens(content, startOffset, endOffset);
	}

	/**
	 * Parse the line for tokens to highlight.
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

	/**
	 * Parse the line for quote-tokens.
	 */
	private synchronized int getQuoteToken(String content, int startOffset, int endOffset) {
		String quoteDelimiter = content.substring(startOffset, startOffset + 1);
		String escapeString = getEscapeString(quoteDelimiter);
		int index;
		int endOfQuote = startOffset;
		// skip over the escape quotes in this quote
		index = content.indexOf(escapeString, endOfQuote + 1);
		while ((index > -1) && (index < endOffset)) {
			endOfQuote = index + 1;
			index = content.indexOf(escapeString, endOfQuote);
		}
		// now find the matching delimiter
		index = content.indexOf(quoteDelimiter, endOfQuote + 1);
		if ((index < 0) || (index > endOffset))
			endOfQuote = endOffset;
		else
			endOfQuote = index;
		doc.setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, EditorPreferences.QUOTE, false);
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
			doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.KEYWORD, false);
		if (isObject(token))
			doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.OBJECT, false);
		else if (isNumber(token))
			doc.setCharacterAttributes(startOffset, endOfToken - startOffset, EditorPreferences.NUMBER, false);
		return endOfToken + 1;
	}

	/**
	 * Is character a delimiter (,;:{}()[]+-/%<=>!&|^~*$).
	 */
	protected synchronized boolean isDelimiter(String character) {
		String operands = EditorPreferences.DELIMITERS;
		if (Character.isWhitespace(character.charAt(0)) || operands.indexOf(character) != -1)
			return true;
		return false;
	}

	/**
	 * Is character a qoutedelimiter (").
	 */
	protected synchronized boolean isQuoteDelimiter(String character) {
		if (EditorPreferences.QUOTESTRING.indexOf(character) < 0)
			return false;
		return true;
	}

	/**
	 * Is character a keyword.
	 */
	protected synchronized boolean isKeyword(String token) {
		Object o = EditorPreferences.KEYWORDS.get(token);
		return o == null ? false : true;
	}

	/**
	 * Is character a number.
	 */
	protected synchronized boolean isNumber(String token) {
		return token.matches("[[0-9]+.[0-9]+]*[0-9]+");
	}

	/**
	 * Is character a object in current workspace.
	 */
	protected synchronized boolean isObject(String token) {
		Object o = EditorPreferences.KEYWORDS_OBJECTS.get(token);
		return o == null ? false : true;
	}

	/**
	 * Get character which indicates comments (#).
	 * 
	 * @return #
	 */
	protected synchronized String getSingleLineDelimiter() {
		return EditorPreferences.SINGLELINECOMMENT;
	}

	/**
	 * Get escape character.
	 * 
	 * @return \\
	 */
	protected synchronized String getEscapeString(String quoteDelimiter) {
		return "\\" + quoteDelimiter;
	}

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
		return "{\n" + whiteSpace.toString() + whiteSpace.toString() + "\n" + whiteSpace.toString() + "}";
	}

	protected void fireInsertUpdate(DocumentEvent evt) {
		super.fireInsertUpdate(evt);
		try {
			processChangedLines(evt.getOffset(), evt.getLength());
		} catch (BadLocationException ex) {
			// do something about this
		}
	}

	protected void fireRemoveUpdate(DocumentEvent evt) {
		super.fireRemoveUpdate(evt);
		try {
			processChangedLines(evt.getOffset(), evt.getLength());
		} catch (BadLocationException ex) {
			// do something about this
		}
	}
}