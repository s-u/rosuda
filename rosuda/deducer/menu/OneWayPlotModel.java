package org.rosuda.deducer.menu;

public class OneWayPlotModel {
	public boolean plot = false;
	public double alpha = .2;
	public boolean box=true;
	public boolean scale=true;
	public boolean points=true;
	
	public String getCmd(String subn,String outcomes,String factor){
		String cmd="";
		if(plot)
			cmd+="oneway.plot(variables="+outcomes+",factor.var="+factor+",data="+subn+
				(alpha!=.2?",\n\talpha="+alpha:"")+
				(box?"":",\n\tbox=FALSE")+
				(points?"":",\n\tpoints=FALSE")+
				(scale?"":",\n\tscale=FALSE")+")\n";
		
		return cmd;
	}
	
	
}
