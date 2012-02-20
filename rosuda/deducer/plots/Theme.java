package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamAny;
import org.rosuda.deducer.widgets.param.ParamCharacter;
import org.rosuda.deducer.widgets.param.ParamColor;
import org.rosuda.deducer.widgets.param.ParamNone;
import org.rosuda.deducer.widgets.param.ParamNumeric;
import org.rosuda.deducer.widgets.param.RFunctionList;
import org.rosuda.deducer.widgets.param.ParamVector;
import org.rosuda.deducer.widgets.param.RFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Theme implements ElementModel{

	private String name;
	
	public Vector params = new Vector();
	
	public static Theme makeBw(){
		Theme t = new Theme();
		t.setName("theme_bw");
		
		ParamNumeric p;
		
		p = new ParamNumeric();
		p.setName("base_size");
		p.setTitle("base text size");
		p.setViewType(Param.VIEW_ENTER);
		p.setValue(new Double(12));
		p.setDefaultValue(new Double(12));
		p.setLowerBound(new Double(0));
		t.params.add(p);
		
		return t;
		
	}
	
	public static Theme makeGrey(){
		Theme t = new Theme();
		t.setName("theme_grey");
		
		ParamNumeric p;
		
		p = new ParamNumeric();
		p.setName("base_size");
		p.setTitle("base text size");
		p.setViewType(Param.VIEW_ENTER);
		p.setValue(new Double(12));
		p.setDefaultValue(new Double(12));
		p.setLowerBound(new Double(0));
		t.params.add(p);
		
		return t;
	}
	
	public static Theme makeXLab(){
		Theme t = new Theme();
		t.setName("xlab");
		ParamCharacter p;
		
		p = new ParamCharacter("label");
		t.params.add(p);
		
		return t;
	}
	
	public static Theme makeYLab(){
		Theme t = new Theme();
		t.setName("ylab");
		ParamCharacter p;
		
		p = new ParamCharacter("label");
		t.params.add(p);
		
		return t;
	}
	
	public static Theme makeOpts(){
		Theme t = new Theme();
		t.setName("opts");
		Param p;
		ParamNone pnone;
		RFunctionList pf;
		
		pnone = new ParamNone("Plot");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		p = new ParamCharacter();
		p.setName("title");
		p.setTitle("Title");
		p.setViewType(Param.VIEW_ENTER_LONG);
		t.params.add(p);		
		
		pf = new RFunctionList();
		pf.setName("plot.background");
		pf.setTitle("Background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("plot.title");
		pf.setTitle("Title text");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("plot.margin");
		pf.setTitle("Margin");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pnone = new ParamNone("Axis");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		
		pf = new RFunctionList();
		pf.setName("axis.line");
		pf.setTitle("Line");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);

		pf = new RFunctionList();
		pf.setName("axis.ticks");
		pf.setTitle("Ticks");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("axis.ticks.length");
		pf.setTitle("Tick length");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("axis.ticks.margin");
		pf.setTitle("Tick margin");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pnone = new ParamNone("x-Axis");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);

		pf = new RFunctionList();
		pf.setName("axis.title.x");
		pf.setTitle("Title");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("axis.text.x");
		pf.setTitle("Text");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pnone = new ParamNone("y-Axis");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		pf = new RFunctionList();
		pf.setName("axis.title.y");
		pf.setTitle("Title");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("axis.text.y");
		pf.setTitle("Text");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pnone = new ParamNone("Legend");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		pf = new RFunctionList();
		pf.setName("legend.background");
		pf.setTitle("Background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("legend.key");
		pf.setTitle("Key");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("legend.key.size");
		pf.setTitle("Key size");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("legend.text");
		pf.setTitle("Text");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("legend.title");
		pf.setTitle("Title");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		p = new ParamCharacter("Position");
		p.setName("position");
		p.setViewType(Param.VIEW_COMBO);
		p.setOptions(new String[] {"none","top","right","bottom","left"});
		t.params.add(p);
		
		p = new ParamVector("Position");
		p.setName("position");
		p.setViewType(Param.VIEW_TWO_VALUE_ENTER);
		p.setLowerBound(new Double(0));
		p.setUpperBound(new Double(1));
		t.params.add(p);
		
		p = new ParamNumeric("Justification");
		p.setLowerBound(new Double(0));
		p.setUpperBound(new Double(1));
		t.params.add(p);
		
		p = ParamFactory.makeParam("direction");
		p.setName("legend.direction");
		p.setTitle("Direction");
		t.params.add(pnone);
		
		pnone = new ParamNone("Panel");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		pf = new RFunctionList();
		pf.setName("panel.background");
		pf.setTitle("Background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("panel.border");
		pf.setTitle("Border");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("panel.grid.major");
		pf.setTitle("Grid lines (major)");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_line", makeThemeLine());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("panel.grid.minor");
		pf.setTitle("Grid lines (minor)");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_line", makeThemeLine());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("panel.margin");
		pf.setTitle("Margin");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pnone = new ParamNone("Strip");
		pnone.setViewType(ParamNone.VIEW_SEPERATOR);
		t.params.add(pnone);
		
		pf = new RFunctionList();
		pf.setName("strip.background");
		pf.setTitle("Background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("strip.text.x");
		pf.setTitle("Text (x)");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new RFunctionList();
		pf.setName("strip.text.y");
		pf.setTitle("Text (y)");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		return t;
	}
	
	public static RFunction makeThemeBlank(){
		RFunction rf = new RFunction();
		rf.setName("theme_blank");
		return rf;
	}
	
	public static RFunction makeThemeText(){
		RFunction rf = new RFunction();
		rf.setName("theme_text");
		
		Param rfp;
		ParamNumeric rfpn;
		ParamCharacter rfpc;
		
		rfpc = new ParamCharacter("family");
		rfpc.setTitle("font family");
		rfpc.setViewType(Param.VIEW_EDITABLE_COMBO);
		rfpc.setOptions(new String[] {"AvantGarde", "Bookman", "Courier", "Helvetica",
				"Helvetica-Narrow", "NewCenturySchoolbook", "Palatino" ,"Times", "URWGothic", 
				"URWBookman", "NimbusMon", "NimbusSan", "NimbusSanCond", "CenturySch", 
				"URWPalladio" ,"NimbusRom"});
		rf.add(rfpc);
		
		rfpc = new ParamCharacter("face");
		rfpc.setViewType(Param.VIEW_COMBO);
		rfpc.setOptions(new String[] {"plain","italic","bold"});
		rfpc.setValue("plain");
		rfpc.setDefaultValue("plain");
		rf.add(rfpc);
		
		rfp = new ParamColor("colour");
		rfp.setViewType(Param.VIEW_COLOR);
		rfp.setValue(Color.black);
		rfp.setDefaultValue(Color.black);
		rf.add(rfp);
		
		rfpn = new ParamNumeric();
		rfpn.setName("size");
		rfpn.setTitle("size");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(10));
		rfpn.setDefaultValue(new Double(10));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("vjust");
		rfpn.setTitle("vjust");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(.5));
		rfpn.setDefaultValue(new Double(.5));
		rfpn.setLowerBound(new Double(0));
		rfpn.setUpperBound(new Double(1));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("hjust");
		rfpn.setTitle("hjust");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(.5));
		rfpn.setDefaultValue(new Double(.5));
		rfpn.setLowerBound(new Double(0));
		rfpn.setUpperBound(new Double(1));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("angle");
		rfpn.setTitle("angle");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(0));
		rfpn.setDefaultValue(new Double(0));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("lineheight");
		rfpn.setTitle("lineheight");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(1.1));
		rfpn.setDefaultValue(new Double(1.1));
		rf.add(rfpn);
		return rf;
	}
	
	public static RFunction makeThemeSegment(){
		RFunction rf = new RFunction();
		rf.setName("theme_segment");
		
		Param rfp;
		ParamNumeric rfpn;
		
		rfp = new ParamColor("colour");
		rfp.setViewType(Param.VIEW_COLOR);
		rfp.setValue(Color.black);
		rfp.setDefaultValue(Color.black);
		rf.add(rfp);
		
		rfpn = new ParamNumeric();
		rfpn.setName("size");
		rfpn.setTitle("size");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(.5));
		rfpn.setDefaultValue(new Double(.5));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("linetype");
		rfpn.setTitle("linetype");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(1));
		rfpn.setDefaultValue(new Double(1));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		return rf;
	}
	
	public static RFunction makeThemeLine(){
		RFunction rf = new RFunction();
		rf.setName("theme_line");
		
		Param rfp;
		ParamNumeric rfpn;
		
		
		rfp = new ParamColor("colour");
		rfp.setViewType(Param.VIEW_COLOR);
		rfp.setValue(Color.black);
		rfp.setDefaultValue(Color.black);
		rf.add(rfp);
		
		rfpn = new ParamNumeric();
		rfpn.setName("size");
		rfpn.setTitle("size");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(.5));
		rfpn.setDefaultValue(new Double(.5));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("linetype");
		rfpn.setTitle("linetype");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(1));
		rfpn.setDefaultValue(new Double(1));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		return rf;
	}
	
	public static RFunction makeThemeRect(){
		RFunction rf = new RFunction();
		rf.setName("theme_rect");
		
		Param rfp;
		ParamNumeric rfpn;
		
		rfp = new ParamColor("fill");
		rfp.setViewType(Param.VIEW_COLOR);
		rf.add(rfp);
		
		rfp = new ParamColor("colour");
		rfp.setViewType(Param.VIEW_COLOR);
		rfp.setValue(Color.black);
		rfp.setDefaultValue(Color.black);
		rf.add(rfp);
		
		rfpn = new ParamNumeric();
		rfpn.setName("size");
		rfpn.setTitle("size");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(.5));
		rfpn.setDefaultValue(new Double(.5));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		
		rfpn = new ParamNumeric();
		rfpn.setName("linetype");
		rfpn.setTitle("linetype");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setValue(new Double(1));
		rfpn.setDefaultValue(new Double(1));
		rfpn.setLowerBound(new Double(0));
		rf.add(rfpn);
		return rf;
	}
	
	public static RFunction makeUnit(){
		RFunction rf = new RFunction();
		rf.setName("unit");
		Param rfp;
		ParamVector rfpn;
		
		rfpn = new ParamVector();
		rfpn.setName("x");
		rfpn.setTitle("plot margins");
		rfpn.setNumeric(true);
		rf.add(rfpn);		
		
		rfp = new ParamCharacter("units");
		rfp.setTitle("units");
		rfp.setViewType(Param.VIEW_EDITABLE_COMBO);
		rfp.setOptions(new String[] {"npc", "cm", "inches", "mm",
				"points", "picas", "bigpts" ,"dida", "cicero", 
				"scaledpts", "lines", "char", "native", "snpc", 
				"strwidth" ,"strheight","grobwidth","grobheight"});
		rf.add(rfp);
		

		
		return rf;
	}
	
	public static Theme makeTheme(String name){
		if(name.equals("bw"))
			return makeBw();
		else if(name.equals("grey"))
			return makeGrey();
		else if(name.equals("opts"))
			return makeOpts();
		else if(name.equals("xlab"))
			return makeXLab();
		else if(name.equals("ylab"))
			return makeYLab();
		return null;
	}
	
	public Object clone(){
		Theme s = new Theme();		
		try{	
			for(int i=0;i<params.size();i++)
				s.params.add(((Param)params.get(i)).clone());
			s.setName(name);
		}catch(Exception e){
			e.printStackTrace();
		}
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
		String call = Deducer.makeRCollection(paramCalls, getName(), false);
		return call;
	}

	public String getType() {
		return "theme";
	}

	public ElementView getView() {
		return new DefaultElementView(this);
	}


	public Vector getParams() {
		return params;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public String getData() {
		return null;
	}	
	
	public Element toXML() {
		try{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element node = doc.createElement("ElementModel");
			node.setAttribute("className", "org.rosuda.deducer.plots.Theme");
			if(name!=null)
				node.setAttribute("name", name);
			
			for(int i=0;i<params.size();i++){
				Element pEl = ((Param)params.get(i)).toXML();
				pEl = (Element) doc.importNode(pEl, true);
				node.appendChild(pEl);
			}
			doc.appendChild(node);
			return node;
		}catch(Exception ex){ex.printStackTrace();return null;}		
	}

	public void setFromXML(Element node) {
		String cn = node.getAttribute("className");
		if(!cn.equals("org.rosuda.deducer.plots.Theme")){
			System.out.println("Error Theme: class mismatch: " + cn);
			(new Exception()).printStackTrace();
		}
		if(node.hasAttribute("name"))
			name = node.getAttribute("name");
		else 
			name = null;
		params = new Vector();
		NodeList nl = node.getChildNodes();
		for(int i=0;i<nl.getLength();i++){
			if(!(nl.item(i) instanceof Element))
				continue;
			Element n = (Element) nl.item(i);
			cn = n.getAttribute("className");
			Param p = Param.makeParam(cn);
			p.setFromXML(n);
			params.add(p);
		}
	}	
	
	
}
