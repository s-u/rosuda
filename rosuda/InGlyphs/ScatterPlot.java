package org.rosuda.InGlyphs;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public final class ScatterPlot {
    
	public static void draw(Dataset dataSet, String proportionalMethod, Graphics2D g2, int gridWidth, int gridHeight) {

        
        // Definition of the polygonRay as a fraction of the minimal frame dimension.
                

Float FSize = Float.valueOf((String)"0.1");
        float polygonRay = Math.min(gridWidth,gridHeight) * FSize.floatValue();

		
		
		// Retrieve the maximal dimensions
		
        float xMax = dataSet.limitInCol(0,"Max");
        float xMin = dataSet.limitInCol(0,"Min");
        float yMax = dataSet.limitInCol(1,"Max");
        float yMin = dataSet.limitInCol(1,"Min");
        
        float xLenght = xMax - xMin;
        float yLenght = yMax - yMin;
        
        // Compute the drawing unity
        
        float xUnity = 0;
        float yUnity = 0;
        float coordCenterX = 0;
        float coordCenterY = 0;
        
		if (xLenght != 0) {
        	xUnity = gridWidth / xLenght;
        	coordCenterX = xUnity * xMin;
		}
		else {
			coordCenterX = gridWidth / 2;
		}
        if (yLenght != 0) {
        	yUnity = gridHeight / yLenght;
        	coordCenterY = yUnity * yMin;
        }
        else {
        	coordCenterY = gridHeight / 2;
		}

		float sumT = dataSet.sumTotal();
		float nRows = dataSet.recordSet.size();

		// Start painting the polygons

        for ( int row = 0; row < dataSet.recordSet.size(); row++ ) {

            ArrayList record = new ArrayList((ArrayList)dataSet.recordSet.get(row));
			ArrayList angles = new ArrayList();

			Float Fr = Float.valueOf((String)"1");	
			float r = Fr.floatValue();

			// Compute the center of the polygon

            String SPolCenterX = (String)record.get(0);
            Float FPolCenterX = Float.valueOf(SPolCenterX);
            float fPolCenterX = FPolCenterX.floatValue();

            String SPolCenterY = (String)record.get(1);
            Float FPolCenterY = Float.valueOf(SPolCenterY);
            float fPolCenterY = FPolCenterY.floatValue();
            
 			float polCenterX = coordCenterX + fPolCenterX * xUnity;           
            float polCenterY = coordCenterY + fPolCenterY * yUnity;

			// Compute the angles of the polygon

            
			
			drawInscribedPolygon(g2, polCenterX, polCenterY, r, angles);

        }
        
        
        // Draw the axis
        
		g2.draw(new Line2D.Double(0, coordCenterY ,gridWidth, coordCenterY ) );
		g2.draw(new Line2D.Double(coordCenterX, 0, coordCenterX, gridHeight) );
		
    }

	public static void drawInscribedPolygon(Graphics2D g2, float polCenterX, float polCenterY, float polygonRay, ArrayList angles) {
		
		// Paint the circunscribed cercle

		g2.draw(new Ellipse2D.Double(polCenterX - polygonRay, polCenterY - polygonRay, polygonRay * 2, polygonRay * 2));

	}
	
}
	


