/*
 * @author Gavin Alexander
 *
 * The event handler class for iTickBoxes.
 * 
 */

package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
 
public class iTickBoxListener implements ItemListener
{
	Object framework; // The iPlots Framework object which handles all the plots.	 
	String tickBoxID;
	Notifier notifier = Common.breakDispatcher; // iPlots Notifier object
	

	// Constructor
	public iTickBoxListener (Object f, String id)
	{
		framework = f;
		tickBoxID = id;
	}

	// For info -
	// Constructor parameters for NotifyMsg:
	// public NotifyMsg( Object src,  int msgid,  String command,  Object[] params )
	

	public void itemStateChanged(ItemEvent ie)
	{
		Integer selectState = null;
		
		// An int constant indicating whether tickbox is selected (=1)/deselected (=2)
		selectState = new Integer(ie.getStateChange());
		
		// Label on selected/deslected tickBox
		String label = ((Checkbox)ie.getSource()).getLabel();
				
		// Params stored as an Object array to suit the NotifyMsg constructor
//		Object[] state = { selectState, label };
		Object[] state = { selectState };
		

		NotifyMsg msg = new NotifyMsg(framework, iWCommon.TICKBOX_EVENT, tickBoxID, state);
		notifier.NotifyAll(msg);
	}
}


