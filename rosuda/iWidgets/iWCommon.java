/*
 * @author Gavin Alexander
 *
 * Holds constants and static methods common to other iWidgets classes
 * More stuff will no doubt reside here in future..
 *
*/

package org.rosuda.iWidgets;

import org.rosuda.ibase.*;

public class iWCommon
{
	//Constant ints for each type of event used by iWidgets
	public static int BUTTON_EVENT 		= Common.NM_ExtEvent + 101;
	public static int TICKBOX_EVENT 	= Common.NM_ExtEvent + 102;
	public static int RADIOBUTTON_EVENT     = Common.NM_ExtEvent + 103;
	public static int SLIDER_EVENT 		= Common.NM_ExtEvent + 104;
	public static int TEXTBOX_EVENT 	= Common.NM_ExtEvent + 105;
	
	// Probably won't need these...
	public static int getButton_Event()
	{
		return BUTTON_EVENT;
	}
	
	public static int getTickBox_Event()
	{
		return TICKBOX_EVENT;
	}
	
}

