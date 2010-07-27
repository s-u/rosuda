package org.rosuda.deducer.widgets.param;

import java.awt.Color;

import org.rosuda.deducer.Deducer;

public class ParamColor extends Param{
	protected Color value;
	protected Color defaultValue;			//default	
	

	
	public ParamColor(){
		name = "";
		title = "";
		value = null;
		defaultValue = null;
		view = VIEW_COLOR;
	}
	
	public ParamColor(String nm){
		name = nm;
		title = nm;
		value = null;
		defaultValue = null;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamColor(String theName, String theTitle, String theView,
			String theValue,String theDefaultValue){
		name = theName;
		title = theTitle;
		view = theView;
		value = Color.decode(theValue);
		defaultValue = Color.decode(theDefaultValue);
		view = VIEW_ENTER_LONG;
	}
	
	public ParamColor(String theName, String theTitle, String theView,
			Color theValue,Color theDefaultValue){
		name = theName;
		title = theTitle;
		view = theView;
		value = theValue;
		defaultValue =theDefaultValue;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamWidget getView(){
		if(getViewType() == Param.VIEW_COLOR)
			return new ParamColorWidget(this);
		return null;
	}
	
	public Object clone(){
		Param p = new ParamColor();
		p.setName(this.name);
		p.setTitle(this.title);
		if(getValue()!=null){
			Color c = (Color) getValue();
			p.setValue(new Color(c.getRGB()));
		}
		if(getDefaultValue()!=null){
			Color c = (Color) getDefaultValue();
			p.setDefaultValue(new Color(c.getRGB()));
		}
		p.setViewType(this.getViewType());
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(value!=null && !value.equals(defaultValue)){
			String val = "";
			if(getValue().toString().length()>0)
				val = "'#"+ Integer.toHexString(((Color)getValue()).getRGB()).substring(2)+"'";
			if(val.length()>0)
				calls = new String[]{name + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
		return calls;
	}

	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Color || value ==null)
			this.defaultValue = (Color) defaultValue;
		else
			System.out.println("ParamColor: invalid setDefaultValue");
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setValue(Object value) {
		if(value instanceof Color || value==null)
			this.value = (Color) value;
		else
			System.out.println("ParamColor: invalid setValue");
	}
	public Object getValue() {
		return value;
	}
}
