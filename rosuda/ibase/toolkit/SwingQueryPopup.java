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
      
    SVarSet vs;
    
    SWINGGraphicsDevice grdev;
	
    public SwingQueryPopup(SWINGGraphicsDevice gd, final Window ow, final SVarSet vs, final String ct) {
        this(gd, ow,vs,ct,-1,-1);
    }

    public SwingQueryPopup(SWINGGraphicsDevice gd, final Window ow, final SVarSet uvs, final String ct, final int w, final int cid)
    {
    	grdev=gd;
    	vs = uvs;
    	
    	ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    	ToolTipManager.sharedInstance().setInitialDelay(0);
    	ToolTipManager.sharedInstance().setReshowDelay(0);
    	ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
   }
    
    
	public void show() {
		if (Global.DEBUG>0) System.out.println("SwingQueryPopup.show");
	    //if (Global.DEBUG>0) System.out.println("rendering win visible");
	    ToolTipManager.sharedInstance().setEnabled(true);
	}
	public void hide() {
		if (Global.DEBUG>0) System.out.println("SwingQueryPopup.hide");
	    //if (Global.DEBUG>0) System.out.println("hiding win");
	    ToolTipManager.sharedInstance().setEnabled(false);
	}
	public void setContent(final String s) {
		setContent(s,-1);
	}
	
	public void setContent(final String t, final int cid) {
        String s=t;
        if (vs!=null && cid>=0) {
            
            s+="\n";
            int i = 0;
            while (i<vs.count()) {
                if (vs.at(i)!=null && vs.at(i).isSelected()) {
                    s+=vs.at(i).getName()+": "+vs.at(i).atS(cid)+"\n";
                }
                i++;
            }
        }
        setContentString(s);
	}
	
    public void setContent(final String t, final int[] cid) {
        String s=t;
        if (vs!=null && cid!=null && cid.length>0) {
            
            s+="\n";
            boolean isFirst=true;
            int i = 0;
            while (i<vs.count()) {
                if (vs.at(i)!=null && vs.at(i).isSelected()) {
                    if (isFirst) { isFirst=false; s+=" "; }
                	s += "\n";
                    if (vs.at(i).isNum()) {
                        int j=0;
                        double sum = 0.0;
                        double ct = 0.0;
                        while (j<cid.length) {
                            if (vs.at(i).at(cid[j])!=null) {
                                sum+=vs.at(i).atD(cid[j]);
                                ct+=1.0;
                            }
                            j++;
                        }
                        if (ct>0) {
                            
                            final double mean=sum/ct;
                            j=0;
                            double sd = 0.0;
                            while (j<cid.length) {
                                if (vs.at(i).at(cid[j])!=null) {
                                    final double dif=vs.at(i).atD(cid[j])-mean;
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

	
	void setContentString(final String s) {
		String t = "<html>";
		t+= "<BODY BGCOLOR=\"rgb("+Math.abs(Common.popupColor.getRed())+","+Math.abs(Common.popupColor.getGreen())+","+Math.abs(Common.popupColor.getBlue())+")\">";
//		t+= "<BODY BGCOLOR=\"YELLOW\">";
        final StringTokenizer st=new StringTokenizer(s,"\n");
    	while(st.hasMoreElements()) {
    		t+=st.nextToken();
    		if(st.hasMoreElements()) t+="<br>";
    	}
		t+="</body></html>";
        grdev.setToolTipText(t);
	}

	
	public void setLocation(final int x, final int y) {
		hide();show();
	}
	
    public void mouseClicked(final MouseEvent e) {
    	hide();
    }
    public void mousePressed(final MouseEvent e) {}
    public void mouseReleased(final MouseEvent e) {}
    public void mouseEntered(final MouseEvent e) {}
    public void mouseExited(final MouseEvent e) {}
	
    public Component getQueryComponent() {
    	return win; // returns null, win isn't used in SWING
    }
    
}
