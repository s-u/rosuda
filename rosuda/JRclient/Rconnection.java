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

    /** authorization type: plain text */
    public static final int AT_plain = 0;
    /** authorization type: unix crypt */
    public static final int AT_crypt = 1;

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
	    byte[] pc=rp.getCont();
	    REXP rx=null;
	    if (pc.length>0) {
		rx=new REXP();
		REXP.parseREXP(rx,pc,0);
	    };
	    succeeded=true;
	    return rx;
	};
	lastError="Request return code: "+rp.getStat();
	return null;
    }

    public boolean assign(String sym, String ct) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return false;
	}
	byte[] rq=new byte[sym.length()+5+ct.length()+5];
        sym.getBytes(0,sym.length(),rq,4);
	ct.getBytes(0,ct.length(),rq,9+sym.length());
	Rtalk.setHdr(Rtalk.DT_STRING,sym.length()+1,rq,0);
	Rtalk.setHdr(Rtalk.DT_STRING,ct.length()+1,rq,sym.length()+5);
	rq[sym.length()+4]=0; rq[rq.length-1]=0;
	Rpacket rp=rt.request(Rtalk.CMD_setSEXP,rq);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;
    }

    public boolean assign(String sym, REXP r) {
	succeeded=false;
	if (!connected || rt==null) {
	    lastError="Error: not connected!";
	    return false;
	}
	int rl=r.getBinaryLength();
	byte[] rq=new byte[sym.length()+5+rl+4];
        sym.getBytes(0,sym.length(),rq,4);
	Rtalk.setHdr(Rtalk.DT_STRING,sym.length()+1,rq,0);
	rq[sym.length()+4]=0;
	Rtalk.setHdr(Rtalk.DT_SEXP,rl,rq,sym.length()+5);
	r.getBinaryRepresentation(rq,sym.length()+9);
	Rpacket rp=rt.request(Rtalk.CMD_setSEXP,rq);
	if (rp!=null && rp.isOk())
	    return succeeded=true;
	lastError="Request return code: "+rp.getStat();
	return false;
    }

    public RFileInputStream openFile(String fn) throws IOException {
	return new RFileInputStream(rt,fn);
    };

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

