package org.rosuda.deducer.widgets.param;

public class ParamNumeric extends Param{

	
	protected Double value;
	protected Double defaultValue;			//default	
	
	protected Double lowerBound ;			//If bounded, the lower bound
	protected Double upperBound ;			//if bounded, the upper bound
	
	public ParamNumeric(){
		name = "";
		title = "";
		value = null;
		defaultValue = null;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamNumeric(String nm){
		name = nm;
		title = nm;
		value = null;
		defaultValue = null;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamNumeric(String theName, String theTitle, String theView,
			Double theValue,Double theDefaultValue){
		name = theName;
		title = theTitle;
		view = theView;
		value = theValue;
		defaultValue = theDefaultValue;
		view = VIEW_ENTER_LONG;
	}
	
	public ParamWidget getView(){
		if(view == ParamNumeric.VIEW_COMBO || view == ParamNumeric.VIEW_EDITABLE_COMBO)
			return new ParamComboBoxWidget(this);
		else
			return new ParamTextFieldWidget(this);
	}
	
	public Object clone(){
		ParamNumeric p = new ParamNumeric();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		if(this.getLowerBound()!=null)
			p.setLowerBound(new Double(this.getLowerBound().doubleValue()));
		if(this.getUpperBound()!=null)
			p.setUpperBound(new Double(this.getUpperBound().doubleValue()));
		p.setViewType(this.getViewType());
		if(value!=null)
			p.setValue(new Double(value.doubleValue()));
		if(defaultValue!=null)
			p.setDefaultValue(new Double(defaultValue.doubleValue()));
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
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			String val = "";
			if(getDefaultValue()==null || (getValue()!=null && !getDefaultValue().toString().equals(getValue().toString())))
				val = getValue().toString();
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
		if(defaultValue instanceof Double || defaultValue ==null)
			this.defaultValue = (Double) defaultValue;
		else
			System.out.println("ParamNumeric: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Double || value==null)
			this.value = (Double) value;
		else if(value instanceof String){
			this.value =new Double(Double.parseDouble(value.toString()));
		}else{
			System.out.println("ParamNumeric: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public Object getValue() {
		return value;
	}
	public void setLowerBound(Double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}

	public Double getUpperBound() {
		return upperBound;
	}
	
}
