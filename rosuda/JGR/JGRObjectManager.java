package org.rosuda.JGR;

/**
 *  JGRObjectManager
 * 
 * 	browse workspace
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */


import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.toolkit.*;

public class JGRObjectManager extends iFrame implements ActionListener, MouseListener {

	private JButton close = new JButton("Close");
	private JButton refresh = new JButton("Refresh");
	private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	
	private ModelBrowserTable mBrowser;
	private ObjectBrowserTree dBrowser;
	private ObjectBrowserTree oBrowser;
	private ObjectBrowserTree fBrowser;
	
	private JTabbedPane browsers = new JTabbedPane();
	
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
		
		buttonPanel.add(refresh);
		buttonPanel.add(close);

		
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
		
		fBrowser = new ObjectBrowserTree(this,JGR.FUNCTIONS,"functions");
		JScrollPane f = new JScrollPane(fBrowser);
		f.addMouseListener(this);
		browsers.add("Functions",f);
		
		
		browsers.addMouseListener(this);
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(browsers,BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
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
	
	public void refresh() {
		RController.refreshObjects();
		mBrowser.refresh();
		oBrowser.refresh(JGR.OTHERS);
		dBrowser.refresh(JGR.DATA);
		fBrowser.refresh(JGR.FUNCTIONS);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (summary != null) summary.hide();
	}	
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}		
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd=="close") dispose();
		else if (cmd=="refresh") refresh();
 	}
}