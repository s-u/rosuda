package org.rosuda.deducer.plots;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.data.ExDefaultTableModel;
import org.rosuda.deducer.plots.LegendPanel;
import org.rosuda.deducer.plots.ParamScaleLegend;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class ParamScaleWidget extends ParamWidget{
	private LegendPanel legendPanel;
	public ParamScaleWidget(){
		super();
	}
	
	public ParamScaleWidget(Param p){
		super();
		setModel(p);
	}
	
	public void setModel(Param p){
		model = p;		
		initAsScale();
		if(model.value!=null){
			Vector v = (Vector) model.value;
			String text = (String) v.get(0);
			Boolean show = (Boolean) v.get(1);
			ExDefaultTableModel tm = (ExDefaultTableModel) v.get(2);
			legendPanel.setName(text);
			legendPanel.setShowLegend(show.booleanValue());
			legendPanel.setNumeric(p.dataType == ParamScaleLegend.DATA_SCALE_NUMERIC);
			legendPanel.setTableModel(tm);
		}
	}
	
	public void updateModel(){
		Vector newValue = new Vector();
		newValue.add(legendPanel.getName());
		newValue.add(new Boolean(legendPanel.getShowLegend()));
		ExDefaultTableModel tm = legendPanel.getTableModel();
		if(model.dataType == ParamScaleLegend.DATA_SCALE_NUMERIC)
			for(int j=0;j<tm.getColumnCount();j++){
				String val = (String) tm.getValueAt(j, 0);
				try{
					Double.parseDouble(val);
				}catch(Exception e){
					tm.setValueAt("", j, 0);
				}
			}
		legendPanel.setTableModel(tm);
		newValue.add(tm);
		model.value = newValue;
	}
	
	public Param getModel(){
		updateModel();
		return model;
	}
	
	private void initAsScale() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(291, 166));
			int labelWidth = leftPos-22; 
			{
				label = new JLabel();
				this.add(label, new AnchorConstraint(202, 234, 689, 12, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				if(model!=null){
					label.setText(model.title);
					labelWidth = SwingUtilities.computeStringWidth(
							label.getFontMetrics(label.getFont()),
							model.title);
				}

			}	
			{
				int textPos = Math.max(labelWidth+22, leftPos);
				legendPanel = new LegendPanel();
				this.add(legendPanel, new AnchorConstraint(3, 750, 1003, textPos, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				legendPanel.setPreferredSize(new java.awt.Dimension(255, 255));
				legendPanel.setMaximumSize(new java.awt.Dimension(255, 1000));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setPreferredSize(new Dimension(300,255));
		this.setMaximumSize(new Dimension(500,400));
	}
}
