import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.image.*;         
import javax.swing.*;
import javax.swing.event.*;

public class Test extends Canvas {
  protected JFrame frame;                         // The frame we are within.

  /** This constructor requires a Frame and a desired size */
  public Test() {
    this.setSize(400,400);
  }

  public void paint(Graphics g) {
  
    g.fillRect(10,10,300,300);
  }
}







