import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.image.*;         
import java.awt.datatransfer.*;  // Clipboard, Transferable, DataFlavor, etc.
import java.awt.event.*;         // New event model.
import java.io.*;                // Object serialization streams.
import java.util.*;              // For StingTokenizer.
import java.util.zip.*;          // Data compression/decompression streams.
import java.util.Vector;         // To store the scribble in.
import java.util.Properties;     // To store printing preferences in.
import java.lang.*;              // 
import javax.swing.*;
import javax.swing.event.*; 

public class Barchart extends DragBox implements ActionListener {
  private Vector rects = new Vector(256,0);            // Store the tiles.
  private Vector labels = new Vector(256,0);           // Store the labels.
  private int width, height, realHeight, startX;       // The preferred size.
  protected int oldWidth, oldHeight;                   // The last size for constructing the bars.
  private Table tablep;                                // The datatable to deal with.
  public String displayMode = "Barchart";
  private boolean moving = false;
  private MyRect movingRect;
  private MyText movingText;
  private int    movingId;
  private int oldY;
  private Image bi;
  private Graphics bg;
  private int k;
  
  public Barchart(JFrame frame, int width, int height, Table tablep) {
    super(frame);
    this.tablep = tablep;
    this.name = tablep.name;
    this.levels = tablep.levels;
    this.names = tablep.names;
    this.lnames = tablep.lnames;
    this.width = width;
    this.height = height;
    
    sb.setUnitIncrement(22);
    sb.setBlockIncrement(22);

 /*   ToolTipManager.sharedInstance().setDismissDelay(300000);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setReshowDelay(0);
    
    this.setToolTipText(" This is it\n is this it?"); */

    frame.getContentPane().add(this);
    
    border = 20;
    
    String titletext;
    if( tablep.count == -1 )
      titletext = "Barchart("+names[0]+")";
    else    
      titletext = "Barchart("+names[0]+"|"+tablep.data.getName(tablep.count)+")";

    frame.setTitle(titletext);

    //    this.setBackground(new Color(255, 255, 152));
    evtq = Toolkit.getDefaultToolkit().getSystemEventQueue();

    // We use low-level events, so we must specify
    // which events we are interested in.
    this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    this.enableEvents(AWTEvent.KEY_EVENT_MASK);
    this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    this.requestFocus();
  }

  public void addDataListener(DataListener l) {
    listener = l;
  }
  
  public void processEvent(AWTEvent evt) {
    if( evt instanceof DataEvent ) {
      if( listener != null )
	listener.dataChanged(tablep.initialVars[0]);
    }
    else super.processEvent(evt);
  }

  public void maintainSelection(Selection S) {

    Rectangle sr = S.r;
    int mode = S.mode;

    Dimension size = this.getSize();

    double x1, x2, y1, y2;

    x2 = (double)(S.r.x + S.r.width - startX)/(size.width - startX - border);

    if( S.r.x < startX ) {
      x1 = (double)(S.r.x - startX);
      if( S.r.x + S.r.width < startX )
        x2 = (double)(S.r.x - startX + S.r.width);
    }
    else
      x1 = (double)(S.r.x - startX)/(double)(size.width - startX - border);

    S.o = new floatRect(x1, 
                        (double)(S.r.y-border)/(double)(realHeight), 
                        x2, 
                        (double)(S.r.y + S.r.height - border)/(realHeight)); 

//System.out.println("In: "+((floatRect)S.o).y1+" <-> "+((floatRect)S.o).y2+" ... "+realHeight);

    S.condition = new Query();
    for( int i = 0;i < rects.size(); i++) {
      MyRect r = (MyRect)rects.elementAt(i);
      if ( r.intersects( sr )) {
        S.condition.addCondition("OR", tablep.names[0]+" = '"+tablep.lnames[0][i]+"'");
        if( tablep.data.isDB )
          tablep.getSelection();
        else {
          double sum=0, sumh=0;
          for( int j=0; j<r.tileIds.size(); j++ ) {
            int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
//System.out.println("Id: "+id+":"+i);
            tablep.setSelection(id,1,mode);
            sumh += tablep.getSelected(id)*tablep.table[id];
            sum  += tablep.table[id];
          }
          r.setHilite( sumh/sum );
        }
      } else
        if( !tablep.data.isDB )
          for( int j=0; j<r.tileIds.size(); j++ ) {
            int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
            tablep.setSelection(id,0,mode);
          }
    }	
  }
    
  public void updateSelection() {
      paint(this.getGraphics());
  }

  public void dataChanged(int var) {
    if( var == tablep.initialVars[0] ) {
      tablep.rebreak();
      realHeight = create(border, border, width-border, height-border, "");
      paint(this.getGraphics());
    }
  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
    paint(this.getGraphics());
  }

  public void scrollTo(int id) {
    sb.setValue(id);
    if( !moving )
      paint(this.getGraphics());
  }

    public void paint(Graphics g) {

      //System.out.println("Still alive 1!");

      tablep.getSelection();

      //System.out.println("Still alive 2!");

      frame.setBackground(MFrame.backgroundColor);

      Dimension size = this.getViewportSize();

      if( oldWidth != size.width || oldHeight != size.height ) {
        this.width = size.width;
        this.height = size.height;
        realHeight = create(border, border, size.width-border, size.height-border, "");
        this.setSize( size.width, realHeight + 2 * border);
        size = this.getSize();
        oldWidth = size.width;
        oldHeight = size.height;
      }

      if( g instanceof PrintGraphics ) {
        size = pj.getPageDimension();
        Font SF = new Font("SansSerif", Font.BOLD, 12);
        g.setFont(SF);
        bg = g;
      }
      else {
        if( bi != null ) {
          if( bi.getWidth(null) != size.width || bi.getHeight(null) != size.height ) {
            bg.dispose();
            bi = null;
            System.gc();
            //System.out.println("New Image!");
            bi = createImage(size.width, size.height);	// double buffering from CORE JAVA p212
          }
        }
        else {
          bi = createImage(size.width, size.height);	// double buffering from CORE JAVA p212
        }
        bg = bi.getGraphics();
        bg.clearRect(0, 0, size.width, size.height);
        bg.translate(0, -sb.getValue());
      }

      int start = -1, stop = k-1;

      for( int i = 0;i < labels.size(); i++) {
        MyText t = (MyText)labels.elementAt(i);
        if( t.y >= sb.getValue() ) {
          t.draw(bg, 1);
          if( start == -1 )
            start = Math.max(0, i-1);
        }
        if( t.y - sb.getValue() > size.height ) {
          stop = i-1;
          i = labels.size();
        }
      }

      for( int i = start;i <= stop; i++) {
        MyRect r = (MyRect)rects.elementAt(i);
        double sum=0, sumh=0;
        for( int j=0; j<r.tileIds.size(); j++ ) {
          int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
          sumh += tablep.getSelected(id)*tablep.table[id];
          sum  += tablep.table[id];
        }
        r.setHilite( sumh/sum );
        r.draw(bg);
      }
      if( moving ) {
        System.out.println("Moving in Barchart: paint");
        movingRect.draw(bg);
      }

      if( !(g instanceof PrintGraphics) ) {
        drawSelections(bg);
        g.drawImage(bi, 0, 0, null);
        bg.dispose();
        //System.out.println("Painting in Barchart from: "+start+"  to: "+stop);
      }  
    }
    
  public void drawSelections(Graphics bg) {

    for( int i=0; i<Selections.size(); i++) {
      Selection S = (Selection)Selections.elementAt(i);
      drawBoldDragBox(bg, S);
    }
  }
  
  public void processMouseMotionEvent(MouseEvent e) {

    boolean info = false;

    if( moving ) {
      movingRect.moveTo(-1, e.getY()+sb.getValue());
      paint(this.getGraphics());
    }/*
    else {
      for( int i = 0;i < rects.size(); i++) {
	MyRect r = (MyRect)rects.elementAt(i);
	if ( r.contains( e.getX(), e.getY()+sb.getValue() )) {
	  info = true;
	  ToolTipManager.sharedInstance().setEnabled(true);
	  this.setToolTipText(r.getLabel());
	}
      }
      if( !info ) {
	ToolTipManager.sharedInstance().setEnabled(false);
	this.setToolTipText("");
      }
    }*/
    super.processMouseMotionEvent(e);  // Pass other event types on.
  }

    public void processMouseEvent(MouseEvent e) {

      if( e.isPopupTrigger() )
        super.processMouseEvent(e);  // Pass other event types on.
      if( changePop ) {
        changePop = false;
        return;
      } 

      boolean info = false;
      if (e.getID() == MouseEvent.MOUSE_PRESSED ||
          e.getID() == MouseEvent.MOUSE_RELEASED ) {
        if ( e.isPopupTrigger() && e.getModifiers() != BUTTON1_DOWN + ALT_DOWN ) {
//System.out.println("pop up trigger in Barchart!!!!"+e.getModifiers());          
          for( int i = 0;i < rects.size(); i++) {
            MyRect r = (MyRect)rects.elementAt(i);
            if ( r.contains( e.getX(), e.getY()+sb.getValue() )) {
              info = true;
              r.pop(this, e.getX(), e.getY());
            }
          }
          if( !info ) {
            JPopupMenu mode = new JPopupMenu();
            if( displayMode.equals("Barchart") ) {
              JMenuItem Spineplot = new JMenuItem("Spineplot");
              mode.add(Spineplot);
              Spineplot.setActionCommand("Spineplot");
              Spineplot.addActionListener(this);
            }
            else {
              JMenuItem Barchart  = new JMenuItem("Barchart");
              mode.add(Barchart);
              Barchart.setActionCommand("Barchart");
              Barchart.addActionListener(this);
            }
            JMenu sorts = new JMenu("Sort by");
            JMenuItem abs = new JMenuItem("absolute Hiliting");
            JMenuItem rel = new JMenuItem("relative Hiliting");
            JMenuItem lex = new JMenuItem("lexicographic");
            sorts.add(abs);
            sorts.add(rel);
            sorts.add(lex);
            abs.setActionCommand("abs");
            rel.setActionCommand("rel");
            lex.setActionCommand("lex");
            abs.addActionListener(this);
            rel.addActionListener(this);
            lex.addActionListener(this);
            mode.add(sorts);

            mode.show(this, e.getX(), e.getY());
          }	
        }
        else
          if( e.getModifiers() ==  BUTTON1_UP + ALT_DOWN ) {
            for( int i = 0;i < rects.size(); i++) {
              MyRect r = (MyRect)rects.elementAt(i);
              if ( r.contains( e.getX(), e.getY()+sb.getValue() )) {
                movingId   = i;
System.out.println("Mooving ....................");                
                movingRect = r;
                oldY       = r.getRect().y;
                movingText = (MyText)labels.elementAt(i);
                moving = true;
                frame.setCursor(Frame.HAND_CURSOR);
              }
            }
          }
        else if( (e.getID() == MouseEvent.MOUSE_RELEASED) && moving &&
                 ((e.getModifiers() ==  BUTTON1_UP + ALT_DOWN) ||
                  (e.getModifiers() ==  BUTTON1_UP) ||
                  (e.getModifiers() ==  ALT_DOWN))) {
          //	 ((e.getModifiers() ==  BUTTON1_UP + CTRL_DOWN) || (e.getModifiers() ==  BUTTON1_UP))) {
          System.out.println("in Barchart up: e.getModifiers(): "+e.getModifiers()+"  BUTTON1_UP: "+BUTTON1_UP);
          (this.getGraphics()).drawImage(bi, 0, 0, null);
          moving = false;
          movingRect.moveTo(-1, oldY);
          frame.setCursor(Frame.DEFAULT_CURSOR);
          dataSet.Variable v = (dataSet.Variable)(tablep.data.data.elementAt(tablep.initialVars[0]));
out: {
  for( int i = 0;i < rects.size(); i++) {
    MyRect r = (MyRect)rects.elementAt(i);
    if( r.contains( e.getX(), e.getY()+sb.getValue() ) ) {
      //System.out.println("Exchange: "+movingId+" with: "+i);
      int tmp = v.permA[movingId];
      v.permA[movingId] = v.permA[i];
      v.permA[i] = tmp;
      break out;
    }
    if( (e.getY()+sb.getValue() < (r.getRect()).y) ) {
      //System.out.println("Insert: "+movingId+" before: "+i);
      if( movingId < i ) {
        int tmp = v.permA[movingId];
        for( int j=movingId; j<i-1; j++ ) {
          v.permA[j] = v.permA[j+1];
        }
        v.permA[i-1] = tmp;
      }
      else {
        int tmp = v.permA[movingId];
        for( int j=movingId; j>i; j-- ) {
          v.permA[j] = v.permA[j-1];
        }
        v.permA[i] = tmp;
      }		  
      break out;
    }
  }
  int tmp = v.permA[movingId];                    // Insert AFTER the last Bin
  for( int j=movingId; j<rects.size()-1; j++ ) {
    v.permA[j] = v.permA[j+1];
  }
  v.permA[rects.size()-1] = tmp;
}
for( int j=0; j<v.levelP; j++ )
v.IpermA[v.permA[j]] = j;

this.dataFlag = true;                            // this plot was responsible
dataChanged(tablep.initialVars[0]);              // and is updated first!

DataEvent de = new DataEvent(this);              // now the rest is informed ...
evtq.postEvent(de);
          }
else
super.processMouseEvent(e);  // Pass other event types on.
    }
else
super.processMouseEvent(e);  // Pass other event types on.
  }
  
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if( command.equals("Barchart") || command.equals("Spineplot")) {
      displayMode = command;
      rects.removeAllElements();
      realHeight = create(border, border, width-border, height-border, "");
      Graphics g = this.getGraphics();
      paint(g);
      g.dispose();
    }
    else if( command.equals("abs") || command.equals("rel") || command.equals("lex") ) {
        double[] sortA = new double[tablep.levels[0]];
        dataSet.Variable v = (dataSet.Variable)(tablep.data.data.elementAt(tablep.initialVars[0]));
        //
        // first get all highlighting fixed
        //
        for( int i = 0;i <sortA.length; i++) {
            MyRect r = (MyRect)rects.elementAt(i);
            double sum=0, sumh=0;
            for( int j=0; j<r.tileIds.size(); j++ ) {
                int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
                sumh += tablep.getSelected(id)*tablep.table[id];
                sum  += tablep.table[id];
            }
            r.setHilite( sumh/sum );
        }
        //
        // filling arrays to sort them
        //
        if( command.equals("abs") )
            for( int i=0; i<sortA.length; i++ ) 
                sortA[i] = ((MyRect)rects.elementAt(i)).getAbsHilite();
        if( command.equals("rel") ) 
            for( int i=0; i<sortA.length; i++ )
                sortA[i] = ((MyRect)rects.elementAt(i)).getHilite();
        int[] perm = Qsort.qsort(sortA, 0, sortA.length-1);
        int[] tperm = new int[perm.length];
        for( int i=0; i<perm.length; i++ )
            tperm[i] = v.permA[perm[i]];
        for( int i=0; i<perm.length; i++ ) {
            v.permA[i] = tperm[i];
            v.IpermA[v.permA[i]] = i;
        }
        if( command.equals("lex") )
            v.sortLevels();	

        this.dataFlag = true;                            // this plot was responsible
        dataChanged(tablep.initialVars[0]);              // and is updated first!
      
        DataEvent de = new DataEvent(this);              // now the rest is informed ...
        evtq.postEvent(de);
    }
    else
        super.actionPerformed(e);
  }
  
    public int create(int x1, int y1, int x2, int y2, String info) {

      rects.removeAllElements();
      labels.removeAllElements();

      this.name = tablep.name;
      this.levels = tablep.levels;
      this.names = tablep.names;
      this.lnames = tablep.lnames;

      this.k = levels[0];
      double sum = 0;
      double max = 0;
      Vector[] tileIds = new Vector[k];

//      boolean allDotNull = true;
//      for(int i=0; i<lnames[0].length; i++)
//        if( !lnames[0][i].endsWith(".0"))
//          allDotNull = false;
//      if( allDotNull )
//        for(int i=0; i<lnames[0].length; i++)
//          lnames[0][i] = lnames[0][i].substring(0, lnames[0][i].length()-2);
      
      for(int i=0; i<k; i++ ) {
        sum += tablep.table[i];
        max = Math.max( max, tablep.table[i] );
        tileIds[i] = new Vector(1,0);
        tileIds[i].addElement(new Integer(i));
      }

      Image ti = createImage(10,10);
      Graphics g = ti.getGraphics();
      FontMetrics FM = g.getFontMetrics();

      int fh = FM.getHeight();
      g.dispose();

      int x = 0;
      for(int i=0; i<k; i++ )
        if( tablep.data.phoneNumber( tablep.initialVars[0] ) )
          x = Math.max(x, FM.stringWidth(Util.toPhoneNumber(Util.atod(lnames[0][i]))));
        else
          x = Math.max(x, FM.stringWidth(lnames[0][i]));

      x = Math.min(x, 100);
      startX= x1 + x;
      int y = 0;

      int w;
      double h, hi;

      for(int i=0; i<k; i++ ) {
        h = (Math.max(y2-y1, k*22)+10)/k-10;
        w = (x2-x1-x);
        if( tablep.data.phoneNumber( tablep.initialVars[0] ) )
          labels.addElement(new MyText( Util.toPhoneNumber(Util.atod(lnames[0][i])), x1 + x-10, y1 + y+(int)(h/2)+fh/2));
        else {
          String shorty = lnames[0][i];
          String addOn = "";
          while( FM.stringWidth( shorty ) >=100 ) {
            shorty = shorty.substring(0,shorty.length() - 1);
            addOn = "...";
          }
          labels.addElement(new MyText( shorty.trim()+addOn, x1 + x-8, y1 + y+(int)(h/2) + fh/2));      
        }
        hi = Math.max(12, h);
        if( displayMode.equals("Barchart") ) {
          h = hi;
          w *= tablep.table[i] / max;
        }
        else 
          h *= tablep.table[i] / max;
        if( tablep.data.phoneNumber( tablep.initialVars[0] ) )
          rects.addElement(new MyRect( true, 'x', "Observed", x1 + x, y1 + y, w, (int)h, 
                                       tablep.table[i], tablep.table[i], 1, 0, 
                                       Util.toPhoneNumber(Util.atod(lnames[0][i]))+'\n', tileIds[i]));
        else
          rects.addElement(new MyRect( true, 'x', "Observed", x1 + x, y1 + y, w, (int)h, 
                                       tablep.table[i], tablep.table[i], 1, 0, lnames[0][i]+'\n', tileIds[i]));
        y += hi+10;
      }

      realHeight = y - 10;

      Dimension size = this.getSize();

      for( int i=0; i<Selections.size(); i++) {
        Selection S = (Selection)Selections.elementAt(i);
        if( ((floatRect)S.o).x1 <= 0 )
          S.r.x      = (int)(startX + ((floatRect)S.o).x1);	    
        else
          S.r.x      = startX + (int)(((floatRect)S.o).x1*(double)(size.width-startX-border));

        if( ((floatRect)S.o).x2 <= 0 )
          S.r.width  = -(int)(((floatRect)S.o).x1 - ((floatRect)S.o).x2);
        else
          S.r.width  = (int)(((floatRect)S.o).x2*(size.width - startX - border) + startX - S.r.x);

        S.r.y        = border + (int)(((floatRect)S.o).y1*(double)(realHeight));
        S.r.height   = (int)(((floatRect)S.o).y2*(realHeight)  - 
                             ((floatRect)S.o).y1*(realHeight));

        //System.out.println("Out: "+S.r.y+" <-> "+S.r.height);
      }

      return realHeight;
    }
    
  private String name;          // the name of the table;
  private double table[];	// data in classical generalized binary order
  private int[] levels;    	// number of levels for each variable
  private int[] plevels;        // reverse cummulative product of levels

  private String[] names;	// variable names
  private String[][] lnames;	// names of levels
  private DataListener listener;
  private static EventQueue evtq;

  class floatRect {
    
    double x1, y1, x2, y2;
    
    public floatRect(double x1, double y1, double x2, double y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
    }
  }
}

class DataEvent extends AWTEvent {
  public DataEvent(Barchart b) {
    super( b, DATA_EVENT );
  }
  public static final int DATA_EVENT = AWTEvent.RESERVED_ID_MAX + 3;
}
