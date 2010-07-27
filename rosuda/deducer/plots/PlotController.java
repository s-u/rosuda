package org.rosuda.deducer.plots;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlotController {

	public static boolean initialized = false;
	protected static String[] names = {"Templates","Geometric Elements",
		"Statistics","Scales","Facets","Coordinates","Other"};
	protected static Map geoms;
	protected static Map stats;
	protected static Map scales;
	protected static Map facets;
	protected static Map themes;
	protected static Map coords;
	protected static Map pos;
	protected static String[] geomNames = {"abline","area","bar","bin2d","blank","boxplot","contour","crossbar","density","density2d",
			"errorbar","errorbarh","freqpoly","hex","histogram","hline","jitter","line","linerange",
			"path","point","pointrange","polygon","quantile","rect","ribbon","smooth","step","text",
			"tile","vline"};
	protected static String[] statNames = {"abline","bin","bin2d","binhex","boxplot","contour","density","density2d","function",
			"hline","identity","qq","quantile","smooth","spoke","sum","summary","unique","vline"};
	protected static String[] scaleNames = {"area","colour_brewer","fill_brewer","x_continuous","y_continuous","x_date","y_date",
		"x_datetime","y_datetime","x_discrete","y_discrete","z_discrete","colour_gradient","fill_gradient","colour_gradient2",
		"fill_gradient2","colour_gradientn","fill_gradientn","colour_grey","fill_grey", "colour_hue","fill_hue",
		"colour_identity","fill_identity","linetype_identity","shape_identity","size_identity","linetype","colour_manual",
		"fill_manual","linetype_manual","shape_manual","size_manual","shape","size","alpha"};
	protected static String[] facetNames = {"grid","wrap"};
	protected static String[] coordNames = {"cartesian","equal","flip","map","polar","trans"};
	protected static String[] posNames = {"dodge","identity","jitter","stack"};
	protected static String[] themeNames = { "grey","bw","opts"};	
	
	
	public static void init(){
		if(initialized==false){
			geoms = new LinkedHashMap();
			for(int j=0;j<geomNames.length;j++)
				geoms.put(geomNames[j], PlottingElement.createElement("geom",geomNames[j]));
			stats = new LinkedHashMap();
			for(int j=0;j<statNames.length;j++)
				stats.put(statNames[j], PlottingElement.createElement("stat",statNames[j]));			
			scales = new LinkedHashMap();
			for(int j=0;j<scaleNames.length;j++)
				scales.put(scaleNames[j], PlottingElement.createElement("scale",scaleNames[j]));
			facets = new LinkedHashMap();
			for(int j=0;j<facetNames.length;j++)
				facets.put(facetNames[j], PlottingElement.createElement("facet",facetNames[j]));
			coords = new LinkedHashMap();
			for(int j=0;j<coordNames.length;j++)
				coords.put(coordNames[j], PlottingElement.createElement("coord",coordNames[j]));
			themes = new LinkedHashMap();
			for(int j=0;j<themeNames.length;j++)
				themes.put(themeNames[j], PlottingElement.createElement("theme",themeNames[j]));
			pos = new LinkedHashMap();
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
	public static Map getGeoms(){
		return geoms;
	}
	public static String[] getGeomNames(){
		return geomNames;
	}
	public static Map getStats(){
		return stats;
	}
	public static String[] getStatNames(){
		return statNames;
	}
	public static Map getScales(){
		return scales;
	}
	public static String[] getScaleNames(){
		return scaleNames;
	}
	public static Map getFacets(){
		return facets;
	}
	public static String[] getFacetNames(){
		return facetNames;
	}
	public static Map getCoords(){
		return coords;
	}
	public static String[] getCoordNames(){
		return coordNames;
	}
	public static Map getPositions(){
		return pos;
	}
	public static String[] getPositionNames(){
		return posNames;
	}
	public static Map getThemes(){
		return themes;
	}
	public static String[] getThemeNames(){
		return themeNames;
	}
	
	public static void addGeom(PlottingElement pe){
		String[] nm = geomNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		geoms.put(pe.getName(), pe);
	}
	
	public static void addStat(PlottingElement pe){
		String[] nm = statNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		stats.put(pe.getName(), pe);
	}
	
	public static void addScale(PlottingElement pe){
		String[] nm = scaleNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		scales.put(pe.getName(), pe);
	}
	
	public static void addFacet(PlottingElement pe){
		String[] nm = facetNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		facets.put(pe.getName(), pe);
	}
	
	public static void addCoord(PlottingElement pe){
		String[] nm = coordNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		coords.put(pe.getName(), pe);
	}
	
	public static void addPosition(Position p){
		String[] nm = posNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = p.name;
		pos.put(p.name, p);
	}
	
	public static void addTheme(PlottingElement pe){
		String[] nm = themeNames;
		int l = nm.length;
		String[] newNames = new String[l+1];
		for(int i=0;i<l;i++)
			newNames[i] = nm[i];
		newNames[l] = pe.getName();
		themes.put(pe.getName(), pe);
	}
	
}
