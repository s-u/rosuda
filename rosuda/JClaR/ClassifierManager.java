/*
 * ClassifierManager.java
 *
 * Created on 25. September 2005, 16:25
 *
 */

package org.rosuda.JClaR;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author tobias
 */
public final class ClassifierManager {
    
    private static ArrayList classifiers;
    private static DataClassifierListenerIF listener;
    
    /** Creates a new instance of ClassifierManager */
    private ClassifierManager() {
    }
    
    static List getClassifiers(){
        if(classifiers==null) classifiers=new ArrayList();
        return classifiers;
    }
    
    static void addClassifier(final Classifier newClassifier){
        if(classifiers==null) classifiers=new ArrayList();
        classifiers.add(newClassifier);
        if(listener!=null) listener.classifiersChanged();
    }
    
    static void setListener(final DataClassifierListenerIF aListener) {
        listener = aListener;
    }
    
}
