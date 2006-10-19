/*
 * SelectImageFilter.java
 *
 * Created on 7. September 2005, 14:30
 *
 */

package org.rosuda.JClaR;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;


/**
 *
 * @author tobias
 */
public final class SelectImageFilter extends RGBImageFilter {
    
    static Image createSelectedImage(final Image image){
        final SelectImageFilter filter = new SelectImageFilter();
        final ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
	final Image selectedImage = Toolkit.getDefaultToolkit().createImage(prod);
	return selectedImage;
    }

    public int filterRGB(final int x, final int y, final int rgb) {
        final Color c = (new Color(rgb)).darker();
        final float[] hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(), c.getBlue(),null);
        return (new Color(Color.HSBtoRGB(240, hsb[1], hsb[2]))).getRGB();
    }
    
}
