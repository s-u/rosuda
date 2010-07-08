package org.rosuda.deducer.plots;

import java.util.HashMap;
import java.util.Vector;

public class PlotController {

	public static boolean initialized = false;
	protected static String[] names = {"Templates","Geometric Elements","Statistics","Scales","Facets","Coordinates","Other"};
	protected static HashMap geoms;
	protected static HashMap stats;
	protected static HashMap scales;
	protected static HashMap facets;
	protected static HashMap themes;
	protected static HashMap coords;
	protected static HashMap pos;
	protected static String[] geomNames = {"abline","area","bar","bin2d","blank","boxplot","contour","crossbar","density","density2d",
			"errorbar","errorbarh","freqpoly","hex","histogram","hline","jitter","line","linerange",
			"path","point","pointrange","polygon","quantile","rect","ribbon","smooth","step","text",
			"tile","vline"};
	protected static String[] statNames = {"abline","bin","bin2d","binhex","boxplot","contour","density","density2d","function",
			"hline","identity","qq","quantile","smooth","spoke","sum","summary","unique","vline"};
	protected static String[] scaleNames = {"brewer","continuous","date","datetime","discrete","gradient","gradient2","gradientn","grey",
			"hue","identity","linetype","manual","shape","size"};
	protected static String[] facetNames = {"grid","wrap"};
	protected static String[] coordNames = {"cartesian","equal","flip","map","polar","trans"};
	protected static String[] posNames = {"dodge","identity","jitter","stack"};
	protected static String[] themeNames = { "grey","bw","opts"};	
	
	
	public static void init(){
		if(initialized==false){
			
			geoms = new HashMap();
			for(int j=0;j<geomNames.length;j++)
				geoms.put(geomNames[j], PlottingElement.createElement("geom",geomNames[j]));
			stats = new HashMap();
			for(int j=0;j<statNames.length;j++)
				stats.put(statNames[j], PlottingElement.createElement("stat",statNames[j]));			
			scales = new HashMap();
			for(int j=0;j<scaleNames.length;j++)
				scales.put(scaleNames[j], PlottingElement.createElement("scale",scaleNames[j]));
			facets = new HashMap();
			for(int j=0;j<facetNames.length;j++)
				facets.put(facetNames[j], PlottingElement.createElement("facet",facetNames[j]));
			themes = new HashMap();
			for(int j=0;j<themeNames.length;j++)
				themes.put(themeNames[j], PlottingElement.createElement("theme",themeNames[j]));
			coords = new HashMap();
			for(int j=0;j<coordNames.length;j++)
				coords.put(coordNames[j], PlottingElement.createElement("coord",coordNames[j]));
			themes = new HashMap();
			for(int j=0;j<themeNames.length;j++)
				themes.put(themeNames[j], PlottingElement.createElement("theme",themeNames[j]));
			pos = new HashMap();
			pos.put("identity", new Position("identity",null,null));
			pos.put("stack", new Position("stack",null,null));
			pos.put("dodge", new Position("dodge",null,null));
			pos.put("jitter", new Position("jitter",null,null));
			initialized = true;
		}
	}
	
	public static String[] getNames(){
		return names;
	}
	public static HashMap getGeoms(){
		return geoms;
	}
	public static String[] getGeomNames(){
		return geomNames;
	}
	public static HashMap getStats(){
		return stats;
	}
	public static String[] getStatNames(){
		return statNames;
	}
	public static HashMap getScales(){
		return scales;
	}
	public static String[] getScaleNames(){
		return scaleNames;
	}
	public static HashMap getFacets(){
		return facets;
	}
	public static String[] getFacetNames(){
		return facetNames;
	}
	public static HashMap getCoords(){
		return coords;
	}
	public static String[] getCoordNames(){
		return coordNames;
	}
	public static HashMap getPositions(){
		return pos;
	}
	public static String[] getPositionNames(){
		return posNames;
	}
	public static HashMap getThemes(){
		return themes;
	}
	public static String[] getThemeNames(){
		return themeNames;
	}
}
