import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.geom.*;         
import java.awt.image.*;         
import java.awt.event.*;         // 
import java.util.*;              // 
import java.util.Vector;         // 
import java.lang.*;              // 
import java.io.*;              // 
import javax.swing.*;
import javax.swing.event.*;
import org.rosuda.JRclient.*;	   // For Rserve
 
public class Scatter2D extends DragBox {
  private Vector rects = new Vector(512,512);    // Store the tiles.
  private int width, height;                   // The preferred size.
  protected int oldWidth, oldHeight;           // The last size for constructing the polygons.
  private int hiliteId = 0;
  private double xMin , xMax, yMin, yMax;         // Scalings for plot
  private int shiftx, shifty;
  private double scalex, scaley;
  private dataSet data;
  private JComboBox Varlist;
  private int displayVar = -1;
  private Image bi, tbi, ttbi, fi;			// four buffer: 1. double, 2. hilite, 3. labels, 4. filtered
  private MediaTracker media = new MediaTracker(this);
  private Graphics2D fg, bg, tbg, ttbg;
  private int[] Vars;
  private JList varList;
  private double[] xVal;
  private double[] yVal;
  private double[] byVal;
  private double[] coeffs;
  private double[] selCoeffs = {-10000, -10000, 0};
  private int radius = 3;			// radius of points
  private int[] alphas = {1, 2, 4, 8, 16, 32, 50, 68, 84, 92, 96, 98, 99};
  private int alphap = 6;
  private int alpha = alphas[alphap];			// transparency of points
  private String displayMode = "Free";
  private String modeString = "bins";
  private String smoothF = "none";
  private int smoother = 5;
  private boolean plotLines = false;
  private boolean plotLoess = false;
  private boolean connectLines = false;
  private int lastPointId = -1;
  private int byVar = -1;
  private int outside = 5;
  private int tick    = 5;
  private boolean info = false;
  private int roundX;
  private int roundY;
  private Table binning;
  private boolean force = false;
  private boolean invert = false;
  private boolean alphaChanged = false;
  private boolean smoothChanged = false;
  
  /** This constructor requires a Frame and a desired size */
  public Scatter2D(JFrame frame, int width, int height, dataSet data, int[] Vars, JList varList) {
    super(frame);
    this.data = data;
    this.width = width;
    this.height = height;
    border = 30;
    xShift = 15;
    this.varList = varList;
    this.Vars = Vars;

    //    this.setBackground(new Color(255, 255, 152));
    //this.setBackground(new Color(0, 0, 0));

    // the events we are interested in.
    this.enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    this.enableEvents(AWTEvent.ITEM_EVENT_MASK);
    this.requestFocus();

    create();

    roundX = (int)Math.max(0, 2 - Math.round((Math.log(xMax-xMin)/Math.log(10))));
    roundY = (int)Math.max(0, 2 - Math.round((Math.log(yMax-yMin)/Math.log(10))));
    
    frame.getContentPane().add(this);
  }

  public void maintainSelection(Selection S) {

    Rectangle sr = S.r;
    int mode = S.mode;

    if( sr.width < 4 && sr.height < 5 && S.status == Selection.KILLED) {
      // This is a oneClick selection -> we expand the rectangle
      if( modeString.equals("points") ) {
        sr.x -= radius/2+1;
        sr.y -= radius/2+1;
        sr.width = radius;
        sr.height = radius;
      }
    }

    S.o = new floatRect(worldToUserX(S.r.x),
			worldToUserY(S.r.y),
			worldToUserX(S.r.x + S.r.width),
			worldToUserY(S.r.y + S.r.height));

    if( modeString.equals("points") ) {
        for( int i = 0;i < data.n; i++) {
            if ( sr.contains((int)userToWorldX( xVal[i]), (int)userToWorldY( yVal[i] )) )
                data.setSelection(i,1,mode);
            else
                data.setSelection(i,0,mode);
        }
    } else {
        for( int i=0; i<rects.size(); i++ ) {
            MyRect r = (MyRect)rects.elementAt(i);
            if( r.intersects(sr) ) {
//                S.condition.addCondition("OR", binning.names[0]+" = '"+binning.lnames[0][i]+"'");
                if( binning.data.isDB )
                    binning.getSelection();
                else {
                    double sum=0, sumh=0;
                    for( int j=0; j<r.tileIds.size(); j++ ) {
                        int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
                        // System.out.println("Id: "+id+":"+i);
                        binning.setSelection(id,1,mode);
                        sumh += binning.getSelected(id)*binning.table[id];
                        sum  += binning.table[id];
                    }
                    r.setHilite( sumh/sum );
                }
            } else
                if( !binning.data.isDB )
                    for( int j=0; j<r.tileIds.size(); j++ ) {
                        int id = ((Integer)(r.tileIds.elementAt(j))).intValue();
                        binning.setSelection(id,0,mode);
                    }
        }
    }
  }

  public void updateSelection() {
    paint(this.getGraphics());
  }
  
  public void dataChanged(int var) {
  }

    public void processMouseEvent(MouseEvent e) {

      if( e.isPopupTrigger() )
        super.processMouseEvent(e);  // Pass other event types on.
      if( changePop ) {
        changePop = false;
        return;
      }

      if (e.getID() == MouseEvent.MOUSE_PRESSED ||
          e.getID() == MouseEvent.MOUSE_RELEASED ) {
        if (e.isPopupTrigger() || e.isPopupTrigger() && e.isShiftDown() ) {
          info = false;
          if( modeString.equals("points") ) {
            int minDist = 5000;
            int minId=0;
            int minCount=0;
            int maxOverplot = 25;
            int restPoints = 0;
            int minIds[] = new int[maxOverplot];

            for( int i=0; i<data.n; i++ ) {
              int dist = (int)Math.pow( Math.pow(userToWorldX( xVal[i] )-e.getX(), 2)
                                        + Math.pow(userToWorldY( yVal[i] )-e.getY(), 2), 0.5 );
              if( dist < minDist ) {
                minDist = dist;
                minIds[minCount=0] = i;
                restPoints = 0;
                minCount++;
              } else if( dist == minDist ) {
                if( minCount < maxOverplot )
                  minIds[minCount++] = i;
                else
                  restPoints++;
              }
            }
            if( minDist < 5 ) {
              //System.out.print("Count: "+minCount);
              int[] selectedIds = varList.getSelectedIndices();
              if( selectedIds.length == 0 )
                selectedIds = Vars;
              JPopupMenu infoPop = new JPopupMenu();
              for( int ids=0; ids<minCount; ids++ ) {
                //System.out.print(" Ids: "+minIds[ids]);
                JMenu selCase = new JMenu("Case #"+(ids+1));
                for( int sel=0; sel<selectedIds.length; sel++ ) {
                  JMenuItem x;
                  if( data.categorical(selectedIds[sel]) )
                    if( data.alpha(selectedIds[sel]) )
                      x = new JMenuItem(data.getName(selectedIds[sel])+": "
                                        +data.getLevelName(selectedIds[sel], (data.getNumbers(selectedIds[sel]))[minIds[ids]]));
                    else
                      x = new JMenuItem(data.getName(selectedIds[sel])+": "
                                        +data.getLevelName(selectedIds[sel], (data.getRawNumbers(selectedIds[sel]))[minIds[ids]]));
                  else
                    x = new JMenuItem(data.getName(selectedIds[sel])+": "
                                      +(data.getRawNumbers(selectedIds[sel]))[minIds[ids]]);
                  if( minCount == 1 )
                    infoPop.add(x);
                  else
                    selCase.add(x);
                }
                if( minCount > 1 )
                  infoPop.add(selCase);
              }
              if( restPoints > 0 )
                infoPop.add(new JMenuItem("and "+restPoints+" more ..."));
              if( minCount > 1 )
                infoPop.add(new JMenuItem("Dismiss"));
              info = true;
              infoPop.show(e.getComponent(), e.getX(), e.getY());
            }
          } else {
            for( int i = 0;i < rects.size(); i++) {
              MyRect r = (MyRect)rects.elementAt(i);
              if ( r.contains( e.getX(), e.getY() )) {
                System.out.println(">>>>>>>>> hit at : "+i);
                System.out.println("testing: "+i+"  "+e.getX()+"  "+e.getY()+"  "+r.x+"  "+r.y+"  "+r.w+"  "+r.h);
                info = true;
                r.pop(this, e.getX(), e.getY());
                r.draw(this.getGraphics());
              }
            }
          }
          if( !info ) {
            if( plotLines && Math.abs( (int)userToWorldY( worldToUserX(e.getX()) * coeffs[1] + coeffs[0]) - e.getY() ) < 4 ) {
              //System.out.println(data.getName(Vars[1])+" = "+data.getName(Vars[0])+" * "+coeffs[1]+" + "+coeffs[0]);
              JPopupMenu line = new JPopupMenu();
              JMenuItem formula = new JMenuItem(data.getName(Vars[1])+" = "+data.getName(Vars[0])+" * "+Stat.roundToString(coeffs[1], 4)+" + "+Stat.roundToString(coeffs[0], 4));
              line.add(formula);
              JMenuItem r2 = new JMenuItem("R^2: "+Stat.roundToString(100*coeffs[2], 1));
              line.add(r2);
              line.show(e.getComponent(), e.getX(), e.getY());
            } else if( plotLines && Math.abs( (int)userToWorldY( worldToUserX(e.getX()) * selCoeffs[1] + selCoeffs[0]) - e.getY() ) < 4 ) {
              JPopupMenu line = new JPopupMenu();
              JMenuItem formula = new JMenuItem(data.getName(Vars[1])+" = "+data.getName(Vars[0])+" * "+Stat.roundToString(selCoeffs[1], 4)+" + "+Stat.roundToString(selCoeffs[0], 4));
              line.add(formula);
              JMenuItem r2 = new JMenuItem("R^2: "+Stat.roundToString(100*selCoeffs[2], 1));
              line.add(r2);
              line.show(e.getComponent(), e.getX(), e.getY());
            } else {
              JPopupMenu mode = new JPopupMenu();
              if( displayMode.equals("Fixed") ) {
                JMenuItem free = new JMenuItem("free aspect ratio");
                mode.add(free);
                free.setActionCommand("Free");
                free.addActionListener(this);
              }
              else {
                JMenuItem fixed  = new JMenuItem("fixed aspect ratio");
                mode.add(fixed);
                fixed.setActionCommand("Fixed");
                fixed.addActionListener(this);
              }
              JMenuItem axes = new JMenuItem("flip axes");
              mode.add(axes);
              axes.setActionCommand("axes");
              axes.addActionListener(this);

              JMenu smoothers = new JMenu("smoothers");

              JCheckBoxMenuItem nosmooth = new JCheckBoxMenuItem("none");
              smoothers.add(nosmooth);
              nosmooth.setActionCommand("none");
              nosmooth.addActionListener(this);
              if( smoothF.equals("none") ) {
                nosmooth.setSelected(true);
                nosmooth.setEnabled(false);
              }
              JCheckBoxMenuItem lsline = new JCheckBoxMenuItem("ls-line");
              smoothers.add(lsline);
              lsline.setActionCommand("ls-line");
              lsline.addActionListener(this);
              if( smoothF.equals("ls-line") ) {
                lsline.setSelected(true);
                lsline.setEnabled(false);
              }
              JCheckBoxMenuItem loess = new JCheckBoxMenuItem("loess ("+Stat.round(3.75/smoother,2)+")");
              smoothers.add(loess);
              loess.setActionCommand("loess");
              loess.addActionListener(this);
              if( smoothF.equals("loess") ) {
                loess.setSelected(true);
                loess.setEnabled(false);
              }
              JCheckBoxMenuItem splines = new JCheckBoxMenuItem("splines ("+smoother+")");
              smoothers.add(splines);
              splines.setActionCommand("splines");
              splines.addActionListener(this);
              if( smoothF.equals("splines") ) {
                splines.setSelected(true);
                splines.setEnabled(false);
              }
              JCheckBoxMenuItem locfit = new JCheckBoxMenuItem("locfit ("+Stat.round(3.5/smoother,2)+")");
              smoothers.add(locfit);
              locfit.setActionCommand("locfit");
              locfit.addActionListener(this);
              if( smoothF.equals("locfit") ) {
                locfit.setSelected(true);
                locfit.setEnabled(false);
              }
              
              mode.add(smoothers);
              
              JMenu conlines = new JMenu("add lines by");
              JCheckBoxMenuItem off = new JCheckBoxMenuItem("no lines");
              conlines.add(off);
              off.setActionCommand("nobyvar");
              if( byVar < 0 )
                off.setSelected(true);                    
              off.addActionListener(this);                  
              for(int i=0; i<data.k; i++) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(data.getName(i));
                conlines.add(item);
                item.setActionCommand("byvar"+i);
                item.addActionListener(this);
                if( i == byVar )
                  item.setSelected(true);
              }
              mode.add(conlines);

              JMenu disMode = new JMenu("Mode");
              if( modeString.equals("bins") ) {
                JMenuItem points = new JMenuItem("force points");
                disMode.add(points);
                points.setActionCommand("points");
                points.addActionListener(this);
              }
              else {
                JMenuItem bins  = new JMenuItem("force bins");
                disMode.add(bins);
                bins.setActionCommand("bins");
                bins.addActionListener(this);
              }
              if( force ) {
                JMenuItem auto  = new JMenuItem("auto");
                disMode.add(auto);
                auto.setActionCommand("auto");
                auto.addActionListener(this);
              }
              mode.add(disMode);

              JMenuItem invert  = new JMenuItem("invert plot");
              mode.add(invert);
              invert.setActionCommand("invert");
              invert.addActionListener(this);

              mode.add(new JMenuItem("dismiss"));

              mode.show(e.getComponent(), e.getX(), e.getY());
            }
          }
        }		
        else
          super.processMouseEvent(e);  // Pass other event types on.
      }
      else
        super.processMouseEvent(e);  // Pass other event types on.
    }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if( command.equals("Fixed") || command.equals("Free") || command.equals("axes") || command.equals("invert") || command.equals("none") || command.equals("ls-line") || command.equals("loess") || command.equals("splines") || command.equals("locfit") || command.equals("nobyvar") || command.substring(0,Math.min(5, command.length())).equals("byvar") || command.equals("points") || command.equals("bins") || command.equals("auto") ) {
      if( command.equals("Fixed") || command.equals("Free")) {
        displayMode = command;
      } else if( command.equals("bins") || command.equals("points")) {
        modeString = command;
        force = true;
      } else if( command.equals("auto") ) {
        force = false;
      } else if( command.equals("invert") ) {
        if(invert)
          invert = false;
        else
          invert = true;
      } else if( command.equals("none") || command.equals("ls-line") || command.equals("loess") || command.equals("splines") || command.equals("locfit") ) {
        smoothF = command;
        smoothChanged = true;
      } else if( command.equals("nobyvar") ) {
        connectLines = false;
        byVar = -1;
      } else if( command.substring(0,Math.min(5, command.length())).equals("byvar") ) {
        connectLines = true;
System.out.println(" ........................ by var "+command.substring(5,command.length()));
        byVar = (int)Util.atod( command.substring(5,command.length()) );
      } else if( command.equals("axes") ) {
        int tmp = Vars[1];
        Vars[1] = Vars[0];
        Vars[0] = tmp;
	
        for( int i=0; i<Selections.size(); i++) {
          Selection S = (Selection)Selections.elementAt(i);
          Rectangle sr = S.r;
          double tmp1 = ((floatRect)S.o).x1;
          ((floatRect)S.o).x1 = ((floatRect)S.o).y2;
          ((floatRect)S.o).y2 = tmp1;
          tmp1 = ((floatRect)S.o).x2;
          ((floatRect)S.o).x2 = ((floatRect)S.o).y1;
          ((floatRect)S.o).y1 = tmp1;
        }
        rects.removeAllElements();
      }

      scaleChanged = true;
      create();
      Graphics g = this.getGraphics();
      paint(g);
      g.dispose();
    }
    else
      super.actionPerformed(e);
  }

  public void processMouseMotionEvent(MouseEvent e) {

      Graphics2D g = (Graphics2D)this.getGraphics();
      FontMetrics fm = bg.getFontMetrics();
      ttbg = (Graphics2D)ttbi.getGraphics();
      ttbg.drawImage(tbi, 0, 0, null);

      drawSelections(ttbg);

      if ((e.getID() == MouseEvent.MOUSE_MOVED)) {

          if( (e.getModifiers() == CTRL_DOWN) ) {

            frame.setCursor(Frame.CROSSHAIR_CURSOR);
              
              info = true;
              ttbg.setColor(MFrame.backgroundColor);

              // Draw x-Label for CRTL_DOWN event
              int egetX = e.getX();

              if( egetX < userToWorldX(getLlx()) )
                  egetX = (int)userToWorldX(getLlx());
              if( egetX > userToWorldX(getUrx()) )                                        
                  egetX = (int)userToWorldX(getUrx());
              
              double ratioX = (worldToUserX(egetX) - getLlx())/(getUrx()-getLlx());
              int  minWidth = fm.stringWidth(Stat.roundToString(getLlx(), roundX));
              int  maxWidth = fm.stringWidth(Stat.roundToString(getUrx(), roundX));

              if( egetX <= userToWorldX(getLlx()) + minWidth +4 )
                  ttbg.fillRect( (int)userToWorldX(getLlx()), (int)userToWorldY( getLly() ) + outside + tick + 1,
                                 minWidth +4,  fm.getMaxAscent() + fm.getMaxDescent());
              if( egetX >= userToWorldX(getUrx()) - maxWidth -4 )
                  ttbg.fillRect( (int)userToWorldX(getUrx()) - maxWidth -4, (int)userToWorldY( getLly() ) + outside + tick + 1,
                                 maxWidth +4,  fm.getMaxAscent() + fm.getMaxDescent());
              
              ttbg.setColor(Color.black);
              ttbg.drawLine( egetX , (int)userToWorldY( getLly() ) + outside, 
                             egetX , (int)userToWorldY( getLly() ) + outside + tick );  
              ttbg.drawString(Stat.roundToString(worldToUserX(egetX), roundX),
                              egetX - fm.stringWidth(Stat.roundToString(worldToUserX(egetX), roundX)) / 2
                                    - (int)(fm.stringWidth(Stat.roundToString(worldToUserX(egetX), roundX)) *
                                      (ratioX - 0.5)),
                           (int)userToWorldY( getLly() ) + outside + tick + fm.getMaxAscent() + fm.getMaxDescent() );

              // Draw y-Label for CRTL_DOWN event
              int egetY = e.getY();
              // Attention: Y-axis is head to toe!
              if( egetY < userToWorldY(getUry()) )
                  egetY = (int)userToWorldY(getUry());
              if( egetY > userToWorldY(getLly()) )                                        
                  egetY = (int)userToWorldY(getLly());
              
              double ratioY = (worldToUserY(egetY) - getLly())/(getUry()-getLly());
              minWidth = fm.stringWidth(Stat.roundToString(getLly(), roundY));
              maxWidth = fm.stringWidth(Stat.roundToString(getUry(), roundY));

              ttbg.setColor(MFrame.backgroundColor);
              if( egetY < userToWorldY(getUry()) + minWidth + 4 )
                  ttbg.fillRect( 0, (int)userToWorldY(getUry()),
                                 (int)userToWorldX( getLlx() ) - outside - tick, minWidth + 4 );
              if( egetY > userToWorldY(getLly()) - maxWidth - 4 )
                  ttbg.fillRect( 0, (int)userToWorldY(getLly()) -maxWidth - 4,
                                 (int)userToWorldX( getLlx() ) - outside - tick, maxWidth + 4 );

              // Fadenkreuz
              ttbg.setColor(Color.lightGray);
              ttbg.drawLine( egetX - outside, egetY,
                             (int)userToWorldX( getLlx() ) , egetY );  
              ttbg.drawLine( egetX, egetY + outside,
                             egetX, (int)userToWorldY( getLly() ) );
              
              ttbg.setColor(Color.black);
              ttbg.drawLine( (int)userToWorldX( getLlx() ) - outside, egetY,
                             (int)userToWorldX( getLlx() ) - outside - tick, egetY );  
              ttbg.rotate(-Math.PI/2);
              ttbg.drawString(Stat.roundToString(worldToUserY(egetY), roundY), 
                            (int)(-egetY - ratioY * fm.stringWidth(Stat.roundToString(worldToUserY(egetY), roundY))),
                            (int)userToWorldY( getUry() ) - fm.getMaxAscent() - tick +1);          
              ttbg.rotate(Math.PI/2);
              g.drawImage(ttbi, 0, 0, Color.black, null);
              ttbg.dispose();
          }
          else {
              if( info ) {
                  frame.setCursor(Frame.DEFAULT_CURSOR);
                  paint( this.getGraphics() );
                  info = false;
              }
          }
      }
      super.processMouseMotionEvent(e);  // Pass other event types on.
  }

  public void processKeyEvent(KeyEvent e) {

      if (e.getID() == KeyEvent.KEY_PRESSED && (e.getKeyCode() == KeyEvent.VK_UP
                                            ||  e.getKeyCode() == KeyEvent.VK_DOWN
                                            ||  e.getKeyCode() == KeyEvent.VK_UP && e.isShiftDown()
                                            ||  e.getKeyCode() == KeyEvent.VK_DOWN && e.isShiftDown()
                                            ||  e.getKeyCode() == KeyEvent.VK_LEFT
                                            ||  e.getKeyCode() == KeyEvent.VK_RIGHT) ) {
          if( e.getKeyCode() == KeyEvent.VK_DOWN && !e.isShiftDown() ) {
              if( radius > 1 ) {
                  radius-=2;
                  scaleChanged = true;
              } else
                  return;
          }
          if( e.getKeyCode() == KeyEvent.VK_UP && !e.isShiftDown() ) {
              if( radius < width/2 ) {
                  radius+=2;
                  scaleChanged = true;
              }	
          }
          if( e.getKeyCode() == KeyEvent.VK_LEFT ) {
              if( alphap > 0 ) {
                alpha = alphas[--alphap];
                alphaChanged = true;
              } else
                  return;
          }
          if( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
            if( alphap < alphas.length-1 ) {
              alpha = alphas[++alphap];
              alphaChanged = true;
            } else
              return;
          }
          if( e.getKeyCode() == KeyEvent.VK_UP && e.isShiftDown() ) {
            if( smoother < 30 ) {
              smoother += 1;
              smoothChanged = true;
            }
          }
          if( e.getKeyCode() == KeyEvent.VK_DOWN && e.isShiftDown() ) {
            if( smoother > 1 ) {
              smoother -= 1;
            smoothChanged = true;
          }
          }
        paint( this.getGraphics() );
      }
      super.processKeyEvent(e);  // Pass other event types on.
  }
  
  public void paint(Graphics2D g) {

    frame.setCursor(Frame.DEFAULT_CURSOR);

    int pF = 1;
    if( printing )
      pF = printFactor;      

    if( displayMode.equals("Fixed") )
      setAspect(1);
    else
      setAspect(-1);

    Dimension size = this.getSize();

    if( oldWidth != size.width || oldHeight != size.height || scaleChanged || frame.getBackground() != MFrame.backgroundColor) {
      frame.setBackground(MFrame.backgroundColor);
      this.width = size.width;
      this.height = size.height;

      // dispose old background image after size changed
      if( bg != null ) {
        System.out.println("Dispose BG");
        bg.dispose();
        bg = null;
      }
      updateScale();
      int num=0;
      for( int i=0; i<data.n; i++ )           // Check how many points we need to render !
        if( xVal[i]>=getLlx() && xVal[i]<getUrx() && yVal[i]>=getLly() && yVal[i]<getUry() )
          num++;
      if( !force )
        if (num > data.graphicsPerf) {
          modeString="bins";
        } else
          modeString="points";

//      size = this.getSize();
      for( int i=0; i<Selections.size(); i++) {
        Selection S = (Selection)Selections.elementAt(i);
        S.r.x      = (int)userToWorldX( ((floatRect)S.o).x1 );
        S.r.y      = (int)userToWorldY( ((floatRect)S.o).y1 );
        S.r.width  = (int)userToWorldX( ((floatRect)S.o).x2 ) - (int)userToWorldX( ((floatRect)S.o).x1 );
        S.r.height = (int)userToWorldY( ((floatRect)S.o).y2 ) - (int)userToWorldY( ((floatRect)S.o).y1 );
      }
      
      oldWidth = size.width;
      oldHeight = size.height;
      scaleChanged = false;
    }

    long start = new Date().getTime();
    
    if( bg == null || alphaChanged || printing ) {

      if( printing ) {
//        System.out.println("Setting Graphics for Printing");
        bg = g;
        tbg = g;
      }
      else {
        bi = createImage(size.width, size.height);	
        fi = createImage(size.width, size.height);	
        tbi = createImage(size.width, size.height);	
        ttbi = createImage(size.width, size.height);	
        fg = (Graphics2D)fi.getGraphics();
        bg = (Graphics2D)bi.getGraphics();
        tbg = (Graphics2D)tbi.getGraphics();
        ttbg = (Graphics2D)ttbi.getGraphics();
      }
      FontMetrics fm = bg.getFontMetrics();      

      border = 30*pF;
      xShift = 0;
      yShift = 0;

      if( !alphaChanged )
        create();
      else
        alphaChanged = false;

      bg.setColor(Color.black);
      if(invert) {
//        Properties p = new Properties(System.getProperties());
//        p.setProperty("com.apple.macosx.AntiAliasedGraphicsOn", "false");
//        System.setProperties(p);
        
        fg.setColor(Color.gray);
        fg.fillRect(0,0,size.width, size.height);
        fg.setColor(Color.white);
      }
      Graphics2D pg;
      Image ti;
      if( modeString.equals("points") ) {
        if( invert )
          pg = fg;
        else
          pg = bg;
        pg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)alpha/100)));
        
        for( int i=0; i<data.n; i++) 
          if( xVal[i]>=getLlx() && xVal[i]<=getUrx() && yVal[i]>=getLly() && yVal[i]<=getUry() )
            pg.fillOval( (int)userToWorldX( xVal[i] )-(radius*pF-1)/2, (int)userToWorldY( yVal[i] )-(radius*pF-1)/2, radius*pF, radius*pF);

        if( invert ) {
          media.addImage(bi,0);
          try {
            media.waitForID(0);
            ti = Util.makeColorTransparent(fi, new Color(0).gray);
            bg.drawImage(ti, 0, 0, Color.black, null);
          }
          catch(InterruptedException e) {}
          pg.dispose();
        }
      } else {
        for( int i=0; i<rects.size(); i++ ) {
          MyRect r = (MyRect)rects.elementAt(i);
//          bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
          if( invert ) {
            bg.setColor(Color.black);
            bg.fillRect(r.x, r.y, r.w, r.h);
            r.setColor(Color.white);
          } else {
            bg.setColor(Color.white);
            bg.fillRect(r.x, r.y, r.w, r.h);
            r.setColor(Color.black);
          }
          bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)Math.min(1, r.obs/100*alpha))));
          r.draw(bg);
          r.setColor(Color.black);
        }
      }
      bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      bg.setColor(Color.black);
      
      // x axis
      bg.drawLine( (int)userToWorldX( getLlx() ), (int)userToWorldY( getLly() ) + outside*pF, 
                   (int)userToWorldX( getUrx() ), (int)userToWorldY( getLly() ) + outside*pF );  
      // x-ticks  
      bg.drawLine( (int)userToWorldX( getLlx() ), (int)userToWorldY( getLly() ) + outside*pF, 
                   (int)userToWorldX( getLlx() ), (int)userToWorldY( getLly() ) + outside*pF + tick*pF );  
        
      bg.drawLine( (int)userToWorldX( getUrx() ), (int)userToWorldY( getLly() ) + outside*pF, 
                   (int)userToWorldX( getUrx() ), (int)userToWorldY( getLly() ) + outside*pF + tick*pF );  

      bg.drawString(Stat.roundToString(getLlx(), roundX), 
                    (int)userToWorldX( getLlx() ), 
                    (int)userToWorldY( getLly() ) + outside*pF + tick*pF + fm.getMaxAscent() + fm.getMaxDescent() );
      
      bg.drawString(Stat.roundToString(getUrx(), roundX), 
                    (int)userToWorldX( getUrx() ) - fm.stringWidth(Stat.roundToString(getUrx(), roundX)), 
                    (int)userToWorldY( getLly() ) + outside*pF + tick*pF + fm.getMaxAscent() + fm.getMaxDescent() );
      
      // y-axis  
      bg.drawLine( (int)userToWorldX( getLlx() ) - outside*pF, (int)userToWorldY( getLly() ), 
                   (int)userToWorldX( getLlx() ) - outside*pF, (int)userToWorldY( getUry() ) );  
      // y-ticks  
      bg.drawLine( (int)userToWorldX( getLlx() ) - outside*pF,        (int)userToWorldY( getLly() ), 
                   (int)userToWorldX( getLlx() ) - outside*pF - tick*pF, (int)userToWorldY( getLly() ) );  

      bg.drawLine( (int)userToWorldX( getLlx() ) - outside*pF,        (int)userToWorldY( getUry() ), 
                   (int)userToWorldX( getLlx() ) - outside*pF - tick*pF, (int)userToWorldY( getUry() ) );  
        
      bg.rotate(-Math.PI/2);
      bg.drawString(Stat.roundToString(getLly(), roundY), 
                    -(int)userToWorldY( getLly() ), 
                    (int)userToWorldY( getUry() ) - fm.getMaxAscent() - tick*pF +1*pF);
      bg.drawString(Stat.roundToString(getUry(), roundY), 
                    -(int)userToWorldY( getUry() ) - fm.stringWidth(Stat.roundToString(getUry(), roundY) ),
                    (int)userToWorldY( getUry() ) - fm.getMaxAscent() - tick*pF +1*pF);          
      bg.rotate(Math.PI/2);
    } // end, new background graphics	

    if( !printing )
      tbg.drawImage(bi, 0, 0, Color.black, null);

    tbg.setColor(DragBox.hiliteColor);
    
    if( modeString.equals("points") ) {

      double[] selection = data.getSelection();

      // add lines by third variable
      if( connectLines ) {

        tbg.setColor(DragBox.hiliteColor);
        byVal = data.getRawNumbers(byVar);
        for( int i=1; i<data.n; i++) {
          if( selection[i] > 0 ) {
            int j=i-1;
            while( j > 0 && (byVal[i] != byVal[j] || selection[j] == 0) )
              j--;
            if( byVal[i] == byVal[j] && selection[j] > 0 )
              tbg.drawLine( (int)userToWorldX( xVal[j] ),
                            (int)userToWorldY( yVal[j] ),
                            (int)userToWorldX( xVal[i] ),
                            (int)userToWorldY( yVal[i] ) );
          }
        }
      }
      
/*      for( int i=0; i<data.n; i++) {
        if( xVal[i]>=getLlx() && xVal[i]<=getUrx() && yVal[i]>=getLly() && yVal[i]<=getUry() )
          if( selection[i] > 0 ) {
            tbg.setColor(MFrame.backgroundColor);
            tbg.fillOval( (int)userToWorldX( xVal[i] )-(radius-1)/2, (int)userToWorldY( yVal[i] )-(radius-1)/2, radius*pF, radius*pF);
          }
      }*/
      tbg.setColor(DragBox.hiliteColor);
      if( ((MFrame)frame).getAlphaHi() )
        tbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)Math.pow((float)alpha/100,0.75))));
      for( int i=0; i<data.n; i++) {
        if( xVal[i]>=getLlx() && xVal[i]<=getUrx() && yVal[i]>=getLly() && yVal[i]<=getUry() )
          if( selection[i] > 0 ) {
            tbg.fillOval( (int)userToWorldX( xVal[i] )-(radius-1)/2, (int)userToWorldY( yVal[i] )-(radius-1)/2, radius*pF, radius*pF);
          }
      }
    } else {
      for( int i=0; i<rects.size(); i++ ) {
        MyRect r = (MyRect)rects.elementAt(i);
        bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)Math.min(1, r.obs/100*alpha))));
        int id = ((Integer)(r.tileIds.elementAt(0))).intValue();
        double weight;
        if( (weight = binning.getSelected(id)) > 0 ) {
          r.setHilite( weight );
          r.draw(tbg);
        }
      }
    }
            
    if( !printing )
      ttbg.drawImage(tbi, 0, 0, Color.black, null);

    if( smoothChanged || true ) { // add regression lines
      smoothChanged = false;
      if( smoothF.equals("ls-line" ) ) {
        coeffs = data.regress(Vars[0], Vars[1]);
        ttbg.drawLine( (int)userToWorldX( xMin ), (int)userToWorldY( xMin * coeffs[1] + coeffs[0]),
                     (int)userToWorldX( xMax ), (int)userToWorldY( xMax * coeffs[1] + coeffs[0]) );
      }

      if( smoothF.equals("loess") || smoothF.equals("splines") || smoothF.equals("locfit") ) {
        try {
          Rconnection c = new Rconnection();
          if( smoothF.equals("splines") )
            c.voidEval("library(splines)");
          if( smoothF.equals("locfit") )
            c.voidEval("library(locfit)" );

          c.assign("x",data.getRawNumbers(Vars[0]));
          c.assign("y",data.getRawNumbers(Vars[1]));

          double[] xForFit = new double[200+1];
          double step = (xMax-xMin)/200;
          for( int f=0; f<200+1; f++ )
            xForFit[f] = xMin + step*(double)f;
          c.assign("xf",xForFit);

          double[] fitted = {0};
          double[] CIl = {0};
          double[] CIu = {0};
          if( smoothF.equals("loess") ) 
            fitted = c.eval("predict(loess(y~x, span=3.75/"+smoother+"), data.frame(x=xf))").asDoubleArray();
          if( smoothF.equals("locfit") ) {
            RList sL = c.eval("sL <- preplot(locfit.raw(x, y, alpha=3.5/"+smoother+"), xf, band=\"global\")").asList();
            fitted = (double[]) sL.at("fit").getContent();
            CIl    = new double[fitted.length];
            CIu    = new double[fitted.length];
            double[] se = (double[]) sL.at("se.fit").getContent();
            for( int f=0; f<=200; f++ ) {
              CIl[f] = fitted[f] - se[f];
              CIu[f] = fitted[f] + se[f];
            }
          }
//            fitted = c.eval("predict(locfit(y~x), data.frame(x=xf))").asDoubleArray();
          if( smoothF.equals("splines") ) {
            c.voidEval("sP <- predict(lm(y~ns(x,"+smoother+")), interval=\"confidence\", data.frame(x=xf))");
            fitted = c.eval("sP[,1]").asDoubleArray();
            CIl = c.eval("sP[,2]").asDoubleArray();
            CIu = c.eval("sP[,3]").asDoubleArray();
          }
          if( smoothF.equals("splines") || smoothF.equals("locfit") ) {
            Polygon CI = new Polygon();
            for( int f=0; f<200+1; f++ ) {
              CI.addPoint( (int)userToWorldX( xMin+step*(double)f ), (int)userToWorldY( CIl[f] ) );
            }
            for( int f=200; f>=0; f-- ) {
              CI.addPoint( (int)userToWorldX( xMin+step*(double)f ), (int)userToWorldY( CIu[f] ) );
            }
            ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)0.25)));
            ttbg.fillPolygon(CI);
            ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)1.0)));
          }

          for( int f=0; f<200; f++ ) {
            ttbg.drawLine( (int)userToWorldX( xMin+step*(double)f ),     (int)userToWorldY( fitted[f] ),
                           (int)userToWorldX( xMin+step*(double)(f+1) ), (int)userToWorldY( fitted[f+1] ));
          }

          c.close();
        } catch(RSrvException rse) {System.out.println("Rserve exception: "+rse.getMessage());}
      }
      int nSel = data.countSelection();
      if( nSel > 1 ) {
        ttbg.setColor(DragBox.hiliteColor);
        if( smoothF.equals("ls-line" ) ) {
          selCoeffs = data.selRegress(Vars[0], Vars[1]);
          ttbg.drawLine( (int)userToWorldX( xMin ), (int)userToWorldY( xMin * selCoeffs[1] + selCoeffs[0]),
                        (int)userToWorldX( xMax ), (int)userToWorldY( xMax * selCoeffs[1] + selCoeffs[0]) );
        }
        if( smoothF.equals("loess") || smoothF.equals("splines") || smoothF.equals("locfit") ) {
          try {
            Rconnection c = new Rconnection();
            if( smoothF.equals("splines") )
              c.voidEval("library(splines)");
            if( smoothF.equals("locfit") )
              c.voidEval("library(locfit)" );

            double[] selX = new double[nSel];
            double[] selY = new double[nSel];
            double[] selection = data.getSelection();
            int k=0;
            for( int i=0; i<data.n; i++ )
              if( selection[i] > 0 ) {
                selX[k]   = xVal[i];
                selY[k++] = yVal[i];
              }
                c.assign("x",selX);
            c.assign("y",selY);

            double xSelMin = data.getSelQuantile(Vars[0], 0.0);
            double xSelMax = data.getSelQuantile(Vars[0], 1.0);

            double[] xForFit = new double[200+1];
            double step = (xSelMax-xSelMin)/200;
            for( int f=0; f<200+1; f++ )
              xForFit[f] = xSelMin + step*(double)f;
            c.assign("xf",xForFit);

            double[] fitted = {0};
            double[] CIl = {0};
            double[] CIu = {0};
            if( smoothF.equals("loess") ) 
              fitted = c.eval("predict(loess(y~x, span=3.75/"+smoother+"), data.frame(x=xf))").asDoubleArray();
            if( smoothF.equals("locfit") ) {
              RList sL = c.eval("sL <- preplot(locfit.raw(x, y, alpha=3.5/"+smoother+"), xf, band=\"global\")").asList();
              fitted = (double[]) sL.at("fit").getContent();
              CIl    = new double[fitted.length];
              CIu    = new double[fitted.length];
              double[] se = (double[]) sL.at("se.fit").getContent();
              for( int f=0; f<=200; f++ ) {
                CIl[f] = fitted[f] - se[f];
                CIu[f] = fitted[f] + se[f];
              }
            }
//						fitted = c.eval("predict(locfit(y~x), data.frame(x=xf))").asDoubleArray();
            if( smoothF.equals("splines") ) {
              c.voidEval("sP <- predict(lm(y~ns(x,"+smoother+")), interval=\"confidence\", data.frame(x=xf))");
              fitted = c.eval("sP[,1]").asDoubleArray();
              CIl = c.eval("sP[,2]").asDoubleArray();
              CIu = c.eval("sP[,3]").asDoubleArray();
            }
            if( smoothF.equals("splines") || smoothF.equals("locfit") ) {
              Polygon CI = new Polygon();
              for( int f=0; f<=200; f++ ) {
                CI.addPoint( (int)userToWorldX( xSelMin+step*(double)f ), (int)userToWorldY( CIl[f] ) );
              }
              for( int f=200; f>=0; f-- ) {
                CI.addPoint( (int)userToWorldX( xSelMin+step*(double)f ), (int)userToWorldY( CIu[f] ) );
              }
              ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)0.25)));
              ttbg.fillPolygon(CI);
              ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)1.0)));
            }

            for( int f=0; f<200; f++ )
              ttbg.drawLine( (int)userToWorldX( xSelMin+step*(double)f ),     (int)userToWorldY( fitted[f] ),
                            (int)userToWorldX( xSelMin+step*(double)(f+1) ), (int)userToWorldY( fitted[f+1] ));

            c.close();
          } catch(RSrvException rse) {System.out.println("Rserve exception: "+rse.getMessage());}
        }
      }
    }

    ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
    ttbg.setColor(Color.black);
    if( !printing ) {
      ttbg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));      
      drawSelections(ttbg);
      g.setColor(Color.black);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F));
      g.drawImage(ttbi, 0, 0, Color.black, null);
    }

    long stop = new Date().getTime();
    //System.out.println("Time for points: "+(stop-start)+"ms");
  }
    
  public void drawSelections(Graphics g) {

    for( int i=0; i<Selections.size(); i++) {
      Selection S = (Selection)Selections.elementAt(i);
      drawBoldDragBox(g, S);
    }
  }

  public void adjustmentValueChanged(AdjustmentEvent e) {
  }

  public void scrollTo(int id) {
  }
  
  class floatRect {
    
    double x1, y1, x2, y2;
    
    public floatRect(double x1, double y1, double x2, double y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
    }
  }

  public void create() {
    
      if( rects.size() == 0 ) {
          xMin=data.getMin(Vars[0]); 
          xMax=data.getMax(Vars[0]); 
          yMin=data.getMin(Vars[1]); 
          yMax=data.getMax(Vars[1]);

          if( data.n < 100 )
              alpha = 100;
          else if ( data.n < 500 )
              alpha = 70;
          else if ( data.n < 2000 )
              alpha = 50;
          else
              alpha = 30;
    
          setCoordinates(xMin, yMin, xMax, yMax, -1);

          xVal = data.getRawNumbers(Vars[0]);
          yVal = data.getRawNumbers(Vars[1]);

          frame.setTitle("Scatterplot(x: "+data.getName(Vars[0])+" y: "+data.getName(Vars[1])+")");
          //(15-radius)*15
          binning = data.discretize2D("Dummy", Vars[0], getLlx(), getUrx()+0.01*(getUrx()-getLlx()), width/radius,
                                               Vars[1], getLly(), getUry()+0.01*(getUry()-getLly()), width/radius);
      } else {
        binning.update2DBins(getLlx(), getUrx()+0.01*(getUrx()-getLlx()), (int)(width/radius),
                             getLly(), getUry()+0.01*(getUry()-getLly()), (int)(width/radius));
      }
      
    //System.out.println(getLlx()+ " - " +getUrx()+ " - " + getLly()+ " - " +getUry());
    //binning.print();

    rects.removeAllElements();
    int X = (int)userToWorldX(getLlx());
    int nextX, Y;
    for( int i=0; i<binning.levels[0]; i++) {
        nextX = (int)userToWorldX(getLlx()+(i+1)*(getUrx()-getLlx())/(width/radius));
        int lastY = (int)userToWorldY(getLly());
        for( int j=0; j<binning.levels[1]; j++) {
            int index = i*(binning.levels[1])+j;
            Vector tileIds = new Vector(1,0);
            tileIds.addElement(new Integer(index));
            Y = (int)userToWorldY(getLly()+(j+1)*(getUry()-getLly())/(width/radius));
            if( binning.table[index] > 0 )
                rects.addElement(new MyRect( true, 'f', "Observed", X, Y, nextX - X, lastY - Y, binning.table[index],
                                             binning.table[index], 1.0, 0.0, binning.names[0]+": "+binning.lnames[0][i]+"\n"+binning.names[1]+": "+binning.lnames[1][j]+'\n', tileIds));
            lastY = Y;
        }
        X = nextX;
    }
//    System.out.println(" Llx: "+getLlx()+" Lly: "+getLly());
//    System.out.println(" Num Bins: "+(binning.levels[0]*binning.levels[1])+" Num Tiles: "+rects.size());
  }
}
