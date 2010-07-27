package org.rosuda.deducer.widgets.param;

public abstract class Param implements Cloneable{

	
	
	final public static String VIEW_ENTER = "enter";
	final public static String VIEW_ENTER_LONG = "enter long";
	final public static String VIEW_COMBO = "combo";
	final public static String VIEW_EDITABLE_COMBO = "edit combo";
	final public static String VIEW_CHECK_BOX = "check";
	final public static String VIEW_VECTOR_BUILDER = "vector";
	final public static String VIEW_TWO_VALUE_ENTER = "two value";
	final public static String VIEW_COLOR = "colour";
	final public static String VIEW_RFUNCTION = "r function view";
	
	
	protected String name;					//name of parameter
	protected String title;				//title to be displayed
	
	protected String[] options;			//passible parameter values
	protected String[] labels;				//descr of param values
	
	protected String view = VIEW_ENTER_LONG;
	
	protected Double lowerBound ;			//If bounded, the lower bound
	protected Double upperBound ;			//if bounded, the upper bound
	
	public Param(){}
	
	public Param(String nm){
		setName(nm);
		setTitle(nm);
	}
	
	public abstract ParamWidget getView();
	
	public abstract Object clone();
	
	public abstract String[] getParamCalls();
	
	
	public abstract void setValue(Object value);

	public abstract Object getValue() ;

	public abstract void setDefaultValue(Object defaultValue);

	public abstract Object getDefaultValue();

	public void setViewType(String view) {
		this.view = view;
	}

	public String getViewType() {
		return view;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}


	public void setOptions(String[] options) {
		this.options = options;
	}

	public String[] getOptions() {
		return options;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public String[] getLabels() {
		return labels;
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
