/*
 * MFrame.java
 *
 * Created on 12. August 2005, 15:56
 *
 */

package org.rosuda.JClaR;
import java.awt.event.ComponentEvent;
import javax.swing.JFrame;

/**
 * JFrame doesn't enforce its minimum size. This is a workaround.
 * Should be removed as soon as that bug is fixed.
 * See {@link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4320050}.
 * @author tobias
 */
public class MFrame {
    
    /**
     * Taken from {@link http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4320050}.
     */
    private static void lockInMinSize(final JFrame frame) {
        //Ensures user cannot resize frame to be smaller than frame is right now.
        final int origX = frame.getSize().width;
        final int origY = frame.getSize().height;
        frame.addComponentListener(new
                java.awt.event.ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                frame.setSize(
                        (frame.getWidth() < origX) ? origX :
                            frame.getWidth(),
                        (frame.getHeight() < origY) ? origY :
                            frame.getHeight());
                frame.repaint();
            }
        });
    }
}
