//
//  ImageSelection.java
//
//  Created by Simon Urbanek on Tue Jun 22 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
// $Id$

package org.rosuda.util;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

/** Represents an image stored on the clipboard - also provides some useful static functions for putting iamges on the clipbpard. */
public class ImageSelection implements Transferable {
    private Image image;

    /** creates a transferable conatining the supplied image */
    public ImageSelection(Image image) {
        this.image = image;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return image;
    }

    /** puts an image on the (system) clipboard - implicitly creates an instance of ImageSelection */
    public static ImageSelection setClipboard(Image image) {
        ImageSelection imgSel = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
        return imgSel;
    }

    public static ImageSelection copyComponent(Component c, boolean whiteBg, boolean antiAliased) {
        Dimension d = c.getSize();
        Image img = c.createImage(d.width, d.height);
        Graphics g = img.getGraphics();
        Graphics2D g2=(Graphics2D) g;
        if (antiAliased)
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (whiteBg) {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, d.width, d.height);
        }
        c.paint(g2);
        return setClipboard(img);
    }
}
