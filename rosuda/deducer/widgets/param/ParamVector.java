package org.rosuda.deducer.widgets.param;

import javax.swing.DefaultComboBoxModel;

import org.rosuda.deducer.Deducer;

public class ParamVector extends Param{

	protected String[] value = new String[]{};
	protected String[] defaultValue = new String[]{};			//default	
	protected boolean numeric = true;
	
	protected Double lowerBound ;			//If bounded, the lower bound
	protected Double upperBound ;			//if bounded, the upper bound
	
	public ParamVector(){
		name = "";
		title = "";
		view = Param.VIEW_VECTOR_BUILDER;
	}
	
	public ParamVector(String nm){
		name = nm;
		title = nm;
		view = Param.VIEW_VECTOR_BUILDER;
	}
	
	public ParamVector(String theName, String theTitle, String theView,
			String[] theValue,String[] theDefaultValue,boolean isNumeric){
		name = theName;
		title = theTitle;
		view = theView;
		value = theValue;
		defaultValue = theDefaultValue;
		view = Param.VIEW_VECTOR_BUILDER;
		setNumeric(isNumeric);
	}
	
	public ParamWidget getView(){
		if(getViewType() == Param.VIEW_TWO_VALUE_ENTER)
			return new ParamTwoValueWidget(this);
		else if(getViewType() == Param.VIEW_VECTOR_BUILDER)
			return new ParamVectorBuilderWidget(this);
		return null;
	}
	
	public Object clone(){
		ParamVector p = new ParamVector();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		if(this.getLowerBound()!=null)
			p.setLowerBound(new Double(this.getLowerBound().doubleValue()));
		if(this.getUpperBound()!=null)
			p.setUpperBound(new Double(this.getUpperBound().doubleValue()));
		p.setViewType(this.getViewType());
		if(getValue()!=null){
			String[] oldV = (String[]) getValue();
			String[] v = new String[oldV.length];
			for(int i=0;i<oldV.length;i++)
				v[i] = oldV[i];
			p.setValue(v);			
		}
		if(getDefaultValue()!=null){
			String[] oldV = (String[]) getDefaultValue();
			String[] v = new String[oldV.length];
			for(int i=0;i<oldV.length;i++)
				v[i] = oldV[i];
			p.setDefaultValue(v);			
		}
		p.setNumeric(isNumeric());
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			String val ="";
			String[] vecVals = (String[]) getValue();
			String[] dvecVals = (String[]) getDefaultValue();
			boolean identical = true;
			if(vecVals==null)
				identical = true;
			else if(dvecVals==null)
				identical=false;
			else if(vecVals.length!=dvecVals.length)
				identical=false;
			else
				for(int i=0;i<vecVals.length;i++)
					if(!vecVals[i].equals(dvecVals[i]))
						identical=false;
			if(!identical && vecVals!=null){			
				val = Deducer.makeRCollection(new DefaultComboBoxModel(vecVals), "c",false);
			}else
				val="";
			if(val.length()>0)
				calls = new String[]{getName() + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
	return calls;
	}
	
	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof String[] || defaultValue ==null)
			this.defaultValue = (String[]) defaultValue;
		else
			System.out.println("ParamVector: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof String[] || value==null)
			this.value = (String[]) value;
		else{
			System.out.println("ParamVector: invalid setValue");
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

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public boolean isNumeric() {
		return numeric;
	}
}
