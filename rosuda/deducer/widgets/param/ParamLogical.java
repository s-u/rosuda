package org.rosuda.deducer.widgets.param;

public class ParamLogical extends Param{
	protected Boolean value;
	protected Boolean defaultValue;			//default	
	
	
	public ParamLogical(){
		name = "";
		title = "";
		value = null;
		defaultValue = null;
		view = VIEW_CHECK_BOX;
	}
	
	public ParamLogical(String nm){
		name = nm;
		title = nm;
		value = null;
		defaultValue = null;
		view = VIEW_CHECK_BOX;
	}
	
	public ParamLogical(String theName, String theTitle, String theView,
			Boolean theValue,Boolean theDefaultValue){
		name = theName;
		title = theTitle;
		view = theView;
		value = theValue;
		defaultValue = theDefaultValue;
		view = VIEW_CHECK_BOX;
	}
	
	public ParamWidget getView(){
		if(getViewType() == Param.VIEW_CHECK_BOX)
			return new ParamCheckBoxWidget(this);
		return null;
	}
	
	public Object clone(){
		ParamLogical p = new ParamLogical();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		p.setViewType(this.getViewType());
		if(value!=null)
			p.setValue(new Boolean(value.booleanValue()));
		if(defaultValue!=null)
			p.setDefaultValue(new Boolean(defaultValue.booleanValue()));
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			String val = "";
			if(getDefaultValue()==null || (getValue()!=null && !getDefaultValue().toString().equals(getValue().toString())))
				val = value.booleanValue() ? "TRUE" : "FALSE";
			else
				val ="";
			if(val.length()>0)
				calls = new String[]{getName() + " = "+val};
			else
				calls = new String[]{};
			
		}else
			calls = new String[]{};
	return calls;
	}
	
	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Boolean || defaultValue ==null)
			this.defaultValue = (Boolean) defaultValue;
		else
			System.out.println("ParamBoolean: invalid setDefaultValue");
	}
	
	public void setDefaultValue(boolean value){
		this.defaultValue = new Boolean(value);
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Boolean || value==null){
			this.value = (Boolean) value;
		}else{
			System.out.println("ParamNumeric: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public void setValue(boolean value){
		this.value = new Boolean(value);
	}
	
	public Object getValue() {
		return value;
	}
	
	
}
