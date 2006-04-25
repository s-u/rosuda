/**
 * 
 */
package org.rosuda.JGR.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.undo.CannotUndoException;

import org.rosuda.JGR.toolkit.JGRFrame;
import org.rosuda.JGR.toolkit.JGRMenu;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.toolkit.ToolBar;
import org.rosuda.ibase.toolkit.FrameDevice;
import org.rosuda.util.RecentList;

/**
 * @author Markus Helbig
 *
 */
public class Editor extends JGRFrame implements ActionListener {
	
	private EditorWorker ew = null;
	private SyntaxArea sArea = null;
	private ToolBar toolBar;
	
	/** Recent documents which where opened the last times with the editor.*/
    public static RecentList recentOpen;
    /** Menuitem for the recent-list*/
    private JMenu recentMenu;

	String[] Menu = {
	            "+", "$FFile", "@N$NNew", "new", "@O$OOpen", "open","#$ROpen Recent","",
	            "@S$SSave", "save", "!S$ASave as", "saveas",
	            "-", "@P$PPrint", "print","~File.Basic.End",
	            "~Editor",
	            "+", "$TTools", "$IIncrease Font Size", "fontBigger", "$DDecrease Font Size",
	            "fontSmaller",
	            "~Window",
	            "~Help", "~About", "0"};
	
	public Editor() {
		this(new DefaultEditorWorker());
	}
	
	public Editor(EditorWorker ew) {
		super("Editor",FrameDevice.clsEditor);
		
		this.ew = ew;
		
		JGRMenu.getMenu((JFrame)this.getFrame(), this, Menu);
		JMenu rm=recentMenu=(JMenu) JGRMenu.getItemByLabel(this,"Open Recent");
        if (rm!=null) {
            if (recentOpen==null)
                recentOpen=new RecentList("JGR","RecentOpenFiles",8);
            String[] shortNames=recentOpen.getShortEntries();
            String[] longNames =recentOpen.getAllEntries();
            int i=0;
            while (i<shortNames.length) {
                JMenuItem mi=new JMenuItem(shortNames[i]);
                mi.setActionCommand("recent:"+longNames[i]);
                mi.addActionListener(this);
                rm.add(mi);
                i++;
            }
            if (i>0) rm.addSeparator();
            JMenuItem ca=new JMenuItem("Clear list");
            ca.setActionCommand("recent-clear");
            ca.addActionListener(this);
            rm.add(ca);
            if (i==0) ca.setEnabled(false);
        }
        
        toolBar = new ToolBar(this,false);
        
		this.sArea = new SyntaxArea();
		sArea.getDocument().addUndoableEditListener(toolBar.undoMgr);
		
		JPanel footer = new JPanel();
		
		JScrollPane jsp = new JScrollPane(sArea);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        LineNumbers ln = new LineNumbers(sArea, jsp);
		jsp.setRowHeaderView(ln);
		
		this.add(toolBar,BorderLayout.NORTH);
		this.add(jsp,BorderLayout.CENTER);
		this.add(footer,BorderLayout.SOUTH);
		
		this.setSize(new Dimension(600,800));
	}
	
	/**
	 * Set title of the editor, which will be the shortend filename. 
	 */
    public void setTitle(String title) {
		int length,cc=1;
		if (System.getProperty("os.name").startsWith("Win")) {
			super.setTitle(title==null?"Editor":title);
			return;
		}
		try {
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
		} catch (Exception e) {
			super.setTitle(title==null?"Editor":title);
			return;
		}
		boolean next = true;
		while (length > this.getWidth()-100 && next) {
			StringTokenizer st = new StringTokenizer(title,File.separator);
			int i = st.countTokens();
			if (!JGRPrefs.isMac) title = st.nextElement()+""+File.separator;
			else title = File.separator;
			if (cc > i) {
				for (int z = 1; z< i && st.hasMoreTokens(); z++)
					st.nextToken();
				if (st.hasMoreTokens()) title = st.nextToken();
				next = false;
			}
			else {
				for (int z = 1; z <= i && st.hasMoreTokens(); z++) {
					if (z <= i/2 - (cc - cc/2) || z > i/2 + cc /2 )
						title += st.nextToken()+""+(st.hasMoreTokens()?File.separator:"");
					else {
						title += "..."+File.separator;
						st.nextToken();
					}
				}
				next = true;
			}
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
			cc++;
		}
		super.setTitle(title);
    }
	
	public static void main(String[] args) {
		EditorPreferences.initialize();
		new Editor().setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		System.out.println("ActionCommand: "+cmd);
		
		if ("cut".equalsIgnoreCase(cmd)) sArea.cut();
		if ("copy".equalsIgnoreCase(cmd)) sArea.copy();
		if ("paste".equalsIgnoreCase(cmd)) sArea.paste();
		
		if ("undo".equalsIgnoreCase(cmd)) {
            try {
                if (toolBar.undoMgr.canUndo())
                    toolBar.undoMgr.undo();
            } catch (CannotUndoException ex) { /***/ }
        }
		
		if ("redo".equalsIgnoreCase(cmd)) {
            try {
                if (toolBar.undoMgr.canRedo())
                    toolBar.undoMgr.redo();
            } catch (CannotUndoException ex) { /***/ }
        }
		
        if ("recent-clear".equalsIgnoreCase(cmd)) {
            if (recentOpen!=null && recentMenu!=null) {
                recentMenu.removeAll();
                recentMenu.addSeparator();
                JMenuItem ca=new JMenuItem("Clear list");
                ca.setActionCommand("recent-clear");
                ca.addActionListener(this);
                ca.setEnabled(false);
                recentMenu.add(ca);
                recentOpen.reset();
            }
        }
	}
}
