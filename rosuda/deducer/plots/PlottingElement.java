package org.rosuda.deducer.plots;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class PlottingElement implements Transferable{
	
	private ElementModel model;
	
	
	private ImageIcon icon;
	private JPanel panel;
	
	private String name;
	private String type;
	
	private boolean active = true;
	private boolean compound = false; //can break out into other elements
	
	public static DataFlavor DATAFLAVOR = new DataFlavor(PlottingElement.class,"Plotting element");
	
	public PlottingElement(){}
	
	public PlottingElement(String filename,String elementType,String elementName){
		super();
		URL url = getClass().getResource(filename);
		icon = new ImageIcon(url);
		name=elementName;
		type = elementType;
		Layer l;
		if(type=="geom"){
			l = Layer.makeGeomLayer(name);
			model = l;			
		}else if(type == "stat"){
			l = Layer.makeStatLayer(name);
			model = l;
		}else if(type == "scale"){
			String[] s = name.split("_");
			if(s.length>1){
				model = Scale.makeScale(s[0], s[1]);
			}else {
				model = Scale.makeScale(null, s[0]);
			}
		}else if(type == "coord"){
			Coord c = Coord.makeCoord(name);
			model = c;
		}else if(type == "facet"){
			Facet f = Facet.makeFacet(name);
			model = f;
		}
	}
	
	public Object clone(){
		PlottingElement p = new PlottingElement();
		p.model = (ElementModel) this.model.clone();
		p.icon = this.icon;
		p.name = this.name;
		p.type = this.type;
		return p;
	}
	
	public static PlottingElement createElement(String type,String name){
		String nm = name;
		String[] s = nm.split("_");
		if(s.length>1)
			nm = s[s.length-1];
		PlottingElement el = new PlottingElement("/icons/ggplot_icons/"+type+"_"+nm+".png",type,name);
		return el;
	}
	
	public JPanel makeComponent(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel iconLabel;
		if(active)
			iconLabel = new JLabel(icon);
		else{
			URL url = getClass().getResource("/icons/edit_remove_32.png");
			iconLabel = new JLabel(new ImageIcon(url));
		}
		iconLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		panel.add(iconLabel);
		JLabel label = new JLabel(type);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setFont(new Font("Dialog", Font.PLAIN, 8) );
		panel.add(label);
		String[] s = name.split("_");
		for(int i=0;i<s.length;i++){
			label = new JLabel(s[i]);
			label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			label.setFont(new Font("Dialog", Font.PLAIN, 12) );
			panel.add(label);			
		}
		if(!active)
		panel.setPreferredSize(new Dimension(80,70));
		panel.setBorder(new EtchedBorder());		
		return panel;
	}
	
	public Image getImage(){
		return icon.getImage();
	}
	
	public void setImage(Image i){
		icon = new ImageIcon(i);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String n){
		name=n;
	}
	
	public JPanel getPanel(){
		return model.getView();
	}
	
	public ElementModel getModel(){
		return model;
	}
	
	public void setModel(ElementModel m){
		model = m;
	}
	
	public boolean isActive(){return active;}
	public void setActive(boolean act){
		active = act;
	}
	
	public boolean isCompound(){return compound;}
	public void setCompound(boolean comp){compound = comp;}	

	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
		return this;
	}

	public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] f = new DataFlavor[] {new DataFlavor(PlottingElement.class,"Plot element")};
		return f;
	}

	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return true;
	}
}

