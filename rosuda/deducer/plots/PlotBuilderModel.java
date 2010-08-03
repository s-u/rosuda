package org.rosuda.deducer.plots;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PlotBuilderModel {

	private DefaultListModel listModel = new DefaultListModel();
	
	public DefaultListModel getListModel(){
		return listModel;
	}
	
	public void setListModel(DefaultListModel m){
		listModel = m;
	}
	
	public Object clone(){
		PlotBuilderModel b = new PlotBuilderModel();
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			b.listModel.addElement(e.clone());
		}
		return b;
	}
	
	public boolean isValidAddition(ElementModel em){
		return true;
	}
	
	public String getCall(){
		String cmd ="";
		cmd+="ggplot()";
		boolean hasLayerOrTemplate = false;
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			if(e.getModel().getType().equals("layer") ||
					e.getModel().getType().equals("template"))
				hasLayerOrTemplate=true;
		}
		if(!hasLayerOrTemplate)
			return null;
		for(int i=0;i<listModel.getSize();i++){
			PlottingElement e = (PlottingElement) listModel.get(i);
			if(e.isActive())
				cmd += " +\n\t" + e.getModel().getCall();
		}
		return cmd;
	}
	
	
	
	public void tryToFillRequiredAess(Layer l){
		Vector aess = l.aess;
		for(int j=0;j<aess.size();j++){
			Aes aes = (Aes) aess.get(j);
			if(!aes.required)
				continue;
			if(!(aes.variable==null || aes.variable.length()==0) || aes.value!=null)
				continue;
			for(int i=listModel.size()-1;i>=0;i--){
				if(aes.variable!=null && aes.variable.length()>0)
					continue;
				PlottingElement e = (PlottingElement) listModel.get(i);
				ElementModel em = e.getModel();
				if(em instanceof Layer){
					Vector laess = ((Layer)em).aess;
					if(l.data != null && !l.data.equals(((Layer)em).data))
						continue;
					for(int k=0;k<laess.size();k++){
						Aes laes = (Aes) laess.get(k);
						if(laes.name.equals(aes.name) && laes.variable!=null && laes.variable.length()>0
								&& !laes.variable.startsWith("..")){
							aes.variable = laes.variable;
							l.data = ((Layer)em).data;
						}
					}
				}
			}
		}
	}
	
	
	public Element toXML(){
		try{
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element node = doc.createElement("PlottingBuilderModel");
			for(int i=0;i<listModel.getSize();i++){
				Element e = ((PlottingElement)listModel.get(i)).toXML();
				e = (Element) doc.importNode(e, true);
				node.appendChild(e);
			}
			node.setAttribute("className", "org.rosuda.deducer.plots.PlotBuilderModel");
			doc.appendChild(node);
			return node;
			
        }catch(Exception e){e.printStackTrace();return null;}
	}
	
	public void setFromXML(Element node){
		String cn = node.getAttribute("className");
		if(!cn.equals("org.rosuda.deducer.plots.PlotBuilderModel")){
			System.out.println("Error PlotBuilderModel: class mismatch: " + cn);
			(new Exception()).printStackTrace();
		}
		try{
			listModel = new DefaultListModel();
			NodeList nl = node.getChildNodes();// .getElementsByTagName("PlottingElement");
			for(int i=0;i<nl.getLength();i++){
				if(!(nl.item(i) instanceof Element))
					continue;
				Element e = (Element) nl.item(i);
				String className = e.getAttribute("className");
				PlottingElement pe = (PlottingElement) Class.forName(className).newInstance();
				pe.setFromXML(e);
				listModel.addElement(pe);
			}
		} catch (Exception e1) {e1.printStackTrace();}
	}
	
	public void saveToFile(File f){
		Element e = this.toXML();
		Document doc = e.getOwnerDocument();
		try{
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans;
			trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();
			//System.out.println(xmlString);
			
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
			out.write(xmlString);
			
			out.close();
			fos.close();
			sw.close();
			
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void setFromFile(File f){
		try{
			listModel.removeAllElements();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(f);
			Element e = (Element)doc.getChildNodes().item(0);
			
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans;
			trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();
			//System.out.println(xmlString);
			
			
			this.setFromXML(e);
		}catch(Exception ex){ex.printStackTrace();}
	}
	
}
