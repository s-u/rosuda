
//
// A drag box - for my good friend Marvin.
//

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.*;
//import javax.swing.event.*;

public
abstract class DragBox

extends JPanel
implements MouseListener, MouseMotionListener, AdjustmentListener, ActionListener

{

  static Color hiliteColor = new Color(180, 96, 135);
  //  static Color hiliteColor = Color.green;

  protected Color	background = Color.black;
  protected Color	dragboxcolor = Color.red;
  protected Graphics dragboxgraphics = null;

  public JFrame frame;                               // The frame we are within.
  public JScrollBar sb;                             // We might need a scroll bar

  public boolean selectFlag;                        // True if selection occured in this DragBox

  public boolean dataFlag;                          // True if change occured in this DragBox

  public boolean selectAll = false;                          // True if the user pressed META-A

  public boolean deleteAll = false;                          // True if the user pressed META-BACKSPACE

  public boolean switchSel = false;                          // True if the user pressed META-M

  public boolean changePop = false;												// True if the Sel Seq Popup was triggered
  
  public boolean scaleChanged = false;              // To indicate paint the new scale (without using events)

  public boolean printing;                               // flag to avoid double buffering while printing ...

  public PrintJob pj;

  public Dimension printerPage;                               // must be accessible in different paints ...

  //
  // The PC implementation may need two minor changes:
  //
  //    1) BUTTON1_DOWN = InputEvent.BUTTON1_MASK
  //    2) Exchange definitions for buttons 2 and 3??
  //
  // Modifiers, as seen during button press - button 1 is strange!
  //

  protected final static int	BUTTON1_DOWN = 16; //InputEvent.BUTTON1_MASK;
  protected final static int	BUTTON2_DOWN = 8;  //InputEvent.BUTTON2_MASK;
  protected final static int	BUTTON3_DOWN = 4;  //InputEvent.BUTTON3_MASK;

  //
  // Modifiers, as seen during button release - notice button 1.
  //

  protected final static int	BUTTON1_UP = BUTTON1_DOWN; //InputEvent.BUTTON1_MASK;
  protected final static int	BUTTON2_UP = BUTTON2_DOWN; //InputEvent.BUTTON2_MASK;
  protected final static int	BUTTON3_UP = BUTTON3_DOWN; //InputEvent.BUTTON3_MASK;

  //
  // Modifiers, as seen during button release - notice button 1.
  //

  protected final static int	SHIFT_DOWN = 1; //InputEvent.SHIFT_MASK;
  protected final static int	CTRL_DOWN  = 2; //InputEvent.CTRL_MASK;
  protected final static int	META_DOWN  = 4; //InputEvent.META_MASK;
  protected final static int	ALT_DOWN   = 8; //InputEvent.ALT_MASK;

  //
  // Mouse status.
  //

  protected final int	AVAILABLE = 0;
  protected final int	DRAGGING  = 1;
  protected final int	MOVING    = 2;
  protected final int	RESIZEN   = 3;
  protected final int	RESIZENE  = 4;
  protected final int	RESIZEE   = 5;
  protected final int	RESIZESE  = 6;
  protected final int	RESIZES   = 7;
  protected final int	RESIZESW  = 8;
  protected final int	RESIZEW   = 9;
  protected final int	RESIZENW  = 10;
  protected final int	CHANGE    = 11;
  protected final int	ZOOMING   = 12;
  protected int	mouse = AVAILABLE;

  //
  // System Type.
  //

  protected final int	MAC  = 32;
  protected final int	WIN  = 64;
  protected final int	LNX  = 128;
  protected final int	NN   = 0;
  
  protected int SYSTEM;

  protected int movingID = 0;

  //
  // Permanent arrays for drawing polygon.
  //

  protected int	xcorner[] = {0, 0, 0, 0};
  protected int	ycorner[] = {0, 0, 0, 0};

  protected int diffX;
  protected int diffY;

  int border = 0;
  int xShift = 0;
  int yShift = 0;

  Vector Selections = new Vector(10,0);

  Selection activeS;

  protected int minX = 0;
  protected int minY = 0;
  protected int maxX = 10000;
  protected int maxY = 10000;
  protected int maxWidth = 10000;
  protected int maxHeight = 10000;

  ///////////////////////// World - Screen Coordinates ///////////////////////////////////

  protected double hllx, llx;
  protected double hlly, lly;

  protected double hurx, urx;
  protected double hury, ury;

  protected double aspectRatio;

  protected Vector zooms = new Vector(10,0);

  ///////////////////////// Methods for Coordinates /////////////////////////////////////

  public void setCoordinates(double llx, double lly, double urx, double ury, double aspectRatio) {

    this.hllx = llx;
    this.hlly = lly;
    this.hurx = urx;
    this.hury = ury;
    this.aspectRatio = aspectRatio;
    reScale(llx, lly, urx, ury);
  }

  public void setAspect(double aspectRatio) {
    this.aspectRatio = aspectRatio;
    updateScale();
  }

  public double getAspect() {
    return aspectRatio;
  }

  public void print() {
  }

  public void reScale(double llx, double lly, double urx, double ury) {

    this.llx = llx;
    this.lly = lly;
    this.urx = urx;
    this.ury = ury;

    zooms.addElement( new double[]{llx, lly, urx, ury});
    updateScale();
  }

  public void updateScale() {
    double mllx = ((double[])(zooms.elementAt( zooms.size()-1 )))[0];
    double mlly = ((double[])(zooms.elementAt( zooms.size()-1 )))[1];
    double murx = ((double[])(zooms.elementAt( zooms.size()-1 )))[2];
    double mury = ((double[])(zooms.elementAt( zooms.size()-1 )))[3];

    Dimension size = this.getSize();
    size.width  -= 2 * border;
    size.height -= 2 * border;
    if( size.width > 0 && size.height > 0 ) 
      if( aspectRatio > 0 )
        if( (double)size.width/(double)size.height < (murx-mllx)/(mury-mlly) ) {
          this.ury = ((double)size.height / (double)size.width) * ((murx-mllx) / (mury-mlly)) * (mury-mlly) + mlly;
          this.urx = murx;
        }
          else {
            this.ury = mury;
            this.urx = ((double)size.width / (double)size.height) * ((mury-mlly) / (murx-mllx)) * (murx-mllx) + mllx;
          }
          //System.out.println("Height: "+size.height+" Width: "+size.width+" llx: "+llx+" lly: "+lly+" urx: "+urx+" ury:"+ury+" #:"+zooms.size());  
  }

    public void home() {
      llx = this.hllx;
      lly = this.hlly;
      urx = this.hurx;
      ury = this.hury;
    }

    public double userToWorldX(double x) {

      Dimension size = this.getSize();
      size.width  -= 2 * border;
      //    return (x - llx) / (urx - llx) * size.width;
      return border + xShift + (x - llx) / (urx - llx) * size.width;
    }

    public double userToWorldY(double y) {

      Dimension size = this.getSize();
      size.height -= 2 * border;
      //    return size.height - (y - lly) / (ury - lly) * size.height;
      return border + yShift + size.height - (y - lly) / (ury - lly) * size.height;
    }

    public double worldToUserX(int x) {

      Dimension size = this.getSize();
      size.width  -= 2 * border;
      //    return llx + (urx - llx) * x / size.width;
      return llx + (urx - llx) * (x-border-xShift) / size.width;
    }

    public double worldToUserY(int y) {

      Dimension size = this.getSize();
      size.height -= 2 * border;
      //    return lly + (ury - lly) * (size.height - y) / size.height;
      return lly + (ury - lly) * (size.height - (y-border-yShift)) / size.height;
    }

    public double getLlx() {
      return llx;
    }

    public double getLly() {
      return lly;
    }

    public double getUrx() {
      return urx;
    }

    public double getUry() {
      return ury;
    }
    ///////////////////////////////////////////////////////////////////////////

    public
      DragBox(JFrame frame) {

        this.frame = frame;

        addMouseListener(this);
        addMouseMotionListener(this);

        evtq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        enableEvents(0);

        //    sb = new Scrollbar(Scrollbar.VERTICAL, 0, 300, 0, 300);
        sb = new JScrollBar(Scrollbar.VERTICAL, 0, 300, 0, 300);
        sb.addAdjustmentListener(this);
        frame.getContentPane().add(sb,"East");
        sb.setVisible(false);

        this.enableEvents(AWTEvent.KEY_EVENT_MASK);
        this.requestFocus();

        frame.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) {processKeyEvent(e);} });

        if( ((System.getProperty("os.name")).toLowerCase()).indexOf("mac") > -1 )
          SYSTEM = MAC;
        else if( ((System.getProperty("os.name")).toLowerCase()).indexOf("win") > -1 )
          SYSTEM = WIN;
        else if( ((System.getProperty("os.name")).toLowerCase()).indexOf("linux") > -1 )
          SYSTEM = LNX;
        else
          SYSTEM = NN;
      }

    public void setDragBoxConstraints(int minX, int minY, int maxX, int maxY, int maxWidth, int maxHeight) {
      this.minX      = minX;
      this.minY      = minY;
      this.maxX      = maxX;
      this.maxY      = maxY;
      this.maxWidth  = maxWidth;
      this.maxHeight = maxHeight;
    }    

    public Dimension getViewportSize() {

      Dimension size;

      size = frame.getSize();
      size.height -= (frame.getInsets().top+frame.getInsets().bottom);
      size.width  -= (frame.getInsets().right+frame.getInsets().left);    

      return size;
    }

    public void setSize(int widthN, int heightN) {

      Dimension size;    
      size = frame.getSize();
      size.height -= (frame.getInsets().top+frame.getInsets().bottom);
      size.width  -= (frame.getInsets().right+frame.getInsets().left);    

      if( heightN >  size.height) {
        super.setSize( size.width, size.height );
        sb.setValues(sb.getValue(),size.height,0,heightN);
        sb.setUnitIncrement(22);
        sb.setBlockIncrement((frame.getSize()).height);
        if( !sb.isVisible() ) {
          sb.setVisible(true);
          frame.setVisible(true);
        }
      }
      else {
        super.setSize( size.width, size.height );
        sb.setValues(0,size.height,0,size.height);
        sb.setVisible(false);
      }    
    }

    public void update(Graphics g) {
      paint(g);
    }

    public Color getHiliteColor() {
      return hiliteColor;
    }  

    public void addSelectionListener(SelectionListener l) {
      listener = l;
    }

    public void processEvent(AWTEvent evt) {
      if( evt instanceof SelectionEvent ) {
        if( listener != null )
          listener.updateSelection();
      }
      else super.processEvent(evt);
    }

    ///////////////////////////////////////////////////////////////////////////

    public void	mouseDragged(MouseEvent e) {

      if (mouse != CHANGE) {
        dragBox(e.getX(), e.getY(), e);
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    public void mousePressed(MouseEvent e) {

System.out.println("SYSTEM = "+SYSTEM);
System.out.println("mouse = "+mouse);
System.out.println("Mouse pres: ... "+e.getModifiers());

      if (mouse == AVAILABLE) {
        if (e.getModifiers() == BUTTON1_DOWN ||
            e.getModifiers() == BUTTON1_DOWN + SHIFT_DOWN )
          dragBegin(e.getX(), e.getY(), e);
        if (e.getModifiers() ==  BUTTON1_DOWN + META_DOWN && SYSTEM == MAC ||
            e.getModifiers() ==  BUTTON2_DOWN             && SYSTEM != MAC ) {
          mouse = ZOOMING;
          System.out.println("Start ZOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOMING");
          dragBegin(e.getX(), e.getY(), e);
        }	 
        if ((e.isPopupTrigger() ) && !e.isShiftDown() && e.getModifiers() !=24 ) {  // || (e.getModifiers() ==  BUTTON3_DOWN && SYSTEM == WIN)
          mouse = CHANGE;
          System.out.println("Start CHANGGEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
          dragBegin(e.getX(), e.getY(), e);
        }	 
      }   // End if // 
    }

    ///////////////////////////////////////////////////////////////////////////

    public void
      mouseReleased(MouseEvent e) {

        int ev;
        ev=e.getModifiers();

        if (e.isPopupTrigger() && SYSTEM == WIN && !e.isShiftDown() ) {
          mouse = CHANGE;
          System.out.println("Start CHANGGEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
          dragBegin(e.getX(), e.getY(), e);
        }

        System.out.println("Mouse rel: ... "+ev);
        if (mouse != AVAILABLE) {
          switch (ev) {
            case 0:
            case 1:
            case 2:
              //	            if((System.getProperty("os.name")).equals("Mac OS") && (mouse != CHANGE) ) {
              //		        dragEnd(e);
              //		    }
              mouse = AVAILABLE;
              break;
            case BUTTON1_UP:
            case BUTTON1_UP + CTRL_DOWN:
            case BUTTON1_UP + SHIFT_DOWN:
            case BUTTON1_UP + META_DOWN:
            case BUTTON2_UP:
            case BUTTON3_UP:
              if( mouse != CHANGE ) {
System.out.println(" dragEnd! ");
                dragEnd(e);
              }
              mouse = AVAILABLE;
              break;
          }	// End switch //
        }   // End if //
      }

    ///////////////////////////////////////////////////////////////////////////

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}

    ///////////////////////////////////////////////////////////////////////////

    public void dragboxCallback(int x0, int y0, int x1, int y1, MouseEvent e) {

      SelectionEvent se;
      Selection S;
      Rectangle sr;

      int lx = Math.min(x0,x1); 
      int ly = Math.min(y0,y1);
      int lw = Math.abs(x1-x0);
      int lh = Math.abs(y1-y0);

      sr = new Rectangle(lx, ly, lw, lh);

      int modifiers = e.getModifiers();
System.out.println("modifiers callback = "+e.getModifiers());
System.out.println("mouse "+mouse);

      if( modifiers == BUTTON1_UP ||
          modifiers == BUTTON1_UP + META_DOWN - 16 && SYSTEM == MAC ||
          modifiers == BUTTON2_UP && SYSTEM != MAC ) {
        if( mouse != CHANGE && mouse != ZOOMING ) {
          if( mouse == DRAGGING ) {
            S = new Selection(sr, null, 0, Selection.MODE_STANDARD, this);
            activeS = S;
            Selections.addElement(S);
          }   
          else {
            S = ((Selection)Selections.elementAt(movingID));
            S.r = sr;
          }
          selectFlag = true;
          se = new SelectionEvent(this);
          evtq.postEvent(se);
        } else if ( mouse == ZOOMING ) {
          if( Math.abs(x0-x1) < 5 && Math.abs(y0-y1) < 5 ) {
            if( zooms.size() > 1 ) {
              zooms.removeElementAt( zooms.size()-1 );
              reScale( ((double[])(zooms.elementAt( zooms.size()-1 )))[0],
                       ((double[])(zooms.elementAt( zooms.size()-1 )))[1],
                       ((double[])(zooms.elementAt( zooms.size()-1 )))[2],
                       ((double[])(zooms.elementAt( zooms.size()-1 )))[3] );
              zooms.removeElementAt( zooms.size()-1 );
            }
          }
          else
            reScale( worldToUserX( Math.min(x0, x1) ),
                     worldToUserY( Math.max(y0, y1) ),
                     worldToUserX( Math.max(x0, x1) ),
                     worldToUserY( Math.min(y0, y1) ));
          scaleChanged = true;
          update(this.getGraphics());
        }
      }
      if( modifiers == BUTTON1_UP + SHIFT_DOWN) {
        if( mouse == DRAGGING ) {
            S = new Selection(sr, null, 0, Selection.MODE_XOR, this);
            activeS = S;
            Selections.addElement(S);
          }
          else {
            S = ((Selection)Selections.elementAt(movingID));
            S.r = sr;
          }
          selectFlag = true;
          se = new SelectionEvent(this);
          evtq.postEvent(se);
      }
    }	

    ///////////////////////////////////////////////////////////////////////////

    public abstract void maintainSelection(Selection s);

    public abstract void updateSelection();

    ///////////////////////////////////////////////////////////////////////////

    public abstract void dataChanged(int id);

    public abstract void paint(Graphics g);

    ///////////////////////////////////////////////////////////////////////////

    public abstract void adjustmentValueChanged(AdjustmentEvent e);
    public abstract void scrollTo(int id);

    ///////////////////////////////////////////////////////////////////////////

    public void drawBoldDragBox(Graphics g, Selection S) {

      Rectangle r = S.r;

      g.setColor( new Color(255, 255, 255, 90));

      g.fillRect(r.x, r.y, r.width, r.height);

      g.setColor( new Color(255, 255, 255, 150));
      
      if( mouse != ZOOMING && mouse !=DRAGGING && mouse!= AVAILABLE)
        g.drawString(S.step+"", r.x+r.width/2-3, r.y+r.height/2+5);

      if( S == activeS )
        g.setColor( Color.black );
      else
        g.setColor( Color.gray );

//      g.drawRect(r.x, r.y, r.width, r.height);
//      g.drawRect(r.x-1, r.y-1, r.width+2, r.height+2);

      g.fillRect(r.x-3, r.y-3, 4, 4);
      g.fillRect(r.x+r.width/2-2, r.y-3, 4, 4);
      g.fillRect(r.x+r.width, r.y-3, 4, 4);

      g.fillRect(r.x-3, r.y+r.height/2-2, 4, 4);
      g.fillRect(r.x+r.width, r.y+r.height/2-2, 4, 4);

      g.fillRect(r.x-3, r.y+r.height, 4, 4);
      g.fillRect(r.x+r.width/2-2, r.y+r.height, 4, 4);
      g.fillRect(r.x+r.width, r.y+r.height, 4, 4);
    }

    public void
      setColor(Color c) {

        dragboxcolor = c;
      }

    ///////////////////////////////////////////////////////////////////////////

    protected void
      drawDragBox() {

        if (xcorner[0] != xcorner[2] || ycorner[0] != ycorner[2]) {
          int[] drawYCorner = new int[4];
          for( int i=0; i<4; i++ )
            drawYCorner[i] = ycorner[i] - sb.getValue();
          dragboxgraphics.drawPolygon(xcorner, drawYCorner, 4);
        }
      }

    ///////////////////////////////////////////////////////////////////////////

    protected void dragBegin(int x, int y, MouseEvent e) {

      boolean inBox = false;
      Rectangle sr=null;

      int modifiers = e.getModifiers();

      if (dragboxgraphics == null) {
        dragboxgraphics = getGraphics();
        dragboxgraphics.setColor(dragboxcolor);
        dragboxgraphics.setXORMode(getBackground());
      }   // End if //

System.out.println("Mouse Action before check: "+mouse);
      if( mouse != ZOOMING ) {
        mouse = DRAGGING;
        for( int i=0; i<Selections.size(); i++ ) {
          int locMouse = determineAction(((Selection)Selections.elementAt(i)).r, new Point(x, y+sb.getValue()));
          if(( locMouse <=10) && (locMouse >= 2 ))
            activeS = (Selection)Selections.elementAt(i);
          if( locMouse != DRAGGING && mouse != ZOOMING ) {
            movingID = i; 
            mouse = locMouse;
            sr = ((Selection)Selections.elementAt(movingID)).r;
            xcorner[0] = sr.x;
            xcorner[1] = xcorner[0];
            xcorner[2] = sr.x + sr.width;
            xcorner[3] = xcorner[2];
            ycorner[0] = sr.y;
            ycorner[1] = sr.y + sr.height;
            ycorner[2] = ycorner[1];
            ycorner[3] = ycorner[0];
          }
        }
      }
      else
        mouse = ZOOMING;

//System.out.println("Mouse Action: "+mouse);

      switch (mouse) {

        case DRAGGING:
        case ZOOMING:
          xcorner[0] = x;
          ycorner[0] = y + sb.getValue();
          xcorner[2] = xcorner[0];
          ycorner[2] = ycorner[0];
          System.out.println("Mouse Action: DRAGGING");
          break;
        case MOVING:
          if( modifiers == BUTTON1_DOWN ) {
            diffX = sr.x - x;
            diffY = sr.y - (y+sb.getValue());
          } 
          else if( e.isPopupTrigger() || (e.getModifiers() ==  BUTTON3_DOWN && SYSTEM == WIN) && !e.isShiftDown() ) { // modifiers == BUTTON1_DOWN + SHIFT_DOWN ) {
            mouse = CHANGE;
//System.out.println("Get/Change Info of Brush No: "+movingID);
            JPopupMenu changeSelection = new JPopupMenu();
            Selection S = ((Selection)Selections.elementAt(movingID));
            int selStep = S.step;
            JMenuItem Step = new JMenuItem("Step: "+selStep);
            changeSelection.add(Step);

            //	  PopupMenu mode = new PopupMenu(S.getModeString(S.mode));
            JMenu mode = new JMenu(S.getModeString(S.mode));
            JMenuItem modeM = new JMenuItem(S.getModeString(S.mode));	    

            if( selStep > 1) {
              if( S.mode != Selection.MODE_STANDARD ) {
                JMenuItem Replace = new JMenuItem(S.getModeString(Selection.MODE_STANDARD));
                mode.add(Replace);
                Replace.addActionListener(this);
                Replace.setActionCommand(S.getModeString(Selection.MODE_STANDARD));
              }
              if( S.mode != Selection.MODE_AND ) {
                JMenuItem And = new JMenuItem("And");
                mode.add(And);
                And.addActionListener(this);
                And.setActionCommand(S.getModeString(Selection.MODE_AND));
              }
              if( S.mode != Selection.MODE_OR ) {
                JMenuItem Or = new JMenuItem("Or");
                mode.add(Or);
                Or.addActionListener(this);
                Or.setActionCommand(S.getModeString(Selection.MODE_OR));
              }
              if( S.mode != Selection.MODE_XOR ) {
                JMenuItem XOr = new JMenuItem("Xor");
                mode.add(XOr);
                XOr.addActionListener(this);
                XOr.setActionCommand(S.getModeString(Selection.MODE_XOR));
              }
              if( S.mode != Selection.MODE_NOT ) {
                JMenuItem Not = new JMenuItem("Not");
                mode.add(Not);
                Not.addActionListener(this);
                Not.setActionCommand(S.getModeString(Selection.MODE_NOT));
              }
              changeSelection.add(mode);
            }
            else
              changeSelection.add(modeM);

            JMenuItem Delete = new JMenuItem("Delete");
            changeSelection.add(Delete);
            Delete.setAccelerator(KeyStroke.getKeyStroke(Event.BACK_SPACE, 0));

            Delete.setActionCommand("Delete");
            Delete.addActionListener(this);

            JMenuItem DeleteAll = new JMenuItem("Delete All");
            changeSelection.add(DeleteAll);
            DeleteAll.setAccelerator(KeyStroke.getKeyStroke(Event.BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            DeleteAll.setActionCommand("DeleteAll");
            DeleteAll.addActionListener(this);

            frame.getContentPane().add(changeSelection);
            changeSelection.show(e.getComponent(), e.getX(), e.getY());

            changePop = true;

            mouse = AVAILABLE;                            // We don't get a BOTTON_RELEASED Event on a popup
          }
          break;
      }
  }

    ///////////////////////////////////////////////////////////////////////////

    protected void dragBox(int x, int y, MouseEvent e) {

      if( (mouse != MOVING) && (Math.abs(xcorner[1] - x) > maxWidth) ) 
        x = xcorner[2];

      if( mouse == DRAGGING || mouse == ZOOMING )
        drawDragBox();

      if( y <= 10 ) {
        scrollTo( sb.getValue() - 22 );
      }
      if( y >= ((frame.getSize()).height - (frame.getInsets().top+frame.getInsets().bottom) - 10) ) {
        scrollTo( sb.getValue() + 22 );
      }	

      if( mouse != DRAGGING && mouse != ZOOMING ) {
/*        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();

        AWTEvent evt = null;
        AWTEvent pE = eq.peekEvent();

        while( (pE != null) && ((pE.getID() == MouseEvent.MOUSE_DRAGGED) ||
                                (pE.getID() == SelectionEvent.SELECTION_EVENT) ))
          try {
            evt = eq.getNextEvent();
            pE = eq.peekEvent();
//System.out.println("====> trashed Event!"+evt.toString());
          }
        catch( InterruptedException ex)
        {}  */

        switch( mouse ) {
          case MOVING:
            Rectangle sr = ((Selection)Selections.elementAt(movingID)).r;
            xcorner[0] = x                + diffX;
            xcorner[1] = xcorner[0];
            xcorner[2] = x + sr.width     + diffX;
            xcorner[3] = xcorner[2];
            ycorner[0] = y                + diffY + sb.getValue();
            ycorner[1] = y + sr.height    + diffY + sb.getValue();
            ycorner[2] = ycorner[1];
            ycorner[3] = ycorner[0];
            break;
          case RESIZENW:
            xcorner[0] = x;
            ycorner[0] = y + sb.getValue();
            xcorner[1] = x;
            ycorner[3] = y + sb.getValue();
            break;
          case RESIZEN:
            ycorner[0] = y + sb.getValue();
            ycorner[3] = y + sb.getValue();
            break;	
          case RESIZENE:
            xcorner[3] = x;
            ycorner[0] = y + sb.getValue();
            xcorner[2] = x;
            ycorner[3] = y + sb.getValue();
            break;
          case RESIZEE:
            xcorner[2] = x;
            xcorner[3] = x;
            break;	
          case RESIZESE:
            xcorner[2] = x;
            ycorner[2] = y + sb.getValue();
            xcorner[3] = x;
            ycorner[1] = y + sb.getValue();	
            break;
          case RESIZES:
            ycorner[1] = y + sb.getValue();
            ycorner[2] = y + sb.getValue();
            break;	
          case RESIZESW:
            xcorner[0] = x;
            ycorner[2] = y + sb.getValue();
            xcorner[1] = x;
            ycorner[1] = y + sb.getValue();	
            break;
          case RESIZEW:
            xcorner[0] = x;
            xcorner[1] = x;
            break;	
        }
        dragboxCallback(xcorner[0], ycorner[0], xcorner[2], ycorner[2], e);
      }
      else {
        xcorner[1] = xcorner[0];
        ycorner[1] = y + sb.getValue();
        xcorner[2] = x;
        ycorner[2] = ycorner[1];
        xcorner[3] = xcorner[2];
        ycorner[3] = ycorner[0];

        drawDragBox();
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    protected void
      dragEnd(MouseEvent e) {
        // MTh   drawDragBox();
        dragboxgraphics.dispose();
        dragboxgraphics = null;

        dragboxCallback(xcorner[0], ycorner[0], xcorner[2], ycorner[2], e);
      }

    ///////////////////////////////////////////////////////////////////////////

    public void processKeyEvent(KeyEvent e) {

      if ((e.getID() == KeyEvent.KEY_PRESSED) && 
          (e.getKeyCode() == KeyEvent.VK_P)   &&
          (e.isControlDown()) ) {

        printing = true;
        PrintUtilities.printComponent(this);
        printing = false;
      }
      if ((e.getID() == KeyEvent.KEY_PRESSED) && 
          (e.getKeyCode() == KeyEvent.VK_A)   &&
          (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) ) {

        selectAll = true;
        SelectionEvent se = new SelectionEvent(this);
        evtq.postEvent(se);
      }
      if ((e.getID() == KeyEvent.KEY_PRESSED) && 
          (e.getKeyCode() == Event.BACK_SPACE))  {
        if( Selections.size() > 0 ) {
          //  Selection S = (Selection)Selections.lastElement();
          int activeIndex = Selections.indexOf(activeS);

          Selections.removeElement(activeS);
          activeS.status = Selection.KILLED;
          if (activeIndex > 0) activeS = (Selection) Selections.elementAt(activeIndex-1);

          SelectionEvent se = new SelectionEvent(this);
          evtq.postEvent(se);
        }
      }
      if ((e.getID() == KeyEvent.KEY_PRESSED) && 
          (e.getKeyCode() == Event.BACK_SPACE) &&
          (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))  {
        System.out.println("Delete All Selections");
        deleteAll = true;
        SelectionEvent se = new SelectionEvent(this);
        evtq.postEvent(se);
      }
      if ((e.getID() == KeyEvent.KEY_PRESSED) && 
          (e.getKeyCode() == KeyEvent.VK_M) &&
          (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))  {
        System.out.println("Switch Selection Mode");
        switchSel = true;
        SelectionEvent se = new SelectionEvent(this);
        evtq.postEvent(se);
      }
      if (e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() && e.getKeyCode() == KeyEvent.VK_W ) {
        ((MFrame)frame).close();
      }
    }

    public void keyPressed(KeyEvent e) {      System.out.println("Key typed");
    }

    public void keyReleased(KeyEvent e) {      System.out.println("Key typed");
    }


    public int determineAction(Rectangle r, Point p) {

      int tolerance = 6;

      if( (new Rectangle(r.x, r.y, r.width, r.height)).contains(p) )
        return MOVING;

      if( ((new HotRect(r.x-3, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZENW;

      if( ((new HotRect(r.x+r.width/2-2, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZEN;

      if( ((new HotRect(r.x+r.width, r.y-3, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZENE;

      if( ((new HotRect(r.x-3, r.y+r.height/2-2, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZEW;

      if( ((new HotRect(r.x+r.width, r.y+r.height/2-2, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZEE;

      if( ((new HotRect(r.x-3, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZESW;

      if( ((new HotRect(r.x+r.width/2-2, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZES;

      if( ((new HotRect(r.x+r.width, r.y+r.height, 4, 4)).larger(tolerance)).contains(p) )
        return RESIZESE;

      return DRAGGING;
    }

    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();

      Selection S = ((Selection)Selections.elementAt(movingID));
      if( command.equals("Delete") ) {	// die Abfrage nach der aktivierten Selektion kann man sich sparen - es ist sowieso nur moeglich auf aktivierte Elemente zuzugreifen	
        Selections.removeElement(S);
        S.status = Selection.KILLED;

        if( Selections.size() > 1 )
          activeS = (Selection) Selections.elementAt(Selections.size()-1);	// solange nur aktivierte Element geloescht werden koennen, reicht es
                                                                           // anschliessend das vorletzte Element zu selektieren
      }	
      if( command.equals("DeleteAll") ) {
        deleteAll = true;
      }
      if( command.equals(S.getModeString(Selection.MODE_STANDARD)) )
        S.mode = Selection.MODE_STANDARD;
      if( command.equals(S.getModeString(Selection.MODE_AND)) )
        S.mode = Selection.MODE_AND;
      if( command.equals(S.getModeString(Selection.MODE_OR)) ) 
        S.mode = Selection.MODE_OR;
      if( command.equals(S.getModeString(Selection.MODE_XOR)) )
        S.mode = Selection.MODE_XOR;
      if( command.equals(S.getModeString(Selection.MODE_NOT)) )
        S.mode = Selection.MODE_NOT;

      SelectionEvent se = new SelectionEvent(this);
      evtq.postEvent(se);	
    }

    private SelectionListener listener;
    private static EventQueue evtq;

    class HotRect extends Rectangle {

      public HotRect(int x, int y, int w, int h) {
        super.x = x;
        super.y = y;
        super.width = w;
        super.height = h;
      }

      public Rectangle larger(int t) {
        return new Rectangle(this.x-t, this.y-t, this.width+2*t, this.height+2*t);
      }
    }
}

class SelectionEvent extends AWTEvent {
  public SelectionEvent(DragBox s) {
    super( s, SELECTION_EVENT );
  }
  public static final int SELECTION_EVENT = AWTEvent.RESERVED_ID_MAX + 1;
}
