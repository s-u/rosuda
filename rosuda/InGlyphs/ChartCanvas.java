package org.rosuda.InGlyphs;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.SMarker;
import org.rosuda.ibase.toolkit.BaseCanvas;
import org.rosuda.ibase.toolkit.EzMenu;
import org.rosuda.ibase.toolkit.PPrimPolygon;
import org.rosuda.ibase.toolkit.PPrimRectangle;
import org.rosuda.ibase.toolkit.PlotPrimitive;
import org.rosuda.pograss.PoGraSS;

public class ChartCanvas extends BaseCanvas {

    static int nRows;
    static int nCols;

    GFrame frame;

    ChartCanvas(GFrame f, SMarker m) {
    	
		super(f,m);
		frame = f;
		
		String myMenu[]={"+","File","~File.Graph","~Edit","+","View","!RRotate","rotate","~Window","0"};
		EzMenu.getEzMenu(f,this,myMenu);

		nRows = frame.dataSet.recordSet.size();
		nCols = ((ArrayList)frame.dataSet.recordSet.get(0)).size();

		PoGraSS g = new PoGraSS();
		drawChart(g, frame.dataSet, frame.chartType, frame.scale, frame.getSize().width, frame.getSize().height);
		
	}

	public void paintBack(PoGraSS g) {
	  	drawChart(g, frame.dataSet, frame.chartType, frame.scale, this.getSize().width, this.getSize().height);
	}
	
	public void updateObjects() {
		
	}

	public String queryObject(int i) {

		return "var=" + String.valueOf(nCols);

	}

	public void drawChart(PoGraSS g, Dataset dataSet, String chartType, String scale, int gridWidth, int gridHeight) {  
		
		if (pp==null) {
			pp = new PlotPrimitive[nRows*nCols];
		} 
		 
		// Definition of the dimension as a fraction of the minimal frame dimension.

		Float FSize = Float.valueOf((String)"0.1");
		float dimension = Math.min(gridWidth,gridHeight) * FSize.floatValue();		

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

		float dim = dimension;
		
		float[] rays = new float[dataSet.recordSet.size()];
		float maxRay = 0;
		
		if (frame.scale.equals("same scale")) {
			for (int row = 0; row < dataSet.recordSet.size(); row++) {
				float sumT = dataSet.sumTotal();
				dim = dimension * (dataSet.sumInRow(row) / (sumT / nRows) ) / 2;
				if (dim>maxRay) maxRay=dim;
				rays[row]=dim;
			}
		}
		else if (frame.scale.equals("std scale")) {
			dim = dimension/2;
			maxRay = dimension/2;
			for (int row = 0; row < dataSet.recordSet.size(); row++)
				rays[row]=maxRay;
		}
		
		if (xLenght != 0) {
			xUnity = (gridWidth  - maxRay*2) / xLenght;
			coordCenterX = maxRay - xUnity * xMin;
		}
		else {
			coordCenterX = gridWidth / 2;
		}
		if (yLenght != 0) {
			yUnity = (gridHeight - maxRay*2) / yLenght;
			coordCenterY = maxRay - yUnity * yMin;
		}
		else {
			coordCenterY = gridHeight / 2;
		}
		
		float glyphCenterX = 0;
		float glyphCenterY = 0;

		// Start painting the polygons

		for (int row = 0; row < dataSet.recordSet.size(); row++) {

			ArrayList record = new ArrayList((ArrayList)dataSet.recordSet.get(row));

			// Compute the center of the polygon
			
			float ray = rays[row];
			
			glyphCenterX = coordCenterX + Float.valueOf((String)record.get(0)).floatValue() * xUnity;
			glyphCenterY = coordCenterY + Float.valueOf((String)record.get(1)).floatValue() * yUnity;
			
			// Compute the angles of the polygon
			
			if (chartType.equals((String)"Ponigoli")) {
				drawPonigoli(record, row, glyphCenterX, glyphCenterY, ray);
			}
			else if (chartType.equals((String)"Bars")) {
				drawBars(record, row, glyphCenterX, glyphCenterY, ray);
			}
			else if (chartType.equals((String)"Scatters")) {
				drawScatters(record, row, glyphCenterX, glyphCenterY);
			}
			else if (chartType.equals((String)"Stars")) {
			}
			else if (chartType.equals((String)"Lines")) {
			}
			else if (chartType.equals((String)"Boxes")) {
			}
			else if (chartType.equals((String)"Faces")) {
			}
		}
	}

	public void drawPonigoli(ArrayList record, int row, float glyphCenterX, float glyphCenterY, float r) {
		
		ArrayList angles = getAngles(record, row);
		
		float x1 = 0;
		float y1 = 0;
		float x2 = glyphCenterX + r;
		float y2 = glyphCenterY;
		float fAng = 0;

		for ( int col = 0; col < angles.size(); col++ ) {

			// Compute the triangle' vertex

			String SAng = (String)angles.get(col);
			Float FAng = Float.valueOf(SAng);
			fAng = fAng += FAng.floatValue();

			x1 = x2;
			y1 = y2;

			String SCos = String.valueOf(Math.cos(fAng));
			Float FCos = Float.valueOf(SCos);
			x2 = glyphCenterX + r * FCos.floatValue();

			String SSin = String.valueOf(Math.sin(fAng));
			Float FSin = Float.valueOf(SSin);
			y2 = glyphCenterY + r * FSin.floatValue();

			// Paint the triangle

			float x3Points[] = {glyphCenterX, x1, x2};
			float y3Points[] = {glyphCenterY, y1, y2};
			
			//Color color = Common.getHCLcolor(100);

                        PPrimPolygon pr = new PPrimPolygon();
			pp[row * nCols + col] = pr;
			pr.pg = new Polygon();

			pr.col=Common.getHCLcolor(col*360/angles.size());

			pr.pg.addPoint(Float.valueOf(String.valueOf(glyphCenterX)).intValue(),Float.valueOf(String.valueOf(glyphCenterY)).intValue());
			pr.pg.addPoint(Float.valueOf(String.valueOf(x1)).intValue(),Float.valueOf(String.valueOf(y1)).intValue());
			pr.pg.addPoint(Float.valueOf(String.valueOf(x2)).intValue(),Float.valueOf(String.valueOf(y2)).intValue());

			pr.ref=new int[1];
			pr.ref[0]=col;

		}
	}

	public void drawBars(ArrayList record, int row, float glyphCenterX, float glyphCenterY, float base) {
		
		int cX = Float.valueOf(String.valueOf(glyphCenterX)).intValue();
		int cY = Float.valueOf(String.valueOf(glyphCenterY)).intValue();
		int b = Float.valueOf(String.valueOf(base/7)).intValue();
		int h;		
		int x = cX; //- (record.size()*b/2);
			
		for (int col=2; col<record.size(); col++) {

                    h = b * Integer.valueOf((String)record.get(col)).intValue()/5;

                    PPrimRectangle pr = new PPrimRectangle();
                    pp[row * nCols + col] = pr;
                    pr.r = new Rectangle(x+b*(col-2),cY-h,b,h);
                    pr.ref=new int[1];
                    pr.ref[0]=col;		

		}
	}

	public void drawScatters(ArrayList record, int row, float glyphCenterX, float glyphCenterY) {

		int cX = Float.valueOf(String.valueOf(glyphCenterX)).intValue();
		int cY = Float.valueOf(String.valueOf(glyphCenterY)).intValue();
					
		for (int col=2; col<record.size(); col++) {
                    PPrimRectangle pr = new PPrimRectangle();
                    pp[row * nCols + col] = pr;
                    pr.r = new Rectangle(cX,cY,2,2);

                    pr.ref=new int[1];
                    pr.ref[0]=col;		

		}
	}
	
	public ArrayList getAngles(ArrayList record, int row) {

		ArrayList angles = new ArrayList();

		ArrayList nRecord = frame.dataSet.normalizedRecord(row, 2, record.size());
	
		for ( int col = 0; col < nRecord.size(); col++ ) {
			String SnElement = (String)nRecord.get(col);
			Float FnElement = Float.valueOf(SnElement);
			float fAngle = 0;
			fAngle += 2 * Math.PI * FnElement.floatValue();
			angles.add(String.valueOf(fAngle));
		}

		return angles;
	}

	public ArrayList getStdRecord(ArrayList record, int row) {

		return frame.dataSet.normalizedRecord(row, 2, record.size());

	}
}