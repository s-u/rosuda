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
import java.net.URL;
import java.sql.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

public class dataSet {

  protected Vector data = new Vector(256,256);
  protected Vector name = new Vector(256,256);
  protected boolean[] alpha={true};
  protected double[] selectionArray;
  protected double[] filterA;
  protected int[] filterGrpSize;
  protected int[] filterSelGrpSize;
  protected boolean groupsSet = false;
  public int filterVar = -1;
  public double filterVal;
  public int filterGrp;
  private String[] columnType={""};
  public int n=0;
  public int k=0;
  public boolean isDB;
  public String setName;
  private Driver d;
  public Connection con;
  private String DB;
  private String Table;
  public Query sqlConditions = new Query();
  public int graphicsPerf=0;
  int counter;
  boolean selChanged;

  public dataSet(String setName) {
    this.isDB = false;
    this.setName = setName;
  }

  public dataSet(Driver d, Connection con, String DB, String Table) {
    this.isDB = true;
    this.setName = Table;
    this.d = d;
    this.con = con;
    this.DB = DB;
    this.Table = Table;

    try {
      Statement stmt = con.createStatement();    
      String query = "show fields from "+Table+" from "+DB;    
      ResultSet rs = stmt.executeQuery(query);

      this.k = 0;
      while( rs.next() )
        this.k++;
      rs.close();
      stmt.close();
	  
      stmt = con.createStatement();    
      query = "show fields from "+Table+" from "+DB;    
      rs = stmt.executeQuery(query);
      
      alpha = new boolean[k];
      columnType = new String[k];
      for( int j=0; j<k; j++ ) {
        if( rs.next() ) {
          String varName = rs.getString(1);
          name.addElement(varName);
          columnType[j] = rs.getString(2);
          if( columnType[j].startsWith("varchar") ) {
            alpha[j] = true;
          }
          else
            alpha[j] = false;
          Variable Var = new Variable(alpha[j], varName);
          data.addElement(Var);
        }
      }
      rs.close();
      stmt.close();
    } catch (Exception ex) {
      System.out.println("DB Exception: get fields ... "+ex);
    }

    try {
      Statement stmt = con.createStatement();    
      String query = "select count(*) from "+Table;    
      ResultSet rs = stmt.executeQuery(query);

      if( rs.next() ) {
        this.n = (int)Util.atod(rs.getString(1));
//            selectionArray = new double[this.n];
      }
      rs.close();
      stmt.close();
    } catch (Exception ex) {
      System.out.println("DB Exception: get size ... "+ex);
    } 	   
  }
  
  public boolean[] sniff(BufferedReader br) {

    String line, dummy="";
    
    try {
      line = br.readLine();
      
      StringTokenizer head = new StringTokenizer(line, "\t");
      
      k = head.countTokens();
      
      alpha = new boolean[this.k];
      
      for( int j=0; j<this.k; j++ )
        alpha[j] = false;
      
      try{
        while( ((line = br.readLine()) != null) && !((line.trim()).equals("")) ) {
          n++;
          StringTokenizer dataLine = new StringTokenizer(line, "\t");
          for( int j=0; j<this.k; j++ ) {
            if( !alpha[j] ) {
              alpha[j] = false;
              try {
                dummy = dataLine.nextToken();
                Float fdummy = Float.valueOf(dummy);
              }
              catch( NumberFormatException e) {		
                alpha[j] = true;
              }
            }
            else
              dummy = dataLine.nextToken();
          }
        }
      }
      catch(NoSuchElementException e) {}
    }
    catch( IOException e ) {
      System.out.println("Error: "+e);
      System.exit(1);
    }
    
    selectionArray = new double[n];
    filterA = new double[n];
    
    return alpha;
  }
  
  public void read(BufferedReader br, boolean[] alpha, JProgressBar progBar) {

    Variable Var;
    String line, varName;
    String[] last = new String[k];

    try {
      line = br.readLine();
      
      StringTokenizer head = new StringTokenizer(line, "\t");
      
      for( int j=0; j<this.k; j++ ) {
        varName = head.nextToken();
        name.addElement(varName);
//System.out.println("adding Variable: "+varName);
        Var = new Variable(this.n, alpha[j], varName);
        if( varName.length() > 1 ) {
          if( varName.substring(0,2).equals("/T") ) 
            Var.phoneNumber = true;
          if( varName.substring(0,2).equals("/P") ) {
            Var.isPolyID = true;
            Var.forceCategorical = true;
          }
          if( varName.substring(0,2).equals("/C") )
            Var.isCategorical = false;
          if( varName.substring(0,2).equals("/D") )
            Var.forceCategorical = true;
        }
        data.addElement(Var);
      }
      
      try{
        double x;
        String token;
//progBar.setIndeterminate(true);  //
        progBar.setValue(0);
        for( int i=0; i<this.n; i++ ) {
          if( (i % (int)(Math.max(n/20, 1)) == 0 ) && (n > 1000) ) {
            progBar.setValue(i);
            progBar.repaint();
//System.out.println("Reading Line: "+i);
          }
          line = br.readLine();
          StringTokenizer dataLine = new StringTokenizer(line, "\t");
          for( int j=0; j<this.k; j++ ) {
            Var = (Variable)data.elementAt(j);
            if( alpha[j] )
              Var.data[i] = Var.isLevel(dataLine.nextToken().trim());
            else {
          //    token = dataLine.nextToken();
          //    if( token.equals(last[j]) )
          //      Var.data[i] = Var.data[i-1];
          //    else {
          //    Var.data[i] = Double.valueOf(token).doubleValue();
              Var.data[i] = Double.valueOf(dataLine.nextToken()).doubleValue();
                Var.isLevel( Double.toString(Var.data[i]) );
          //    }
          //    last[j] = token;
            }
          }
        }
      }
      catch(NoSuchElementException e) {}
    }
    catch( IOException e ) {
      System.out.println("Error: "+e);
      System.exit(1);
    }
    for( int i=0; i<k; i++ ) {
      Var = (Variable)data.elementAt(i);
      if( Var.isCategorical )
        Var.sortLevels();
      else
        if( !Var.alpha )
          Var.sortData();
    }
  }	

  public void numToCat(int i) {
    Variable Var=((Variable)data.elementAt(i));
    if( alpha[i] && Var.isCategorical )
      return;
    else {
      for( int l=0; l<Var.grpSize.length; l++ )
        Var.grpSize[l] = 0;
      Var.forceCategorical = true;
      Var.isCategorical = true;
      for( int j=0; j<this.n; j++ )
        Var.isLevel( Double.toString(Var.data[j]) );
      Var.sortLevels();
    }
  }
  
  public void catToNum(int i) {
    Variable Var=((Variable)data.elementAt(i));
    if( alpha[i] && !Var.isCategorical )
      return;
    else {
      Var.forceCategorical = false;
      Var.isCategorical = false;
      Var.sortData();
    }
  }
  
  public Table discretize(String name, int dvar, double start, double width, int weight) {

      int     tablelength = (int)((this.getMax(dvar) - start) / width) + 1;
      int[]          vars = new int[1];
                  vars[0] = dvar;
      double[]    bdtable = new double[tablelength];	// !
      int[]      tableDim = new int[tablelength];	// !
      String[][]   lnames = new String[1][tablelength];	// !
      double[]   datacopy = this.getRawNumbers(dvar);
      int[]     varlevels = new int[1];			// !
             varlevels[0] = tablelength;
      String[]   varnames = new String[1];		// !
              varnames[0] = this.getName(dvar);
      int[][]         Ids = new int[tablelength][];	// !
      int[]      pointers = new int[tablelength];
                int round = (int)Math.max(0, 3 - Math.round((Math.log(getMax(dvar)-getMin(dvar))/Math.log(10))));
       Query initialQuery = new Query();

      if( isDB ) {
          try {	
              Statement stmt = con.createStatement();
              Query query = new Query();
              String itemStr = "CASE ";
              String filler = "                                                                                          ";
              itemStr += "WHEN "+getName(dvar)+"<"+Stat.roundToString(start, round)+" THEN '["+filler.substring(0, tablelength)+Stat.roundToString(start, round)+", "+Stat.roundToString(start+1*width, round)+")' ";
              for( int i=0; i < tablelength; i++ ) {
                  itemStr += "WHEN "+getName(dvar)+">="+Stat.roundToString(start+i*width, round)+" AND "+getName(dvar)+"<"+Stat.roundToString(start+(i+1)*width, round)+" THEN '["+filler.substring(0, tablelength-i)+Stat.roundToString(start+i*width, round)+", "+Stat.roundToString(start+(i+1)*width, round)+")' ";
              }
              itemStr += "ELSE '["+Stat.roundToString(start+tablelength*width, round)+", "+Stat.roundToString(start+(tablelength+1)*width, round)+")' END AS category42";

//System.out.println(itemStr); 
              query.addItem(itemStr);
              query.addTable(setName);
              query.addCondition("AND", getName(dvar)+" IS NOT NULL ");
              query.addGroup("category42");
              query.addOrder("category42");
System.out.print(" Initial setup:" ); query.print();
              
              ResultSet rs = stmt.executeQuery(query.makeQuery());
              int i=0;
              while( rs.next() ) {
System.out.println(" i: "+i+" String:"+rs.getString(1).trim()+" Value: "+rs.getInt(2));                
                  lnames[0][i] = rs.getString(1).trim();
                  String tmp = lnames[0][i].substring(1,lnames[0][i].length()).trim();
                  lnames[0][i] = "["+tmp.substring(0, tmp.indexOf(','))+", "+tmp.substring(tmp.indexOf(',')+1, tmp.length()).trim();
                  bdtable[i] = rs.getInt(2);
                  i++;
              }
              rs.close();
              stmt.close();
              initialQuery = query;
          } catch (Exception ex) {
            System.out.println("DB Exception: get histo breakdown ... "+ex);
          }
      } else {
        if( weight == -1 )
          for( int i=0; i<this.n; i++ ) {
            int index = (int)((datacopy[i]-start)/width);
            bdtable[index]++;
            tableDim[index]++;
          } else {
          double[] weights;
          weights = getRawNumbers(weight);

          for( int i=0; i<this.n; i++ ) {
            int index = (int)((datacopy[i]-start)/width);
            bdtable[index] += weights[i];
            tableDim[index]++;
          }
        }
      }

      for( int i=0; i<tablelength; i++ ) {
          if( !isDB ) {
              lnames[0][i] = "["+Stat.roundToString(start + i*width, round)+", "+Stat.roundToString(start + (i+1)*width, round)+")";
              Ids[i] = new int[(int)tableDim[i]];
          } else
              Ids[i] = new int[1];
          pointers[i] = 0;
      }

      int index=0;

      if( !isDB ) 
          for( int i=0; i<this.n; i++ ) {
              index = (int)((datacopy[i]-start)/width); 
              Ids[index][pointers[index]++] = i; 
          }
       else 
          for( int i=0; i<tablelength; i++ )
              Ids[i][0] = i;

      Table tmpTable = new Table(name, bdtable, 1, varlevels, varnames, lnames, vars, Ids, this, weight);
      tmpTable.initialQuery = initialQuery;
      return (tmpTable);
  }

  public Table discretize2D(String name, int xVar, double xStart, double xEnd, int nX, int yVar, double yStart, double yEnd, int nY) {

      int           x_num = nX;
      int           y_num = nY;
      double 	   xWidth = (xEnd - xStart) / nX;
      double 	   yWidth = (yEnd - yStart) / nY;
//System.out.println("x: "+x_num+"y: "+y_num);
      int     tablelength = x_num * y_num;
      int[]          vars = new int[2];
      vars[0]             = xVar;
      vars[1]             = yVar;
      double[]    bdtable = new double[tablelength];	// !
      String[][]   lnames = new String[2][];	// !
                lnames[0] = new String[x_num];
                lnames[1] = new String[y_num];
      double[]   datacopyX = this.getRawNumbers(xVar);
      double[]   datacopyY = this.getRawNumbers(yVar);
      int[]     varlevels = new int[2];			// !
      varlevels[0] = x_num;
      varlevels[1] = y_num;
      String[]   varnames = new String[2];		// !
      varnames[0] = this.getName(xVar);
      varnames[1] = this.getName(yVar);
      int[][]         Ids = new int[tablelength][];	// !
      int[]      pointers = new int[tablelength];
      int roundX = (int)Math.max(0, 3 - Math.round((Math.log(xEnd-xStart)/Math.log(10))));
      int roundY = (int)Math.max(0, 3 - Math.round((Math.log(yEnd-yStart)/Math.log(10))));
      Query initialQuery = new Query();

      if( isDB ) {
          System.out.println("DB not yet implemented");
      } else {
          for( int i=0; i<this.n; i++ )
              if( datacopyX[i] < xEnd && datacopyX[i] >= xStart && datacopyY[i] < yEnd && datacopyY[i] >= yStart )
                  bdtable[(int)((datacopyX[i]-xStart)/xWidth) * y_num + (int)((datacopyY[i]-yStart)/yWidth)]++; 
      }

      for( int i=0; i<x_num; i++ ) {
          if( !isDB ) {
              lnames[0][i] = "["+Stat.roundToString(xStart + i*xWidth, roundX)+", "+Stat.roundToString(xStart + (i+1)*xWidth, roundX)+")";
          }
      }
      for( int i=0; i<y_num; i++ ) {
          if( !isDB ) {
              lnames[1][i] = "["+Stat.roundToString(yStart + i*yWidth, roundY)+", "+Stat.roundToString(yStart + (i+1)*yWidth, roundY)+")";
          }
      }
      for( int i=0; i<tablelength; i++ ) {
          if( !isDB ) {      
              Ids[i] = new int[(int)bdtable[i]];
          } else
              Ids[i] = new int[1];
          pointers[i] = 0;
      }
      
      int index=0;
      if( !isDB ) 
          for( int i=0; i<this.n; i++ ) {
              if( datacopyX[i] < xEnd && datacopyX[i] >= xStart && datacopyY[i] < yEnd && datacopyY[i] >= yStart ) {
                  index = (int)((datacopyX[i]-xStart)/xWidth) * y_num + (int)((datacopyY[i]-yStart)/yWidth); 
                  Ids[index][pointers[index]++] = i;
              }
          }
              else 
                  for( int i=0; i<tablelength; i++ )
                      Ids[i][0] = i;

      Table tmpTable = new Table(name, bdtable, 2, varlevels, varnames, lnames, vars, Ids, this, -1);
      tmpTable.initialQuery = initialQuery;
      return (tmpTable);
  }

  public Table breakDown(String name, int[] vars, int count) {

    int     tablelength = 1;
    int           index;
    double[]    bdtable;
    String[][]   lnames = new String[vars.length][];
    double[][] datacopy = new double[vars.length][];
    int[]     varlevels = new int[vars.length];
    String[]   varnames = new String[vars.length];
    int[]       plevels = new int[vars.length];
    String        query = "";    
    int[][]         Ids;
    int[]          dimA;
    Query	newQ = new Query();

    for( int j=0; j<vars.length; j++ ) {
      varlevels[j] = getNumLevels(vars[j]);
      tablelength *= varlevels[j];
      varnames[j] = this.getName(vars[j]);
      lnames[j] = this.getLevels(vars[j]);
      if( isDB ) {
        newQ.addItem(varnames[j]);
        newQ.addGroup(varnames[j]);
        newQ.addOrder(varnames[j]);
      } else
        datacopy[j] = this.getNumbers(vars[j]);
    }
    plevels[vars.length-1] = 1;
    for( int j=vars.length-2; j>=0; j-- ) {
      plevels[j] = varlevels[j+1] * plevels[j+1];
    }
    for( int j=0; j<vars.length; j++ ) {
//      System.out.println("Tablelength: "+tablelength+"  Name: "+ varnames[j]+"  Levels: "+lnames[j][0]+"..."+"   Plevels: "+plevels[j]);
    }
    Ids = new int[tablelength][];
    dimA = new int[tablelength];

    bdtable = new double[tablelength];

    if( isDB ) {
      try {
        newQ.addTable(Table);
System.out.println(newQ.makeQuery());
        Statement stmt = con.createStatement();    
        ResultSet rs = stmt.executeQuery(newQ.makeQuery());
	
        while( rs.next() ) {
          index = 0;
          for( int j=0; j<vars.length; j++ )
            index += plevels[j] * ((Variable)data.elementAt(vars[j])).Level((rs.getString(j+1)).trim());
          bdtable[index] = rs.getInt(vars.length+1);
        }
        rs.close();
        stmt.close();
      } catch (Exception ex) {
        System.out.println("DB Exception: "+ex);
      }      
      for( int j=0; j<tablelength; j++ ) {
        Ids[j] = new int[1];
        Ids[j][0] = j;
      }
    }	
    else {
      for( int i=0; i<this.n; i++ ) {
        index = 0;
        for( int j=0; j<vars.length; j++ )
          index += plevels[j] * datacopy[j][i];

//System.out.println("Index: "+index);

        if( count == -1 )
          bdtable[index]++;
        else {
          dimA[index]++;
          bdtable[index] += (this.getRawNumbers(count))[i];	
        }
      }

      for( int j=0; j<tablelength; j++ ) {
        if( count == -1 )
          Ids[j] = new int[(int)bdtable[j]];
        else
          Ids[j] = new int[dimA[j]];
        if( Ids[j].length > 0 )
          Ids[j][0] =-1;
      }

      int[] topIndex = new int[tablelength];
      for( int j=0; j<tablelength; j++)
        topIndex[j] = 0;
        
      for( int i=0; i<this.n; i++ ) {
        index = 0;
        for( int j=0; j<vars.length; j++ )
          index += plevels[j] * datacopy[j][i];
        Ids[index][topIndex[index]++] = i;
      }
    }
//System.out.println("Tablelength: "+tablelength+"  Name: "+ varnames[0]+"  Levels: "+bdtable[0]+"... "+bdtable[1]+"... "+bdtable[2]+"... "+bdtable[3]+"... "+"   Plevels: "+plevels[0]);
    Table tmpTable = new Table(name, bdtable, vars.length, varlevels, varnames, lnames, vars, Ids, this, count);
    tmpTable.initialQuery = newQ;
    return (tmpTable);
  }

  public double[] regress(int k, int l) {

    double Sxx, Sxy, a, b, r2;
    double sumx = 0;
    double sumy = 0;
    double sumxx = 0;
    double sumxy = 0;
    double sumyy = 0;

    double[] x = this.getRawNumbers(k);
    double[] y = this.getRawNumbers(l);

    for( int i=0; i<this.n; i++ ) {
      sumx += x[i];
      sumy += y[i];
      sumxx += x[i]*x[i];
      sumyy += y[i]*y[i];
      sumxy += x[i]*y[i];
    }

    Sxx = sumxx - sumx * sumx / this.n;
    Sxy = sumxy - sumx * sumy / this.n;
    b = Sxy/Sxx;
    a = (sumy - b * sumx) / this.n;
    r2 = b * (this.n * sumxy - sumx * sumy) / (this.n * sumyy - sumy * sumy);

    //System.out.println("f(x) = "+b+" * x + "+a);

    return new double[] {a, b, r2};
  }

  public double[] selRegress(int k, int l) {

    if( this.countSelection() < 2 )
      return new double[] {0, 0, 0};
      
    double Sxx, Sxy, a, b, r2;
    double sumx = 0;
    double sumy = 0;
    double sumxx = 0;
    double sumxy = 0;
    double sumyy = 0;

    double[] x = this.getRawNumbers(k);
    double[] y = this.getRawNumbers(l);

    for( int i=0; i<this.n; i++ ) {
      if(selectionArray[i] > 0 ) {
        sumx += x[i];
        sumy += y[i];
        sumxx += x[i]*x[i];
        sumyy += y[i]*y[i];
        sumxy += x[i]*y[i];
      }
    }

    Sxx = sumxx - sumx * sumx / this.countSelection();
    Sxy = sumxy - sumx * sumy / this.countSelection();
    b = Sxy/Sxx;
    a = (sumy - b * sumx) / this.countSelection();
    r2 = b * (this.countSelection() * sumxy - sumx * sumy) / (this.countSelection() * sumyy - sumy * sumy);

    return new double[] {a, b, r2};
  }

  public boolean alpha(int i) {
    return alpha[i];
  }

  public boolean categorical(int i) {
    return ((Variable)data.elementAt(i)).isCategorical;
  }

  public boolean phoneNumber(int i) {
    return ((Variable)data.elementAt(i)).phoneNumber;
  }

  public boolean isPolyID(int i) {
    return ((Variable)data.elementAt(i)).isPolyID;
  }

  public String getName(int i) {
    String retName = (String)name.elementAt(i);
    if( retName.length()>1 && retName.substring(0,1).equals("/") )
      return retName.substring(2);
    else
      return retName;
  }

  public int getNumLevels(int i) {
    return ((Variable)data.elementAt(i)).getNumLevels();
  }

  public String[] getLevels(int i) {
    return ((Variable)data.elementAt(i)).getLevels();
  }

  public String getLevelName(int i, double val) {
    Variable v = (Variable)data.elementAt(i);
    String[] LevelString = v.getLevels();
    if( alpha[i] )
      return LevelString[(int)val];
    else
      return LevelString[v.IpermA[(int)v.Level( Double.toString(val) )]];
  }

  public double[] getNumbers(int i) {
    Variable v = (Variable)data.elementAt(i);
    if( !v.isCategorical )
      return v.data;
    else {
      if( v.IpermA == null )
        v.sortLevels();
      double[] retA = new double[this.n];
      if( alpha[i] ) {
        for( int j=0; j<this.n; j++ )
          retA[j] = (double)v.IpermA[(int)v.data[j]];
      }
      else {
        for( int j=0; j<this.n; j++ )
          retA[j] = (double)v.IpermA[(int)v.Level( Double.toString(v.data[j]) )];
      }
      return retA;
    }
  }

  public double[] getRawNumbers(int i) {
    Variable v = (Variable)data.elementAt(i);
    return v.data;
  }

  public int[] getRank(int i) {
    Variable v = (Variable)data.elementAt(i);
    int[] ranks = new int[this.n];
    if( !categorical(i) )
      for( int j=0; j<this.n; j++ )
        ranks[v.sortI[j]] = j;
    else
      for( int j=0; j<this.n; j++ )
        ranks[j] = v.permA[(int)v.data[j]];
    return ranks;
  }

  public double getMin(int i) {
    return ((Variable)data.elementAt(i)).Min();
  }

  public double getSelMin(int i) {
    return ((Variable)data.elementAt(i)).SelMin();
  }

  public double getMax(int i) {
    return ((Variable)data.elementAt(i)).Max();
  }

  public double getSelMax(int i) {
    return ((Variable)data.elementAt(i)).SelMax();
  }

  public double getMean(int i) {
    return ((Variable)data.elementAt(i)).Mean();
  }

  public double getSelMean(int i) {
    return ((Variable)data.elementAt(i)).selMean();
  }

  public double getSDev(int i) {
    return ((Variable)data.elementAt(i)).SDev();
  }

  public double getSelSDev(int i) {
    return ((Variable)data.elementAt(i)).selSDev();
  }

  public double[] getSelection() {
    return selectionArray;
  }

  public double getSelected(int i) {
    return selectionArray[i];
  }

  public void setSelection(int i, double s, int mode) {
    if( filterVar != -1 && filterA[i] != filterVal )
      s = 0;
//      if( s == 0 )
//        selectionArray[i] = 0;
//      else
//        return;
//    else {	
//    if( (filterVar != -1 && filterA[i] == filterVal) || filterVar == -1 ) {
      selChanged = true;
      switch( mode ) {
        case Selection.MODE_STANDARD:
          selectionArray[i] = s;
//System.out.println("REPLACE at: "+i+" with: "+s);
          break;
        case Selection.MODE_AND:
//System.out.print("AND at: "+i+" from: "+selectionArray[i]);
          selectionArray[i] *= s;
//System.out.println(" to: "+selectionArray[i]);
          break;
        case Selection.MODE_OR:
          selectionArray[i] = Math.max( s, selectionArray[i]);
          break;
        case Selection.MODE_XOR:
          if( s > 0 )
            if( selectionArray[i] >0 )
              selectionArray[i] = 0;
            else
              selectionArray[i] = s;
          break;
        case Selection.MODE_NOT:
          if( s > 0 )
            selectionArray[i] = 0;
//      }
    }
  }

  public void setFilter( int var, String grp ) {

    for( int i=0; i<this.n; i++ ) {
      filterA[i] = (((Variable)data.elementAt(var)).data)[i];
    }

    filterGrp = (int)(((Variable)data.elementAt(var)).Level(grp));
    if( (((Variable)data.elementAt(var)).alpha) )
      filterVal = filterGrp;
    else
      filterVal = Util.atod(grp);
    filterVar = var;
    filterGrpSize = new int[((Variable)data.elementAt(var)).getNumLevels()];
    filterSelGrpSize = new int[((Variable)data.elementAt(var)).getNumLevels()];
    for( int i=0; i<filterGrpSize.length; i++ ) {
      filterGrpSize[i] = (((Variable)data.elementAt(var)).grpSize)[i];
      //System.out.println("i: "+i+"GrpSize: "+filterGrpSize[i]+" unsort: "+(((Variable)data.elementAt(var)).grpSize)[i]);
    }
    if( (((Variable)data.elementAt(var)).alpha) ) { 
      for( int i=0; i<n; i++ )
        if( selectionArray[i] > 0 )
          filterSelGrpSize[(int)(((Variable)data.elementAt(var)).data[i])]++;
    } else
      for( int i=0; i<n; i++ )
        if( selectionArray[i] > 0 )
          filterSelGrpSize[(int)(((Variable)data.elementAt(var)).Level(""+(((Variable)data.elementAt(var)).data)[i]))]++;
  }

  public void resetFilter() {
      filterVar = -1;
      filterVal = 0;
      filterGrp = -1;
  }

  public void selectAll() {
      for( int i=0; i<this.n; i++ )
          setSelection(i, 1, Selection.MODE_STANDARD );
  }
      
  public int countSelection() {
      if( this.isDB ) {
          if( sqlConditions.getConditions().equals("") )
              return 0;
          else {
              try {
                  Statement stmt = con.createStatement();
                  String query = "SELECT COUNT(*) FROM "+setName+" WHERE "+sqlConditions.getConditions();
                  ResultSet rs = stmt.executeQuery(query);
                  rs.next();
                  int returner=rs.getInt(1);
                  rs.close();
                  stmt.close();
                  return returner;
              } catch (Exception ex) {
                  System.out.println("DB Exception: get num hilited ... "+ex);
              }
              return 0;
          }
      } else {
          if( selChanged ) {
              counter = 0;
              for( int i=0; i<this.n; i++ )
                  if( selectionArray[i] > 0 )
                      counter++;
              selChanged = false;
              return counter;
          }
          else {
              return counter;
          }
      }
  }
  
  public void clearSelection() {
    selectionArray = new double[n];
  }
  
  public double getQuantile(int i, double q) {
    return ((Variable)data.elementAt(i)).getQuantile(q);   
  }
  
  public double getSelQuantile(int i, double q) {
    return ((Variable)data.elementAt(i)).getSelQuantile(q);   
  }
  
  public double getFirstGreater(int i, double q) {
    return ((Variable)data.elementAt(i)).getFirstGreater(q);   
  }
  
  public double getFirstSelGreater(int i, double q) {
    return ((Variable)data.elementAt(i)).getFirstSelGreater(q);   
  }
  
  public double getFirstSmaller(int i, double q) {
    return ((Variable)data.elementAt(i)).getFirstSmaller(q);   
  }
  
  public double getFirstSelSmaller(int i, double q) {
    return ((Variable)data.elementAt(i)).getFirstSelSmaller(q);   
  }
  
  public double[] getAllSmaller(int i, double q) {
    return ((Variable)data.elementAt(i)).getAllSmaller(q);   
  }
  
  public double[] getAllSelSmaller(int i, double q) {
    return ((Variable)data.elementAt(i)).getAllSelSmaller(q);   
  }
  
  public double[] getAllGreater(int i, double q) {
    return ((Variable)data.elementAt(i)).getAllGreater(q);   
  }
  
  public double[] getAllSelGreater(int i, double q) {
    return ((Variable)data.elementAt(i)).getAllSelGreater(q);   
  }
    
  class Variable {
    private int catThres = (n>800)?(15 * Math.max(1, (int)(Math.log(n)/Math.log(10))-1)):((int)(1.5*Math.sqrt(n)));
    protected Vector level = new Vector(100, 100);
    private int dimThres = 17000;
    protected String[] levelA = new String[dimThres];
    protected int[] grpSize = new int[dimThres];
    protected int[] permA;
    protected int[] IpermA;
    protected int levelP = 0;
    protected boolean alpha;
    public boolean isCategorical = true;
    public boolean forceCategorical = false;
    public boolean phoneNumber = false;
    public boolean isPolyID = false;
    private String name;
    public double[] data;
    public int[] sortI;
    public double min=1e+100, max=-1e+100;
    protected boolean minSet=false, maxSet=false, levelsSet=false;
    
    Variable(boolean alpha, String name) {
      this.alpha = alpha;
      this.name = name;
      if( name.substring(0,2).equals("/P") )
        isCategorical = false;
    }

    Variable(int n, boolean alpha, String name) {
      this.alpha = alpha;
      this.name = name;
      if( name.length() > 1 )
        if( name.substring(0,2).equals("/P") )
          isCategorical = false;
      data = new double[n];
    }

    public double isLevel(String name) {
      if( !levelsSet && isDB )
        maintainDBVariable();
      if( isCategorical ) {
        for( int i=0; i< levelP; i++) {
          if( levelA[i].equals(name) ) {
            grpSize[i]++;
            return i;
          }	
        }
        grpSize[levelP]++;
        levelA[ this.levelP++ ] = name;	
        if( ( this.levelP >=catThres || this.levelP > dimThres-2 ) && !forceCategorical && !alpha)
          isCategorical = false;	
        return this.levelP-1;
      }
      else {
        isCategorical = false;
        return -1;
      }
    }    

    public double Level(String name) {
      if( !levelsSet && isDB )
        maintainDBVariable();
      if( isCategorical ) {
        if( IpermA == null )
          sortLevels();
        for( int i=0; i< levelP; i++)
          if(	 levelA[i].equals(name) ) {
            return i; //permA[i];
          }
            return 3.1415926;
      }
      else {
        return -1;
      }
    }    
    
    public int getNumLevels() {
      if( !levelsSet && isDB )
        maintainDBVariable();
      return levelP;
    }
    
    public String[] getLevels() {
      String[] returnA = new String[levelP];
      for( int i=0; i<levelP; i++ ) {
        returnA[i] = levelA[permA[i]];
      }
      return returnA;
    }

    public String getLevel(int id) {
          return levelA[permA[id]];
    }
    
    public void sortData() {

      System.out.println("--------- Real Sort --------: "+name);
      double[] sA = new double[n];
      System.arraycopy(data, 0, sA, 0, n);
      sortI = Qsort.qsort(sA, 0, n-1);
    }

    public void sortLevels() {
      System.out.println("------ Discret Sort --------: "+name);
      if( !alpha ) {

        double[] ss = new double[levelP];
        for( int i=0; i<levelP; i++ ) {
//System.out.println( Double.valueOf( levelA[i] ).doubleValue() );
          ss[i] = Double.valueOf( levelA[i] ).doubleValue();
        }
        permA = Qsort.qsort(ss, 0, levelP-1);
      }
      else {

        String[] sA = new String[levelP];
        for( int i=0; i<levelP; i++ ) 
          sA[i] = levelA[i];
        permA = Qsort.qsort(sA, 0, levelP-1);
      }
      IpermA = new int[levelP];
      for( int i=0; i<levelP; i++ ) {
        IpermA[permA[i]] = i;
      }	
    }

    public int getGroupSize( int grp ) {
      System.out.println( "grp: "+grp+" grpSize: "+getNumLevels() );
      return grpSize[grp];
    }
    
    public double Min() {
        if( !minSet )
            if( isDB ) {
                try {
                    Statement stmt = con.createStatement();    
                    String query = "select min("+name+") from "+Table;    
                    ResultSet rs = stmt.executeQuery(query);

                    if( rs.next() ) 
                        this.min = Util.atod(rs.getString(1));
                    rs.close();
                    stmt.close();
System.out.println("query: "+query+" ---> "+this.min);                    
                } catch (Exception ex) {
                    System.out.println("DB Exception: get min ... "+ex);
                }                
            } else
                for ( int i=0; i<data.length; i++ ) 
                    this.min = Math.min(data[i], this.min);
        minSet = true;
        return this.min;
    }
    
    public double SelMin() {
      double SM = Double.MAX_VALUE;
      for ( int i=0; i<data.length; i++ )
        if( selectionArray[i] > 0 )
          SM = Math.min(SM, data[i]);
      return SM;
    }

    public double Max() {
        if( !maxSet )
            if( isDB ) {
                try {
                    Statement stmt = con.createStatement();    
                    String query = "select max("+name+") from "+Table;
                    ResultSet rs = stmt.executeQuery(query);

                    if( rs.next() ) 
                        this.max = Util.atod(rs.getString(1));
                    rs.close();
                    stmt.close();
System.out.println("query: "+query+" ---> "+this.max);                    
                } catch (Exception ex) {
                    System.out.println("DB Exception: get max ... "+ex);
                }
            } else
                for ( int i=0; i<data.length; i++ ) 
                    this.max = Math.max(data[i], this.max);
        maxSet = true;
        return this.max;
    }

    public double SelMax() {
      double SM = Double.MIN_VALUE;
      for ( int i=0; i<data.length; i++ )
        if( selectionArray[i] > 0 )
          SM = Math.max(SM, data[i]);
      return SM;
    }

    public double Mean() {
      double sum=0;
      for ( int i=0; i<data.length; i++ ) 
        sum += data[i];
      return sum/data.length;
    }

    public double selMean() {
      double sum=0;
      int counter=0;
      for ( int i=0; i<data.length; i++ )
        if( selectionArray[i] > 0 ) {
          sum += data[i];
          counter++;
        }
      return sum/counter;
    }

    public double SDev() {
      double sum2=0;
      for ( int i=0; i<data.length; i++ ) 
        sum2 += data[i] * data[i];
      return Math.pow((sum2 - Math.pow(Mean(),2) * data.length) / (data.length - 1), 0.5);
    }

    public double selSDev() {
      double sum2=0;
      int counter=0;
      for ( int i=0; i<data.length; i++ )
        if( selectionArray[i] > 0 ) {
          sum2 += data[i] * data[i];
          counter++;
        }
      return Math.pow((sum2 - Math.pow(selMean(),2) * counter) / (counter - 1), 0.5);
    }

    public double getQuantile(double q) {
      if( filterVar == -1 )
        if( !isCategorical ) {
          return data[sortI[(int)((n-1)*q)]];
        } else
          return 0;
      else {
        int count=0;
        int i=0;
        if( q==0 ) {
          while( filterA[sortI[i++]] != filterVal ) {
            //System.out.println("filter Val: "+filterVal+" filterVar: "+filterVar+" i:"+i+" - "+filterA[sortI[i]]);
          }
            return data[sortI[i-1]];
        }
        if( q==1 ) {
          i=n-1;
          while( filterA[sortI[i--]] != filterVal ) {}
            return data[sortI[i+1]];
        }
        System.out.println("filterGrp: "+filterGrp+" filterGrps: "+filterGrpSize.length);
        int stop = (int)(q * (filterGrpSize[filterGrp]-1));
        System.out.println("filter Val: "+filterVal+" filterVar: "+filterVar+" GroupSize. "+filterGrpSize[filterGrp]);
        while( count <= stop )
          if( filterA[sortI[i++]] == filterVal ) {
            count++;
            //System.out.println("i: "+i+" filter Val: "+filterVal+" testVal: "+filterA[sortI[i]]+" GroupSize. "+filterGrpSize[filterGrp]);
          }
            return data[sortI[i-1]];
      }
    }

    public double getSelQuantile(double q) {
      int count=0;
      int i=0;
      if( filterVar == -1 ) {
        if( q==0 ) {
          while( selectionArray[sortI[i++]] == 0 ) {}
          return data[sortI[i-1]];
        }
        if( q==1 ) {
          i=n-1;
          while( selectionArray[sortI[i--]] == 0 ) {}
          return data[sortI[i+1]];
        }
        int stop = (int)(q * (countSelection()-1));
        while( count <= stop )
          if( selectionArray[sortI[i++]] > 0 )
            count++;
        return data[sortI[i-1]];
      } else {
        if( q==0 ) {
          while( (selectionArray[sortI[i]] == 0) || (filterA[sortI[i]] != filterVal) ) {i++;/*System.out.println("i: "+i+" "+selectionArray[sortI[i]]+" "+filterA[sortI[i]]+" "+filterVal);*/}
          return data[sortI[i]];
        }
        if( q==1 ) {
          i=n-1;
          while( selectionArray[sortI[i]] == 0 || filterA[sortI[i]] != filterVal) {i--;/*System.out.println("i: "+i);*/}
          return data[sortI[i]];
        }
        int stop = (int)(q * (filterSelGrpSize[filterGrp]-1));
//System.out.println("in q grpSize: "+filterSelGrpSize[filterGrp]);
        while( count <= stop ) {
          if( selectionArray[sortI[i]] > 0 && filterA[sortI[i]] == filterVal) 
            count++;
          i++;
        }
        return data[sortI[i-1]];
      }
    }
    
    public double getFirstGreater(double g) {
      int i=0;
      if( filterVar == -1 ) {
        double ret=data[sortI[i]];
        while( (ret=data[sortI[i]]) < g )
          i++;
        return ret;
      } else {
        while( i<n-1 && (data[sortI[i]]) < g )
          i++;
        while( i<n-1 && filterA[sortI[i]] != filterVal )
          i++;
        return data[sortI[i]];
      }
    }

    public double getFirstSelGreater(double g) {
      int i=0;
      if( filterVar == -1 ) {
        while( i<n-1 && (data[sortI[i]]) < g )
          i++;
        while( i<n-1 && selectionArray[sortI[i]] == 0 )
          i++;
        return data[sortI[i]];
      } else {
        while( i<n-1 && (data[sortI[i]]) < g )
          i++;
        while( i<n-1 && (selectionArray[sortI[i]] == 0 || filterA[sortI[i]] != filterVal) )
          i++;
        return data[sortI[i]];
      }
    }

    public double getFirstSmaller(double s) {
      int i=n-1;
      if( filterVar == -1 ) {
        double ret=data[sortI[i]];
        while( (ret=data[sortI[i]]) > s )
          i--;
        return ret;
      } else {
        while( i>0 && (data[sortI[i]]) > s )
          i--;
        while( i>0 && filterA[sortI[i]] != filterVal )
          i--;
        return data[sortI[i]];
      }
    }

    public double getFirstSelSmaller(double s) {
      int i=n-1;
      if( filterVar == -1 ) {
        while( i>0 && (data[sortI[i]]) > s )
          i--;
        while( i>0 && selectionArray[sortI[i]] == 0 )
          i--;
        return data[sortI[i]];
      } else {
        while( i>0 && (data[sortI[i]]) > s )
          i--;
        while( i>0 && (selectionArray[sortI[i]] == 0 || filterA[sortI[i]] != filterVal) )
          i--;
        return data[sortI[i]];
      }
    }

    public double[] getAllSmaller(double s) {
      int i=0;
      if( filterVar == -1 ) {
        while( (data[sortI[i++]]) < s ) {}
        double[] ret = new double[i-1];
        for( int j=0; j<i-1; j++)
          ret[j] = data[sortI[j]];
        return ret;
      } else {
        int count=0;
        while( i<n && data[sortI[i]] < s )
          if( filterA[sortI[i++]] == filterVal )
            count++;
        //while( i<n && filterA[sortI[i++]] != filterVal ) {}
        //count++;
        if( count > 0 ) {
          double[] ret = new double[count];
          count=0;
          for( int j=0; j<i; j++)
            if( filterA[sortI[j]] == filterVal )
              ret[count++] = data[sortI[j]];
          return ret;
        } 
        else {
          return new double[0];
        }
      }
    }

    public double[] getAllSelSmaller(double s) {
      int i=0;
      int count=0;
      if( filterVar == -1 ) {
        while( i<n && data[sortI[i]] <= s )
          if( selectionArray[sortI[i++]] > 0 )
            count++;
        while( i<n && selectionArray[sortI[i++]] == 0 ) {}
        count++;
        if( count > 0 ) {
          double[] ret = new double[count];
          count=0;
          for( int j=0; j<i; j++)
            if( selectionArray[sortI[j]] > 0 )
              ret[count++] = data[sortI[j]];
          return ret;
        } 
        else {
          return new double[0];
        }
      } else {
        while( i<n && data[sortI[i]] < s ) {
          if( filterA[sortI[i]] == filterVal && selectionArray[sortI[i]] > 0) 
            count++;
            i++;
          }
        if( count > 0 ) {
          double[] ret = new double[count];
          count=0;
          for( int j=0; j<i; j++)
            if( filterA[sortI[j]] == filterVal && selectionArray[sortI[j]] > 0)
              ret[count++] = data[sortI[j]];
          return ret;
        } 
        else {
          return new double[0];
        }
      }
    }

  public double[] getAllGreater(double g) {
    int i=n-1;
    if( filterVar == -1 ) {
      while( (data[sortI[i--]]) > g ) {}
      double[] ret = new double[n-i-2];
      for( int j=n-1; j>i+1; j--)
        ret[n-j-1] = data[sortI[j]];
      return ret;
    } else {
      int count=0;
      while( i>=0 && (data[sortI[i]]) > g )
        if( filterA[sortI[i--]] == filterVal )
          count++;
      if( count > 0 ) {
        double[] ret = new double[count];
        count=0;
        for( int j=n-1; j>i; j--)
          if( filterA[sortI[j]] == filterVal )
            ret[count++] = data[sortI[j]];
        return ret;
      } 
      else {
        return new double[0];
      }
    }
  }

  public double[] getAllSelGreater(double g) {
    int i=n-1;
    int count=0;
    if( filterVar == -1 ) {
      while( i>=0 && (data[sortI[i]]) >= g )
        if( selectionArray[sortI[i--]] > 0 )
          count++;
      while( i>=0 && selectionArray[sortI[i--]] == 0 ) {}
      count++;
      if( count > 0 ) {
        double[] ret = new double[count];
        count=0;
        for( int j=n-1; j>i; j--)
          if( selectionArray[sortI[j]] > 0 )
            ret[count++] = data[sortI[j]];
        return ret;
      } 
      else {
        return new double[0];
      }
    } else {
      while( i>=0 && (data[sortI[i]]) > g ) {
        if( filterA[sortI[i]] == filterVal && selectionArray[sortI[i]] > 0 )
          count++;
        i--;
      }
      if( count > 0 ) {
        double[] ret = new double[count];
        count=0;
        for( int j=n-1; j>i; j--)
          if( filterA[sortI[j]] == filterVal && selectionArray[sortI[j]] > 0)
            ret[count++] = data[sortI[j]];
        return ret;
      } 
      else {
        return new double[0];
      }
    }
  }

    void maintainDBVariable() {
      try {
        levelP = 0;
        Statement stmt = con.createStatement();    
        String query = "select "+name+" from "+Table+" where "+name+" is not null group by "+name+ " order by trim(" + name +")";    
        System.out.println("Processing: "+name+" "+query);
        ResultSet rs = stmt.executeQuery(query);
	
        while( rs.next() ) {
          System.out.println("Level: "+(rs.getString(1)).trim());
          levelA[ levelP++ ] = (rs.getString(1)).trim();
        }
        permA = new int[levelP];
        IpermA = new int[levelP];
        for( int i=0; i<levelP; i++ ) {
          permA[i] = i;
          IpermA[permA[i]] = i;
        }
	
        rs.close();
        stmt.close();	
        levelsSet = true;
      } catch (Exception ex) {
        System.out.println("DB Exception in Maintain: "+ex);
      }
    }
  }  
}
