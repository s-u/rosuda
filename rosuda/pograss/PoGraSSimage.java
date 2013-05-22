package org.rosuda.pograss;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

/** PoGraSS representation of a bitmap image 
 @version $Id: $
 */
public class PoGraSSimage {
	Image img;
	
	public PoGraSSimage(String filename) throws IOException {
		img = javax.imageio.ImageIO.read(new File(filename));
	}

	public PoGraSSimage(File file) throws IOException {
		img = javax.imageio.ImageIO.read(file);
	}

	public PoGraSSimage(java.io.InputStream stream) throws IOException {
		img = javax.imageio.ImageIO.read(stream);
	}
	
	/** Get the underlying graphics image representation */	 
	protected Image getImage() {
		return img;
	}
}
