package org.rosuda.deducer;

import org.rosuda.JGR.JGR;

public class Deducer {
	public String sayHello(){
		return "Hello From Java";
	}
	
	public void printHello(){
		JGR.MAINRCONSOLE.execute("'Hello from Deducer through JGR'");
	}
	
	public static void main(String[] args){}
}
