/*
 * SnapshotPanel.java
 *
 * Created on 27. Juli 2005, 14:40
 */

package org.rosuda.JClaR;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ImageIcon;

/**
 *
 * @author  tobias
 */
public final class SnapshotPanel extends javax.swing.JPanel {
    /**
     * FIXME: serialVersionUID field auto-generated by RefactorIT
     */
    static final long serialVersionUID = 200602271310L;
    
    private final SnapshotListModel slm;    
    private int currentSnapshot;
    
    /** Creates new form SnapshotPanel */
    SnapshotPanel() {
        initComponents();
        
        slm = new SnapshotListModel();
        lstSnapshots.setModel(slm);
    }
    
    void addSnapshot(final SnapshotContainer snapC){
        slm.addSnapshot(snapC);
        currentSnapshot=0;
    }
    
    SnapshotContainer getSelectedSnapshot(){
        final int index;
        if ((index=lstSnapshots.getSelectedIndex())!=-1) {
            return slm.getSnapshotAt(index);
        }
        
        else  {
            return null;
        }
        
    }
    
    void  addRestoreActionListener(final ActionListener l){
        butRestore.addActionListener(l);
    }
    
    
    private final class SnapshotList extends javax.swing.JList {
        /**
         * FIXME: serialVersionUID field auto-generated by RefactorIT
         */
        static final long serialVersionUID = 200602271310L;
        public String getToolTipText(final java.awt.event.MouseEvent event) {
            return slm.getSnapshotAt(locationToIndex(event.getPoint())).getToolTipText();
        }
    }
    
    private final class SnapshotListModel extends javax.swing.AbstractListModel {
        /**
         * FIXME: serialVersionUID field auto-generated by RefactorIT
         */
        static final long serialVersionUID = 200602271310L;
        private ArrayList<SnapshotContainer> snapshots=new ArrayList<SnapshotContainer>();

        private Hashtable<SnapshotContainer,ImageIcon> cache=new Hashtable<SnapshotContainer,ImageIcon>();
        
        public void addSnapshot(final SnapshotContainer snapC){
            snapshots.add(0,snapC);
            fireContentsChanged(this, getSize()-2,getSize()-1);
        }
        
        public Object getElementAt(final int index){
            ImageIcon ret=null;
            if (lstSnapshots.isSelectedIndex(index)){
                if((ret=cache.get(snapshots.get(index)))==null){
                    ret = new ImageIcon(SelectImageFilter.createSelectedImage(((SnapshotContainer)snapshots.get(index)).getThumbnail().getImage()));
                    cache.put(snapshots.get(index), ret);
                }
            } else{
                ret = ((SnapshotContainer)snapshots.get(index)).getThumbnail();
            }
            if(index == currentSnapshot){
                final ImageIcon ii = (ImageIcon)ret;
                final BufferedImage bi = new BufferedImage(ii.getIconWidth(),ii.getIconHeight(),BufferedImage.TYPE_INT_RGB);
                final Graphics g = bi.getGraphics();
                g.drawImage(ii.getImage(),0,0,null);
                g.setColor(Color.black);
                g.fillRect(0,0, 5, 5);
                ret = new ImageIcon(bi);
            }
            return ret;
        }
        
        public int getSize(){
            if (snapshots!=null)  {
                return snapshots.size();
            }
            
            else  {
                return 0;
            }
            
        }
        
        public SnapshotContainer getSnapshotAt(final int index){
            return (SnapshotContainer)snapshots.get(index);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        butRestore = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstSnapshots = new SnapshotList();

        setLayout(new java.awt.BorderLayout());

        setMaximumSize(new java.awt.Dimension(135, 32767));
        setMinimumSize(new java.awt.Dimension(131, 57));
        setPreferredSize(new java.awt.Dimension(135, 10));
        butRestore.setText("Restore");
        butRestore.addActionListener(new java.awt.event.ActionListener() {
            public final void actionPerformed(final java.awt.event.ActionEvent evt) {
                butRestoreActionPerformed(evt);
            }
        });

        jPanel1.add(butRestore);

        add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        lstSnapshots.setMinimumSize(new java.awt.Dimension(120, 0));
        jScrollPane2.setViewportView(lstSnapshots);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents

    private void butRestoreActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRestoreActionPerformed
        currentSnapshot=lstSnapshots.getSelectedIndex();
        lstSnapshots.repaint();
    }//GEN-LAST:event_butRestoreActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butRestore;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lstSnapshots;
    // End of variables declaration//GEN-END:variables
    
}
