package org.rosuda.JGR.menu;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.JGR.toolkit.DJList;
import org.rosuda.JGR.toolkit.IconButton;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

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
public class FactorDialog extends JDialog implements ActionListener {
	private JPanel listPanel;
	private JScrollPane levelScroller;
	private DJList levelList;
	private JCheckBox ordered;
	private JButton cancel;
	private JButton okay;
	private JButton contrast;
	private IconButton remove;
	private IconButton down;
	private IconButton up;
	
	private String variable;

	
	public FactorDialog(JFrame frame, String var) {
		super(frame);
		variable = var;
		initGUI();
		
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(280, 331));
			this.setTitle("Edit Factor");
			{
				cancel = new JButton();
				AnchorLayout cancelLayout = new AnchorLayout();
				cancel.setLayout(cancelLayout);
				getContentPane().add(cancel, new AnchorConstraint(836, 630, 920, 373, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				cancel.setText("Cancel");
				cancel.setPreferredSize(new java.awt.Dimension(72, 26));
				cancel.addActionListener(this);
			}
			{
				okay = new JButton();
				getContentPane().add(okay, new AnchorConstraint(810, 937, 946, 694, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				okay.setText("OK");
				okay.setPreferredSize(new java.awt.Dimension(68, 42));
				okay.addActionListener(this);
			}
			{
				ordered = new JCheckBox();
				getContentPane().add(ordered, new AnchorConstraint(690, 769, 752, 123, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				ordered.setText("Ordered");
				ordered.setPreferredSize(new java.awt.Dimension(92, 19));
				ordered.addActionListener(this);
				ordered.setSelected(JGR.R.eval("is.ordered("+variable+")").asBool().isTRUE());
			}
			{
				contrast = new JButton();
				getContentPane().add(contrast, new AnchorConstraint(674, 373, 771, 12, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				contrast.setText("Contrasts");
				contrast.setPreferredSize(new java.awt.Dimension(92, 30));
				contrast.addActionListener(this);
				contrast.setVisible(false);
			}
			{
				remove = new IconButton("/icons/button_cancel_32.png","Delete",this,"Delete");
				getContentPane().add(remove, new AnchorConstraint(161, 937, 655, 769, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				remove.setPreferredSize(new java.awt.Dimension(47, 41));
			}
			{
				down = new IconButton("/icons/1downarrow_32.png","Down",this,"Down");
				getContentPane().add(down, new AnchorConstraint(70, 937, 412, 769, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				down.setPreferredSize(new java.awt.Dimension(47, 41));
			}
			{
				up = new IconButton("/icons/1uparrow_32.png","Up",this,"Up");
				getContentPane().add(up, new AnchorConstraint(39, 937, 260, 769, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				up.setPreferredSize(new java.awt.Dimension(47, 41));
			}
			{
				listPanel = new JPanel();
				getContentPane().add(listPanel, new AnchorConstraint(40, 748, 655, 12, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				BorderLayout listPanelLayout = new BorderLayout();
				listPanel.setLayout(listPanelLayout);
				listPanel.setBorder(BorderFactory.createTitledBorder("Levels"));
				listPanel.setPreferredSize(new java.awt.Dimension(197, 190));
				{
					levelScroller = new JScrollPane();
					listPanel.add(levelScroller, BorderLayout.CENTER);
					{
						DefaultListModel levelListModel = new DefaultListModel();
						String[] levels = JGR.R.eval("levels("+variable+")").asStringArray();
						for(int i=0;i<levels.length;i++)
							levelListModel.addElement(levels[i]);
						levelList = new DJList();
						levelScroller.setViewportView(levelList);
						levelList.setModel(levelListModel);
					}
				}
			}
			this.setSize(280, 331);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd=="Up"){
			int[] ind= levelList.getSelectedIndices();
			if(ind.length>1){
				levelList.setSelectedIndex(ind[0]);
			}else if(ind.length == 1 && ind[0]>0){
				DefaultListModel model = (DefaultListModel)levelList.getModel();
				Object obj =model.remove(ind[0]);
				model.add(ind[0]-1, obj);
				levelList.setSelectedIndex(ind[0]-1);
			}
		}else if(cmd=="Down"){
			int[] ind= levelList.getSelectedIndices();
			if(ind.length>1){
				levelList.setSelectedIndex(ind[0]);
			}else if(ind.length == 1 && ind[0]<(levelList.getModel().getSize()-1)){
				DefaultListModel model = (DefaultListModel)levelList.getModel();
				Object obj =model.remove(ind[0]);
				model.add(ind[0]+1, obj);
				levelList.setSelectedIndex(ind[0]+1);
			}
		}else if(cmd == "Delete"){
			int[] ind= levelList.getSelectedIndices();
			if(ind.length>0){
				DefaultListModel model = (DefaultListModel)levelList.getModel();
				for(int i=ind.length-1;i>=0;i--){
					model.remove(ind[i]);
				}
			}
		}else if(cmd == "Cancel"){
			this.dispose();
		}else if(cmd == "OK"){
			ArrayList newLevels = new ArrayList();
			DefaultListModel model = (DefaultListModel)levelList.getModel();
			for(int i=0;i<model.getSize();i++){
				newLevels.add(model.get(i));
			}
			String rLevels = RController.makeRStringVector(newLevels);
			String order;
			if(ordered.isSelected())
				order="TRUE";	
			else
				order="FALSE";	
			JGR.MAINRCONSOLE.executeLater(variable+"<-factor("+variable+
					",levels="+rLevels+",ordered="+order+")");
			this.dispose();
		}
		
	}

}
