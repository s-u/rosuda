/*
 * @author Gavin Alexander
 *
 * The event handler class for iButtons.
 * 
 */
 
package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
 
public class iButtonListener implements ActionListener
{
	Object framework; // The iPlots Framework object which handles all the plots.	 
	Notifier notifier = Common.breakDispatcher; // Notifier object
	
	//Constructor
	public iButtonListener (Object f)
	{
		framework = f;
	}


	// For info -
	// Constructor parameters for NotifyMsg:
	// public NotifyMsg( Object src,  int msgid,  String command,  Object[] params )

	
	// This bypasses the actionPerformed() method in Framework.
	// iWCommon : holds static variables representing the event handlers for each
	// type of iWidget. In the case of iButtons - iWCommon.BUTTON_EVENT.
	// e.getActionCommand returns the command string associated with this action
	// (ie the button label string).
	public void actionPerformed(ActionEvent e)
	{	
		NotifyMsg msg = new NotifyMsg(framework, iWCommon.BUTTON_EVENT, e.getActionCommand());
		notifier.NotifyAll(msg);
	}
}


