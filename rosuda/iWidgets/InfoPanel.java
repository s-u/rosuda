/**
 * @author Gavin Alexander
 * 
 * Creates and organises an InfoPanel.
 * Messages in the InfoPanel have two components - the message itself,
 * and an updateable value in the message.
 * InfoPanel can add new messages and update their values.
 * After updating InfoPanel messages and adding them to the main Panel object (infoPanel),
 * InfoPanel makes it available (via getInfoPanel()) to be added to an iplot frame.
 *
 */

package org.rosuda.iWidgets;

import java.awt.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.iplots.*;
import org.rosuda.ibase.toolkit.*;

public class InfoPanel
{
	Framework framework;	// iPlots Framework object
	PGSCanvas pgsCanvas;	// iPlots PGSCanvas object, ie the current iPlot
	Frame frame;			// iPlot frame
	Panel infoPanel;		// Contains all the message panels
	ArrayList  messageList;	// List of all existing messages in the Info Panel
	int numCols = 2;		// Number of columns in the mainPanel
	int numRows = 0;		// Initial number of rows in the mainPanel
	int numMessages = 0;	// Counter to tally number of messages
	
	
	//Constructor
	public InfoPanel (Framework fw, PGSCanvas pgsc)
	{
		framework = fw;
		pgsCanvas = pgsc;
		
		frame = pgsCanvas.getFrame();	//Get plot's frame		
		messageList = new ArrayList();
		
		initialisePanel();
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Create panel, set Layout manager etc
	// This is done only once, after calling the constructor
	private void initialisePanel()
	{
		infoPanel = new Panel();
		infoPanel.setLayout(new GridLayout(numRows,numCols));
		
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Adds message to the info panel,
	// then adds the panel to the iPlots frame
	public void addMessage(String name, String message, String value)
	{
		// Check that no messages with this name are already in the list
		if (getInfoMessage(name) == null)
		{
			// Create the new InfoMessage object and add to the messageList
			InfoMessage im = new InfoMessage(name, message, value);
			messageList.add(im);
			
			// Create a new panel containing the new message and value
			Panel newPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
			newPanel.add(im.getLabel());

			
			// If number of labels is greater than the preset capacity of the InfoPanel
			if ((numMessages+1) > (numRows*numCols))
			{
				//Set number of grid rows in infoPanel
				if (numMessages%2 == 0)// If there's an even number of labels already (inc zero)
					numRows++;		// increment number of rows
				infoPanel.setLayout(new GridLayout(numRows,numCols));
			}	
			infoPanel.add(newPanel);	// Add new panel in the new infoPanel row
			numMessages++;				// Increment number of labels
			
			frame.show();
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Update value displayed in an InfoPanel message
	// Uses the message name to find the message
	public void updateMessage (String msgName, String msgValue)
	{
		InfoMessage im = getInfoMessage(msgName);
		
		if(im != null)
			// set the value part of the message
			im.setValue(msgValue);
			
		frame.show();
	}
	
	//////////////////////////////////////////////////////////////////////
	// Reset values of all mnessages to blank
	public void resetMessages()
	{
		int length = messageList.size();
		
		InfoMessage im;
		
		// Loop through list of InfoMessages
		for(int i=0; i<length; i++)
		{
			im = (InfoMessage) messageList.get(i);
			
			// set the value part of the message
			im.setValue("");
		}
		
		frame.show();
	}
	
	//////////////////////////////////////////////////////////////////////
	// Returns a message based on the message name
	private InfoMessage getInfoMessage(String name)
	{
		int length = messageList.size();
		
		InfoMessage im;
		
		// Loop through list of InfoMessages
		for(int i=0; i<length; i++)
		{
			im = (InfoMessage) messageList.get(i);
			
			if(im.getName().equals(name))
			{
				return im;
			}
		}
		return null;
	}
	
	///////////////////////////////////////////////////////////////////////
	// Return the InfoPanel panel
	public Panel getInfoPanel()
	{
		return infoPanel;
	}
}