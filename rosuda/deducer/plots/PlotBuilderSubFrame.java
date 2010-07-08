package org.rosuda.deducer.plots;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.toolkit.OkayCancelPanel;
import org.rosuda.deducer.toolkit.SideWindow;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;




public class PlotBuilderSubFrame extends SideWindow implements ActionListener{
	private JPanel panel;
	private OkayCancelPanel okayCancel;
	
	private JPanel elementView;
	private PlottingElement element;
	private ElementModel initialModel;

	
	public PlotBuilderSubFrame(Window theParent) {
		super(theParent);
		initGUI();
		updateLocation();
		updateSize();
	}
	
	private void initGUI() {
		try {

			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			{
				okayCancel = new OkayCancelPanel(false,false,this);
				getContentPane().add(okayCancel, new AnchorConstraint(929, 17, 6, 504, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE));
				okayCancel.setPreferredSize(new java.awt.Dimension(200, 38));
			}
			pack();
			this.setSize(455, 554);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addActionListener(ActionListener l){
		okayCancel.addActionListener(l);
	}
	
	public void removeAllActionListeners(){
		okayCancel.removeAllActionListeners();
	}
	
	public void setElement(PlottingElement el){
		if(elementView!=null)
			getContentPane().remove(elementView);
		elementView = el.getPanel();
		element = el;
		initialModel = (ElementModel) el.getModel().clone();
		getContentPane().add(elementView, new AnchorConstraint(0, 1001, 44, 1, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL));
	}
	
	public void setToInitialModel(){
		element.setModel(initialModel);
	}

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd == "OK"){
			try{
			ElementView ev = (ElementView) elementView;
			ElementModel em = ev.getModel();
			String s = em.checkValid();
			if(s!=null){
				JOptionPane.showMessageDialog(this, s);
				return;
			}else{
				this.setVisible(false);
			}
			}catch(Exception e){e.printStackTrace();}
		}else{
			setToInitialModel();
			this.setVisible(false);			
		}
	}

}
