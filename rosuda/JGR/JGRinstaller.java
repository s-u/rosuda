package org.rosuda.JGR;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.Vector;


import javax.naming.ldap.Control;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.rosuda.JRI.*;

/**
 *  JGRInstaller and updater: install nescessary R packages if they can not be found in current R version.
 *
 *	@author Markus Helbig
 *
 * 	RoSuDa 2003 - 2005
 */

public class JGRinstaller implements RMainLoopCallbacks {
	
	static String VERSION = "0.1";
	static int DEBUG = 1;
	
	Rengine R = null;
	
	JGRinstSync sync = new JGRinstSync();
	
	String packages = "";
	
	//Just for development use ... 
	String contriburl = "http://rosuda.org/R/nightly";
	// sonst String contriburl = null;
	JProgressBar p = null;
	JLabel l = null;
	
	/**
	 * Install JGR from the web CRAN or Rosuda whatever you want
	 * @param pkgs packages which should be installed: "JGR,"JavaGD",...
	 * @param url contriburl
	 */
	public JGRinstaller(String pkgs, String url) {
		this.packages = pkgs;
		if (url != null) this.contriburl = url;
		
		if (!System.getProperty("os.name").startsWith("Win")) {
			JFrame f = new JFrame("Installing JGR");
			p = new JProgressBar();
			p.setMinimumSize(new Dimension(400,25));
			p.setPreferredSize(new Dimension(400,25));
			p.setMaximumSize(new Dimension(400,25));
			p.setIndeterminate(true);
			l = new JLabel("Starting JGR installer");
			f.getContentPane().add(l,BorderLayout.NORTH);
			f.getContentPane().add(p,BorderLayout.SOUTH);
			f.setSize(new Dimension(400,(System.getProperty("os.name").startsWith("Mac")?70:40)));
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
			f.setLocation((screenSize.width-f.getSize().width)/2,(screenSize.height-f.getSize().height)/2);
			f.setResizable(false);
			f.setVisible(true);
			// It is not advisable to quit the installing process manually because your locking the R library directory
			/*f.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(1);
				}
			});*/
		}

		// let's preemptively load JRI - if we do it here, we can show an error
		// message
		try {
			System.loadLibrary("jri");
		} catch (UnsatisfiedLinkError e) {
			String libName = "libjri.so";
			JOptionPane.showMessageDialog(null,
					"Cannot find Java/R Interface (JRI) library",
					"Cannot find JRI library", JOptionPane.ERROR_MESSAGE);
			System.err.println("Cannot find JRI native library!\n");
			e.printStackTrace();
			System.exit(1);
		}

		if (!Rengine.versionCheck()) {
			JOptionPane.showMessageDialog(null,
					"Java/R Interface (JRI) library doesn't match this JGR version.\nPlease update JGR and JRI to the latest version.",
					"Version Mismatch", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
		R = new Rengine(new String[] {"--vanilla","--quiet"}, true, this);
		R.DEBUG = DEBUG;
		if (DEBUG > 0)
			System.out.println("Rengine created, waiting for R");
		if (!R.waitForR()) {
			System.out.println("Cannot load R");
			System.exit(1);
		}
		if (l != null) l.setText("Installing R packages: "+packages);
		//if (System.getProperty("os.name").startsWith("Mac") && contriburl != null)
		//	sync.triggerNotification("install.packages(c("+packages+"),contriburl=contrib.url(getOption(\"CRAN\"),type=\"mac.binary\"))");
		//else 
			sync.triggerNotification("install.packages(c("+packages+")"+(contriburl==null?"":",contriburl=\""+contriburl+"\"")+")");
		sync.triggerNotification("y");
		sync.triggerNotification("q('no')");
	}
	
	/**
	 * Print help message for JGRinstaller commandline tool.
	 */
	public static void printHelp() {
		System.out.println("JGR Installer " + VERSION);
		System.out.println("\nOptions:");
		System.out.println("\n\t--pkg=[packages separated by ,] Install packages from CRAN");
		System.out.println("\n\t--update Update JGR and it's support packages from CRAN");
		System.exit(0);
	}
	
	public static void main(String[] args) {
		String packages = "", contriburl = null;
		if (args.length < 1) {
			System.out.println("Error using JGR Installer.\n");
			printHelp();
		}
		else if (args[0].startsWith("--pkg=")) {
			StringTokenizer st = new StringTokenizer(args[0],",=");
			while (st.hasMoreTokens()) {
				String t = st.nextToken();
				if (!t.startsWith("--pkg")) {
					if (st.hasMoreTokens())
						packages += "\""+t+"\",";
					else 
						packages += "\""+t+"\"";
				}
			}
		}
		else if (args[0].startsWith("--update")) {
			packages = "\"JGR\",\"JavaGD\",\"rJava\",\"iplots\",\"iWidgets\"";
			if (args[0].startsWith("--update-devel") && !System.getProperty("os.name").startsWith("Win"))
				contriburl = "http://rosuda.org/R/nightly";
		}
		else {
			System.out.println("Error using JGR Installer.\n");
			printHelp();
		}
		if (DEBUG > 0)
			System.out.println("Packages to install: "+packages);
		new JGRinstaller(packages,contriburl);
	}

	public void rWriteConsole(Rengine re, String text) {
		if (DEBUG > 0)
			System.out.println("Rengine: "+text);
	}

	public void rBusy(Rengine re, int which) {
		/*if (p != null && !System.getProperty("os.name").startsWith("Win")) {
			p.setIndeterminate(which==0?false:true);
		}*/
	}

	//---------------R Loopbacks
	
	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		if (DEBUG > 0)
			System.out.print(prompt);
		String s = sync.waitForNotification();
		if (DEBUG > 0)
			System.out.println(s);
		return s==null?"\n":s.trim()+"\n";
	}
	
	public void rShowMessage(Rengine re, String message) {
	}

	public String rChooseFile(Rengine re, int newFile) {
		return null;
	}

	public void rFlushConsole(Rengine re) {
	}

	public void rSaveHistory(Rengine re, String filename) {
	}

	public void rLoadHistory(Rengine re, String filename) {
	}
	
	class JGRinstSync {
	    Vector msgs;
	    
	    public JGRinstSync() {
	        msgs=new Vector();
	    }

	    private boolean notificationArrived=false;

	    /** this internal method waits until {@link #triggerNotification} is called by another thread. It is implemented by using {@link wait()} and checking {@link notificationArrived}. */
	    public synchronized String waitForNotification() {
	        while (!notificationArrived) {
	            try {
	                wait(100);
					if (R!=null) R.rniIdle();
	            } catch (InterruptedException e) {
	            }
	        }
	        String s=null;
	        if (msgs.size()>0) {
	            s=(String)msgs.elementAt(0);
	            msgs.removeElementAt(0);
	        }
	        if (msgs.size()==0)
	            notificationArrived=false;
	        return s;
	    }

	    /** this methods awakens {@link #waitForNotification}. It is implemented by setting {@link #notificationArrived} to <code>true</code>, setting {@link #lastNotificationMessage} to the passed message and finally calling {@link notifyAll()}. */
	    public synchronized void triggerNotification(String msg) {
	        notificationArrived=true;
	        msgs.addElement(msg);
	        notifyAll();
	    }
	}
}

