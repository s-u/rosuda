package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.RFunction;

public class Theme implements ElementModel{

	String name;
	
	public Vector params = new Vector();
	
	public static Theme makeBw(){
		Theme t = new Theme();
		t.name = "theme_bw";
		
		Param p;
		
		p = new Param();
		p.name = "base_size";
		p.title = "base text size";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(12);
		p.defaultValue = new Double(12);
		p.lowerBound = new Double(0);
		t.params.add(p);
		
		return t;
		
	}
	
	public static Theme makeGrey(){
		Theme t = new Theme();
		t.name = "theme_grey";
		
		Param p;
		
		p = new Param();
		p.name = "base_size";
		p.title = "base text size";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(12);
		p.defaultValue = new Double(12);
		p.lowerBound = new Double(0);
		t.params.add(p);
		
		return t;
	}
	
	public static Theme makeOpts(){
		Theme t = new Theme();
		t.name = "opts";
		Param p;
		HashMap hm;
		Vector v ;
		
		p = new Param();
		p.name = "title";
		p.title = "title";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_ENTER_LONG;
		p.value = null;
		p.defaultValue = null;
		t.params.add(p);		
		
		p = new Param();
		p.name = "axis.line";
		p.title = "axis.line";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_segment"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_segment", makeThemeSegment());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.text.x";
		p.title = "axis.text.x";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.text.y";
		p.title = "axis.text.y";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.ticks";
		p.title = "axis.ticks";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_segment"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_segment", makeThemeSegment());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.title.x";
		p.title = "axis.title.x";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.title.y";
		p.title = "axis.title.y";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.ticks.length";
		p.title = "axis.ticks.length";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"unit"};
		hm = new HashMap();
		hm.put("unit", makeUnit());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "axis.ticks.margin";
		p.title = "axis.ticks.margin";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"unit"};
		hm = new HashMap();
		hm.put("unit", makeUnit());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "legend.background";
		p.title = "legend.background";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_rect"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_rect", makeThemeRect());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "legend.key";
		p.title = "legend.key";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_rect"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_rect", makeThemeRect());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "legend.key.size";
		p.title = "legend.key.size";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"unit"};
		hm = new HashMap();
		hm.put("unit", makeUnit());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "legend.text";
		p.title = "legend.text";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "legend.title";
		p.title = "legend.title";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param("legend.position");
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.options = new String[] {"right","left"};
		t.params.add(p);
		
		p = new Param("legend.position");
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		t.params.add(p);
		
		p = new Param();
		p.name = "panel.background";
		p.title = "panel.background";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_rect"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_rect", makeThemeRect());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "panel.border";
		p.title = "panel.border";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_segment"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_segment", makeThemeSegment());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "panel.grid.major";
		p.title = "panel.grid.major";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_line"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_line", makeThemeLine());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "panel.grid.minor";
		p.title = "panel.grid.minor";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_line"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_line", makeThemeLine());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "panel.margin";
		p.title = "panel.margin";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"unit"};
		hm = new HashMap();
		hm.put("unit", makeUnit());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "strip.background";
		p.title = "strip.background";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_rect"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_rect", makeThemeRect());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "strip.text.x";
		p.title = "strip.text.x";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "strip.text.y";
		p.title = "strip.text.y";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "plot.background";
		p.title = "plot.background";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_rect"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_rect", makeThemeRect());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "plot.title";
		p.title = "plot.title";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"theme_blank","theme_text"};
		hm = new HashMap();
		hm.put("theme_blank", makeThemeBlank());
		hm.put("theme_text", makeThemeText());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
		p = new Param();
		p.name = "plot.margin";
		p.title = "plot.margin";
		p.dataType = Param.DATA_RFUNCTION;
		p.view = Param.VIEW_RFUNCTION;
		p.options = new String[]{"unit"};
		hm = new HashMap();
		hm.put("unit", makeUnit());
		v = new Vector();
		v.add("");
		v.add(hm);
		p.value = v;
		t.params.add(p);
		
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
		
		Param rfp = new Param("family");
		rfp.title = "font family";
		rfp.dataType = Param.DATA_CHARACTER;
		rfp.view = Param.VIEW_EDITABLE_COMBO;
		rfp.options = new String[] {"AvantGarde", "Bookman", "Courier", "Helvetica",
				"Helvetica-Narrow", "NewCenturySchoolbook", "Palatino" ,"Times", "URWGothic", 
				"URWBookman", "NimbusMon", "NimbusSan", "NimbusSanCond", "CenturySch", 
				"URWPalladio" ,"NimbusRom"};
		rf.add(rfp);
		
		rfp = new Param("face");
		rfp.dataType = Param.DATA_CHARACTER;
		rfp.view = Param.VIEW_COMBO;
		rfp.options = new String[] {"plain","italic","bold"};
		rfp.value = "plain";
		rfp.defaultValue = "plain";
		rf.add(rfp);
		
		rfp = new Param("colour");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rfp.value = Color.black;
		rfp.defaultValue = Color.black;
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "size";
		rfp.title = "size";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(10);
		rfp.defaultValue = new Double(10);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "vjust";
		rfp.title = "vjust";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rfp.upperBound = new Double(1);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "hjust";
		rfp.title = "hjust";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rfp.upperBound = new Double(1);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "angle";
		rfp.title = "angle";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(0);
		rfp.defaultValue = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "lineheight";
		rfp.title = "lineheight";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1.1);
		rfp.defaultValue = new Double(1.1);
		rf.add(rfp);
		return rf;
	}
	
	public static RFunction makeThemeSegment(){
		RFunction rf = new RFunction();
		rf.setName("theme_segment");
		
		Param rfp = new Param("colour");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rfp.value = Color.black;
		rfp.defaultValue = Color.black;
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "size";
		rfp.title = "size";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "linetype";
		rfp.title = "linetype";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1);
		rfp.defaultValue = new Double(1);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		return rf;
	}
	
	public static RFunction makeThemeLine(){
		RFunction rf = new RFunction();
		rf.setName("theme_line");
		
		Param rfp = new Param("colour");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rfp.value = Color.black;
		rfp.defaultValue = Color.black;
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "size";
		rfp.title = "size";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "linetype";
		rfp.title = "linetype";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1);
		rfp.defaultValue = new Double(1);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		return rf;
	}
	
	public static RFunction makeThemeRect(){
		RFunction rf = new RFunction();
		rf.setName("theme_rect");
		
		Param rfp = new Param("fill");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rf.add(rfp);
		
		rfp = new Param("colour");
		rfp.dataType = Param.DATA_COLOUR;
		rfp.view = Param.VIEW_COLOUR;
		rfp.value = Color.black;
		rfp.defaultValue = Color.black;
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "size";
		rfp.title = "size";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(.5);
		rfp.defaultValue = new Double(.5);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		
		rfp = new Param();
		rfp.name = "linetype";
		rfp.title = "linetype";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.value = new Double(1);
		rfp.defaultValue = new Double(1);
		rfp.lowerBound = new Double(0);
		rf.add(rfp);
		return rf;
	}
	
	public static RFunction makeUnit(){
		RFunction rf = new RFunction();
		rf.setName("theme_text");
		Param rfp;
		rfp = new Param();
		rfp.name = "x";
		rfp.title = "x";
		rfp.dataType = Param.DATA_NUMERIC;
		rfp.view = Param.VIEW_ENTER;
		rfp.lowerBound = new Double(0);
		rf.add(rfp);		
		
		rfp = new Param("units");
		rfp.title = "units";
		rfp.dataType = Param.DATA_CHARACTER;
		rfp.view = Param.VIEW_EDITABLE_COMBO;
		rfp.options = new String[] {"npc", "cm", "inches", "mm",
				"points", "picas", "bigpts" ,"dida", "cicero", 
				"scaledpts", "lines", "char", "native", "snpc", 
				"strwidth" ,"strheight","grobwidth","grobheight"};
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
			s.name = name;
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
		String call = Deducer.makeRCollection(paramCalls, name, false);
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
	
	
}
