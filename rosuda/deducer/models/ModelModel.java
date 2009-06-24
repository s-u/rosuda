package org.rosuda.deducer.models;

import javax.swing.DefaultListModel;

public class ModelModel extends Object{
	public String formula = "";
	public String data = "";
	public String subset = "";
	public DefaultListModel outcomes = new DefaultListModel();
	public DefaultListModel numericVars = new DefaultListModel();
	public DefaultListModel factorVars = new DefaultListModel();
}
