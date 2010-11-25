package org.spark.runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;



/**
 * Class for analyzing collected data
 * @author Monad
 */
public class DataAnalyzer {
	/* Experimental data */
	private final ArrayList<Double> experimentalData;
	
	/* Time points for the experimental data */
	private final ArrayList<Long> experimentalTimePoints;
	
	/* Method name */
	private String method;
	
	
	/**
	 * Creates a data analyzer for the given experimental data file
	 * @param fname
	 * @param methodName
	 */
	public DataAnalyzer(String fname, String methodName) throws Exception {
		experimentalData = new ArrayList<Double>();
		experimentalTimePoints = new ArrayList<Long>();
		
		readFile(fname);
		
		this.method = methodName;
	}
	
	
	/**
	 * Reads data from the file
	 * @param fname
	 */
	private void readFile(String fname) throws Exception {
		FileReader fr = new FileReader(fname);
		BufferedReader br = new BufferedReader(fr);

		String line = br.readLine();
		if (line == null)
			return;
		
		// TODO: analyze first line

		for (int i = 2;; i++) {
			line = br.readLine();
			if (line == null)
				break;
			
			String[] vals = line.split(",");
			if (vals.length != 2)
				throw new Exception("Bad format at line " + i + ": " + line);
			
			long tick = Long.parseLong(vals[0]);
			double val = Double.parseDouble(vals[1]);
			
			experimentalTimePoints.add(tick);
			experimentalData.add(val);
		}
		
		br.close();
	}
	
	
	
	/**
	 * Analyzes the given data and returns the error
	 * @return
	 */
	public double analyze(DataSet dataSet, String variableName) {
		double error = 0;
		
		ArrayList<Number> data = dataSet.getDataAtGivenTicks(variableName, experimentalTimePoints);
		if (data == null)
			return 1e+10;
			
		int n = Math.min(data.size(), experimentalData.size());
		if (n <= 1)
			return 1e+10;
		
		if (method.equals("Least Squares")) {
			for (int i = 0; i < n; i++) {
				double val1 = data.get(i).doubleValue();
				double val2 = experimentalData.get(i);
				
				error += (val1 - val2) * (val1 - val2);
			}
			
			error = Math.sqrt(error);
		}
		else if (method.equals("Correlation")) {
			double s1 = 0, s2 = 0;
			double m1 = 0, m2 = 0;
			double A = 0;
			
			for (int i = 0; i < n; i++) {
				double val1 = data.get(i).doubleValue();
				double val2 = experimentalData.get(i);

				A += val1 * val2;
				
				m1 += val1;
				m2 += val2;
				
				s1 += val1 * val1;
				s2 += val2 * val2;
			}
			
			s1 = Math.sqrt((s1 - m1 * m1 / n) / (n - 1));
			s2 = Math.sqrt((s2 - m2 * m2 / n) / (n - 1));
			
			double r = (A - m1 * m2 / n) / ((n - 1) * s1 * s2);
			
			// r = 1 => error = 0
			error = 1 - Math.abs(r);
		}
		
		return error;
	}
	
}
