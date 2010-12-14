package org.rosuda.deducer.data;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.rosuda.JGR.DataLoader;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.deducer.Deducer;
import org.rosuda.deducer.toolkit.IconButton;
import org.rosuda.ibase.toolkit.EzMenuSwing;


public class DataViewerController {

	
	private static ArrayList dataWindows;
	
	private static Map tabFactories;
	
	private static boolean started = false;
	
	private static JPanel panel;
	
	public static void init(){
		if(!started){
			dataWindows = new ArrayList();
			tabFactories = new LinkedHashMap();
			addTabFactory("Data View", new DataViewFactory());
			addTabFactory("Variable View", new VariableViewFactory());
			

			
			
			panel = new JPanel();
			GridBagLayout panelLayout = new GridBagLayout();
			panelLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			panel.setLayout(panelLayout);
			ActionListener lis = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if(cmd=="Open Data"){	
						new DataLoader();			
					}else if(cmd=="New Data"){
						String inputValue = JOptionPane.showInputDialog("Data Name: ");
						if(inputValue!=null)
							Deducer.eval(inputValue.trim()+"<-data.frame(Var1=NA)");
					}
				}
			};
			JButton newButton = new IconButton("/icons/newdata_128.png","New Data Frame",lis,"New Data");
			newButton.setPreferredSize(new java.awt.Dimension(128,128));
			panel.add(newButton, new GridBagConstraints(0, 0, 1,1,  0.0, 0.0, 
					GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			
			IconButton openButton = new IconButton("/icons/opendata_128.png","Open Data Frame",lis,"Open Data");
			openButton.setPreferredSize(new java.awt.Dimension(128,128));
			panel.add(openButton, new GridBagConstraints(0, 1, 1,1,  0.0, 0.0,
					GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		}
	}
	
	public static void addViewerWindow(DataViewer v){
		dataWindows.add(0, v);
	}
	
	public static ArrayList getViewerWindows(){return dataWindows;}
	
	public static void removeViewerWindow(DataViewer v){
		dataWindows.remove(v);
	}
	
	
	public static void addTabFactory(String name,DataViewerTabFactory t){
		tabFactories.put(name, t);
		for(int i=0;i<dataWindows.size();i++){
			DataViewer dv = (DataViewer)dataWindows.get(i);
			String data = dv.getData();
			dv.reloadTabs(data);
		}
	}
	
	public static void removeTabFactory(String name){
		tabFactories.remove(name);
		for(int i=0;i<dataWindows.size();i++){
			DataViewer dv = (DataViewer)dataWindows.get(i);
			String data = dv.getData();
			dv.reloadTabs(data);
		}
	}
	
	public static String[] getTabNames(){
		Object[] o = tabFactories.keySet().toArray();
		String[] tn = new String[o.length];
		for(int i=0; i<o.length;i++){
			tn[i] = (String) o[i];
		}
		return tn;
	}
	
	
	
	public static DataViewerTab generateTab(String tabName,String dataName){
		DataViewerTabFactory factory = (DataViewerTabFactory) tabFactories.get(tabName);
		if(factory==null){
			new ErrorMsg("Unknown DataViewerTabFactory: " + tabName);
			return null;
		}
		return factory.makeViewerTab(dataName);
	}
	
	public static JPanel getDefaultPanel(){
		return panel;
	}
	
	public static void setDefaultPanel(JPanel p){
		panel = p;
	}
	
}






