package org.rosuda.JGR;


/**
 *  JGRDataFileDialog
 *  
 *  load datasets into R
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;


public class JGRDataFileDialog extends JDialog implements ActionListener, ItemListener, PropertyChangeListener {

    private JFileChooser fileChooser = new JFileChooser();
    private JTextField dataName = new JTextField();
    private JTextField otherSeps = new JTextField();
    private JCheckBox header = new JCheckBox("Header",true);
    private JCheckBox attach = new JCheckBox("Attach",false);

    private JComboBox sepsBox = new JComboBox(new String[] {"Default","\\t",",",";","|",""});
    private String[] seps = new String[] {"","\\t",",",";","|"};

    private JComboBox quoteBox = new JComboBox(new String[] {"Default",""});
    private String[] quotes = new String[] {""};

    private boolean useHeader = true;

    private Dimension screenSize = Common.getScreenRes();

    public JGRDataFileDialog(Frame f,String directory) {
        super(f,"Load DataFile",true);

        dataName.setMinimumSize(new Dimension(160,20));
        dataName.setPreferredSize(new Dimension(160,20));
        dataName.setMaximumSize(new Dimension(160,20));

        quoteBox.setMinimumSize(new Dimension(90,20));
        quoteBox.setPreferredSize(new Dimension(90,20));
        quoteBox.setMaximumSize(new Dimension(90,20));

        sepsBox.setMinimumSize(new Dimension(90,20));
        sepsBox.setPreferredSize(new Dimension(90,20));
        sepsBox.setMaximumSize(new Dimension(90,20));

        
        quoteBox.addItemListener(this);
        sepsBox.addItemListener(this);

        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        if (directory != null && new File(directory).exists()) fileChooser.setCurrentDirectory(new File(directory));

        this.getContentPane().setLayout(new BorderLayout());

        JPanel options = new JPanel(new GridBagLayout());
        
        JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
        command.add(dataName);
        command.add(new JLabel(" <- (...,"));
        command.add(header);
        command.add(new JLabel(", sep="));
        command.add(sepsBox);
        command.add(new JLabel(", quote="));
        command.add(quoteBox);
        command.add(new JLabel(")"));
        
        options.add(command,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(1, 5, 1, 5), 0, 0));
        options.add(attach,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        		, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(1, 5, 1, 5), 0, 0));
        options.add(new JPanel(), new GridBagConstraints(3, 0, 1, 2, 1.0, 1.0
	            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	            new Insets(1, 5, 1, 5), 0, 0));


        this.getContentPane().add(fileChooser,BorderLayout.CENTER);
        this.getContentPane().add(options,BorderLayout.SOUTH);
        this.setLocation((screenSize.width-500)/2,(screenSize.height-450)/2);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });
        this.pack();
        this.setSize(new Dimension(600,450));
        this.setVisible(true);
    }

    public void loadFile() {
        if (fileChooser.getSelectedFile() != null) {
            JGRConsole.directory = fileChooser.getCurrentDirectory().getAbsolutePath()+File.separator;
            String file = fileChooser.getSelectedFile().toString();

            String useSep;
            if (sepsBox.getSelectedIndex() >= seps.length) useSep = sepsBox.getSelectedItem().toString();
            else useSep = seps[sepsBox.getSelectedIndex()];
            String useQuote;
            if (quoteBox.getSelectedIndex() >= quotes.length) useQuote = quoteBox.getSelectedItem().toString();
            else useQuote = quotes[quoteBox.getSelectedIndex()];

            String cmd = dataName.getText().trim().replaceAll("\\s","")+ "<- read.table(\""+file.replace('\\','/')+"\",header="+(header.isSelected()?"T":"F")+",sep=\""+useSep+"\", quote=\""+useQuote+"\")"+(attach.isSelected()?";attach("+dataName.getText().trim().replaceAll("\\s","")+")":"")+"";
            JGR.MAINRCONSOLE.execute(cmd);
        }
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println(cmd);
        if (cmd == "ApproveSelection") loadFile();
        else if (cmd == "CancelSelection") dispose();
    }

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean edit = false;
        if (source == quoteBox) {
            edit = quoteBox.getSelectedIndex() == quoteBox.getItemCount()-1?true:false;
            quoteBox.setEditable(edit);
        }
        else if (source == sepsBox) {
            edit = sepsBox.getSelectedIndex() == sepsBox.getItemCount()-1?true:false;
            sepsBox.setEditable(edit);
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void propertyChange(PropertyChangeEvent e) {
        File file = fileChooser.getSelectedFile();
        if(file!=null && !file.isDirectory()) {
            String name = file.getName().replaceAll("\\..*", "");
            name = name.replaceAll("^[0-9]+|[^a-zA-Z|^0-9|^_]",".");
            dataName.setText(name);
        }
        else dataName.setText(null);
    }
}
