package org.rosuda.deducer.plots;

public class Position {
	String name;
	public Double width;
	public Double height;
	
	public Position(String n,double w, double h){
		name=n;
		width=new Double(w);
		height = new Double(h);
	}
	
	public Position(String n,Double w, Double h){
		name=n;
		width=w;
		height = h;
	}
	
	public Position(){
		name="identity";
	}
	public Position(String n){
		name=n;
	}
	
	public Object clone(){
		return new Position(name,
				width==null ? null : new Double(width.doubleValue()),
				height==null ? null : new Double(height.doubleValue()));
	}
	
	public static Position makePosition(String posName){
		Position p = new Position(posName);
		return p;
	}
	
}
