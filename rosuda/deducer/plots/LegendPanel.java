package org.rosuda.deducer.plots;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import javax.swing.JButton;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.rosuda.deducer.data.ExDefaultTableModel;
import org.rosuda.deducer.data.ExScrollableTable;
import org.rosuda.deducer.data.ExTable;


public class LegendPanel extends JPanel implements ActionListener {
	private JLabel nameLabel;
	private ExScrollableTable tableScroller;
	private JPanel tablePanel;
	private JCheckBox showCheckBox;
	private JTextField nameField;
	private ExTable table;
	private ExDefaultTableModel tableModel;
	private JButton addButton;

	private boolean numeric = true;
	
	public LegendPanel() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(255, 255));
			{
				tablePanel = new JPanel();
				BorderLayout tablePanelLayout = new BorderLayout();
				tablePanel.setLayout(tablePanelLayout);
				this.add(tablePanel, new AnchorConstraint(40, 0, 32, 0, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS));
				tablePanel.setPreferredSize(new java.awt.Dimension(125, 104));
				{
					tableModel = new LegendTableModel();
					tableModel.addColumn("Value");
					tableModel.addColumn("Label");
					tableModel.addRow(new String[] {"",""});
					table = new ExTable(tableModel);
					table.getTableHeader().removeMouseListener(table.getColumnListener());
				}
				{
					tableScroller = new ExScrollableTable(table);
					tablePanel.add(tableScroller, BorderLayout.CENTER);
				}
			}
			{
				showCheckBox = new JCheckBox();
				this.add(showCheckBox, new AnchorConstraint(822, 0, 968, 0, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				showCheckBox.setText("Show");
				showCheckBox.setSelected(true);
				showCheckBox.setPreferredSize(new java.awt.Dimension(133, 19));
			}
			{
				nameField = new JTextField();
				this.add(nameField, new AnchorConstraint(15, 0, 200, 0, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				nameField.setPreferredSize(new java.awt.Dimension(125, 22));
			}
			{
				nameLabel = new JLabel();
				this.add(nameLabel, new AnchorConstraint(0, 922, 90, 0, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				nameLabel.setText("Title");
				nameLabel.setPreferredSize(new java.awt.Dimension(125, 15));
			}
			{
				addButton = new JButton("+");
				this.add(addButton, new AnchorConstraint(35, 0, 9, 0, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE));		
				addButton.setPreferredSize(new java.awt.Dimension(40, 22));
				addButton.addActionListener(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isNumeric(){
		return numeric;
	}
	
	public void setNumeric(boolean num){
		numeric = num;
	}
	
	public String getName(){
		return nameField.getText();
	}
	
	public void setName(String n){
		nameField.setText(n);
	}
	
	public ExDefaultTableModel getTableModel(){
		return tableModel;
	}
	
	public void setTableModel(ExDefaultTableModel tm){
		tableModel = tm;
		table.setModel(tm);
		tableScroller.getRowNamesModel().initHeaders(tableModel.getRowCount());
	}
	
	public boolean getShowLegend(){
		return showCheckBox.isSelected();
	}
	
	public void setShowLegend(boolean b){
		showCheckBox.setSelected(b);
	}
	

	class LegendTableModel extends ExDefaultTableModel{
	
		public void removeRow(int row){
			super.removeRow(row);
			tableScroller.getRowNamesModel().initHeaders(tableModel.getRowCount());
		}
		public void insertNewRow(int index){
			this.insertRow(index, new String[] {"",""});
			tableScroller.getRowNamesModel().initHeaders(tableModel.getRowCount());
		}
		public void removeColumn(int row){}
		public void insertNewColumn(int index){}
	}

	public void actionPerformed(ActionEvent a) {
		String cmd = a.getActionCommand();
		if(cmd == "+"){
			tableModel.addRow(new String[] {"",""});
			tableScroller.getRowNamesModel().initHeaders(tableModel.getRowCount());
		}
	}
}
