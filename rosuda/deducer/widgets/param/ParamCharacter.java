package org.rosuda.deducer.widgets.param;

import org.rosuda.deducer.Deducer;

public class ParamCharacter extends Param{
	
	protected String value;
	protected String defaultValue;			//default	
	

	
	public ParamCharacter(){
		name = "";
		title = "";
		value = "";
		defaultValue = "";
		view = VIEW_ENTER_LONG;
	}
	
	public ParamCharacter(String nm){
		name = nm;
		title = nm;
		value = "";
		defaultValue = "";
		view = VIEW_ENTER_LONG;
	}
	
	public ParamCharacter(String theName, String theTitle, String theView,
			String theValue,String theDefaultValue){
		name = theName;
		title = theTitle;
		view = theView;
		value = theValue;
		defaultValue = theDefaultValue;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamWidget getView(){
		if(getViewType() == Param.VIEW_ENTER || 
				getViewType() == Param.VIEW_ENTER_LONG)
			return new ParamTextFieldWidget(this);
		else if(getViewType() == Param.VIEW_COMBO || 
				getViewType() == Param.VIEW_EDITABLE_COMBO)
			return new ParamComboBoxWidget(this);
		
		return null;
	}
	
	public Object clone(){
		Param p = new ParamCharacter();
		p.setName(this.name);
		p.setTitle(this.title);
		p.setValue(this.value);
		p.setDefaultValue(this.defaultValue);
		if(this.getOptions()!=null){
			String[] v = new String[this.getOptions().length];
			for(int i=0;i<this.getOptions().length;i++)
				v[i] = this.getOptions()[i];
			p.setOptions(v);
		}
		if(this.getLabels()!=null){
			String[] v = new String[this.getLabels().length];
			for(int i=0;i<this.getLabels().length;i++)
				v[i] = this.getLabels()[i];
			p.setLabels(v);
		}
		p.setViewType(this.getViewType());
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(value!=null && !value.equals(defaultValue)){
			String val = "";
			if(getValue().toString().length()>0)
				val = "'" + Deducer.addSlashes(getValue().toString()) + "'";
			if(val.length()>0)
				calls = new String[]{name + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
		return calls;
	}

	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof String || value ==null)
			this.defaultValue = (String) defaultValue;
		else
			System.out.println("ParamCharacter: invalid setDefaultValue");
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setValue(Object value) {
		if(value instanceof String || value==null)
			this.value = (String) value;
		else
			System.out.println("ParamCharacter: invalid setValue");
	}
	public Object getValue() {
		return value;
	}
}
