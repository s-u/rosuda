import java.awt.*;               // 
import java.awt.geom.*;         
import java.io.*;               // 

public class MyText {
  
  String s;
  public int x;
  public int y;
  double angle=0;
  int extend=10000;

  public MyText(String s, int x, int y) {

    this.s = s;
    this.x = x;
    this.y = y;
  }

  public MyText(String s, int x, int y, double angle, int extend) {

    this.s = s;
    this.x = x;
    this.y = y;
    this.angle = angle;
    this.extend = extend;
  }

  public void draw(Graphics g) {
    g.drawString(s, x, y);
  }

  public void moveYTo(int y) {
    this.y = y;
  }    

  public void moveXTo(int x) {
    this.x = x;
  }    

  public void moveTo(int x, int y) {
    this.x = x;
    this.y = y;
  }    

  public void draw(Graphics2D g2d) {

    FontMetrics FM;
    FM = g2d.getFontMetrics();

    if( FM.stringWidth(s) >= extend ) {
      String shorty = s;	
      String addOn = "";
      while( FM.stringWidth( shorty ) > extend ) {
        shorty = shorty.substring(0,shorty.length() - 1);
        addOn = "É";
      }
      s = shorty.trim()+addOn;
    }
    // Draw string rotated clockwise angle degrees
    AffineTransform at = new AffineTransform();
    at.setToRotation(angle);
    g2d.setTransform(at);
    g2d.drawString(s, x - FM.stringWidth(s)/2, y);
    at.setToRotation(0);
    g2d.setTransform(at);
  }

  public void draw(Graphics g, int align) {

    FontMetrics FM;
      switch(align) {
        case 0:
          g.drawString(s, x, y);
          break;
        case 1:
          FM = g.getFontMetrics();
          g.drawString(s, x - FM.stringWidth(s), y);
          break;
        case 2:
          FM = g.getFontMetrics();
          g.drawString(s, x - FM.stringWidth(s)/2, y);
    }
  }
}
