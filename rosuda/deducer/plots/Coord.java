package org.rosuda.deducer.plots;

import java.util.Vector;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.widgets.param.Param;

public class Coord implements ElementModel{
	
	String name;
	
	public Vector params = new Vector();
	
	
	public static Coord makeCartesian(){
		Coord c = new Coord();
		c.name = "coord_cartesian";
		Param p;
		
		
		p = new Param();
		p.name = "xlim";
		p.title = "x axis range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		c.params.add(p);
		
		p = new Param();
		p.name = "ylim";
		p.title = "y axis range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		c.params.add(p);
		
		
		return c;
	}
	
	public static Coord makeEqual(){
		Coord c = new Coord();
		c.name = "coord_equal";
		Param p;
		
		
		p = new Param();
		p.name = "ratio";
		p.title = "x to y ratio";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(1);
		p.defaultValue = new Double(1);
		c.params.add(p);
		
		return c;
	}
	
	public static Coord makeFlip(){
		Coord c = new Coord();
		c.name = "coord_flip";
		Param p;
		
		
		p = new Param();
		p.name = "xlim";
		p.title = "x axis range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		c.params.add(p);
		
		p = new Param();
		p.name = "ylim";
		p.title = "y axis range";
		p.dataType = Param.DATA_NUMERIC_VECTOR;
		p.view = Param.VIEW_TWO_VALUE_ENTER;
		p.value = new String[]{};
		p.defaultValue = new String[]{};
		c.params.add(p);
		
		return c;
	}
	
	public static Coord makeMap(){
		Coord c = new Coord();
		c.name = "coord_map";
		Param p;
		
		
		p = new Param();
		p.name = "projection";
		p.title = "Projection";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = null;
		p.defaultValue = null;		
		p.options = new String[]{ "mercator","sinusoidal","cylequalarea","cylindrical","rectangular",
				"gall","mollweide","gilbert","","azequidistant","azequalarea","gnomonic","perspective","orthographic",
				"stereographic","laue","fisheye","newyorker","","conic","simpleconic","lambert","albers","bonne",
				"","polyconic","aitoff","lagrange","bicentric","elliptic","globular","vandergrinten","eisenlohr","",
				"guyou","square","tetra","hex","","harrison","trapezoidal","lune","","mecca","homing"};
		p.labels = new String[]{ "equally spaced straight meridians, conformal, straight compass courses",
				"equally spaced parallels, equal-area",
				"equally spaced straight meridians, equal-area, true scale on lat0",
				"central projection on tangent cylinder",
				"equally spaced parallels, equally spaced straight meridians, true scale on lat0",
				"parallels spaced stereographically on prime meridian, equally spaced straight meridians, true scale on lat0",
				"(homalographic) equal-area, hemisphere is a circle",
				"sphere conformally mapped on hemisphere and viewed orthographically",
				"",
				"equally spaced parallels, true distances from pole",
				"equal-area",
				"central projection on tangent plane, straight great circles",
				"viewed along earth's axis dist earth radii from center of earth",
				"viewed from infinity",
				"conformal, projected from opposite pole",
				"radius = tan(2 * colatitude) used in xray crystallography",
				"",
				"stereographic seen through medium with refractive index n",
				"radius = log(colatitude/r) map from viewing pedestal of radius r degrees",
				"central projection on cone tangent at lat0",
				"equally spaced parallels, true scale on lat0 and lat1",
				"conformal, true scale on lat0 and lat",
				"equal-area, true scale on lat0 and lat1",
				"equally spaced parallels, equal-area, parallel lat0 developed from tangent cone",
				"",
				"parallels developed from tangent cones, equally spaced along Prime Meridian",
				"equal-area projection of globe onto 2-to-1 ellipse, based on azequalarea",
				"conformal, maps whole sphere into a circle",
				"points plotted at true azimuth from two centers on the equator at longitudes +lon0 and -lon0",
				"points are plotted at true distance from two centers on the equator at longitudes +lon0 and -lon0",
				"hemisphere is circle, circular arc meridians equally spaced on equator, circular arc parallels equally spaced on 0- and 90-degree meridians",
				"sphere is circle, meridians as in globular, circular arc parallels resemble mercator",
				"conformal with no singularities, shaped like polyconic",
				"",
				"W and E hemispheres are square",
				"world is square with Poles at diagonally opposite corners",
				"map on tetrahedron with edge tangent to Prime Meridian at S Pole, unfolded into equilateral triangle",
				"world is hexagon centered on N Pole, N and S hemispheres are equilateral triangles",
				"",
				"oblique perspective from above the North Pole, dist earth radii from center of earth, looking along the Date Line angle degrees off vertical",
				"equally spaced parallels, straight meridians equally spaced along parallels, true scale at lat0 and lat1 on Prime Meridian",
				"conformal, polar cap above latitude lat maps to convex lune with given angle at 90E and 90W",
				"",
				"equally spaced vertical meridians",
				"distances to Mecca are true"
				};
		c.params.add(p);
		
		p = new Param();
		p.name = "lat0";
		p.title = "lat0";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "lat1";
		p.title = "lat2";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "dist";
		p.title = "dist";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "n";
		p.title = "n";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "r";
		p.title = "r";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "angle";
		p.title = "angle";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		
		p = new Param();
		p.name = "lat";
		p.title = "lat";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		c.params.add(p);
		return c;
	}
	
	public static Coord makePolar(){
		Coord c = new Coord();
		c.name = "coord_polar";
		Param p;
		
		
		p = new Param();
		p.name = "theta";
		p.title = "Angle is";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "x";
		p.defaultValue = "x";
		p.options = new String[]{"x","y"};
		c.params.add(p);
		
		p = new Param();
		p.name = "start";
		p.title = "Offset from 12 o'clock in radians";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_ENTER;
		p.value = new Double(0);
		p.defaultValue = new Double(0);
		c.params.add(p);
		
		p = new Param();
		p.name = "direction";
		p.title = "Direction";
		p.dataType = Param.DATA_NUMERIC;
		p.view = Param.VIEW_COMBO;
		p.value = "1";
		p.defaultValue = "1";
		p.options = new String[] {"1","-1"};
		p.labels = new String[] {"clockwise","counter clockwise"};
		c.params.add(p);
		
		p = new Param();
		p.name = "expand";
		p.title = "Expand axis";
		p.dataType = Param.DATA_LOGICAL;
		p.view = Param.VIEW_CHECK_BOX;
		p.value = new Boolean(false);
		p.defaultValue = new Boolean(false);
		c.params.add(p);
		
		
		return c;
	}
	
	public static Coord makeTrans(){
		Coord c = new Coord();
		c.name = "coord_trans";
		Param p;
		
		
		p = new Param();
		p.name = "xtrans";
		p.title = "x-axis transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		c.params.add(p);
		
		p = new Param();
		p.name = "ytrans";
		p.title = "y-axis transformation";
		p.dataType = Param.DATA_CHARACTER;
		p.view = Param.VIEW_COMBO;
		p.value = "identity";
		p.defaultValue = "identity";
		p.options = new String[] {"asn","exp","identity","log","log10","probit","recip","reverse","sqrt"};
		c.params.add(p);
		
		return c;
	}
	
	
	public static Coord makeCoord(String coord){
		if(coord.equals("cartesian"))
			return makeCartesian();
		else if(coord.equals("equal"))
			return makeEqual();		
		else if(coord.equals("flip"))
			return makeEqual();	
		else if(coord.equals("map"))
			return makeMap();	
		else if(coord.equals("polar"))
			return makePolar();	
		else if(coord.equals("trans"))
			return makeTrans();	
		return null;
	}
	
	
	public Object clone(){
		Coord c = new Coord();
		for(int i=0;i<params.size();i++)
			c.params.add(((Param)params.get(i)).clone());
		c.name = name;
		return c;
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
		return new DefaultElementView(this);
	}


	public Vector getParams() {
		return params;
	}

	
	
	
	
	
	
	
}
