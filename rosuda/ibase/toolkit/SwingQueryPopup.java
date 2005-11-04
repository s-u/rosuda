package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.ToolTipManager;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class SwingQueryPopup implements MouseListener, QueryPopup {

	JToolTip win;
    Window owner;
    PlotComponent pcomp;
    
    SVarSet vs;
	
    public SwingQueryPopup(PlotComponent pc, Frame own, SVarSet vs, String ct) {
        this(pc,own,vs,ct,-1,-1);
    }

    public SwingQueryPopup(PlotComponent pc, Frame own, SVarSet uvs, String ct, int w, int cid)
    {
    	pcomp = pc;
    	owner=own;
    	vs = uvs;
    	
    	ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    	ToolTipManager.sharedInstance().setInitialDelay(0);
    	ToolTipManager.sharedInstance().setReshowDelay(0);
    	ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
   }
    
    
	public void show() {
		if (Global.DEBUG>0) System.out.println("QueryPopup.show");
	    if (Global.DEBUG>0) System.out.println("rendering win visible");
	    ToolTipManager.sharedInstance().setEnabled(true);
	}
	public void hide() {
		if (Global.DEBUG>0) System.out.println("QueryPopup.hide");
	    if (Global.DEBUG>0) System.out.println("hiding win");
	    ToolTipManager.sharedInstance().setEnabled(false);
	}
	public void setContent(PlotComponent pc, String s) {
		setContent(pc,s,-1);
	}
	
	public void setContent(PlotComponent pc, String t, int cid) {
        String s=t;
        if (vs!=null && cid>=0) {
            int i=0;
            s+="\n";
            while (i<vs.count()) {
                if (vs.at(i)!=null && vs.at(i).isSelected()) {
                    s+=vs.at(i).getName()+": "+vs.at(i).atS(cid)+"\n";
                }
                i++;
            }
        }
        setContentString(pc,s);
	}
	
    public void setContent(PlotComponent pc, String t, int[] cid) {
        String s=t;
        if (vs!=null && cid!=null && cid.length>0) {
            int i=0;
            s+="\n";
            boolean isFirst=true;
            while (i<vs.count()) {
                if (vs.at(i)!=null && vs.at(i).isSelected()) {
                    if (isFirst) { isFirst=false; s+=" "; }
                	s += "\n";
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
        setContentString(pc,s);
    }

	
	void setContentString(PlotComponent pc, String s) {
		String t = "<html>";
//		t+= "<BODY BGCOLOR=\"#"+ Math.abs(Common.popupColor.getRGB()) + "\">";
		t+= "<BODY BGCOLOR=\"YELLOW\">";
        StringTokenizer st=new StringTokenizer(s,"\n");
    	while(st.hasMoreElements()) {
    		t+=st.nextToken();
    		if(st.hasMoreElements()) t+="<br>";
    	};
		t+="</body></html>";
        pc.setToolTipText(t);
	}

	
	public void setLocation(int x, int y) {
		hide();show();
	}
	
    public void mouseClicked(MouseEvent e) {
    	hide();
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	
    public Component getQueryComponent() {
    	return win; // returns null, win isn't used in SWING
    }
/*   
    public Window getOwnerWindow() {
    	return owner;
    }
    */
/*    
    public Component getOwnerComponent() {
    	return pcomp.getComponent();
    }
    */
}
