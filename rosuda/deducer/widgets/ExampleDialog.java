package org.rosuda.deducer.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;

import javax.swing.JCheckBox;
import javax.swing.JDialog;

import javax.swing.JFrame;

import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.toolkit.HelpButton;
import org.rosuda.deducer.toolkit.OkayCancelPanel;


public class ExampleDialog extends JDialog implements ActionListener {
	private VariableSelectorWidget variableSelector;
	private OkayCancelPanel okayCancelPanel;
	private SliderWidget slider;
	private ComboBoxWidget comboBox;
	private TextAreaWidget textArea;
	private ButtonGroupWidget radioButtons;
	private CheckBoxesWidget checkBoxes;
	private SingleVariableWidget singleVariableSelector;
	private HelpButton helpButton;
	private VariableListWidget variableList;
	
	private Vector widgets;									//Collection of all the DeducerWidgets
	
	private static ExampleDialog theDialog;

	/**
	 * Creates the dialog (if necessary) and
	 * makes it visible.
	 */
	public static void run() {
		if(theDialog == null){
			theDialog = new ExampleDialog(null);
		}
		theDialog.setToLast();
		theDialog.setVisible(true);
	}

	/**
	 * Creates a new Example dialog
	 * 
	 * @param frame Parent JFrame
	 */
	public ExampleDialog(JFrame frame) {
		super(frame);
		widgets = new Vector();
		initGUI();
	}
	
	/**
	 * Sets-up all the GUI components
	 */
	private void initGUI() {
		try {
			/*
			 * AnchorLayout is a flexible GUI layout used
			 * by Jigloo (A GUI builder) that
			 * allows dialog resizing.
			 * see:
			 * http://home.elka.pw.edu.pl/~pdrabik/com/cloudgarden/layout/AnchorLayout.html
			 * 
			 */
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			
			
			
			
			{
				/* Creates a new slider widget */
				slider = new SliderWidget("SliderWidget.java",new String[]{"begin lab","end lab"});
				getContentPane().add(slider, new AnchorConstraint(810, 931, 920, 580, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				slider.setPreferredSize(new java.awt.Dimension(187, 44));
			}
			{
				/* Creates a new ComboBox with three options*/
				comboBox = new ComboBoxWidget("ComboBoxWidget.java",
						new String[]{"Default Option","option 1","option 2"});
				getContentPane().add(comboBox, new AnchorConstraint(675, 378, 764, 23, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				comboBox.setPreferredSize(new java.awt.Dimension(189, 29));
				
				comboBox.setDefaultModel("Default Option"); 	//set default selection
			}
			{
				/* New Text Box */
				textArea = new TextAreaWidget("TextAreaWidget.java");
				getContentPane().add(textArea, new AnchorConstraint(764, 378, 900, 23, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				textArea.setPreferredSize(new java.awt.Dimension(189, 54));
			}
			{
				/* New group of three radio buttons*/
				radioButtons = new ButtonGroupWidget("ButtonGroupWidget.java",
						new String[]{"option 1","option 2","option 3"});
				getContentPane().add(radioButtons, new AnchorConstraint(651, 931, 805, 580, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
				radioButtons.setPreferredSize(new java.awt.Dimension(187, 97));
			}
			{
				/* New group of 6 check boxes laid out in 2 columns*/
				checkBoxes = new CheckBoxesWidget("CheckBoxesWidget.java",
						new String[] {"option 1","option 2","option 3","option 4","option 5","option 6"},2);
				getContentPane().add(checkBoxes, new AnchorConstraint(437, 931, 631, 580, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				checkBoxes.setPreferredSize(new java.awt.Dimension(187, 115));
				
				checkBoxes.addButton(new JCheckBox("added"));	//add another checkbox
				
			}
			{
				/* New Help button pointing to the ExampleDialog Page on the wiki*/
				helpButton = new HelpButton("pmwiki.php?n=Main.ExampleDialog");
				getContentPane().add(helpButton, new AnchorConstraint(940, 77, 980, 23, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				helpButton.setPreferredSize(new java.awt.Dimension(32, 32));
			}
			{
				/* Adds Okay, Cancel and Run buttons. Sets this class to listen for button pushes*/
				okayCancelPanel = new OkayCancelPanel(true, true, this);
				getContentPane().add(okayCancelPanel, new AnchorConstraint(926, 978, 980, 402, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE));
				okayCancelPanel.setPreferredSize(new java.awt.Dimension(307, 32));
			}
			{
				/* New Variable Selector */
				variableSelector = new VariableSelectorWidget();
				getContentPane().add(variableSelector, new AnchorConstraint(12, 428, 660, 12, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS));
				variableSelector.setPreferredSize(new java.awt.Dimension(216, 379));
				variableSelector.setTitle("VariableSelectorWidget.java");
			}
			{
				/* Creates a variable list and links it to the variable selector 
				 * Note: must be created after VariableSelector */
				variableList = new VariableListWidget("VariableListWidget.java",variableSelector);
				getContentPane().add(variableList, new AnchorConstraint(21, 978, 301, 460, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				variableList.setPreferredSize(new java.awt.Dimension(276, 166));
			}
			{
				/* Creates a variable list limited to one item and links it to the variable selector 
				*  Note: must be created after VariableSelector */
				singleVariableSelector = new SingleVariableWidget("SingleVariableWidget.java",variableSelector);
				getContentPane().add(singleVariableSelector, new AnchorConstraint(321, 978, 427, 460, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				singleVariableSelector.setPreferredSize(new java.awt.Dimension(276, 63));
			}
			
			/* Adds Deducer widgets to collection*/
			widgets.add(variableSelector);
			widgets.add(variableList);
			widgets.add(singleVariableSelector);
			widgets.add(checkBoxes);
			widgets.add(radioButtons);
			widgets.add(comboBox);
			widgets.add(textArea);
			widgets.add(slider);
			
			
			reset();			//Sets widgets to defaults
			
			
			this.setSize(553, 645);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Resets widget states to their default values
	 */
	private void reset(){
		for(int i=0;i<widgets.size();i++)
			((DeducerWidget)widgets.get(i)).reset();
	}
	
	/**
	 * Sets widget states to the last time the
	 * dialog was successfully completed. If it
	 * has never been completed, they are set to
	 * the defaults
	 */
	private void setToLast(){
		for(int i=0;i<widgets.size();i++)
			((DeducerWidget)widgets.get(i)).resetToLast();
	}
	
	/**
	 * Called when the dialog is successfully completed
	 * (i.e. the run button is pressed). Sets the 'Last'
	 * states of the widgets to the current values.
	 */
	private void completed(){
		for(int i=0;i<widgets.size();i++){
			DeducerWidget wid = (DeducerWidget)widgets.get(i);
			wid.setLastModel(wid.getModel());
		}
	}

	/**
	 * Handles the Okay, Cancel and Run button actions
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if(cmd=="Run"){
			
			//collects the widget states into a list that can be interpreted by R
			Vector items = new Vector();
			for(int i=0;i<widgets.size();i++){
				DeducerWidget wid = (DeducerWidget)widgets.get(i);
				items.add("\n'" + wid.getTitle()+"'="+wid.getRModel());
			}
			String command = Deducer.makeRCollection(items, "list", false);		//takes the elements of items and makes an R list call
			
			Deducer.execute(command);		//execute command as if it had been entered into the console
			
			completed();	//dialog completed
			this.setVisible(false);
		}else if(cmd=="Cancel")
			this.setVisible(false);
		else if(cmd=="Reset")
			reset();
		
	}

}
