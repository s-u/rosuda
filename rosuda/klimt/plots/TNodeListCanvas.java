import java.awt.*;
import java.util.Enumeration;

/** TNodeListCanvas - lists path info of a node (i.e. list of nodes leading to the current one)
    @version $Id$
*/

class TNodeListCanvas extends DBCanvas
{
    public SNode cn;

    TNodeListCanvas()
    {
	setBackground(new Color(255,255,128));
	setSize(250,200);
	cn=null;
    };

    public void setNode(SNode n)
    {
	if (cn!=n) { cn=n; repaint(); };
    };

    public void paintBuffer(Graphics g)
    {
	if (cn==null) return;
	//Font fo=getFont();
	//Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
	g.setColor(Color.black);
	Dimension d=getSize();
	g.drawRect(0,0,d.width-1,d.height-1);
	g.setColor(new Color(0,0,128));
	//g.setFont(f2);
	SNode n=cn;
	int y=20+((n==null)?0:(n.getLevel()*14));
	while (n!=null) {
	    g.drawString(n.Cond+" ("+n.Cases+" cases)",15,y);
	    y-=14;
	    n=(SNode)n.getParent();
	};
    };
};

