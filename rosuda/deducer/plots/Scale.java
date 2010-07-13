package org.rosuda.deducer.plots;

import java.awt.Color;
import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.widgets.param.Param;


public class Scale implements ElementModel{

	String name;
	public String aesName;
	
	public Vector params = new Vector();
	
	public static Scale makeAlpha(){
		Scale s = new Scale();
		s.name = "scale_alpha";
		s.aesName = "alpha";
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "Legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_NUMERIC;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};

		s.params.add(p);
		
		p = new Param();
		p.name = "to";
		p.title = "Alpha range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{"0","1"};
		p.defaultValue = new String[]{"0","1"};
		p.lowerBound = new Double(0);
		p.upperBound = new Double(1);
		s.params.add(p);		

		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeBrewer(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_brewer";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		
		p = new Param();
		p.name = "limits";
		p.title = "Included levels";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "palette";
		p.title = "Colour palette";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "Set3";
		p.defaultValue = "Set3";
		p.options = new String[]{"YlOrRd","YlOrBr","YlGnBu","YlGn","Reds","RdPu",
				"Pruples","PuRd","PuBuGn","PuBu","OrRd","Oranges","Greys","Greens",
				"GnBu","BuPu","BuGn","Blues","","Set3","Set2","Set1","Pastel2","Pastel1",
				"Paired","Dark2","Accent","","Spectral","RdYlGn","RdYlBu","RdGy",
				"RdBu","PuOr","PRGn","PiYG","BrBG"};
		s.params.add(p);
		

		
		
		return s;		
	}
	
	public static Scale makeContinuous(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_continuous";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "ticks";
		p.title = "Ticks";
		p.dataType = ParamScaleLegend.DATA_SCALE_NUMERIC;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "expand";
		p.title = "Expansion factors (*,+)";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{"0.05","0.55"};
		p.defaultValue = new String[]{"0.05","0.55"};
		s.params.add(p);		

		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "scientific";
		p.defaultValue = "scientific";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeDate(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_date";
		s.aesName = aes;
		
		Param p;
		
		p = new Param();
		p.name = "name";
		p.title = "Name";
		p.dataType = Param.DATA_ANY;
		p.view = Param.VIEW_ENTER_LONG;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
			

		p = new Param();
		p.name = "major";
		p.title = "Major ticks";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"days","weeks","months","years"};
		s.params.add(p);
		
		p = new Param();
		p.name = "minor";
		p.title = "Minor ticks";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"days","weeks","months","years"};
		s.params.add(p);
		
		p = new Param();
		p.name = "format";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"%m/%d/%y","%d/%m/%y","%m/%d","%d/%m","%b","%b-%Y"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeDatetime(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_datetime";
		s.aesName = aes;
		
		Param p;
		
		p = new Param();
		p.name = "name";
		p.title = "Name";
		p.dataType = Param.DATA_ANY;
		p.view = Param.VIEW_ENTER_LONG;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
			

		p = new Param();
		p.name = "major";
		p.title = "Major ticks";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"days","weeks","months","years"};
		s.params.add(p);
		
		p = new Param();
		p.name = "minor";
		p.title = "Minor ticks";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"days","weeks","months","years"};
		s.params.add(p);
		
		p = new Param();
		p.name = "format";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_EDITABLE_COMBO;
		p.options = new String[] {"%m/%d/%y","%d/%m/%y","%m/%d","%d/%m","%b","%b-%Y"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeDiscrete(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_discrete";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "ticks";
		p.title = "ticks";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "expand";
		p.title = "expand";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{"0.05","0.55"};
		p.defaultValue = new String[]{"0.05","0.55"};
		s.params.add(p);
		
		p = new Param();
		p.name = "limits";
		p.title = "Included levels";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "drop";
		p.title = "Drop unused levels";
		p.dataType = Param.DATA_LOGICAL;
		p.view = Param.VIEW_CHECK_BOX;
		p.value = new Boolean(false);
		p.defaultValue = new Boolean(false);
		s.params.add(p);
		
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeGradient(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_gradient";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "low";
		p.title = "Low colour";
		p.dataType = Param.DATA_COLOUR;
		p.view = Param.VIEW_COLOUR;
		p.value = Color.decode("#3B4FB8");
		p.defaultValue = Color.decode("#3B4FB8");
		s.params.add(p);

		p = new Param();
		p.name = "high";
		p.title = "High colour";
		p.dataType = Param.DATA_COLOUR;
		p.view = Param.VIEW_COLOUR;
		p.value = Color.decode("#B71B1A");
		p.defaultValue = Color.decode("#B71B1A");
		s.params.add(p);
		
		p = new Param();
		p.name = "space";
		p.title = "Colour space";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "rgb";
		p.defaultValue = "rgb";
		p.options = new String[] {"rgb","Lab"};
		s.params.add(p);
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeGradient2(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_gradient2";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "low";
		p.title = "Low colour";
		p.dataType = Param.DATA_COLOUR;
		p.view = Param.VIEW_COLOUR;
		p.value = Color.decode("#3B4FB8");
		p.defaultValue = Color.decode("#3B4FB8");
		s.params.add(p);

		p = new Param();
		p.name = "mid";
		p.title = "Mid-point colour";
		p.dataType = Param.DATA_COLOUR;
		p.view = Param.VIEW_COLOUR;	
		s.params.add(p);
		
		p = new Param();
		p.name = "midpoint";
		p.title = "Mid-point value";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		s.params.add(p);
		
		p = new Param();
		p.name = "high";
		p.title = "High colour";
		p.dataType = Param.DATA_COLOUR;
		p.view = Param.VIEW_COLOUR;
		p.value = Color.decode("#B71B1A");
		p.defaultValue = Color.decode("#B71B1A");
		s.params.add(p);
		
		p = new Param();
		p.name = "space";
		p.title = "Colour space";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "rgb";
		p.defaultValue = "rgb";
		p.options = new String[] {"rgb","Lab"};
		s.params.add(p);
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeGradientn(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_gradientn";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "colours";
		p.title = "Colours";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "space";
		p.title = "Colour space";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "rgb";
		p.defaultValue = "rgb";
		p.options = new String[] {"rgb","Lab"};
		s.params.add(p);
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		return s;		
	}
	
	public static Scale makeGrey(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_grey";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "start";
		p.title = "Low grey";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(.2);
		p.defaultValue = new Double(.2);
		p.lowerBound = new Double(0);
		p.upperBound = new Double(1);
		s.params.add(p);
		
		p = new Param();
		p.name = "end";
		p.title = "high grey";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(.2);
		p.defaultValue = new Double(.2);
		p.lowerBound = new Double(0);
		p.upperBound = new Double(1);
		s.params.add(p);
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		return s;
	}
	
	public static Scale makeHue(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_hue";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "Legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Included levels";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "h";
		p.title = "Hue range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{"15","375"};
		p.defaultValue = new String[]{"15","375"};
		s.params.add(p);		
		
		p = new Param();
		p.name = "l";
		p.title = "Luminance [0, 100]";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(65);
		p.defaultValue = new Double(65);
		p.lowerBound = new Double(0);
		p.upperBound = new Double(100);
		s.params.add(p);
		
		p = new Param();
		p.name = "c";
		p.title = "Chroma";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(100);
		p.defaultValue = new Double(100);
		p.lowerBound = new Double(0);
		s.params.add(p);
		
		p = new Param();
		p.name = "h.start";
		p.title = "Hue start";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(0);
		p.defaultValue = new Double(0);
		p.lowerBound = new Double(0);
		s.params.add(p);

		p = new Param();
		p.name = "direction";
		p.title = "Colour wheel direction";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_COMBO;
		p.value = "1";
		p.defaultValue = "1";
		p.options = new String[] {"1","-1"};
		p.labels = new String[] {"clockwise","counter clockwise"};
		s.params.add(p);

		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeIdentity(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_identity";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "Legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		

		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeLineType(){
		Scale s = new Scale();
		
		s.name = "scale_linetype";
		s.aesName = "linetype";
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Included levels";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "drop";
		p.title = "Drop unused levels";
		p.dataType = Param.DATA_LOGICAL;
		p.view = Param.VIEW_CHECK_BOX;
		p.value = new Boolean(false);
		p.defaultValue = new Boolean(false);
		s.params.add(p);
		
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeManual(String aes){
		Scale s = new Scale();
		s.name = "scale_"+aes+"_manual";
		s.aesName = aes;
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "Legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		

		p = new Param();
		p.name = "values";
		p.title = "Values";
		if(aes.equals("colour") || aes.equals("fill"))
			p.dataType = Param.DATA_CHARACTER_VECTOR;
		else
			p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeShape(){
		Scale s = new Scale();
		s.name = "scale_shape";
		s.aesName = "shape";
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Included levels";
		p.dataType = Param.DATA_CHARACTER_VECTOR;
		p.view = Param.VIEW_VECTOR_BUILDER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "solid";
		p.title = "Use solid points";
		p.dataType = Param.DATA_LOGICAL;
		p.view = Param.VIEW_CHECK_BOX;
		p.value = new Boolean(true);
		p.defaultValue = new Boolean(true);
		s.params.add(p);
		
		p = new Param();
		p.name = "formatter";
		p.title = "Format";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"identity","comma","dollar","percent","scientific","precision"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeSize(){
		Scale s = new Scale();
		s.name = "scale_size";
		s.aesName = "size";
		
		Param p;
		
		p = new ParamScaleLegend();
		p.name = "legend";
		p.title = "Legend";
		p.dataType = ParamScaleLegend.DATA_SCALE_CHARACTER;
		p.view = ParamScaleLegend.VIEW_SCALE;
		s.params.add(p);		
		
		p = new Param();
		p.name = "limits";
		p.title = "Data range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		s.params.add(p);
		
		p = new Param();
		p.name = "to";
		p.title = "size range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{"1","6"};
		p.defaultValue = new String[]{"1","6"};
		s.params.add(p);
		
		p = new Param();
		p.name = "trans";
		p.title = "Transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		s.params.add(p);
		
		
		return s;		
	}
	
	public static Scale makeArea(){
		Scale s = Scale.makeSize();
		s.name = "scale_area";
		return s;
	}
	
	public static Scale makeScale(String aes,String scale){
		System.out.println(scale);
		if(scale.equals("linetype"))
			return makeLineType();
		else if(scale.equals("discrete"))
			return makeDiscrete(aes);
		else if(scale.equals("shape"))
			return makeShape();
		else if(scale.equals("hue"))
			return makeHue(aes);
		else if(scale.equals("alpha"))
			return makeAlpha();
		else if(scale.equals("continuous"))
			return makeContinuous(aes);
		else if(scale.equals("date"))
			return makeDate(aes);
		else if(scale.equals("datetime"))
			return makeDatetime(aes);
		else if(scale.equals("gradient"))
			return makeGradient(aes);
		else if(scale.equals("gradient2"))
			return makeGradient2(aes);
		else if(scale.equals("grey"))
			return makeGrey(aes);
		else if(scale.equals("identity"))
			return makeIdentity(aes);
		else if(scale.equals("manual"))
			return makeManual(aes);
		else if(scale.equals("size"))
			return makeSize();
		else if(scale.equals("area"))
			return makeArea();
		else if(scale.equals("brewer"))
			return makeBrewer(aes);
		return null;
	}
	
	public Object clone(){
		Scale s = new Scale();
		for(int i=0;i<params.size();i++)
			s.params.add(((Param)params.get(i)).clone());
		s.name = name;
		s.aesName = aesName;
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
		return "scale";
	}

	public ElementView getView() {
		if(!this.name.endsWith("brewer"))
			return new DefaultElementView(this);
		else
			return new ScaleBrewerPanel(this);
	}


	public Vector getParams() {
		return params;
	}

}
