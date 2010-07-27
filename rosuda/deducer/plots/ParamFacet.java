package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.widgets.VariableSelectorWidget;
import org.rosuda.deducer.widgets.param.Param;
import org.rosuda.deducer.widgets.param.ParamWidget;

public class ParamFacet extends Param{

	public String[] yVarsGrid = new String[]{};
	public String[] xVarsGrid = new String[]{};
	public String[] varsWrap = new String[]{};
	
	public String facetType;
	public String scaleGrid;
	public Boolean margins;
	public Boolean spaceFixed;
	public Boolean asTableGrid;
	
	public Integer nrow;
	public Integer ncol;
	public String scaleWrap;
	public Boolean asTableWrap;
	public Boolean drop;
	
	public ParamFacet(){
		asTableGrid = new Boolean(true);
		asTableWrap = new Boolean(true);
		drop = new Boolean(true);
		margins = new Boolean(false);
		setName("facet");
		setTitle("facet");
		ncol = null;
		nrow = null;
		scaleGrid = "fixed";
		scaleWrap = "fixed";
		spaceFixed = new Boolean(true);
		facetType = "wrap";
	}
	
	public ParamWidget getView(){
		return new ParamFacetWidget(new VariableSelectorWidget(),this);
	}
	
	public Object clone(){
		ParamFacet f = new ParamFacet();
		String[] s = new String[yVarsGrid.length];
		for(int i=0;i<s.length;i++)
			s[i] = yVarsGrid[i];
		f.yVarsGrid =s;
		
		s = new String[xVarsGrid.length];
		for(int i=0;i<s.length;i++)
			s[i] = xVarsGrid[i];
		f.xVarsGrid = s;
		
		s = new String[varsWrap.length];
		for(int i=0;i<s.length;i++)
			s[i] = varsWrap[i];
		f.varsWrap = s;
		
		f.scaleGrid = this.scaleGrid;
		f.scaleWrap = scaleWrap;
		f.margins = new Boolean(margins.booleanValue());
		f.spaceFixed = new Boolean(spaceFixed.booleanValue());
		f.asTableGrid = new Boolean(asTableGrid.booleanValue());
		f.asTableWrap = new Boolean(asTableWrap.booleanValue());
		f.drop = new Boolean(drop.booleanValue());
		if(nrow!=null)
			f.nrow = new Integer(nrow.intValue());
		if(ncol!=null)
			f.ncol = new Integer(ncol.intValue());
		f.facetType = facetType;
		return f;
	}
	
	public String[] getParamCalls(){
		String[] calls = new String[]{};
		Vector v = new Vector();
		if(facetType.equals("grid")){
			String rhs = "";
			if(yVarsGrid.length==0)
				rhs = ".";
			for(int i=0;i<yVarsGrid.length;i++){
				if(i!=0)
					rhs+= " + ";
				rhs+= yVarsGrid[i];
			}
			String lhs = "";
			if(xVarsGrid.length==0)
				lhs = ".";
			for(int i=0;i<xVarsGrid.length;i++){
				if(i!=0)
					lhs+= " + ";
				lhs+= xVarsGrid[i];
			}
			v.add("facets = " + lhs + " ~ " + rhs);
			if(margins.booleanValue())
				v.add(", margins = TRUE");
			
			if(!scaleGrid.equals("fixed"))
				v.add(", scales = '" + scaleGrid + "'");
			
			if(!spaceFixed.booleanValue())
				v.add(", space = 'free'");
			
			if(!asTableGrid.booleanValue())
				v.add(", as.table = FALSE");
		}else{
			String rhs = "";
			for(int i=0;i<varsWrap.length;i++){
				if(i!=0)
					rhs+= " + ";
				rhs+= varsWrap[i];
			}
			v.add("facets = ~" + rhs);
			
			if(nrow!=null)
				v.add(", nrow = " + nrow.toString());
			if(ncol!=null)
				v.add(", ncol = " + ncol.toString());
			if(!scaleWrap.equals("fixed"))
				v.add(", scales = '"+scaleWrap+"'");
			if(!asTableWrap.booleanValue())
				v.add(", as.table = FALSE");	
			if(!drop.booleanValue())
				v.add(", as.table = FALSE");
		}
		calls = new String[v.size()];
		for(int i=0;i<v.size();i++)
			calls[i] = v.get(i).toString();
			
		return calls;
	}

	public Object getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultValue(Object defaultValue) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(Object value) {
		// TODO Auto-generated method stub
		
	}
}
