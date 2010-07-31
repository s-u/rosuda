package org.rosuda.deducer.widgets.param;

import java.awt.Color;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.rosuda.deducer.plots.Coord;
import org.rosuda.deducer.plots.Layer;
import org.rosuda.deducer.plots.ParamFacet;
import org.rosuda.deducer.plots.ParamScaleLegend;
import org.rosuda.deducer.plots.PlottingElement;
import org.rosuda.deducer.plots.Stat;
import org.rosuda.deducer.plots.Theme;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

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
	
	public void setLowerBound(double lowerBound) {
		this.lowerBound = new Double(lowerBound);
	}

	public Double getLowerBound() {
		return lowerBound;
	}

	public void setUpperBound(Double upperBound) {
		this.upperBound = upperBound;
	}
	
	public void setUpperBound(double upperBound) {
		this.upperBound = new Double(upperBound);
	}

	public Double getUpperBound() {
		return upperBound;
	}
	
	public Element toXML(){
		try{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element node = doc.createElement("Param");
			if(name!=null)
				node.setAttribute("name", name);
			if(title!=null)
				node.setAttribute("title", title);
			if(view!=null)
				node.setAttribute("viewType", getViewType());
			
			Element optionNode = doc.createElement("options");
			if(options!=null)
				for(int i=0;i<options.length;i++)
					optionNode.setAttribute("element_"+i, options[i]);
			node.appendChild(optionNode);
        
			Element labelsNode = doc.createElement("labels");
			if(labels!=null)
				for(int i=0;i<labels.length;i++)
					labelsNode.setAttribute("element_"+i, labels[i]);
			node.appendChild(labelsNode);
			
			if(lowerBound!=null)
				node.setAttribute("lowerBound", lowerBound.toString());
			if(upperBound!=null)
				node.setAttribute("upperBound", upperBound.toString());
			
			doc.appendChild(node);
			return node;
			
        }catch(Exception e){e.printStackTrace();return null;}
	}
	
	public void setFromXML(Element node){
		if(node.hasAttribute("name"))
			name = node.getAttribute("name");
		else
			name = null;
		if(node.hasAttribute("title"))
			title = node.getAttribute("title");		
		else
			title = null;
		if(node.hasAttribute("viewType"))
			view = node.getAttribute("viewType");
		else
			view = VIEW_ENTER_LONG;
		
		Node optionNode =node.getElementsByTagName("options").item(0);
		NamedNodeMap attr = optionNode.getAttributes();
		if(attr.getLength()>0){
			options = new String[attr.getLength()];
			for(int i=0;i<attr.getLength();i++)
				options[i] = attr.item(i).getNodeValue();
		}
		
		Node labelsNode =node.getElementsByTagName("labels").item(0);
		attr = labelsNode.getAttributes();
		if(attr.getLength()>0){
			labels = new String[attr.getLength()];
			for(int i=0;i<attr.getLength();i++)
				labels[i] = attr.item(i).getNodeValue();
		}
		if(node.hasAttribute("lowerBound"))
			lowerBound = new Double(Double.parseDouble(node.getAttribute("lowerBound")));
		else
			lowerBound = null;
		if(node.hasAttribute("upperBound"))
			upperBound = new Double(Double.parseDouble(node.getAttribute("upperBound")));
		else
			upperBound = null;
	}
	
	public static Param makeParam(String className){
		Param p;
		try {
			p = (Param) Class.forName(className).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		return p;
	}
	
}
