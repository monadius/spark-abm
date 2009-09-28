package org.spark.utils;

import jsr166y.ForkJoinPool;
import extra166y.ParallelArray;
import extra166y.ParallelDoubleArray;
import extra166y.Ops.DoubleOp;

//import jsr166y.forkjoin.*;
/**
 * @author max
 *
 */
public class Parallel2dDoubleArray extends ParallelDoubleArray
{
	Parallel2dDoubleArray(ForkJoinPool executor, int xSize, int ySize) 
	{
		this(executor, new double[xSize*ySize], xSize, ySize);			
	}

	
	/**
	 * Simplest Parallel2dDoubleArray with only size parameters 
	 * @param xSize
	 * @param ySize
	 */
	public Parallel2dDoubleArray(int xSize, int ySize) 
	{
		this(ParallelArray.defaultExecutor(), xSize, ySize);			
	}

	public Parallel2dDoubleArray(Parallel2dDoubleArray Original, ParallelDoubleArray Generated) 
	{
		this(Original.getExecutor(), Generated.getArray(), Original.getRowsNumber(), 
				Original.getColsNumber());				
	}
	
	public Parallel2dDoubleArray(ForkJoinPool executor, double[] array,  int xSize, int ySize)
	{
		super(executor, array, xSize*ySize);
		rows = xSize;
		columns = ySize;
		size = rows*columns;
	}
	
	
	/**
	 * return new Parallel2dDoubleArray
	 * @param array data which will base of new array
	 * @param xSize
	 * @param ySize
	 * @return
	 */
	public static Parallel2dDoubleArray CreateFromDouble(double[] array, int xSize, int ySize)
	{
		return new Parallel2dDoubleArray(ParallelArray.defaultExecutor(), array, xSize, ySize);		
	}
	
	Parallel2dDoubleArray(ForkJoinPool executor, double[] array, int xSize,
			int ySize, int limit) 
	{
		super(executor, array, limit);
		rows = ySize; 
		columns = xSize;
		size = rows*columns;
		if (executor == null || array == null)
			throw new NullPointerException();
		if (limit < 0 || limit > array.length)
			throw new IllegalArgumentException();
	}	
	
	
	//ParallelDoubleArray BaseArray;
	/**
	 * Maximum by Y dimension, array is [X,Y] = [columns,rows]
	 */
	int rows;
	/**
	 * Maximum by X dimension, array is [X,Y] = [columns,rows]
	 */
	int columns;
	int size;	
 	
	/**Return column of elements in 2d array
	 * @param pos element number in 1d underlying array
	 * @return X Coordinate of element in 2d array (X, Y)
	 */
	public int X(int pos)
	{		
		return pos - ((int)pos/columns)*columns;
	}
	
	/**Return row of elements in 2d array
	 * @param pos element number in 1d underlying array
	 * @return
	 */
	public int Y(int pos)
	{
		int y = pos/columns;		
		return y;
	}
	
	/**return position element in 1d
	 * @param x
	 * @param y
	 * @return
	 */
	int Pos(int x, int y)
	{
		//TODO Ask Alexey.
		while (y<0) y+=rows;
		while (y >rows-1) y -= rows;
		while (x<0) x+=columns;
		while (x>columns-1) x -= columns;
		
		return y*columns+x;		
	}	
	
	public double GetValueAt(int x, int y)
	{
		return super.get(Pos(x,y)); 
	}
	
	public double getValueAt(int index) {
		return super.get(index);
	}
	
	
	public void setDoubleAt(int index, double value) {
		super.set(index, value);
	}
	
	
	public void SetDoubleAt(int x, int y, double value)
	{
		super.set(Pos(x,y),value);		
	}
		
	/**return new 2d array
	 * @return new instance representation 2d array.
	 */
	public double[][] getDouble2d() {
		double[][] newDouble = new double[columns][rows];
		int pos = 0;

		for (int y = 0; y < rows; y++)
			for (int x = 0; x < columns; x++) {
				newDouble[x][y] = super.get(pos);
				pos++;
			}
		return newDouble;
	}

	/**
	 * @return the number of rows of this matrix. maximal Y coordinate in (x,y)
	 */
    public int getRowsNumber()
    {
        return rows;
    }

    /**
     * @return the number of columns of this matrix. maximal X coordinate in (x,y)
     */
    public int getColsNumber()
    {
        return columns;
    }
    
    public int getSize()
    {return size;}
    
    /**
     * * Creates a new ParallelDoubleArray using the given executor and
     * an array of the given size, but with an initial effective size
     * of zero, enabling incremental insertion via {@link
     * ParallelDoubleArray#asList} operations.
     * @param xSize the array xSize
     * @param ySize the array ySize
     * @param executor
     * @return new ParallelDoubleArray
     */
    public static Parallel2dDoubleArray createEmpty
    (int xSize, int ySize, ForkJoinPool executor)
    {
    	double[] array = new double[xSize*ySize];
    	return new Parallel2dDoubleArray(executor, array, xSize, ySize, xSize*ySize);
    }
    
    public static Parallel2dDoubleArray createEmpty
    (int xSize, int ySize)
    {
    	double[] array = new double[xSize*ySize];
    	return new Parallel2dDoubleArray(ParallelArray.defaultExecutor(), array, xSize, ySize, xSize*ySize);
    }   
    
    public static Parallel2dDoubleArray createFromCopy(double[] array, int xSize, int ySize)
    {
    	//double[] array = new double[xSize*ySize];
    	return new Parallel2dDoubleArray(ParallelArray.defaultExecutor(), array, xSize, ySize, xSize*ySize);
    }   
    
    public void multiply(double value) 
	{
		super.replaceWithMapping(new Multiplier(value));		
	}
	
	/**
	 * @author max
	 *Used for Multiplying value to all ellements at array in parallel way 
	 */
	static class Multiplier implements DoubleOp 
	{
		private final double Multiply;
		Multiplier(final double Multiply) 
		    {
		        this.Multiply = Multiply;
		    }

		public double op(final double a) 
		    {
		        return a *Multiply;
		    }
	}
	
	public void add(double value) 
	{			
		super.replaceWithMapping(new Adder(value));		
	}
	/**
	 * @author max
	 *Used for adding value to all ellements at array in parallel way 
	 */
	static class Adder implements DoubleOp 
	{
		private final double addend;
		    Adder(final double addend) 
		    {
		        this.addend = addend;
		    }

		    public double op(final double a) 
		    {
		        return a + addend;
		    }
	}
}
