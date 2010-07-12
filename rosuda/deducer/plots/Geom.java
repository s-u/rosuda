package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Vector;

import org.rosuda.deducer.widgets.param.Param;

public class Geom {

	public String name;
	public Vector aess = new Vector();
	public Vector params = new Vector();
	
	public String defaultStat;
	
	public String defaultPosition;
	
	public Object clone(){
		Geom g = new Geom();
		g.name = this.name;
		g.defaultStat = this.defaultStat;
		g.defaultPosition = this.defaultPosition;
		for(int i=0;i<aess.size();i++){
			Aes aes = (Aes) aess.get(i);
			g.aess.add(aes.clone());
		}
		for(int i=0;i<params.size();i++){
			Param p = (Param) params.get(i);
			g.params.add(p.clone());
		}
		return g;
	}
	
	public static Geom makeAbline(){
		Geom g = new Geom();
		
		g.name = "abline";
		g.defaultPosition = "identity";
		g.defaultStat = "abline";
		Aes aes;
		
		aes = Aes.makeAes("intercept");
		aes.required = false;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("slope");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(0.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeArea(){
		Geom g = new Geom();
		
		g.name = "area";
		g.defaultPosition = "identity";
		g.defaultStat = "identity";
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("y");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.defaultValue = new Color(51,51,51);
		aes.value = new Color(51,51,51);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		g.aess.add(aes);
		
		Param p = Param.makeParam("na.rm");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeBar(){
		Geom g = new Geom();
		
		g.name = "bar";
		g.defaultStat = "bin";
		g.defaultPosition = "stack";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.value = new Color(51,51,51);
		aes.defaultValue = new Color(51,51,51);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeBin2d(){
		Geom g = new Geom();
		
		g.name = "bin2d";
		g.defaultStat = "bin2d";
		g.defaultPosition = "identity";
		
		Aes aes;
		
		aes = Aes.makeAes("xmin");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("xmax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.value = new Color(153,153,153);
		aes.defaultValue = new Color(153,153,153);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeBlank(){
		Geom g = new Geom();
		
		g.name = "blank";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
		
		return g;
	}
	
	public static Geom makeBoxplot(){
		Geom g = new Geom();
		
		g.name = "boxplot";
		g.defaultStat = "boxplot";
		g.defaultPosition = "dodge";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("lower");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("upper");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("middle");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.value = Color.white;
		aes.defaultValue = Color.white;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("outlier.colour");
		g.params.add(p);
		
		p = Param.makeParam("outlier.shape");
		g.params.add(p);
		
		p = Param.makeParam("outlier.size");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeContour(){
		Geom g = new Geom();
		
		g.name = "contour";
		g.defaultStat = "contour";
		g.defaultPosition = "identity";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("y");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("colour");
		aes.required = false;
		aes.value = Color.decode("#3366FF");
		aes.defaultValue = Color.decode("#3366FF");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		aes.required = false;
		aes.defaultValue = new Double(.5);
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		aes.required = false;
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("arrow");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeCrossbar(){
		Geom g = new Geom();
		
		g.name = "crossbar";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("y");
		aes.required = true;
		g.aess.add(aes);	
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = Color.black;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("fatten");
		g.params.add(p);
		
		p = Param.makeParam("width");
		p.title = "Middle bar width";
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeDensity(){
		Geom point = new Geom();
		
		point.name = "density";
		
		point.defaultStat = "density";
		
		point.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("x");
		aes.required = true;
		point.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		point.aess.add(aes);		
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = Color.black;
		point.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		point.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		point.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		point.aess.add(aes);
		
		aes = Aes.makeAes("size");
		point.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		point.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		point.aess.add(aes);
		
		Param p = Param.makeParam("na.rm");
		point.params.add(p);
		
		return point;
	}	
	
	public static Geom makeDensity2d(){
		Geom point = new Geom();
		
		point.name = "density2d";
		
		point.defaultStat = "density2d";
		
		point.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("x");
		aes.required = true;
		point.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		point.aess.add(aes);		
		
		aes = Aes.makeAes("colour");
		aes.value = Color.decode("#3366FF");
		aes.defaultValue = Color.decode("#3366FF");
		point.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		point.aess.add(aes);
		
		aes = Aes.makeAes("size");
		point.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		point.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		point.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		point.aess.add(aes);
		
		Param p = Param.makeParam("na.rm");
		point.params.add(p);
		
		return point;
	}	
	
	public static Geom makeErrorbar(){
		Geom g = new Geom();
		
		g.name = "errorbar";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = Color.black;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("width");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		
		return g;
	}
	
	public static Geom makeErrorbarh(){
		Geom g = new Geom();
		
		g.name = "errorbarh";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);	
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = Color.black;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("width");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		
		return g;
	}
	
	public static Geom makeFreqpoly(){
		Geom point = new Geom();
		
		point.name = "freqpoly";
		
		point.defaultStat = "bin";
		
		point.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = Color.black;
		point.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		point.aess.add(aes);
		
		aes = Aes.makeAes("size");
		point.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		point.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		point.aess.add(aes);
		
		return point;
	}	
	
	public static Geom makeHex(){
		Geom g = new Geom();
		
		g.name = "hex";
		
		g.defaultStat = "binhex";
		
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.defaultValue = new Color(127,127,127);
		aes.value = aes.defaultValue;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		g.aess.add(aes);
		
		return g;
	}	
	
	public static Geom makeHistogram(){
		Geom g = new Geom();
		
		g.name = "histogram";
		
		g.defaultStat = "bin";
		
		g.defaultPosition = "stack";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		
		aes = Aes.makeAes("colour");
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.defaultValue = new Color(51,51,51);
		aes.value = aes.defaultValue;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.required = false;
		g.aess.add(aes);
		
		return g;
	}	
	
	public static Geom makeHline(){
		Geom g = new Geom();
		
		g.name = "hline";
		
		g.defaultStat = "hline";
		
		g.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = aes.value;
		g.aess.add(aes);

		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}	
	
	public static Geom makeJitter(){
		Geom g = new Geom();
		
		g.name = "jitter";
		
		g.defaultStat = "identity";
		
		g.defaultPosition = "jitter";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("shape");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = aes.value;
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("na.rm");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeLine(){
		Geom g = new Geom();
		
		g.name = "line";
		
		g.defaultStat = "identity";
		
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.value = Color.black;
		aes.defaultValue = aes.value;
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("arrow");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeLinerange(){
		Geom g = new Geom();
		
		g.name = "linerange";
		
		g.defaultStat = "identity";
		
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makePath(){
		Geom g = new Geom();
		
		g.name = "path";
		
		g.defaultStat = "identity";
		
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("arrow");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makePoint(){
		Geom point = new Geom();
		
		point.name = "point";
		
		point.defaultStat = "identity";
		
		point.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("x");
		point.aess.add(aes);
		
		aes = Aes.makeAes("y");
		point.aess.add(aes);
		
		aes = Aes.makeAes("shape");
		point.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		point.aess.add(aes);
		
		aes = Aes.makeAes("size",new Double(2.0),null);
		point.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		point.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		point.aess.add(aes);
		
		aes = Aes.makeAes("group");
		point.aess.add(aes);
		
		Param p = Param.makeParam("na.rm");
		point.params.add(p);
		
		return point;
	}
	
	public static Geom makePointrange(){
		Geom g = new Geom();
		
		g.name = "pointrange";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("shape");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makePolygon(){
		Geom g = new Geom();
		
		g.name = "polygon";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill",new Color(51,51,51),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeQuantile(){
		Geom g = new Geom();
		
		g.name = "quantile";
		g.defaultStat = "quantile";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.decode("#3366FF"),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("arrow");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeRect(){
		Geom g = new Geom();
		
		g.name = "rect";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("xmin");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("xmax");
		aes.required = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill",new Color(51,51,51),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeRibbon(){
		Geom g = new Geom();
		
		g.name = "ribbon";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymin");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("ymax");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill",new Color(51,51,51),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeRug(){
		Geom g = new Geom();
		
		g.name = "rug";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("colour");
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeSegment(){
		Geom g = new Geom();
		
		g.name = "segment";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("xend");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("yend");
		aes.required=true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("arrow");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeSmooth(){
		Geom g = new Geom();
		
		g.name = "smooth";
		g.defaultStat = "smooth";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		
		aes = Aes.makeAes("colour",Color.decode("#3366FF"),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill",new Color(153,153,153),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("weight");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha",new Double(0.4),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeStep(){
		Geom g = new Geom();
		
		g.name = "step";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha",new Double(0.4),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		Param p = Param.makeParam("direction");
		g.params.add(p);
		
		return g;
	}
	
	public static Geom makeText(){
		Geom g = new Geom();
		
		g.name = "text";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("label");
		aes.required = true;
		aes.defaultUseVariable = true;
		aes.useVariable = true;
		g.aess.add(aes);
		
		aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size",new Double(5.0),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("angle");
		g.aess.add(aes);
		
		aes = Aes.makeAes("hjust");
		g.aess.add(aes);
		
		aes = Aes.makeAes("vjust");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha",new Double(0.4),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeTile(){
		Geom g = new Geom();
		
		g.name = "tile";
		g.defaultStat = "identity";
		g.defaultPosition = "identity";
			
		
		Aes aes = Aes.makeAes("x");
		g.aess.add(aes);
		
		aes = Aes.makeAes("y");
		g.aess.add(aes);
		
		aes = Aes.makeAes("fill",new Color(51,51,51),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size",new Double(0.1),null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeVline(){
		Geom g = new Geom();
		
		g.name = "vline";
		g.defaultStat = "vline";
		g.defaultPosition = "identity";
		
		Aes aes = Aes.makeAes("colour",Color.black,null);
		g.aess.add(aes);
		
		aes = Aes.makeAes("size");
		g.aess.add(aes);
		
		aes = Aes.makeAes("linetype");
		g.aess.add(aes);
		
		aes = Aes.makeAes("alpha");
		g.aess.add(aes);
		
		aes = Aes.makeAes("group");
		g.aess.add(aes);
		
		return g;
	}
	
	public static Geom makeGeom(String geomName){
		if(geomName=="point")
			return Geom.makePoint();
		else if(geomName=="abline")
			return Geom.makeAbline();
		else if(geomName=="area")
			return Geom.makeArea();
		else if(geomName=="bar")
			return Geom.makeBar();
		else if(geomName=="bin2d")
			return Geom.makeBin2d();
		else if(geomName=="blank")
			return Geom.makeBlank();
		else if(geomName=="boxplot")
			return Geom.makeBoxplot();
		else if(geomName=="contour")
			return Geom.makeContour();
		else if(geomName=="crossbar")
			return Geom.makeCrossbar();
		else if(geomName=="density")
			return Geom.makeDensity();
		else if(geomName=="density2d")
			return Geom.makeDensity2d();
		else if(geomName=="errorbar")
			return Geom.makeErrorbar();
		else if(geomName=="errorbarh")
			return Geom.makeErrorbarh();
		else if(geomName=="freqpoly")
			return Geom.makeFreqpoly();
		else if(geomName=="hex")
			return Geom.makeHex();
		else if(geomName=="histogram")
			return Geom.makeHistogram();
		else if(geomName=="hline")
			return Geom.makeHline();
		else if(geomName=="jitter")
			return Geom.makeJitter();
		else if(geomName=="line")
			return Geom.makeLine();
		else if(geomName=="linerange")
			return Geom.makeLinerange();
		else if(geomName=="path")
			return Geom.makePath();
		else if(geomName=="pointrange")
			return Geom.makePointrange();
		else if(geomName=="polygon")
			return Geom.makePolygon();
		else if(geomName=="quantile")
			return Geom.makeQuantile();
		else if(geomName=="rect")
			return Geom.makeRect();
		else if(geomName=="ribbon")
			return Geom.makeRibbon();
		else if(geomName=="rug")
			return Geom.makeRug();
		else if(geomName=="segment")
			return Geom.makeSegment();
		else if(geomName=="smooth")
			return Geom.makeSmooth();
		else if(geomName=="step")
			return Geom.makeStep();
		else if(geomName=="text")
			return Geom.makeText();
		else if(geomName=="tile")
			return Geom.makeTile();
		else if(geomName=="vline")
			return Geom.makeVline();
		
		return null;
	}

	
}
