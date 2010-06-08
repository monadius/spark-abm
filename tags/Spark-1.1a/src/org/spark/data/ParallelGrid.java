

package org.spark.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import org.spark.core.Observer;
import org.spark.gui.render.DataLayerWithColors;
import org.spark.math.Function;
import org.spark.space.BoundedSpace;
import org.spark.space.Space;
import org.spark.space.SpaceAgent;
import org.spark.utils.Parallel2dDoubleArray;
import org.spark.utils.Vector;

import extra166y.Ops;
import extra166y.ParallelArray;
import extra166y.ParallelDoubleArray;

//import jsr166y.forkjoin.Ops;
//import jsr166y.forkjoin.ParallelArray;
//import jsr166y.forkjoin.ParallelDoubleArray;

/*
 * The basic implementation of the data layer interface Values are stored inside
 * cells of a grid of the given dimension
 */
public class ParallelGrid implements AdvancedDataLayer, DataLayerWithColors {
	private static final long serialVersionUID = 5864203993080064052L;

	// Reference to the space object
	protected transient Space space;

	// Dimension of the grid
	/**
	 * x Size of Space
	 */
	protected int xSize;

	/**
	 * y size of Space
	 */
	protected int ySize;// , totalSize;

	// Topology of the grid
	protected boolean wrapX, wrapY;

	protected double xMin, xMax, yMin, yMax;

	// Auxiliary values for fast computations
	// The size of each rectangular grid cell
	protected double xStep, yStep;

	// The inverse of the grid cell size
	protected double invXStep, invYStep;

	// Data stored in the grid
	protected transient Parallel2dDoubleArray data;

	// Auxiliary array for some computations
	// protected Parallel2dDoubleArray dataCopy;

	// Step operation
	protected transient DataLayerStep stepOperation;

	private double colorScale = 10;

	/**
	 * Creates the xSize by ySize grid
	 * 
	 * @param xSize
	 * @param ySize
	 */
	public ParallelGrid(int xSize, int ySize) {
		this(Observer.getDefaultSpace(), xSize, ySize, null);
	}

	public ParallelGrid(BoundedSpace space, int xSize, int ySize) {
		this(space, xSize, ySize, null);
	}

	public ParallelGrid(int xSize, int ySize, DataLayerStep step) {
		this(Observer.getDefaultSpace(), xSize, ySize, step);
	}

	public double getXStep() {
		return xStep;
	}

	public double getYStep() {
		return yStep;
	}

	public ParallelGrid(Space space0, int xSize, int ySize, DataLayerStep step) {
		assert (xSize > 0 && ySize > 0);
		assert (space0 != null);

		if (!(space0 instanceof BoundedSpace))
			throw new Error("Grid can be defined only for BoundedSpace");

		BoundedSpace space = (BoundedSpace) space0;
		this.space = space;
		this.xSize = xSize;
		this.ySize = ySize;
		// this.totalSize = xSize*ySize;

		this.stepOperation = step;

		xStep = space.getXSize() / xSize;
		yStep = space.getYSize() / ySize;
		invXStep = 1.0 / xStep;
		invYStep = 1.0 / yStep;

		this.wrapX = space.getWrapX();
		this.wrapY = space.getWrapY();
		this.xMin = space.getXMin();
		this.xMax = space.getXMax();
		this.yMin = space.getYMin();
		this.yMax = space.getYMax();

		// data = new double[xSize][ySize];
		data = Parallel2dDoubleArray.createEmpty(xSize, ySize);

	}

	/**
	 * Returns the data array. Use this method carefully
	 * 
	 * @return
	 */
	public Parallel2dDoubleArray getData() {
		return data;
	}

	/**
	 * Restricts the x coordinate in accordance with grid topology
	 * 
	 * @param x
	 * @return
	 */
	public int restrictX(int x) {
		if (wrapX) {
			if (x < 0) {
				x += xSize;
				if (x < 0) {
					x += (((-x) / xSize) + 1) * xSize;
				}
			} else if (x >= xSize) {
				x = x - xSize;
				if (x >= xSize) {
					x -= (x / xSize) * xSize;
				}
			}

		} else {
			if (x < 0)
				x = 0;
			else if (x >= xSize)
				x = xSize - 1;
		}

		return x;
	}

	/**
	 * Restricts the y coordinate in accordance with grid topology
	 * 
	 * @param y
	 * @return
	 */
	public int restrictY(int y) {
		if (wrapY) {
			if (y < 0) {
				y += ySize;
				if (y < 0) {
					y += (((-y) / ySize) + 1) * ySize;
				}
			} else if (y >= ySize) {
				y = y - ySize;
				if (y >= ySize) {
					y -= (y / ySize) * ySize;
				}
			}

		} else {
			if (y < 0)
				y = 0;
			else if (y >= ySize)
				y = ySize - 1;
		}

		return y;
	}

	/**
	 * Returns the grid x-coordinate corresponding to the space x-coordinate
	 * 
	 * @param x
	 * @return
	 */
	public int findX(double x) {
		x -= xMin;
		x *= invXStep;

		// TODO: torus
		int xx = (int) Math.floor(x);
		// while (xx<0)xx+=xSize;
		// while (xx>=xSize) xx-= xSize;

		if (xx < 0)
			xx = 0;
		else if (xx >= xSize)
			xx = xSize - 1;

		return xx;
	}

	/**
	 * Returns the grid y-coordinate corresponding to the space y-coordinate
	 * 
	 * @param y
	 * @return
	 */
	public int findY(double y) {
		y -= yMin;
		y *= invXStep;

		// TODO: think about torus
		int yy = (int) Math.floor(y);
		if (yy < 0)
			yy = 0;
		else if (yy >= ySize)
			yy = ySize - 1;

		return yy;
	}

	/**
	 * Returns the coordinates of the grid cell
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Vector getCenter(int x, int y) {
		return new Vector(x * xStep + xMin + xStep / 2, y * yStep + yMin
				+ yStep / 2, 0);
	}

	// **************************
	// Rendering implementation
	// **************************

	public Vector[][] getGeometry() {
		Vector[][] data = new Vector[xSize + 1][ySize + 1];

		for (int i = 0; i <= xSize; i++) {
			for (int j = 0; j <= ySize; j++) {
				data[i][j] = new Vector(xMin + i * xStep, yMin + j * yStep, 0);
			}
		}

		return data;
	}

	// TODO: new color schemes are required
	public Vector[][] getColors() {
		Vector[][] data = new Vector[xSize + 1][ySize + 1];
		double scale = 1.0 / colorScale;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				data[i][j] = new Vector(this.data.GetValueAt(i, j) * scale, 0,
						0);
				// data[i][j] = f(i, j);
			}
			data[i][ySize] = new Vector(this.data.GetValueAt(i, ySize - 1)
					* scale, 0, 0);
			data[i][ySize] = org.spark.space.SpaceAgent.RED;
		}

		for (int j = 0; j < ySize; j++) {
			data[xSize][j] = new Vector(this.data.GetValueAt(xSize - 1, j)
					* scale, 0, 0);
			// data[xSize][j] = RED;
		}
		data[xSize][ySize] = new Vector(this.data.GetValueAt(xSize - 1,
				ySize - 1)
				* scale, 0, 0);
		// data[xSize][ySize] = RED;

		return data;
	}

	private transient Vector[][] colors = null;

	public Vector[][] getColors(double val1, double val2, Vector color1,
			Vector color2) {
		if (colors == null) {
			colors = new Vector[xSize + 1][ySize + 1];

			for (int i = 0; i <= xSize; i++)
				for (int j = 0; j <= ySize; j++)
					colors[i][j] = new Vector();
		}

		if (Math.abs(val1 - val2) < 1e-3)
			val2 = val1 + 1;

		double x1, y1, z1;
		double x2, y2, z2;
		double a1, b1, a2, b2, a3, b3;

		x1 = color1.x;
		y1 = color1.y;
		z1 = color1.z;
		x2 = color2.x;
		y2 = color2.y;
		z2 = color2.z;

		b1 = (x2 - x1) / (val2 - val1);
		a1 = x1 - b1 * val1;

		b2 = (y2 - y1) / (val2 - val1);
		a2 = y1 - b2 * val1;

		b3 = (z2 - z1) / (val2 - val1);
		a3 = z1 - b3 * val1;

		double x, y, z, t;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				t = data.GetValueAt(i, j);
				x = a1 + b1 * t;
				y = a2 + b2 * t;
				z = a3 + b3 * t;

				colors[i][j].set(x, y, z);
			}

			t = data.GetValueAt(i, ySize - 1);
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[i][ySize].set(x, y, z);
		}

		for (int j = 0; j < ySize; j++) {
			t = data.GetValueAt(xSize - 1, j);
			x = a1 + b1 * t;
			y = a2 + b2 * t;
			z = a3 + b3 * t;

			colors[xSize][j].set(x, y, z);
		}

		t = data.GetValueAt(xSize - 1, ySize - 1);
		x = a1 + b1 * t;
		y = a2 + b2 * t;
		z = a3 + b3 * t;

		colors[xSize][ySize].set(x, y, z);

		return colors;
	}

	// ************************************
	// DataLayer interface implementation
	// ************************************

	public double getValue(Vector p) {
		return data.GetValueAt(findX(p.x), findY(p.y));
	}

	public double addValue(Vector p, double value) {
		double temp = data.GetValueAt(findX(p.x), findY(p.y)) + value;
		data.SetDoubleAt(findX(p.x), findY(p.y), temp);
		return temp;
	}

	public void setValue(Vector p, double value) {
		data.SetDoubleAt(findX(p.x), findY(p.y), value);
	}

	public void setValue(double value) {
		data.replaceWithValue(value);
	}

	public Vector getGradient(Vector p) {
		int x = findX(p.x);
		int y = findY(p.y);

		double v = data.GetValueAt(x, y);
		int x1 = x, y1 = y;

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int xx = x + i;
				int yy = y + j;

				if (xx < 0)
					xx = xSize - 1;
				else if (xx >= xSize)
					xx = 0;

				if (yy < 0)
					yy = ySize - 1;
				else if (yy >= ySize)
					yy = 0;

				double tmp = data.GetValueAt(xx, yy);
				if (tmp > v) {
					x1 = xx;
					y1 = yy;
					v = tmp;
				}
			}
		}

		double dv = v - data.GetValueAt(x, y);
		return new Vector((x1 - x) * dv, (y1 - y) * dv, 0);
	}

	public double addValue(SpaceAgent agent, double value) {
		return addValue(agent.getPosition(), value);
	}

	public double getValue(SpaceAgent agent) {
		return getValue(agent.getPosition());
	}

	public void setValue(SpaceAgent agent, double value) {
		setValue(agent.getPosition(), value);
	}

	public double getTotalNumber() {
		return data.sum();
	}

	/**
	 * Return mean value from all datalayer elements
	 * 
	 * @return
	 */
	public double getMeanOfElements() {
		return data.sum() / data.size();
	}

	/**
	 * Return amount of element in datalayer.
	 * 
	 * @return
	 */
	public double getTotalElements() {
		return data.size();
	}

	public void setValue(final Function f) {
		data = new Parallel2dDoubleArray(data, data.withIndexedMapping(
				new Ops.IntAndDoubleToDouble() {
					public double op(final int i, final double element_i) {
						int x = data.X(i);
						int y = data.Y(i);
						Vector v = new Vector();
						v.x = x;
						v.y = y;
						return f.getValue(v);
					}
				}).all());
	}

	// ************************************
	// AdvancedDataLayer interface implementation
	// ************************************

	public double getTotalNumber(final double xMin, final double xMax,
			final double yMin, final double yMax) {
		return data.withIndexedFilter(new Ops.IntAndDoublePredicate() {
			public boolean op(int index, double arg1) {
				int x = data.X(index);
				int y = data.Y(index);
				if (((x >= xMin) && (x <= xMax))
						&& ((y >= yMin) && (y <= yMax)))
					return true;
				return false;
			}
		}).sum();
	}

	public enum CompareCondition
	{
		Larger, 
		Smaller, 
		Equal
	}
	
	
	/**
	 * Return Summ of all elements in datalayer depend from filter.
	 * @param filter
	 * @param Limit
	 * @param Cond
	 * @return
	 */
	public double getTotalNumber(ParallelGrid filter, final double Limit,
			final CompareCondition Cond) {
		
		return data.withMapping(new Ops.BinaryDoubleOp() {
			public double op(double value, double filter) {
				if (((Cond == CompareCondition.Equal) && (filter == Limit))
						|| ((Cond == CompareCondition.Smaller) && (filter < Limit))
						|| ((Cond == CompareCondition.Larger) && (filter > Limit)))
					return value;
				else
					return 0;
			}
		}, filter.data).all().sum();
	}
	
	public void process(final long tick) {
		if (stepOperation != null) {
			data = new Parallel2dDoubleArray(data, data.withIndexedMapping(
					new Ops.IntAndDoubleToDouble() {
						public double op(final int i, final double element_i) {
							int x = data.X(i);
							int y = data.Y(i);
							return OurStep(tick, x, y, element_i);
						}
					}).all());
		}
	}

	private double OurStep(long tick, int x, int y, double element_i) {
		double result = stepOperation.step(tick, x, y, element_i);
		return result;
	}

	public double getValue(int x, int y) {
		return data.GetValueAt(x, y);
	}

	public void setValue(int x, int y, double value) {
		data.SetDoubleAt(x, y, value);
	}

	private transient Vector[][] geometry2;

	public Vector[][] getGeometry2() {
		if (geometry2 != null)
			return geometry2;

		geometry2 = new Vector[xSize][ySize];

		double xStepHalf = xStep / 2;
		double yStepHalf = yStep / 2;

		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				geometry2[i][j] = new Vector(xMin + i * xStep + xStepHalf, yMin
						+ j * yStep + yStepHalf, 0);
			}
		}

		return geometry2;
	}

	public void add(double value) {
		data.add(value);
	}

	public void multiply(double value) {
		data.multiply(value);
	}

	public double getMax() {
		return data.max();
	}

	public double getMin() {
		return data.min();
	}

	public Space getSpace() {
		return space;
	}

	/**
	 * Return the double array which represent line in datalayer
	 * @param lineNumber
	 * @return
	 */
	public double[] getLine(int lineNumber) {
		lineNumber = restrictY(lineNumber);
		double[] line = new double[xSize];
		double[] src = data.getArray();
		System.arraycopy(src, xSize * lineNumber, line, 0, xSize);
		return line;
	}


		
	/**
	 * Method return list of sizes of areas with value smaller or larger than limit
	 * P.S. Method pretty slow. Don't recommended to execute each tick.
	 * 
	 * @param limit Limit which will be used for find sports
	 * @param condition Can be larger or smaller. P.S equal will not be calculated.
	 * @return Sorted List<Double> which contain all sports. Largerst sport
	 *         first. In case when absent any sports return list with one
	 *         element equal 0. method never return null value.
	 *         P.s amount of sports you can get by result.size();
	 */
	@SuppressWarnings("unchecked")
	public List<Double> getSpotSizes(final double limit, final CompareCondition condition) {
		// Create lists of all connected lines in row
		// each element of LineIndexer represent number of line in 2D array (y
		// coordinate)
		Object[] temp = new Object[ySize];
		final ParallelArray LineIndexer = ParallelArray.createFromCopy(
				temp, ParallelDoubleArray.defaultExecutor());

		LineIndexer.replaceWithMappedIndex(new Ops.IntToObject<LinkedList<ConnectedLine>>() {
			public LinkedList<ConnectedLine> op(final int y) {
				final LinkedList<ConnectedLine> Line = new LinkedList<ConnectedLine>();
				ConnectedLine Current = null;
				for (int x = 0; x < xSize; x++) {
					final double value = getValue(x, y);
					if (((condition == CompareCondition.Smaller) && (value < limit))
							|| ((condition == CompareCondition.Larger) && (value > limit))
							|| ((condition == CompareCondition.Equal) && (value == limit))) {
						if (Current == null) {
							Current = new ConnectedLine(y, x);
							Line.add(Current);
						}
						Current.maxX = x;
						Current.size++;
					} else
						Current = null;
				}
				if (!Line.isEmpty()) {
					if ((Line.size() > 1) && (Line.getFirst().minX == 0)
							&& (Line.getLast().maxX >= xSize - 1)) {
						Line.getFirst().DownConnections.add(Line.getLast());
						Line.getLast().UpConnections.add(Line.getFirst());
					}
					return Line;
				}
				return null;
			}
		});
				
		if (LineIndexer.allNonidenticalElements().size()==0) {
			LinkedList<Double> Zero = new LinkedList<Double>();
			Zero.add(0.0);
			return Zero;
		}
		
		// Create connection between lines.
		ParallelDoubleArray StrikesCounter = (ParallelDoubleArray) LineIndexer
				.withIndexedMapping(
						new Ops.IntAndObjectToDouble<LinkedList<ConnectedLine>>() {
							public double op(int y,
									LinkedList<ConnectedLine> Line) {
								if (Line != null) {
									int LineBelowIndex = y - 1;
									if (LineBelowIndex < 0)
										LineBelowIndex = ySize - 1;
									LinkedList<ConnectedLine> LineBelow = (LinkedList<ConnectedLine>) LineIndexer
											.get(LineBelowIndex);
									if (LineBelow != null) {
										for (int j = 0; j < Line.size(); j++) {
											ConnectedLine Current = Line.get(j);
											if (Current.minX <= LineBelow
													.get(LineBelow.size() - 1).maxX) {//overlaping possible
												for (int k = 0; k < LineBelow
														.size(); k++) {
													ConnectedLine Below = LineBelow
															.get(k);
													if (((Current.minX >= Below.minX) && (Current.minX <= Below.maxX))
															|| ((Current.maxX >= Below.minX) && (Current.maxX <= Below.maxX))
															|| ((Below.minX >= Current.minX) && (Below.minX <= Current.maxX))
															|| ((Below.maxX >= Current.minX) && (Below.maxX <= Current.maxX))) {
														Current.DownConnections
																.add(Below);
														Below.UpConnections
																.add(Current);
													}
													if (Current.maxX < Below.minX)
														break;// overlaping impossible
												}
											}
										}
									}
									return Line.size();
								} else
									return 0.0;
							}
						}).all();
		
		ParallelDoubleArray result = ParallelDoubleArray.createEmpty((int) StrikesCounter.sum(), ParallelDoubleArray.defaultExecutor());

//		 Calculate summs of areas for all spots
		for (int index = 0; index< LineIndexer.size(); index++)
		{
			LinkedList<ConnectedLine> Line = (LinkedList<ConnectedLine>) LineIndexer.get(index);
			if (Line != null)
			{
				for (int i = 0; i < Line.size(); i++) {
					ConnectedLine Current = Line.get(i);
					if (!Current.accounted) {
						Double size = Current.CalculateArea();
						result.asList().add(size);
					}
				}
			}
		}

		result.sort(new Ops.DoubleComparator() {
			public int compare(double arg0, double arg1) {
				if (arg0 > arg1) return -1;
				else return 1;
			}
		});

		return result.asList();
	}
	
	/**
	 * Used for calculate area with values more or less than some value
	 */
	private class ConnectedLine {
		public int y;

		public int minX;

		public int maxX;

		public LinkedList<ConnectedLine> DownConnections;

		public LinkedList<ConnectedLine> UpConnections;

		public double size = 0;

		public boolean accounted = false;

		public double CalculateArea() {			
			accounted = true;
			double area = size;
			
			area += checkConection(DownConnections);
			area += checkConection(UpConnections);			
			return area;
		}

		private Double checkConection(LinkedList<ConnectedLine> checkStrikeList) {
			double area =0;
			if (!checkStrikeList.isEmpty()) {
				for (int i = 0; i < checkStrikeList.size(); i++) {
					ConnectedLine next = checkStrikeList.get(i);
					if (!next.accounted)
						area += next.CalculateArea();
				}
			}
			return area;
		}

		public ConnectedLine(int y, int Started) {
			this.y = y;
			this.minX = Started;
			this.maxX = Started;
			DownConnections = new LinkedList<ConnectedLine>();
			UpConnections = new LinkedList<ConnectedLine>();
		}

	}

	public double getTotalNumber(DataLayer filter, double val) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		
		((Observer.MyObjectInputStream) ois).setUserClass(true);
		stepOperation = (DataLayerStep) ois.readObject();
		((Observer.MyObjectInputStream) ois).setUserClass(false);
		
		// Read in the data array
		int r = ois.readInt();
		int c = ois.readInt();
		
		double[] array = (double[]) ois.readObject();
		data = Parallel2dDoubleArray.createFromCopy(array, c, r);
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		
		oos.writeObject(stepOperation);
		
		// Write out the data array
		oos.writeInt(data.getRowsNumber());
		oos.writeInt(data.getColsNumber());
		
		oos.writeObject(data.getArray());
	}

	
	// FIXME: remove
	public void setSpace(Space space) {
		this.space = space;
	}
	
	
	
	/**
	 * Does nothing
	 */
	public void beginStep() {
	}
	

	/**
	 * Does nothing
	 */
	public void endStep() {
	}
}
