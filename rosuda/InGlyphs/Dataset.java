package org.rosuda.InGlyphs;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Dataset {

	ArrayList recordSet = new ArrayList();
	
	public ArrayList getRecordSet() {
		return recordSet;
	}

	public Dataset(String strFileName) throws IOException,EOFException {
		
		StringTokenizer dbTokenizer = null;
		ArrayList dataTypes = new ArrayList();
		String dbLine = "";
		String dbField = null;


		// Fill the dbTable array

		BufferedReader dbReader = new BufferedReader(new FileReader(strFileName));

		while (dbLine != null) {
			dbLine = dbReader.readLine();
			ArrayList record = new ArrayList();
			if (dbLine != null) {
				dbTokenizer = new StringTokenizer(dbLine);
				dataTypes = new ArrayList();
				while (dbTokenizer.hasMoreTokens()) {
					dbField = dbTokenizer.nextToken();
					record.add(dbField);
                    
					String type = "nominal";
					try {
						Float FDbField = new Float(dbField);
						type = "real";

						Integer IDbField = new Integer(FDbField.intValue());
						Float FFDbField = new Float(IDbField.floatValue());
						System.out.print(FFDbField);
						System.out.print(" ");
						System.out.println(FFDbField);
						if (FFDbField.equals(FDbField)) {
							type = "discrete";
						}
					}
					catch (Exception ex) {
                    	
					}
					dataTypes.add(type);                                  
				}
				recordSet.add(record);
			}
            
		}

		System.out.println(dataTypes);
	}

    
	public float limitInCol(int col, String limitType) {
	
		float limit = 0;
		
		for ( int row = 0; row < recordSet.size(); row++ ) {

			ArrayList record = new ArrayList((ArrayList)recordSet.get(row));

			String SElement = (String)record.get(col);
			Float FElement = Float.valueOf(SElement);
			float fElement = FElement.floatValue();
            
			if (row == 0) {
				limit = fElement;
			}
			if (limitType == "Max") {
				limit = Math.max(limit,fElement);
			}
			if (limitType == "Min") {
				limit = Math.min(limit,fElement);
			}
		}
		
		return limit;
	}
	
	public ArrayList normalizedRecord(int row,int fromCol,int toCol) {
		
		ArrayList record = new ArrayList((ArrayList)recordSet.get(row));
		ArrayList normalized = new ArrayList();
		
		float fSum = 0;
		
		for (int col = fromCol; col < toCol; col++) {
			String SRes = (String)record.get(col);
			Float FRes = Float.valueOf(SRes);
			fSum += FRes.floatValue();
			}
		for (int col = 2; col < record.size(); col++) {
			String SRes = (String)record.get(col);
			Float FRes = Float.valueOf(SRes);
			float normalizedElement = FRes.floatValue() / fSum;
			normalized.add(String.valueOf(normalizedElement));
		}
        
		return normalized;
	}
	
	public float sumInRow(int row) {
		
		ArrayList record = new ArrayList((ArrayList)recordSet.get(row));
		float fSum = 0;
		
		for (int col = 2; col < record.size(); col++) {
			String SRes = (String)record.get(col);
			Float FRes = Float.valueOf(SRes);
			fSum += FRes.floatValue();
		}
        
		return fSum;
	}
	
	public float sumTotal() {
		
		float fSum = 0;
		
		for (int row = 0; row < recordSet.size(); row++) {
		
			ArrayList record = new ArrayList((ArrayList)recordSet.get(row));
					
			for (int col = 2; col < record.size(); col++) {
				String SRes = (String)record.get(col);
				Float FRes = Float.valueOf(SRes);
				fSum += FRes.floatValue();
			}
		}
        
		return fSum;
	}
}
