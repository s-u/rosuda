import java.awt.*;
import java.util.*;

/** query popup 
    @version $Id$
*/
class QueryPopup
{    
    class QPCanvas extends LayerCanvas {
	String[] content;
	int width;

	Window win;

	int topMargin=8;
	int bottomMargin=8;
	int leftMargin=10;
	int rightMargin=10; // only for autoWidth, i.e. width<0

	public boolean doUpdate=true;

	QPCanvas(Window wn, String ct, int w) {
	    width=w; win=wn;
	    setContent(ct); 
	}

	public void setContent(String s) {
	    StringTokenizer st=new StringTokenizer(s,"\n");
	    int tok=st.countTokens();
	    if (tok<1) {
		content=new String[1]; content[0]="";
	    } else {
		content=new String[tok];
		int i=0;
		for(;st.hasMoreElements();) {
		    String c=st.nextToken();
		    content[i]=c; i++;
		};
	    };
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
		if (lw>xw) xw=lw;
		j++;
	    };

	    if (rw<30) rw=leftMargin+rightMargin+xw;
	    doUpdate=false;
	    Dimension csz=getSize();
	    if (csz.width!=rw || csz.height!=rh) {
		setSize(rw,rh); 
		win.setSize(rw,rh);
		win.pack();
	    };
	}

	public void paintLayer(Graphics g, int layer) {
	    if (doUpdate) updateGeometry(g);
	    Dimension s=getSize();
	    g.drawRect(0,0,s.width-1,s.height-1);
	    int y=topMargin+xh*3/4;
	    int i=0;
	    while (i<content.length) {
		g.drawString(content[i],leftMargin,y);
		y+=xh;
		i++;
	    };
	}
    }

    QPCanvas cvs;
    Window win;
    Window owner;
   
    public QueryPopup(Frame own, String ct, int w)
    {
	owner=own;
	win=new Window(own);
	win.add(cvs=new QPCanvas(win,ct,w));
	cvs.setSize(100,50);
	win.setBackground(new Color(255,255,220));	
	win.pack();
    }

    public void setContent(String s) {
	cvs.setContent(s);
    }
    
    public void show() {
	if (Common.DEBUG>0) System.out.println("QueryPopup.show");
	if (!win.isVisible()) {	 
	    if (Common.DEBUG>0) System.out.println("rendering win visible");
	    win.pack(); win.setVisible(true);
	};
    }

    public void hide() {
	if (Common.DEBUG>0) System.out.println("QueryPopup.hide");
	if (win.isVisible()) {
	    if (Common.DEBUG>0) System.out.println("hiding win");
	    win.dispose();
	};
    }
    
    public void setLocation(int x, int y) {
	Dimension d=win.getSize();
        if (y+d.height+5>Common.getScreenRes().height)
            y=Common.screenRes.height-d.height-5;
        if (x+d.width+5>Common.getScreenRes().width)
            x=Common.screenRes.width-d.width-5;
	win.setLocation(x,y);
    }
}

