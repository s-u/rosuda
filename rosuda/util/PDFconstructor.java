package org.rosuda.util;

import java.io.*;

/** This class provides means of constructing one-pass, basic, uncompressed PDF files. It is the lowest level (objects-level) of PDF construction. Reference index and trailer is created automatically. Output is written to a supplied {@link PrintStream}.<p>Sample usage:<pre>
PDFconstructor p=new PDFconstructor(System.out,1);
p.addObject("<< /Type /Catalog\n/Outlines 2 0 R\n/Pages 3 0 R\n>>"); // ID 1 document root
p.addObject("<< /Type /Outlines\n/Count 0\n>>"); // ID 2 outlines
p.addObject("<< /Type /Pages\n/Kids [4 0 R]\n/Count 1\n>>"); // ID 3 pages layout
p.addObject("<< /Type /Page\n/Parent 3 0 R\n/MediaBox [0 0 612 792]\n/Contents 5 0 R\n/Resources << /ProcSet 6 0 R\n/Font << /F1 7 0 R >>\n>>\n>>"); // ID 4 page def.
p.addObject(PDFconstructor.asStream("150 250 m\n150 350 l\nS\n4 w\n[4 6] 0 d\n150 250 m\n400 250 l\nS\n[] 0 d\n1 w\n1.0 0.0 0.0 RG\n0.5 0.75 1.0 rg\n200 300 50 75 re\nB 0.5 0.1 0.2 RG\n0.7 g\n300 300 m\n300 400 400 400 400 300 c\nb\nBT\n/F1 24 Tf\n100 100 Td\n(Hello World) Tj\nET")); // ID 5 contents
p.addObject("[/PDF /Text]"); // ID 6 proc set
p.addObject("<< /Type /Font\n/Subtype /Type1\n/Name /F1\n/BaseFont /Helvetica\n/Encoding /MacRomanEncoding\n>>"); // ID 7 fonts
p.end(1);</pre>
*/
public class PDFconstructor {
    PrintStream pps;
    int offset;
    int oid;
    StringBuffer xref;

    /** begin construction of a new PDF file. Major PDF version will always be 1, minor version is supplied by the caller. The compilance of the PDF file is in the hands of the calling program since the storage format created by {@link #PDFconstructor} is compatible from PDF 1.1 on, but the objects used may require higher PDF version. The creation is started immediately and PDF version string is sent to the output stream.<p>Important! PDF files use absolute indexing, therefore the output stream should have no preceeding or trailing contents, otherwise the resulting file will be invalid! In plain text the PrintStream should be created and immediately passed to PDFcreator. Upon finish PDFcreator automatically closes the stream. No other methods than those of the PDFconstructor should be used on the stream.
        @param os output stream
        @param pdfver minor version of the PDF format used (major version is always 1). For example pdfver=2 corresponds to PDF-1.2 */
    public PDFconstructor(PrintStream os, int pdfver) {
	pps=os;
	offset=0;
	oid=1;
	out("%PDF-1."+pdfver+"\n");
	xref=new StringBuffer("0000000000 65535 f \n");
    };

    /** add specified string to the stream output. You should not call this method directly, since any contents added this way is not indexed and hence ignored by the PDF reader. (unless you really know what you're doing...)
        @param s string to be written to the stream
        */
    public void out(String s) {
	pps.print(s);
	offset+=s.length();
    };

    /** add the specified object to the PDF file. Newline is added implicitely.
        @param s string containing the object to be added
        @return ID of the object. All objects are numbered sequentially starting from 1, therefore the caller can rely on such ordering without the need to check this return value. */
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

    /** constructs a string that can be passed to {@link #addObject} that represents a (non-compressed) PDF stream.
        @param s string which will be encapsulated as the contants of the stream (such as graphics commands etc.) Trailing newline is added implicitely.
        @return valid PDF object of the type stream */
    public static String asStream(String s) {
	return "<< /Length "+(s.length()+1)+" >>\nstream\n"+s+"\nendstream";
    };

    /** finish construction of the PDF file, add index/reference and trailer. The output stream is closed.
        @param root ID of the root object
        @return size of the entire PDF file */
    public int end(int root) {
	out("\nxref\n0 "+oid+"\n"+xref.toString()+"\ntrailer\n<< /Size "+oid+"\n/Root "+root+" 0 R\n>>\nstartxref\n"+(offset+1)+"\n%%EOF\n");
	pps.close();
	return offset;
    };
};
