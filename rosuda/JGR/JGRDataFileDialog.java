package org.rosuda.JGR;

import java.awt.Frame;

/**
 * @author markus
 *
 * @deprecated use JGRDataFileOpenDialog
 */
public class JGRDataFileDialog {
    
    /**
     * Create a new DataFileDialog (Open)
     * @param f parent frame
     * @param directory current directory
     */
    public JGRDataFileDialog(Frame f,String directory) {
        new JGRDataFileOpenDialog(f, directory);
    }
}
