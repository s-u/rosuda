/**
 * @author Gavin Alexander
 *
 * Creates and organises a Control Panel and adds it to an iplot frame.
 * The Control Panel contains all of the iWidgets for that iPlot,
 * accessible via a list.
 * The Control Panel also creates and accesses the InfoPanel obect associated 
 * with the same iPlot.
 * 
 */

package org.rosuda.iWidgets;

import java.awt.*;
import java.util.*;

import org.rosuda.pograss.*;
import org.rosuda.iplots.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

public class ControlPanel
{
	Framework framework;		//iPlots Framework object
	PGSCanvas pgsCanvas;		//iPlots PGSCanvas object, ie the current iPlot
	Frame frame;				//iPlot frame
	Panel controlPanel;			//The control panel itself
	Panel subPanel;				//Panel containing non-grouped iWidgets
	InfoPanel infoPanel;		//The InfoPanel paired with this ControlPanel
	String iPlotNum;			//The ID number of the current iPlot
	
	/////////////////////////////////////////////////////////////////////////
	// Constructor
	public ControlPanel (Framework fw, PGSCanvas pgsc, String iplot)
	{
		framework = fw;
		pgsCanvas = pgsc;
		iPlotNum = iplot;
		
		infoPanel = new InfoPanel(fw, pgsc); // Create iPlot's InfoPanel
		frame = pgsCanvas.getFrame();		 // Get iPlot's frame
		
		initialisePanels();		
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Accessors
	public InfoPanel getInfoPanel()
	{
		return infoPanel;
	}

	//////////////////////////////////////////////////////////////////////////
	// Create panel, set Layout manager etc
	// This is done only once, after calling the constructor
	private void initialisePanels()
	{
		controlPanel = new Panel();
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		controlPanel.setLayout(flowLayout);
		
		subPanel = new Panel();
		
		frame.setLayout(new BorderLayout());
	}
	
	////////////////////////////////////////////////////////////////////////
	// Methods to add specific iWidgets to Control Panel
	public void addiLabel(String iWidgetLabel)
	{
		//Create label and add to controlPanel
		Label label = new Label(iWidgetLabel);
		subPanel.add(label);
		controlPanel.add(subPanel);
	}
	
	public void addiButton(String iWidgetLabel)
	{
		//Create new button 
		Button button = new Button(iWidgetLabel);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		// Set ActionCommand to incorporate button label and iPlot ID number
		// - to prevent interaction between iPlots which have iWidgets with the
		// same labels.
		button.setActionCommand(iWidgetLabel+"_"+iPlotNum);

		//Add actionlistener to button
		button.addActionListener(new iButtonListener(framework));

		subPanel.add(button);	//add button to panel
		controlPanel.add(subPanel);
	}
	
	public void addiTickBox(String iWidgetLabel)
	//For future choice of tickBox state:
//	private void addiTickBox(String iWidgetLabel, String tickState)

	{
		//Create new tickbox and add to controlPanel
		Checkbox tickBox = new Checkbox(iWidgetLabel);
		
		//For future choice of tickBox state
//		if(tickState != null && tickState.equals("1"))
//			tickBox.setState(true);
			
		String tickBoxID = iWidgetLabel+"_"+iPlotNum;

		//Add actionlistener to tickBox
		tickBox.addItemListener(new iTickBoxListener(framework, tickBoxID));

		subPanel.add(tickBox);
		controlPanel.add(subPanel);
	}
	
	public void addiRadioButtons(String title, String iWidgetLabels)
	{
		//Split String up into a list of labels using delimiters
		ArrayList list = labelsToList(iWidgetLabels);
		
		//Count length of ArrayList
		int numButtons = list.size();

		Panel radioPanel = new Panel();	//subpanel containing radio buttons
		radioPanel.setBackground(new Color(210,200,210));
		radioPanel.setLayout(new GridLayout(numButtons+1, 1)); // One column with one row per button
		radioPanel.add(new Label(title));
		
		CheckboxGroup cbGroup = new CheckboxGroup();
		
		// Add each button
		for(int i=0; i<numButtons; i++)
		{
			String label = list.get(i).toString();
			
			Checkbox radioButton = new Checkbox(label, cbGroup, false);

			String radioButtonID = label+"_"+iPlotNum+"_"+title;
			radioButton.addItemListener(new iRadioButtonListener(framework, radioButtonID));

			radioPanel.add(radioButton);
		}
		controlPanel.add(radioPanel);		
	}
	
	// Still to do...
	public void addiSlider(String iWidgetLabels)
	{
	}
	
	public void addiTextBox(String iWidgetLabel, String w)
	{
		int width = Integer.parseInt(w);
		
		TextField textBox = new TextField(width);

		String textBoxID = iWidgetLabel+"_"+iPlotNum;

		textBox.addActionListener(new iTextBoxListener(framework, textBoxID));
		
		Label label = new Label(iWidgetLabel);
		subPanel.add(label);
		subPanel.add(textBox);
		controlPanel.add(subPanel);
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Convert string of comma separated labels to an ArrayList
	private ArrayList labelsToList(String labels)
	{
		ArrayList list = new ArrayList();
		StringTokenizer tokens = new StringTokenizer(labels);
		
		while (tokens.hasMoreTokens())
		{
			list.add(tokens.nextToken(","));
		}
		
		return list;
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Methods to create and update InfoPanel messages
	//
	// Adds a text message to the InfoPanel
	public void addToInfoPanel(String messageName, String message, String messageValue)
	{
		infoPanel.addMessage(messageName, message, messageValue);
	}

	public void addToInfoPanel(String messageName, String message, int mv)
	{
		String messageValue = ""+mv;
		infoPanel.addMessage(messageName, message, messageValue);
	}

	// Updates the value part of an existing message in the InfoPanel
	public void updateInfoPanel(String messageName, String messageValue)
	{
		infoPanel.updateMessage(messageName, messageValue);
	}

	public void updateInfoPanel(String messageName, int mv)
	{
		String messageValue = ""+mv;
		infoPanel.updateMessage(messageName, messageValue);
	}

	// Resets all values in InfoPanel
	public void resetInfoPanel()
	{
		infoPanel.resetMessages();
	}

	/////////////////////////////////////////////////////////////////////////
	// Add control panel containing newly-added iWidget to the iPlot frame
	public void addToFrame()
	{
		//Change the frame layout and add both the
		//plot canvas and the new panel to the frame
		frame.add(infoPanel.getInfoPanel(), BorderLayout.NORTH);
		frame.add(pgsCanvas, BorderLayout.CENTER);
		frame.add(controlPanel, BorderLayout.SOUTH);
		frame.pack();
	}	
}
