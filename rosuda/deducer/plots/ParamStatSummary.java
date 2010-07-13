package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class ParamStatSummary extends Param{

	final public static String DATA_SUMMARY= "summary";
	
	final public static String VIEW_SUMMARY = "summary";
	
	public ParamStatSummary(){
		view = VIEW_SUMMARY;
		Vector newValue = new Vector();
		newValue.add(new Integer(0));
		newValue.add("0.95");
		newValue.add("1");
		newValue.add("1000");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		value = newValue;
		
		newValue = new Vector();
		newValue.add(new Integer(-1));
		newValue.add("0.95");
		newValue.add("2");
		newValue.add("1000");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		newValue.add("");
		defaultValue = newValue;
	}
	
	public ParamStatSummary(String nm){
		this();
		name=nm;
		title =nm;
	}
	
	public ParamWidget getView(){
		if(view.equals(VIEW_SUMMARY))
			return new ParamStatSummaryWidget(this);
		return null;
	}
	
	public Object clone(){
		Param p = new ParamStatSummary();
		p.name = this.name;
		p.title = this.title;
		p.dataType = this.dataType;
		p.value = this.view;
		if(this.lowerBound!=null)
			p.lowerBound = new Double(this.lowerBound.doubleValue());
		if(this.upperBound!=null)
			p.upperBound = new Double(this.upperBound.doubleValue());
		if(this.value!=null){
			Vector val = (Vector) value;
			Vector newVal = new Vector();
			newVal.add(new Integer(((Integer)val.get(0)).intValue()));
			for(int i=1;i<val.size();i++)
				newVal.add(val.get(i));
			p.value = newVal;
		}else
			p.value=null;
		return p;
	}
	
	public String[] getAesCalls(){
		return new String[]{};
	}
	
	public String[] getParamCalls(){
		String[] calls = new String[]{};
		Vector val = (Vector) value;
		if(value!=null ){
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
}
