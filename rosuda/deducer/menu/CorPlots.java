package org.rosuda.deducer.menu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.rosuda.deducer.toolkit.OkayCancelPanel;


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
public class CorPlots extends JDialog implements ActionListener{
	private JPanel optionPanel;
	private JRadioButton scatterArray;
	private JComboBox scatterArrayLine;
	private JSeparator sep;
	private JPanel matrixPanel;
	private JRadioButton matrix;
	private JSeparator sep1;
	private OkayCancelPanel okayCancelPanel;
	private JRadioButton circles;
	private JRadioButton none;
	private ButtonGroup buttonGroup;
	private JRadioButton ellipse;
	private JLabel lineLabel;
	private JCheckBox scatterArrayCommon;
	
	private CorModel.Plots model;

	
	public CorPlots(JDialog d,CorModel.Plots mod) {
		super(d);
		initGUI();
		okayCancelPanel.addActionListener(this);
		setModel(mod);
		ellipse.setEnabled(false);
		circles.setEnabled(false);
		matrix.setEnabled(false);
	}
	
	private void initGUI() {
		try {
			{
				getContentPane().setLayout(null);
				{
					optionPanel = new JPanel();
					getContentPane().add(optionPanel);
					optionPanel.setBounds(12, 12, 372, 195);
					optionPanel.setBorder(BorderFactory.createTitledBorder("Correlation Arrays"));
					optionPanel.setLayout(null);
					{
						scatterArray = new JRadioButton();
						optionPanel.add(scatterArray);
						scatterArray.setText("Scatter Plots");
						scatterArray.setBounds(17, 32, 120, 19);
					}
					{
						scatterArrayCommon = new JCheckBox();
						optionPanel.add(scatterArrayCommon);
						scatterArrayCommon.setText("Common Axis");
						scatterArrayCommon.setBounds(143, 18, 172, 19);
					}
					{
						ComboBoxModel scatterArrayLineModel = 
							new DefaultComboBoxModel(
									new String[] { "Linear", "Smooth","None" });
						scatterArrayLine = new JComboBox();
						optionPanel.add(scatterArrayLine);
						scatterArrayLine.setModel(scatterArrayLineModel);
						scatterArrayLine.setBounds(194, 45, 127, 22);
					}
					{
						lineLabel = new JLabel();
						optionPanel.add(lineLabel);
						lineLabel.setText("Lines:");
						lineLabel.setBounds(149, 49, 45, 15);
					}
					{
						sep = new JSeparator();
						optionPanel.add(sep);
						sep.setBounds(34, 79, 288, 10);
					}
					{
						ellipse = new JRadioButton();
						optionPanel.add(ellipse);
						optionPanel.add(getCircles());
						optionPanel.add(getSep1());
						ellipse.setText("Ellipses");
						ellipse.setBounds(17, 101, 120, 19);
					}
				}
				{
					matrixPanel = new JPanel();
					getContentPane().add(matrixPanel);
					matrixPanel.setBounds(12, 219, 372, 92);
					matrixPanel.setBorder(BorderFactory.createTitledBorder("Correlation Matrices (No 'with' variables allowed)"));
					matrixPanel.setLayout(null);
					matrixPanel.add(getMatrix());
				}
				{
					okayCancelPanel = new OkayCancelPanel(false,false);
					getContentPane().add(okayCancelPanel);
					getContentPane().add(getNone());
					okayCancelPanel.setBounds(146, 367, 238, 42);
				}
			}
			getButtonGroup() ;
			this.setSize(396, 443);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setModel(CorModel.Plots mod){
		model=mod;
		scatterArray.setSelected(mod.scatterArray);
		ellipse.setSelected(mod.ellipse);
		circles.setSelected(mod.circles);
		matrix.setSelected(mod.matrix);
		none.setSelected(mod.none);
		scatterArrayCommon.setSelected(mod.common);
		scatterArrayLine.setSelectedItem(mod.saLines);
	}
	
	public boolean updateModel(){
		model.scatterArray=scatterArray.isSelected();
		model.ellipse=ellipse.isSelected();
		model.circles=circles.isSelected();
		model.matrix=matrix.isSelected();
		model.none=none.isSelected();
		model.common=scatterArrayCommon.isSelected();
		model.saLines=(String)scatterArrayLine.getSelectedItem();		
		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		String cmd = arg0.getActionCommand();
		if(cmd=="Cancel")
			this.dispose();
		else if(cmd=="OK"){
			if(updateModel())
				this.dispose();
		}
		
	}
	
	private ButtonGroup getButtonGroup() {
		if(buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			buttonGroup.add(scatterArray);
			buttonGroup.add(ellipse);
			buttonGroup.add(circles);
			buttonGroup.add(matrix);
			buttonGroup.add(none);
		}
		return buttonGroup;
	}
	
	private JRadioButton getNone() {
		if(none == null) {
			none = new JRadioButton();
			none.setText("No Plots");
			none.setBounds(28, 323, 98, 19);
		}
		return none;
	}
	
	private JRadioButton getCircles() {
		if(circles == null) {
			circles = new JRadioButton();
			circles.setText("Circles");
			circles.setBounds(17, 152, 120, 19);
		}
		return circles;
	}
	
	private JSeparator getSep1() {
		if(sep1 == null) {
			sep1 = new JSeparator();
			sep1.setBounds(34, 136, 288, 10);
		}
		return sep1;
	}
	
	private JRadioButton getMatrix() {
		if(matrix == null) {
			matrix = new JRadioButton();
			matrix.setText("Correlation Matrix");
			matrix.setBounds(17, 37, 157, 19);
		}
		return matrix;
	}

}
