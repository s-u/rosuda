package org.rosuda.JGR.data;

import java.awt.BorderLayout;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;

import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;

import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.util.*;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class DataFrameList extends javax.swing.JPanel {
	public JList dataList;


	
	public DataFrameList() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			RController.refreshObjects();
			this.setPreferredSize(new java.awt.Dimension(169, 174));
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			this.setBorder(BorderFactory.createTitledBorder("Data Frames"));
			ListModel dataListModel = 
					new DefaultComboBoxModel(JGR.DATA);
			dataList = new JList();
			this.add(dataList, BorderLayout.CENTER);
			dataList.setModel(dataListModel);

		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}
	public void setMultipleSelection(boolean mult){
		if(mult)
			dataList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);			
		else
			dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	public RObject getSelectedValue(){
		return (RObject) dataList.getSelectedValue();
	}

	public RObject[] getMultSelection(){
		return (RObject[]) dataList.getSelectedValues();
	}
}
