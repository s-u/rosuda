/*
 * @author Gavin Alexander
 *
 * Holds details of an individual message which will appear in the
 * InfoPanel of an iPlot frame.
 *
*/

package org.rosuda.iWidgets;

import java.awt.*;

import org.rosuda.ibase.*;

class InfoMessage
{
	String name;	//Name of the InfoMessage, for identification
	String message;	//The fixed part of the message
	String value;	//The changeable part of the message
	Label label;	//The label containing the message
	
	public InfoMessage(String n, String m, String v)
	{
		name = n;
		message = m;
		value = v;
		label = new Label(message +" "+ value, Label.LEFT);
	}
	
	//////////////////////////////////////////////////////////////////
	// Get methods
	public String getName()
	{
		return name;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public Label getLabel()
	{
		return label;
	}
	
	//////////////////////////////////////////////////////////////////
	// Set methods
	public void setValue(String newValue)
	{
		label.setText(message +" "+ newValue);
	}
}