import java.util.*;

/** Portable Graphics SubSystem, meta data implementation
    @version $Id$
*/

public class PoGraSSmeta extends PoGraSS
{
    StringBuffer ct;
    
    public PoGraSSmeta() { ct=new StringBuffer(); };

    public void addComment(String c) {
	ct.append("p.addComment(\n\""+c+"\");\n");
    };
    public void setTitle(String t) {
	ct.append("p.setTitle(\n\""+t+"\");\n");
    };
    public void setBounds(int w, int h) { 
	ct.append("p.setBounds(\n"+w+",\n"+h+");\n"); 
    };
    public void setBounds(int x, int y,int w, int h) { 
	ct.append("p.setBounds(\n"+x+",\n"+y+",\n"+w+",\n"+h+");\n"); 
    };
    public void defineColor(String nam, int R, int G, int B) {
	ct.append("p.defineColor(\n\""+nam+"\",\n"+R+",\n"+G+",\n"+B+");\n");
    };
    public void setColor(int R, int B, int G) {
	ct.append("p.setColor(\n"+R+",\n"+G+",\n"+B+");\n");
    };
    public void setColor(String nam) {
	ct.append("p.setColor(\n\""+nam+"\");\n");
    };
    public void drawLine(int x1, int y1, int x2, int y2) {
	ct.append("p.drawLine(\n"+x1+",\n"+y1+",\n"+x2+",\n"+y2+");\n");
    };
    public void moveTo(int x, int y) {
	ct.append("p.moveTo(\n"+x+",\n"+y+");\n");
    };
    public void lineTo(int x, int y) {
	ct.append("p.lineTo(\n"+x+",\n"+y+");\n");
    };
    public void drawRect(int x1, int y1, int x2, int y2) {
	ct.append("p.drawRect(\n"+x1+",\n"+y1+",\n"+x2+",\n"+y2+");\n");
    };
    public void fillRect(int x1, int y1, int x2, int y2) {
	ct.append("p.fillRect(\n"+x1+",\n"+y1+",\n"+x2+",\n"+y2+");\n");
    };
    public void drawRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	ct.append("p.drawRoundRect(\n"+x1+",\n"+y1+",\n"+x2+",\n"+y2+",\n"+dx+",\n"+dy+");\n");
    };
    public void fillRoundRect(int x1, int y1, int x2, int y2, int dx, int dy) {
	ct.append("p.fillRoundRect(\n"+x1+",\n"+y1+",\n"+x2+",\n"+y2+",\n"+dx+",\n"+dy+");\n");
    };
    public void drawOval(int x, int y, int rx, int ry) {
	ct.append("p.drawOval(\n"+x+",\n"+y+",\n"+rx+",\n"+ry+");\n");
    };
    public void fillOval(int x, int y, int rx, int ry) {
	ct.append("p.drawOval(\n"+x+",\n"+y+",\n"+rx+",\n"+ry+");\n");
    };
    public void setLineWidth(int w) {
	ct.append("p.setLineWidth(\n"+w+");\n");
    };
    public void setFillStyle(int s) {
	ct.append("p.setFillStyle(\n"+s+");\n");
    };
    public void drawString(String txt, int x, int y) {
	ct.append("p.drawString(\n\""+txt+"\",\n"+x+",\n"+y+");\n");
    };

    public void begin() { ct.append("p.begin(\n);\n"); };
    public void end() { ct.append("p.end(\n);\n"); };

    public void resetMeta() {
	ct=new StringBuffer();
    };

    public static boolean executeMeta(PoGraSS p, String s)
    {
	String[] par = new String[16];
	int[] pari = new int[16];
	int pars=0;
	
	StringTokenizer st = new StringTokenizer(s,"\n");
	while (st.hasMoreTokens()) {
	    String cmd=st.nextToken();
	    cmd=cmd.substring(2,cmd.length()-1); // cut leading p. and trailing (
	    //System.out.println("cmd=\""+cmd+"\"\n");
	    pars=0;
	    do {
		String pp=st.nextToken();
		//System.out.println("  pp=\""+pp+"\"\n");
		if (pp.substring(pp.length()-2).compareTo(");")==0) {
		    par[pars]=pp.substring(0,pp.length()-2); 
		    if (par[pars].length()>0) {
			if (par[pars].charAt(0)=='"')
			    par[pars]=par[pars].substring(1,par[pars].length()-1);
			else {
			    try {
				pari[pars]=Integer.valueOf(par[pars]).intValue();
			    } catch(Exception ee) { pari[pars]=0; };
			};		    
			pars++;
		    };
		    break;
		};
		par[pars]=pp.substring(0,pp.length()-1);
		if (par[pars].charAt(0)=='"')
		    par[pars]=par[pars].substring(1,par[pars].length()-1);
		else {
		    try {
			pari[pars]=Integer.valueOf(par[pars]).intValue();
		    } catch(Exception ee) { pari[pars]=0; };
		};		    
		pars++;
	    } while(pars<16);

	    // process commands
	    if(cmd.compareTo("addComment")==0)
		p.addComment(par[0]);
	    if(cmd.compareTo("setTitle")==0)
		p.setTitle(par[0]);
	    if(cmd.compareTo("setBounds")==0)
		p.setBounds(pari[0],pari[1]);	    
	    if(cmd.compareTo("defineColor")==0)
		p.defineColor(par[0],pari[1],pari[2],pari[3]);	    
	    if(cmd.compareTo("setColor")==0) {
		if(pars>2)
		    p.setColor(pari[0],pari[1],pari[2]);
		else
		    p.setColor(par[0]);
	    };
	    if(cmd.compareTo("drawLine")==0)
		p.drawLine(pari[0],pari[1],pari[2],pari[3]);
	    if(cmd.compareTo("moveTo")==0)
		p.moveTo(pari[0],pari[1]);
	    if(cmd.compareTo("lineTo")==0)
		p.lineTo(pari[0],pari[1]);
	    if(cmd.compareTo("drawRect")==0)
		p.drawRect(pari[0],pari[1],pari[2],pari[3]);
	    if(cmd.compareTo("fillRect")==0)
		p.fillRect(pari[0],pari[1],pari[2],pari[3]);
	    if(cmd.compareTo("drawRoundRect")==0)
		p.drawRoundRect(pari[0],pari[1],pari[2],pari[3],pari[4],pari[5]);
	    if(cmd.compareTo("fillRoundRect")==0)
		p.fillRoundRect(pari[0],pari[1],pari[2],pari[3],pari[4],pari[5]);
	    if(cmd.compareTo("drawOval")==0)
		p.drawOval(pari[0],pari[1],pari[2],pari[3]);
	    if(cmd.compareTo("fillOval")==0)
		p.fillOval(pari[0],pari[1],pari[2],pari[3]);
	    if(cmd.compareTo("setLineWidth")==0)
		p.setLineWidth(pari[0]);
	    if(cmd.compareTo("setFillStyle")==0)
		p.setFillStyle(pari[0]);
	    if(cmd.compareTo("drawString")==0)
		p.drawString(par[0],pari[1],pari[2]);
	    if(cmd.compareTo("begin")==0)
		p.begin();
	    if(cmd.compareTo("end")==0)
		p.end();
	};
	return true;
    };

    public String getMeta() {
	return ct.toString();
    };
};
