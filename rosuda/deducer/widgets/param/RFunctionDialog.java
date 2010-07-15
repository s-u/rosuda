package org.rosuda.deducer.widgets.param;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.toolkit.OkayCancelPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class RFunctionDialog extends javax.swing.JDialog implements ActionListener {
	private JPanel panel;
	private JPanel okayCancel;
	private JButton help;
	private RFunctionView view;
	private RFunction initialModel;
	private RFunction model;
	public RFunctionDialog(JFrame frame,RFunction el) {
		super(frame);
		initGUI();
		setModel(el);
	}
	
	public RFunctionDialog(RFunction el) {
		super();
		initGUI();
		setModel(el);
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				help = new JButton();
				getContentPane().add(help, new AnchorConstraint(923, 92, 12, 12, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS));
				help.setPreferredSize(new java.awt.Dimension(29, 26));
			}
			{
				okayCancel = new OkayCancelPanel(false,false,this);
				getContentPane().add(okayCancel, new AnchorConstraint(923, 21, 0, 521, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE));
				okayCancel.setPreferredSize(new java.awt.Dimension(195, 38));
			}
			{
				panel = new JPanel();
				BorderLayout panelLayout = new BorderLayout();
				panel.setLayout(panelLayout);
				getContentPane().add(panel, new AnchorConstraint(1, 994, 44, 1, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL));
				panel.setPreferredSize(new java.awt.Dimension(447, 449));
			}
			this.setSize(450, 515);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setModel(RFunction el){
		panel.removeAll();
		view = el.getView();
		panel.add(view);
		initialModel = (RFunction) el.clone();
	}
	public void setToInitialModel(){
		RFunction newModel = (RFunction) initialModel.clone();
		model.setName(newModel.getName());
		model.setParams(newModel.getParams());
		setModel(model);
	}
	
	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd == "OK"){
			view.updateModel();
			String s = view.getModel().checkValid();
			if(s!=null){
				JOptionPane.showMessageDialog(this, s);
			}else{
				this.dispose();
			}
		}else if(cmd == "Cancel"){
			setToInitialModel();
			this.dispose();
		}
	}
	
}
