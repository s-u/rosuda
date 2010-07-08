package org.rosuda.deducer.plots;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.rosuda.deducer.Deducer;

public class Layer implements ElementModel{

	String name;
	
	public boolean isGeom = false;
	public boolean isStat = false;
	
	public Geom geom;
	public Stat stat;
	
	public Position pos;
	
	public Vector aess;
	
	public String data;
	
	public void setStat(Stat s){
		stat = s;
		if(geom!=null)
			generateAes();
	}
	public Stat getStat(){
		return stat;
	}
	
	public void setGeom(Geom g){
		geom = g;
		if(stat!=null)
			generateAes();
	}
	
	public Geom getGeom(){
		return geom;
	}
	
	public Object clone(){
		Layer l = new Layer();
		l.isGeom = this.isGeom;
		l.isStat = this.isStat;
		l.data = this.data;
		l.geom = (Geom) this.geom.clone();
		l.stat = (Stat) this.stat.clone();
		l.pos = (Position) this.pos.clone();
		l.generateAes();
		return l;
	}
	
	public String getCall(){
		Vector aesCalls = new Vector();
		Vector paramCalls = new Vector();
		for(int i=0;i<aess.size();i++){
			Aes aes = (Aes) aess.get(i);
			String[] c = aes.getAesCalls();
			for(int j=0;j<c.length;j++)
				aesCalls.add(c[j]);
			String[] p = aes.getParamCalls();
			for(int j=0;j<p.length;j++)
				paramCalls.add(p[j]);			
		}
		Vector params = stat.params;
		for(int i=0;i<params.size();i++){
			Param prm = (Param) params.get(i);
			String[] p = prm.getParamCalls();
			for(int j=0;j<p.length;j++)
				paramCalls.add(p[j]);				
		}
		params = geom.params;
		for(int i=0;i<params.size();i++){
			Param prm = (Param) params.get(i);
			String[] p = prm.getParamCalls();
			for(int j=0;j<p.length;j++)
				paramCalls.add(p[j]);				
		}
		String aes = Deducer.makeRCollection(aesCalls,"aes",false);
		String dataCall = data==null ? null : ("data="+data);
		if(dataCall!=null)
			paramCalls.add(0, dataCall);
		if(aesCalls.size()>0)
			paramCalls.add(0, aes);
		String func = "";
		if(isGeom){
			func = "geom_"+geom.name;
			if(geom.defaultStat != stat.name)
				paramCalls.add("stat = "+stat.name);
		}else if(isStat){
			func = "stat_"+stat.name;
			if(stat.defaultGeom != geom.name)
				paramCalls.add("geom = "+geom.name);
		}
		if(!(pos.name == geom.defaultPosition && pos.height==null && pos.width==null)){
			String posCall = "position = " + pos.name + "(" +
				(pos.height==null ? "" : "hieght = " + pos.height.toString()) +
				(pos.height!=null && pos.width!=null ? "," : "") +
				(pos.width==null ? "" : "width = " + pos.width.toString()) +")" ;
			paramCalls.add(posCall);
		}
		return Deducer.makeRCollection(paramCalls, func, false);
	}
	
	public String checkValid(){
		for(int i=0;i<aess.size();i++){
			Aes aes = (Aes) aess.get(i);
			if(aes.required && (aes.value==null && aes.variable==null))
				return "Required variable " + aes.name + " not specified";
		}
		return null;
	}
	
	public String getType(){return "layer";}
	
	public ElementView getView(){
		LayerPanel p = new LayerPanel(this);
		return p;
	}
	
	public void generateAes() {
		Vector s = stat.aess;
		Vector g = geom.aess;
		aess = new Vector();
		int k=0;
		for(int i=0;i<g.size();i++){
			Aes saes = new Aes();
			Aes gaes = (Aes) g.get(i);
			if(stat.generated.contains(gaes.name))
				continue;
			boolean contained = false;
			for(int j=0;j<s.size();j++){
				saes = (Aes) s.get(j);
				if(gaes.name == saes.name){
					contained = true;
					break;
				}
			}
			if(!contained || saes.variable==null){
				if(gaes.required)
					aess.add(k++,gaes);
				else
					aess.add(gaes);
			}else{
				if(saes.required)
					aess.add(k++,saes);
				else
					aess.add(saes);
			}
		}
		int f=0;		
		for(int i=0;i<s.size();i++){
			Aes saes = (Aes) s.get(i);
			boolean contained = false;
			Aes aes;
			for(int j=0;j<aess.size();j++){
				aes = (Aes) aess.get(j);
				if(saes.name == aes.name){
					contained = true;
					break;
				}
			}
			if(!contained){
				if(saes.required)
					aess.add(f++,saes);
				else
					aess.add(saes);
			}
		}
	}
	
	public static Layer makeGeomLayer(String geomName){
		Geom g = Geom.makeGeom(geomName);
		if(g==null || g.defaultStat ==null || g.defaultPosition==null){
			JOptionPane.showMessageDialog(null, geomName+" is not a valid geom.");
		}
		Stat s = Stat.makeStat(g.defaultStat);
		if(s==null){
			JOptionPane.showMessageDialog(null, g.defaultStat+" is not a valid stat"+
					"when making geom "+geomName);
		}
		
		Position p = Position.makePosition(g.defaultPosition);
		Layer l = new Layer();
		l.name = "geom_"+geomName;
		l.isGeom = true;
		l.geom = g;
		l.stat = s;
		l.pos = p;
		l.generateAes();
		return l;
	}
	
	public static Layer makeStatLayer(String statName){
		Stat s = Stat.makeStat(statName);
		if(s==null || s.defaultGeom==null){
			JOptionPane.showMessageDialog(null, statName+" is not valid");
		}		
		Geom g = Geom.makeGeom(s.defaultGeom);		
		if(g==null || s ==null || g.defaultPosition==null){
			JOptionPane.showMessageDialog(null, statName+" is not a valid geom.");
		}
		Position p = Position.makePosition(g.defaultPosition);
		Layer l = new Layer();
		l.name = "stat_"+statName;
		l.isStat = true;
		l.geom = g;
		l.stat = s;
		l.pos = p;
		l.generateAes();
		return l;
	}
	
}
