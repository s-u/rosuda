package org.rosuda.JGR;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import org.rosuda.JGR.toolkit.*;

/**
 *  JGRObjectManager (should better be named ObjectBrowser) - implemenation of a browser showing the R-workspace: Datasets, and other objects as a tree, models in a table for sorting and comparing them, functions as a list.
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2005
 */

public class JGRObjectManager extends iFrame implements ActionListener, MouseListener {

    private JButton close = new JButton("Close");
    private JButton refresh = new JButton("Refresh");
    /**Save Data button */
    public JButton savedata = new JButton("Save Data");
    private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

    private ModelBrowserTable mBrowser;
    private ObjectBrowserTree dBrowser;
    private ObjectBrowserTree oBrowser;
    private FunctionList fBrowser;

    private JTabbedPane browsers = new JTabbedPane();

    /** Tooltip showing the summary for selected object*/
    public Popup summary = null;

    public JGRObjectManager() {
        super("Object Browser",iFrame.clsObjBrowser);

        String[] Menu = {"~Window","0"};
        iMenu.getMenu(this, this, Menu);

        while(!JGR.STARTED);
        RController.refreshObjects();

        refresh.setActionCommand("refresh");
        refresh.addActionListener(this);
        refresh.setToolTipText("Browse Workspace");

        close.setActionCommand("close");
        close.addActionListener(this);
        close.setToolTipText("Close Browser");
        
        savedata.setActionCommand("savedata");
        savedata.addActionListener(this);
        savedata.setToolTipText("Save Data");
        savedata.setEnabled(false);
        

        buttonPanel.add(refresh);
        buttonPanel.add(close);
        buttonPanel2.add(savedata);


        dBrowser = new ObjectBrowserTree(this,JGR.DATA,"data");
        JScrollPane d = new JScrollPane(dBrowser);
        d.addMouseListener(this);
        browsers.add("Data Objects",d);

        mBrowser = new ModelBrowserTable(this,JGR.MODELS);
        JScrollPane m = new JScrollPane(mBrowser);
        m.addMouseListener(this);
        browsers.add("Models",m);

        oBrowser = new ObjectBrowserTree(this,JGR.OTHERS,"other");
        JScrollPane o = new JScrollPane(oBrowser);
        d.addMouseListener(this);
        browsers.add("Other Objects",o);

        fBrowser = new FunctionList(this,JGR.FUNCTIONS);
        JScrollPane f = new JScrollPane(fBrowser);
        f.addMouseListener(this);
        browsers.add("Functions",f);


        browsers.addMouseListener(this);

		this.getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(1,1,1,1);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
        this.getContentPane().add(browsers,gbc);
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(1,1,1,10);
		this.getContentPane().add(buttonPanel2,gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(1,1,1,10);
		
        this.getContentPane().add(buttonPanel,gbc);
		
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowDeactivated(WindowEvent evt) {
                if (summary != null) summary.hide();
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose();
            }
        });
        this.setSize(new Dimension(400,500));
        this.setLocation(this.getLocation().x+400, 10);
    }

    /**
     * Refresh shown objects and synchronize them with workspace (R -> Objectbrowser).
     */
    public void refresh() {
        RController.refreshObjects();
        mBrowser.refresh();
        oBrowser.refresh(JGR.OTHERS);
        dBrowser.refresh(JGR.DATA);
        fBrowser.refresh(JGR.FUNCTIONS);
        savedata.setEnabled(false);
    }
    
    /**
     * mouseClicked: handle mouse event: disable Save Data button.
     */
    public void mouseClicked(MouseEvent e) {
        if (summary != null) summary.hide();
        savedata.setEnabled(false);
    }

    /**
     * mouseEntered: handle mouse event.
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * mouseExited: handle mouse event.
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * mousePressed: handle mouse event.
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * mouseReleased: handle mouse event.
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * actionPerformed: handle action event: menus and buttons.
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="close") dispose();
        else if (cmd=="refresh") refresh();
        else if (cmd=="savedata") 
            try {
                ((ObjectBrowserTree)((JScrollPane) browsers.getSelectedComponent()).getViewport().getComponent(0)).saveData();
            } catch (Exception ex) { ex.printStackTrace();}
    }
}
