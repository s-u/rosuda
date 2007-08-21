/*
 * Plot.java
 *
 * Created on 15. Mai 2005, 20:09
 */

package org.rosuda.JClaR;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public abstract class Plot {
    private static final String PLOTFILE_PNG = "plot.png";
    private static final String PLOTFILE_JPG = "plot.jpg";
    
    private Classifier classifier;
    private String plotCall;
    double zoom = 1;
    private Image image;
    private static String plotFile = PLOTFILE_PNG;
    
    static final int DEVICE_NO = -1;
    static final int DEVICE_GDD = 0;
    private static final int DEVICE_PNG = 1;
    static final int DEVICE_JPG = 2;
    
    private int width,height;
    
    
    private static int device = DEVICE_GDD;
    
    Component parent;
    
    Plot(){}
    
    final Image plot(final Dimension dim){
	width=dim.width;
	height=dim.height;
	return plot(width, height);
    }
    
    final Image plot(final int width, final int height) {
	this.width=width;
	this.height=height;
	if(device==DEVICE_NO) return null;
	try{
	    final String widthOpt = ",w="+width;
	    final String heightOpt = ",h="+height;
	    
	    switch(device){
		case DEVICE_GDD:
		    RserveConnection.voidEval("GDD(file='" + plotFile + "',type='png'" + widthOpt + heightOpt + ")");
		    break;
		case DEVICE_JPG:
		    RserveConnection.voidEval("jpeg(filename='" + plotFile + "', quality=100" + widthOpt + heightOpt + ")");
		    break;
		case DEVICE_PNG:
		    RserveConnection.voidEval("png(filename='" + plotFile + "'" + widthOpt + heightOpt + ")");
		    break;
	    }
	    
	    if(plotCall!=null){
		RserveConnection.voidEval(plotCall);
		RserveConnection.voidEval("dev.off()");
		byte[] plotByte = RserveConnection.readFile(plotFile);
		return (image=Toolkit.getDefaultToolkit().createImage(plotByte));
	    } else  {
		return null;
	    }
	    
	} catch(RSrvException rse) {
	    ErrorDialog.show(parent,"Rserve exception in Plot.plot(int,int): "+rse.getMessage());
	    return null;
	} catch (java.lang.NullPointerException e){
	    ErrorDialog.show(parent,"NullPointerException: " + e.getMessage());
	    return null;
	}
    }
    
    final Image getImage(){
	return image;
    }
    
    final void setClassifier(final Classifier cl) {
	this.classifier = cl;
    }
    
    final void setPlotCall(final String plotCall) {
	this.plotCall = plotCall;
    }
    
    private final String getPlotCall(){
	return plotCall;
    }
    
    void setZoom(final double zoom){
	this.zoom = zoom;
    }
    
    final void saveAs(final File file){
	if(!file.canWrite())  {
	    ErrorDialog.show(parent,"Can't write to this file.");
	}
	
	else {
	    try{
		final BufferedImage bi = new BufferedImage(width,height,
			BufferedImage.TYPE_INT_RGB);
		final Graphics g = bi.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		ImageIO.write(bi,"jpg",file);
	    } catch(java.io.IOException e){
		ErrorDialog.show(parent,"An error occured while saving the plot: " + e.getMessage());
	    }
	}
	
    }
    
    abstract void setShowDataInPlot(boolean showDataInPlot);
    abstract boolean getShowDataInPlot();
    abstract void setVerticalShift(double shift);
    abstract void setHorizontalShift(double shift);
    
    private final Classifier getClassifier() {
	return classifier;
    }
    
    private static final int getDevice() {
	return device;
    }
    
    static final void setDevice(final int aDevice) {
	device = aDevice;
	switch(device){
	    case DEVICE_GDD:
	    case DEVICE_PNG:
		plotFile = PLOTFILE_PNG;
		break;
	    case DEVICE_JPG:
		plotFile = PLOTFILE_JPG;
		break;
	}
    }
}
