import java.awt.*;
import java.io.*;

/** tools to be used by all other classes
    @version $Id$
*/
public class Tools {
    /** displays a dialog box to save a file and returns the
	corresponding printstream for the newly created file
	@par par parent frame for the dialog (if <code>null</code> then Common.mainFrame will be used)
	@par title dialog title
	@par deffn default file name
	@return <code>null</code> if create failed or user canceled operation, the PrintStream otherwise.
    */
    public static PrintStream getNewOutputStreamDlg(Frame par,
						    String title,
						    String deffn) {
	FileDialog fd=new FileDialog((par==null)?Common.mainFrame:par,title,FileDialog.SAVE);
	if (deffn!=null) fd.setFile(deffn);
	fd.setModal(true);
	fd.show();
	String fnam="";

	if (fd.getDirectory()!=null) fnam+=fd.getDirectory();
	if (fd.getFile()!=null) fnam+=fd.getFile();
	else return null;
	  
	try {
	    PrintStream outs=new PrintStream(new FileOutputStream(fnam));
	    return outs;
	} catch(Exception e) {
	};
	return null;	
    };

    public static double nlogn(double n) {
        return (n<=0)?0:n*Math.log(n);
    }

    public static double nlogn(int n) {
        return (n<=0)?0.0:((double)n)*Math.log((double)n);
    }
    
    public static String getDisplayableValue(double val) { return getDisplayableValue(val,val); };
    
    public static String getDisplayableValue(double val, double range) {
        double vLenLog10=(range>0)?Math.log(range)/Math.log(10):0;
        int dac=((2-((int)vLenLog10))<0)?0:(2-((int)vLenLog10));
        return getDisplayableValue(val,dac);
    };

    public static String getDisplayableValue(double val, int dac) {
        if (dac==0) return ""+((int)val);
        double mplr=10.0;
        while(dac>0) { mplr*=10.0; dac--; };
        int front=(int)(Math.round(val*mplr)/mplr);
        mplr/=10.0;
        double post=(val-((double)front))*mplr;
        if (post<0) post=-post;
        return ""+front+((Math.round(post)==0)?"":"."+Math.round(post));
    };

    public static double parseDouble(String s) {
        double d=0;
        try {
            Double dd=Double.valueOf(s);
            d=dd.doubleValue();
        } catch(Exception dce) {};
        return d;
    }
};
