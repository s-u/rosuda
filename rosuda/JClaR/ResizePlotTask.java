/*
 * ResizePlotTask.java
 *
 * Created on 28. Juli 2005, 16:56
 *
 */

package org.rosuda.JClaR;

import java.util.TimerTask;

/**
 *
 * @author tobias
 */
public final class ResizePlotTask extends TimerTask {
    
    ClassificationWindow svmw;
    
    /** Creates a new instance of ResizePlotTask */
    public ResizePlotTask(final ClassificationWindow svmw) {
        this.svmw = svmw;
    }

    public void run() {
        if (svmw!=null)  {
            svmw.updatePlot(false,  ClassificationWindow.CHANGE_TYPE_RESIZE);
        }
        
    }
    
}
