package org.rosuda.JGR.toolkit;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.util.ErrorMsg;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.rosuda.JGR.JGR;


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
public class VariableSelectionDialog extends JDialog implements ActionListener {
	private VariableSelector selector;
	private JButton select;
	private JButton cancel;
	private ArrayList selectedVariables =new ArrayList();


	
	public VariableSelectionDialog(JFrame frame) {
		super(frame,true);
		initGUI();
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				select = new JButton();
				getContentPane().add(select, new AnchorConstraint(858, 892, 983, 556, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				select.setText("Select");
				select.setPreferredSize(new java.awt.Dimension(80, 35));
				select.addActionListener(this);
			}
			{
				cancel = new JButton();
				getContentPane().add(cancel, new AnchorConstraint(879, 439, 958, 128, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancel.setText("Cancel");
				cancel.setPreferredSize(new java.awt.Dimension(74, 22));
				cancel.addActionListener(this);
			}
			{
				selector = new VariableSelector();
				getContentPane().add(selector, new AnchorConstraint(40, 1002, 836, 2, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				selector.setPreferredSize(new java.awt.Dimension(238, 233));
				selector.setBorder(BorderFactory.createTitledBorder("Variables"));
			}
			this.setSize(300, 600);
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		try{
			String cmd = arg0.getActionCommand();
			if(cmd == "Cancel"){
				this.dispose();
			}else if(cmd == "Select"){
				Object[] sel =  selector.getJList().getSelectedValues();
				String data = (String)selector.getJComboBox().getSelectedItem();
				for(int i = 0;i<sel.length;i++){
					selectedVariables.add(data+"$"+sel[i]);
				}
				this.dispose();
			}
		}catch(Exception er){
			new ErrorMsg(er);
		}
	}
	
	
	public void SetSingleSelection(boolean selectMode){
		if(selectMode)
			selector.getJList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		else
			selector.getJList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	
	public void setComboBox(String dataName){
		selector.getJComboBox().setSelectedItem(dataName);
	}
	
	public List getSelectedVariables(){
		return selectedVariables;
	}
	
	public String getSelecteditem(){
		if(selectedVariables.size()>0)
			return (String) selectedVariables.get(0);
		else
			return null;
	}
	
	public void setRFilter(String rFunction){
		selector.setRFilter(rFunction);
	}
	
	
}
