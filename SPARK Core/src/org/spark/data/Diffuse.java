package org.spark.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//import jsr166y.forkjoin.Ops;

import org.spark.space.BoundedSpace;
import org.spark.utils.Parallel2dDoubleArray;

import extra166y.Ops;

public class Diffuse extends ParallelGrid {

	private static final long serialVersionUID = 4703600340624630323L;

	Diffuse(BoundedSpace space, int xSize, int ySize) {
		super(space, xSize, ySize);
	}
	
	private double Evaporation;
	
	/**
	 * return Evaporation rate
	 * @return
	 */
	public double getEvaporation(){return Evaporation;}
	
	/**
	 * Set new Evaporation rate
	 * @param value
	 */
	public void setEvaporation(Double value) {
		Evaporation = value;
		if (UseGreenDiffusion)
			createGreenMatrix();		
	}
	
	private boolean UseGreenDiffusion = false;

	/**
	 * Datalayer wit methods for calculate diffusion.
	 * Simplest and fastest way of calculate diffusion. Diffusion take in account only
	 * neighbor cells. 
	 * @param xSize x size of space
	 * @param ySize y size of space
	 * @param step method which will be executed each step
	 * @param DiffKoef Diffusion Koefficient. Whic part of content can diffuse. Should be between 0(nothing can diffuse) and 1(all content can diffuse). 
	 * @param Evapor Evaporation rate. Which part of content will be evaporated. should be between 0 (nothing evaporated) and 1 (everything evaporated) 
	 */
	public Diffuse(int xSize, int ySize, DataLayerStep step, double DiffKoef,
			double Evapor) {
		super(xSize, ySize, step);
		setDiffusionKoefficient(DiffKoef);
		setEvaporation(Evapor);
	}
	
	protected double DiffusionKoefficient;
	/**
	 * Return Diffusion Koefficient which currently using
	 * @return
	 */
	public double getDiffusionKoefficient(){return DiffusionKoefficient;}
	
	/**
	 * Changing Diffusion Koefficient. This method can be used for modify Diffusion Coefficient.
	 * We assume that DiffusionKoefficient and All other parameters was already setup by constructor.
	 * @param value
	 */
	public void setDiffusionKoefficient(Double value){ 
		DiffusionKoefficient = value;
		if (UseGreenDiffusion)
			createGreenMatrix();
	}

	protected double GreenSteps;
	/**
	 * Return amount of Green steps. Green Steps mean 
	 * equvalent additional steps which system should do 
	 * for calculate similar diffusion effect
	 * @return
	 */
	public double getGreenSteps(){return GreenSteps;}
	/**
	 * Setup amount of Green steps. Green Steps mean 
	 * equvalent additional steps which system should do 
	 * for calculate similar diffusion effect
	 * @param value
	 */
	public void setGreenSteps(double value){
		GreenSteps = value;
		if (UseGreenDiffusion)
			createGreenMatrix();
	}
	
	protected transient Parallel2dDoubleArray GreenMatrix;

	protected int ConvolutionRadius;
	
	/**
* Datalayer wit methods for calculate diffusion. For calculation of
	 * diffusion used convolution. Convolution matrix create by using Bell shape
	 * distribution. Slower than simplest diffusion but
	 * create better distribution and smoother gradient. Speed of execution
	 * reverse proportional square of GreenRadius.

	 * @param xSize
	 *            x size of space
	 * @param ySize
	 *            y size of space
	 * @param TimeTick could be in s, ms, mks.....
	 * @param cell_area could be in m^2, mm^2 ....
	 * @param step
	 *            method which will be executed each step
	 * @param DiffKoef
	 *            Diffusion Koefficient. could be in m^2/s, mkm^2/ms.... it should correlate to cell_area and TimeTick
	 * @param Evapor
	 *            Evaporation rate. Which part of content will be evaporated.
	 *            should be between 0 (nothing evaporated) and 1 (everything
	 *            evaporated)
	 * @param maxDifferences Used for calculate Green Radius
	 * @param minGreenRadius Low limit for GreenRadius
	 */
	public Diffuse(int xSize, int ySize, double TimeTick, double cell_area,
			DataLayerStep step, double DiffKoef, double Evapor, double maxDifferences, int minGreenSteps) {
		this(xSize, ySize, step, DiffKoef, Evapor);
		ConvolutionRadius = FindConvolutionRadius(TimeTick, cell_area, maxDifferences, minGreenSteps, -1); 
		//setGreenSteps(20);//not really needed; just debugging
		GreenSteps = 1;// not used for this diffusion
		UseGreenDiffusion = true;
		//System.out.println(ConvolutionRadius);	
		createGreenMatrix(TimeTick, cell_area);
	}
	
	/**
	 * Datalayer wit methods for calculate diffusion. For calculation of
	 * diffusion used convolution. Convolution matrix create by using Bell shape
	 * distribution. Slower than simplest diffusion but
	 * create better distribution and smoother gradient. Speed of execution
	 * reverse proportional square of GreenRadius.

	 * @param xSize
	 *            x size of space
	 * @param ySize
	 *            y size of space
	 * @param TimeTick could be in s, ms, mks.....
	 * @param cell_area could be in m^2, mm^2 ....
	 * @param step
	 *            method which will be executed each step
	 * @param DiffKoef
	 *            Diffusion Koefficient. could be in m^2/s, mkm^2/ms.... it should correlate to cell_area and TimeTick
	 * @param Evapor
	 *            Evaporation rate. Which part of content will be evaporated.
	 *            should be between 0 (nothing evaporated) and 1 (everything
	 *            evaporated)
	 * @param maxDifferences Used for calculate Green Radius
	 * @param minGreenRadius Low limit for GreenRadius
	 * @param maxGreenRadius Up Limit for GreenRadius
	 */
	public Diffuse(int xSize, int ySize, double TimeTick, double cell_area,
			DataLayerStep step, double DiffKoef, double Evapor, double maxDifferences, int minGreenRadius, int maxGreenRadius) {
		this(xSize, ySize, step, DiffKoef, Evapor);
		ConvolutionRadius = FindConvolutionRadius(TimeTick, cell_area, maxDifferences, minGreenRadius, maxGreenRadius); 
		//setGreenSteps(20);//not really needed; just debugging
		GreenSteps = 1;// not used for this diffusion
		UseGreenDiffusion = true;
		//System.out.println(ConvolutionRadius);	
		createGreenMatrix(TimeTick, cell_area);
	}
	
	/**
	 * Datalayer wit methods for calculate diffusion. For calculation of
	 * diffusion used convolution. Convolution matrix create by using Bell shape
	 * distribution. Slower than simplest diffusion but
	 * create better distribution and smoother gradient. Speed of execution
	 * reverse proportional square of GreenRadius.
	 * @param xSize
	 *            x size of space
	 * @param ySize
	 *            y size of space
	 * @param GreenRadius
	 *            maximum distance where can be observed changes after one
	 *            diffusion step.
	 * @param TimeTick could be in s, ms, mks.....
	 * @param cell_area could be in m^2, mm^2 ....
	 * @param step
	 *            method which will be executed each step
	 * @param DiffKoef
	 *            Diffusion Koefficient. could be in m^2/s, mkm^2/ms.... it should correlate to cell_area and TimeTick
	 * @param Evapor
	 *            Evaporation rate. Which part of content will be evaporated.
	 *            should be between 0 (nothing evaporated) and 1 (everything
	 *            evaporated)
	 */
	public Diffuse(int xSize, int ySize, int GreenRadius, double TimeTick, double cell_area,
			DataLayerStep step, double DiffKoef, double Evapor) {
		this(xSize, ySize, step, DiffKoef, Evapor);
		ConvolutionRadius = GreenRadius; 
		//setGreenSteps(20);//not really needed; just debugging
		GreenSteps = 1;// not used for this diffusion
		UseGreenDiffusion = true;
		createGreenMatrix(TimeTick, cell_area);
	}	
	
	/**
	 * we compare the values of the green's function at 0, 0 and at r, r by
	 * taking a ratio. Green's function at 0, 0 is 1. When the ratio is < 1E-4,
	 * the radius is OK. This sort of computation might significantly slow the
	 * model.
	 * 
	 * @param TimeTick
	 * @param cell_area
	 * @param maxDifferences
	 * @param minGreenRadius
	 * @return
	 */
	protected int FindConvolutionRadius(double TimeTick, double cell_area,
			double maxDifferences, int minGreenRadius, int maxGreenRadius) {
		double r = 0;
		double compare;
		do {
			r++;
			compare = computeGreen(r, r, TimeTick, cell_area);
		} while (compare > maxDifferences);
		if (r > minGreenRadius)
			if ((r < maxGreenRadius) || (maxGreenRadius <0))
				return (int) r;
			else
				return maxGreenRadius;
		else
			return minGreenRadius;
	}
	
	/**
	 * @param i
	 * @param j
	 * @param TimeTick in s, ms or ...
	 * @param cell_area in mk^2, or m^2 or.....
	 * @return
	 */
	private double computeGreen(double i, double j, double TimeTick, double cell_area){
		return Math.exp(-(Math.pow(i, 2) + Math.pow(j, 2))/(4*TimeTick*DiffusionKoefficient*cell_area));
	}
	
	/**
	 * From Gousian distribution.
	 * @param TimeTick
	 * @param cell_area
	 */
	private void createGreenMatrix(final double TimeTick, final double cell_area) {
		final Parallel2dDoubleArray TempGreenMatrix = new Parallel2dDoubleArray(2*ConvolutionRadius + 1,
				2*ConvolutionRadius + 1);
		
		TempGreenMatrix.replaceWithMappedIndex(new Ops.IntToDouble(){
			public double op(int index) {
				int x = TempGreenMatrix.X(index);
				int y = TempGreenMatrix.Y(index);
				return computeGreen(((double)x - ConvolutionRadius), ((double)y - ConvolutionRadius), TimeTick, cell_area);
			}			
		});		
		//*/
		double NormalizationKoeff = TempGreenMatrix.sum();
		TempGreenMatrix.multiply((1-Evaporation)/NormalizationKoeff);
		GreenMatrix = TempGreenMatrix;
	}
	
	/**
	 * Datalayer wit methods for calculate diffusion. For calculation of
	 * diffusion used convolution. Convolution matrix create by calculation
	 * simplest diffusion GreenSteps Times. Slower than simplest diffusion but
	 * create better distribution and smoother gradient. Speed of execution
	 * reverse proportional square of GreenRadius and don't depend from amount
	 * of additional steps.
	 * 
	 * @param xSize
	 *            x size of space
	 * @param ySize
	 *            y size of space
	 * @param GreenRadius
	 *            maximum distance where can be observed changes after one
	 *            diffusion step. Recommended value should be not larger than
	 *            GreenSteps amount.
	 * @param GreenSteps
	 *            is equvalent how much additional steps of simplest diffusion
	 *            will be made.
	 * @param step
	 *            method which will be executed each step
	 * @param DiffKoef
	 *            Diffusion Koefficient. Whic part of content can diffuse.
	 *            Should be between 0(nothing can diffuse) and 1*GreenSteps(all
	 *            content can diffuse).
	 * @param Evapor
	 *            Evaporation rate. Which part of content will be evaporated.
	 *            should be between 0 (nothing evaporated) and 1 (everything
	 *            evaporated)
	 */
	public Diffuse(int xSize, int ySize, int GreenRadius, double GreenSteps,
			DataLayerStep step, double DiffKoef, double Evapor) {
		this(xSize, ySize, step, DiffKoef, Evapor);
		ConvolutionRadius = GreenRadius;
		setGreenSteps(GreenSteps);
		UseGreenDiffusion = true;
		createGreenMatrix();
	}
	
	
	private void createGreenMatrix() {
		double StartConst = 1;
		///Calculate diffusion for small steps of time
		final Diffuse GreenTemp = new Diffuse(this.xSize, this.ySize, null, 
				DiffusionKoefficient/ GreenSteps, 0);//Evapor / GreenSteps);
		
		GreenTemp.setValue(0, 0, StartConst);
		for (int i = 0; i < GreenSteps; i++)
			GreenTemp.data
					.replaceWithMappedIndex((new Ops.IntAndDoubleToDouble() {
						public double op(final int i, final double element_i) {
							int x = GreenTemp.data.X(i);
							int y = GreenTemp.data.Y(i);
							return GreenTemp.DiffuseOneElement(x, y, element_i);
						}
					}));

		//Copy data 		
		Parallel2dDoubleArray TempGreenMatrix = new Parallel2dDoubleArray(2*ConvolutionRadius + 1,
				2*ConvolutionRadius + 1);
		for (int i = -ConvolutionRadius; i <= ConvolutionRadius; i++) {
			for (int j = -ConvolutionRadius; j <= ConvolutionRadius; j++) {
				double v = GreenTemp.getValue(i, j) / StartConst;
				TempGreenMatrix.SetDoubleAt(i + ConvolutionRadius, j + ConvolutionRadius, v);
			}
		}

		//Normalize GreenMatrix
		double NormalizationKoeff = TempGreenMatrix.sum();
		TempGreenMatrix.multiply((1-Evaporation)/NormalizationKoeff);
		GreenMatrix = TempGreenMatrix;		
	}
	
	Diffuse(int xSize, int ySize) {
		super(xSize, ySize);
	}

	/**
	 * Calculate diffusion. If In constractor was used parameters for
	 * GreenMatrix will use GreenMatrix Else will use diffusion without
	 * GreenMatrix.
	 * 
	 * @param x
	 * @param y
	 * @param element_i
	 * @return
	 */
	public double DiffuseOneElement(int x, int y, double element_i) {
		if (UseGreenDiffusion)
			return GreenDefuse(x, y);
		else
			return DiffuseOneElement(x, y, element_i, DiffusionKoefficient,
					Evaporation);
	}

	/**
	 * Traditional Diffusion without using green matrix.
	 * @param x
	 * @param y
	 * @param element_i
	 * @param DiffKoef
	 * @param DiffRate
	 * @param Evapor
	 * @return
	 */
	double DiffuseOneElement(int x, int y, double element_i, double DiffKoef,
			double Evapor) {
		double result = element_i * (1 - DiffKoef);
		double Koef = DiffKoef/8;

		for (int i = -1; i <= 1; i++) {
			int xx = x + i;

			if (xx < 0)
				xx = xSize - 1;
			else if (xx >= xSize)
				xx = 0;

			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0)
					continue;

				int yy = y + j;

				if (yy < 0)
					yy = ySize - 1;
				else if (yy >= ySize)
					yy = 0;

				result += data.GetValueAt(xx, yy) * Koef;				
			}
		}
		return result * (1 - Evapor);
	}

	/**
	 * Calculate diffusion with using Green Matrix.
	 * @param x
	 * @param y
	 * @return
	 */
	double GreenDefuse(int x, int y) {
		double sum = 0;
		for (int i = -ConvolutionRadius; i <= ConvolutionRadius; i++) {
			for (int j = -ConvolutionRadius; j <= ConvolutionRadius; j++) {
				sum += GreenMatrix.GetValueAt(i + ConvolutionRadius, j
						+ ConvolutionRadius)
						* data.GetValueAt(x + i, y + j);
			}
		}
		return sum;
	}
	
	
	
	/**
	 * Custom deserialization is needed.
	 */
	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		
		// Read in the data array
		int r = ois.readInt();
		
		if (r > 0) {
			int c = ois.readInt();
		
			double[] array = (double[]) ois.readObject();
			GreenMatrix = Parallel2dDoubleArray.createFromCopy(array, c, r);
		}
	}

	/**
	 * Custom serialization is needed.
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		
		if (GreenMatrix != null) {
			// Write out the data array
			oos.writeInt(GreenMatrix.getRowsNumber());
			oos.writeInt(GreenMatrix.getColsNumber());
			
			oos.writeObject(GreenMatrix.getArray());
		}
		else {
			oos.writeInt(0);
		}
	}
}
