/*
 * RserveConnection.java
 *
 * Created on 10. Juli 2005, 19:59
 *
 */

package org.rosuda.JClaR;

import java.util.Vector;
import org.rosuda.JRclient.RFileOutputStream;
import org.rosuda.JRclient.RFileInputStream;
import org.rosuda.JRclient.REXP;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

/**
 * Manages the connection to Rserve.
 * @author tobias
 */
public final class RserveConnection {
    
    private static final int RERROR_SYNTAX = 3;
    static final int RERROR_OTHER = 127;
    
    private static Rconnection rcon;
    private static RserveConnection instance;
    private static boolean windows;
    
    private static String lastRcall="";
    
    /** Creates a new instance of RserveConnection */
    private RserveConnection() {
    }
    
    /**
     * Opens a connection to Rserve if not done so already.
     * @return the Rconnection
     */
    private static RserveConnection getRconnection() {
	if(instance==null) {
	    instance = new RserveConnection();
	    if(System.getProperty("os.name").toLowerCase().indexOf("windows")!=-1)  {
		windows=true;
	    }
	    
	    else  {
		windows=false;
	    }
	}
	if(rcon==null) {
	    if (checkLocalRserve())  {
		try{
		    rcon = new Rconnection();
		} catch (RSrvException rse){
		    ErrorDialog.show(null, rse, "RserveConnection.getRconnection()");
		}
	    } else {
		ErrorDialog.show(null, "Could not start Rserve. Please start it manually and restart JClaR.");
		System.exit(-1);
	    }
	}
	return instance;
    }
    
    /**
     * From org.rosuda.Mondrian.
     * @author Matrin Theus
     */
    private static final class StreamHog extends Thread {
	InputStream is;
	
	StreamHog(final InputStream is) { this.is=is; start(); }
	public void run() {
	    try {
		final BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line=null;
		while ( (line = br.readLine()) != null) {
		    System.out.println("Rserve>" + line);
		}
	    } catch (IOException e) { e.printStackTrace(); }
	}
    }
    
    /**
     * From org.rosuda.Mondrian.
     * @author Martin Theus
     */
    private static boolean launchRserve(final String cmd) {
	try {
	    final Process p = Runtime.getRuntime().exec(cmd);
	    System.out.println("waiting for Rserve to start ... ("+p+")");
	    // we need to fetch the output - some platforms will die if you don't ...
	    final StreamHog errorHog = new StreamHog(p.getErrorStream());
	    final StreamHog outputHog = new StreamHog(p.getInputStream());
	    p.waitFor();
	    System.out.println("call terminated, let us try to connect ...");
	} catch (Exception x) {
	    System.out.println("failed to start Rserve process with "+x.getMessage());
	    return false;
	}
	try {
	    
	    System.out.println("Rserve is running.");
	    final Rconnection c = new Rconnection();
	    c.close();
	    return true;
	} catch (Exception e2) {
	    System.out.println("Try failed with: "+e2.getMessage());
	}
	return false;
    }
    
    /**
     * From org.rosuda.Mondrian.
     * @author Martin Theus
     */
    private static boolean checkLocalRserve() {
	try {
	    
	    System.out.println("Rserve is running.");
	    final Rconnection c = new Rconnection();
	    c.close();
	    return true;
	} catch (Exception e) {
	    System.out.println("First connect try failed with: "+e.getMessage());
	}
	final String opt=" CMD Rserve --no-save";
	return (launchRserve("R"+opt) ||
		((new File("/usr/local/lib/R/bin/R")).exists() &&
		launchRserve("/usr/local/lib/R/bin/R"+opt)) ||
		((new File("/usr/lib/R/bin/R")).exists() && launchRserve("/usr/lib/R/bin/R"+opt)) ||
		((new File("/usr/local/bin/R")).exists() && launchRserve("/usr/local/bin/R"+opt)) ||
		((new File("/sw/bin/R")).exists() && launchRserve("/sw/bin/R"+opt)) ||
		((new File("/Library/Frameworks/R.framework/Resources/bin/R")).exists() &&
		launchRserve("/Library/Frameworks/R.framework/Resources/bin/R"+opt)));
    }
    
    /**
     * Delegate method for org.rosuda.JRclient.Rconnection.Rserve.eval(String str).
     * Adds try(...) on Windows systems.
     * @param str The R command to be executed.
     * Has to be exactly one command, i.e. not multiple commands separated by
     * semicolons or line breaks.
     * @throws org.rosuda.JRclient.RSrvException The RSrvException thrown by the delegated method.
     * @return The object returned by R.
     */
    static REXP eval(final String str) throws RSrvException {
	if(rcon==null) getRconnection();
	lastRcall=str;
	if("".equals(str) || str==null)  {
	    return null;
	}
	
	try{
	    if(windows) {
		return rcon.eval("try(" + str + ")");
	    } else {
		return rcon.eval(str);
	    }
	} catch (RSrvException rse){
	    switch(rse.getRequestReturnCode()){
		case RERROR_SYNTAX:
		    syntaxError(str);
		    return null;
		default:
		    throw(rse);
	    }
	}
    }
    
    /**
     * Delegate method for org.rosuda.JRclient.Rconnection.voidEval(String str).
     * Adds try(...) on Windows systems.
     * @param str The R command to be executed.
     * Has to be exactly one command, i.e. not multiple commands separated by
     * semicolons or line breaks.
     * @throws org.rosuda.JRclient.RSrvException The RSrvException thrown by the delegated method.
     */
    static void voidEval(final String str) throws RSrvException {
	if(rcon==null) getRconnection();
	lastRcall=str;
	if("".equals(str) || str==null)  {
	    return;
	}
	
	try{
	    if(windows) {
		rcon.voidEval("try(" + str + ")");
	    } else{
		rcon.voidEval(str);
	    }
	} catch (RSrvException rse){
	    switch(rse.getRequestReturnCode()){
		case RERROR_SYNTAX:
		    syntaxError(str);
		    break;
		default:
		    throw(rse);
	    }
	}
    }
    
    /**
     * Delegate method for org.rosuda.JRclient.Rconnection.Rserve.openFile(String str).
     */
    public static RFileInputStream openFile(final String str) throws IOException {
	return rcon.openFile(str);
    }
    
    private static void syntaxError(final String rcall){
	ErrorDialog.show(null,"Syntax error in R command. Please report this to the developer.\n" +
		"Error occured on evaluating\n\n" +
		rcall);
    }
    
    static String getLastRcall() {
	return lastRcall;
    }
    
    static RFileOutputStream createFile(final String str) throws IOException {
	return rcon.createFile(str);
    }
    
    private static void writeTable(final String data, final File file) throws RSrvException {
	writeTable(data, file, "");
    }
    
    static void writeTable(final String data, final File file, final String options) throws RSrvException {
	String path=file.getPath();
	if (File.separatorChar == '\\')  {
	    path = path.replace('\\', '/');
	}
	voidEval("write.table(" + data + ",file='" + path + "'" + options + ")");
    }
    
    static int evalI(final String string) throws RSrvException {
	final REXP rex = eval(string);
	if(rex==null) throw new RSrvException(rcon,"Eval returned null.");
	else return rex.asInt();
    }
    
    static int[] evalIA(final String string) throws RSrvException {
	final REXP rex = eval(string);
	if(rex==null) return null;
	else return rex.asIntArray();
    }
    
    static double evalD(final String string) throws RSrvException {
	final REXP rex = eval(string);
	if(rex==null) throw new RSrvException(rcon,"Eval returned null.");
	else return rex.asDouble();
    }
    
    static boolean evalB(final String string) throws RSrvException {
	final REXP rex = eval(string);
	if(rex==null) throw new RSrvException(rcon,"Eval returned null.");
	else return rex.asBool().isTRUE();
    }
    
    static String[] evalSL(final String string) throws RSrvException {
	final REXP rex = eval(string);
	if(rex==null) return null;
	else return rex.asStringArray();
    }
    
    static byte[] readFile(final String file) {
	RFileInputStream is=null;
	try {
	    is = rcon.openFile(file);
	} catch (IOException ex) {
	    ErrorDialog.show(null,"Could not open file " + file + ".");
	    ex.printStackTrace();
	    return null;
	}
	if(is==null) return null;
	Vector<byte[]> buffers=new Vector<byte[]>();
	int bufSize=65536;
	byte[] buf=new byte[bufSize];
	int imgLength=0;
	int n=0;
	while (true) {
	    try {
		n=is.read(buf);
	    } catch (IOException ex) {
		ErrorDialog.show(null,"Could not read file " + file + ".");
		ex.printStackTrace();
		return null;
	    }
	    if (n==bufSize) {
		buffers.addElement(buf);
		buf=new byte[bufSize];
	    }
	    if (n>0) imgLength+=n;
	    if (n<bufSize) break;
	}
	byte[] imgCode=new byte[imgLength];
	int imgPos=0;
	for (Enumeration e = buffers.elements() ; e.hasMoreElements() ;) {
	    byte[] b = (byte[]) e.nextElement();
	    System.arraycopy(b,0,imgCode,imgPos,bufSize);
	    imgPos+=bufSize;
	}
	if (n>0) System.arraycopy(buf,0,imgCode,imgPos,n);
	try {
	    is.close();
	} catch (IOException ex) {
	    ErrorDialog.show(null,"Could not close file " + file +
		    ". Read data might be corrupt.");
	    ex.printStackTrace();
	} finally {
	    return imgCode;
	}
    }
}
