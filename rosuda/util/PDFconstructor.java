import java.io.*;


public class PDFconstructor {
    PrintStream pps;
    int offset;
    int oid;
    StringBuffer xref;

    public PDFconstructor(PrintStream os, int pdfver) {
	pps=os;
	offset=0;
	oid=1;
	out("%PDF-1."+pdfver+"\n");
	xref=new StringBuffer("0000000000 65535 f \n");
    };
    
    public void out(String s) {
	pps.print(s);
	offset+=s.length();
    };

    public int addObject(String s) {
	int ofs=offset;
	out(""+oid+" 0 obj\n");
	out(s);
	out("\nendobj\n");
	String t=""+ofs;
	while(t.length()<10) { t="0"+t; };
	xref.append(t+" 00000 n \n");
	return oid++;
    };
    
    public static String asStream(String s) {
	return "<< /Length "+(s.length()+1)+" >>\nstream\n"+s+"\nendstream";
    };

    public int end(int root) {
	out("\nxref\n0 "+oid+"\n"+xref.toString()+"\ntrailer\n<< /Size "+oid+"\n/Root "+root+" 0 R\n>>\nstartxref\n"+(offset+1)+"\n%%EOF\n");	
	pps.close();
	return offset;
    };
};
