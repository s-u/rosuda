package org.rosuda.JGR.toolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.rosuda.ibase.*;

/**
 *  PrefsDialog - dialog for setting preferences in JGR
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDa 2003 - 2004 
 */

public class PrefsDialog extends JDialog implements ActionListener, ItemListener {

    private Dimension screenSize = Common.getScreenRes();

    private String[] sizes = {"2","4","6","8","9","10","11","12","14","16","18","20","22","24"}; //,"30","36","48","72" };
    private String[] fonts;

    private JComboBox font = new JComboBox();
    private JComboBox size = new JComboBox();

    private DefaultComboBoxModel mf;
    private DefaultComboBoxModel ms;

    private JSpinner helptabs = new JSpinner();
    private JCheckBox useHelpAgent = new JCheckBox("Use Help Agent",JGRPrefs.useHelpAgent);
    private JCheckBox useHelpAgentConsole = new JCheckBox("in Console",JGRPrefs.useHelpAgentConsole);
    private JCheckBox useHelpAgentEditor = new JCheckBox("in Editor",JGRPrefs.useHelpAgentEditor);
    private JCheckBox useEmacsKeyBindings = new JCheckBox("Use Emacs Key Bindings",JGRPrefs.useEmacsKeyBindings);

    private JButton cancel = new JButton("Cancel");
    private JButton apply = new JButton("Apply");
    private JButton save = new JButton("Save");

    public PrefsDialog() {
        this(null);
    }

    /** Create PrefsDialog.
     *  @param f parent JFrame */
    public PrefsDialog(JFrame f) {
        super(f,"Preferences",true);

        helptabs.setMinimumSize(new Dimension(40,24));
        helptabs.setPreferredSize(new Dimension(40,24));
        helptabs.setMaximumSize(new Dimension(40,24));
        helptabs.setValue(new Integer(JGRPrefs.maxHelpTabs));
                
        fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        font.setModel(mf = new DefaultComboBoxModel(fonts));
        size.setModel(ms = new DefaultComboBoxModel(sizes));
        mf.setSelectedItem(JGRPrefs.FontName);
        ms.setSelectedItem(new Integer(JGRPrefs.FontSize));

        font.setMinimumSize(new Dimension(200,22));
        font.setPreferredSize(new Dimension(200,22));
        font.setMaximumSize(new Dimension(200,22));
        size.setMinimumSize(new Dimension(50,22));
        size.setPreferredSize(new Dimension(50,22));
        size.setMaximumSize(new Dimension(50,22));
        size.setEditable(true);
        
        JPanel prefs = new JPanel();
        prefs.setLayout(new GridBagLayout());
		
        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontPanel.add(new JLabel("Font: "));
        fontPanel.add(font);
        fontPanel.add(new JLabel(" Size: "));
        fontPanel.add(size);
        
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        helpPanel.add(new JLabel("#Help Pages: "));
        helpPanel.add(helptabs);
        
        useHelpAgentConsole.setEnabled(useHelpAgent.isSelected());
        useHelpAgentEditor.setEnabled(useHelpAgent.isSelected());
        
        JPanel consoleAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        consoleAgentPanel.add(new JLabel("  "));
        consoleAgentPanel.add(useHelpAgentConsole);
        
        JPanel editorAgentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editorAgentPanel.add(new JLabel("  "));
        editorAgentPanel.add(useHelpAgentEditor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5,2,2,2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        prefs.add(fontPanel,gbc);
        gbc.gridy = 1;
        prefs.add(helpPanel,gbc);
        gbc.gridy = 2;
        prefs.add(useHelpAgent,gbc);
        gbc.gridy = 3;
        prefs.add(consoleAgentPanel,gbc);
        gbc.gridy = 4;
        prefs.add(editorAgentPanel,gbc);
        gbc.gridy = 5;
        prefs.add(useEmacsKeyBindings,gbc);
        gbc.gridy = 6;
        prefs.add(new JLabel("* Emacs Keybindings are only advisable for Mac OS X!"),gbc);
        

        cancel.setActionCommand("cancel");
        apply.setActionCommand("apply");
        save.setActionCommand("save");

        cancel.addActionListener(this);
        apply.addActionListener(this);
        save.addActionListener(this);

        cancel.setToolTipText("Cancel");
        apply.setToolTipText("Apply changes to current session");
        save.setToolTipText("Save changes for future sessions and quit");
        
        useHelpAgent.addItemListener(this);
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(apply);
        buttons.add(save);

        	
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(prefs,BorderLayout.NORTH);
        this.getContentPane().add(buttons,BorderLayout.SOUTH);
        
        this.getRootPane().setDefaultButton(save);
        this.setResizable(false);
        this.setSize(new Dimension(400, 350));
        this.setLocation((screenSize.width-400)/2,(screenSize.height-350)/2);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });
        this.setVisible(true);
    }

    /**
     * Apply changes the user made.
     */
    public void applyChanges() {
        JGRPrefs.FontName = font.getSelectedItem().toString();
        JGRPrefs.FontSize = new Integer(size.getSelectedItem().toString()).intValue();
        JGRPrefs.maxHelpTabs = ((Integer)helptabs.getValue()).intValue();
        JGRPrefs.useHelpAgent = useHelpAgent.isSelected();
        JGRPrefs.useHelpAgentConsole = useHelpAgentConsole.isSelected();
        JGRPrefs.useHelpAgentEditor = useHelpAgentEditor.isSelected();
        JGRPrefs.useEmacsKeyBindings = useEmacsKeyBindings.isSelected();
        JGRPrefs.apply();
    }

    /**
     * actionPerformed: handle action event: buttons.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="apply") applyChanges();
        else if (cmd=="cancel") dispose();
        else if (cmd=="save") {
            applyChanges();
            JGRPrefs.writePrefs(false);
            this.dispose();
        }
    }
    
    /**
     * itemStateChanged: handle item events: enable/ disable useHelpAgentConsole/Editor
     */
    public void itemStateChanged(ItemEvent e) {
    	useHelpAgentConsole.setEnabled(useHelpAgent.isSelected());
    	useHelpAgentEditor.setEnabled(useHelpAgent.isSelected());
    	if (!useHelpAgent.isSelected()) {
    		useHelpAgentEditor.setSelected(false);
    		useHelpAgentConsole.setSelected(false);
    	}
    }
}
