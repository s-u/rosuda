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
	@returns <code>null</code> if create failed or user canceled operation, the PrintStream otherwise.
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
	  
	PrintStream ps=null;
	try {
	    PrintStream outs=new PrintStream(new FileOutputStream(fnam));
	    return outs;
	} catch(Exception e) {
	};
	return null;	
    };    
};
