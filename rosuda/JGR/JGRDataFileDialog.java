/*
 * Created on Dec 5, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rosuda.JGR;

import java.awt.Frame;

/**
 * @author markus
 *
 * @deprecated use JGRDataFileOpenDialog
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JGRDataFileDialog {
    
    public JGRDataFileDialog(Frame f,String directory) {
        new JGRDataFileOpenDialog(f, directory);
    }
}
