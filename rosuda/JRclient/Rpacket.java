package org.rosuda.JRclient;

/** small class encapsulating packets from/to Rserv
    @version $Id$
*/
public class Rpacket {
    int cmd;
    byte[] cont;

    /** construct new packet
	@param Rcmd command
	@param Rcont content */
    public Rpacket(int Rcmd, byte[] Rcont) {
	cmd=Rcmd; cont=Rcont;
    }
    
    /** get command
        @return command */
    public int getCmd() { return cmd; }
    
    /** check last response for RESP_OK
	@return <code>true</code> if last response was OK */
    public boolean isOk() { return ((cmd&15)==1); }
    
    /** check last response for RESP_ERR
	@return <code>true</code> if last response was ERROR */
    public boolean isError() { return ((cmd&15)==2); }
    
    /** get status code of last response
	@return status code returned on last response */
    public int getStat() { return ((cmd>>24)&127); }

    /** get content
	@return inner package content */
    public byte[] getCont() { return cont; }
}
