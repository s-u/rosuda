package org.rosuda.JGR.robjects;

/**
 *  RModel - This is a simple java-representation of an object from R providing some information about it.
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2005 
 */

public class RObject {

    private String name = null;
    private String type = null;
    
    private RObject parent = null;

    private String info = null;
    
    private boolean realName = false;

    /**
     * Create a new RObject
     * @param name name of object
     * @param type type of object
     * @param parent parent object, e.g. if it's an inner vector of a data.frame
     * @param b is there are real R-name or should we set a number instead
     */
    public RObject(String name, String type, RObject parent, boolean b) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.realName = b;
    }

    /**
     * Create a new RObject
     * @param name name of object
     * @param type type of object
     * @param b is there are real R-name or should we set a number instead
     */
    public RObject(String name, String type, boolean b) {
    	this(name,type,null, b);
    }
    
    /**
     * Set name of object.
     * @param name name of object
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get name of object
     * @return name of object
     */
    public String getName() {
        return name;
    }

    /**
     * Get type of object: numeric, character ....
     * @return type of object
     */
    public String getType() {
        return type;
    }

    /**
     * Set parent object.
     * @param p parent object
     */
    public void setParent(RObject p) {
        this.parent = p;
    }
    
    /**
     * Get parent object.
     * @return parent object
     */
    public RObject getParent() {
    	return parent;
    }

    /**
     * Check wether the object is atomar or not, atomar objects are: function, integer, numeric, character, logical, factor, environment (because they are not supported).
     * @return true if it's atomar, false if it is not
     */
    public boolean isAtomar() {
        if (type.indexOf("function") >= 0 || type.indexOf("integer") >= 0 || type.indexOf("numeric") >= 0 || type.indexOf("character") >= 0 || type.indexOf("logical") >= 0 || type.indexOf("factor") >= 0 || type.indexOf("environment") >= 0)
            return true;
        return false;
    }
    
    /**
     * Check if editing this object is supported, types which are supported are: numeric, integer, factor, character, data.frame, matrix, list.
     * @return true if editing is allowed, false if not
     */
    public boolean isEditable() {
        if (type.indexOf("numeric") >= 0 || type.indexOf("integer") >= 0 || type.indexOf("factor") >= 0 || type.indexOf("character") >= 0 || type.indexOf("data.frame") >= 0 || type.indexOf("matrix") >= 0 || type.indexOf("list") >= 0)
            return true;
        return false;
    }

    /**
     * Get real R-name of this object, e.g.: Cars[["displacement"]].
     * @return R-name
     */
    public String getRName() {
    	if (parent != null && parent.getType().equals("matrix"))
    		return parent.getName()+"[,"+(realName?"\"":"")+getName()+(realName?"\"":"")+"]";
    	if (parent != null && parent.getType().equals("table"))
    		return "dimnames("+parent.getRName()+")[["+(realName?"\"":"")+getName()+(realName?"\"":"")+"]]";
    	return parent==null?getName():parent.getRName()+"[["+(realName?"\"":"")+getName()+(realName?"\"":"")+"]]";
    }
    
    /**
     * Set information about object, depends on object: data.frame -> dimension, function -> arguments, factor -> levels, .... 
     * @param s information depending on object
     */
    public void setInfo(String s){
    	this.info = s;
    }

    /**
     * Get provided information about this object.
     * @return information
     */
    public String getInfo() {
    	return info;
    }

    public String toString() {
    	return name+"\t ("+type+") "+(info!=null?info:"");
    }
}

