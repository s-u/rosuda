package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamCharacter;
import org.rosuda.deducer.widgets.param.ParamColor;
import org.rosuda.deducer.widgets.param.ParamNumeric;
import org.rosuda.deducer.widgets.param.ParamRFunction;
import org.rosuda.deducer.widgets.param.ParamVector;
import org.rosuda.deducer.widgets.param.RFunction;

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
	
	public static Theme makeOpts(){
		Theme t = new Theme();
		t.setName("opts");
		Param p;
		ParamRFunction pf;
		
		p = new ParamCharacter();
		p.setName("title");
		p.setTitle("title");
		p.setViewType(Param.VIEW_ENTER_LONG);
		p.setValue(null);
		p.setDefaultValue(null);
		t.params.add(p);		
		
		pf = new ParamRFunction();
		pf.setName("axis.line");
		pf.setTitle("axis.line");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.text.x");
		pf.setTitle("axis.text.x");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.text.y");
		pf.setTitle("axis.text.y");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.ticks");
		pf.setTitle("axis.ticks");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.title.x");
		pf.setTitle("axis.title.x");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.title.y");
		pf.setTitle("axis.title.y");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.ticks.length");
		pf.setTitle("axis.ticks.length");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("axis.ticks.margin");
		pf.setTitle("axis.ticks.margin");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("legend.background");
		pf.setTitle("legend.background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("legend.key");
		pf.setTitle("legend.key");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("legend.key.size");
		pf.setTitle("legend.key.size");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("legend.text");
		pf.setTitle("legend.text");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("legend.title");
		pf.setTitle("legend.title");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		p = new ParamCharacter("legend.position");
		p.setViewType(Param.VIEW_COMBO);
		p.setOptions(new String[] {"right","left"});
		t.params.add(p);
		
		p = new ParamVector("legend.position");
		p.setViewType(Param.VIEW_TWO_VALUE_ENTER);
		p.setLowerBound(new Double(0));
		p.setUpperBound(new Double(1));
		t.params.add(p);
		
		pf = new ParamRFunction();
		pf.setName("panel.background");
		pf.setTitle("panel.background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("panel.border");
		pf.setTitle("panel.border");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_segment", makeThemeSegment());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("panel.grid.major");
		pf.setTitle("panel.grid.major");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_line", makeThemeLine());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("panel.grid.minor");
		pf.setTitle("panel.grid.minor");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_line", makeThemeLine());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("panel.margin");
		pf.setTitle("panel.margin");
		pf.addRFunction("unit", makeUnit());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("strip.background");
		pf.setTitle("strip.background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("strip.text.x");
		pf.setTitle("strip.text.x");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("strip.text.y");
		pf.setTitle("strip.text.y");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("plot.background");
		pf.setTitle("plot.background");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_rect", makeThemeRect());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("plot.title");
		pf.setTitle("plot.title");
		pf.addRFunction("theme_blank", makeThemeBlank());
		pf.addRFunction("theme_text", makeThemeText());
		t.params.add(pf);
		
		pf = new ParamRFunction();
		pf.setName("plot.margin");
		pf.setTitle("plot.margin");
		pf.addRFunction("unit", makeUnit());
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
		
		rfp = new ParamNumeric();
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
		rf.setName("theme_text");
		Param rfp;
		ParamNumeric rfpn;
		
		rfpn = new ParamNumeric();
		rfpn.setName("x");
		rfpn.setTitle("x");
		rfpn.setViewType(Param.VIEW_ENTER);
		rfpn.setLowerBound(new Double(0));
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
	
	
}
