import java.util.*;
import java.io.*;
import java.net.*;

/** this class encapsulated the QAP1 protocol used by Rserv.
    it is independent of the undelying protocol(s), therefore Rtalk
    can be used over any transport layer
    @version $Id$
*/
public class Rtalk {
    public static final int DT_INT=1;
    public static final int DT_CHAR=2;
    public static final int DT_DOUBLE=3;
    public static final int DT_STRING=4;
    public static final int DT_BYTESTREAM=5;
    public static final int DT_SEXP=10;
    public static final int DT_ARRAY=11;

    public static final int CMD_login=0x001;
    public static final int CMD_voidEval=0x002;
    public static final int CMD_eval=0x003;
    public static final int CMD_shutdown=0x004;
    public static final int CMD_openFile=0x010;
    public static final int CMD_createFile=0x011;
    public static final int CMD_closeFile=0x012;
    public static final int CMD_readFile=0x013;
    public static final int CMD_writeFile=0x014;

    InputStream is;
    OutputStream os;
    
    /** constructor; parameters specify the streams
	@param sis socket input stream
	@param sos socket output stream */

    public Rtalk(InputStream sis, OutputStream sos) {
	is=sis; os=sos;
    }

    /** writes bit-wise int to a byte buffer at specified position in Intel-endian form
	@param v value to be written
	@param buf buffer
	@param o offset in the buffer to start at. An int takes always 4 bytes */
    public static void setInt(int v, byte[] buf, int o) {
	buf[o]=(byte)(v&255); o++;
	buf[o]=(byte)((v&0xff00)>>8); o++;
	buf[o]=(byte)((v&0xff0000)>>16); o++;
	buf[o]=(byte)((v&0xff000000)>>24);
    }

    /** writes cmd/resp/type byte + 3 bytes len into a byte buffer at specified offset
	@param ty type/cmd/resp byte
	@param len length
	@param buf buffer
	@param o offset */
    public static void setHdr(int ty, int len, byte[] buf, int o) {
	buf[o]=(byte)(ty&255); o++;
	buf[o]=(byte)(len&255); o++;
	buf[o]=(byte)((len&0xff00)>>8); o++;
	buf[o]=(byte)((len&0xff0000)>>16); o++;
    };

    /** converts bit-wise store int in Intel-endian form into Java int
	@param buf buffer containg the representation
	@param o offset where to start (4 bytes will be used)
	@return the int value. no bounds checking is done so you need to
	        make sure that the buffer is big enough */
    public static int getInt(byte[] buf, int o) {
	return ((buf[o]&255)|((buf[o+1]&255)<<8)|((buf[o+2]&255)<<16)|((buf[o+3]&255)<<24));
    }

    /** converts bit-wise store length from a header, it corresponds to
	calling getInt(buf,o+1) except that only 3 bytes are read
	@param buf buffer
	@param o offset of the header (length is at o+1)
	@return length */
    public static int getLen(byte[] buf, int o) {
	return ((buf[o+1]&255)|((buf[o+2]&255)<<8)|((buf[o+3]&255)<<16));
    }

    /** converts bit-wise Intel-endian format into long
	@param buf buffer
	@param o offset (8 bytes will be used)
	@return long value */
    public static long getLong(byte[] buf, int o) {
	long low=((long)getInt(buf,o))&0xffffffffL;
	long hi=((long)getInt(buf,o+4))&0xffffffffL;
	hi<<=32; hi|=low;
	return hi;
    }

    /** sends a request with no attached parameters
	@param cmd command
	@return returned packet or <code>null</code> if something went wrong */
    Rpacket request(int cmd) {
        byte[] d = new byte[0];
        return request(cmd,d);
    }

    /** sends a request with attached parameters
	@param cmd command
	@param cont contents - parameters
	@return returned packet or <code>null</code> if something went wrong */
    Rpacket request(int cmd, byte[] cont) {
	byte[] hdr=new byte[16];
	setInt(cmd,hdr,0);
	setInt((cont==null)?0:cont.length,hdr,4);
	for(int i=8;i<16;i++) hdr[i]=0;
	try {
	    os.write(hdr);
	    if (cont!=null && cont.length>0)
		os.write(cont);

	    byte[] ih=new byte[16];
	    if (is.read(ih)!=16)
		return null;
	    int rep=getInt(ih,0);
	    int rl =getInt(ih,4);
	    if (rl>0) {
		byte[] ct=new byte[rl];
		int n=is.read(ct);
		return new Rpacket(rep,ct);
	    }
	    return new Rpacket(rep,null);
	} catch(Exception e) {
	    return null;
	}
    }

    /** sends a request with one string parameter attached
	@param cmd command
	@param par parameter - length and DT_STRING will be prepended
	@return returned packet or <code>null</code> if something went wrong */
    Rpacket request(int cmd, String par) {
	try {
            par=par+"\n";
	    byte[] b=par.getBytes("UTF-8");
	    byte[] rq=new byte[par.length()+5];
	    for(int i=0;i<b.length;i++)
		rq[i+4]=b[i];
	    rq[b.length+4]=0;
	    setHdr(DT_STRING,b.length+1,rq,0);
	    return request(cmd,rq);
	} catch (Exception e) {
	};
	return null;
    };
}
