/*
 * SnapshotContainer.java
 *
 * Created on 22. Juli 2005, 19:43
 *
 */

package org.rosuda.JClaR;

import javax.swing.ImageIcon;

/**
 *
 * @author tobias
 */
public final class SnapshotContainer {
    
    private SVMSnapshotIF svmSnap;
    private FixVariablesDialogSnapshotIF fvdSnap;
    
    /** Creates a new instance of SnapshotContainer */
    public SnapshotContainer(final SVMSnapshotIF svmSnap, final FixVariablesDialogSnapshotIF fvdSnap) {
        this.svmSnap = svmSnap;
        this.fvdSnap = fvdSnap;
    }
    
    public ImageIcon getThumbnail(){
        if(svmSnap!=null)  {
            return svmSnap.getThumbnail();
        }
        
        else  {
            return null;
        }
        
    }
    

    public FixVariablesDialogSnapshotIF getFvdSnap() {
        return this.fvdSnap;
    }

    public SVMSnapshotIF getSvmSnap() {
        return this.svmSnap;
    }
    
    public String getToolTipText(){
        if(svmSnap!=null)  {
            return svmSnap.getToolTipText();
        }
        
        else  {
            return null;
        }
        
    }
}
