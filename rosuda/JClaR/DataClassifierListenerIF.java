/*
 * DataClassifierListenerIF.java
 *
 * Created on 25. September 2005, 15:12
 *
 */

package org.rosuda.JClaR;

/**
 * @author tobias
 */
public interface DataClassifierListenerIF {
    void datasetsChanged();
    void classifiersChanged();
}
