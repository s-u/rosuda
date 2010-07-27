package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class ParamStatSummary extends Param{

	protected Vector value;
	protected Vector defaultValue;
	
	final public static String VIEW_SUMMARY = "summary";
	
	public ParamStatSummary(){
		setViewType(VIEW_SUMMARY);
		Vector newValue = new Vector();
		newValue.add(new Integer(0));
		newValue.add("0.95");
		newValue.add("1");
		newValue.add("1000");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		setValue(newValue);
		
		newValue = new Vector();
		newValue.add(new Integer(-1));
		newValue.add("0.95");
		newValue.add("2");
		newValue.add("1000");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		setDefaultValue(newValue);
	}
	
	public ParamStatSummary(String nm){
		this();
		setName(nm);
		setTitle(nm);
	}
	
	public ParamWidget getView(){
		if(getViewType().equals(VIEW_SUMMARY))
			return new ParamStatSummaryWidget(this);
		return null;
	}
	
	public Object clone(){
		Param p = new ParamStatSummary();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		p.setViewType(this.getViewType());
		if(this.getLowerBound()!=null)
			p.setLowerBound(new Double(this.getLowerBound().doubleValue()));
		if(this.getUpperBound()!=null)
			p.setUpperBound(new Double(this.getUpperBound().doubleValue()));
		if(this.getValue()!=null){
			Vector val = (Vector) getValue();
			Vector newVal = new Vector();
			newVal.add(new Integer(((Integer)val.get(0)).intValue()));
			for(int i=1;i<val.size();i++)
				newVal.add(val.get(i));
			p.setValue(newVal);
		}else
			p.setValue(null);
		return p;
	}
	
	public String[] getAesCalls(){
		return new String[]{};
	}
	
	public String[] getParamCalls(){
		String[] calls = new String[]{};
		Vector val = (Vector) getValue();
		if(getValue()!=null ){
			int sel = ((Integer)val.get(0)).intValue();
			if(sel==0){
				calls = new String[]{"fun.data = mean_sdl",
									"mult = " + val.get(2).toString()};
			}
			if(sel==1){
				calls = new String[]{"fun.data = mean_cl_normal",
									"conf.int = " + val.get(1).toString()};
			}
			if(sel==2){
				calls = new String[]{"fun.data = median_hilow",
									"conf.int = " + val.get(1).toString()};
			}
			if(sel==3){
				calls = new String[]{"fun.data = mean_cl_boot",
									"conf.int = " + val.get(1).toString(),
									"B = " + val.get(3).toString()};
			}
			if(sel==4){
				Vector v = new Vector();
				String y = val.get(4).toString();
				String ymin = val.get(5).toString();
				String ymax = val.get(6).toString();
				String data = val.get(7).toString();
				if(!data.equals("")){
					return new String[]{"fun.data = " + data};
				}
				if(!y.equals(""))
					v.add("fun.y = "+y);
				if(!ymin.equals(""))
					v.add("fun.ymin = "+ymin);
				if(!ymax.equals(""))
					v.add("fun.ymax = "+ymax);
				calls = new String[v.size()];
				for(int i=0;i<v.size();i++)
					calls[i] = v.get(i).toString();
			}
			
		}
		return calls;
	}

	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Vector || defaultValue ==null)
			this.defaultValue = (Vector) defaultValue;
		else
			System.out.println("ParamStatSummary: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Vector || value==null)
			this.value = (Vector) value;
		else{
			System.out.println("ParamStatSummary: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public Object getValue() {
		return value;
	}
}
