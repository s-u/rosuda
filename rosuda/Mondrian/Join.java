// Please use this software or at least parts of it to
//    - navigate airplanes and supertankers
//    - control nuclear power plants and chemical plants
//    - launch cruise missiles automatically
//
//      thanks
//
//      To Do:
//               
//	       - Sorting of Intervalls in Histos ?? (DB)
//
//         - PC
//           - zoom after permuting the axis ???
//           - zoom back -> common scale ?!


import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.image.*;         
import java.awt.event.*;         // New event model.
import java.io.*;                // Object serialization streams.
import java.io.InputStream;      // Object serialization streams.
import java.util.*;              // For StingTokenizer.
import java.util.Vector;         // 
import java.util.Properties;     // To store printing preferences in.
import java.util.jar.JarFile; 	 // To load logo
import java.util.zip.ZipEntry;
import java.lang.*;              // 
import java.net.URL;
import java.sql.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import org.rosuda.JRclient.*;
import com.apple.mrj.*;

/**
*/

class Join extends JFrame implements SelectionListener, DataListener, MRJOpenDocumentHandler {  
  
  /** Remember # of open windows so we can quit when last one is closed */
  protected static int num_windows = 0;
  protected static Vector dataSets;
  public Vector Plots = new Vector(10,0);
  public Vector selList = new Vector(10,0);
  public Query sqlConditions;
  public boolean selseq = false;
  public boolean alphaHi = false;
  private Vector polys = new Vector(256,256);
  private JList varNames = null;
  private int numCategorical = 0;
  private int weightIndex = 0;
  private JScrollPane scrollPane;
  private JProgressBar progBar;
  private JPanel progPanel;
  private JLabel progText;
  private JMenuBar menubar;
  public JMenu windows;
  private JMenuItem n, nw, c, q, t, m, o, s, ss, p, od, mn, pr, b, bw, pc, pb, sc, sc2, hi, hiw, cs, vm;
  public  JMenuItem ca;
  private JCheckBoxMenuItem se, ah;
  private ModelNavigator Mn;
  private PreferencesFrame Pr;
  private int thisDataSet  = -1;
  private int graphicsPerf;
  static String user;
  private boolean mondrianRunning = false;
  private String justFile = "";
  
  public Join(Vector dataSets, boolean load, boolean loadDB, File loadFile) {

    MRJApplicationUtils.registerOpenDocumentHandler ( this );
    
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName() );
    }
    catch (Exception e) { }
    
    System.out.println("Starting RServe ... "+Srs.checkLocalRserve());

    user = System.getProperty("user.name");
    System.out.println(user+" on "+System.getProperty("os.name"));
    
    if( user.indexOf("dibene") > -1 || user.indexOf("hofmann") > -1) {
      PreferencesFrame.setScheme(1);
      selseq = true;
    } else if( user.indexOf("unwin") > -1 ) {
      PreferencesFrame.setScheme(0);
    } else if( user.indexOf("theus") > -1 ) {
      selseq = true;
    }
    PreferencesFrame.setScheme(2);
    
    Font SF = new Font("SansSerif", Font.BOLD, 12);
    this.setFont(SF);
    this.dataSets = dataSets;
    this.setTitle("Mondrian");               // Create the window.
    num_windows++;                           // Count it.
    
    menubar = new JMenuBar();         // Create a menubar.
    
    // Create menu items, with menu shortcuts, and add to the menu.
    JMenu file = (JMenu) menubar.add(new JMenu("File"));
    //   JMenu file = new JMenu("File");            // Create a File menu.
    file.add(o = new JMenuItem("Open"));
    o.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    file.add(od = new JMenuItem("Open Database"));
    od.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    if( user.indexOf("theus") > -1 )
      od.setEnabled(true);
    else
      od.setEnabled(false);
    file.add(s = new JMenuItem("Save"));
    s.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    s.setEnabled(false);
    file.add(ss = new JMenuItem("Save Selection"));
    ss.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    ss.setEnabled(false);
    file.add(c = new JMenuItem("Close Dataset"));
    c.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    c.setEnabled(false);
    //    file.add(p = new JMenuItem("Print Window",new JMenuShortcut(KeyEvent.VK_P)));
    if( ((System.getProperty("os.name")).toLowerCase()).indexOf("mac") == -1 ) {
      file.addSeparator();                     // Put a separator in the menu
      file.add(q = new JMenuItem("Quit"));
      q.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      q.addActionListener(new ActionListener() {     // Quit the program.
        public void actionPerformed(ActionEvent e) {
          try {																				// Shut down RServe if running ...
            Rconnection c=new Rconnection();
            c.shutdown();
          } catch (Exception x) {};

          System.exit(0); }
      });
    }
    menubar.add(file);                         // Add to menubar.
                                               //
    JMenu plot = new JMenu("Plot");            // Create a Plot menu.
    plot.add(n = new JMenuItem("Mosaic Plot"));
    n.setEnabled(false);
    plot.add(nw = new JMenuItem("Weighted Mosaic Plot"));
    nw.setEnabled(false);
    plot.add(b = new JMenuItem("Barchart"));
    b.setEnabled(false);
    plot.add(bw = new JMenuItem("Weighted Barchart"));
    bw.setEnabled(false);
    plot.add(hi = new JMenuItem("Histogram"));
    hi.setEnabled(false);
    plot.add(hiw = new JMenuItem("Weighted Histogram"));
    hiw.setEnabled(false);
    plot.add(pc = new JMenuItem("Parallel Coordinates"));
    pc.setEnabled(false);
    plot.add(pb = new JMenuItem("Parallel Boxplots"));
    pb.setEnabled(false);
    plot.add(sc2 = new JMenuItem("Scatterplots"));
    sc2.setEnabled(false);
    plot.add(m = new JMenuItem("Map"));
    m.setEnabled(false);
    if( user.indexOf("theus") > -1 ) {
      plot.addSeparator();                     // Put a separator in the menu
      plot.add(t = new JMenuItem("Test"));
    }
    menubar.add(plot);                         // Add to menubar.
    //
    JMenu options = new JMenu("Options");      // Create an Option menu.
    options.add(se = new JCheckBoxMenuItem("Selection Sequences", selseq));
    se.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    options.add(cs = new JMenuItem("Clear Sequences"));
    cs.setAccelerator(KeyStroke.getKeyStroke(Event.BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    options.addSeparator();                     // Put a separator in the menu
    options.add(ah = new JCheckBoxMenuItem("Alpha on Hilite", alphaHi));
    ah.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    options.addSeparator();                     // Put a separator in the menu
    options.add(vm = new JMenuItem("Switch Variable Mode"));
    vm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    options.addSeparator();                     // Put a separator in the menu
    options.add(mn = new JMenuItem("Model Navigator", KeyEvent.VK_J));
    mn.setEnabled(false);

    options.addSeparator();                     // Put a separator in the menu
    options.add(pr = new JMenuItem("Preferences ...", KeyEvent.VK_K));
    pr.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    
    menubar.add(options);                      // Add to menubar.
    
    windows = (JMenu) menubar.add(new JMenu("Window"));

    windows.add(ca = new JMenuItem("Close All"));
    ca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    ca.setEnabled(false);

    windows.addSeparator();

    this.setJMenuBar(menubar);                 // Add it to the frame.
    
    Icon MondrianIcon = new ImageIcon(readGif("Logo.gif"));    
    
    JLabel MondrianLabel = new JLabel(MondrianIcon);
    scrollPane = new JScrollPane(MondrianLabel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    getContentPane().add("Center", scrollPane);

    // Add the status/progress bar
    progPanel = new JPanel();
    progText = new JLabel("   Welcome !    "); 
    progPanel.add("North", progText);
    progBar = new JProgressBar();
    progBar.setMinimum(0);
    progBar.setMaximum(1);
    progBar.setValue(0);
    progBar.addChangeListener(new ChangeListener() {     
      public void stateChanged(ChangeEvent e) { showIt();}
    });
    progPanel.add("South", progBar);
    
    getContentPane().add("South", progPanel);
    
    // Create and register action listener objects for the menu items.
    n.addActionListener(new ActionListener() {     // Open a new mosaic plot window
      public void actionPerformed(ActionEvent e) {
        mosaicPlot();
      }
    });
    nw.addActionListener(new ActionListener() {     // Open a new weighted mosaic plot window
      public void actionPerformed(ActionEvent e) {
        weightedMosaicPlot();
      }
    });
    b.addActionListener(new ActionListener() {     // Open a new mosaic plot window
      public void actionPerformed(ActionEvent e) {
        barChart();
      }
    });
    bw.addActionListener(new ActionListener() {     // Open a new mosaic plot window
      public void actionPerformed(ActionEvent e) {
        weightedbarChart();
      }
    });
    hi.addActionListener(new ActionListener() {     // Open a histogram window
      public void actionPerformed(ActionEvent e) {
        histogram();
      }
    });
    hiw.addActionListener(new ActionListener() {     // Open a weighted histogram window
      public void actionPerformed(ActionEvent e) {
        weightedHistogram();
      }
    });
    pc.addActionListener(new ActionListener() {     // Open a parallel coordinate plot window
      public void actionPerformed(ActionEvent e) {
        pc("Poly");
      }
    });
    pb.addActionListener(new ActionListener() {     // Open a parallel boxplot plot window
      public void actionPerformed(ActionEvent e) {
        pc("Box");
      }
    });
    sc2.addActionListener(new ActionListener() {     // Open a scatterplot window
      public void actionPerformed(ActionEvent e) {
        scatterplot2D();
      }
    });
    if( user.indexOf("theus") > -1 )
      t.addActionListener(new ActionListener() {     // Open a new test window
        public void actionPerformed(ActionEvent e) {
          test();
        }
      }); 
    o.addActionListener(new ActionListener() {     // Load a dataset
      public void actionPerformed(ActionEvent e) {
        loadDataSet(false, null);
      }
    });
    od.addActionListener(new ActionListener() {     // Load a database
      public void actionPerformed(ActionEvent e) {
        loadDataSet(true, null);
      }
    });
    m.addActionListener(new ActionListener() {     // Open a new window to draw an interactive maps
      public void actionPerformed(ActionEvent e) {
        mapPlot();
      }
    });
    se.addActionListener(new ActionListener() {     // Change the selection mode
      public void actionPerformed(ActionEvent e) {
        switchSelection();
      }
    });
    ah.addActionListener(new ActionListener() {     // Change the alpha mode for highlighted cases
      public void actionPerformed(ActionEvent e) {
        switchAlpha();
      }
    });
    mn.addActionListener(new ActionListener() {     // Open a new window for the model navigator
      public void actionPerformed(ActionEvent e) {
        modelNavigator();
      }
    });
    pr.addActionListener(new ActionListener() {     // Open the Preference Box
      public void actionPerformed(ActionEvent e) {
        preferenceFrame();
      }
    });
    cs.addActionListener(new ActionListener() {     // Delete the current selection sequence
      public void actionPerformed(ActionEvent e) {
        deleteSelection();
      }
    });
    vm.addActionListener(new ActionListener() {     // Delete the current selection sequence
      public void actionPerformed(ActionEvent e) {
        switchVariableMode();
      }
    });
    ca.addActionListener(new ActionListener() {     // Close all Windows
      public void actionPerformed(ActionEvent e) {
        closeAll();
      }
    });
    c.addActionListener(new ActionListener() {     // Close this window.
      public void actionPerformed(ActionEvent e) { close(); }
    });
    
    // Another event listener, this one to handle window close requests.
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) { close(); }
    });
    
    // Set the window size and pop it up.
    this.setResizable(false);
    this.setSize(295,315);
    this.show();

    if( dataSets.isEmpty() )
      graphicsPerf = setGraphicsPerformance();
    else if (((dataSet)dataSets.firstElement()).graphicsPerf != 0)
      graphicsPerf = ((dataSet)dataSets.firstElement()).graphicsPerf;
    else
      graphicsPerf = 25000;
    
    Graphics g = this.getGraphics();
    g.setFont(new Font("SansSerif",0,11));
    g.drawString("RC1.0e", 250, 280);

    mondrianRunning = true;
    
    if( load )
      if( loadDB )
        loadDataSet(true, null);  
      else
        loadDataSet(false, loadFile); 
  }

  void showIt() {
    paintAll(this.getGraphics());
  }

  byte[] readGif(String name) {

    byte[] arrayLogo;
    try {
      JarFile MJF;
      try {
        MJF = new JarFile("Mondrian.app/Contents/Resources/Java/Mondrian.jar");
      } catch (Exception e) {
        MJF = new JarFile(System.getProperty("java.class.path"));
      }
      ZipEntry LE = MJF.getEntry(name);
      InputStream inputLogo = MJF.getInputStream(LE);
      arrayLogo = new byte[(int)LE.getSize()];
      for( int i=0; i<arrayLogo.length; i++ ) {
        arrayLogo[i] = (byte)inputLogo.read();
      }
    } catch (Exception e) {
      System.out.println("Logo Exception: "+e);
      arrayLogo = new byte[1];
    }
    return arrayLogo;
  }
  
  int setGraphicsPerformance() {
    
    int graphicsPerf=0;
    Image testI = createImage(200, 200);	    //
    Graphics2D gI = (Graphics2D)testI.getGraphics();
    gI.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float)0.05)));
    long start = new java.util.Date().getTime();
    while( new java.util.Date().getTime() - start < 1000) {
      graphicsPerf++;
      gI.fillOval(10, 10, 3, 3);
    }
    System.out.println("Graphics Performance: "+ graphicsPerf);
    
    return graphicsPerf;
  }
  
  /** Close a window.  If this is the last open window, just quit. */
  void close() {
    // Modal dialog with OK button

    if( thisDataSet == -1 ) {
      this.dispose();
      if( --num_windows == 0 )
        System.exit(0);
      return;
    }
    
    String message="";
    if( num_windows > 1 )
      message = "Close dataset \""+((dataSet)dataSets.elementAt(thisDataSet)).setName+"\" and\n all corresponding plots?";
    else if (num_windows == 1)
      message = "Close dataset \""+((dataSet)dataSets.elementAt(thisDataSet)).setName+"\" and\nquit Mondrian?";
    // Modal dialog with yes/no button
    int answer = JOptionPane.showConfirmDialog(this, message);
    if (answer == JOptionPane.YES_OPTION) {
      num_windows--;
      for( int i=Plots.size()-1; i>=0; i-- )
        ((MFrame)((DragBox)Plots.elementAt(i)).frame).close();
      this.dispose();
      if (num_windows == 0) {
        System.exit(0);
      }
    } else if (answer == JOptionPane.NO_OPTION) {
      // User clicked NO.
    }
  }

  public void closeAll() {
    for( int i=Plots.size()-1; i>=0; i-- ) {
      ((MFrame)((DragBox)Plots.elementAt(i)).frame).close();
      Plots.removeElementAt(i);
    }
  }

  public void switchSelection() {
    selseq = se.isSelected();
//System.out.println("Selection Sequences : "+selseq);
    if( !selseq )
      deleteSelection();
  }

  public void switchAlpha() {
    alphaHi = ah.isSelected();
  }

  
  public void deleteSelection() {
    if( selList.size() > 0 ) {
      for( int i=0; i<Plots.size(); i++ )
        (((DragBox)Plots.elementAt(i)).Selections).removeAllElements();
      for( int i=0; i< selList.size(); i++ ) 
        ((Selection)selList.elementAt(i)).status = Selection.KILLED;
      maintainWindowMenu(false);
      updateSelection();
    }
  }

  public void updateSelection() {
    // Remove Selections from list, which are no longer active
    //
    boolean selectAll = false;
    boolean deleteAll = false;
    boolean switchSel = false;
    
    for( int i=0; i<Plots.size(); i++ ) {
      if( ((DragBox)Plots.elementAt(i)).selectAll ) {    // This window has caused the select all event 
        ((DragBox)Plots.elementAt(i)).selectAll = false;
        selectAll = true;
      }
      if( ((DragBox)Plots.elementAt(i)).deleteAll ) {    // This window has caused the deletion event 
        ((DragBox)Plots.elementAt(i)).deleteAll = false;
        deleteSelection();
        return;
      }
      if( ((DragBox)Plots.elementAt(i)).switchSel ) {    // This window has caused the switch event 
        ((DragBox)Plots.elementAt(i)).switchSel = false;
        se.setSelected(!se.isSelected());
        switchSelection();
        return;
      }
      if( ((DragBox)Plots.elementAt(i)).switchAlpha ) {    // This window has caused the switch event
        ((DragBox)Plots.elementAt(i)).switchAlpha = false;
        ah.setSelected(!ah.isSelected());
        switchAlpha();
        ((DragBox)Plots.elementAt(i)).updateSelection();
        return;
      }
    }
    
    if( !selectAll ) {
      
      for( int i=selList.size()-1; i>=0; i-- ) {
        if( (((Selection)selList.elementAt(i)).status == Selection.KILLED) || 
            !((Selection)selList.elementAt(i)).d.frame.isVisible() ) {
          selList.removeElementAt(i);
        }
      }
      
      selList.trimToSize();
      
      Selection oneClick = null;

      // Get the latest selection and add it, if its a new selection
      //
      for( int i=0; i<Plots.size(); i++ )
        if( ((DragBox)Plots.elementAt(i)).frame.isVisible() ) {  // Plotwindow still exists
          if( ((DragBox)Plots.elementAt(i)).selectFlag ) {       // This window has caused the selection event 
            ((DragBox)Plots.elementAt(i)).selectFlag = false;    // We need to get the last selection from this plot
            Selection S = (Selection)(((DragBox)Plots.elementAt(i)).Selections.lastElement());
            
            if( selList.indexOf(S) == -1 )  { // Not in the list yet => new Selection to add !
              if( S.r.width > 2 && S.r.height > 2 && selseq) {
                S.step = selList.size() + 1;
                selList.addElement(S);
              } else {
                oneClick = S;
                System.out.println("Click Selection  !!");
                oneClick.status = Selection.KILLED;
                ((DragBox)Plots.elementAt(i)).Selections.removeElementAt(((DragBox)Plots.elementAt(i)).Selections.size()-1);
              }
            }
          }
        } else 
          Plots.removeElementAt(i);
      
      if( selList.size() > 0 ) {
        ((Selection)(selList.firstElement())).mode = Selection.MODE_STANDARD;
      }
      // Do the update over all selections
      //
      if( oneClick != null ) {
        //  This is a oneClick selection -> make it visible for Windows ...
        oneClick.r.width += 1;
        oneClick.r.height += 1;
        (oneClick.d).maintainSelection(oneClick);
      } else {
        maintainWindowMenu(false);

        for( int i=0; i< selList.size(); i++ ) {
          Selection S = ((Selection)selList.elementAt(i));
          S.step = i + 1;
          (S.d).maintainSelection(S);
          ((MFrame)((S.d).frame)).maintainMenu(S.step);
        }
      }
      sqlConditions = new Query();				// Replace ???
      if( ((dataSet)dataSets.elementAt(thisDataSet)).isDB )
        for( int i=0; i< selList.size(); i++ ) {
          Selection S = ((Selection)selList.elementAt(i));
          if( S.mode == S.MODE_STANDARD )
            sqlConditions.clearConditions();
          String condStr = S.condition.getConditions();
          if( !condStr.equals("") )
            sqlConditions.addCondition(S.getSQLModeString(S.mode), "("+condStr+")");
        };
      ((dataSet)(dataSets.elementAt(thisDataSet))).sqlConditions = sqlConditions;

//      System.out.println("Main Update: "+sqlConditions.makeQuery());
      
    } else {
      ((dataSet)(dataSets.elementAt(thisDataSet))).selectAll();
      if( ((dataSet)dataSets.elementAt(thisDataSet)).isDB )
        sqlConditions.clearConditions();
    }
				
    // Finally get the plots updated
    //
    for( int i=0; i<Plots.size(); i++ ) {	
      //     progText.setText("Query: "+i);
      progBar.setValue(1);   
      ((DragBox)Plots.elementAt(i)).updateSelection(); 
    }
				
    int nom   = ((dataSet)dataSets.elementAt(thisDataSet)).countSelection();
    int denom = ((dataSet)dataSets.elementAt(thisDataSet)).n;
    String Display = nom+"/"+denom+" ("+Stat.roundToString(100F*nom/denom,2)+"%)";
    progText.setText(Display);
    progBar.setValue(nom);   
  }
  
  public void dataChanged(int id) {
    for( int i=0; i<Plots.size(); i++ )
      if( ((DragBox)Plots.elementAt(i)).frame.isVisible() )  // Plotwindow still exists
        if( ((DragBox)Plots.elementAt(i)).dataFlag )         // This window was already updated 
          ((DragBox)Plots.elementAt(i)).dataFlag = false;
        else
          ((DragBox)Plots.elementAt(i)).dataChanged(id);
      else
        Plots.removeElementAt(i);
  }
  
  public void loadDataSet(boolean isDB, File file) {
    if( thisDataSet == -1 ) {
      if( isDB ) {
        loadDataBase();
      } 
      else {
        if( loadAsciiFile(file) ) {
          setVarList();
          this.setTitle("Mondrian("+((dataSet)dataSets.elementAt(thisDataSet)).setName+")");               // 
          c.setEnabled(true);

          int nom   = ((dataSet)dataSets.elementAt(thisDataSet)).countSelection();
          int denom = ((dataSet)dataSets.elementAt(thisDataSet)).n;
          String Display = nom+"/"+denom+" ("+Stat.roundToString(100*nom/denom,2)+"%)";
          progText.setText(Display);
          progBar.setValue(nom);          
        }
      }
    }
    else {
      new Join( dataSets, true , isDB, file);
    }
    if( thisDataSet != -1 )
      ((dataSet)dataSets.elementAt(thisDataSet)).graphicsPerf = graphicsPerf;	
  }
  
  public void setVarList() {
    if( thisDataSet == -1 )
      thisDataSet = dataSets.size() - 1;
    final dataSet data = (dataSet)dataSets.elementAt(thisDataSet); 
    String listNames[] = new String[data.k];
    for( int j=0; j<data.k; j++)
      listNames[j] = " "+data.getName(j);
    
    varNames = new JList(listNames);
    scrollPane.setViewportView(varNames);
    
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    varNames.addListSelectionListener(new ListSelectionListener() {     
      public void valueChanged(ListSelectionEvent e) { maintainPlotMenu(); }
    });

    varNames.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int index = varNames.locationToIndex(e.getPoint());
          if( !data.alpha(index) ) {
            if( data.categorical(index) )
              data.catToNum(index);
            else
              data.numToCat(index);
            setVarList();
            maintainPlotMenu();
          }
        }
      }
    });

    varNames.setCellRenderer(new MCellRenderer());

    RepaintManager currentManager = RepaintManager.currentManager(varNames);
    currentManager.setDoubleBufferingEnabled(true);    
    
    if( polys.size() > 0 )
      m.setEnabled(true);

    this.setResizable(true);

    this.show();
  }
  
  Driver d;
  Connection con;
  
  public boolean DBConnect(String URL,String  Username,String  Passwd) {
    try {
      // Connect to the database at that URL. 
      //	  URL="jdbc:mysql://137.250.124.51:3306/datasets";
      //      System.out.println("Database trying to connect ...: "+URL+"?user="+Username+"&password="+Passwd);
      con = DriverManager.getConnection(URL, Username, Passwd);
      //      con = DriverManager.getConnection(URL+"?user="+Username+"&password="+Passwd);
      System.out.println("Database Connected");
      return true;
    } catch (Exception ex) {
      System.out.println("Connection Exception: "+ex);
      return false;
    }
  }
  
  public boolean LoadDriver(String Driver) {
    try {
      d = (Driver)Class.forName(Driver).newInstance();
      System.out.println("Driver Registered");
      return true;
    } catch (Exception ex) {
      System.out.println("Driver Exception: "+ex);
      return false;
    }
  }
  
  public void loadDataBase() {
    if( thisDataSet == -1 ) {
      final JFrame DBFrame = new JFrame();
      DBFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) { DBFrame.dispose(); }
      });
      
      DBFrame.setTitle("DB Connection");
      GridBagLayout gbl = new GridBagLayout();
      DBFrame.getContentPane().setLayout(gbl);
      
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.weightx = 20;
      gbc.weighty = 100;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.EAST;
      
      Util.add(DBFrame, new JLabel(" Driver: "), gbc, 0,0,1,1);
      Util.add(DBFrame, new JLabel(" URL: "), gbc, 0,1,1,1);
      Util.add(DBFrame, new JLabel(" User: "), gbc, 0,2,1,1);
      Util.add(DBFrame, new JLabel(" Pwd: "), gbc, 2,2,1,1);
      Util.add(DBFrame, new JLabel(" DB: "), gbc, 0,3,1,1);
      Util.add(DBFrame, new JLabel(" Table: "), gbc, 0,4,1,1);
      
      final JTextField DriverName = new JTextField("org.gjt.mm.mysql.Driver",35);
      final JTextField URL = new JTextField("jdbc:mysql://137.250.124.51:3306/datasets",35);
      final JTextField Username = new JTextField("theusm",16);
      final JPasswordField Passwd = new JPasswordField("",16);
      final Choice DBList = new Choice();
      DBList.addItem("Not Connected");
      DBList.setEnabled(false);
      final Choice tableList = new Choice();
      tableList.addItem("Choose DB");
      tableList.setEnabled(false);
      final JButton Select = new JButton("Select");
      Select.setEnabled(false);
      final JButton Cancel = new JButton("Cancel");
      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor = GridBagConstraints.CENTER;
      Util.add(DBFrame, DriverName, gbc, 1,0,3,1);
      Util.add(DBFrame, URL, gbc, 1,1,3,1);
      Util.add(DBFrame, Username, gbc, 1,2,1,1);
      Util.add(DBFrame, Passwd, gbc, 3,2,1,1);
      Util.add(DBFrame, DBList, gbc, 1,3,3,1);
      Util.add(DBFrame, tableList, gbc, 1,4,3,1);
      gbc.fill = GridBagConstraints.NONE;
      Util.add(DBFrame, Select, gbc, 1,5,1,1);
      Util.add(DBFrame, Cancel, gbc, 3,5,1,1);
      
      final JButton Load = new JButton("Load");
      DBFrame.getRootPane().setDefaultButton(Load);
      final JButton Connect = new JButton("Connect");
      Connect.setEnabled(false);
      gbc.fill = GridBagConstraints.BOTH;
      gbc.anchor = GridBagConstraints.CENTER;
      Util.add(DBFrame, Load, gbc, 4,0,1,1);
      Util.add(DBFrame, Connect, gbc, 4,2,1,1);
      
      DBFrame.pack();
      DBFrame.show();
      Load.addActionListener(new ActionListener() {     // 
        public void actionPerformed(ActionEvent e) {
          if( LoadDriver(DriverName.getText()) ) {
            Connect.setEnabled(true);
            DBFrame.getRootPane().setDefaultButton(Connect);
          }
        }
      });
      
      Cancel.addActionListener(new ActionListener() {     // 
        public void actionPerformed(ActionEvent e) {
          DBFrame.dispose(); 
        }
      });
      
      Connect.addActionListener(new ActionListener() {     // 
        public void actionPerformed(ActionEvent e) {
          if( DBConnect(URL.getText(), Username.getText(), Passwd.getText()) ) {
            try {
              // Create statement
              Statement stmt = con.createStatement();
              
              // Execute query
              String query = "show databases";
              
              // Obtain the result set
              ResultSet rs = stmt.executeQuery(query);
              
              DBList.removeAll();
              while( rs.next() ) {
                DBList.addItem(rs.getString(1));
              }
              DBList.setEnabled(true);
              
              rs.close();
              
              // Close statement
              stmt.close(); 	    
            } catch (Exception ex) {
              System.out.println("Driver Exception: "+ex);
            }
          }
        }
      });
      
      DBList.addItemListener(new ItemListener() {     // 
        public void itemStateChanged(ItemEvent e) {
          try {
            // Create statement
            Statement stmt = con.createStatement();
            
            // Execute query
            String query = "show tables from "+DBList.getSelectedItem();
            
            // Obtain the result set
            ResultSet rs = stmt.executeQuery(query);
            
            tableList.removeAll();
            while( rs.next() ) {
              tableList.addItem(rs.getString(1));
            }
            tableList.setEnabled(true);
            
            rs.close();
            
            // Close statement
            stmt.close(); 	    
            con.close();                                // disconnect from DB and connect to selected DB
            String url = URL.getText();
            DBConnect(url.substring(0, url.lastIndexOf("/")+1)+DBList.getSelectedItem(), Username.getText(), Passwd.getText());
          } catch (Exception ex) {
            System.out.println("Can't get tables out of DB: "+ex);
          }
        }
      });
      
      tableList.addItemListener(new ItemListener() {     // 
        public void itemStateChanged(ItemEvent e) {
          Select.setEnabled(true);	    
          try {
            // Create statement
            Statement stmt = con.createStatement();
            
            // Execute query
            String query = "show fields from "+tableList.getSelectedItem()+" from "+DBList.getSelectedItem();
            
            // Obtain the result set
            ResultSet rs = stmt.executeQuery(query);
            
            while( rs.next() ) {
              System.out.println(rs.getString(1)+" - "+rs.getString(2));
            }
            
            rs.close();
            
            // Close statement
            stmt.close();
            
            DBFrame.getRootPane().setDefaultButton(Select);
          } catch (Exception ex) {
            System.out.println("Can't retreive columns of table >"+tableList.getSelectedItem()+"<: "+ex);
          }
        }
      });
      
      Select.addActionListener(new ActionListener() {     // 
        public void actionPerformed(ActionEvent e) {
          dataSet data = new dataSet(d, con, DBList.getSelectedItem(), tableList.getSelectedItem());
          dataSets.addElement( data );
          setVarList();
          DBFrame.dispose();
        }
      });
    }
  }

  boolean loadAsciiFile(File file) {

    boolean[] alpha;
    dataSet data;
    String filename = "";

    if( file == null ) {
      FileDialog f = new FileDialog(this, "Load Data", FileDialog.LOAD);
      //      JFileChooser f = new JFileChooser(this, "Load Data", FileDialog.LOAD);
      if((System.getProperty("os.name")).equals("Irix") )
        f.setDirectory("~theus/Data");
      f.setFile("");
      f.show();
      //System.out.println("->"+f.getDirectory()+"<-.->" + f.getFile());
      if (f.getFile() != null ) { 
        justFile = f.getFile();
        filename = f.getDirectory() + justFile;
      } else
        filename = "";
    } else {
      filename = file.getAbsolutePath();
      justFile = file.getName();
    }

    if( !filename.equals("") ) {

      String line="";

      try {
        BufferedReader br = new BufferedReader( new FileReader(filename) );
        data = new dataSet( justFile );
        dataSets.addElement(data);
        progText.setText("Peaking ...");
        alpha = data.sniff(br);
        progBar.setMaximum(data.n);
        br = new BufferedReader( new FileReader(filename) );
        progText.setText("Loading ...");
        data.read(br, alpha, progBar);

        br.mark(1000000);
        line = br.readLine();

        while( line != null && (line.trim()).equals("") ) {       // skip empty lines
          br.mark(1000000);
          line = br.readLine();
        }

        if( line != null ) {                          // more lines detected -> read the polygon

          progText.setText("Polygons ..."); 

          //====================== Check Scaling of the Polygon ===============================//
          String tLine;

          double xMin =  10e10;
          double xMax = -10e10;
          double yMin =  10e10;
          double yMax = -10e10;

          try {
            tLine = line;

            StringTokenizer head = new StringTokenizer(tLine, "\t");

            try{
              int      Id = Integer.valueOf(head.nextToken()).intValue();
              String name = head.nextToken();
              int npoints = Integer.valueOf(head.nextToken()).intValue();
              double[] x = new double[npoints];
              double[] y = new double[npoints];

              for( int i=0; i<npoints; i++ ) {
                tLine = br.readLine();
                StringTokenizer coord = new StringTokenizer(tLine);
                x[i] = Float.valueOf(coord.nextToken()).floatValue();
                xMin = Math.min(xMin, x[i]);
                xMax = Math.max(xMax, x[i]);
                y[i] = Float.valueOf(coord.nextToken()).floatValue();
                yMin = Math.min(yMin, y[i]);
                yMax = Math.max(yMax, y[i]);
              }
              //                  System.out.println("Read: "+npoints+" Points - xMin: "+xMin+"xMax: "+xMax+"yMin: "+yMin+"yMax: "+yMax);
            }	
            catch(NoSuchElementException e) {System.out.println("Poly Read Error: "+line);}
          }
          catch( IOException e ) {
            System.out.println("Error: "+e);
            System.exit(1);
          }
          //==================================================================//

          br.reset();
          int count = 0;
          while( line != null ) {
            MyPoly p = new MyPoly();
            p.read(br, xMin, 100000/Math.min(xMax-xMin, yMax-yMin), yMin, 100000/Math.min(xMax-xMin, yMax-yMin));
            if( count++ % (int)(Math.max(data.n/20, 1)) == 0 )
              progBar.setValue(Math.min(count, data.n));
            //MyPoly newP = p.thinHard();
            polys.addElement(p);
            line = br.readLine();                          // Read seperator (single blank line)
          }
        }
      }

      catch( IOException e ) {
        System.out.println("Error: "+e);
        System.exit(1);
      }
      progText.setText(""); 
      progBar.setValue(0);
      
      return true;
    } else
      return false;
  }

  public void handleOpenFile( File inFile )
  {
    while( !mondrianRunning ) {}   // Wait until Mondrian initialized

    loadDataSet( false, inFile );
  }

  public int[] getWeightVariable(int[] vars, dataSet data) {

    if( numCategorical == (varNames.getSelectedIndices()).length - 1 ) {
      int[] returner = new int[varNames.getSelectedIndices().length];
      System.arraycopy(varNames.getSelectedIndices(),0,returner,0,returner.length);
      for( int i=0; i<returner.length-1; i++ ) {
        System.out.println("checking ind = "+varNames.getSelectedIndices()[i]);
        if( varNames.getSelectedIndices()[i] == weightIndex ) {
          System.out.println("Swapping ...");
          int swap = varNames.getSelectedIndices()[returner.length - 1];
          returner[returner.length - 1] = weightIndex;
          returner[i] = swap;
        } else
          returner[i] = varNames.getSelectedIndices()[i];
      }
      for( int i=0; i<returner.length; i++ ) {
        System.out.println("ind old = "+varNames.getSelectedIndices()[i]+" ind new = "+returner[i]);
      }
      return returner ;
    } else {
      final Dialog countDialog = new Dialog(this, " Choose Weight Variable", true);
      Choice getCount = new Choice();
      for( int j=0; j<vars.length; j++ ) {
        if( data.getName(vars[j]).length()>1 && data.getName(vars[j]).substring(0,1).equals("/") )
          getCount.addItem(data.getName(vars[j]).substring(2));
        else
          getCount.addItem(data.getName(vars[j]));
      }
      for( int j=0; j<getCount.getItemCount(); j++ )
        if( getCount.getItem(j).toLowerCase().equals("count")    ||
            getCount.getItem(j).toLowerCase().equals("counts")   ||
            getCount.getItem(j).toLowerCase().equals("n")        ||
            getCount.getItem(j).toLowerCase().equals("weight")   ||
            getCount.getItem(j).toLowerCase().equals("observed") ||
            getCount.getItem(j).toLowerCase().equals("number") )
          getCount.select(j);
      Panel p1 = new Panel();
      p1.add(getCount);
      countDialog.add(p1, "Center");
      Button OK = new Button("OK");
      Panel p2 = new Panel();
      p2.add(OK);
      countDialog.add(p2, "South");
      OK.addActionListener(new ActionListener() {     //
        public void actionPerformed(ActionEvent e) {
          countDialog.dispose();
        }
      });
      countDialog.pack();
      if( countDialog.getWidth() < 240 )
        countDialog.setSize(240, countDialog.getHeight());
      countDialog.setResizable(false);
      countDialog.setModal(true);
      countDialog.setBounds(this.getBounds().x+this.getBounds().width/2-countDialog.getBounds().width/2,
                            this.getBounds().y+this.getBounds().height/2-countDialog.getBounds().height/2,
                            countDialog.getBounds().width,
                            countDialog.getBounds().height);
      countDialog.show();

      String[] selecteds = new String[(varNames.getSelectedValues()).length];
      for( int i=0; i < (varNames.getSelectedValues()).length; i++)
        selecteds[i] = (String)(varNames.getSelectedValues())[i];
      int[] selected = varNames.getSelectedIndices();
      int[] returner = new int[selected.length];
      for( int i=0; i<selected.length; i++) {
        if( (selecteds[i].trim()).equals(getCount.getSelectedItem()) ) {
          returner[selected.length-1] = selected[i];
          for( int j=i; j<selected.length-1; j++ )
            returner[j] = selected[j+1];
          i = selected.length;
        } else
          returner[i] = selected[i];
      }
      return returner;
    }
  }
  
  public void modelNavigator() {
    if( Mn == null )
      Mn = new ModelNavigator();
    else
      Mn.show();
  }
  
  public void preferenceFrame() {
    PreferencesFrame.showPrefsDialog(this);
  }
  
  public void test() {
    int p = (varNames.getSelectedIndices()).length;
    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));
    final MFrame scatterMf = new MFrame(this);
    scatterMf.setSize(200*p,200*p + 20);
    scatterMf.getContentPane().setLayout(new GridLayout(p,p));

    int[] tmpVars = new int[2];
    for(int i=0; i<p; i++)
      for(int j=0; j<p; j++) {
        if( i==j ) {
          int k = varNames.getSelectedIndices()[i];
          double start = tempData.getMin(k);
          double width = (tempData.getMax(k) - tempData.getMin(k)) / 8.9;
          Table discrete = tempData.discretize(tempData.setName, k, start, width, -1);

          Histogram scat = new Histogram(scatterMf, 200, 200, discrete, start, width, -1);
          scat.addSelectionListener(this);
          Plots.addElement(scat);
        }
        else {
          tmpVars[0] = varNames.getSelectedIndices()[j];
          tmpVars[1] = varNames.getSelectedIndices()[i];
          //
          Scatter2D scat = new Scatter2D(scatterMf, 200, 200, (dataSet)dataSets.elementAt(thisDataSet), tmpVars, varNames);
          scat.addSelectionListener(this);
          Plots.addElement(scat);
        }
    }
    scatterMf.setLocation(300, 0);
    scatterMf.setTitle("Scatterplot Matrix");
    scatterMf.show();
  }
  
  public void pc(String mode) {
    final MFrame pC = new MFrame(this);
    
    int totWidth = (Toolkit.getDefaultToolkit().getScreenSize()).width;
    int tmpWidth = 50 * (1 + (varNames.getSelectedIndices()).length);
    if( tmpWidth > totWidth)
      if( 20 * (1 + (varNames.getSelectedIndices()).length) < totWidth )
        tmpWidth = totWidth;
      else
        tmpWidth = 20 * (1 + (varNames.getSelectedIndices()).length);
      
    pC.setSize(tmpWidth, 400);
    pC.setLocation(300, 0);
    
    PC plotw = new PC(pC, (dataSet)dataSets.elementAt(thisDataSet), varNames.getSelectedIndices(), mode);
    plotw.addSelectionListener(this);
    Plots.addElement(plotw);
    pC.getContentPane().add(plotw);
    pC.show();
  }
  
  public void weightedMosaicPlot() {
    final MFrame mondrian = new MFrame(this);
    mondrian.setSize(400,400);
    
    dataSet data = (dataSet)dataSets.elementAt(thisDataSet);
    
    int[] vars = getWeightVariable(varNames.getSelectedIndices(), data);
    int[] passed = new int[vars.length-1];
    System.arraycopy(vars,0,passed,0,vars.length-1);
    int weight = vars[vars.length-1];
    Table breakdown = data.breakDown(data.setName, passed, weight);
    for( int i=0; i<passed.length-1; i++ )
      breakdown.addInteraction( new int[] { i }, false );
    breakdown.addInteraction( new int[] { passed.length-1 } , true  );
    final Mosaic plotw = new Mosaic(mondrian, 400, 400, breakdown);
    plotw.addSelectionListener(this);
    Plots.addElement(plotw);
    mondrian.getContentPane().add(plotw);                      // Add it
    mondrian.setLocation(300, 0);
    mondrian.show();
    
    if( Mn == null )	
      Mn = new ModelNavigator();
    plotw.addModelListener(Mn);    
    mn.setEnabled(true);
  }
  
  public void mosaicPlot() {
    final MFrame mondrian = new MFrame(this);
    mondrian.setSize(400,400);
    
    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));
    
    Table breakdown = tempData.breakDown(tempData.setName, varNames.getSelectedIndices(), -1);
    for( int i=0; i<(varNames.getSelectedIndices()).length-1; i++ ) {
      breakdown.addInteraction( new int[] { i }, false );
    }
    breakdown.addInteraction( new int[] { (varNames.getSelectedIndices()).length-1 } , true  );
    
    final Mosaic plotw = new Mosaic(mondrian, 400, 400, breakdown);
    plotw.addSelectionListener(this);
    Plots.addElement(plotw);
    mondrian.getContentPane().add(plotw);                      // Add it
    mondrian.setLocation(300, 0);
    mondrian.show();
    
    mondrian.addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {  plotw.processWindowEvent(e); }
    });
    
    if( Mn == null )
      Mn = new ModelNavigator();
    plotw.addModelListener(Mn);
    mn.setEnabled(true); 
  }
  
  public void barChart() {
    
    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));
    
    int[] indices = varNames.getSelectedIndices();
    int lastY = 333;
    int col=0;
    for(int i=0; i<indices.length; i++) {
      final MFrame bars = new MFrame(this);
      
      int[] dummy = {0};
      dummy[0] = indices[i];
      
      Table breakdown = tempData.breakDown(tempData.setName, dummy, -1);
      
      int totHeight = (Toolkit.getDefaultToolkit().getScreenSize()).height;
      int tmpHeight = Math.min(totHeight-30, 60 + breakdown.levels[0] * 30);
      
      bars.setSize(300, tmpHeight);
      final Barchart plotw = new Barchart(bars, 300, tmpHeight, breakdown);

      plotw.setScrollX();
      plotw.addSelectionListener(this);
      plotw.addDataListener(this);
      Plots.addElement(plotw);
      if( lastY + bars.getHeight() > (Toolkit.getDefaultToolkit().getScreenSize()).height ) {
        col += 1;
        lastY = 0;
      }
      if( 300*col > (Toolkit.getDefaultToolkit().getScreenSize()).width - 50 ) {
        col = 0;
        lastY = 353;
      }
      bars.setLocation(300*col, lastY);
        
      bars.show();
      if( lastY==0 )
        lastY += bars.getY();
      lastY += bars.getHeight();
    }
  }
  
  public void weightedbarChart() {
    
    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));
    
    int[] vars = getWeightVariable(varNames.getSelectedIndices(), tempData);
    int[] passed = new int[vars.length-1];
    System.arraycopy(vars,0,passed,0,vars.length-1);
    int weight = vars[vars.length-1];
    int lastY = 333;
    int col=0;
    
    for(int i=0; i<passed.length; i++) {
      final MFrame bars = new MFrame(this);
      
      int[] dummy = {0};
      dummy[0] = passed[i];
      Table breakdown = tempData.breakDown(tempData.setName, dummy, weight);
      
      int totHeight = (Toolkit.getDefaultToolkit().getScreenSize()).height;
      int tmpHeight = Math.min(totHeight-20, 60 + breakdown.levels[0] * 30);
      
      bars.setSize(300, tmpHeight);
      final Barchart plotw = new Barchart(bars, 300, tmpHeight, breakdown);

      plotw.setScrollX();
      plotw.addSelectionListener(this);
      plotw.addDataListener(this);
      Plots.addElement(plotw);
      if( lastY + bars.getHeight() > (Toolkit.getDefaultToolkit().getScreenSize()).height ) {
        col += 1;
        lastY = 0;
      }
      if( 300*col > (Toolkit.getDefaultToolkit().getScreenSize()).width - 50 ) {
        col = 0;
        lastY = 333;
      }
      bars.setLocation(300*col, lastY);

      bars.show();
      if( lastY==0 )
        lastY += bars.getY();
      lastY += bars.getHeight();
    }
  }


  public void weightedHistogram() {

    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));

    int[] vars = getWeightVariable(varNames.getSelectedIndices(), tempData);
    if( vars.length > 1 ) {
      int[] passed = new int[vars.length-1];
      System.arraycopy(vars,0,passed,0,vars.length-1);
      int weight = vars[vars.length-1];

      System.out.println(passed[0]+", "+weight);
      
      histoCore(tempData, passed, weight);
    } else
      histoCore(tempData, vars, vars[0]);
  }


  public void histogram() {
    
    dataSet tempData = ((dataSet)dataSets.elementAt(thisDataSet));
    int[] indices = varNames.getSelectedIndices();

    histoCore(tempData, indices, -1);
  }

  public void histoCore(dataSet tempData, int[] indices, int weight) {
    int lastX = 310, oldX = 0;
    int row=0;
    int menuOffset=0, xOff=0;

    for(int i=0; i<indices.length; i++) {
      final MFrame hists = new MFrame(this);
      
      int dummy = 0;
      dummy = indices[i];
      double start = tempData.getMin(dummy);
      double width = (tempData.getMax(dummy) - tempData.getMin(dummy)) / 8.9;
      Table discrete = tempData.discretize(tempData.setName, dummy, start, width, weight);
      
      hists.setSize(310, 250);
      final Histogram plotw = new Histogram(hists, 250, 310, discrete, start, width, weight);
      
      plotw.addSelectionListener(this);
      Plots.addElement(plotw);
      if( lastX + hists.getWidth() > (Toolkit.getDefaultToolkit().getScreenSize()).width +50 ) {   	// new Row
        row += 1;
        lastX = oldX % 310;
      }
      if( 250*row > (Toolkit.getDefaultToolkit().getScreenSize()).height - 125 ) {									// new Page
        row = 0;
        lastX = 310+xOff;
        xOff += menuOffset;
      }
      hists.setLocation(lastX, xOff+250*row);
      lastX += hists.getWidth();
      oldX = lastX;
      
      hists.show();
      if( i==0 ) {
        menuOffset = hists.getY();
        xOff = menuOffset;
      }
    }
  }
  
  public void mapPlot() {
    final MFrame mapf = new MFrame(this);
    mapf.setSize(400,400);
    mapf.setTitle("Map");

    Map map = new Map(mapf, 400, 400, (dataSet)dataSets.elementAt(thisDataSet), polys, varNames);
    map.addSelectionListener(this);
    Plots.addElement(map);

    if( map.ratio > 1 )
      mapf.setSize((int)(350 * map.ratio), 350 + 56);
    else
      mapf.setSize(350, (int)(350 / map.ratio) + 56);
    mapf.setLocation(0, 333);
      
    mapf.show();
  }
  
  public void scatterplot2D() {
    final MFrame scatterf = new MFrame(this);
    scatterf.setSize(400,400);
    scatterf.setTitle("Scatterplot 2D");
    
    Scatter2D scat = new Scatter2D(scatterf, 400, 400, (dataSet)dataSets.elementAt(thisDataSet), varNames.getSelectedIndices(), varNames);
    scat.addSelectionListener(this);
    Plots.addElement(scat);
    scatterf.setLocation(300, 333);
    scatterf.show();
  }

  public void switchVariableMode(){
    for(int i=0; i<varNames.getSelectedIndices().length; i++) {
      int index=(varNames.getSelectedIndices())[i];
      dataSet data = (dataSet)dataSets.elementAt(thisDataSet);
      if( !data.alpha(index) ) {
        if( data.categorical(index) )
          data.catToNum(index);
        else
          data.numToCat(index);
      }
    }
    setVarList();
    maintainPlotMenu();
  }
  
  public void getSelectedTypes() {
    numCategorical = 0;
    for( int i=0; i<varNames.getSelectedIndices().length; i++ ) {
      if( ((dataSet)dataSets.elementAt(thisDataSet)).categorical(varNames.getSelectedIndices()[i]) )
        numCategorical++;
      else
        weightIndex = varNames.getSelectedIndices()[i];
    }
  }
  
  public void maintainPlotMenu() {
    
    getSelectedTypes();
    
//    System.out.println("number categorical: "+numCategorical+", weight Index "+weightIndex);
    
    switch( (varNames.getSelectedIndices()).length ) {
      case 0:
        n.setEnabled(false);
        b.setEnabled(false);
        bw.setEnabled(false);
        nw.setEnabled(false);
        hi.setEnabled(false);
        hiw.setEnabled(false);
        pc.setEnabled(false);
        pb.setEnabled(false);
        //              sc.setEnabled(false);
        sc2.setEnabled(false);
        break;
      case 1:
        if( numCategorical == (varNames.getSelectedIndices()).length ) {
          b.setEnabled(true);
          hi.setEnabled(false);
        }
        else {
          b.setEnabled(false);
          hi.setEnabled(true);
          hiw.setEnabled(true);
        }
        n.setEnabled(false);
        bw.setEnabled(false);
        nw.setEnabled(false);
        pc.setEnabled(false);
        pb.setEnabled(true);
        //              sc.setEnabled(false);
        sc2.setEnabled(false);
        break;
      case 2: 
        if( numCategorical == (varNames.getSelectedIndices()).length ) {
          b.setEnabled(true);
          n.setEnabled(true);
        } else {
          b.setEnabled(false);
          n.setEnabled(false);
        }
        if( numCategorical == 1 ) {
          bw.setEnabled(true);
          nw.setEnabled(true);
        } else {
          bw.setEnabled(false);
          nw.setEnabled(false);
        }
        if( numCategorical == 0 ) {
          hi.setEnabled(true);
          hiw.setEnabled(true);
        } else {
          hi.setEnabled(false);
          hiw.setEnabled(false);
        }
        pc.setEnabled(true);
        pb.setEnabled(true);
        sc2.setEnabled(true);
        break;
      default:
        if( numCategorical == (varNames.getSelectedIndices()).length ) {
          b.setEnabled(true);
          n.setEnabled(true);
        } else {
          b.setEnabled(false);
          n.setEnabled(false);
        }
        if( numCategorical == (varNames.getSelectedIndices()).length - 1 ) {
          bw.setEnabled(true);
          nw.setEnabled(true);
        } else {
          bw.setEnabled(false);
          nw.setEnabled(false);
        }
        if( numCategorical == 0 ) {
          hi.setEnabled(true);
          hiw.setEnabled(true);
        } else {
          hi.setEnabled(false);
          hiw.setEnabled(false);
        }
        pc.setEnabled(true);      
        pb.setEnabled(true);
        sc2.setEnabled(false);
        //        sc.setEnabled(false);
    }
  }

  public void maintainWindowMenu(boolean preserve) {
    for( int i=0; i<Plots.size(); i++ ) 
        ((MFrame)(((DragBox)Plots.elementAt(i)).frame)).maintainMenu(preserve);
  }

  class MCellRenderer extends JLabel implements ListCellRenderer {

    final dataSet data = (dataSet)dataSets.elementAt(thisDataSet); 

    final ImageIcon alphaIcon = new ImageIcon(readGif("alpha.gif"));
    final Icon catIcon = new ImageIcon(readGif("cat.gif"));
    final Icon numIcon = new ImageIcon(readGif("num.gif"));
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(
                                                  JList list,
                                                  Object value,            // value to display
                                                  int index,               // cell index
                                                  boolean isSelected,      // is the cell selected
                                                  boolean cellHasFocus)    // the list and the cell have the focus
    {
      String s = value.toString();
      setText(s);
      if( data.alpha(index) )
        setIcon(alphaIcon);
      else if( data.categorical(index) )
        setIcon(catIcon);
      else
        setIcon(numIcon);
    
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setOpaque(true);
      return this;
    }
  }
}