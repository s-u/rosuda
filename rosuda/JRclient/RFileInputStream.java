import java.io.*;


public class RFileInputStream extends InputStream {
    Rtalk rt;
    boolean closed;
    boolean eof;

    /** tried to open file on the R server, using specified {@link Rtalk} object
	and filename. Be aware that the filename has to be specified in host
	format (which is usually unix). In general you should not use directories
	since Rserve provides an own directory for every connection. Future Rserve
	servers may even strip all directory navigation characters for security
	purposes. Therefore only filenames without path specification are considered
	valid, the behavior in respect to filenames in undefined. */
    RFileInputStream(Rtalk rti, String fn) throws IOException {
	rt=rti;
	Rpacket rp=rt.request(Rtalk.CMD_openFile,fn);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	closed=false; eof=false;
    }

    public int read() throws IOException {
	byte[] b=new byte[1];
	if (read(b,0,1)<1) return -1;
	return b[0];
    }

    public int read(byte[] b, int off, int len) throws IOException {
	if (closed) throw new IOException("File is not open");
	if (eof) return -1;
	Rpacket rp=rt.request(Rtalk.CMD_readFile,len);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	byte[] rd=rp.getCont();
	if (rd==null) {
	    eof=true;
	    return -1;
	};
	int i=0;
	while(i<rd.length) { b[off+i]=rd[i]; i++; };
	return rd.length;
    }

    public void close() throws IOException {
	Rpacket rp=rt.request(Rtalk.CMD_closeFile,(byte[])null);
	if (rp==null || !rp.isOk())
	    throw new IOException((rp==null)?"Connection to Rserve failed":("Request return code: "+rp.getStat()));
	closed=true;
    }
}
