package org.rosuda.deducer.widgets.param;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ParamRFunction extends Param{
	
	protected Vector value;
	protected Vector defaultValue;			//default	
	
	
	public ParamRFunction(){
		name = "";
		title = "";
		value = null;
		defaultValue = null;
		view = VIEW_RFUNCTION;
	}
	
	public ParamRFunction(String nm){
		name = nm;
		title = nm;
		value = null;
		defaultValue = null;
		view = VIEW_RFUNCTION;
	}
	
	public ParamWidget getView(){
		if(getViewType().equals(Param.VIEW_RFUNCTION))
			return new ParamRFunctionWidget(this);
		System.out.println("invalid view");
		(new Exception()).printStackTrace();
		return null;
	}
	
	public Object clone(){
		ParamRFunction p = new ParamRFunction();
		p.setName(this.getName());
		p.setTitle(this.getTitle());
		p.setViewType(this.getViewType());
		if(this.getOptions()!=null){
			String[] v = new String[this.getOptions().length];
			for(int i=0;i<this.getOptions().length;i++)
				v[i] = this.getOptions()[i];
			p.setOptions(v);
		}
		if(getValue()!=null){
			Vector v = (Vector) getValue();
			Vector vNew = new Vector();
			vNew.add(v.get(0));
			HashMap hm = (HashMap) v.get(1);
			HashMap newHm = new HashMap();
			for(int i=0;i<getOptions().length;i++)
				newHm.put(getOptions()[i], ((RFunction)hm.get(getOptions()[i])).clone());
			vNew.add(newHm);
			p.setValue(vNew);
		}
		if(getDefaultValue()!=null){
			Vector v = (Vector) getDefaultValue();
			Vector vNew = new Vector();
			vNew.add(v.get(0));
			HashMap hm = (HashMap) v.get(1);
			HashMap newHm = new HashMap();
			for(int i=0;i<getOptions().length;i++)
				newHm.put(getOptions()[i], ((RFunction)hm.get(getOptions()[i])).clone());
			vNew.add(newHm);
			p.setDefaultValue(vNew);				
		}
		
		return p;
	}
	
	public String[] getParamCalls(){
		String[] calls;
		if(getValue()!=null && !getValue().equals(getDefaultValue())){
			String val="";
			Vector v = (Vector) getValue();
			String fName = (String) v.get(0);
			RFunction rf = (RFunction) ((HashMap)v.get(1)).get(fName);
			if(rf != null)
				val = rf.getCall();
			else
				val = "";
			if(val.length()>0)
				calls = new String[]{getName() + " = "+val};
			else
				calls = new String[]{};
		}else
			calls = new String[]{};
		return calls;
	}
	
	public void setDefaultValue(Object defaultValue) {
		if(defaultValue instanceof Vector || defaultValue ==null)
			this.defaultValue = (Vector) defaultValue;
		else
			System.out.println("ParamRFunction: invalid setDefaultValue");
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Object value) {
		if(value instanceof Vector || value==null)
			this.value = (Vector) value;
		else{
			System.out.println("ParamRFunction: invalid setValue");
			Exception e = new Exception();
			e.printStackTrace();
		}
	}
	
	public Object getValue() {
		return value;
	}
	
	public void addRFunction(String name,RFunction rf){
		if(value==null){
			value = new Vector();
			value.add("");
			value.add(new HashMap());
			setOptions(new String[]{});
		}
		HashMap hm = (HashMap) value.get(1);
		int l = getOptions().length;
		String[] opts = new String[l+1];
		for(int i=0;i<l;i++)
			opts[i] = getOptions()[i];
		opts[l] = name;
		setOptions(opts);
		hm.put(name, rf);
	}
	
	
	public Element toXML(){
		Element e = super.toXML();
		Document doc = e.getOwnerDocument();
		if(value!=null){
			if(value.get(0)!=null)
				e.setAttribute("selectedFunction", value.get(0).toString());
			HashMap hm = (HashMap) value.get(1);
			Element el = doc.createElement("valueMap");
			for(int i=0;i<options.length;i++){
				RFunction fn = (RFunction) hm.get(options[i]);
				Element element = fn.toXML();
				element = (Element) doc.importNode(element, true);
				Element element1 = doc.createElement("keyValuePair");
				element1.setAttribute("key", options[i]);
				element1.appendChild(element);
				el.appendChild(element1);
			}
			e.appendChild(el);
		}
		if(defaultValue!=null){
			if(defaultValue.get(0)!=null)
				e.setAttribute("defaultSelectedFunction", defaultValue.get(0).toString());
			HashMap hm = (HashMap) defaultValue.get(1);
			Element el = doc.createElement("defaultValueMap");
			for(int i=0;i<options.length;i++){
				RFunction fn = (RFunction) hm.get(options[i]);
				Element element = fn.toXML();
				element = (Element) doc.importNode(element, true);
				Element element1 = doc.createElement("keyValuePair");
				element1.setAttribute("key", options[i]);
				element1.appendChild(element1);
				el.appendChild(element1);
			}
			e.appendChild(el);			
		}
		e.setAttribute("className", "org.rosuda.deducer.widgets.param.ParamRFunction");
		return e;
	}
	
	public void setFromXML(Element node){
		String cn = node.getAttribute("className");
		if(!cn.equals("org.rosuda.deducer.widgets.param.ParamRFunction")){
			System.out.println("Error ParamRFunction: class mismatch: " + cn);
			(new Exception()).printStackTrace();
		}
		super.setFromXML(node);
		
		value = null;
		defaultValue = null;
		if(node.hasAttribute("selectedFunction")){
			String selectedFunction = null;
			if(node.hasAttribute("selectedFunction"))
				selectedFunction = node.getAttribute("selectedFunction");
			HashMap hm = new HashMap();
			Element map = (Element) node.getElementsByTagName("valueMap").item(0);
			NodeList pairs = map.getElementsByTagName("keyValuePair");
			for(int i=0;i<pairs.getLength();i++){
				Element pair = (Element) pairs.item(i);
				String key = pair.getAttribute("key");
				Element val = (Element) pair.getElementsByTagName("*").item(0);
				RFunction rf = new RFunction();
				rf.setFromXML(val);
				hm.put(key, rf);
			}
			Vector v = new Vector();
			v.add(selectedFunction);
			v.add(hm);
			value = v;
		}
		if(node.hasAttribute("defaultSelectedFunction")){
			String selectedFunction = null;
			if(node.hasAttribute("selectedFunction"))
				selectedFunction = node.getAttribute("defaultSelectedFunction");
			HashMap hm = new HashMap();
			Element map = (Element) node.getElementsByTagName("defaultValueMap").item(0);
			NodeList pairs = map.getChildNodes();
			for(int i=0;i<pairs.getLength();i++){
				Element pair = (Element) pairs.item(i);
				String key = pair.getAttribute("key");
				Element val = (Element) pair.getFirstChild();
				RFunction rf = new RFunction();
				rf.setFromXML(val);
				hm.put(key, rf);
			}
			Vector v = new Vector();
			v.add(selectedFunction);
			v.add(hm);
			defaultValue = v;
		}
	}
	
	
}
