/*
 * @author Gavin Alexander
 *
 * The event handler class for iRadioButtons.
 * 
 */

package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;
 
import org.rosuda.ibase.*;

public class iRadioButtonListener implements ItemListener
{
	Object framework; //The iPlots Framework object which handles all the plots.	 
	String iRadioButtonID;
	Notifier notifier = Common.breakDispatcher; // Notifier object
	
	//Constructor
	public iRadioButtonListener (Object f, String id)
	{
		framework = f;
		iRadioButtonID = id;
	}

	// For info - Constructor parameters for NotifyMsg:
	// public NotifyMsg( Object src,  int msgid,  String command,  Object[] params )
		
	public void itemStateChanged(ItemEvent ie)
	{
		// Label on selected/deslected tickBox
		String label = ((Checkbox)ie.getSource()).getLabel();
				
		// Params stored as an Object array to suit the NotifyMsg constructor
		Object[] state = { label };

		NotifyMsg msg = new NotifyMsg(framework, iWCommon.RADIOBUTTON_EVENT, iRadioButtonID, state);
		notifier.NotifyAll(msg);
	}
}


