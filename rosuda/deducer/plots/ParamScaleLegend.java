package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.data.ExDefaultTableModel;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParamScaleLegend extends Param{
	
	
	final public static String VIEW_SCALE = "scale";
	
	protected Vector value;
	protected Vector defaultValue;
	protected boolean numeric =true;
	
	public ParamScaleLegend(){
		setViewType(VIEW_SCALE);
	}
	
	public ParamScaleLegend(String nm){
		setName(nm);
		setTitle(nm);
		setViewType(VIEW_SCALE);
	}
	
	public ParamWidget getView(){
		if(getViewType().equals(ParamScaleLegend.VIEW_SCALE))
			return new ParamScaleWidget(this);
		System.out.println("invalid view");
		(new Exception()).printStackTrace();
		return null;
	}
	
	public Object clone(){
		ParamScaleLegend p = new ParamScaleLegend();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		p.setViewType(this.getViewType());
		p.setNumeric(this.isNumeric());
		if(this.getLowerBound()!=null)
			p.setLowerBound(new Double(this.getLowerBound().doubleValue()));
		if(this.getUpperBound()!=null)
			p.setUpperBound(new Double(this.getUpperBound().doubleValue()));
		if(this.getValue()!=null){
			Vector newValue = new Vector();
			Vector curValue = (Vector) this.getValue();
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
			p.setValue(newValue);
		}else
			p.setValue(null);
		return p;
	}
	
	public String[] getAesCalls(){
		return new String[]{};
	}
	
	public String[] getParamCalls(){
		String[] calls = new String[]{};
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			Vector dBreaks = new Vector();
			Vector dLabels = new Vector();		
			String dNm = null;
			Boolean dShow = null;
			if(getDefaultValue()!=null){
				Vector v = (Vector) getDefaultValue();
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
			if(getValue()!=null){
				Vector v = (Vector) getValue();
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
							!numeric);
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

	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Vector || defaultValue ==null)
			this.defaultValue = (Vector) defaultValue;
		else
			System.out.println("ParamScaleLegend: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Vector || value==null)
			this.value = (Vector) value;
		else{
			System.out.println("ParamScaleLegend: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public Object getValue() {
		return value;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public boolean isNumeric() {
		return numeric;
	}
	
	public Element toXML(){
		Element e = super.toXML();
		Document doc = e.getOwnerDocument();
		if(value!=null){
			Vector v = (Vector) getValue();
			String text = (String) v.get(0);
			Boolean show = (Boolean) v.get(1);
			ExDefaultTableModel tm = (ExDefaultTableModel) v.get(2);
			Element node = doc.createElement("value");
			node.setAttribute("legendTitle", text);
			node.setAttribute("show", show.toString());
			node.setAttribute("nrow", tm.getRowCount()+"");
			
			Element cNode = doc.createElement("column_0");
			for(int i=0;i<tm.getRowCount();i++)
				cNode.setAttribute("element_"+i, tm.getValueAt(i, 0)==null ? null : tm.getValueAt(i, 0).toString());
			node.appendChild(cNode);
			
			cNode = doc.createElement("column_1");
			for(int i=0;i<tm.getRowCount();i++)
				cNode.setAttribute("element_"+i, tm.getValueAt(i, 1)==null ? null : tm.getValueAt(i, 1).toString());
			node.appendChild(cNode);
			e.appendChild(node);
		}
		
		if(defaultValue!=null){
			Vector v = (Vector) getDefaultValue();
			String text = (String) v.get(0);
			Boolean show = (Boolean) v.get(1);
			ExDefaultTableModel tm = (ExDefaultTableModel) v.get(2);
			Element node = doc.createElement("defaultValue");
			node.setAttribute("legendTitle", text);
			node.setAttribute("show", show.toString());
			node.setAttribute("nrow", tm.getRowCount()+"");
			
			Element cNode = doc.createElement("column_0");
			for(int i=0;i<tm.getRowCount();i++)
				cNode.setAttribute("element_"+i, tm.getValueAt(i, 0)==null ? null : tm.getValueAt(i, 0).toString());
			node.appendChild(cNode);
			
			cNode = doc.createElement("column_1");
			for(int i=0;i<tm.getRowCount();i++)
				cNode.setAttribute("element_"+i, tm.getValueAt(i, 1)==null ? null : tm.getValueAt(i, 1).toString());
			node.appendChild(cNode);
			e.appendChild(node);
		}
		
		e.setAttribute("className", "org.rosuda.deducer.plots.ParamScaleLegend");
		return e;
	}
	
	public void setFromXML(Element node){
		String cn = node.getAttribute("className");
		if(!cn.equals("org.rosuda.deducer.plots.ParamScaleLegend")){
			System.out.println("Error ParamScaleLegend: class mismatch: " + cn);
			(new Exception()).printStackTrace();
		}
		super.setFromXML(node);
		NodeList valEls = node.getElementsByTagName("value");
		value = null;
		defaultValue = null;
		if(valEls.getLength()>0){
			Element e = (Element) valEls.item(0);
			Vector v = new Vector();
			v.add(e.getAttribute("legendTitle"));
			v.add(new Boolean(e.getAttribute("show").equals("true")));
			ExDefaultTableModel tm = new ExDefaultTableModel();
			tm.addColumn("value");
			tm.addColumn("label");
			tm.setRowCount(Integer.parseInt(e.getAttribute("nrow")));
			
			Node cNode =e.getElementsByTagName("column_0").item(0);
			NamedNodeMap attr = cNode.getAttributes();
			Node c1Node =e.getElementsByTagName("column_1").item(0);
			NamedNodeMap attr1 = c1Node.getAttributes();
			if(attr.getLength()>0){
				for(int i=0;i<attr.getLength();i++){
					tm.setValueAt(attr.item(i).getNodeValue(),i,0);
					tm.setValueAt(attr1.item(i).getNodeValue(),i,1);
				}
			}
			v.add(tm);
			value = v;
		}
		NodeList deValEls = node.getElementsByTagName("defaultValue");
		if(deValEls.getLength()>0){
			Element e = (Element) deValEls.item(0);
			Vector v = new Vector();
			v.add(e.getAttribute("legendTitle"));
			v.add(new Boolean(e.getAttribute("show").equals("true")));
			ExDefaultTableModel tm = new ExDefaultTableModel();
			tm.addColumn("value");
			tm.addColumn("label");
			tm.setRowCount(Integer.parseInt(e.getAttribute("nrow")));
			
			Node cNode =e.getElementsByTagName("column_0").item(0);
			NamedNodeMap attr = cNode.getAttributes();
			Node c1Node =e.getElementsByTagName("column_1").item(0);
			NamedNodeMap attr1 = c1Node.getAttributes();
			if(attr.getLength()>0){
				for(int i=0;i<attr.getLength();i++){
					tm.setValueAt(attr.item(i).getNodeValue(),i,0);
					tm.setValueAt(attr1.item(i).getNodeValue(),i,1);
				}
			}
			v.add(tm);
			defaultValue = v;
		}
	}
	
	
}
