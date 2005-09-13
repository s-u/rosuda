package org.rosuda.JRclient;

public class RSession {
    String host;
    int port;
    byte[] key;

    Rpacket attachPacket=null; // response on session attach
    int rsrvVersion;

    RSession(Rconnection c, Rpacket p) throws RSrvException {
	this.host=c.host;
	this.rsrvVersion=c.rsrvVersion;
	byte[] ct = p.getCont();
	if (ct==null || ct.length!=32+3*4)
	    throw new RSrvException(c, "Invalid response to session detach request.");
	this.port = Rtalk.getInt(ct, 4);
	this.key=new byte[32];
	System.arraycopy(ct, 12, this.key, 0, 32);
    }

    /** attach/resume this session */
    public Rconnection attach() throws RSrvException {
	Rconnection c = new Rconnection(this);
	attachPacket = c.rt.request(-1);
	return c;
    }
}
