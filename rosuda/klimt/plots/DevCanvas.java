import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** implementation of deviation plots
    @version $Id$
*/
public class DevCanvas extends PGSCanvas implements Dependent, MouseListener, KeyListener, ActionListener, Commander
{
    /** associated tree root */
    SNode root;
    /** current final node */
    SNode fin;
    /** associated marker */
    SMarker m;

    Axis ax,ay;
    boolean cumulate=true, lines=false;
    double[] dev;
    double[] dgain;
    int devs=0;

    double min,max;

    int leftm=40, rightm=10, topm=10, botm=20;
    int lastw, lasth;

    MenuItem MIcum, MIlines;
    
    /** creates a new deviance canvas
	@param f frame owning this canvas or <code>null</code> if none
	@param t source tree root variable
    */
    public DevCanvas(Frame f,SNode t) {
	root=t; m=t.getSource().getMarker(); setFrame(f); setTitle("Deviance plot");
	m.addDepend(this);
	setBackground(Common.backgroundColor);
	addMouseListener(this);
	//addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
        ax=new Axis(null,Axis.O_X,Axis.T_EqSize); ax.addDepend(this);
        ay=new Axis(null,Axis.O_Y,Axis.T_Num); ay.addDepend(this);
	setNode(fin);
        MenuBar mb=null;
        String myMenu[]={"+","File","Save as PGS ...","exportPGS","Save as PostScript ...","exportPS","-","Close","WTMclose","Quit","exit","+","View","Show gain","cumulate","Show lines","lines","0"};
        f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
        MIcum=mb.getMenu(1).getItem(0); MIlines=mb.getMenu(1).getItem(1);
    };

    public void setNode(SNode n) {
	if (n==null) n=root;
	if (fin!=n) {
	    fin=n;
	    devs=1;
	    SNode c=n;
	    while (c.getParent()!=null) {
		c=(SNode)c.getParent(); devs++;
	    };
	    dev=new double[devs];
	    dgain=new double[devs];
	    int i=devs-1;
	    c=n;
            min=max=dev[i]=c.F1; dgain[i]=c.isLeaf()?c.F1:c.devGain; i--;
	    while (c.getParent()!=null) {
                c=(SNode)c.getParent(); dev[i]=c.F1; dgain[i]=c.devGain; i--;
		if (c.F1>max) max=c.F1;
		if (c.F1<min) min=c.F1;
	    };
	    ay.setValueRange(0,max);
	    ax.setValueRange(devs);
	    repaint();
	}
    };

    public void Notifying(Object o, Vector path) {
	setNode(m.getNode());
	repaint();
    };

    public Dimension getMinimumSize() { return new Dimension(leftm+rightm+10,topm+botm+10); };

    public void paintPoGraSS(PoGraSS g) {
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
        if (w!=lastw || h!=lasth) {
            ax.setGeometry(ax.or,leftm,w-leftm-rightm);
            ay.setGeometry(ay.or,h-botm,topm+botm-h);
        };

        g.setBounds(w,h);
        g.begin();
        g.defineColor("fill",255,255,255);
        g.defineColor("outline",0,0,0);
        g.defineColor("red",255,0,0);
        g.defineColor("labels",0,0,64);
        
	g.setColor("outline");
	g.drawLine(leftm,h-botm,leftm,topm); 
	g.drawLine(leftm,h-botm,w-rightm,h-botm);

	int i=0;
        int lsy=0;
        if (lines) g.setColor("red");
        
	while(i<devs) {
            int x1=ax.getValuePos(i);
            int x2=ax.getValuePos(i+1);

            int ly=ay.getValuePos(0);
            int vy=ay.getValuePos(cumulate?dev[i]:dgain[i]);
            
            if (lines) {
                int dy=x2-x1; x1-=dy/2; x2-=dy/2;
                if (i==0 && devs==1)
                    g.drawLine(x1,vy,x2,vy);
                else {
                    if (i>0)
                        g.drawLine(x1,lsy,x2,vy);
                };
                lsy=vy;                    
            } else {
                g.setColor("fill");
                g.fillRect(x1,vy,x2-x1,ly-vy);
                g.setColor("outline");
                g.drawRect(x1,vy,x2-x1,ly-vy);
            };
	    i++;
	};

        g.setColor("labels");
	{
	    double f=ay.getSensibleTickDistance(50,18);
	    double fi=ay.getSensibleTickStart(f);
	    while (fi<ay.vBegin+ay.vLen) {
		int t=ay.getValuePos(fi);
		g.drawLine(leftm-5,t,leftm,t);
		//if (showLabels)
		String s=""+fi;
		if (s.length()>2 && s.substring(s.length()-2).compareTo(".0")==0)
		    s=s.substring(0,s.length()-2);
		g.drawString(s,5,t+5);
		fi+=f;
	    };
	}
        g.end();
    };
    
    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
    };

    public void mousePressed(MouseEvent ev) {
	int x=ev.getX(), y=ev.getY();
    };
    public void mouseReleased(MouseEvent e) {
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};

    public void mouseDragged(MouseEvent e) {
	int x=e.getX();
    };
    public void mouseMoved(MouseEvent e) {
    };

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='C') run(this,"exportCases");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='l') run(this,"lines");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='c') run(this,"cumulate");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="lines") {
            lines=!lines;
            MIlines.setLabel(lines?"Show bars":"Show lines");
            repaint();
        };
	if (cmd=="cumulate") {
	    cumulate=!cumulate;
            MIcum.setLabel(cumulate?"Show gain":"Show deviance");
	    repaint();
	};
	return null;
    };

    /** action listener methods reroutes all request to the commander interface */
    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    };
    
};
