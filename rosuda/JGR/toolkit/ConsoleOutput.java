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
 *  ConsoleOutput - implementation of a textpane providing different export possibilities.
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2005
 */

public class ConsoleOutput extends JTextPane {

    public ConsoleOutput() {
        if (FontTracker.current == null) FontTracker.current = new FontTracker();
        FontTracker.current.add(this);
        setDocument(new JGRStyledDocument());
    }

    /**
     * Open export dialog.
     */
    public void startExport() {
        new ExportOutput(this);
    }

    private void exportCommands(File file) {
        saveToFile(file,getCommands());
    }

    /**
     * Copy only the commands out of the pane.
     */
    public void copyCommands() {
        java.awt.datatransfer.StringSelection s = new java.awt.datatransfer.StringSelection(getCommands().toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s,s);
    }

    private StringBuffer getCommands() {
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
        return bf;
    }




    private void exportOutput(File file) {
        saveToFile(file,getOutput());
    }

    /**
     * Copy the whole content of the pane.
     */
    public void copyOutput() {
        java.awt.datatransfer.StringSelection s = new java.awt.datatransfer.StringSelection(getOutput().toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s,s);
    }

    private StringBuffer getOutput() {
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
        return bf;
    }


    private void exportResult(File file) {
        saveToFile(file,getResult());
    }

    /**
     * Copy only the results from the pane.
     */
    public void copyResults() {
        java.awt.datatransfer.StringSelection s = new java.awt.datatransfer.StringSelection(getResult().toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s,s);
    }

    private StringBuffer getResult() {
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
        return bf;
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

    
    private String getLine(int i) {
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

    /**
     * Get text content of pane.
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
     * Get text content of pane, specified by supplied startposition and length.
     */
    public String getText(int offs, int len) {
        try {
            Document doc = this.getDocument();
            return doc.getText(0,doc.getLength()).substring(offs,offs+len);
        } catch (BadLocationException e) {
            return null;
        }
    }

    /**
     * Append text to pane.
     * @param str text which shoudl be appended
     * @param a attribute-set for insertion
     */
    public void append(String str, AttributeSet a) {
        Document doc = getDocument();
        if (doc != null) {
            try {
                doc.insertString(doc.getLength(), str, a);
            } catch (BadLocationException e) {
            }
        }
    }

    /**
     * Count lines in this pane.
     * @return number of lines
     */
    public int getLineCount() {
        Element map = getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    /**
     * Get startposition of line.
     * @param line line-number
     * @return startposition
     * @throws BadLocationException
     */
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

    /**
     * Get endposition of line.
     * @param line line-number
     * @return endposition
     * @throws BadLocationException
     */
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

    /**
     * Get line related to current position.
     * @param offset current position
     * @return line-number
     * @throws BadLocationException
     */
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
     * Removes text beginnig at supplied position.
     * @param index startindex
     * @throws BadLocationException
     */
    public void removeAllFrom(int index) throws BadLocationException {
        this.getDocument().remove(index,this.getDocument().getLength()-index);
    }

    /**
     * Apply font to pane.
     */
    public void setFont(Font f) {
        super.setFont(f);
        try {
            ((StyledDocument) this.getDocument()).setCharacterAttributes(0, this.getText().length(),JGRPrefs.SIZE, false);
        } catch (Exception e) {}
    }

    class ExportOutput extends JFileChooser implements ActionListener { 

        private ConsoleOutput out;

        private JRadioButton wholeOutput = new JRadioButton("Complete Output",true);
        private JRadioButton cmdsOutput = new JRadioButton("Commands",false);
        private JRadioButton resultOutput = new JRadioButton("Results",false);
		
		private JButton ok = new JButton("Save");
		private JButton cancel = new JButton("Cancel");

        public ExportOutput(ConsoleOutput co) {
            this.out = co;
            
            ButtonGroup bg = new ButtonGroup();
            bg.add(wholeOutput);
            bg.add(cmdsOutput);
            bg.add(resultOutput);

            this.addActionListener(this);
            
    		if (System.getProperty("os.name").startsWith("Window")) {
    			JPanel fileview = (JPanel)((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(2);
    			
    			JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
            	options.add(new JLabel("Options: ")); 
            	options.add(wholeOutput);
            	options.add(cmdsOutput); 
            	options.add(resultOutput); 
            
    			fileview.add(options);
    			JPanel pp = (JPanel) ((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(0);
    			pp.add(new JPanel());
    		}
    		else {
    			JPanel filename = (JPanel) this.getComponent(this.getComponentCount()-1);
            	JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
            	options.add(new JLabel("Options: ")); 
            	options.add(wholeOutput);
            	options.add(cmdsOutput); 
            	options.add(resultOutput); 
            
            	filename.add(options,filename.getComponentCount()-1);
    		}
			
            this.showSaveDialog(co);
        }


        public void export(File file) {
            if (wholeOutput.isSelected())
                out.exportOutput(file);
            else if (cmdsOutput.isSelected())
                out.exportCommands(file);
            else if (resultOutput.isSelected())
                out.exportResult(file);
        }

        public void actionPerformed(ActionEvent e){
            String cmd = e.getActionCommand();
            if (cmd == "ApproveSelection") export(this.getSelectedFile());
        }
    }
}
