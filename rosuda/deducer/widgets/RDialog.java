package org.rosuda.deducer.widgets;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JDialog;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.REngine.REXP;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.WindowTracker;
import org.rosuda.deducer.toolkit.HelpButton;
import org.rosuda.deducer.toolkit.OkayCancelPanel;

/**
 * A JDialog that keeps track of any widgets put into it
 * @author Ian
 *
 */
public class RDialog extends JDialog {

	protected Vector widgets;
	private Vector components;
	private OkayCancelPanel okayCancelPanel;
	private HelpButton helpButton;
	
	protected static RDialog theDialog;


	/*
	 * JDialog overrides
	 */
	
	public RDialog(){
		super();
		initGUI();
	}
	public RDialog(Dialog owner){
		super(owner);
		initGUI();
	}
	public RDialog(Dialog owner, boolean modal){
		super(owner, modal);
		initGUI();
	}
	public RDialog(Dialog owner, String title){
		super(owner, title);
		initGUI();
	}
	public RDialog(Dialog owner, String title, boolean modal){
		super(owner, title, modal);
		initGUI();
	}
	public RDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc){
		super(owner, title, modal,gc);
		initGUI();
	}
	public RDialog(Frame owner){
		super(owner);
		initGUI();
	}
	public RDialog(Frame owner, boolean modal){
		super(owner, modal);
		initGUI();
	}
	public RDialog(Frame owner, String title){
		super(owner, title);
		initGUI();
	}
	public RDialog(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		initGUI();
	}
	public RDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc){
		super(owner, title, modal,gc);
		initGUI();
	}
	
	/*
	 * Component overrides
	 */
	
	 public Component 	add(Component comp){
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 if(comp instanceof DeducerWidget)
			 widgets.add(comp);
		 else
			 components.add(comp);
		 return super.add(comp);
	 }
	 public Component 	add(Component comp, int index){
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 if(comp instanceof DeducerWidget)
			 widgets.add(comp);
		 else
			 components.add(comp);
		 return super.add(comp,index);
	 }
	 public void 	add(Component comp, Object constraints){
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 if(comp instanceof DeducerWidget)
			 widgets.add(comp);
		 else
			 components.add(comp);
		 super.add(comp,constraints);
	 }
	 public void 	add(Component comp, Object constraints, int index){
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 if(comp instanceof DeducerWidget)
			 widgets.add(comp);
		 else
			 components.add(comp);
		 super.add(comp,constraints,index);
	 }
	 public Component 	add(String name, Component comp){
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 if(comp instanceof DeducerWidget)
			 widgets.add(comp);
		 else
			 components.add(comp);
		 return super.add(name,comp);
	 }
	 public void 	remove(Component comp){
		 super.remove(comp);
		 for(int i=0;i<components.size();i++){
			 if(comp==components.get(i))
				 components.remove(i);
		 }
		 for(int i=0;i<widgets.size();i++){
			 if(comp==widgets.get(i))
				 widgets.remove(i);
		 }
	 }
	 public void 	remove(int index){
		 Component comp = this.getComponent(index);
		 for(int i=0;i<components.size();i++){
			 if(comp==components.get(i))
				 components.remove(i);
		 }
		 for(int i=0;i<widgets.size();i++){
			 if(comp==widgets.get(i))
				 widgets.remove(i);
		 }		 
		 super.remove(index);
	 }
	 public void 	removeAll(){
		 super.removeAll();
		 components.removeAllElements();
		 widgets.removeAllElements();
	 }
	
	
	public void initGUI(){
		
		 if(widgets == null)
			 widgets = new Vector();
		 if(components == null)
			 components = new Vector();
		 
		AnchorLayout thisLayout = new AnchorLayout();
		this.setLayout(thisLayout);
		
		this.setSize(555, 645);
	}
	
	/**
	 * adds a help button
	 * @param pageLocation
	 */
	public void addHelpButton(String pageLocation){
		if(helpButton!=null)
			this.remove(helpButton);
		
		helpButton = new HelpButton(pageLocation);
		this.add(helpButton, new AnchorConstraint(940, 77, 980, 23, 
				AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
		helpButton.setPreferredSize(new java.awt.Dimension(32, 32));		
	}
	
	/**
	 * adds the okay, cancel and run buttons
	 * @param showReset show the reset button?
	 * @param isRun should the approve button be named "okay" or "run"
	 * @param lis the action listener
	 */
	public void setOkayCancel(boolean showReset,boolean isRun,ActionListener lis){
		if(okayCancelPanel!=null){
			this.remove(okayCancelPanel);
		}
		OkayCancelPanel okayCancelPanel = new OkayCancelPanel(showReset, isRun, lis);
		this.add(okayCancelPanel, new AnchorConstraint(926, 978, 980, 402, 
				AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, 
				AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE));
		okayCancelPanel.setPreferredSize(new java.awt.Dimension(307, 32));
	}
	
	/**
	 * Sets all of the widgets to their last state
	 */
	public void setToLast() {
		for(int i=0;i<widgets.size();i++)
			((DeducerWidget)widgets.get(i)).resetToLast();
	}
	
	/**
	 * resets widget states to default
	 */
	public void reset(){
		for(int i=0;i<widgets.size();i++)
			((DeducerWidget)widgets.get(i)).reset();
	}
	
	/**
	 * dialog successfully completed
	 */
	public void completed(){
		for(int i=0;i<widgets.size();i++){
			DeducerWidget wid = (DeducerWidget)widgets.get(i);
			wid.setLastModel(wid.getModel());
		}
	}
	/**
	 * sets the help page for the help button
	 * @param page
	 */
	public void setHelpWikiPage(String page){
		helpButton.setUrl(page);
	}
	
	/**
	 * toggles help button visibility
	 * @param show
	 */
	public void setHelpVisible(boolean show){
		helpButton.setVisible(show);
	}
	
	/**
	 * toggles okay cancel and run button visibility
	 * @param show
	 */
	public void setOkayCancelVisible(boolean show){
		okayCancelPanel.setVisible(show);
	}
	
	/**
	 * 
	 * @return a string which can be evaluated to an R list representation of
	 * 			the widget states
	 */
	public String getWidgetStatesAsString(){
		Vector items = new Vector();
		for(int i=0;i<widgets.size();i++){
			DeducerWidget wid = (DeducerWidget)widgets.get(i);
			items.add("\n'" + wid.getTitle()+"'="+wid.getRModel());
		}
		String states = Deducer.makeRCollection(items, "list", false);	
		return states;
	}

	/**
	 * 
	 * @return an r list of widget states
	 */
	public REXP getWidgetStates(){
		return Deducer.eval(getWidgetStatesAsString());
	}
	
	public void add(Component comp,int top,int right, int bottom, int left, String topType,
			String rightType, String bottomType, String leftType){
			int topTyp = topType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (topType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
			int rightTyp = rightType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (rightType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
			int bottomTyp = bottomType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (bottomType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
			int leftTyp = leftType.equals("REL") ? AnchorConstraint.ANCHOR_REL : (leftType.equals("ABS") ? AnchorConstraint.ANCHOR_ABS : AnchorConstraint.ANCHOR_NONE );
			AnchorConstraint constr = new AnchorConstraint(top, right, bottom, left, 
					topTyp, rightTyp, bottomTyp, leftTyp);
			this.add(comp,constr);
	}
	
	/**
	 * run the dialog
	 */
	public void run(){
		this.setToLast();
		this.setVisible(true);
		if(!Deducer.isJGR()){
			WindowTracker.addWindow(this);
			WindowTracker.waitForAllClosed();
		}
	}
	
}
