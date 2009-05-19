package org.rosuda.deducer.menu;



import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import org.rosuda.JGR.*;
import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.util.ErrorMsg;

public class MergeDialog extends javax.swing.JDialog implements ActionListener{
	private JList dataList;
	private JLabel data1;
	private JLabel newDataLabel;
	private JTextField newName;
	private JList jList1;
	private JLabel data2;
	private JButton cancelButton;
	private JButton contButton;
	private static String lastSelected1;
	private static String lastSelected2;
	private static String lastNewData;

	
	public MergeDialog() {
		super();
		initGUI();
	}
	
	public MergeDialog(JFrame f) {
		super(f);
		initGUI();
	}
	
	private void initGUI() {
		try {
			RController.refreshObjects();
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			this.setTitle("Merge Data");
			{
				newName = new JTextField();
				getContentPane().add(newName, new AnchorConstraint(855, 428, 945, 29, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				if(lastNewData==null)
					newName.setText(JGR.MAINRCONSOLE.getUniqueName("data.merged"));
				else
					newName.setText(lastNewData);
				newName.setPreferredSize(new java.awt.Dimension(166, 22));
			}
			{
				newDataLabel = new JLabel();
				getContentPane().add(newDataLabel, new AnchorConstraint(766, 428, 827, 29, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				newDataLabel.setText("Merged Data Name:");
				newDataLabel.setPreferredSize(new java.awt.Dimension(166, 15));
			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton, new AnchorConstraint(863, 675, 953, 488, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				cancelButton.setPreferredSize(new java.awt.Dimension(78, 22));
				cancelButton.addActionListener(this);
			}
			{
				contButton = new JButton();
				getContentPane().add(contButton, new AnchorConstraint(827, 931, 977, 720, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				contButton.setText("Continue");
				contButton.setPreferredSize(new java.awt.Dimension(88, 37));
				contButton.setActionCommand("mergedata");
				contButton.addActionListener(this);
			}
			{
				data2 = new JLabel();
				getContentPane().add(data2, new AnchorConstraint(136, 972, 176, 569, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
				data2.setText("Select Second Data Frame");
				data2.setPreferredSize(new java.awt.Dimension(186, 15));
			}
			{
				ListModel jList1Model = 
					new DefaultComboBoxModel();
				for(int i=0;i<JGR.DATA.size();i++){
					((DefaultComboBoxModel)jList1Model).addElement(((RObject)JGR.DATA.elementAt(i)).getName());
				}
				jList1 = new JList();
				JScrollPane pane = new JScrollPane(jList1,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);				
				getContentPane().add(pane, new AnchorConstraint(197, 926, 72, 569, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE));
				jList1.setModel(jList1Model);
				jList1.setPreferredSize(new java.awt.Dimension(149, 126));
				jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			{
				data1 = new JLabel();
				getContentPane().add(data1, new AnchorConstraint(135, 444, 176, 29, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				data1.setText("Select First Data Frame:");
				data1.setPreferredSize(new java.awt.Dimension(173, 15));
			}
			{
				ListModel dataListModel = 
					new DefaultComboBoxModel();
				for(int i=0;i<JGR.DATA.size();i++){
					((DefaultComboBoxModel)dataListModel).addElement(((RObject)JGR.DATA.elementAt(i)).getName());
				}
				dataList = new JList();
				JScrollPane spane = new JScrollPane(dataList,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);	
				getContentPane().add(spane, new AnchorConstraint(197, 447, 72, 13, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS));
				dataList.setModel(dataListModel);
				dataList.setPreferredSize(new java.awt.Dimension(147, 126));
				dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			if(lastSelected1!=null && lastSelected2!=null){
				dataList.setSelectedValue(lastSelected1, true);
				jList1.setSelectedValue(lastSelected2, true);
			}
			pack();
			this.setSize(417, 268);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd= e.getActionCommand();
		if(cmd=="mergedata"){
			if(!newName.getText().startsWith("data.merged"))
				lastNewData =newName.getText();
			lastSelected1 = (String)dataList.getSelectedValue();
			lastSelected2 = (String)jList1.getSelectedValue();
			if(lastSelected1==null || lastSelected2==null || 
					lastSelected1.equals(lastSelected2)){
				JOptionPane.showMessageDialog(this,"Please Select Two Unique Data Frames to Merge");
				return;
			}
			MergeData inst = new MergeData(newName.getText(), lastSelected1,lastSelected2);
			inst.setLocationRelativeTo(this);
			inst.setVisible(true);
			this.dispose();
		}else if(cmd == "Cancel"){
			this.dispose();
		}
	}
}
