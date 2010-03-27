package org.rosuda.deducer.widgets;

import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.toolkit.SingletonAddRemoveButton;
import org.rosuda.deducer.toolkit.SingletonDJList;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;

/**
 * Creates a widget for selecting a single variable from a VariableSelector
 * @author Ian
 *
 */
public class SingleVariableWidget extends JPanel implements DeducerWidget, ActionListener{
	private SingletonAddRemoveButton addRemoveButton;
	private SingletonDJList singleList;
	private JPanel listPanel;
	private VariableSelectorWidget selector;
	private String title;
	private DefaultListModel initialModel;
	private DefaultListModel lastModel;
	
	/**
	 * Create a new SingleVariableWidget
	 * @param panelTitle title
	 * @param varSel VariableSelector to link
	 */
	public SingleVariableWidget(String panelTitle, VariableSelectorWidget varSel) {
		super();
		selector = varSel;
		title = panelTitle;
		selector.getJComboBox().addActionListener(this);
		initGUI();
	}
	/**
	 * create an untitled SingleVariableWidget
	 * @param varSel VariableSelector to link
	 */
	public SingleVariableWidget( VariableSelectorWidget varSel) {
		this(null,varSel);
	}
	
	/**
	 * set-up GUI components
	 */
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(239, 50));
			{
				listPanel = new JPanel();
				BorderLayout listPanelLayout = new BorderLayout();
				this.add(listPanel, new AnchorConstraint(5, 1002, 940, 64, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				listPanel.setPreferredSize(new java.awt.Dimension(195, 45));
				listPanel.setLayout(listPanelLayout);
				if(title!=null)
					listPanel.setBorder(BorderFactory.createTitledBorder(title));
				{
					singleList = new SingletonDJList();
					listPanel.add(singleList, BorderLayout.CENTER);
					singleList.setModel(new DefaultListModel());
				}
			}
			{
				addRemoveButton = new SingletonAddRemoveButton(new String[]{"Add","Remove"},
						new String[]{"Add","Remove"},singleList,selector);
				this.add(addRemoveButton, new AnchorConstraint(14, 156, 845, 0, 
						AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_NONE, 
						AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_ABS));
				addRemoveButton.setPreferredSize(new java.awt.Dimension(32, 34));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * handles changes to the variableSelector
	 */
	public void actionPerformed(ActionEvent act) {
		String cmd = act.getActionCommand();
		
		if(cmd == "comboBoxChanged")
			singleList.setModel(new DefaultListModel());
	}
	
	/**
	 * 
	 * @return the variable list
	 */
	public SingletonDJList getList(){
		return singleList;
	}
	
	/**
	 * 
	 * @return the add remove button
	 */
	public SingletonAddRemoveButton getButton(){
		return addRemoveButton;
	}
	
	public String getSelectedVariable(){
		DefaultListModel mod = (DefaultListModel) singleList.getModel();
		if(mod.size()==0)
			return null;
		else
			return mod.get(0).toString();
	}
	
	/**
	 * adds either an action, mouse or list selection listener 
	 * @param lis
	 */
	public void addListener(EventListener lis) {
		if(lis instanceof ActionListener)
			addRemoveButton.addActionListener((ActionListener) lis);
		if(lis instanceof ListSelectionListener)
			singleList.addListSelectionListener((ListSelectionListener) lis);
		if(lis instanceof MouseListener)
			singleList.addMouseListener((MouseListener) lis);
	}
	
	/*
	 * Start DeducerWidget methods
	 * 
	 * The state (or model) is a DefaultListModel
	 */
	
	
	public void setModel(DefaultListModel mod, boolean removeFromVariableSelector){
		if(removeFromVariableSelector && selector==null)
			setModel(mod,false);
		else{
			if(mod==null)
				mod = new DefaultListModel();
			DefaultListModel newModel = new DefaultListModel();
			boolean exists;
			for(int i=0;i<mod.size();i++){
				if(removeFromVariableSelector){
					exists = selector.remove(mod.get(i));
					if(exists)
						newModel.addElement(mod.get(i));
				}else
					newModel.addElement(mod.get(i));
			}
			singleList.setModel(newModel);
		}
	}

	public Object getModel() {
		return singleList.getModel();
	}

	public String getRModel() {
		String rcall = Deducer.makeRCollection(singleList.getModel(),"c",true);
		return rcall;
	}

	public String getTitle() {
		return title;
	}

	public void reset() {
		setModel(initialModel);
	}

	public void resetToLast() {
		setModel(lastModel);
	}

	public void setDefaultModel(Object model){
		initialModel = (DefaultListModel) model;
		if(lastModel==null)
			lastModel = (DefaultListModel) model;
	}

	public void setLastModel(Object model){
		lastModel = (DefaultListModel) model;
	}

	public void setModel(Object model){
		setModel((DefaultListModel) model,true);
	}

	public void setTitle(String t, boolean show) {
		title=t;
		if(t==null)
			listPanel.setBorder(BorderFactory.createEmptyBorder());
		else if(show)
			listPanel.setBorder(BorderFactory.createTitledBorder(title));
		
	}

	public void setTitle(String t) {
		setTitle(t,false);
	}

	/*
	 * End DeducerWidget methods
	 */
}
