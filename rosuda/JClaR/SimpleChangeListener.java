/*
 * SimpleChangeListener.java
 *
 * Created on 12. Juli 2005, 17:24
 *
 */

package org.rosuda.JClaR;

import java.util.EventListener;

/**
 *
 * @author tobias
 */
public interface SimpleChangeListener extends EventListener {
    void stateChanged(SimpleChangeEvent e);
}
