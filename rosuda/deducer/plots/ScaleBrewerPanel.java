package org.rosuda.deducer.plots;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ScaleBrewerPanel extends DefaultElementView{

	public ScaleBrewerPanel(ElementModel em){
		super(em);
	}
	
	
	public void updatePanel(){
		super.updatePanel();
		URL url = getClass().getResource("/icons/ggplot_icons/brewer_palettes.png");
		JLabel brewer = new JLabel(new ImageIcon(url));
		brewer.setAlignmentX(CENTER_ALIGNMENT);
		paramPanel.add(brewer);
		paramPanel.validate();
	}
}
