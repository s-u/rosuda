package org.rosuda.ibase;

/** Commander interface to pass action commands between objects
    @version $Id$
 */
public interface Commander
{
    /** run command cmd issued by another object
	@param o origin of the command
	@param cmd command string 
	@return any object, the actual interpretation is up to the calling object
    */
    public Object run(Object o, String cmd);
};
