/*
 * MainWindow.java
 *
 * Created on 25. September 2005, 14:05
 */

package org.rosuda.JClaR;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;


/**
 *
 * @author  tobias
 */
public class MainWindow extends javax.swing.JFrame implements DataClassifierListenerIF, ListSelectionListener {
    
    NonEditableTableModel dataTM, classifierTM;
    DefaultListSelectionModel dataSM, classifierSM;
    final Object[] columnNamesData = {"File", "#Variables", "Length"};
    final Object[] columnNamesClassifier = {"#", "Method"};
    
    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();
        
        dataTM = new NonEditableTableModel(columnNamesData, 0);
        tblDatasets.setModel(dataTM);
        
        classifierTM = new NonEditableTableModel(columnNamesClassifier, 0);
        tblClassifiers.setModel(classifierTM);
        
        dataSM = new DefaultListSelectionModel();
        tblDatasets.setSelectionModel(dataSM);
        dataSM.addListSelectionListener(this);
        
        classifierSM = new DefaultListSelectionModel();
        tblClassifiers.setSelectionModel(classifierSM);
        classifierSM.addListSelectionListener(this);
        
        DatasetManager.setListener(this);
        ClassifierManager.setListener(this);
    }
    
    private void train(Data data){
        final SVM svm;
        final ChooseVariableDialog cvd = new ChooseVariableDialog(null,data.getVariables());
        cvd.setText("Please select the variable that contains the classes.");
        cvd.show();
        final int variablePos = cvd.getVariable();
        if (variablePos>-1){
            int[] unused = cvd.getUnusedVariables();
            final Data preparedData;
            if (unused!=null && unused.length>0){
                preparedData = data.getRestrictedData(cvd.getUnusedVariables());
            } else {
                preparedData = data;
            }
            preparedData.unclass(variablePos);
            preparedData.refactor(variablePos);
            svm=new SVM(preparedData,variablePos);
            if (svm!=null){
                final ClassificationWindow window = new SVMWindow(svm);
                window.show();
            }
            ClassifierManager.addClassifier(svm);
        }
    }
    
    private void classify(Data data, Classifier classifier){
        String result = classifier.classify(data);
        if(result!=null){
            DataFileSaveDialog dfsd = new DataFileSaveDialog(this, result, Main.getLast_directory());
        }
    }
    
    private Data openDataset(){
        //open data
        final Data data = new Data();
        
        DataFileOpenDialog dfod = new DataFileOpenDialog(new Frame(), Main.getLast_directory(), data.getRname());
        switch(dfod.getStatus()){
            case DataFileOpenDialog.STATUS_SUCCESS:
                
                File file = dfod.getSelectedFile();
                data.setFile(file);
                //TODO: check if dataset has been opened already and ask what to do.
                
                data.removeNAs();
                try{
                    data.update();
                } catch (NullPointerException ex){
                    ErrorDialog.show(null, "Error reading file " + data.getPath() + ".");
                    return null;
                }
                if(data.getNumberOfVariables()<2){
                    ErrorDialog.show(null,"Too few variables in dataset " + data.getPath() + ".");
                    return null;
                }
                break;
            case DataFileOpenDialog.STATUS_ERROR:
                ErrorDialog.show(null, "Error reading file " + dfod.getSelectedFile().getAbsolutePath() + ".");
                return null;
            case DataFileOpenDialog.STATUS_CANCELED:
            default: // dialog closed or canceled
                return null;
        }
        DatasetManager.addDataset(data);
        return data;
    }
    
    public void datasetsChanged() {
        Vector datasets = DatasetManager.getDataVector();
        Vector dataVector = new Vector(datasets.size());
        
        for(Enumeration en=datasets.elements(); en.hasMoreElements();){
            Vector v = new Vector(3);
            Data d = (Data)en.nextElement();
            v.add(d.getPath());
            v.add(new Integer(d.getNumberOfVariables()));
            v.add(new Integer(d.getLength()));
            dataVector.add(v);
        }
        
        dataTM.setDataVector(datasets, dataVector, columnNamesData);
    }

    public void classifiersChanged() {
        Vector classifiers = ClassifierManager.getClassifiers();
        Vector dataVector = new Vector(classifiers.size());
        
        for(Enumeration en=classifiers.elements(); en.hasMoreElements();){
            Vector v = new Vector(2);
            Classifier c = (Classifier)en.nextElement();
            v.add(new Integer(c.getNumber()));
            v.add("SVM");
            dataVector.add(v);
        }
        
        classifierTM.setDataVector(classifiers, dataVector, columnNamesClassifier);
    }
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        if(e.getSource().equals(dataSM)){
            if(tblDatasets.getSelectedRow()==-1){
                butClassify.setEnabled(false);
                butTrain.setEnabled(false);
            } else{
                if(tblClassifiers.getSelectedRow()!=-1 && tblClassifiers.getRowCount()!=0){
                    butClassify.setEnabled(true);
                }
                butTrain.setEnabled(true);
            }
        } else if(e.getSource().equals(classifierSM)){
            if(tblClassifiers.getSelectedRow()==-1){
                butClassify.setEnabled(false);
            } else{
                if(tblDatasets.getSelectedRow()!=-1 && tblDatasets.getRowCount()!=0){
                    butClassify.setEnabled(true);
                }
            }
        }
    }
    
    public final class NonEditableTableModel extends javax.swing.table.DefaultTableModel {
        private Vector objects;
        
        public NonEditableTableModel(Object[] columnNames, int rowCount){
            super(columnNames, rowCount);
        }
        
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public void setDataVector(Vector objects, Vector dataVector, Object[] columnIdentifiers){
            this.objects = objects;
            setDataVector(dataVector, convertToVector(columnIdentifiers));
        }
        
        public Object getObjectAt(int row){
            return objects.elementAt(row);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDatasets = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        butTrain = new javax.swing.JButton();
        butClassify = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblClassifiers = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        m_File = new javax.swing.JMenu();
        m_FileOpenDataset = new javax.swing.JMenuItem();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.X_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JClassifieR");
        jScrollPane1.setViewportView(tblDatasets);

        getContentPane().add(jScrollPane1);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));

        butTrain.setMnemonic('t');
        butTrain.setText("Train");
        butTrain.setEnabled(false);
        butTrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butTrainActionPerformed(evt);
            }
        });

        jPanel1.add(butTrain);

        butClassify.setMnemonic('c');
        butClassify.setText("Classify");
        butClassify.setEnabled(false);
        butClassify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butClassifyActionPerformed(evt);
            }
        });

        jPanel1.add(butClassify);

        getContentPane().add(jPanel1);

        jScrollPane2.setViewportView(tblClassifiers);

        getContentPane().add(jScrollPane2);

        m_File.setMnemonic('f');
        m_File.setText("File");
        m_FileOpenDataset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        m_FileOpenDataset.setMnemonic('o');
        m_FileOpenDataset.setText("Open dataset");
        m_FileOpenDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_FileOpenDatasetActionPerformed(evt);
            }
        });

        m_File.add(m_FileOpenDataset);

        jMenuBar1.add(m_File);

        setJMenuBar(jMenuBar1);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void butClassifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butClassifyActionPerformed
        classify((Data)dataTM.getObjectAt(tblDatasets.getSelectedRow()),
                (Classifier)classifierTM.getObjectAt(tblClassifiers.getSelectedRow()));
    }//GEN-LAST:event_butClassifyActionPerformed
    
    private void butTrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butTrainActionPerformed
        train((Data)dataTM.getObjectAt(tblDatasets.getSelectedRow()));
    }//GEN-LAST:event_butTrainActionPerformed
    
    private void m_FileOpenDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_FileOpenDatasetActionPerformed
        openDataset();
    }//GEN-LAST:event_m_FileOpenDatasetActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butClassify;
    private javax.swing.JButton butTrain;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenu m_File;
    private javax.swing.JMenuItem m_FileOpenDataset;
    private javax.swing.JTable tblClassifiers;
    private javax.swing.JTable tblDatasets;
    // End of variables declaration//GEN-END:variables
    
}
