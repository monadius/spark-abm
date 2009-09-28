package org.spark.test.other;

public class ArrayTest {

	public static void main(String[] args) {
		double[][] array1 = new double[3000][3000];
		Array2D array2 = new Array2D(3000, 3000);

		long start, end;
		long time1, time2;
		
		start = System.currentTimeMillis();
		
		for (int i = 0; i < 3000; i++) {
			for (int j = 0; j < 3000; j++) {
				array1[i][j] = 0d;
//				array1[i][j] = (i + j) * (i + j) / (i * i + 1);
				array1[i][j] = Math.sin(i);
			}
		}
		
		end = System.currentTimeMillis();
		time1 = end - start;

		
		start = System.currentTimeMillis();
		
		for (int i = 0; i < 3000; i++) {
			for (int j = 0; j < 3000; j++) {
				array2.set(i, j, 0d);
//				array2.set(i, j, (i + j) * (i + j) / (i * i + 1));
				array2.set(i, j, Math.sin(i));
			}
		}

		end = System.currentTimeMillis();
		time2 = end - start;

		System.out.println("Array1: " + time1);
		System.out.println("Array1: " + time2);
		
	}

}
