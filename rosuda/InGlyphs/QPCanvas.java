package org.rosuda.InGlyphs;

import java.awt.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

class QPCanvas extends LayerCanvas {

	public String[] content;
	int width;

	Window win;
	SVarSet vs;

	int topMargin=8;
	int bottomMargin=8;
	int leftMargin=10;
	int rightMargin=10; // only for autoWidth, i.e. width<0

	public boolean doUpdate=true;

	QPCanvas(Window wn, SVarSet uvs, String ct, int w, int cid) {
		width=w;
		win=wn;
		vs=uvs;
		setContent(ct,cid); 
	}

	public void setContent(String t, int cid) {
		String s=t;
		if (vs!=null && cid>=0) {
			int i=0;
			s+=" \n";
			while (i<vs.count()) {
				if (vs.at(i)!=null && vs.at(i).isSelected()) {
					s+=vs.at(i).getName()+": "+vs.at(i).atS(cid)+"\n";
				}
				i++;
			}
		}
		setContentString(s);
	}

	public void setContent(String t, int[] cid) {
		String s=t;
		if (vs!=null && cid!=null && cid.length>0) {
			int i=0;
			s+="\n \n";
			while (i<vs.count()) {
				if (vs.at(i)!=null && vs.at(i).isSelected()) {
					if (vs.at(i).isNum()) {
						int j=0;
						double sum=0.0, ct=0.0, sd=0.0;
						while (j<cid.length) {
							if (vs.at(i).at(cid[j])!=null) {
								sum+=vs.at(i).atD(cid[j]);
								ct+=1.0;
							}
							j++;
						}
						if (ct>0) {
							double mean=sum/ct;
							j=0;
							while (j<cid.length) {
								if (vs.at(i).at(cid[j])!=null) {
									double dif=vs.at(i).atD(cid[j])-mean;
									sd+=dif*dif;
								}
								j++;
							}
							s+=vs.at(i).getName()+": mean="+Tools.getDisplayableValue(sum/ct)+" sd="+Tools.getDisplayableValue(Math.sqrt(sd/ct))+"\n";
						}
					} else {
					}
				}
				i++;
			}
		}
		setContentString(s);
	}

	void setContentString(String s) {
		StringTokenizer st=new StringTokenizer(s,"\n");
		int tok=st.countTokens();
		if (tok<1) {
			content=new String[1];
			content[0]="";
		}
		else {
			content=new String[tok];
			int i=0;
			for(;st.hasMoreElements();) {
				String c=st.nextToken();
				content[i]=c;
				i++;
			}
		}
		doUpdate=true;
		repaint();
	}

	int xh;

	void updateGeometry(Graphics g) {
		int rw=width;
		FontMetrics fm=g.getFontMetrics();
		int xw=0;
		xh=fm.getHeight();
		int rh=xh*content.length+topMargin+bottomMargin;
		int j=0;
		while(j<content.length) {
			int lw=fm.stringWidth(content[j]);
			if (lw>xw) {
				xw=lw;
			} 
			j++;
		}
		if (rw<30) {
			rw=leftMargin+rightMargin+xw;
		} 
		doUpdate=false;
		Dimension csz=getSize();
		if (csz.width!=rw || csz.height!=rh) {
			setSize(rw,rh); 
			win.setSize(rw,rh);
			win.pack();
		}
	}
	public void paintLayer(Graphics g, int layer) {
		if (doUpdate) {
			updateGeometry(g);
		} 
		Dimension s=getSize();
		g.drawRect(0,0,s.width-1,s.height-1);
		int y=topMargin+xh*3/4;
		int i=0;
		while (i<content.length) {
			g.drawString(content[i],leftMargin,y);
			y+=xh;
			i++;
		}
	}
}