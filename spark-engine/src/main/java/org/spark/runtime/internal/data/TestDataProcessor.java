package org.spark.runtime.internal.data;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.spark.runtime.data.DataObject;
import org.spark.runtime.data.DataRow;


public class TestDataProcessor extends DataProcessor {
	private final ArrayList<DataRow> rows;
	
	
	public TestDataProcessor() {
		rows = new ArrayList<DataRow>();
	}
	
	
	@Override
	public void finalizeProcessing() throws Exception {
		int n = rows.size();
		
		if (n == 0)
			return;

		DataRow row = rows.get(0);
		String[] headers = row.getNames();
		if (headers.length == 0)
			return;
	
		FileOutputStream fos = new FileOutputStream("data.csv");
		PrintStream out = new PrintStream(fos);

		// Print out headers
		for (int i = 0; i < headers.length; i++) {
			out.print(headers[i]);
			if (i < headers.length - 1)
				out.print(',');
		}
		
		out.println();
		for (int k = 0; k < n; k++) {
			row = rows.get(k);
			// Save data values
			for (int i = 0; i < headers.length; i++) {
				DataObject obj = row.get(headers[i]);
				if (obj == null)
					out.print("null");
				else
					out.print(obj.toString());
				if (i < n - 1)
					out.print(',');
			}
			
			out.println();
		}
		
		out.close();
	}

	@Override
	public void processDataRow(DataRow row) throws Exception {
		rows.add(row);
	}
	
}
