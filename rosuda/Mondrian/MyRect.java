import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.event.*;         // New event model.
import java.util.*;              // For StingTokenizer.
import java.util.Vector;         // To store the scribble in.
import java.lang.*;              // 
import javax.swing.*;
import javax.swing.event.*;

public class MyRect extends Rectangle implements ActionListener {
  public int x, y, w, h;
  private int plusX = 0;
  private int plusY = 1;
  private String info;
  private String mode;
  private Graphics g;
  private JMenuItem infoText;
  private JPopupMenu popup;
  private boolean full;
  public boolean censored = false;
  public char dir;
  public double obs = 1;
  private double hilite;
  private double exp;
  private double scale;
  private float p;
  private Color drawColor = Color.black;
  private JPanel panel;
  public Vector tileIds;

  public MyRect(boolean full, char dir, String mode, 
                int x, int y, int w, int h, 
                double obs, double exp, double scale, double p, String info, Vector tileIds) {
    super(x, y, w, h);
    this.full = full; this.dir = dir; this.exp = exp; this.scale = scale; this.mode = mode;
    this.x = x; this.y = y; this.w = w; this.h = h; this.obs = obs; this.p = (float)p; this.info = info;
    this.tileIds = tileIds; this.panel = panel;
  }

  public void moveTo(int x, int y) {
    if( x != -1 )
      this.x = x;
    if( y != -1 )
      this.y = y;
  }

  public Rectangle getRect() {
    return (new Rectangle(x, y, w, h));
  }

  public void actionPerformed(ActionEvent e) {
    // Dummy, since we just use it for information display
  }

  public void setHilite(double hilite) {
    this.hilite = hilite;
  }

  public void setColor(Color color) {
    this.drawColor = color;
  }

  public double getHilite() {
    return hilite;
  }

  public double getAbsHilite() {
    return hilite * obs;
  }

  public void draw(Graphics g) {

    //System.out.println(residual);
    if( obs > 0 ) {
      if( dir != 'f' ) {
        if( info.indexOf("¥") == -1 )
          g.setColor(Color.lightGray);
        else
          g.setColor(Color.white);
        g.fillRect(x, y+Math.max(0, h-height), Math.min(w, width), Math.min(h, height) + 1);
      } else {
        g.setColor(drawColor);
        g.fillRect(x, y, w, h);
      }
    }
    if( mode.equals("Expected") ) {
      int high = (int)(192+63*(0.15+Stat.pnorm((1-p-0.9)*10)));
      int low =  (int)(192*(0.85-Stat.pnorm((1-p-0.9)*10)));
      //System.out.println(Stat.pnorm((1-p-0.9)*15));
      if( obs-exp > 0.00001 )
        g.setColor(new Color(low, low, high));
      else if( obs-exp < -0.00001 )
        g.setColor(new Color(high, low, low));
      else
        g.setColor(Color.lightGray);	
      if( dir == 'x' )
        g.fillRect(x, y, (int)((double)w*Math.abs((obs-exp)/Math.sqrt(exp)*scale)), h);
      else if ( dir == 'y' )
        g.fillRect(x, y+h-(int)((double)h*Math.abs((obs-exp)/Math.sqrt(exp)*scale)),
                   w, (int)((double)h*Math.abs((obs-exp)/Math.sqrt(exp)*scale)));
    }
    if( obs == 0 || censored )
      g.setColor(Color.red);
    else
      g.setColor(Color.black);

    if( hilite > 0 ) {
      Color c = g.getColor();
      g.setColor(DragBox.hiliteColor);
//System.out.println("wh: "+(int)((double)w*hilite));
      if( Math.min(w, width) > 2 && Math.min(h, height) > 2 ) {  // Mit Rahmen
        plusX = 1;
        plusY = 0;
      }	
      if( dir == 'x' )
        g.fillRect(x+plusX, y+Math.max(0, h-height), (((int)((double)w*hilite) == 0) ? 1: (int)Math.min(width, ((double)w*hilite))), Math.min(h, height));
      else if ( dir == 'y' )
        g.fillRect(x, y+Math.max(0, h-height)+Math.min(h, height)-(((int)((double)(h+plusY)*hilite) == 0) ? (1-plusY): (int)Math.min(height, ((double)h*hilite))),
                   Math.min(w, width), (((int)((double)(h+plusY)*hilite) == 0) ? 1: (int)Math.min(height+plusY, ((double)(h+plusY)*hilite))));
      else {
        g.setColor(new Color(255, 0, 0, (int)(255*hilite)));
        g.fillRect(x, y, w, h);
      }
      g.setColor( c );
    }
    if( dir !='f' && Math.min(w, width) > 2 && Math.min(h, height) > 2 || obs == 0 || censored )
      g.drawRect(x, y+Math.max(0, h-height), Math.min(w, width), Math.min(h, height));
  }	

  public void pop(DragBox panel, int x, int y) {
    popup = new JPopupMenu();

    String pinfo = getLabel();

    StringTokenizer info = new StringTokenizer(pinfo, "\n");

    while( info.hasMoreTokens()) {
      infoText = new JMenuItem ( info.nextToken() );
      popup.add(infoText);
      infoText.addActionListener(this);
      //      infoText.setEnabled(false);
    }

    popup.show(panel, x, y);
  }

  public String getLabel() {
    String pinfo = info.toString();
    if( obs > 0 )
      pinfo += "\n" + "Count: "+obs;
    else
      pinfo += "\n" + "Empty Bin ";
    if( hilite > 0 )
      pinfo += "\n" + "Hilited: "+Stat.round(hilite*obs,0)+" ("+Stat.round(100*hilite,2)+"%)";
    if( mode.equals("Expected") ) {
      pinfo += "\n" + "Expected: "+Stat.round(exp,2);
      pinfo += "\n" + "Residual: "+Stat.round(obs-exp,3);
      pinfo += "\n" + "Scaled Res.:"+Stat.round(Math.abs((obs-exp)/Math.sqrt(exp)*scale/4*100),1)+"%";
    }

    return pinfo;
  }
}

