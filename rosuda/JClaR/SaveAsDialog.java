/*
 * SaveAsDialog.java
 *
 * Created on 13. Juni 2005, 15:01
 */

package org.rosuda.JClaR;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author tobias
 */
public final class SaveAsDialog extends JFileChooser {
    
    public boolean showSaveAsDialog(final int extensions){
        switch (extensions){
            case EXTENSIONS_PLOT:
                //XXX: png
                final ExtFileFilter filter = new ExtFileFilter();
                filter.addExtension("jpg");
                filter.addExtension("jpeg");
                filter.setDescription("Jpeg images");
                setFileFilter(filter);
                break;
            default:
                break;
        }
        while(true){
            final int returnVal = showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file = getSelectedFile();
                if(file.exists())
                {
                    switch(JOptionPane.showConfirmDialog(this, "File " + file.getAbsolutePath() + " already exists.\nOverwrite?")){
                        case JOptionPane.YES_OPTION:
                            return true;
                        case JOptionPane.CANCEL_OPTION:
                            return false;
                        default:
                            break;
                    }
                }
                else  {
                    return true;
                }
                
            }
            else  {
                return false;
            }
            
        }
    }
    
    public boolean showSaveAsDialog(){
        return showSaveAsDialog(EXTENSIONS_NONE);
    }
    
    public static final int EXTENSIONS_NONE = 0;
    public static final int EXTENSIONS_PLOT = 1;
    
}
