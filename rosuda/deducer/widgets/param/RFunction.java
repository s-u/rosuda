package org.rosuda.deducer.widgets.param;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.rosuda.deducer.Deducer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RFunction {

	private String name;
	private Vector params = new Vector();
	
	public Vector getParams(){return params;}
	public void setParams(Vector p){params = p;}
	public String getName(){return name;}
	public void setName(String n){name = n;}
	
	
	public Object clone(){
		RFunction s = new RFunction();
		for(int i=0;i<params.size();i++)
			s.params.add(((Param)params.get(i)).clone());
		s.name = name;
		return s;
	}

	
	public String checkValid() {
		return null;
	}

	public String getCall() {
		Vector paramCalls = new Vector();
		for(int i=0;i<params.size();i++){
			Param prm = (Param) params.get(i);

			String[] p = prm.getParamCalls();
			for(int j=0;j<p.length;j++)
				paramCalls.add(p[j]);				
		}
		String call = Deducer.makeRCollection(paramCalls, name, false);
		return call;
	}


	public RFunctionView getView() {
		return new DefaultRFunctionView(this);
	}
	
	public void add(Param p){
		params.add(p);
	}
	public Param get(int i){
		return (Param) params.get(i);
	}
	public void remove(Param p){
		params.remove(p);
	}
	
	public Element toXML(){
		try{
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		Element node = doc.createElement("RFunction");
		if(name!=null)
			node.setAttribute("name", name);
		for(int i=0;i<params.size();i++){
			Param p = (Param) params.get(i);
			Element el = p.toXML();
			Node n = doc.importNode(el, true);
			node.appendChild(n);
		}
		doc.appendChild(node);
		return node;
		
		}catch(Exception e){e.printStackTrace();return null;}
	}
	
	public void setFromXML(Element node){
		if(node.hasAttribute("name"))
			name = node.getAttribute("name");
		else
			name = null;
		params = new Vector();
		NodeList nl = node.getElementsByTagName("Param");
		for(int i=0;i<nl.getLength();i++){
			Element n = (Element) nl.item(i);
			String cn = n.getAttribute("className");
			Param p = Param.makeParam(cn);
			p.setFromXML(n);
			params.add(p);
		}
	}
	
}
