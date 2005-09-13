package org.rosuda.JClaR;


/**
 * Objects implementing this interface can be notified by {@link SelectionPanel}
 */
public interface SelectionModIF {
    
    void selectAll();
    void selectNothing();
    void invertSelection();
    
}
