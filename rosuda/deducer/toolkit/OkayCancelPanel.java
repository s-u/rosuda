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

	/**
	* Auto-generated main method to display this 
	* JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new OkayCancelPanel(true,true,null));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public OkayCancelPanel(boolean showReset,boolean isRun,ActionListener lis) {
		super();
		initGUI();
		if(!showReset)
			resetButton.setVisible(false);
		if(isRun)
			okayButton.setText("Run");
		resetButton.addActionListener(lis);
		okayButton.addActionListener(lis);
		cancelButton.addActionListener(lis);
	}
	
	private void initGUI() {
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
				this.add(cancelButton, new AnchorConstraint(219, 658, 798, 360, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancelButton.setText("Cancel");
				
				cancelButton.setPreferredSize(new java.awt.Dimension(109, 33));
			}
			{
				okayButton = new JButton();
				this.add(okayButton, new AnchorConstraint(8, 1001, 1008, 702, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okayButton.setText("OK");
				okayButton.setPreferredSize(new java.awt.Dimension(109, 57));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

