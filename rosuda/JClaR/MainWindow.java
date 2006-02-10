/*
 * MainWindow.java
 *
 * Created on 25. September 2005, 14:05
 */

package org.rosuda.JClaR;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionListener;


/**
 *
 * @author  tobias
 */
public class MainWindow extends javax.swing.JFrame implements DataClassifierListenerIF, ListSelectionListener {
    
    private NonEditableTableModel dataTM, classifierTM;
    private DefaultListSelectionModel dataSM, classifierSM;
    private final Object[] columnNamesData = {"File", "#Variables", "Length"};
    private final Object[] columnNamesClassifier = {"#", "Method"};
    
    private Point popupMenuPoint;
    
    /** Creates new form MainWindow */
    MainWindow() {
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
        classifier.classify(data);
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
    
    private final class NonEditableTableModel extends javax.swing.table.DefaultTableModel {
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
        popm_classifiers = new javax.swing.JPopupMenu();
        m_openClassificationWindow = new javax.swing.JMenuItem();
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
        m_FileOpenModel = new javax.swing.JMenuItem();
        m_FileExit = new javax.swing.JMenuItem();

        m_openClassificationWindow.setText("Item");
        m_openClassificationWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_openClassificationWindowActionPerformed(evt);
            }
        });

        popm_classifiers.add(m_openClassificationWindow);

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

        tblClassifiers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblClassifiersMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblClassifiersMouseReleased(evt);
            }
        });

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

        m_FileOpenModel.setText("Open classification model...");
        m_FileOpenModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_FileOpenModelActionPerformed(evt);
            }
        });

        m_File.add(m_FileOpenModel);

        m_FileExit.setMnemonic('x');
        m_FileExit.setText("Exit");
        m_FileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_FileExitActionPerformed(evt);
            }
        });

        m_File.add(m_FileExit);

        jMenuBar1.add(m_File);

        setJMenuBar(jMenuBar1);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void m_openClassificationWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_openClassificationWindowActionPerformed
        int row=tblClassifiers.rowAtPoint(popupMenuPoint);
        if(row==-1) return;
        
        Classifier c = (Classifier)classifierTM.getObjectAt(row);
        if(c!=null) c.show();
    }//GEN-LAST:event_m_openClassificationWindowActionPerformed

    private void tblClassifiersMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClassifiersMousePressed
        if(evt.isPopupTrigger()) {
            popupMenuPoint = new Point(evt.getX(),evt.getY());
            popm_classifiers.show(tblClassifiers, popupMenuPoint.x,popupMenuPoint.y);
        }
    }//GEN-LAST:event_tblClassifiersMousePressed

    private void tblClassifiersMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClassifiersMouseReleased
        if(evt.isPopupTrigger()) {
            popupMenuPoint = new Point(evt.getX(),evt.getY());
            popm_classifiers.show(tblClassifiers, popupMenuPoint.x,popupMenuPoint.y);
        }
    }//GEN-LAST:event_tblClassifiersMouseReleased
    
    private void m_FileOpenModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_FileOpenModelActionPerformed
        JFileChooser jfc = new JFileChooser(Main.getLast_directory());
        int status = jfc.showOpenDialog(this);
        File file=null;
        if(status==jfc.ERROR_OPTION){
            ErrorDialog.show(null, "An error occured with the file open dialog.");
        } else if(status==jfc.CANCEL_OPTION){
            // do nothing if canceled
        } else {
            file = jfc.getSelectedFile();
        }
        
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Classifier c = (Classifier)ois.readObject();
            if(c!=null) ClassifierManager.addClassifier(c);

        } catch(IOException e){
            ErrorDialog.show(this, "Error reading model: " + e.getMessage());
        } catch(ClassNotFoundException e){
            ErrorDialog.show(this, "Error reading model: " + e.getMessage());
        }
    }//GEN-LAST:event_m_FileOpenModelActionPerformed
    
    private void m_FileExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_FileExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_m_FileExitActionPerformed
    
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
    private javax.swing.JMenuItem m_FileExit;
    private javax.swing.JMenuItem m_FileOpenDataset;
    private javax.swing.JMenuItem m_FileOpenModel;
    private javax.swing.JMenuItem m_openClassificationWindow;
    private javax.swing.JPopupMenu popm_classifiers;
    private javax.swing.JTable tblClassifiers;
    private javax.swing.JTable tblDatasets;
    // End of variables declaration//GEN-END:variables
    
}
