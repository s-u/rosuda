import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public final class Barchart {
    
	public static void draw(Dataset dataSet, String proportionalMethod, Graphics2D g2, int gridWidth, int gridHeight) {

			
		Float Base = Float.valueOf((String)"20");	
		float base = Base.floatValue();
		
		float nCol = ((ArrayList)dataSet.getRecordSet().get(0)).size() - 2;
		
		System.out.println(nCol);
		
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
        	xUnity = (gridWidth - base * nCol * 2) / xLenght;
        	coordCenterX = base * nCol - xUnity * xMin;
		}
		else {
			coordCenterX = gridWidth / 2;
		}
        if (yLenght != 0) {
        	yUnity = gridHeight / yLenght;
        	coordCenterY = - yUnity * yMin;
        }
        else {
        	coordCenterY = gridHeight / 2;
		}

		float sumT = dataSet.sumTotal();
		float nRows = dataSet.recordSet.size();

		// Start painting the barchart

        for ( int row = 0; row < dataSet.recordSet.size(); row++ ) {

            ArrayList record = new ArrayList((ArrayList)dataSet.recordSet.get(row));

			float b = base * ( dataSet.sumInRow(row) / ( sumT ) );

			// Compute the center of the polygon

            String SBarOrigX = (String)record.get(0);
            Float FBarOrigX = Float.valueOf(SBarOrigX);
            float fBarOrigX = FBarOrigX.floatValue();

            String SBarOrigY = (String)record.get(1);
            Float FBarOrigY = Float.valueOf(SBarOrigY);
            float fBarOrigY = FBarOrigY.floatValue();
            
 			float barOrigX = coordCenterX + fBarOrigX * xUnity;           
            float barOrigY = coordCenterY + fBarOrigY * yUnity;

			// Compute the height of the barchat

            
			
			drawBarchart(g2, barOrigX, barOrigY, b, record);

        }
        
        
        // Draw the axis
        
		g2.draw(new Line2D.Double(0, coordCenterY ,gridWidth, coordCenterY ) );
		g2.draw(new Line2D.Double(coordCenterX, 0, coordCenterX, gridHeight) );
		
    }

	public static void drawBarchart(Graphics2D g2, float barOrigX, float barOrigY, float base, ArrayList record) {
		
		// Paint the barchart

		// Start painting the trirecord

        float x = 0;
        float fHeight = 0;
        	
        for ( int col = 2; col < record.size(); col++ ) {

            String SHeight = (String)record.get(col);
            Float FHeight = Float.valueOf(SHeight);
            fHeight = FHeight.floatValue();

            x = barOrigX + base * (col+1);
            
            
            
			g2.setStroke(new BasicStroke(2.0f));
			g2.draw(new Rectangle2D.Double(x, barOrigY - fHeight, base, fHeight));

        }        
	}
}
