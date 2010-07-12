package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Vector;

import org.rosuda.deducer.widgets.param.Param;

public class Stat {
	
	public String name;
	public Vector aess = new Vector();
	public Vector params = new Vector();
	
	public String defaultGeom;
	
	public Vector generated = new Vector();
	
	public Object clone(){
		Stat s = new Stat();
		s.name = this.name;
		s.defaultGeom = this.defaultGeom;
		for(int i=0;i<aess.size();i++){
			Aes aes = (Aes) aess.get(i);
			s.aess.add(aes.clone());
		}
		for(int i=0;i<params.size();i++){
			Param p = (Param) params.get(i);
			s.params.add(p.clone());
		}
		for(int i=0;i<generated.size();i++)
			s.generated.add(generated.get(i));
		
		return s;
	}
	
	public static Stat makeIdentity(){
		Stat s = new Stat();
		s.name = "identity";
		s.defaultGeom = "point";
		return s;
	}
	
	public static Stat makeAbline(){
		Stat s = new Stat();
		s.name = "abline";
		s.defaultGeom = "abline";
		
		return s;
	}
	
	public static Stat makeBin(){
		Stat s = new Stat();
		s.name = "bin";
		s.defaultGeom = "bar";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.defaultVariable = "..count..";
		aes.variable = "..count..";
		s.aess.add(aes);
		
		Param p ;
		p= Param.makeParam("binwidth");
		p.dataType = Param.DATA_NUMERIC;
		p.lowerBound=new Double(0.0);
		s.params.add(p);
		
		p= Param.makeParam("origin");
		p.dataType = Param.DATA_NUMERIC;
		s.params.add(p);
		
		p= Param.makeParam("breaks");
		s.params.add(p);
		
		p= Param.makeParam("width");
		p.dataType = Param.DATA_NUMERIC;
		p.lowerBound=new Double(0.0);
		s.params.add(p);
		
		p= Param.makeParam("drop");
		s.params.add(p);
		
		s.generated.add("count");
		s.generated.add("density");
		s.generated.add("ncount");
		s.generated.add("ndensity");
		
		return s;
	}
	
	public static Stat makeBin2d(){
		Stat s = new Stat();
		s.name = "bin2d";
		s.defaultGeom = "rect";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.defaultVariable = "..count..";
		aes.variable = "..count..";
		s.aess.add(aes);
		
		Param p ;
		p= Param.makeParam("binwidth");
		p.dataType = Param.DATA_NUMERIC;
		p.lowerBound=new Double(0.0);
		s.params.add(p);
		
		p= Param.makeParam("origin");
		p.dataType = Param.DATA_NUMERIC;
		s.params.add(p);
		
		p= Param.makeParam("breaks");
		p.dataType = Param.DATA_NUMERIC;
		p.lowerBound=new Double(0.0);
		s.params.add(p);
		
		
		p= Param.makeParam("drop");
		s.params.add(p);
		
		s.generated.add("count");
		s.generated.add("density");
		s.generated.add("xint");
		s.generated.add("xmin");
		s.generated.add("xmax");
		s.generated.add("yint");
		s.generated.add("ymin");
		s.generated.add("ymax");
		
		return s;
	}	
	
	public static Stat makeBoxplot(){
		Stat s = new Stat();
		s.name = "boxplot";
		s.defaultGeom = "boxplot";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		s.aess.add(aes);
		

		Param p ;
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		p= Param.makeParam("width");
		s.params.add(p);
		
		p= Param.makeParam("coef");
		p.dataType = Param.DATA_NUMERIC;
		p.lowerBound=new Double(0.0);
		s.params.add(p);
		
		s.generated.add("width");
		s.generated.add("ymin");
		s.generated.add("lower");
		s.generated.add("middle");
		s.generated.add("upper");
		s.generated.add("ymax");
		s.generated.add("ymin");
		s.generated.add("ymax");
		
		return s;
	}	
	
	public static Stat makeContour(){
		Stat s = new Stat();
		s.name = "contour";
		s.defaultGeom = "path";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("z");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.defaultUseVariable = true;
		aes.defaultVariable = "..piece..";
		s.aess.add(aes);

		Param p;
		
		p= Param.makeParam("bins");
		s.params.add(p);
		
		
		p= Param.makeParam("binwidth");
		s.params.add(p);
		
		p= Param.makeParam("breaks");
		s.params.add(p);
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		s.generated.add("level");
		s.generated.add("piece");
		
		return s;
	}	
	
	public static Stat makeDensity(){
		Stat s = new Stat();
		s.name = "density";
		s.defaultGeom = "area";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		aes.defaultVariable = "..density..";
		aes.variable = "..density..";
		s.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		s.aess.add(aes);

		Param p;
		
		p= Param.makeParam("adjust");
		s.params.add(p);
		
		
		p= Param.makeParam("kernel");
		s.params.add(p);
		
		p= Param.makeParam("trim");
		s.params.add(p);
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		s.generated.add("density");
		s.generated.add("count");
		s.generated.add("scaled");
		
		return s;
	}	
	
	public static Stat makeDensity2d(){
		Stat s = new Stat();
		s.name = "density2d";
		s.defaultGeom = "density2d";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		aes.required = true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("colour");
		aes.defaultValue = Color.decode("#3366FF");
		aes.value = aes.defaultValue;
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		aes.defaultVariable = "interaction(..piece..,..level..)";
		aes.variable = aes.defaultVariable;
		s.aess.add(aes);

		Param p;
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		
		p= Param.makeParam("contour");
		s.params.add(p);
		
		s.generated.add("level");
		s.generated.add("piece");
		return s;
	}	
	
	public static Stat makeFunction(){
		Stat s = new Stat();
		s.name = "function";
		s.defaultGeom = "path";
		
		Param p;
		
		p= Param.makeParam("fun");
		s.params.add(p);
		
		p= Param.makeParam("args");
		s.params.add(p);
		
		s.generated.add("y");
		s.generated.add("x");
		
		return s;
	}	
	
	public static Stat makeBinhex(){
		Stat s = new Stat();
		s.name = "binhex";
		s.defaultGeom = "hex";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		s.aess.add(aes);
		
		aes = Aes.makeAes("fill");
		aes.defaultVariable = "..count..";
		aes.variable = aes.defaultVariable;
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		s.aess.add(aes);

		Param p;
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		
		p= Param.makeParam("binwidth");
		s.params.add(p);
		
		p= Param.makeParam("bins");
		s.params.add(p);
		
		s.generated.add("level");
		s.generated.add("piece");
		return s;
	}	
	
	public static Stat makeHline(){
		Stat s = new Stat();
		s.name = "hline";
		s.defaultGeom = "hline";
		
		Aes aes;
		
		aes = Aes.makeAes("yintercept");
		s.aess.add(aes);
		
		aes = Aes.makeAes("intercept");
		s.aess.add(aes);
		
		return s;
	}	
	
	public static Stat makeQq(){
		Stat s = new Stat();
		s.name = "qq";
		s.defaultGeom = "point";
		
		Aes aes;
		
		aes = Aes.makeAes("sample");
		aes.required=true;
		s.aess.add(aes);
		
		aes = Aes.makeAes("x",null,"..theoretical..");
		s.aess.add(aes);
		
		aes = Aes.makeAes("y",null,"..sample..");
		s.aess.add(aes);
		
		Param p;
		
		p= Param.makeParam("quantiles");
		p.value = new String[] {};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p= Param.makeParam("distribution");
		s.params.add(p);
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		s.generated.add("theoretical");
		s.generated.add("sample");
		
		return s;
	}
	
	public static Stat makeQuantile(){
		Stat s = new Stat();
		s.name = "quantile";
		s.defaultGeom = "quantile";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);
		
		
		aes = Aes.makeAes("group",null,"..quantile..");
		s.aess.add(aes);
		
		Param p;
		
		p= Param.makeParam("quantiles");
		s.params.add(p);
		
		p= Param.makeParam("formula");
		s.params.add(p);
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		s.generated.add("y");
		s.generated.add("quantile");
		
		return s;
	}	
	
	public static Stat makeSmooth(){
		Stat s = new Stat();
		s.name = "smooth";
		s.defaultGeom = "smooth";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);

		aes = Aes.makeAes("group");
		s.aess.add(aes);
		
		Param p;
		
		p= Param.makeParam("method");
		s.params.add(p);
		
		p= Param.makeParam("formula");
		s.params.add(p);
		
		p= Param.makeParam("se");
		s.params.add(p);
		
		p= Param.makeParam("fullrange");
		s.params.add(p);
		
		p= Param.makeParam("na.rm");
		s.params.add(p);
		
		s.generated.add("ymin");
		s.generated.add("ymax");
		s.generated.add("se");
		
		return s;
	}	
	
	public static Stat makeSpoke(){
		Stat s = new Stat();
		s.name = "spoke";
		s.defaultGeom = "segment";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		s.aess.add(aes);
		
		aes = Aes.makeAes("angle");
		aes.required = true;
		aes.value=null;
		aes.defaultValue=null;
		s.aess.add(aes);
		
		aes = Aes.makeAes("radius");
		s.aess.add(aes);
		
		aes = Aes.makeAes("xend",null,"..xend..");
		s.aess.add(aes);
		
		aes = Aes.makeAes("yend",null,"..yend..");
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		s.aess.add(aes);
		
		s.generated.add("xend");
		s.generated.add("yend");
		
		return s;
	}	
	
	public static Stat makeSum(){
		Stat s = new Stat();
		s.name = "sum";
		s.defaultGeom = "point";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		s.aess.add(aes);
		
		aes = Aes.makeAes("size",null,"..prop..");
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		s.aess.add(aes);
		
		s.generated.add("n");
		s.generated.add("prop");
		
		return s;
	}
	
	public static Stat makeSummary(){
		Stat s = new Stat();
		s.name = "summary";
		s.defaultGeom = "pointrange";
		
		Aes aes;
		
		aes = Aes.makeAes("x");
		s.aess.add(aes);
		
		aes = Aes.makeAes("y");
		s.aess.add(aes);
		
		aes = Aes.makeAes("group");
		s.aess.add(aes);

		//todo: give implementation
		return s;
	}
	
	public static Stat makeUnique(){
		Stat s = new Stat();
		s.name = "unique";
		s.defaultGeom = "point";
		
		return s;
	}
	
	public static Stat makeVline(){
		Stat s = new Stat();
		s.name = "vline";
		s.defaultGeom = "vline";
		
		Aes aes;
		
		aes = Aes.makeAes("xintercept");
		s.aess.add(aes);

		aes = Aes.makeAes("intercept");
		s.aess.add(aes);
		
		return s;
	}
	
	public static Stat makeStat(String statName){
		if(statName=="identity")
			return Stat.makeIdentity();
		else if(statName=="abline")
			return Stat.makeAbline();
		else if(statName=="bin")
			return Stat.makeBin();
		else if(statName=="bin2d")
			return Stat.makeBin2d();
		else if(statName=="binhex")
			return Stat.makeBinhex();		
		else if(statName=="boxplot")
			return Stat.makeBoxplot();
		else if(statName=="contour")
			return Stat.makeContour();
		else if(statName=="density")
			return Stat.makeDensity();
		else if(statName=="density2d")
			return Stat.makeDensity2d();
		else if(statName=="function")
			return Stat.makeFunction();
		else if(statName=="hline")
			return Stat.makeHline();
		else if(statName=="qq")
			return Stat.makeQq();
		else if(statName=="quantile")
			return Stat.makeQuantile();
		else if(statName=="smooth")
			return Stat.makeSmooth();
		else if(statName=="spoke")
			return Stat.makeSpoke();
		else if(statName=="sum")
			return Stat.makeSum();
		else if(statName=="summary")
			return Stat.makeSummary();
		else if(statName=="unique")
			return Stat.makeUnique();
		else if(statName=="vline")
			return Stat.makeVline();
		return null;
	}

	
}
