package org.rosuda.JGR.editor;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import jedit.syntax.DefaultInputHandler;
import jedit.syntax.JEditTextArea;
import jedit.syntax.TextAreaPainter;
import jedit.syntax.TextUtilities;

public class RInputHandler extends DefaultInputHandler {

	public static final ActionListener R_INSERT_TAB = new r_insert_tab();

	public static final ActionListener R_RUN_LINES = new r_run_lines();

	public static final ActionListener R_RUN_ALL = new r_run_all();

	public static final ActionListener R_COMMENT_LINES = new r_comment_lines();
	
	public static final ActionListener R_PREV_LINE = new r_prev_line(false);
	
	public static final ActionListener R_NEXT_LINE = new r_next_line(false);
	
	public static final ActionListener R_CLOSE_POPUPS = new r_close_popups();
	
	public static final ActionListener R_INSERT_BREAK = new r_insert_break();
	
	public static Popup codeCompletion;

	public static class r_insert_tab extends insert_tab {

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null) {
				codeCompletion.hide();
				codeCompletion = null;
			}

			JEditTextArea textArea = getTextArea(evt);

			int carPos = textArea.getCaretPosition();

			if (carPos > 0 && textArea.getText(carPos - 1, 1).trim().length() != 0) {
				int line = textArea.getCaretLine();
				String lineStr = textArea.getLineText(line);
				int start = 0, end = carPos;
				try {
					start = TextUtilities.findWordStart(lineStr, carPos - textArea.getLineStartOffset(line) - 1, null);
				}  catch (StringIndexOutOfBoundsException ex) {
				}
				try {
					end = carPos;//TextUtilities.findWordEnd(lineStr, carPos - textArea.getLineStartOffset(line) - 1, null);
				}  catch (StringIndexOutOfBoundsException ex) {
				}
				
				boolean isfile = false;

				String pattern = lineStr.substring(start, end).trim();
				
				if (pattern.length() <= 0) {
					super.actionPerformed(evt);
					return;
				}
				
				try {
					isfile = lineStr.substring(start - 1, start).equalsIgnoreCase("\"");
				} catch (StringIndexOutOfBoundsException ex) {
				}

				int x = textArea._offsetToX(line, carPos);
				int y = textArea.lineToY(line);
				
				Point loc = new Point(x,y);
				SwingUtilities.convertPointToScreen(loc, (Component)evt.getSource());
				
				int posC = -1;
				
				if (isfile)
					posC = CodeCompletion.getInstance().updateFileList(pattern);
				else {
					posC = CodeCompletion.getInstance().updateList(pattern);
				}
				if (posC > 0) {
					codeCompletion = PopupFactory.getSharedInstance().getPopup((Component)evt.getSource(), (Component)CodeCompletion.getInstance(), loc.x, loc.y + TextAreaPainter.FONTSIZE + 10);
					codeCompletion.show();
				}
				
			} else
				super.actionPerformed(evt);
		}
	}

	public static class r_run_lines extends insert_tab {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			int startLine = textArea.getSelectionStartLine();
			int endLine = textArea.getSelectionEndLine();

			if (startLine >= 0 && endLine < textArea.getLineCount()) {
				for (int line = startLine; line <= endLine; line++) {
					String lineText = textArea.getLineText(line).trim();
					if (lineText.length() > 0)
						System.out.println("run cmd: " + lineText);
				}

			}

		}
	}

	public static class r_comment_lines extends insert_tab {
		public static final String COMMENT_CHAR = "#";

		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			int startLine = textArea.getSelectionStartLine();
			int endLine = textArea.getSelectionEndLine();

			if (startLine < 0)
				startLine = endLine = textArea.getCaretLine();

			for (int line = startLine; line <= endLine; line++) {
				int pos = textArea.getLineStartOffset(line);
				try {
					if (textArea.getLineText(line).trim().startsWith(COMMENT_CHAR)) {
						textArea.getDocument().remove(pos, textArea.getLineText(line).indexOf(COMMENT_CHAR) + 1);
					} else {
						textArea.getDocument().insertString(pos, COMMENT_CHAR, null);
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static class r_run_all extends insert_tab {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			int startLine = 0;
			int endLine = textArea.getLineCount();

			for (int line = startLine; line < endLine; line++) {
				String lineText = textArea.getLineText(line).trim();
				if (lineText.length() > 0)
					System.out.println("run cmd: " + lineText);
			}

		}
	}
	
	public static class r_close_popups implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null) {
				codeCompletion.hide();
				codeCompletion = null;
			}
		}
	}
	
	public static class r_next_line extends next_line {
		public r_next_line(boolean select) {
			super(select);
		}

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null)
				CodeCompletion.getInstance().next();
			else
				super.actionPerformed(evt);

		}
	}
	
	public static class r_prev_line extends prev_line {
		public r_prev_line(boolean select) {
			super(select);
		}

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null)
				CodeCompletion.getInstance().previous();
			else
				super.actionPerformed(evt);
		}
	}
	
	public static class r_insert_break extends insert_break {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.isEditable()) {
				textArea.getToolkit().beep();
				return;
			}

			if (codeCompletion != null) {
				String completion = CodeCompletion.getInstance().getCompletion();
				textArea.setSelectedText(completion);
				codeCompletion.hide();
				codeCompletion = null;
			}
			else
				super.actionPerformed(evt);
		}
	}

	static {
		actions.put("insert-tab", R_INSERT_TAB);
		actions.put("run-lines", R_RUN_LINES);
		actions.put("run-all", R_RUN_ALL);
		actions.put("comment-lines", R_COMMENT_LINES);
		actions.put("prev-line", R_PREV_LINE);
		actions.put("next-line", R_NEXT_LINE);
		actions.put("close-popups", R_CLOSE_POPUPS);
		actions.put("insert-break", R_INSERT_BREAK);
	}

	public void addKeyBindings() {
		addDefaultKeyBindings();
		addKeyBinding("TAB", R_INSERT_TAB);
		addKeyBinding("M+R", R_RUN_LINES);
		addKeyBinding("MS+R", R_RUN_ALL);
		addKeyBinding("M+7", R_COMMENT_LINES);
		addKeyBinding("UP", R_PREV_LINE);
		addKeyBinding("DOWN", R_NEXT_LINE);
		addKeyBinding("ESCAPE", R_CLOSE_POPUPS);
		addKeyBinding("ENTER", R_INSERT_BREAK);
	}
	
	public void keyReleased(KeyEvent evt) {
		if (codeCompletion != null && evt.getKeyCode() != KeyEvent.VK_UP && evt.getKeyCode() != KeyEvent.VK_DOWN) {
			JEditTextArea textArea = getTextArea(evt);
		
			int carPos = textArea.getCaretPosition();

			if (carPos > 0 && textArea.getText(carPos - 1, 1).trim().length() != 0) {
				int line = textArea.getCaretLine();
				String lineStr = textArea.getLineText(line);
				int start = 0, end = carPos;
				try {
					start = TextUtilities.findWordStart(lineStr, carPos - textArea.getLineStartOffset(line) - 1, null);
				}  catch (StringIndexOutOfBoundsException ex) {
				}

				String pattern = null;
				
				try {
					pattern = lineStr.substring(start,end); 
				}  catch (StringIndexOutOfBoundsException ex) {
				}

				int posC = -1;
				if (pattern == null || (posC = CodeCompletion.getInstance().updateList(pattern)) < 1) {
					codeCompletion.hide();
					codeCompletion = null;
				}
			}
		}
		super.keyReleased(evt);
	}
}