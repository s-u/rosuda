import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.image.*;         
import java.awt.event.*;         // New event model.
import java.util.*;              // For StingTokenizer.
import java.util.Vector;         // To store the scribble in.
import java.lang.*;              // 
import javax.swing.*;
import javax.swing.event.*;

public class PC extends DragBox implements ActionListener {
  protected int width, height;                   // The preferred size.
  protected int oldWidth, oldHeight;             // The last size for constructing the polygons.
  private Image bi, tbi;
  private Graphics bg;
  protected int[] vars;
  protected int xVar;
  protected int yVar;
  protected int k;
  protected double slotWidth;
  protected int addBorder = 0;
  protected dataSet data;
  protected double[] dMins, dIQRs, dMedians, dMeans, dSDevs, dMaxs;
  protected double[] Mins, Maxs;
  protected double[][] dataCopy;
  protected String[] lNames;
  protected int[] permA;
  protected double[] sortA;
  Polygon[] poly;
  String Scale = "Common";
  String paintMode = "Box";
  float alpha = 1F;
  int movingID;
  boolean moving;
  MyText movingName;
  private int lastX;
  Vector names = new Vector(10,10);
  Vector bPlots = new Vector(10,10);
  Vector rects = new Vector(30,10);
  Vector tabs = new Vector(10,10);

  public PC(JFrame frame, dataSet data, int[] vars, String mode) {
    super(frame);
    Dimension size = frame.getSize();
    this.width = size.width;
    this.height = size.height;
    oldWidth = size.width;
    oldHeight = size.height;
    this.vars = vars;
    this.data = data;
    this.k = vars.length;
    this.paintMode = mode;

    if( k == 1 )
      addBorder = 30;

    border = 20;

    if( k == 2 ) {
      if( data.categorical(vars[0]) && !data.categorical(vars[1]) ) {
        paintMode = "XbyY";
        xVar = vars[1];
        yVar = vars[0];
      }
      if( data.categorical(vars[1]) && !data.categorical(vars[0])) {
        paintMode = "XbyY";
        xVar = vars[0];
        yVar = vars[1];
      }
    }
    //this.setDoubleBuffered(true);

    frame.getContentPane().add(this);

    Font SF = new Font("SansSerif", Font.BOLD, 12);
    frame.setFont(SF);

    getData();

    this.setBackground(frame.getBackground());

    setCoordinates(0,1,0,1,1);

    if( paintMode.equals("XbyY") )
      frame.setTitle("PC("+data.getName(xVar)+"|"+data.getName(yVar)+")");
    else if( paintMode.equals("Poly") )
      frame.setTitle("PC("+data.setName+")");
    else
      frame.setTitle("PB("+data.setName+")");

    // which events we are interested in.
    this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    this.enableEvents(AWTEvent.KEY_EVENT_MASK);
    this.requestFocus();
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
      if ( e.isPopupTrigger() && e.getModifiers() != BUTTON1_DOWN + ALT_DOWN ) {
        super.processMouseEvent(e);
        int minXDist = 5000;
        int minYDist = 5000;
        int popXId = 0;
        int popYId = 0;
        Polygon p = poly[0];
        for( int j=0; j<k; j++ ) {
          if( Math.abs(p.xpoints[j]-e.getX()) < minXDist ) {
            popXId = j;
            minXDist =  Math.abs(p.xpoints[j]-e.getX());
          }
        }
        for( int i=0; i<data.n; i++ ) {
          p = poly[i];
          if( Math.abs(p.ypoints[popXId]-e.getY()) < minYDist ) {
            popYId = i;
            minYDist =  Math.abs(p.ypoints[popXId]-e.getY());
          }
        }
        if( minXDist < slotWidth/4 ) {
          JPopupMenu name = new JPopupMenu();
          JMenuItem colName = null;
          JMenuItem colVal  = null;
          if(  data.phoneNumber(vars[permA[popXId]]) ) {
            colName = new JMenuItem(data.getName(vars[permA[popXId]])+'\n'+"Number: "+ Util.toPhoneNumber(dataCopy[permA[popXId]][popYId]));
          }
          else if( data.categorical(vars[permA[popXId]]) )
            if( paintMode.equals("Poly") ) {
              colName = new JMenuItem(data.getName(vars[permA[popXId]]));
              colVal  = new JMenuItem("   Level: "+data.getLevelName(vars[permA[popXId]], dataCopy[permA[popXId]][popYId]));
            }
              else
                for( int i = 0;i < rects.size(); i++) {
                  MyRect r = (MyRect)rects.elementAt(i);
                  if ( r.contains( e.getX(), e.getY()+sb.getValue() )) {
                    r.pop(this, e.getX(), e.getY());
                  }
                }
                  else {
                    colName = new JMenuItem(data.getName(vars[permA[popXId]]));
                    colVal  = new JMenuItem("   Value: "+dataCopy[permA[popXId]][popYId]);
                  }
                  if( colName != null ) {
                    name.add(colName);
                    name.add(colVal);

                    name.show(e.getComponent(), e.getX(), e.getY());
                  }
        }
          else {
            JPopupMenu scaleType = new JPopupMenu("Title");

            if( Scale.equals("Common") ) {
              JMenuItem Com = new JMenuItem("Scale Common");
              scaleType.add(Com);
              Com.setActionCommand("Common");
              Com.addActionListener(this);
            }
            else {
              JMenuItem Ind = new JMenuItem("Scale Individual");
              scaleType.add(Ind);
              Ind.setActionCommand("Individual");
              Ind.addActionListener(this);
            }

            JMenu plotM = new JMenu("Type");
            JMenuItem polyM = new JMenuItem("Polygons");
            JMenuItem boxM  = new JMenuItem("Box Plots");
            JMenuItem bothM = new JMenuItem("Both");
            plotM.add(polyM);
            plotM.add(boxM);
            plotM.add(bothM);
            polyM.setActionCommand("Poly");
            boxM.setActionCommand("Box");
            bothM.setActionCommand("Both");
            polyM.addActionListener(this);
            boxM.addActionListener(this);
            bothM.addActionListener(this);	  
            scaleType.add(plotM);

            JMenu sortM = new JMenu("Sort Axes by");
            JMenuItem minM = new JMenuItem("Minimum");
            JMenuItem quarM  = new JMenuItem("IQ-Range");
            JMenuItem medianM  = new JMenuItem("Median");
            JMenuItem meanM = new JMenuItem("Mean");
            JMenuItem sdevM  = new JMenuItem("Std. Dev.");
            JMenuItem maxM  = new JMenuItem("Maximum");
            JMenuItem iniM  = new JMenuItem("Initial");
            JMenuItem revM  = new JMenuItem("Reverse");
            sortM.add(minM);
            sortM.add(quarM);
            sortM.add(medianM);
            sortM.add(meanM);
            sortM.add(sdevM);
            sortM.add(maxM);
            sortM.addSeparator();
            sortM.add(iniM);
            sortM.add(revM);
            minM.setActionCommand("min");
            quarM.setActionCommand("quar");
            medianM.setActionCommand("median");
            meanM.setActionCommand("mean");
            sdevM.setActionCommand("sdev");
            maxM.setActionCommand("max");
            iniM.setActionCommand("ini");
            revM.setActionCommand("rev");
            minM.addActionListener(this);
            quarM.addActionListener(this);
            medianM.addActionListener(this);	  
            meanM.addActionListener(this);	  
            sdevM.addActionListener(this);	  
            maxM.addActionListener(this);	  
            iniM.addActionListener(this);	  
            revM.addActionListener(this);	  
            scaleType.add(sortM);

            JMenu alphaM = new JMenu("Alpha");
            JMenuItem alpha1 = new JMenuItem("1.0");
            JMenuItem alpha01 = new JMenuItem("0.1");
            JMenuItem alpha005  = new JMenuItem("0.05");
            JMenuItem alpha001 = new JMenuItem("0.01");
            JMenuItem alpha0005 = new JMenuItem("0.005");
            alphaM.add(alpha1);
            alphaM.add(alpha01);
            alphaM.add(alpha005);
            alphaM.add(alpha001);
            alphaM.add(alpha0005);
            alpha01.setActionCommand("1.0");
            alpha01.setActionCommand("0.1");
            alpha005.setActionCommand("0.05");
            alpha001.setActionCommand("0.01");
            alpha0005.setActionCommand("0.005");
            alpha1.addActionListener(this);
            alpha01.addActionListener(this);
            alpha005.addActionListener(this);
            alpha001.addActionListener(this);	  
            alpha0005.addActionListener(this);	  
            scaleType.add(alphaM);          
            if( k > 1 && !paintMode.equals("XbyY") ) {
              scaleType.show(e.getComponent(), e.getX(), e.getY());
            }
          }
      }
        else
          if( (e.getID() == MouseEvent.MOUSE_PRESSED ) && !paintMode.equals("XbyY") &&
              ((e.getModifiers() == ALT_DOWN+4) && (System.getProperty("os.name").equals("Irix")) ||
               (e.getModifiers() == ALT_DOWN+4) && (System.getProperty("os.name").equals("Linux")) ||
               (e.getModifiers() == ALT_DOWN) && (System.getProperty("os.name").equals("Mac OS")) ||
               (e.getModifiers() == BUTTON1_DOWN + ALT_DOWN)) ) {
            //System.out.println("Moving Start");
            moving = true;
            frame.setCursor(Frame.HAND_CURSOR);
            Graphics g = this.getGraphics();

            int minXDist = 5000;
            int popXId = 0;
            Polygon p = poly[0];
            for( int j=0; j<k; j++ ) {
              if( Math.abs(p.xpoints[j]-e.getX()) < Math.abs(minXDist) ) {
                popXId = j;
                minXDist =  p.xpoints[j]-e.getX();
              }
            }

            movingID = popXId;

            movingName = (MyText)names.elementAt(movingID);

            g.setColor(MFrame.backgroundColor);

            if( movingID == 0 )
              movingName.draw(g, 0);
            else if( movingID == k-1)
              movingName.draw(g, 1);
            else
              movingName.draw(g, 2);

            g.setXORMode(MFrame.backgroundColor);

            g.setColor(Color.black);
            lastX = e.getX();
            movingName.moveXTo(lastX);
            movingName.draw(g, 2);
            g.fillRect(lastX-1, border, 3, height - 2*border);
          }
        else if( (e.getID() == MouseEvent.MOUSE_RELEASED) && moving ) {
          //System.out.println("Moving Stop");
          Graphics g = this.getGraphics();
          g.setXORMode(MFrame.backgroundColor);
          g.fillRect(lastX-1, border, 3, height - 2*border);
          movingName.moveXTo(lastX);
          movingName.draw(g, 2);
          moving = false;
          frame.setCursor(Frame.DEFAULT_CURSOR);

          int minXDist = 5000;
          int popXId = 0;
          int diff = 0;
          Polygon p = poly[0];
          for( int j=0; j<k; j++ ) {
            diff = p.xpoints[j]-e.getX();
            if( Math.abs(diff) < Math.abs(minXDist) ) {
              popXId = j;
              minXDist = diff;
            }
          }
          int insertBefore = popXId;

          if( minXDist < 0 )
            insertBefore = Math.min(insertBefore + 1, k-1);

          int save = permA[movingID];  
          if(movingID < insertBefore) {
            for( int j=movingID; j<insertBefore; j++ )
              permA[j] = permA[j+1];
            permA[insertBefore-1] = save;
          }
          else {
            for( int j=movingID; j>insertBefore; j-- )
              permA[j] = permA[j-1];
            permA[insertBefore] = save;
          }
          create(width, height);
          update(this.getGraphics());
        }                                    // Moving Axis handling end	  
        else {                               // Zooming into individual axes
          if( ((e.getModifiers() == CTRL_DOWN+16) && (System.getProperty("os.name").equals("Irix"))) ||
              ((e.getModifiers() == CTRL_DOWN+16) && (System.getProperty("os.name").equals("Linux"))) ||
              ((e.getModifiers() == 20) && (System.getProperty("os.name").equals("Mac OS X"))) ||
              (e.getModifiers() == META_DOWN) ) {
            //System.out.println("Return: "+e.getModifiers()+"  Test: "+(BUTTON1_DOWN + CTRL_DOWN));
            if (e.getID() == MouseEvent.MOUSE_PRESSED ) {
              lastX = e.getX();
              //System.out.println("lastX: "+lastX);
            }
            else {
              int popXId = 0;
              Polygon p = poly[0];
              if( Math.abs(e.getX() - lastX) > 4 ) {
                int testX = lastX + (int)(0.5+slotWidth)/2 * ( e.getX() - lastX ) / Math.abs( e.getX() - lastX );
                for( int j=0; j<k; j++ ) {
                  if( (p.xpoints[j] < testX) && (lastX < p.xpoints[j]) ||
                      (p.xpoints[j] > testX) && (lastX > p.xpoints[j]) ){
                    popXId = j;
                    //System.out.println("popXId: "+popXId+"e.getX: "+e.getX());
                  }
                }
                setCoordinates(0, Mins[permA[popXId]], 1, Maxs[permA[popXId]], 0);
                super.processMouseEvent(e);                           // This performs the zoom !!
                Mins[permA[popXId]] = getLly();
                Maxs[permA[popXId]] = getUry();
              }
              else {
                int minXDist = 5000;
                for( int j=0; j<k; j++ ) {
                  if( Math.abs(p.xpoints[j]-e.getX()) < Math.abs(minXDist) ) {
                    popXId = j;
                    minXDist =  p.xpoints[j]-e.getX();
                  }
                }
                Mins[permA[popXId]] = dMins[permA[popXId]];
                Maxs[permA[popXId]] = dMaxs[permA[popXId]];
              }
              //System.out.println("Id: "+popXId);
              create(width, height);
              update(this.getGraphics());
            }
          }
          super.processMouseEvent(e);
        }
    }
      else                                     // if not pressed or released
        super.processMouseEvent(e);
  }

    public void processMouseMotionEvent(MouseEvent e) {
      if( moving ) {
        Graphics g = this.getGraphics();
        g.setXORMode(MFrame.backgroundColor);
        g.fillRect(lastX-1, border, 3, height - 2 * border);
        movingName.moveXTo(lastX);
        movingName.draw(g, 2);
        lastX = e.getX();
        lastX = Math.max(lastX, border);
        lastX = Math.min(lastX, width-border);
        g.fillRect(lastX-1, border, 3, height - 2 * border);
        movingName.moveXTo(lastX);
        movingName.draw(g, 2);
      }
      else
        super.processMouseMotionEvent(e);  // Pass other event types on.
    }

    public void processKeyEvent(KeyEvent e) {

      if (     e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
          && ( e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_NUMPAD0)) {
        if( (e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_NUMPAD0  )
            && e.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() &&
            frame.getWidth() > Toolkit.getDefaultToolkit().getScreenSize().width) {
          frame.setSize((Toolkit.getDefaultToolkit().getScreenSize()).width, frame.getHeight());
          this.setSize((Toolkit.getDefaultToolkit().getScreenSize()).width, this.getHeight());
        }
      } else                                     
          super.processKeyEvent(e);
    }

    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      System.out.println("Command: "+command);
      if( command.equals("Common") || command.equals("Individual") ) {
        if( command.equals("Common") ) 
          Scale = "Individual";
        else
          Scale = "Common";
        create(width, height);
        update(this.getGraphics());
      }
      else if( command.equals("Box") || command.equals("Poly") || command.equals("Both") ) {
        paintMode = command;
        create(width, height);
        update(this.getGraphics());
      }
      else if( command.equals("1.0") || command.equals("0.1") || command.equals("0.05") || command.equals("0.01") || command.equals("0.005") ) {
        alpha = (float)Util.atod(command);
        create(width, height);
        update(this.getGraphics());
      } else if( command.equals("min") || command.equals("quar") || command.equals("sdev") || command.equals("mean") || command.equals("median") || command.equals("max") || command.equals("ini") || command.equals("rev") ) {
        if( command.equals("ini") )
          for( int i=0; i<sortA.length; i++ )
            permA[i] =i;
        else if( command.equals("rev") ) {
          int left  = 0;              // index of leftmost element
          int right = permA.length-1; // index of rightmost element
          while (left < right) {   // exchange the left and right elements
            int temp = permA[left];
            permA[left]  = permA[right];
            permA[right] = temp;
            left++;                // move the bounds toward the center
            right--;
          }
        } else {
          if( command.equals("min") )
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dMins[i];
          if( command.equals("quar") ) 
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dIQRs[i];
          if( command.equals("median") ) 
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dMedians[i];
          if( command.equals("mean") ) 
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dMeans[i];
          if( command.equals("sdev") ) 
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dSDevs[i];
          if( command.equals("max") )
            for( int i=0; i<sortA.length; i++ ) 
              sortA[i] = dMaxs[i];
          int[] perm = Qsort.qsort(sortA, 0, sortA.length-1);
          for( int i=0; i<sortA.length; i++ )
            permA[i] = perm[i];
        }
        create(width, height);
        update(this.getGraphics());
      } else
        super.actionPerformed(e);
    }

    public void paint(Graphics g2d) {

      Graphics g = (Graphics2D)g2d;

      double[] selection;
      Dimension size = this.getSize();

      if( oldWidth != size.width || oldHeight != size.height || frame.getBackground() != MFrame.backgroundColor) {
        frame.setBackground(MFrame.backgroundColor);

        this.width = size.width;
        this.height = size.height;
        create( width, height );
        oldWidth = size.width;
        oldHeight = size.height;
      }
      if( bg == null || printing || g instanceof PSGr ) {
        if( !(printing)  && !(g instanceof PSGr) ) {
          bi = createImage(size.width, size.height);	// double buffering from CORE JAVA p212
          tbi = createImage(size.width, size.height);
          bg = bi.getGraphics();
        }
        else
          bg = g;
        //bg.setColor(Color.black);
        bg.setColor(new Color(0, 0, 0, alpha));
        if( paintMode.equals("Poly") ) {
          for( int i=0; i<data.n; i++ )
            bg.drawPolyline(poly[i].xpoints, poly[i].ypoints, k); 
        }
        for( int j=0; j<k; j++ ) {	
          bg.setColor(new Color(255, 255, 255, 75));
          bg.drawLine( poly[1].xpoints[j]-1, border-2, (poly[1].xpoints)[j]-1, size.height-border+2);
          bg.setColor(new Color(255, 255, 255, 140));
          bg.drawLine( poly[1].xpoints[j],   border-2, (poly[1].xpoints)[j],   size.height-border+2);
          bg.setColor(new Color(255, 255, 255, 75));
          bg.drawLine( poly[1].xpoints[j]+1, border-2, (poly[1].xpoints)[j]+1, size.height-border+2);
        }	
        bg.setColor(Color.black);
        if( paintMode.equals("Box") || paintMode.equals("Both") || paintMode.equals("XbyY")) {
          for( int i=0; i<bPlots.size(); i++ )
            ((boxPlot)(bPlots.elementAt(i))).draw(bg);
          for( int i=0; i<rects.size(); i++ )
            ((MyRect)(rects.elementAt(i))).draw(bg);
        }
        for( int j=0; j<k; j++ ) {
          int x = border+addBorder+(int)(0.5+(j*slotWidth));
          MyText mt = (MyText)names.elementAt(j);
          if( (j % 2) == 1 )
            mt.moveYTo(border - 3);
          else
            mt.moveYTo(height - border + 13);
          mt.moveXTo(x);
          if( k == 1 )
            mt.draw(bg, 2);
          else
            if( j == 0 )
              mt.draw(bg, 0);
          else if( j == k-1)
            mt.draw(bg, 1);
          else
            mt.draw(bg, 2);
        }
      }

      long start = new Date().getTime();
      Graphics tbg;
      if( !(printing)  && !(g instanceof PSGr) )
        tbg = (Graphics2D)tbi.getGraphics();
      else
        tbg = g;

      if( !(printing)  && !(g instanceof PSGr) )    
        tbg.drawImage(bi, 0, 0, null);
      //tbg.setColor(Color.red);
      tbg.setColor(DragBox.hiliteColor);
      if( data.countSelection()>0 ) {
        selection = data.getSelection();
        if( paintMode.equals("Poly") || paintMode.equals("Both")) {
          for( int i=0; i<data.n; i++ ) {
            if( selection[i] > 0 )
              tbg.drawPolyline(poly[i].xpoints, poly[i].ypoints, k);
          }
        }
        if( paintMode.equals("Box") || paintMode.equals("Both") || paintMode.equals("XbyY") ) {
          for( int i=0; i<bPlots.size(); i++ ) {
            if( paintMode.equals("XbyY") )
              data.setFilter( yVar, lNames[i]);
            ((boxPlot)(bPlots.elementAt(i))).drawHighlight(tbg);
            data.resetFilter();
          }

          // for build in barcharts
          //
          int tabcount = 0;
          if( tabs.size() > 0 ) {
            Table tablep = (Table)(tabs.firstElement());
            for( int i = 0;i < rects.size(); i++) {
              MyRect r = (MyRect)rects.elementAt(i);
              double sum=0, sumh=0;
              int id = ((Integer)(r.tileIds.elementAt(0))).intValue();
              if( id == 0 && i != 0 )
                tablep = (Table)(tabs.elementAt(++tabcount));
              sumh = tablep.getSelected(id)*tablep.table[id];
              sum  = tablep.table[id];
              r.setHilite( sumh/sum );
              r.draw(tbg);
            }
          }
        }
      }

      if( !(printing)  && !(g instanceof PSGr) ) {
        tbg.setColor(Color.black);
        drawSelections(tbg);
        g.drawImage(tbi, 0, 0, Color.black, null);
        tbg.dispose();
      }

      long stop = new Date().getTime();
      //System.out.println("Time for polys: "+(stop-start)+"ms");
    }

    public void drawSelections(Graphics g) {

      int plotID = 0;

      for( int i=0; i<Selections.size(); i++) {
        Selection S = (Selection)Selections.elementAt(i);
        for( int j=0; j<k; j++)
          if( (  paintMode.equals("XbyY") && j == ((floatRect)S.o).var ) ||
              ( !paintMode.equals("XbyY") && vars[permA[j]] == ((floatRect)S.o).var ) )
            plotID = j;

        S.r.x      = (int)(border + addBorder + slotWidth * plotID - (1.0-((floatRect)S.o).x) * (double)slotWidth);
        S.r.y      = (int)(-border + height - (height-2*border) * ((((floatRect)S.o).y - Mins[plotID])/(Maxs[plotID]-Mins[plotID])));
        S.r.width  = (int)(((floatRect)S.o).w * Math.round(slotWidth));
        S.r.height = (int)((height-2*border) * ((floatRect)S.o).h / (Maxs[plotID]-Mins[plotID]));

        //System.out.println("S.r.x: "+S.r.x+" S.r.y: "+S.r.y+" S.r.width: "+S.r.width+" S.r.height: "+S.r.height);

        drawBoldDragBox(g, S);
      }
    }

    public void maintainSelection(Selection S) {

      Graphics g = this.getGraphics();

      Rectangle sr = S.r;
      int mode = S.mode;

      int selectCol=0;

      Polygon p = poly[0];
      for( int j=0; j<p.npoints-1; j++ ) {
        if( p.xpoints[j] <= sr.x && p.xpoints[j+1] > sr.x )
          selectCol = j+1;
      }
      //System.out.println(" **** select col: "+selectCol);
      int passID = vars[permA[selectCol]];
      if( paintMode.equals("XbyY") )
        passID = selectCol;

      if( k == 1 )
        S.o = new floatRect((double)((S.r.x - border - addBorder) % slotWidth)/slotWidth,
                            ((double)(height-border-S.r.y))/(height-40)*(Maxs[selectCol]-Mins[selectCol])+Mins[selectCol],
                            (double)(S.r.width)/slotWidth ,
                            (double)(S.r.height)/(height-2*border)*(Maxs[selectCol]-Mins[selectCol]),
                            passID);	
      else
        S.o = new floatRect((double)((S.r.x - border) % slotWidth)/slotWidth,
                            ((double)(height-border-S.r.y))/(height-40)*(Maxs[selectCol]-Mins[selectCol])+Mins[selectCol],
                            (double)(S.r.width)/(p.xpoints[1] - p.xpoints[0]),
                            (double)(S.r.height)/(height-2*border)*(Maxs[selectCol]-Mins[selectCol]),
                            passID);

      if( ((floatRect)S.o).x < 0 )
        ((floatRect)S.o).x = 1-Math.abs(((floatRect)S.o).x);

      int tabcount = 0;
      int thistable = 0;
      boolean intersect = false;
      for( int i = 0;i < rects.size(); i++) {
        MyRect r = (MyRect)rects.elementAt(i);
        int id = ((Integer)(r.tileIds.elementAt(0))).intValue();
        if( id == 0 && i != 0 )
          tabcount++;
        if ( r.intersects( sr )) {
          thistable = tabcount;
          intersect = true;
        }
      }

      if( intersect && !paintMode.equals("Poly") ) {
        Table tablep=(Table)(tabs.firstElement());
        tabcount = 0;
        for( int i = 0;i < rects.size(); i++) {
          MyRect r = (MyRect)rects.elementAt(i);
          double sum=0, sumh=0;
          int id = ((Integer)(r.tileIds.elementAt(0))).intValue();
          if( id == 0 && i != 0 )
            tablep = (Table)(tabs.elementAt(++tabcount));
          if( tabcount == thistable ) {
            if ( r.intersects( sr ))
              tablep.setSelection(id,1,mode);
            else
              tablep.setSelection(id,0,mode);
            sumh = tablep.getSelected(id)*tablep.table[id];
            sum  = tablep.table[id];
            r.setHilite( sumh/sum );	
          }
        }
      }
      else {
        if( paintMode.equals("XbyY") ) {
          for( int i=0; i<data.n; i++ ) {
            p = poly[i];
            if( !sr.contains(p.xpoints[selectCol], p.ypoints[selectCol] ) )
              data.setSelection(i,0,mode);
          }
          data.setFilter( yVar, lNames[selectCol]);
        }
        for( int i=0; i<data.n; i++ ) {
          p = poly[i];
          if( sr.contains(p.xpoints[selectCol], p.ypoints[selectCol] ) ) {
            data.setSelection(i,1,mode);
          }
          else
            data.setSelection(i,0,mode); 
        }
        data.resetFilter();
      }		
    }

    void getData() {

      if( paintMode.equals("XbyY") ) {
        k = data.getNumLevels(yVar);
        vars = new int[k];
        for( int j=0; j<k; j++ ) {
          vars[j] = xVar;
        }
        frame.setSize(50 * (1 + k), 400);
      }

      sortA = new double[k];
      permA = new int[k];
      for(int i=0; i<k; i++)
        permA[i] = i;

      dMins = new double[k];
      dIQRs = new double[k];
      dMedians = new double[k];
      dMeans = new double[k];
      dSDevs = new double[k];
      dMaxs = new double[k];
      Mins = new double[k];
      Maxs = new double[k];
      dataCopy = new double[k][data.n];

      for( int j=0; j<k; j++ ) {
        dMins[j] = data.getMin(vars[j]);
        dIQRs[j] = data.getQuantile(vars[j], 0.75) - data.getQuantile(vars[j], 0.25);
        dMedians[j] = data.getQuantile(vars[j], 0.5);
        dMeans[j] = data.getMean(vars[j]);
        dSDevs[j] = data.getSDev(vars[j]);
        dMaxs[j] = data.getMax(vars[j]);
        if( data.categorical(vars[j]) && !data.alpha(vars[j]) ) {
          dataCopy[j] = data.getRawNumbers(vars[j]);
        }
        else
          dataCopy[j] = data.getNumbers(vars[j]);
      }
    }	

    void create(int width, int height) {

      if( bg != null ) {
        bg.dispose();
        bg = null;
      }

      for( int j=0; j<k; j++ ) {
        Mins[j] = dMins[j];
        Maxs[j] = dMaxs[j];
      }
      if( Scale.equals("Individual") )
        scaleCommon();

      if( k > 1 )
        slotWidth = (width-2.0*border)/(k-1.0);
      else
        slotWidth = 100;

System.out.println("Slot: "+slotWidth);
      
      names.removeAllElements();
      if( paintMode.equals("XbyY") )
        lNames = data.getLevels(yVar);

      for( int j=0; j<k; j++ ) {
        if( !paintMode.equals("XbyY") )
          names.addElement(new MyText(data.getName(vars[permA[j]]), 1, 1));
        else {
          names.addElement(new MyText(lNames[j], 1, 1));
        }
      }

      poly = new Polygon[data.n];
      for( int i=0; i<data.n; i++ ) {
        poly[i] = new Polygon();
        for( int j=0; j<k; j++ ) {	
          int x = border+addBorder+(int)(0.5+slotWidth*j);
          int y = (int)(-border + height - (height-2*border) * ((dataCopy[permA[j]][i] - Mins[permA[j]])/(Maxs[permA[j]]-Mins[permA[j]])));
          poly[i].addPoint(x, y);
        }
      }
      if( paintMode.equals("Box") || paintMode.equals("Both") || paintMode.equals("XbyY") ) {
        bPlots.removeAllElements();
        tabs.removeAllElements();
        rects.removeAllElements();
        for( int j=0; j<k; j++ ) {
          //        System.out.println("Name: "+lNames[j]);
          int x = border+(int)(0.5+slotWidth*j);
          if( !data.categorical(vars[permA[j]]) ) {
            if( paintMode.equals("XbyY") )
              data.setFilter( yVar, lNames[j]);
            bPlots.addElement(new boxPlot( j, vars[permA[j]] , x + addBorder, (int)(0.5+Math.min(slotWidth/2, 40)), border, height-border));
            data.resetFilter();
          }
          else {
            int[] dummy = new int[1];
            dummy[0] = vars[permA[j]];
            Table breakdown = data.breakDown("BlaBla", dummy, -1);
            tabs.addElement(breakdown);
            int lev = breakdown.levels[0];
            int sum = 0;

            Vector[] tileIds = new Vector[lev];
            for(int i=0; i<lev; i++ ) {
              sum += breakdown.table[i];
              tileIds[i] = new Vector(1,0);
              tileIds[i].addElement(new Integer(i));
            }
            int y = (int)(-border + height);
            for( int i=0; i<lev; i++ ) {
              int w = (int)(0.5+slotWidth/2);
              int h = (int)Math.round((height-2*border) * breakdown.table[i]/data.n);
              if( i<lev-1 )
                y -= h;
              else {
                h = y - border;
                y = border;
              }
              rects.addElement(new MyRect( true, 'x', "Observed", 
                                           x-(int)(0.5+slotWidth/4), y, w, h, 
                                           breakdown.table[i], breakdown.table[i], 1, 0, 
                                           breakdown.lnames[0][i]+'\n', tileIds[i]));	  
            }
          }
        }
      }

      /*
        if( paintMode.equals("XbysdddsY") ) {
          bPlots.removeAllElements();
          for( int j=0; j<k; j++ ) {
            int x = border+slotWidth*j;
            bPlots.addElement(new boxPlot( j, xVar, yVar, ((dataSet.Variable)data.data.elementAt(yVar)).isLevel(lNames[j]) ,
                                           x, slotWidth/2, border, height-border));
          }
        }
       */
      setDragBoxConstraints(0, 0, width, height, (int)(0.5+slotWidth)-2, height);
    }

    void scaleCommon() {
      double totMin = 1000000, totMax=-10000000;
      for( int j=0; j<k; j++ ) {
        if( totMin >= Mins[j] )
          totMin = Mins[j];
        if( totMax <= Maxs[j] )
          totMax = Maxs[j];
      }
      for( int j=0; j<k; j++ ) {
        Mins[j] = totMin;
        Maxs[j] = totMax;
      }
    }

    public void updateSelection() {
      paint(this.getGraphics());
    }

    public void dataChanged(int var) {
      getData();
      create(width, height);
      update(this.getGraphics());
    }

    // dummy for scrolling  
    public void adjustmentValueChanged(AdjustmentEvent e) {
    }
    public void scrollTo(int id) {
    }

    class floatRect {

      double x, y, w, h;
      int var;

      public floatRect(double x, double y, double w, double h, int var) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.var = var;
      }
    }



    class boxPlot {

      double min, lHinge, median, uHinge, max;
      double sMin, lSHinge, sMedian, uSHinge, sMax;
      double lWhisker, uWhisker;
      double lSWhisker, uSWhisker;
      double[] lOutlier, uOutlier;
      int var, id;
      int mid, width, low, high;

      public boxPlot( int id, int var, int mid, int width, int low, int high) {
        this.id = permA[id];
        this.var = var;
        this.mid = mid;
        this.width = width;
        this.low = low;
        this.high = high;
        init();
      }

      public void init() {
        min    = data.getQuantile(var, 0);
        lHinge = data.getQuantile(var, 0.25);
        median = data.getQuantile(var, 0.5);
        uHinge = data.getQuantile(var, 0.75);
        max    = data.getQuantile(var, 1);
        lWhisker = data.getFirstGreater(var, lHinge-(uHinge-lHinge)*1.5);
        uWhisker = data.getFirstSmaller(var, uHinge+(uHinge-lHinge)*1.5);
        lOutlier = data.getAllSmaller(var, lWhisker);
        uOutlier = data.getAllGreater(var, uWhisker);
      }

      void draw( Graphics g ) {

        if( (data.filterVar!=-1 && data.filterGrpSize[data.filterGrp] > 3) || (data.filterVar==-1 && data.n > 3) ) {

          int  lWP  = low+(int)((Maxs[id]-lWhisker)/(Maxs[id]-Mins[id])*(high-low));
          int  lHP  = low+(int)((Maxs[id]-lHinge)/(Maxs[id]-Mins[id])*(high-low));
          int  medP = low+(int)((Maxs[id]-median)/(Maxs[id]-Mins[id])*(high-low));
          int  uHP  = low+(int)((Maxs[id]-uHinge)/(Maxs[id]-Mins[id])*(high-low));
          int  uWP  = low+(int)((Maxs[id]-uWhisker)/(Maxs[id]-Mins[id])*(high-low));

          /*System.out.println("        Min:"+min);
          System.out.println("Lower Whisk:"+lWhisker);
          System.out.println("Lower Hinge:"+lHinge);
          System.out.println("     Median:"+median);
          System.out.println("Upper Hinge:"+uHinge);
          System.out.println("Upper Whisk:"+uWhisker);
          System.out.println("        Max:"+max);

          System.out.println(" Number of uppers:"+ uOutlier.length);
          System.out.println(" Number of lowers:"+ lOutlier.length);
          System.out.println("============");*/

          // Base Boxes
          g.setColor(Color.lightGray);
          g.fillRect(mid-width/2, uWP, width, uHP-uWP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2, uWP, width, uHP-uWP);
          g.setColor(Color.gray);
          g.fillRect(mid-width/2, uHP, width, medP-uHP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2, uHP, width, medP-uHP);
          g.setColor(Color.gray);
          g.fillRect(mid-width/2, medP, width, lHP-medP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2, medP, width, lHP-medP);
          g.setColor(Color.lightGray);
          g.fillRect(mid-width/2, lHP, width, lWP-lHP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2, lHP, width, lWP-lHP);

          for( int i=0; i<lOutlier.length; i++ ) {
            if( lOutlier[i] < lHinge-(uHinge-lHinge)*3 )
              g.fillOval(mid-1, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
            g.drawOval(mid-1, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
          }
          for( int i=0; i<uOutlier.length; i++ ) {
            if( uOutlier[i] > uHinge+(uHinge-lHinge)*3 )
              g.fillOval(mid-1, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
            g.drawOval(mid-1, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
          }
        } else {
          int  MinP = low+(int)((Maxs[id]-min)/(Maxs[id]-Mins[id])*(high-low));
          int  MedP = low+(int)((Maxs[id]-median)/(Maxs[id]-Mins[id])*(high-low));
          int  MaxP = low+(int)((Maxs[id]-max)/(Maxs[id]-Mins[id])*(high-low));
          g.drawRect(mid-width/2+4, MinP, width-8, 1);
          g.drawRect(mid-width/2+4, MedP, width-8, 1);
          g.drawRect(mid-width/2+4, MaxP, width-8, 1);
        }
      }

      void drawHighlight( Graphics g ) {

        if( data.filterVar != -1 ) {
          //        System.out.println("***** Group Size: "+data.filterSelGrpSize[data.filterGrp]);
          if( data.filterSelGrpSize[data.filterGrp] == 0) {
            //          System.out.println("Skipping: "+data.filterGrp);
            return;
          }
        }

        int count = data.countSelection();

        sMin    = data.getSelQuantile(var, 0);
        lSHinge = data.getSelQuantile(var, 0.25);
        sMedian = data.getSelQuantile(var, 0.5);
        uSHinge = data.getSelQuantile(var, 0.75);
        sMax    = data.getSelQuantile(var, 1);

        if( count > 3 ) {
          lOutlier = data.getAllSelSmaller(var, lSHinge-(uSHinge-lSHinge)*1.5);
          uOutlier = data.getAllSelGreater(var, uSHinge+(uSHinge-lSHinge)*1.5);
          /*        if( lOutlier.length == 0 )
            lSWhisker = sMin;
          else
            lSWhisker = lOutlier[lOutlier.length-1];
          if( uOutlier.length == 0 )
            uSWhisker = sMax;
          else
            uSWhisker = uOutlier[uOutlier.length-1]; */

          lSWhisker = data.getFirstSelGreater(var, lSHinge-(uSHinge-lSHinge)*1.5);
          uSWhisker = data.getFirstSelSmaller(var, uSHinge+(uSHinge-lSHinge)*1.5);

          int  lSWP  = low+(int)((Maxs[id]-lSWhisker)/(Maxs[id]-Mins[id])*(high-low));
          int  lSHP  = low+(int)((Maxs[id]-lSHinge)/(Maxs[id]-Mins[id])*(high-low));
          int  sMedP = low+(int)((Maxs[id]-sMedian)/(Maxs[id]-Mins[id])*(high-low));
          int  uSHP  = low+(int)((Maxs[id]-uSHinge)/(Maxs[id]-Mins[id])*(high-low));
          int  uSWP  = low+(int)((Maxs[id]-uSWhisker)/(Maxs[id]-Mins[id])*(high-low));
          // Highlight Boxes
          g.setColor(getHiliteColor());
          g.drawRect(mid-width/2+4, uSWP, width-8, 1);
          g.drawLine(mid, uSWP, mid, uSHP);
          g.fillRect(mid-width/2+4, uSHP, width-8, sMedP-uSHP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2+4, uSHP, width-8, sMedP-uSHP);
          g.setColor(getHiliteColor());
          g.fillRect(mid-width/2+4, sMedP, width-8, lSHP-sMedP);
          g.setColor(Color.black);
          g.drawRect(mid-width/2+4, sMedP, width-8, lSHP-sMedP);
          g.setColor(getHiliteColor());
          g.drawRect(mid-width/2+4, lSWP, width-8, 1);
          g.drawLine(mid, lSWP, mid, lSHP);

          for( int i=0; i<lOutlier.length; i++ ) {
            g.setColor(getBackground());
            g.fillOval(mid-2, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
            g.drawOval(mid-2, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
            g.setColor(getHiliteColor());
            if( lOutlier[i] < lSHinge-(uSHinge-lSHinge)*3 )
              g.fillOval(mid-2, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
            g.drawOval(mid-2, low+(int)((Maxs[id]-lOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
          }
          for( int i=0; i<uOutlier.length; i++ ) {
            g.setColor(getBackground());
            g.fillOval(mid-2, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
            g.drawOval(mid-2, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-2, 5, 5);
            g.setColor(getHiliteColor());
            if( uOutlier[i] > uSHinge+(uSHinge-lSHinge)*3 )
              g.fillOval(mid-1, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
            g.drawOval(mid-1, low+(int)((Maxs[id]-uOutlier[i])/(Maxs[id]-Mins[id])*(high-low))-1, 3, 3);
          } 
        }
        else {
          sMin    = data.getSelQuantile(var, 0);
          sMedian = data.getSelQuantile(var, 0.5);
          sMax    = data.getSelQuantile(var, 1);

          int  sMinP = low+(int)((Maxs[id]-sMin)/(Maxs[id]-Mins[id])*(high-low));
          int  sMedP = low+(int)((Maxs[id]-sMedian)/(Maxs[id]-Mins[id])*(high-low));
          int  sMaxP = low+(int)((Maxs[id]-sMax)/(Maxs[id]-Mins[id])*(high-low));
          g.setColor(getHiliteColor());
          g.drawRect(mid-width/2+4, sMinP, width-8, 1);
          g.drawRect(mid-width/2+4, sMedP, width-8, 1);
          g.drawRect(mid-width/2+4, sMaxP, width-8, 1);
        }
      }
    }
}




