/*
 * Created on Dec 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rosuda.JGR;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;

import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.JGR.toolkit.*;

/**
 * @author markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JGRDataFileSaveDialog extends JFileChooser implements ActionListener, ItemListener, KeyListener, PropertyChangeListener {

    private JCheckBox append = new JCheckBox("append",false);
    private JCheckBox quote = new JCheckBox("quote",true);
	
	private JButton ok = new JButton("Save");
	private JButton cancel = new JButton("Cancel");
	
    private JComboBoxExt sepsBox = new JComboBoxExt(new String[] {"Default","\\t",",",";","|","Others..."});
    private String[] seps = new String[] {" ","\\t",",",";","|"};
    
    private String data;
	
	private Dimension screenSize = Common.getScreenRes();
    
    
    public JGRDataFileSaveDialog(Frame f, String data, String directory) {
        this.setDialogTitle("Save DatFile - "+data);
        if (directory != null && new File(directory).exists()) this.setCurrentDirectory(new File(directory));
        this.data = data;
        this.addActionListener(this);
        
        JPanel command = new JPanel(new GridBagLayout());
		command.add(append, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				 , GridBagConstraints.WEST, GridBagConstraints.NONE,
				 new Insets(1, 1, 1, 1), 0, 0));
		command.add(quote, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				 , GridBagConstraints.WEST, GridBagConstraints.NONE,
				 new Insets(1, 1, 1, 1), 0, 0));
		command.add(new JLabel("seps="), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
				 , GridBagConstraints.WEST, GridBagConstraints.NONE,
				 new Insets(1, 1, 1, 1), 0, 0));
		command.add(sepsBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
				 , GridBagConstraints.WEST, GridBagConstraints.NONE,
				 new Insets(1, 1, 1, 1), 0, 0));
		
		JPanel accessory = new JPanel();
		accessory.add(command);
		
		
        this.setAccessory(accessory);
        this.showSaveDialog(f);
    }
    
    
    
    
    public void saveFile() {
		if (this.getSelectedFile() != null) {
			JGRConsole.directory = this.getCurrentDirectory().getAbsolutePath()+File.separator;
			String file = this.getSelectedFile().toString();
			
			String useSep;
			if (sepsBox.getSelectedIndex() >= seps.length) useSep = sepsBox.getSelectedItem().toString();
			else useSep = seps[sepsBox.getSelectedIndex()];
			
			String cmd = "write.table("+data+",\""+file.replace('\\','/')+"\",append="+(append.isSelected()?"T":"F")+",quote="+(quote.isSelected()?"T":"F")+",sep=\""+useSep+"\")";
			JGR.MAINRCONSOLE.execute(cmd);
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "ApproveSelection") saveFile();
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
	    if (e.getSource() == sepsBox) 
			sepsBox.setEditable((sepsBox.getSelectedIndex() == sepsBox.getItemCount()-1?true:false));
	}

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
        System.out.println(e.getSource());
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        System.out.println(e.getSource());
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
        System.out.println(e.getSource());
    }
    
        /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println(evt.getPropertyName());
        System.out.println(evt.getNewValue());
        System.out.println(evt.getOldValue());
    }
}
