import java.awt.*;              
import java.awt.image.*;       
import java.text.*;       
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Util {

  public static void add(JFrame f, Component c, GridBagConstraints gbc, int x, int y, int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    f.getContentPane().add(c, gbc);
  }

  public static String toPhoneNumber(double d) {

    DecimalFormat phoneN = new DecimalFormat("0000000000");
    String tmp = phoneN.format((long)d);
    return (tmp.substring(0,3)+"-"+tmp.substring(3,6)+"-"+tmp.substring(6,10));
  }

  public static double atod(String a) {
    return Double.valueOf(a).doubleValue();
  }

  /**
  * Reallocates an array with a new size, and copies the contents
  * of the old array to the new array.
  * @param oldArray  the old array, to be reallocated.
  * @param newSize   the new array size.
  * @return          A new array with the same contents.
  */
  public static Object resizeArray (Object oldArray, int newSize) {
    int oldSize = java.lang.reflect.Array.getLength(oldArray);
    Class elementType = oldArray.getClass().getComponentType();
    Object newArray = java.lang.reflect.Array.newInstance(elementType,newSize);
    int preserveLength = Math.min(oldSize,newSize);
    if (preserveLength > 0)
      System.arraycopy (oldArray,0,newArray,0,preserveLength);
    return newArray; 
  }

  public static Image makeColorTransparent (Image im, final Color color) {
    ImageFilter filter = new RGBImageFilter() {
      // the color we are looking for... Alpha bits are set to opaque
      public int markerRGB = color.getRGB() | 0xFF000000;

      public final int filterRGB(int x, int y, int rgb) {
        if ( ( rgb | 0xFF000000 ) == markerRGB ) {
          // Mark the alpha bits as zero - transparent
          return 0x00FFFFFF & rgb;
        }
        else {
          // nothing to do
          return rgb;
        }
      }
    };

    ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
    return Toolkit.getDefaultToolkit().createImage(ip);
  }

  public static String color2hrgb(Color c) {
    int i=(c.getRed()<<16)|(c.getGreen()<<8)|(c.getBlue());
    String s=Integer.toHexString(i);
    while (s.length()<6) { s="0"+s; };
    return "#"+s;
  }

  public static Color hrgb2color(String s) {
    if (s!=null && s.length()>0 && s.charAt(0)=='#') {
      int c=Util.parseHexInt(s.substring(1));
      return new Color((c>>16)&255,(c>>8)&255,c&255);
    }
    return null;
  }

  public static int parseHexInt(String s) {
    int i=0;
    try {
      i=Integer.parseInt(s,16);
    } catch(Exception dce) {};
    return i;
  }
  
  public static int[] roundProportions(double[] votes, double total, int pie) {

    int[] rounds = new int[votes.length];

    int start = -1; 
    int stop  = votes.length;
    while( votes[++start] == 0 ) {}
    while( votes[--stop]  == 0 ) {}
//    System.out.println("Start: "+start+" Stop: "+stop);
    int k=1;
    double eps=0;
    int sum=0;
    int converge=24;
    while( sum != pie && k<64) {
      k++;
      sum=0;
      for(int i=start; i<=stop; i++) {
        if( k>=converge )
          eps = Math.random() - 0.5;
        if( votes[i] < 0.0000000001 )
          rounds[i] = 0;
        else
          rounds[i] = (int)Math.round((double)(votes[i])/total*pie + eps);
        sum += rounds[i];
      }
      //System.out.println("k: "+k+" eps: "+eps+" sum: "+sum+" pie: "+pie);
      if( sum > pie )
        eps -= 1/Math.pow(2,k);
      else if( sum < pie )
        eps += 1/Math.pow(2,k);
    }
    if( sum != pie )
      System.out.println(" Rounding Failed !!!");

    return rounds;
  }
  
  public static boolean isNumber(String s)
  {
    String validChars = "-.0123456789";
    boolean isNumber = true;
    
    for (int i = 0; i < s.length() && isNumber; i++) 
    { 
      char c = s.charAt(i); 
      if (validChars.indexOf(c) == -1) 
      {
        isNumber = false;
      }
      else
      {
        isNumber = true;
      }
    }
    return isNumber;
  }

  public static String info2Html(String infoText) {
    
    String infoTxt = "";
    String     sep =":<TD  align='left'> <font size=-1 face='verdana, helvetica'>";
    String    para = "<TR height=5><TD align=right><font size=-1 face='verdana, helvetica'> ";
    
    StringTokenizer info = new StringTokenizer(infoText, "\n");

    String nextT;
    while( info.hasMoreTokens() ) {
      nextT = info.nextToken();
      StringTokenizer line = new StringTokenizer(nextT, ":");

      if( nextT.indexOf(":") > -1  )
        infoTxt = infoTxt + para + line.nextToken() + sep + line.nextToken() + "</TR>";
      else
        infoTxt = infoTxt + "<TR height=5><TD align=center colspan=2><font size=-1 face='verdana, helvetica'>"+nextT+"</TR>";
    }
    //System.out.println("<HTML><TABLE border='0' cellpadding='0' cellspacing='0'>"+infoTxt+" </TABLE></html>");
    return "<HTML><TABLE border='0' cellpadding='0' cellspacing='0'>"+infoTxt+" </TABLE></html>";
  }
}  

 
