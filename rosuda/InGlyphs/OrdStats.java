
/**
 * OrdStats - ordinal statistics of a variable,
 * used internally by {@link BoxCanvas}
 * to get necessary information to plot bopxplots
 **/
class OrdStats { // get ordinal statistics to be used in boxplot

	double med, uh, lh, uh15, lh15, uh3, lh3;
	int[] lastR;
	int lastTop;
	/** indexes of points just above/below the 1.5 hinge
	beware, this is relative to the used r[] so
	use with care and only with the corresponding r[] */
	int lowEdge, highEdge; 

	OrdStats() { med=uh=lh=uh3=lh3=0; };

	double medFrom(SVar v,int[] r,int min,int max) {
		return (((max-min)&1)==0)?
		v.atF(r[min+(max-min)/2])
		:((v.atF(r[min+(max-min)/2])+v.atF(r[min+(max-min)/2+1]))/2);
	}

	void update(SVar v, int[] r) {
		update(v,r,r.length);
	}
	
	/* v=variable, r=ranked index as returned by getRanked, n=# of el to use */
	void update(SVar v, int[] r, int n) {
		lastTop=n;
		med=medFrom(v,r,0,n-1);
		uh=medFrom(v,r,n/2,n-1);
		if ((n&1)==1) {
			lh=medFrom(v,r,0,n/2-1);
		} 
		else {
			lh=medFrom(v,r,0,n/2);
		} 
		lh15=lh-(double)1.5*(uh-lh);
		lh3=lh-3*(uh-lh);
		double x=lh;
		int i=n/4; // find lh15 as extreme between lh and lh15
		while (i>=0) {
			double d=v.atF(r[i]);
			if (d<lh15) break;
			if (d<x) x=d;
			i--;
		}
		lowEdge=i;
		lh15=x;
		uh15=uh+(double)1.5*(uh-lh);
		uh3=uh+3*(uh-lh);
		x=uh;
		i=n*3/4-1; if (i<0) i=0; // find uh15
		while (i<n) {
			double d=v.atF(r[i]);
			if (d>uh15) {
				break;
			} 
			if (d>x) {
				x=d;
			} 
			i++;
		}
		uh15=x;
		highEdge=i;
		lastR=r;
	}
}