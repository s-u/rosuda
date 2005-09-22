package org.rosuda.JClaR;
import java.util.Vector;


public interface Classifier {

    SVMSnapshotIF createSnapshot();

    double getAccuracy();
    
    Vector getClassNames();

    int[] getConfusionMatrix();

    Data getData();

    String getName();

    Plot getPlot();

    String getVariableName();

    int getVariablePos();
    
    boolean isReady();

    Data predict(Data newdata);

    void remove(boolean removeInData);

    void restoreSnapshot(SVMSnapshotIF snapIF);

    void setData(Data data, int variablePos);

    void setPlot(Plot plot);

    void show();
    
    String getRname();
    
    String classify(Data dataset);

}
