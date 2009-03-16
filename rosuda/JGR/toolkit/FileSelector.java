package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.rosuda.ibase.Common;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.util.*;

/**
 * FileSelector - use AWT filedialog on a Mac because of look&feel, and SWING on
 * other machines because it provides more features.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class FileSelector extends JFrame {

	/** @deprecated use LOAD */
	public final static int OPEN = 0;

	/** OPEN DIALOG */
	public final static int LOAD = 0;

	/** SAVE DIALOG */
	public final static int SAVE = 1;
	
	public static String lastDirectory = JGRPrefs.workingDirectory;

	private FileDialog awtDialog = null;

	private JFileChooser swingChooser = null;

	private int type = 0;

	private Frame f;
	
	private int result = JFileChooser.CANCEL_OPTION;

	/**
	 * Create a FileDialog, on Mac we use the AWT on others i'm currently using
	 * SWING.
	 * 
	 * @param f
	 *            parent Frame
	 * @param title
	 *            Title
	 * @param type
	 *            OPEN or SAVE
	 * @param directory
	 *            should we start in a specified directory
	 */
	public FileSelector(Frame f, String title, int type, String directory) {
		this.type = type;
		this.f = f;
		if (Common.isMac()) {
			awtDialog = new FileDialog(f, title, type);
			if (directory != null)
				awtDialog.setDirectory(directory);
		} else {
			if (directory != null)
				swingChooser = new JFileChooser(directory);
			else
				swingChooser = new JFileChooser();
			swingChooser.setDialogTitle(title);
			swingChooser.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
		}
	}
	
	/**
	 * Create a fileDialog using the last selected directory as
	 * the staring place.
	 * 
	 */
	public FileSelector(Frame f, String title, int type) {
		this.type = type;
		this.f = f;
		if (Common.isMac()) {
			awtDialog = new FileDialog(f, title, type);
			awtDialog.setDirectory(lastDirectory);
		} else {
			swingChooser = new JFileChooser(lastDirectory);
			swingChooser.setDialogTitle(title);
			swingChooser.setFileHidingEnabled(!JGRPrefs.showHiddenFiles);
		}
	}
	
	public void addActionListener(ActionListener al) {
		if (!Common.isMac())
			swingChooser.addActionListener(al);
	}

	/**
	 * Show fileselector.
	 */
	public void setVisible(boolean b) {
		if (Common.isMac())
			awtDialog.setVisible(true);
		else if (type == OPEN)
			result = swingChooser.showOpenDialog(f);
		else if (type == SAVE)
			result = swingChooser.showSaveDialog(f);
		else
			result = swingChooser.showDialog(f, "OK");
	}

	/**
	 * Get selected filename.
	 * 
	 * @return filename
	 */
	public String getFile() {
		String fileName = null;
		try {
			if (Common.isMac()){
				fileName = awtDialog.getFile();
				FileSelector.lastDirectory = awtDialog.getDirectory();
			}
			else{
				if(result == JFileChooser.CANCEL_OPTION)
					return null;
				fileName = swingChooser.getSelectedFile().getName();
				FileSelector.lastDirectory = swingChooser.getCurrentDirectory().getAbsolutePath()
				+ File.separator;
			}
			return fileName;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get selected directoryname.
	 * 
	 * @return directory
	 */
	public String getDirectory() {
		try {
			if (Common.isMac()){
				
				FileSelector.lastDirectory = awtDialog.getDirectory();
				return FileSelector.lastDirectory;
			}
			/*if(result == JFileChooser.CANCEL_OPTION)
				return null;*/
			FileSelector.lastDirectory = swingChooser.getCurrentDirectory().getAbsolutePath()
					+ File.separator;
			return FileSelector.lastDirectory;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Set current file.
	 * 
	 * @param file
	 *            filename
	 */
	public void setFile(String file) {
		try {
			if (Common.isMac())
				awtDialog.setFile(file);
			else
				swingChooser.setSelectedFile(new File(file));
		} catch (Exception e) {
		}
	}
	
	/**
	 * Adds a JPanel at the bottom of the dialog
	 * 
	 * 
	 * @param panel
	 * 				the panel to add
	 */
	public void addFooterPanel(JPanel panel){
		JPanel fileView=null;
		try{
		if (!Common.isMac()) {
			fileView = (JPanel) ((JComponent) ((JComponent) swingChooser
					.getComponent(2)).getComponent(2)).getComponent(2);
		}
		
		if(fileView!=null){
			fileView.add(panel);
			JPanel pp = (JPanel) ((JComponent) ((JComponent) swingChooser
					.getComponent(2)).getComponent(2)).getComponent(0);
			JPanel temp = new JPanel();
			temp.setMaximumSize(new Dimension(0,panel.getPreferredSize().height));
			pp.add(temp);
		}
		}catch(Exception e){
			new ErrorMsg(e);
		}
		
	}
	
	

	public boolean isSwing()
	{
		return !Common.isMac();
	}
	
	public Component getSelector() {
		if (Common.isMac())
			return awtDialog;
		return swingChooser;
	}
	
	public JFileChooser getJFileChooser(){
		return swingChooser;
	}
	public FileDialog getAWTChooser(){
		return awtDialog;
	}
}