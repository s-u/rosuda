package org.rosuda.JGR.toolkit;

/**
*  ToolBar
 *
 * 	icon ToolBar
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.awt.Dimension;
import java.awt.BorderLayout;
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
        this.setLayout(new BorderLayout());

        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));

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

        b.add(newButton);
        b.add(openButton);
        b.add(saveButton);
        b.add(new Spacer(10));
        b.add(undoButton);
        b.add(redoButton);
        b.add(new Spacer(10));
        b.add(cutButton);
        b.add(copyButton);
        b.add(pasteButton);
        b.add(new Spacer(10));
        b.add(findButton);
        b.add(new Spacer(10));
        if (useStopButton) b.add(stopButton);
        b.add(new Spacer(10));
        b.add(helpButton);
        this.add(b,BorderLayout.WEST);
        if (progress != null) {
            progress.setVisible(true);
            this.add(/*new JPanel(new FlowLayout(FlowLayout.LEFT)).add(*/progress/*)*/,BorderLayout.EAST);
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
