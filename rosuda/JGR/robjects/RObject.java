package org.rosuda.JGR.robjects;

/**
 *  RObject
 * 
 * 	java-represesantion of an object out of R
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

public class RObject {

    private String name = null;
    private String type = null;
    
    private RObject parent = null;

    private String info = null;
    
    private boolean realName = false;

    public RObject(String name, String type, RObject parent, boolean b) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.realName = b;
    }

    public RObject(String name, String type, boolean b) {
    	this(name,type,null, b);
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setParent(RObject p) {
        this.parent = p;
    }
    
    public RObject getParent() {
    	return parent;
    }

    public boolean isAtomar() {
        if (type.indexOf("function") >= 0 || type.indexOf("integer") >= 0 || type.indexOf("numeric") >= 0 || type.indexOf("character") >= 0 || type.indexOf("logical") >= 0 || type.indexOf("factor") >= 0 || type.indexOf("environment") >= 0)
            return true;
        return false;
    }
    
    public boolean isEditable() {
        if (type.indexOf("numeric") >= 0 || type.indexOf("integer") >= 0 || type.indexOf("factor") >= 0 || type.indexOf("character") >= 0 || type.indexOf("data.frame") >= 0 || type.indexOf("matrix") >= 0 || type.indexOf("list") >= 0)
            return true;
        return false;
    }

    public String getRName() {
    	if (parent != null && parent.getType().equals("matrix"))
    		return parent.getName()+"[,"+(realName?"\"":"")+getName()+(realName?"\"":"")+"]";
    	if (parent != null && parent.getType().equals("table"))
    		return "dimnames("+parent.getRName()+")[["+(realName?"\"":"")+getName()+(realName?"\"":"")+"]]";
    	return parent==null?getName():parent.getRName()+"[["+(realName?"\"":"")+getName()+(realName?"\"":"")+"]]";
    }
    
    public void setInfo(String s){
    	this.info = s;
    }

    public String getInfo() {
    	return info;
    }

    public String toString() {
    	return name+"\t ("+type+") "+(info!=null?info:"");
    }
}

