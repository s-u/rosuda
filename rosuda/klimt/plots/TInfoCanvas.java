import java.awt.*;
import java.util.Enumeration;
//import SNode;
//import DBCanvas;

/** canvas of the query window associated with a tree (hence tree info canvas).
    @version $Id$
*/
class TInfoCanvas extends DBCanvas
{    
    boolean det;
    SNode cn;

    TInfoCanvas()
    {
	setBackground(Common.popupColor);
	det=true;
    };

    /** set node to be used for query and the detail depth
	@param n node to query
	@param detailed if <code>true</code> extended query is displayed
    */
    public void setNode(SNode n, boolean detailed)
    {
	det=detailed; cn=n;
        int h=det?160:80;
	setSize(det?220:150,h);
        getParent().setSize(det?220:150,h);
        Point p=getParent().getLocation();
        if (p.y+h+10>Common.getScreenRes().height)
            getParent().setLocation(p.x,Common.screenRes.height-h-5);
	getParent().repaint();
	repaint();
    };

    public void paintBuffer(Graphics g)
    {
	if (cn==null) return;
	Font fo=getFont();
	Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
	g.setColor(Color.black);
	Dimension d=getSize();
	g.drawRect(0,0,d.width-1,d.height-1);
	g.setColor(new Color(0,0,128));
	g.setFont(f2);
	if (det) {
	    g.drawString("Name: "+cn.Name,15,20);
	    g.drawString("Cases: "+cn.Cases,15,34);
	    g.drawString("Split: "+cn.Cond,15,48);
	    g.drawString("Dev: "+Tools.getDisplayableValue(cn.F1)+" tree / "+Tools.getDisplayableValue(cn.sampleDev)+" sample",15,62);
	    g.drawString("Dev Gain: "+Tools.getDisplayableValue(cn.devGain)+" tree / "+Tools.getDisplayableValue(cn.sampleDevGain)+" sample",15,76);
	    if (cn.getSource()!=null) {
		SVarSet src=cn.getSource();
		SMarker m=cn.getSource().getMarker();
		int dMark=0, dTot=0;
		if (m!=null) {
		    if ((m!=null)&&(cn.data!=null))
			for (Enumeration e=cn.data.elements(); e.hasMoreElements();) {
			    int ix=((Integer)e.nextElement()).intValue();
			    if (m.at(ix)) dMark++;
			    dTot++;
			};
		    
		    g.drawString("Total selected: "+m.marked()+" of "+m.size(),15,90);
		    g.drawString("Node selected: "+dMark+" of "+dTot,15,104);
		};
		//g.drawString("Split value: "+cn.splitVal,15,104);	    
	    };
	} else {
	    g.drawString("Cases: "+cn.Cases,15,20);
	};
	g.setFont(fo);
	g.setColor(Color.black);
	int yp=det?118:37, cln=1;
	for (Enumeration e=cn.V.elements(); e.hasMoreElements();) {
	    double perc=((Float)e.nextElement()).doubleValue();
	    int cas=(int)(perc*(double)cn.Cases+0.5);
	    int pint=(int)(perc*10000.0);
	    g.drawString("class "+cln+": "+((double)pint)/100+"% ["+(cas)+"]",15,yp);
	    cln++; yp+=15;
	};
    };
};

