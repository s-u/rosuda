package org.rosuda.JGR.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.rosuda.JGR.JGR;

/**
 * @author Markus
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConsoleOutput extends JTextPane {
	
	public ConsoleOutput() {
        if (FontTracker.current == null) FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        setDocument(new JGRStyledDocument());
    }
	
	public void startExort() {
		new ExportOutput(this);
	}
	
	private void exportCommand(File file) {
		int l = this.getLineCount();
		StringBuffer bf = new StringBuffer();
		String line = null;
		for (int i = 0; i < l; i++) {
			try {
				if(isCorrectLine(i) && isCommandLine(i)) 
					bf.append(trimFront(getLine(i).replaceFirst(">","")));
			}
			catch (Exception e){
			}
		}
		saveToFile(file,bf);
	}
	
	private void exportOutput(File file) {
		int l = this.getLineCount();
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < l; i++) {
			try {
				if(isCorrectLine(i))
					bf.append(getLine(i));
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		saveToFile(file,bf);		
	}
	
	private void exportResult(File file) {
		int l = this.getLineCount();
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < l; i++) {
			try {
				if(isCorrectLine(i)){
					if (isResultLine(i))
						bf.append(getLine(i));
					else 
						bf.append("\n");
				}
			}
			catch (Exception e){
			}
		}
		saveToFile(file,bf);		
	}
	
	private boolean isCommandLine(int i) throws BadLocationException {
		this.setCaretPosition(getLineStartOffset(i));
		return this.getCharacterAttributes().isEqual(JGRPrefs.CMD);
	}
	
	private boolean isResultLine(int i) throws BadLocationException {
		this.setCaretPosition(getLineStartOffset(i));
		return this.getCharacterAttributes().isEqual(JGRPrefs.RESULT);
	}
	
	private boolean isCorrectLine(int i) {
		return true;
	}
	
	private String trimFront(String s) {
		s = s.replaceFirst("\\s*","");
		return s;
	}
	
	public String getLine(int i) {
		String line = null;
		try {
			
			int s = getLineStartOffset(i);
			int e = getLineEndOffset(i);
			line = this.getText(s,e-s);
		}
		catch (BadLocationException e) {
		}
		return line;
	}	
	
	private void saveToFile(File file, StringBuffer bf) {
        try {
        	BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(bf.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(JGR.MAINRCONSOLE,"Permisson denied","File Errror",JOptionPane.OK_OPTION);
        } finally {
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
    
    public String getText(int offs, int len) {
        try {
            Document doc = this.getDocument();
            return doc.getText(0,doc.getLength()).substring(offs,offs+len);
        } catch (BadLocationException e) {
            return null;
        }   
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
    
    class ExportOutput extends JDialog implements ActionListener{
    	
    	private ConsoleOutput out;
    	
    	private JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
    	private JRadioButton wholeOutput = new JRadioButton("Complete Output",true);
    	private JRadioButton cmdsOutput = new JRadioButton("Commands",false);
    	private JRadioButton resultOutput = new JRadioButton("Results",false);
    	
    	public ExportOutput(ConsoleOutput co) {
    		super(JGR.MAINRCONSOLE,"Export Output");
    		
    		this.out = co;
			this.getContentPane().setLayout(new BorderLayout());
			
			JPanel options = new JPanel(new GridBagLayout());
			options.add(new JLabel("Options"),new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
		            , GridBagConstraints.WEST, GridBagConstraints.NONE,
		            new Insets(1, 5, 1, 5), 0, 0));
			options.add(wholeOutput, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
		            , GridBagConstraints.WEST, GridBagConstraints.NONE,
		            new Insets(1, 5, 1, 5), 0, 0));
			options.add(cmdsOutput, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
		            , GridBagConstraints.WEST, GridBagConstraints.NONE,
		            new Insets(1, 5, 1, 5), 0, 0));
			options.add(resultOutput, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
		            , GridBagConstraints.WEST, GridBagConstraints.NONE,
		            new Insets(1, 5, 1, 5), 0, 0));
			options.add(new JPanel(), new GridBagConstraints(3, 0, 1, 4, 1.0, 1.0
		            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		            new Insets(1, 5, 1, 5), 0, 0));
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(wholeOutput);
			bg.add(cmdsOutput);
			bg.add(resultOutput);
			
			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			fileChooser.addActionListener(this);
			this.getContentPane().add(fileChooser,BorderLayout.CENTER);
			this.getContentPane().add(options,BorderLayout.SOUTH);
			this.pack();
                        this.setSize(new Dimension(500,450));
			this.setVisible(true);
    	}
    	
    	
    	public void export(File file) {
    		if (wholeOutput.isSelected())
    			out.exportOutput(file);
    		else if (cmdsOutput.isSelected())
    			out.exportCommand(file);
    		else if (resultOutput.isSelected())
    			out.exportResult(file);
    	}
    	
    	public void actionPerformed(ActionEvent e){
    		String cmd = e.getActionCommand();
            if (cmd == "ApproveSelection") export(fileChooser.getSelectedFile());
            dispose();    		
    	}
    }
}
