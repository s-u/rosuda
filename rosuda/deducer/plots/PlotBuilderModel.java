package org.rosuda.deducer.plots;

import java.util.Vector;

import javax.swing.DefaultListModel;

public class PlotBuilderModel {

	private DefaultListModel listModel = new DefaultListModel();
	
	public DefaultListModel getListModel(){
		return listModel;
	}
	
	public void setListModel(DefaultListModel m){
		listModel = m;
	}
	
	public Object clone(){
		PlotBuilderModel b = new PlotBuilderModel();
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			b.listModel.addElement(e.clone());
		}
		return b;
	}
	
	public boolean isValidAddition(ElementModel em){
		return true;
	}
	
	public String getCall(){
		String cmd ="";
		cmd+="ggplot()";
		boolean hasLayer = false;
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			if(e.getModel().getType().equals("layer"))
				hasLayer=true;
		}
		if(!hasLayer)
			return null;
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			if(e.isActive())
				cmd += " +\n\t" + e.getModel().getCall();
		}
		return cmd;
	}
	
	
	
	public void tryToFillRequiredAess(Layer l){
		Vector aess = l.aess;
		for(int j=0;j<aess.size();j++){
			Aes aes = (Aes) aess.get(j);
			if(!aes.required)
				continue;
			if(!(aes.variable==null || aes.variable.length()==0) || aes.value!=null)
				continue;
			for(int i=listModel.size()-1;i>=0;i--){
				if(aes.variable!=null && aes.variable.length()>0)
					continue;
				PlottingElement e = (PlottingElement) listModel.get(i);
				ElementModel em = e.getModel();
				if(em instanceof Layer){
					Vector laess = ((Layer)em).aess;
					if(l.data != null && !l.data.equals(((Layer)em).data))
						continue;
					for(int k=0;k<laess.size();k++){
						Aes laes = (Aes) laess.get(k);
						if(laes.name.equals(aes.name) && laes.variable!=null && laes.variable.length()>0
								&& !laes.variable.startsWith("..")){
							aes.variable = laes.variable;
							l.data = ((Layer)em).data;
						}
					}
				}
			}
		}
	}
}
