import java.util.Vector;

/** Axis - implements transformation of cases, values or categories to orthogonal graphical
    coordinates and vice versa. Supported axis types are: numerical, equidistant (i.e. nominal/ordinal by index), categorical-equidistant, categorical-proportional by population.
    @version $Id$ */

public class Axis extends Notifier
{
    /** Axis orientation: horizontal (X) */
    public static final int O_Horiz = 0;
    /** Axis orientation: vertical (Y) */
    public static final int O_Vert  = 1;
    /** Axis orientation: horizontal (X) */
    public static final int O_X = 0;
    /** Axis orientation: vertical (Y) */
    public static final int O_Y = 1;
    /** Axis type: numerical */
    public static final int T_Num = 0;
    /** Axis type: equidistant categorical */
    public static final int T_EqCat = 1;
    /** Axis type: proportional categorical */
    public static final int T_PropCat = 2;
    /** Axis type: equidistant (i.e. discrete/nominal/ordinal by index) */
    public static final int T_EqSize = 3;
    
    /** associated variable */
    SVar v;
    /** orientation (see <code>O_..</code> constants) currently: 0=horiz., 1=vert., 2=custom */
    int or;
    /** graphical start and length */
    int gBegin, gLen;
    /** graphical inter-categorial space */
    int gInterSpc=0;
    /** value begin and length */
    float vBegin, vLen;
    /** count for discrete axes */
    int datacount;
    /** vector of ticks */
    Vector ticks;
    /** type (see <code>T_..</code> constants) currently: 0=numerical,
	1=equidistant categories, 2=proportional category population, 3=equidistant */
    int type;
    /** for categorial vars - sequence of categories i.e. position for each categotry
	(ergo cseq[0] is the position of first cat) */
    int []cseq=null;
    
    /** create a new Axis with variable srcv, default orientation (horizontal) and default type guessing and default range
	@param srcv source variable */
    public Axis(SVar srcv) {
	v=srcv; ticks=null; or=0; gInterSpc=0;
	type=3; // some default type guessing
	if (v.isNum()) type=0;
	if (v.isCat()) type=2;
	setDefaultRange();
     };

    /** create new Axis with variable srvc, specified orientation and type and default range 
     @param srcv source variable
     @param orientation orientation
     @param axisType axis type */
    public Axis(SVar srcv, int orientation, int axisType) {
	v=srcv; type=axisType; or=orientation; ticks=null; gInterSpc=0;
	setDefaultRange();
    };

    /** change axis type (implicitely calls {@link setDefaultRange} but preserves
	cat sequence if switching between "compatible" types, i.e. 1 and 2) */
    public void setType(int nt) {
	if (nt==type) return;
	boolean reset=true;
	if ((nt==1 && type==2)||(nt==2 && type==1))
	    reset=false;
	type=nt;
	setDefaultRange(reset); // that one calls NotifyAll already
    };

    /** set geomery of the axis and notifies dependents upon change
	@param orientation orientation of the axis (horiz/vert)
	@param begin begin/anchor of the axis - i.e. the pixel to correspond to {@link vBegin}
	@param length length of the axis (note: may be negative if necessary)
     */
    public void setGeometry(int orientation, int begin, int len) {
	if(orientation!=or||begin!=gBegin||len!=gLen) { // lazy notification
	    gBegin=begin; gLen=len; or=orientation;
	    NotifyAll();
	};
    };

    /** for numerical variables - set range of the variable's values.
	@param begin begin/anchor of axis in data domain
	@param len length of the axis (can be negative if necessary) */
    public void setValueRange(float begin, float len) {
	if (vBegin!=begin||vLen!=len) { // lazy notification
	    vBegin=begin; vLen=len;
	    NotifyAll();
	};
    };

    /** for discrete axis types - set the data count
	@param dc data count, if <1 then set to 1 */
    public void setValueRange(int dc) {
	if (dc<1) dc=1;
	if (dc!=datacount) { // lazy notification
	    datacount=dc;
	    vBegin=0; vLen=dc; // this is necessary if get SensibleTick.. functions are used
	    NotifyAll();
	};
    };

    /** set default range for the axis (i.e. for numerical variable min, max are used, for all other types the maixmal count is used. For categorial types this also resets categories sequence to default (ordered by cat ID) - equalt to calling @link{setDefaultRange(true)} */
    public void setDefaultRange() { setDefaultRange(true); };

    /** set default range for the axis (i.e. for numerical variable min, max are used, for all other types the maixmal count is used.)
	@param reseCseq If <code>true</code> for categorial types this also resets categories sequence to default (ordered by cat ID) */    
    public void setDefaultRange(boolean resetCseq) {
	if (v.isNum() && type==0) {
	    vBegin=v.getMin();
	    vLen=v.getMax()-vBegin;
	} else {
	    datacount=v.size();
	};
	if (v.isCat() && type==1)
	    datacount=v.getNumCats();
	if (type==2||type==1) {
	    if (cseq==null || resetCseq) {
		if (v.getNumCats()>0) {
		    int i=0;
		    cseq=new int[v.getNumCats()];
		    while (i<cseq.length) { /* initial sequence is by cat ID */
			cseq[i]=i; i++;
		    };
		} else cseq=null;
	    };
	};
	NotifyAll();
    };

    /** get graphical position of case with index i (for categorial vars returns
	the same as getCatCenter called for the category of the case)
	@param i index of the case
	@returns graphical position of the case */
    public int getCasePos(int i) {	
	if (type==3) return gBegin+(int)(((float)gLen)/((float)datacount)*((float)i));
	if (type==0) return gBegin+(int)(((float)gLen)*(v.atF(i)-vBegin)/vLen);
	if (type==2||type==1) return getCatCenter(v.getCatIndex(i));
	return -1;
    };

    /** get graphical position of value <code>val</code> (for type=0 and 3 only)
	@param val value
	@return graphical position of the value */
    public int getValuePos(float val) {
	if (type==3) return gBegin+(int)(((float)gLen)/((float)datacount)*(val));
	if (type==0) return gBegin+(int)(((float)gLen)*(val-vBegin)/vLen);
	return -1;
    };

    /** get value corresponding to a geometrical position <code>val</code>
	(for type=0 and 3 only), hence this is the inverse of {@link getValuePos}	
	@param pos position on the screen
	@returns value corresponding to the supplied position
    */
    public float getValueForPos(int pos) {
	if (type==3) return ((float)(pos-gBegin))*((float)datacount)/((float)gLen);
	if (type==0) return vBegin+((float)(pos-gBegin))*vLen/((float)gLen);
	return -1;
    };

    /** clips supplied graphical value to axis' region
	@param gv graphical value
	@return clipped graphical value */
    public int clip(int gv) {
	return (gv<gBegin)?gBegin:((gv>gBegin+gLen)?gBegin+gLen:gv);    
    };
    
    /** get lower geometry for category of index i (type 1,2 only)
     @param i category index
     @return lower position of the category */
    public int getCatLow(int i) {	
	if (i<0||i>=v.getNumCats()) return -1;
	int xi=(cseq==null)?i:cseq[i];
	if (type==1) return gBegin+gLen*xi/datacount;
	int agg=0,j=0;
	while (j<xi) {
	    agg+=v.getSizeCatAt((cseq==null)?j:cseq[j]); j++;
	};
	return gBegin+gLen*agg/datacount;
    };

    /** get upper geometry for category of index i (type 1,2 only)
	@param i category index
	@return upper position of the category */
    public int getCatUp(int i) {	
	if (i<0||i>=v.getNumCats()) return -1;
	int xi=(cseq==null)?i:cseq[i];
	if (type==1) return gBegin+gLen*(xi+1)/datacount;
	int agg=0,j=0;
	while (j<=xi) {
	    agg+=v.getSizeCatAt((cseq==null)?j:cseq[j]); j++;
	};
	return gBegin+gLen*agg/datacount;
    };

    /** get central geometry for category of index i (just a faster way to get (Low+Up)/2 )
	@param i category index
	@return central position of the category */
    public int getCatCenter(int i) {
	if (i<0||i>=v.getNumCats()) return -1;
	int xi=(cseq==null)?i:cseq[i];
	if (type==1) return gBegin+gLen*xi/datacount+gLen/(2*datacount);
	int agg=0,j=0;
	while (j<=xi) {
	    agg+=(j==xi)
		?v.getSizeCatAt((cseq==null)?j:cseq[j])/2
		:v.getSizeCatAt((cseq==null)?j:cseq[j]); j++;
	};
	return gBegin+gLen*agg/datacount;
    };

    /** get category corresponding to a position on screen (type1 and 2 only)
	@param pos position
	@return category ID or -1 on failure (e.g. if not of type 1 or 2) */
    public int getCatByPos(int pos) {
	if (type!=1&&type!=2) return -1;
	if (pos<gBegin) return 0;
	if (cseq==null&&type==1) {
	    int rc=(pos-gBegin)/gLen;
	    if (rc<0) rc=0;
	    if (rc>=v.getNumCats()) rc=v.getNumCats()-1;
	    return rc;
	};
	int i=0, l=0, maxi=0, maxx=0, agg=0, aggp, aggp2, cs=v.getNumCats();
	int tot=datacount;       
	int[] invcs=null;
	if (cseq!=null) {
	    invcs=new int[cseq.length];
	    int ii=0;
	    while(ii<cseq.length) { invcs[cseq[ii]]=ii; ii++; };
	};
	if (type==1) tot=cs;
	while (i<cs) {
	    aggp=gBegin+gLen*agg/tot;
	    if  (aggp>maxx) { maxx=aggp; maxi=(cseq==null)?i:invcs[i]; };
	    if (type==1)
		l=1;
	    else
		l=v.getSizeCatAt((cseq==null)?i:invcs[i]);
	    agg+=l;
	    aggp2=gBegin+gLen*agg/tot;
	    if (pos>=aggp&&pos<aggp2) return (cseq==null)?i:invcs[i];
	    i++;
	};
	return maxi; // assuming pos is out of range, ergo return maxi
    };

    /** swap positions of two categories
	@param c1 category 1
	@param c2 category 2
	@return <code>true</code> on success, <code>false</code> on failure
	(i.e. some index was out of bounds) */
    public boolean swapCats(int c1, int c2) {
	if (cseq==null||c1<0||c2<0||c1>=cseq.length||c2>=cseq.length)
	    return false;
	if (c1!=c2) {
	    int i=cseq[c1]; cseq[c1]=cseq[c2]; cseq[c2]=i;
	};
	return true;
    };

    /** move category to another position in the sequence, all remaining
	categories between the current and new position will be moved
	correspondingly
	@param c category to move
	@param npos new position in the sequence - it is clipped if necessary,
	i.e. specifying values <0 will move it to the begining and >=cats will
	move it to the end of the sequence
	@return <code>true</code> on success, <code>false</code> on failure */
    public boolean moveCat(int c, int npos) {
	if (cseq==null||c<0||c>=cseq.length)
	    return false;
	if (npos<0) npos=0;
	if (npos>=cseq.length) npos=cseq.length-1;
	int cp=cseq[c];
	if (cp==npos) return true;
	if (cp<npos) {
	    int i=0; 
	    while(i<cseq.length) {
		if (cseq[i]>cp&&cseq[i]<=npos) cseq[i]--;
		i++;
	    };	    
	    cseq[c]=npos;
	} else {
	    int i=0;
	    while(i<cseq.length) {
		if (cseq[i]>=npos&&cseq[i]<cp) cseq[i]++;
		i++;
	    };
	    cseq[c]=npos;
	};
	return true;
    };

    /** for cat types return the position of a category in the sequence
	of categories. w/o reordering it's always c. It is often used
	in conjunction with {@link moveCat} as npos parameter when destination
	is also a category
	@param c category index
	@return position of the category in the sequence
    */
    public int getCatSeqIndex(int c) {
	if (cseq==null) return c;
	return (c<0||c>=cseq.length)?-1:cseq[c];
    };

    /** returns a tick distance that is somewhat "sensible" to be used for 
	ticks given mean required distance. The tick distance will be a power
	of 10. The result can be used to obtain more sophisticated tick
	values by simply dividing by 2,4 or 5 - or alternatively multipl.
	by 2, 2.5 or 5
	@param medDist mean required distance
	@return proposed tick distance */
    public float getSensibleTickDistance(int medDist) {
	return (float)Math.pow(10.0,Math.round(Math.log(vLen*((float)medDist)/((float)gLen))/Math.log(10.0)));
    };
    /** returns first visible tick given a tick distance. it is mostly
	used in conjunction with {@link getSensibleTickDistance}
	@param tickDist tick distance
	@return first visible tick mark
    */
    public float getSensibleTickStart(float tickDist) {
	return tickDist*((float)((int)(vBegin/tickDist)+1));
    };

    /** somewhat simple toString implementation, basically for debugging purposes */
    public String toString() {
	return "Axis(type="+type+",or="+or+",g["+gBegin+":"+(gBegin+gLen)+"],dc="+datacount+",cseq="+((cseq==null)?"<none>":"["+cseq.length+"]")+")";
    };
};
