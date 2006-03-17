package org.rosuda.JClaR;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;


public interface Classifier extends Serializable {

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

    void remove(boolean removeInData);

    void restoreSnapshot(SVMSnapshotIF snapIF);

    void setData(Data data, int variablePos);

    void setPlot(Plot plot);

    void show();
    
    String getRname();
    
    void classify(Data dataset);
    
    void reclassify();
    
    int getNumber();
    
    String getClassifiedDataFrame();
    
    boolean hasClassifiedData();
    
    Data getClassifiedData();

    double getAccuracyOfPrediction();

    boolean hasAccuracyOfPrediction();

}
