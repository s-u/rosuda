/*
 * Created on 30.07.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rosuda.JGR.toolkit;

/**
 * @author Markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public class ToolBar extends JPanel {
	
	
	public IconButton newButton;
	public IconButton openButton;
	public IconButton saveButton;
	public IconButton undoButton;
	public IconButton redoButton;
	public IconButton cutButton;
	public IconButton copyButton;
	public IconButton pasteButton;
	public IconButton findButton;
	public IconButton stopButton;
	public IconButton helpButton;
    
	public InsertRemoveUndoManager undoMgr;
	
	
	public ToolBar(ActionListener al) {
		this(al, false, null);
	}
	
	public ToolBar(ActionListener al, boolean useStopButton) {
		this(al,useStopButton,null);
	}
	
	public ToolBar(ActionListener al, boolean useStopButton, ProgressLabel progress) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.undoMgr = new InsertRemoveUndoManager(al);
		this.newButton = new IconButton("/icons/new.png","New",al,"new"); 
		this.openButton = new IconButton("/icons/open.png","Open",al,"open");
		this.saveButton = new IconButton("/icons/save.png","Save",al,"save");
		this.undoButton = undoMgr.undoButton;
		this.redoButton = undoMgr.redoButton;
		this.cutButton = new IconButton("/icons/cut.png","Cut",al,"cut");
		this.copyButton = new IconButton("/icons/copy.png","Copy",al,"copy");
		this.pasteButton = new IconButton("/icons/paste.png","Paste",al,"paste");
		this.findButton = new IconButton("/icons/find.png","Search",al,"search");
		if (useStopButton) this.stopButton = new IconButton("/icons/stop.png","Stop",al,"stop");
		this.helpButton = new IconButton("/icons/help.png","Help",al,"help");
		
		this.add(newButton);
		this.add(openButton);
		this.add(saveButton);
		this.add(new Spacer(10));
		this.add(undoButton);
		this.add(redoButton);
		this.add(new Spacer(10));
		this.add(cutButton);
		this.add(copyButton);
		this.add(pasteButton);
		this.add(new Spacer(10));
		this.add(findButton);
		this.add(new Spacer(10));
		if (useStopButton) this.add(stopButton);
		this.add(new Spacer(10));
		this.add(helpButton);
        if (progress != null) {
        	this.add(new Spacer(20));
        	this.add(progress);
        }
    }
	
	class Spacer extends JPanel {
		public Spacer(int width) {
			this.setMinimumSize(new Dimension(width,0));
			this.setMaximumSize(new Dimension(width,0));
			this.setPreferredSize(new Dimension(width,0));
		}
	}
}
