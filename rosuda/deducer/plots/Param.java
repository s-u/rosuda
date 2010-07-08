package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;

import org.rosuda.deducer.Deducer;

public class Param {

	final public static String DATA_ANY = "any";
	final public static String DATA_NUMERIC = "numeric";
	final public static String DATA_CHARACTER = "character";
	final public static String DATA_LOGICAL = "logical";
	final public static String DATA_NUMERIC_VECTOR = "numeric vector";
	final public static String DATA_CHARACTER_VECTOR = "character vector";
	
	final public static String VIEW_ENTER = "enter";
	final public static String VIEW_ENTER_LONG = "enter long";
	final public static String VIEW_COMBO = "combo";
	final public static String VIEW_EDITABLE_COMBO = "edit combo";
	final public static String VIEW_CHECK_BOX = "check";
	final public static String VIEW_VECTOR_BUILDER = "vector";
	
	
	public String name;					//name of paramter
	public String title;				//title to be displayed
	
	public Object value;
	public Object defaultValue;			//default	
	
	public String dataType = DATA_ANY;	//Type of data the parameter takes
	public Double lowerBound ;			//If bounded, the lower bound
	public Double upperBound ;			//if bounded, the upper bound
	
	public String[] options;			//passible parameter values
	public String[] labels;				//descr of param values
	
	public String view = VIEW_ENTER_LONG;
	
	public Param(){}
	
	public Param(String nm){
		name=nm;
		title =nm;
	}
	
	public Object clone(){
		Param p = new Param();
		p.name = this.name;
		p.title = this.title;
		p.dataType = this.dataType;
		if(this.lowerBound!=null)
			p.lowerBound = new Double(this.lowerBound.doubleValue());
		if(this.upperBound!=null)
			p.upperBound = new Double(this.upperBound.doubleValue());
		if(this.options!=null){
			String[] v = new String[this.options.length];
			for(int i=0;i<this.options.length;i++)
				v[i] = this.options[i];
			p.options = v;
		}
		if(this.labels!=null){
			String[] v = new String[this.labels.length];
			for(int i=0;i<this.labels.length;i++)
				v[i] = this.labels[i];
			p.labels = v;
		}
		p.view = this.view;
		if(dataType == Param.DATA_LOGICAL){
			if(value!=null){
				p.value = new Boolean(((Boolean)value).booleanValue());
			}
			if(defaultValue!=null){
				p.defaultValue = new Boolean(((Boolean)defaultValue).booleanValue());
			}			
		}else if(dataType == Param.DATA_NUMERIC_VECTOR ||
				dataType == Param.DATA_CHARACTER_VECTOR){
			if(value!=null){
				String[] oldV = (String[]) value;
				String[] v = new String[oldV.length];
				for(int i=0;i<oldV.length;i++)
					v[i] = oldV[i];
				p.value = v;			
			}
			if(defaultValue!=null){
				String[] oldV = (String[]) defaultValue;
				String[] v = new String[oldV.length];
				for(int i=0;i<oldV.length;i++)
					v[i] = oldV[i];
				p.defaultValue = v;			
			}
		}else{
			p.value = this.value;
			p.defaultValue = this.defaultValue;
		}
		return p;
	}
	
	public String[] getAesCalls(){
		return new String[]{};
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(value!=null && !value.equals(defaultValue)){
			String val;
			if(value instanceof Color)
				val = "'#"+ Integer.toHexString(((Color)value).getRGB()).substring(2)+"'";
			else if(dataType == Param.DATA_ANY && value.toString().length()>0){
				try{
					Double.parseDouble(value.toString());
					val = value.toString();
				}catch(Exception e){
					val = "'" + Deducer.addSlashes(value.toString()) + "'";
				}
			}else if(dataType == Param.DATA_CHARACTER && value.toString().length()>0){
				val = "'" + Deducer.addSlashes(value.toString()) + "'";
			}else if(dataType == Param.DATA_LOGICAL){
				val = ((Boolean) value).booleanValue() ? "TRUE" : "FALSE";
			}else if(dataType == Param.DATA_NUMERIC_VECTOR || dataType == Param.DATA_CHARACTER_VECTOR){
				String[] vecVals = (String[]) value;
				String[] dvecVals = (String[]) defaultValue;
				boolean identical = true;
				if(vecVals.length!=dvecVals.length)
					identical=false;
				else
					for(int i=0;i<vecVals.length;i++)
						if(!vecVals[i].equals(dvecVals[i]))
							identical=false;
				if(!identical)
					val = Deducer.makeRCollection(new DefaultComboBoxModel(vecVals), "c", 
							dataType == Param.DATA_CHARACTER_VECTOR);
				else
					val="";
			}else
				val = value.toString();
			if(val.length()>0)
				calls = new String[]{name + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
		return calls;
	}
	
	
	public static Param makeParam(String name){
		Param p = new Param(name);
		p.title = p.name;
		if(name=="na.rm"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(false);
			p.title = "remove missing";
		}else if(name=="drop"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(true);			
		}else if(name=="width"){
			p.dataType = Param.DATA_NUMERIC;
			p.view = Param.VIEW_ENTER;
			p.lowerBound = new Double(0.0);
		}else if(name=="outlier.colour"){
			
		}else if(name=="outlier.shape"){
			
		}else if(name=="outlier.size"){
			
		}else if(name=="arrow"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(false);		
		}else if(name == "bins"){
			p.dataType = Param.DATA_NUMERIC;
			p.view = Param.VIEW_ENTER;
			p.lowerBound = new Double(1.0);			
		}else if(name == "breaks"){
			p.dataType = Param.DATA_NUMERIC_VECTOR;
			p.view = Param.VIEW_VECTOR_BUILDER;
		}else if(name =="binwidth"){
			p.dataType = Param.DATA_NUMERIC;
			p.view = Param.VIEW_ENTER;
			p.lowerBound = new Double(0.0);			
		}else if(name =="coef"){
			p.defaultValue = "2.0";
		}else if(name =="adjust"){
			p.dataType = Param.DATA_NUMERIC;
			p.view = Param.VIEW_ENTER;
			p.lowerBound = new Double(0.0);
		}else if(name =="kernel"){
			p.dataType = Param.DATA_CHARACTER;
			p.view = Param.VIEW_COMBO;
			p.options = new String[] {"gaussian", "epanechnikov", "rectangular",
	                   "triangular", "biweight",
	                   "cosine", "optcosine"};
			p.labels = new String[] {"gaussian", "epanechnikov", "rectangular",
	                   "triangular", "biweight",
	                   "cosine", "optcosine"};
		}else if(name =="trim"){
			p.dataType = Param.DATA_NUMERIC;
			p.view = Param.VIEW_ENTER;
			p.upperBound = new Double(1.0);
		}else if(name=="contour"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(true);		
		}else if(name =="quantiles"){
			p.dataType = Param.DATA_NUMERIC_VECTOR;
			p.view = Param.VIEW_VECTOR_BUILDER;
			p.defaultValue = new String[] {"0.25","0.5","0.75"};
		}else if(name =="method"){
			p.dataType = Param.DATA_CHARACTER;
			p.view = Param.VIEW_COMBO;
			p.options = new String[] {"lm", "gam", "loess", "rlm"};
			p.labels = new String[] {"Linear model", "Generalized additive model",
									"Smooth","Robust linear model"};
		}else if(name =="formula"){
			p.dataType = Param.DATA_CHARACTER;
			p.view = Param.VIEW_EDITABLE_COMBO;
			p.options = new String[] {"y ~ x", "y ~ poly(x,2)", "y ~ poly(x,3)"};
		}else if(name =="fun"){

		}else if(name =="args"){

		}else if(name =="se"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(false);	
			p.title = "Show confidence";
		}else if(name=="fullrange"){
			p.dataType = Param.DATA_LOGICAL;
			p.view = Param.VIEW_CHECK_BOX;
			p.defaultValue = new Boolean(false);	
			p.title = "Full data range";
		}else if(name =="level"){
			p.dataType = Param.DATA_NUMERIC;
			p.lowerBound = new Double(0.0);
			p.upperBound = new Double(1.0);
			p.defaultValue = new Double(0.95);
			p.view = Param.VIEW_ENTER;
		}else if(name =="direction"){
			p.options = new String[]{"vh","hv"};
			p.labels = new String[] {"Vertical then horizontal",
									"Horizontal then vertical"};
			p.defaultValue = "vh";
			p.dataType = Param.DATA_CHARACTER;
			p.view = Param.VIEW_COMBO;
		}
		if(p.value==null)
			p.value = p.defaultValue;
		return p;
	}
	
	
	
	
	
}
