package org.rosuda.JGR.toolkit;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;

/** Simple text pager that displays a specified file.
    @version $Id$
*/
public class TextPager extends iFrame {
    JTextArea t;
    
    public TextPager(String file, String header, String title, boolean deleteFile) {
	super(title,clsHelp);
	t=new JTextArea();
	getContentPane().add(new JScrollPane(t));
        t.setEditable(false); t.setFont(new Font("Monospaced",Font.PLAIN,10));
        FontTracker.current.add(t);
	t.setBackground(Color.white);
	setSize(300,400);
        try {
            BufferedReader r=new BufferedReader(new FileReader(file));
            while (r.ready()) { t.append(r.readLine()); t.append("\n"); }
            r.close(); r=null;
            if (deleteFile) new File(file).delete();
        } catch (Exception e) {
            t.append("Unable to open file \""+file+"\": "+e.getMessage());
        }
	addWindowListener(Common.getDefaultWindowListener());
        setVisible(true);
    }
    
    public static void launchPager(String file, String header, String title, boolean deleteFile) {
        new TextPager(file, header, title, deleteFile);
    }
}
