package org.rosuda.InGlyphs;

import java.util.*;
import drasys.or.nonlinear.*;


public class TrigFunction extends EquationSolution implements FunctionI  {
	
	public static ArrayList coefficients = null;
	public static ArrayList angles = null;
	public static ArrayList takePIComplements = null;
	public static double solution = 0;
	public static boolean solved = false;

	
	public TrigFunction(ArrayList coeffs) {
		
		coefficients = coeffs;
		
		float maxCoeff = 0;
		for ( int i = 0; i < coefficients.size(); i++ ) {

			String SCoefficient = (String)coefficients.get(i);
            Float FCoefficient = Float.valueOf(SCoefficient);
            float fCoefficient = FCoefficient.floatValue();
            if (i == 0) maxCoeff = fCoefficient;
            maxCoeff = Math.max(maxCoeff,fCoefficient);
		}

		takePIComplements = new ArrayList();
		angles = new ArrayList();
		for ( int i = 0; i < coefficients.size(); i++ ) {
			takePIComplements.add("no");
			angles.add("0");
		}

		solvingLoop(1/maxCoeff, 20);

		
		boolean recompute = false;
		
		for ( int i = 0; i < coefficients.size(); i++ ) { 
			String Sangle = (String)angles.get(i);
            Float Fangle = Float.valueOf(Sangle);
            float fangle = Fangle.floatValue();
            double delta = Math.abs( ( fangle - (Math.PI / 2) ) / fangle );
            if (delta < 0.1) {
            	takePIComplements.set(i,"yes");
            	recompute = true;
            }
		}

		if (recompute == true) {
			solvingLoop(1/maxCoeff, 20);
		}
				
		/*for ( int i = 0; i < coefficients.size(); i++ ) {
			String SCoefficient = (String)coefficients.get(i);
            Float FCoefficient = Float.valueOf(SCoefficient);
            double angle = 0;
            if ((String)takePIComplements.get(i) == "no") {
				angle = Math.asin(FCoefficient.floatValue() * s);
            }
            else {
            	angle = Math.PI - Math.asin(FCoefficient.floatValue() * s);
            }
			angles.set(i,String.valueOf(angle));

		}*/
		
		System.out.println(" ");
		System.out.println("FINISHED !!!");
		System.out.println("Solved = " + solved);
		System.out.println("Recompute = " + recompute);
		System.out.println(" ");
		System.out.println(" ");
		
	}

	void solvingLoop(float interval, int nIntervals) {

		float subInterval = interval/nIntervals;
		
		float firstPoint = 0;
		float secondPoint = 0;
		double functionFirstPoint = function(0);
		double functionSecondPoint = function(0);

		int iter = -1;
		
		solved = false;
		
		while ((solved == false) & (iter < nIntervals)) {
			iter ++;
			
			System.out.println(" ");
			System.out.println(" ");
			System.out.println("ITERATION" + iter);
			
			firstPoint = iter * subInterval;
			secondPoint = (iter+1) * subInterval;
			functionSecondPoint = function(secondPoint);
			

			
			if ( ( (functionFirstPoint <= 2 * Math.PI) & (functionSecondPoint >= 2 * Math.PI) ) | ( (functionFirstPoint >= 2 * Math.PI) & (functionSecondPoint <= 2 * Math.PI) ) ) {
				try {
					System.out.println("SOLVING1 !!!");
					solution = solve(this, firstPoint, secondPoint, 2 * Math.PI);
					solved = true;
				}
				catch (Exception e) {
					System.out.println(e);
				}
			}

		}
	}

	public double function(double x) {
		
		double sumAngles = 0;
		
		for ( int i = 0; i < coefficients.size(); i++ ) {
			String SCoefficient = (String)coefficients.get(i);
            Float FCoefficient = Float.valueOf(SCoefficient);

            double angle = 0;
            double asinArgument = 0;
            if (FCoefficient.floatValue() * x > 1) asinArgument = 1;
            else asinArgument = FCoefficient.floatValue() * x;
            
            if ((String)takePIComplements.get(i) == "no") {
				angle = Math.asin(asinArgument);
            }
            else {
            	angle = Math.PI - Math.asin(asinArgument);
            }
            
            angles.set(i,String.valueOf(angle));
			sumAngles += angle;
		}
		System.out.println(" ");
		System.out.println(takePIComplements);
		System.out.println(x);
		System.out.println(angles);
		System.out.println(sumAngles);
				
		return sumAngles;
	}
}
