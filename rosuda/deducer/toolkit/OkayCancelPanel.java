package org.rosuda.deducer.toolkit;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.JFrame;



public class OkayCancelPanel extends JPanel {
	private JButton okayButton;
	private JButton resetButton;
	private JButton cancelButton;

	
	public OkayCancelPanel(boolean showReset,boolean isRun,ActionListener lis) {
		super();
		initGUI(showReset?0:1);
		if(!showReset)
			resetButton.setVisible(false);
		if(isRun)
			okayButton.setText("Run");
		if(lis!=null){
			resetButton.addActionListener(lis);
			okayButton.addActionListener(lis);
			cancelButton.addActionListener(lis);
		}
	}
	
	public OkayCancelPanel(boolean showReset,boolean isRun) {
		super();
		initGUI(showReset?0:1);
		if(!showReset)
			resetButton.setVisible(false);
		if(isRun)
			okayButton.setText("Run");
	}
	
	public void addActionListener(ActionListener lis){
		if(lis!=null){
			resetButton.addActionListener(lis);
			okayButton.addActionListener(lis);
			cancelButton.addActionListener(lis);
		}		
	}
	
	private void initGUI(int reset) {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(365, 57));
			{
				resetButton = new JButton();
				this.add(resetButton, new AnchorConstraint(219, 310, 798, 9, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				resetButton.setText("Reset");
				resetButton.setPreferredSize(new java.awt.Dimension(110, 33));
			}
			{
				cancelButton = new JButton();
				this.add(cancelButton, new AnchorConstraint(219, 658-180*(reset), 798, 360-360*(reset), 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				
				cancelButton.setPreferredSize(new java.awt.Dimension(109, 33));
			}
			{
				okayButton = new JButton();
				this.add(okayButton, new AnchorConstraint(8, 1001, 1008, 702-180*reset, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okayButton.setText("OK");
				okayButton.setPreferredSize(new java.awt.Dimension(109, 57));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JButton getApproveButton(){
		return okayButton;
	}

}

