package org.rosuda.ibase.toolkit;


public interface Queryable {

    /** returns whether this object should respond to queries. */
    boolean isQueryable();
    
    /** checks whether this object contains the given point.*/
    boolean contains(int x, int y);
    
    /** returns text for query */
    String getQueryText();
    
    /** returns text for extended query */
    String getExtQueryText();

}
