import java.util.Vector;
import java.util.Enumeration;

/** Notifier - framework to for recursive cycle-free notification messages to dependent classes
    @version $Id$
*/

public class Notifier {
    /** List of {@link Dependent} classes that will be notified on marker change */
    Vector ton;
    
    /** adds a {@link Dependent} class to be notified on marking change
	@param c class to be added */
    public void addDepend(Dependent c) {
	if (ton==null) ton=new Vector();
	if (!ton.contains(c)) ton.addElement(c);
    };

    /** removes a {@link Dependent} class from the list of classes to be notified on marking change
	@param c class to be removed */
    public void delDepend(Dependent c) {
	if (ton!=null) ton.removeElement(c);
    };

    /** notifies all {@link Dependent} classes in the notify list of a change, except for the specified class. 
	@param c class to be excluded from the current notification (used to prevent loops if a notification method wants to notify all others). If set to <code>NULL</code>, all classes in the list will be notified. */
    public void NotifyAll(NotifyMsg msg, Dependent c) { NotifyAll(msg,c,null); };
    public void NotifyAll(NotifyMsg msg, Vector path) { NotifyAll(msg,null,path); };

    /** initiates cascaded notification process. use this method instead of NotifyAll if you want to make sure that also inderect dependents will recieve the notification */
    public void startCascadedNotifyAll(NotifyMsg msg) {
	Vector path=new Vector();
	path.addElement(this);
	NotifyAll(msg,null,path);
    };

    /** general NotifyAll */    
    public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
	if (batchMode || ton==null || ton.isEmpty()) return;
	for (Enumeration e=ton.elements(); e.hasMoreElements();) {
	    Dependent o=(Dependent)e.nextElement();	    
	    if (o!=c) {
		if (path!=null) {
		    path.addElement(this);
		    o.Notifying(msg,this,path);
		    path.removeElement(this);
		} else o.Notifying(msg,this,null);
	    };
	};
    };    
    
    /** notifies all {@link Dependent} classes in the notify list of a change. (Results in calling {@link #NotifyAll} with <code>NULL</code> parameter */
    public void NotifyAll(NotifyMsg msg) { NotifyAll(msg,null,null); };

    /*--- since v1.3: support for batch mode ---*/
    
    /** batch mode flag */
    boolean batchMode=false;
    /** last message issued in batch mode */
    NotifyMsg batchLastMsg=null;

    /** initiates batch mode - in this mode no notifications are made until endBatch() has been called. */
    public void beginBatch() {
        batchMode=true;
        batchLastMsg=null;
    }

    /** ends batch mode. if any notification reqests has been made since beginBatch() then the last one
        will be passed to dependents. otherwise just batch flag is cleared and no notification is sent. */
    public void endBatch() {
        batchMode=false;
        if (batchLastMsg!=null)
            NotifyAll(batchLastMsg);
    }
};
