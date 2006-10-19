/*
 * Plot.java
 *
 * Created on 15. Mai 2005, 20:09
 */

package org.rosuda.JClaR;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.rosuda.JRclient.RFileInputStream;
import org.rosuda.JRclient.RSrvException;


/**
 *
 * @author tobias
 */
public abstract class Plot {
    private static final String PLOTFILE_PNG = "plot.png";
    
    private Classifier classifier;
    private String plotCall;
    double zoom = 1;
    private BufferedImage image;
    private static String plotFile = PLOTFILE_PNG;
    
    static final int DEVICE_NO = -1;
    static final int DEVICE_GDD = 0;
    private static final int DEVICE_PNG = 1;
    static final int DEVICE_JPG = 2;
    
    
    private static int device = DEVICE_GDD;
    
    Component parent;
    
    Plot(){}
    
    final BufferedImage plot(final Dimension dim){
        return plot(dim.width, dim.height);
    }
    
    final BufferedImage plot(final int width, final int height) {
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
                final RFileInputStream rfis = RserveConnection.openFile(plotFile);
                return (image=ImageIO.read(rfis));
            } else  {
                return null;
            }
            
        } catch(RSrvException rse) {
            ErrorDialog.show(parent,"Rserve exception in Plot.plot(int,int): "+rse.getMessage());
            return null;
        } catch(java.io.IOException e) {
            ErrorDialog.show(parent,"IOException in Plot.plot(int,int): "+e.getMessage());
            return null;
        } catch (java.lang.NullPointerException e){
            ErrorDialog.show(parent,"NullPointerException: " + e.getMessage());
            return null;
        }
    }
    
    final BufferedImage getImage(){
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
                ImageIO.write(getImage(),"jpg",file);
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
                plotFile = "plot.jpg";
                break;
        }
    }
}
