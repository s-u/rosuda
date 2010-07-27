package org.rosuda.deducer.plots;

import java.awt.Color;

import org.rosuda.deducer.Deducer;


public class Aes {
	
	final public static String DATA_ANY = "any";
	final public static String DATA_NONE = "none";
	final public static String DATA_NUMERIC = "numeric";
	//final public static String DATA_CHARACTER = "char";
	final public static String DATA_COLOUR = "col";
	final public static String DATA_LINE= "ln";
	final public static String DATA_SHAPE= "shape";
	final public static String DATA_NUMERIC_BOUNDED = "bounded";
	
	
	public String name;					//name of aes
	public String title;				//title for aes
	
	public String variable;
	public String defaultVariable;	
	
	public Object value;
	public Object defaultValue;			//default	
	
	public boolean required = false;	//is required
	
	public boolean defaultUseVariable = false;//view should default to variable view (rather than value)
	public boolean useVariable;				//is toggled to variable;

	public String dataType;	//Type of data the parameter takes
	public Double lowerBound ;			//If bounded, the lower bound
	public Double upperBound ;			//if bounded, the upper bound
	//public Vector charValues;			//if character, a vector of the valid values
	
	public Object clone(){
		Aes a = new Aes();
		a.name = this.name;
		a.title = this.title;
		a.variable = this.variable;
		a.defaultVariable = this.defaultVariable;
		a.required = this.required;
		a.defaultUseVariable = this.defaultUseVariable;
		a.useVariable = this.useVariable;
		a.dataType = this.dataType;
		if(this.lowerBound!=null)
			a.lowerBound = new Double(this.lowerBound.doubleValue());
		if(this.upperBound!=null)
			a.upperBound = new Double(this.upperBound.doubleValue());
		if(dataType == Aes.DATA_NUMERIC || dataType == Aes.DATA_NUMERIC_BOUNDED ){
			if(value!=null){
				a.value = new Double(((Double)value).doubleValue());
			}
			if(defaultValue!=null){
				a.defaultValue = new Double(((Double)defaultValue).doubleValue());
			}
		}else if(dataType == Aes.DATA_SHAPE ||
			dataType == Aes.DATA_LINE){
			if(value!=null){
				a.value = new Integer(((Integer)value).intValue());
			}
			if(defaultValue!=null){
				a.defaultValue = new Integer(((Integer)defaultValue).intValue());
			}			
		}else if(dataType == Aes.DATA_COLOUR){
			if(value!=null){
				Color c = (Color) value;
				a.value = new Color(c.getRGB());
			}
			if(defaultValue!=null){
				Color c = (Color) defaultValue;
				a.defaultValue = new Color(c.getRGB());
			}
		}else{
			a.value = this.value;
			a.defaultValue = this.defaultValue;
		}
		return a;
	}
	
	public String[] getAesCalls(){
		String[] calls;
		boolean useVar = useVariable || value==null;
		if(variable!=null && variable.length()>0 && variable!=defaultVariable && useVar){
			calls = new String[] {name + " = " + variable};
		}else
			calls = new String[]{};
		return calls;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		boolean useVar = useVariable && variable!=null && variable.length()>0;
		if(value!=null && !value.equals(defaultValue) && !useVar){
			String val;
			if(value instanceof Color){
				val = "'#"+ Integer.toHexString(((Color)value).getRGB()).substring(2)+"'";
			}else if(dataType == Aes.DATA_ANY && value.toString().length()>0){
				try{
					Double.parseDouble(value.toString());
					val = value.toString();
				}catch(Exception e){
					val = "'" + Deducer.addSlashes(value.toString()) + "'";
				}
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
	
	public static Aes makeAes(String type){
		Aes a = new Aes();
		a.name=type;
		String e = type.substring(1);
		a.title = type.substring(0, 1).toUpperCase().concat(e);
		if(type.equals("x")){
			a.dataType = DATA_ANY;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("y")){
			a.dataType = DATA_ANY;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("z")){
			a.dataType = DATA_ANY;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("colour")){
			a.dataType = DATA_COLOUR;
		}else if(type.equals("fill")){
			a.dataType = DATA_COLOUR;
		}else if(type.equals("label")){
			a.dataType = DATA_ANY;
		}else if(type.equals("size")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.defaultValue = new Double(0.5);
			a.lowerBound=new Double(0.0);
		}else if(type.equals("alpha")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.defaultValue = new Double(1.0);
			a.lowerBound = new Double(0.0);
			a.upperBound = new Double(1.0);
			a.defaultUseVariable = false;
			a.title = "Alpha level";
		}else if(type.equals("angle")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.defaultValue = new Double(0.0);
			a.lowerBound = new Double(0.0);
			a.upperBound = new Double(360.0);
		}else if(type.equals("radius")){
			a.required = true;
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound = new Double(0.0);
		}else if(type.equals("hjust")){
			a.name = "hjust";
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound = new Double(0.0);
			a.upperBound = new Double(1.0);
			a.defaultValue = new Double(.5);
			a.defaultUseVariable = false;
		}else if(type.equals("vjust")){
			a.name = "hjust";
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound = new Double(0.0);
			a.upperBound = new Double(1.0);
			a.defaultValue = new Double(.5);
			a.defaultUseVariable = false;
		}else if(type.equals("intercept")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("xintercept")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("yintercept")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("slope")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("linetype")){
			a.dataType = DATA_LINE;
			a.defaultValue = new Integer(1);
			a.title = "Line type";
		}else if(type.equals("size")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound = new Double(0.0);
		}else if(type.equals("xmin")){
			a.dataType = DATA_NUMERIC;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("xmax")){
			a.dataType = DATA_NUMERIC;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("ymin")){
			a.dataType = DATA_NUMERIC;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("ymax")){
			a.dataType = DATA_NUMERIC;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("xend")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("yend")){
			a.dataType = DATA_NUMERIC;
		}else if(type.equals("weight")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound = new Double(0.0);
			a.defaultValue =  new Double(1.0);
		}else if(type.equals("lower")){
			a.dataType = DATA_NONE;
		}else if(type.equals("upper")){
			a.dataType = DATA_NONE;
		}else if(type.equals("middle")){
			a.dataType = DATA_NONE;
		}else if(type.equals("width")){
			a.dataType = DATA_NUMERIC_BOUNDED;
			a.lowerBound=new Double(0.0);
			a.defaultValue = new Double(0.5);
		}else if(type.equals("sample")){
			a.dataType = DATA_NONE;
			a.required = true;
			a.defaultUseVariable = true;
		}else if(type.equals("shape")){
			a.dataType = DATA_SHAPE;
			a.defaultValue = new Integer(16);
		}else if(type.equals("xend")){
			a.dataType = DATA_ANY;
		}else if(type.equals("yend")){
			a.dataType = DATA_ANY;
		}else if(type.equals("group")){
			a.dataType = DATA_NONE;
		}else
			a.dataType = DATA_NONE;
		a.value=a.defaultValue;
		a.variable=a.defaultVariable;
		a.useVariable = a.defaultUseVariable;
		return a;
	}
	public static Aes makeAes(String type,Object dValue,String dVariable){
		Aes aes = makeAes(type);
		if(dValue!=null){
			aes.defaultUseVariable=false;
			aes.useVariable=false;
			aes.value = dValue;
			aes.defaultValue = dValue;
		}
		if(dVariable!=null){
			aes.defaultUseVariable=true;
			aes.useVariable=true;
			aes.variable = dVariable;
			aes.defaultVariable = dVariable;			
		}
		
		return aes;
	}
}

