/*
 * @author Gavin Alexander
 *
 * The event handler class for iTextBoxes.
 * 
 */

package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
 
public class iTextBoxListener implements ActionListener
{
	Object framework;		// The iPlots Framework object which handles all the plots.	 
	TextField textField;	// The source TextField
	String iTextBoxID;
	Notifier notifier = Common.breakDispatcher; // Notifier object
	
	//Constructor for ActionEvent objects from buttons
	public iTextBoxListener (Object f, String tid)
	{
		framework = f;
		iTextBoxID = tid;
	}


	// For info -
	// Constructor parameters for NotifyMsg:
	// public NotifyMsg( Object src,  int msgid,  String command,  Object[] params )

	
	// This bypasses the actionPerformed() method in Framework.
	// iWCommon : holds static variables representing the event handlers for each
	// type of iWidget. In the case of iTextBoxes - iWCommon.TEXTBOX_EVENT.
	public void actionPerformed(ActionEvent e)
	{	
		textField = (TextField)e.getSource();
		String input = textField.getText();

		// Params stored as an Object array to suit the NotifyMsg constructor
		Object[] inputStr = { input };
		
		NotifyMsg msg = new NotifyMsg(framework, iWCommon.TEXTBOX_EVENT, iTextBoxID, inputStr);
		notifier.NotifyAll(msg);
		
		textField.setText("");
	}
}


