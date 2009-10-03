/*
  Copyright 2008 by Alexey Solovyev and University of Pittsburgh
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package org.spark.math;

import java.io.Serializable;

/**
 * Basic implementation of a matrix class
 */
public class Matrix implements Serializable
{
	private static final long serialVersionUID = -9069261486766928469L;

	// Data is stored in a double array
    private double[][]  data;

    /**
     * m is the number of rows
     * n is the number of columns
     */
    private int         m, n;

    /**
     * Constructs an empty matrix of the specified size
     * @param m The number of rows
     * @param n The number of columns
     * @throws Error if the size is incorrect
     */
    public Matrix(int m, int n)
    {
        if (m <= 0 || n <= 0)
            throw new Error("Error");

        this.m = m;
        this.n = n;
        this.data = new double[m][n];
    }

    /**
     * Constructs a copy of the given matrix
     * @param x The matrix to be copied
     * @throws Exception if the argument is null
     */
    public Matrix(Matrix x) throws Exception
    {
        if (x == null)
            throw new Exception("Error");

        this.m = x.m;
        this.n = x.n;
        this.data = new double[m][n];

        for (int i = 0; i < m; i++)
            System.arraycopy(x.data[i], 0, data[i], 0, n);
    }

    /**
     * Constructs a matrix from the array.
     * It is assumed that the dimensions of the subarrays are all the same.
     */
    public Matrix(double[][] array) throws Exception
    {
        if (array == null)
            throw new Exception("Error");

        this.m = array.length;
        this.n = array[0].length;
        this.data = new double[m][n];

        for (int i = 0; i < m; i++)
        {
            assert array[i].length == n : "The incorrect dimension";
            System.arraycopy(array[i], 0, data[i], 0, n);
        }
    }

    /**
     * Creates identity matrix of the specific size
     * @param n The size of the matrix
     * @return the identity matrix of specific size
     * @throws Exception if the size is incorrect
     */
    public static Matrix identityMatrix(int n) throws Exception
    {
        Matrix id = new Matrix(n, n);
        for (int i = 0; i < n; i++)
            id.data[i][i] = 1.0;

        return id;
    }

    /**
     * @return the data array of this matrix
     */
    public double[][] getData()
    {
        return data;
    }

    /**
     * @return the number of rows of this matrix
     */
    public int getRowsNumber()
    {
        return m;
    }

    /**
     * @return the number of columns of this matrix
     */
    public int getColsNumber()
    {
        return n;
    }

    /**
     * @return the (i,j)-th element of the matrix
     */
    public double get(int i, int j)
    {
        return data[i][j];
    }


    /**
     * Sets the value of the (i,j)-th element of the matrix
     */
    public void set(int i, int j, double val)
    {
        data[i][j] = val;
    }

    /**
     * Converts the matrix to the array
     */
    public double[][] toArray()
    {
        double[][] array = new double[m][n];

        for (int i = 0; i < m; i++)
            System.arraycopy(data[i], 0, array[i], 0, n);

        return array;
    }

    /**
     * This operation adds the matrix y to this matrix
     * @return the modified matrix
     * @throws Exception if the matrices have unequal sizes
     */
    public Matrix add(Matrix y) throws Exception
    {
        if (y == null || m != y.m || n != y.n)
            throw new Exception("Error");

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                data[i][j] += y.data[i][j];

        return this;
    }

    /**
     * This operation subtracts the matrix y from this matrix
     * @return the modified matrix
     * @throws Exception if the matrices have unequal sizes
     */
    public Matrix sub(Matrix y) throws Exception
    {
        if (y == null || m != y.m || n != y.n)
            throw new Exception("Error");

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                data[i][j] -= y.data[i][j];

        return this;
    }

    /**
     * This operation multiplies this matrix by a matrix if both matrices are square matrices.
     * Warning! This method can change the data array of the matrix. Hence the previous call of
     * <tt>getData()</tt> is invalid.
     * @return the modified matrix
     * @throws Exception if the matrices have incompatible sizes
     */
    public Matrix mul(Matrix y) throws Exception
    {
        if (y == null || n != m || n != y.m || y.n != y.m)
            throw new Exception("Error");

        double[][] newdata = new double[n][n];

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                double r = 0;
                for (int k = 0; k < n; k++)
                    r += data[i][k] * y.data[k][j];

                newdata[i][j] = r;
            }
        }

        this.data = newdata;
        return this;
    }

    /**
     * This operation multiplies this matrix by a constant value
     * @return the modified matrix
     */
    public Matrix mul(double v)
    {
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                data[i][j] *= v;

        return this;
    }

    /**
     * This operation negates this matrix
     * @return the modified matrix
     */
    public Matrix negate()
    {
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                data[i][j] = -data[i][j];

        return this;
    }

    /**
     * This operation adds up two matrices
     * @return the sum of the arguments
     * @throws Exception if the matrices have unequal sizes
     */
    public static Matrix add(Matrix x, Matrix y) throws Exception
    {
        return new Matrix(x).add(y);
    }

    /**
     * This operation subtracts the second matrix from the first
     * @return the difference between the arguments
     * @throws Exception if the matrices have unequal sizes
     */
    public static Matrix sub(Matrix x, Matrix y) throws Exception
    {
        return new Matrix(x).sub(y);
    }

    /**
     * This operation multiplies two matrices
     * @return the product of the arguments
     * @throws Exception if the matrices have incompatible sizes
     */
    public static Matrix mul(Matrix x, Matrix y) throws Exception
    {
        if (x == null || y == null)
            throw new Exception("Error");
        if (x.n != y.m)
            throw new Exception("Error");

        Matrix z = new Matrix(x.m, y.n);
        double[][] xdata = x.getData();
        double[][] ydata = y.getData();
        double[][] zdata = z.getData();

        for (int i = 0; i < x.m; i++)
        {
            for (int j = 0; j < y.n; j++)
            {
                double r = 0;
                for (int k = 0; k < x.n; k++)
                    r += xdata[i][k] * ydata[k][j];

                zdata[i][j] = r;
            }
        }

        return z;
    }

    /**
     * This operation multiplies a matrix by a constant
     * @return the result of the multiplication
     */
    public static Matrix mul(Matrix x, double v) throws Exception
    {
        return new Matrix(x).mul(v);
    }

    /**
     * This operation multiplies a constant by a matrix
     * @return the result of the multiplication
     */
    public static Matrix mul(double v, Matrix x) throws Exception
    {
        return new Matrix(x).mul(v);
    }

    /**
     * This operation negates a matrix
     * @return the result of the negation
     */
    public static Matrix negate(Matrix x) throws Exception
    {
        return new Matrix(x).negate();
    }

    /**
     * This operation transposes this matrix
     * @return the modified matrix
     */
    public Matrix transpose()
    {
        double[][] newdata = new double[n][m];

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                newdata[j][i] = data[i][j];

        data = newdata;
        return this;
    }

    /**
     * This operation transposes a matrix
     * @return the transposed matrix
     */
    public static Matrix transpose(Matrix x) throws Exception
    {
        return new Matrix(x).transpose();
    }


    /**
     * @return the norm of the matrix subordinated to the Chebyshev's norm
     */
    public double normMax()
    {
        // norm = max( sum(abs(a[i][j]), j = 0..n-1), i = 0..m-1 )
        double norm = 0;

        for (int i = 0; i < m; i++)
        {
            double r = 0;
            for (int j = 0; j < n; j++)
                r += Math.abs(data[i][j]);

            if (r > norm)
                norm = r;
        }

        return norm;
    }

    /**
     * @return the squared Euclidean norm of the matrix
     */
    public double normEuclideanSquared()
    {
        double norm = 0;

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                norm += data[i][j] * data[i][j];

        return norm;
    }

    /**
     * @return the Euclidean norm of the matrix
     */
    public double normEuclidean()
    {
        return Math.sqrt(normEuclideanSquared());
    }

    /**
     * @return the norm of the matrix subordinated to the 1-norm
     */
    public double norm1()
    {
        // norm = max( sum(abs(a[i][j]), i = 0..m-1), j = 0..n-1 )
        double norm = 0;

        for (int j = 0; j < n; j++)
        {
            double r = 0;
            for (int i = 0; i < m; i++)
                r += Math.abs(data[i][j]);

            if (r > norm)
                norm = r;
        }

        return norm;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer(m * n * 5);

        for (int i = 0; i < m; i++)
        {
            str.append('[');
            for (int j = 0; j < n; j++)
            {
                str.append(data[i][j]);
                if (j == n - 1)
                    str.append("]\n");
                else
                    str.append(", ");
            }
        }

        return str.toString();
    }

}
