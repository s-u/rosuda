package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.ExDefaultTableModel;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class ParamScaleLegend extends Param{
	
	final public static String DATA_SCALE_NUMERIC = "scale numeric";
	final public static String DATA_SCALE_CHARACTER = "scale char";
	
	final public static String VIEW_SCALE = "scale";
	
	public ParamScaleLegend(){
		view = VIEW_SCALE;
	}
	
	public ParamScaleLegend(String nm){
		name=nm;
		title =nm;
		view = VIEW_SCALE;
	}
	
	public ParamWidget getView(){
		if(view.equals(ParamScaleLegend.VIEW_SCALE))
			return new ParamScaleWidget(this);
		return null;
	}
	
	public Object clone(){
		Param p = new ParamScaleLegend();
		p.name = this.name;
		p.title = this.title;
		p.dataType = this.dataType;
		p.value = this.view;
		if(this.lowerBound!=null)
			p.lowerBound = new Double(this.lowerBound.doubleValue());
		if(this.upperBound!=null)
			p.upperBound = new Double(this.upperBound.doubleValue());
		if(this.value!=null){
			Vector newValue = new Vector();
			Vector curValue = (Vector) this.value;
			newValue.add(curValue.get(0));
			newValue.add(new Boolean(((Boolean)curValue.get(1)).booleanValue()));
			ExDefaultTableModel curTm = (ExDefaultTableModel) curValue.get(2);
			ExDefaultTableModel tm = new ExDefaultTableModel();
			tm.setRowCount(curTm.getRowCount());
			tm.setColumnCount(curTm.getColumnCount());
			for(int i=0;i<curTm.getRowCount();i++){
				for(int j=0;j<curTm.getColumnCount();j++){
					tm.setValueAt(curTm.getValueAt(i, j), i, j);
				}
			}
			newValue.add(tm);
			p.value = newValue;
		}else
			p.value=null;
		return p;
	}
	
	public String[] getAesCalls(){
		return new String[]{};
	}
	
	public String[] getParamCalls(){
		String[] calls = new String[]{};
		if(value!=null && !value.equals(defaultValue)){
			Vector dBreaks = new Vector();
			Vector dLabels = new Vector();		
			String dNm = null;
			Boolean dShow = null;
			if(defaultValue!=null){
				Vector v = (Vector) defaultValue;
				dNm = (String) v.get(0);
				dShow = ((Boolean)v.get(1));
				ExDefaultTableModel dTm = (ExDefaultTableModel) v.get(2);
				for(int i=0;i<dTm.getRowCount();i++){
					String br = (String) dTm.getValueAt(i, 0);
					String lab = (String) dTm.getValueAt(i, 1);
					if(br!=null && br.length()>0){
						dBreaks.add(br);
						if(lab!=null)
							dLabels.add(lab);
						else
							dLabels.add("");
					}
				}
			}				
			if(value!=null){
				Vector v = (Vector) value;
				String nm = (String) v.get(0);
				Boolean show = ((Boolean)v.get(1));
				ExDefaultTableModel tm = (ExDefaultTableModel) v.get(2);
				Vector breaks = new Vector();
				Vector labels = new Vector();
				for(int i=0;i<tm.getRowCount();i++){
					String br = (String) tm.getValueAt(i, 0);
					String lab = (String) tm.getValueAt(i, 1);
					if(br!=null && br.length()>0){
						breaks.add(br);
						if(lab!=null)
							labels.add(lab);
						else
							labels.add("");
					}
				}
				
				String nameCall = null;
				if(nm!=null && nm!=dNm && !(dNm==null && nm.length()==0)){
					nameCall = "name = '" +nm+"'";
				}
				String showCall = null;
				if(show!=null && !show.equals(dShow) && !(dShow==null && show.booleanValue()))
					showCall = "legend = " + (show.booleanValue() ? "TRUE" : "FALSE");
				String breakCall = null;
				String labelCall = null;
				if(breaks.size()>0 && !(breaks.equals(dBreaks) && labels.equals(dLabels))){
					breakCall = "breaks = " + Deducer.makeRCollection(breaks, "c", 
							dataType == DATA_SCALE_CHARACTER);
					labelCall = "labels = " + Deducer.makeRCollection(labels, "c",true);
				}
				Vector callVector = new Vector();
				if(nameCall!=null)
					callVector.add(nameCall);
				if(showCall != null)
					callVector.add(showCall);
				if(breakCall!=null)
					callVector.add(breakCall);
				if(labelCall!=null)
					callVector.add(labelCall);
				calls = new String[callVector.size()];
				for(int i=0;i<callVector.size();i++)
					calls[i] = (String) callVector.get(i);
			}
		}
		return calls;
	}
	
}
