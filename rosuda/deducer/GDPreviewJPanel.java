package org.rosuda.deducer;


import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDBufferedPanel;

public class GDPreviewJPanel extends GDInterface{
	public JPanel p;
	public static JPanel mostRecent;
	public static Integer mostRecentDevNumber;
	
	public GDPreviewJPanel() {
        super();
    }
	public void gdOpen(double w, double h) {
		if (p!=null) gdClose();
		p=new JGDBufferedPanel(200,200);
		c=(GDContainer) p;
		mostRecent=p;
		
	}
	
	public void gdClose() {
		super.gdClose();
		if (p!=null) {
			c=null;
			p=null;
		}
    }
	
	public static void plot(String call){
		Deducer.rniEval("Sys.setenv(\"JAVAGD_CLASS_NAME\"=\"org/rosuda/deducer/GDPreviewJPanel\")");
		Deducer.rniEval("JavaGD()");
		mostRecentDevNumber = new Integer(Deducer.rniEval("as.integer(dev.cur())").asInt());
		
		String[] lines = call.split("\n");
		for(int i=0;i<lines.length;i++)
			Deducer.rniEval(lines[i]);
		Deducer.rniEval("Sys.setenv(\"JAVAGD_CLASS_NAME\"=\"org/rosuda/JGR/toolkit/JavaGD\")");
		
	}
	
}
