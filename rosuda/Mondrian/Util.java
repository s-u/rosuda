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

  public static Object resizeArray(int[] arr, int newSize) {

    if(arr == null || newSize < 0) return null;
    int[] newArray = new int[newSize];

    // Assure that we get no ArrayIndexOutOfBoundsExceptions
    int maxCopyLength = (arr.length > newSize ? newSize : arr.length);

    System.arraycopy(arr, 0, newArray, 0, maxCopyLength);
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
  
}  

 
