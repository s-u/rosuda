package org.rosuda.JRclient;

import java.util.*;
import java.io.*;
import java.net.*;

/**  class providing TCP/IP connection to an Rserv
     @version $Id$
*/
public class Rconnection {
    /** last error string */
    String lastError=null;
    /** is set to <code>true</code> if last operation was successful,
	<code>false</code>otherwise. */
    boolean succeeded=false;
    Socket s;
    boolean connected=false;
    InputStream is;
    OutputStream os;
    boolean authReq=false;
    int authType=AT_plain;
    String Key=null;
    Rtalk rt=null;

    /** This static variable specifies the character set used to encode string for transfer. Under normal circumstances there should be no reason for changing this variable. The default is UTF-8, which makes sure that 7-bit ASCII characters are sent in a backward-compatible fashion. Currently (Rserve 0.1-7) there is no further conversion on Rserve's side, i.e. the strings are passed to R without re-coding. If necessary the setting should be changed <u>before</u> connecting to the Rserve in case later Rserves will provide a possibility of setting the encoding during the handshake. */
    public static String transferCharset="UTF-8";
    
    /** authorization type: plain text */
    public static final int AT_plain = 0;
    /** authorization type: unix crypt */
    public static final int AT_crypt = 1;

    /** version of the server (as reported in IDstring just after Rsrv) */
    protected int rsrvVersion;
    
    /** make a new local connection on default port (6311) */
    public Rconnection() {
	this("127.0.0.1",6311);
    }

    /** make a new connection to specified host on default port (6311)
	@param host host name/IP
    */
    public Rconnection(String host) {
	this(host,6311);
    }

    /** make a new connection to specified host and given port.
	Make sure you check {@link #isConnected} and/or {@link #isOk}.
	@param host host name/IP
	@param port TCP port
    */
    public Rconnection(String host, int port) {
	succeeded=false;
	try {
	    if (connected) s.close();
	    s=null;
	} catch (Exception e) {};
	connected=false;
	try {
	    s=new Socket(host,port);
	    is=s.getInputStream();
	    os=s.getOutputStream();
	    rt=new Rtalk(is,os);
	    byte[] IDs=new byte[32];
	    int n=is.read(IDs);
	    if (n!=32) {
		lastError="Handshake failed: expected 32 bytes header, got "+n;
		s.close(); is=null; os=null; s=null;
		return;
	    };
	    String ids=new String(IDs);
	    if (ids.substring(0,4).compareTo("Rsrv")!=0) {
		lastError="Handshake failed: Rsrv signature expected, but received \""+ids+"\" instead.";
		s.close(); is=null; os=null; s=null;
	    };
            try {
                rsrvVersion=Integer.parseInt(ids.substring(4,8));
            } catch (Exception px) {}
            if (rsrvVersion>101) {
                lastError="Handshake failed: The server uses more recent protocol than this client.";
                s.close(); is=null; os=null; s=null;
            }
            if (ids.substring(8,12).compareTo("QAP1")!=0) {
		lastError="Handshake failed: unupported transfer protocol ("+ids.substring(8,12)+"), I talk only QAP1.";
		s.close(); is=null; os=null; s=null;
	    };
	    for (int i=12;i<32;i+=4) {
		String attr=ids.substring(i,i+4);
		if (attr.compareTo("ARpt")==0) {
		    if (!authReq) { // this method is only fallback when no other was specified
			authReq=true;
			authType=AT_plain;
		    }
		};
		if (attr.compareTo("ARuc")==0) {
		    authReq=true;
		    authType=AT_crypt;
		};
		if (attr.charAt(0)=='K') {
		    Key=attr.substring(1,3);
		};
	    };
	    connected=true;
	    succeeded=true;
	    lastError="OK";
	} catch (Exception e2) {
	    lastError="Exception: "+e2.getMessage();
	};
    }

    public void finalize() {
        close();
        is=null; is=null;
    }

    public int getServerVersion() {
        return rsrvVersion;
    }
    
    /** closes current connection */
    public void close() {
        try {
            if (s!=null) s.close();
        } catch(Exception e) { };
    }
    
    /** evaluates the given command, but does not fetch the result (useful for assignment
	operations)
	@param cmd command/expression string
	@return <code>true</code> if successful */
    public boolean voidEval(String cmd) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return false;
	}
	Rpacket rp=rt.request(Rtalk.CMD_voidEval,cmd+"\n");
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;	
    }

    /** evaluates the given command and retrieves the result
	@param cmd command/expression string
	@return R-xpression or <code>null</code> if an error occured */
    public REXP eval(String cmd) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return null;
	}
	Rpacket rp=rt.request(Rtalk.CMD_eval,cmd+"\n");
	if (rp!=null && rp.isOk()) {
            int rxo=0;
	    byte[] pc=rp.getCont();
            if (rsrvVersion>100) { /* since 0101 eval responds correctly by using DT_SEXP type/len header which is 4 bytes long */
                rxo=4;
                /* we should check parameter type (should be DT_SEXP) and fail if it's not */
                if (pc[0]!=Rtalk.DT_SEXP) {
                    lastError="Error while processing eval output: SEXP (type "+Rtalk.DT_SEXP+") expected but found result type "+pc[0]+".";
                    return null;
                }
                /* warning: we are not checking or using the length - we assume that only the one SEXP is returned. This is true for the current CMD_eval implementation, but may be not in the future. */
            }
            REXP rx=null;
            if (pc.length>rxo) {
                rx=new REXP();
                REXP.parseREXP(rx,pc,rxo);
            };
            succeeded=true;
            return rx;
	};
        lastError="Request return code: "+rp.getStat();
        return null;
    }

    /** assign a string value to a symbol in R. The symbol is created if it doesn't exist already.
        @param sym symbol name. Currently assign uses CMD_setSEXP command of Rserve, i.e. the symbol value is NOT parsed. It is the responsibility of the user to make sure that the symbol name is valid in R (recall the difference between a symbol and an expression!). In fact R will always create the symbol, but it may not be accessible (examples: "bar\nfoo" or "bar$foo").
        @param ct contents
        @return <code>true</code> on success, otherwise <code>false</code>
        */
    public boolean assign(String sym, String ct) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return false;
	}
        byte[] symn=sym.getBytes();
        byte[] ctn=ct.getBytes();
        byte[] rq=new byte[symn.length+5+ctn.length+5];
        for(int ic=0;ic<symn.length;ic++) rq[ic+4]=symn[ic];
        for(int ic=0;ic<ctn.length;ic++) rq[ic+symn.length+9]=ctn[ic];
	Rtalk.setHdr(Rtalk.DT_STRING,symn.length+1,rq,0);
	Rtalk.setHdr(Rtalk.DT_STRING,ctn.length+1,rq,symn.length+5);
	rq[symn.length+4]=0; rq[rq.length-1]=0;
	Rpacket rp=rt.request(Rtalk.CMD_setSEXP,rq);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;
    }

    /** assign a content of a REXP to a symbol in R. The symbol is created if it doesn't exist already.
        @param sym symbol name. Currently assign uses CMD_setSEXP command of Rserve, i.e. the symbol value is NOT parsed. It is the responsibility of the user to make sure that the symbol name is valid in R (recall the difference between a symbol and an expression!). In fact R will always create the symbol, but it may not be accessible (examples: "bar\nfoo" or "bar$foo").
        @param ct contents. currently only basic types (int, double, int[], double[]) are supported.
        @return <code>true</code> on success, otherwise <code>false</code>
        */
    public boolean assign(String sym, REXP r) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return false;
	}
	int rl=r.getBinaryLength();
        byte[] symn=sym.getBytes();
	byte[] rq=new byte[symn.length+5+rl+4];
        for(int ic=0;ic<symn.length;ic++) rq[ic+4]=symn[ic];
	Rtalk.setHdr(Rtalk.DT_STRING,symn.length+1,rq,0);
	rq[symn.length+4]=0;
	Rtalk.setHdr(Rtalk.DT_SEXP,rl,rq,symn.length+5);
	r.getBinaryRepresentation(rq,symn.length+9);
	Rpacket rp=rt.request(Rtalk.CMD_setSEXP,rq);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;
    }

    /** assign values of an array of doubles to a symbol in R (creating as vector of numbers).<br>
        equals to calling {@link #assign(String, REXP)} */        
    public boolean assign(String sym, double[] val) {
        return assign(sym,new REXP(val));
    }

    /** assign values of an array of integers to a symbol in R (creating as vector of numbers).<br>
        equals to calling {@link #assign(String, REXP)} */        
    public boolean assign(String sym, int[] val) {
        return assign(sym,new REXP(val));
    }

    /** open a file on the Rserve for reading
        @param fn file name. should not contain any path delimiters, since Rserve may restrict the access to local working directory.
        @return input stream to be used for reading. Note that the stream is read-once only, there is no support for seek or rewind. */
    public RFileInputStream openFile(String fn) throws IOException {
	return new RFileInputStream(rt,fn);
    };

    /** remove a file on the Rserve
        @param fn file name. should not contain any path delimiters, since Rserve may restrict the access to local working directory.
        @return <code>true</code> on success, <code>false</code> otherwise */
    public boolean removeFile(String fn) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected"; return succeeded=false;
	}	    
	Rpacket rp=rt.request(Rtalk.CMD_removeFile,fn);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;
    };

    /** shutdown remote Rserv. Note that some Rserves cannot be shut down from
	client side (forked version). */
    public boolean shutdown() {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected"; return succeeded=false;
	}	    
	Rpacket rp=rt.request(Rtalk.CMD_shutdown);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;    
    }

    /** login using supplied user/pwd. Note that login must be the first
	command if used
	@param user username
	@param pwd password
	@return returns <code>true</code> on success */
    public boolean login(String user, String pwd) {
	succeeded=false;
	if (!authReq) return succeeded=true;
	if (authType==AT_crypt) {
	    if (Key==null) Key="rs";
	    Rpacket rp=rt.request(Rtalk.CMD_login,user+"\n"+jcrypt.crypt(Key,pwd));
	    if (rp!=null && rp.isOk())
		return succeeded=true;
	    lastError="Request return code: "+rp.getStat();
	    try { s.close(); } catch(Exception e) {};
	    is=null; os=null; s=null; connected=false;
	    return false;    
	}
	Rpacket rp=rt.request(Rtalk.CMD_login,user+"\n"+pwd);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	try {s.close();} catch (Exception e) {};
	is=null; os=null; s=null; connected=false;
	return false;    
    }

    /** check success of the last operation
	@return <code>true</code> if last function was successful */
    public boolean isOk() { return succeeded; }
    
    /** check connection state. Note that currently this state is not checked on-the-spot,
	that is if connection went down by an outside event this is not reflected by
	the flag
	@return <code>true</code> if this connection is alive */
    public boolean isConnected() { return connected; }
    
    /** check authentication requirement sent by server
	@return <code>true</code> is server requires authentication. In such case first
	command after connecting must be {@link #login}. */
    public boolean needLogin() { return authReq; }
    
    /** get last error string
	@return last error string */
    public String getLastError() { return lastError; }
}

