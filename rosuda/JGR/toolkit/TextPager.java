package org.rosuda.JGR.toolkit;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import org.rosuda.ibase.*;

/** 
 * Simple text pager that displays a specified file.
 * 
 * @author Simon Urbanek
 * 
 * RoSuDA 2003 - 2005
 * 
 */
public class TextPager extends iFrame {
    JTextArea t;
    
    public TextPager(String file, String header, String title, boolean deleteFile) {
		super(title, clsHelp);
		t = new JTextArea();
		getContentPane().add(new JScrollPane(t));
		t.setEditable(false);
		t.setFont(new Font("Monospaced", Font.PLAIN, 10));
		t.setDragEnabled(true);
		FontTracker.current.add(t);
		t.setBackground(Color.white);
		setSize(400, 600);
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (r.ready()) {
				t.append(r.readLine());
				t.append("\n");
			}
			r.close();
			r = null;
			if (deleteFile)
				new File(file).delete();
		} catch (Exception e) {
			t.append("Unable to open file \"" + file + "\": " + e.getMessage());
		}
		addWindowListener(Common.getDefaultWindowListener());
		setVisible(true);
	}
    
    /**
	 * Launch textpager.
	 * 
	 * @param file file to show
	 * @param header header
	 * @param title title for pager
	 * @param deleteFile delete file after displaying it
	 */
    public static void launchPager(String file, String header, String title, boolean deleteFile) {
        new TextPager(file, header, title, deleteFile);
    }
}
