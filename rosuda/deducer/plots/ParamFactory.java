package org.rosuda.deducer.plots;

import java.awt.Color;

import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamAny;
import org.rosuda.deducer.widgets.param.ParamCharacter;
import org.rosuda.deducer.widgets.param.ParamColor;
import org.rosuda.deducer.widgets.param.ParamLogical;
import org.rosuda.deducer.widgets.param.ParamNumeric;
import org.rosuda.deducer.widgets.param.ParamVector;
import org.rosuda.deducer.widgets.param.RFunction;
import org.rosuda.deducer.widgets.param.RFunctionList;

public class ParamFactory {

	public static Param makeParam(String name){
		Param p = new ParamAny(name);
		if(name=="na.rm"){
			ParamLogical pl = new ParamLogical("na.rm");
			pl.setValue(new Boolean(false));
			pl.setDefaultValue(new Boolean(false));
			pl.setTitle("remove missing");
			p = pl;
		}else if(name=="drop"){
			ParamLogical pl = new ParamLogical("drop");
			pl.setValue(new Boolean(true));	
			pl.setDefaultValue(new Boolean(true));		
			p=pl;
		}else if(name=="width"){
			ParamNumeric pn = new ParamNumeric("width");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(0.0));
			p=pn;
		}else if(name=="outlier.colour"){
			ParamColor pcol = new ParamColor("outlier.colour");
			pcol.setViewType(Param.VIEW_COLOR);
			pcol.setDefaultValue(Color.black);
			pcol.setValue(Color.black);
			p=pcol;
		}else if(name=="outlier.shape"){
			
		}else if(name=="outlier.size"){
			
		}else if(name=="arrow"){
			RFunctionList arrowList = new RFunctionList("arrow");
			RFunction arrow = new RFunction("arrow");
			arrow.setTitle("arrow");
			
			ParamNumeric rfpn = new ParamNumeric();
			rfpn.setName("angle");
			rfpn.setTitle("angle");
			rfpn.setViewType(Param.VIEW_ENTER);
			rfpn.setValue(new Double(30));
			rfpn.setDefaultValue(new Double(30));
			rfpn.setLowerBound(new Double(0));
			arrow.add(rfpn);
			
			RFunctionList rfl = new RFunctionList("length");
			RFunction rf = new RFunction();
			rf.setName("unit");
			rf.setTitle("unit");
			Param rfp;
			rfpn = new ParamNumeric();
			rfpn.setName("x");
			rfpn.setTitle("size");
			rfpn.setViewType(Param.VIEW_ENTER);
			rfpn.setValue(new Double(.1));
			rfpn.setDefaultValue(new Double(0));
			rfpn.setLowerBound(new Double(0));
			rf.add(rfpn);		
			rfp = new ParamCharacter("units");
			rfp.setName("units");
			rfp.setTitle("units");
			rfp.setViewType(Param.VIEW_EDITABLE_COMBO);
			rfp.setOptions(new String[] {"npc", "cm", "inches", "mm",
					"points", "picas", "bigpts" ,"dida", "cicero", 
					"scaledpts", "lines", "char", "native", "snpc", 
					"strwidth" ,"strheight","grobwidth","grobheight"});
			rfp.setDefaultValue("inches");
			rfp.setValue("cm");
			rf.add(rfp);	
			rfl.addRFunction("unit", rf);
			arrow.add(rfl);
			
			rfp = new ParamCharacter("ends");
			rfp.setName("ends");
			rfp.setTitle("ends");
			rfp.setViewType(Param.VIEW_COMBO);
			rfp.setOptions(new String[] {"last","first","both"});
			rfp.setDefaultValue("last");
			rfp.setValue("last");			
			arrow.add(rfp);
			
			rfp = new ParamCharacter("type");
			rfp.setName("type");
			rfp.setTitle("type");
			rfp.setViewType(Param.VIEW_COMBO);
			rfp.setOptions(new String[] {"open","closed"});
			rfp.setDefaultValue("open");
			rfp.setValue("open");			
			arrow.add(rfp);
			arrowList.addRFunction("arrow", arrow);
			p=arrowList;
		}else if(name == "bins"){
			ParamNumeric pn = new ParamNumeric("bins");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(1.0));
			p=pn;		
		}else if(name == "breaks"){
			ParamVector pv = new ParamVector("breaks");
			pv.setNumeric(true);
			p = pv;
		}else if(name =="binwidth"){
			ParamNumeric pn = new ParamNumeric("binwidth");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(0.0));
			p=pn;		
		}else if(name =="coef"){
			ParamNumeric pn = new ParamNumeric("coef");
			pn.setDefaultValue(new Double(2));
			pn.setValue(new Double(2));
			p=pn;
		}else if(name =="adjust"){
			ParamNumeric pn = new ParamNumeric("adjust");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(0.0));
			p=pn;
		}else if(name =="kernel"){
			ParamCharacter pc = new ParamCharacter("kernel");
			pc.setViewType(Param.VIEW_COMBO);
			pc.setOptions(new String[] {"gaussian", "epanechnikov", "rectangular",
	                   "triangular", "biweight",
	                   "cosine", "optcosine"});
			pc.setLabels(new String[] {"gaussian", "epanechnikov", "rectangular",
	                   "triangular", "biweight",
	                   "cosine", "optcosine"});
			p = pc;
		}else if(name =="trim"){
			ParamNumeric pn = new ParamNumeric("trim");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(1.0));
			p=pn;
		}else if(name=="contour"){
			ParamLogical pl = new ParamLogical("contour");
			pl.setDefaultValue(new Boolean(true));	
			pl.setValue(new Boolean(true));	
			p=pl;
		}else if(name =="quantiles"){
			ParamVector pv = new ParamVector("quantiles");
			pv.setDefaultValue(new String[] {"0.25","0.5","0.75"});
			pv.setValue(new String[] {"0.25","0.5","0.75"});
			p = pv;
		}else if(name =="method"){
			ParamCharacter pc = new ParamCharacter("method");
			pc.setViewType(Param.VIEW_COMBO);
			pc.setOptions(new String[] {"lm", "gam", "loess", "rlm"});
			pc.setLabels(new String[] {"Linear model", "Generalized additive model",
									"Smooth","Robust linear model"});
			p = pc;
		}else if(name =="formula"){
			ParamCharacter pc = new ParamCharacter("formula");
			pc.setViewType(Param.VIEW_EDITABLE_COMBO);
			pc.setOptions(new String[] {"y ~ x", "y ~ poly(x,2)", "y ~ poly(x,3)"});
			p=pc;
		}else if(name =="fun"){
	
		}else if(name =="args"){
	
		}else if(name =="se"){
			ParamLogical pl = new ParamLogical("se");
			pl.setDefaultValue(new Boolean(true));	
			pl.setValue(new Boolean(true));	
			p=pl;	
			p.setTitle("Show confidence");
		}else if(name=="fullrange"){
			ParamLogical pl = new ParamLogical("fullrange");
			pl.setDefaultValue(new Boolean(false));	
			pl.setValue(new Boolean(false));	
			p=pl;	
			p.setTitle("Full data range");
		}else if(name =="level"){
			ParamNumeric pn = new ParamNumeric("width");
			pn.setViewType(Param.VIEW_ENTER);
			pn.setLowerBound(new Double(0.0));
			pn.setUpperBound(new Double(1.0));
			pn.setDefaultValue(new Double(0.95));
			p = pn;
		}else if(name =="direction"){
			ParamCharacter pc = new ParamCharacter("kernel");
			pc.setOptions(new String[]{"vh","hv"});
			pc.setLabels(new String[] {"Vertical then horizontal",
									"Horizontal then vertical"});
			pc.setDefaultValue("vh");
			pc.setViewType(Param.VIEW_COMBO);
			p=pc;
		}
		if(p.getValue()==null)
			p.setValue(p.getDefaultValue());
		return p;
	}

}
