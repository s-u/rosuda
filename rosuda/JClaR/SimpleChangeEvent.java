/*
 * SimpleChangeEvent.java
 *
 * Created on 12. Juli 2005, 17:13
 *
 */

package org.rosuda.JClaR;

import java.util.EventObject;

/**
 *
 * @author tobias
 */
public final class SimpleChangeEvent extends EventObject {
    
    private int message;
    
    /** Creates a new instance of SimpleChangeEvent */
    public SimpleChangeEvent(final Object source, final int message) {
        super(source);
        this.message = message;
    }
    
    public int getMessage(){
        return message;
    }
    
    public static final int HARD_CHANGE = 1;
    public static final int UPDATE = 2;
}
