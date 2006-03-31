
//
// A closeable Frame for Mondrian.
//

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.*;

public class MFrame extends JFrame implements WindowListener {
  
  private Join J;
  private JMenuItem m;
  public String selString = "";

  static Color backgroundColor = new Color(223, 184, 96);
  static Color objectColor     = Color.lightGray;
  static Color lineColor       = Color.black;
  
  public MFrame(Join J) {
    this.J = J;
    this.setBackground(backgroundColor);
    addWindowListener(this);
  }
  
  public void windowClosing(WindowEvent e) {
    close();
  }

  public boolean getAlphaHi() {
    return J.alphaHi;
  }

  public boolean hasR() {
    return J.hasR;
  }

  public void close() {
    System.out.println("Window Closed!!");

    J.windows.remove(m);
    if( J.windows.getItemCount() < 3 )
      J.ca.setEnabled(false);
    if( !selString.equals("") )
      J.updateSelection();
    this.setVisible(false);
    this.dispose();
  }

  public void maintainMenu(int step) {
    selString += " ["+step+"]";
    m.setText( getTitle() + selString );
  }

  public void maintainMenu(boolean preserve) {
    if( !preserve )
      selString = "";
    m.setText( getTitle() + selString );
  }

  public void show() {
    boolean same = false, added = false;

    m = new JMenuItem(getTitle());
    J.ca.setEnabled(true);

    for(int i=2; i<J.windows.getItemCount(); i++)
      if( ((J.windows.getItem(i)).getText()).substring(0,2).equals((m.getText()).substring(0,2)) )
        same = true;
      else
        if( same ) {
          J.windows.insert(m, i);
          added = true;
          same = false;
        }

    if( !added )
      J.windows.add(m);
    
    m.addActionListener(new ActionListener() {     // Open a new mosaic plot window
      public void actionPerformed(ActionEvent e) {
        toFront();
      }
    });

    super.show();
  }
  
  public void windowClosed(WindowEvent e) {}
  public void windowIconified(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}
  public void windowDeiconified(WindowEvent e) {}
  public void windowActivated(WindowEvent e) {}
  public void windowDeactivated(WindowEvent e) {}
}
