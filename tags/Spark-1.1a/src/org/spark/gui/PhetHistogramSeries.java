/* Copyright 2007, University of Colorado */

package  org.spark.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import jsr166y.forkjoin.ParallelArray;
//import jsr166y.forkjoin.ParallelDoubleArray;

//import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.Series;
//import org.jfree.data.statistics.HistogramBin;

import extra166y.ParallelArray;
import extra166y.ParallelDoubleArray;
//import extra166y.ParallelLongArray;

/**
 * PhetHistogramSeries is a JFreeChart series for histogram observations.
 * <p>
 * A histogram series has an immutable range, which is divided into equal-width bins.
 * When a one-dimensional data point (referred to as an observation) is added to a series,
 * it is placed in the bin that corresponds to its value.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class PhetHistogramSeries extends Series {

    //----------------------------------------------------------------------------
    // Inner classes
    //----------------------------------------------------------------------------
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 6657669894437716963L;

	/**
     * PhetHistogramBin describes a bin in a histogram.
     * A bin has start and end boundaries, and a count of the number of 
     * observations that have fallen in the range between its boundaries.
     */
    private static class PhetHistogramBin {

        private final double startBoundary;
        private final double endBoundary;
        private int numberOfObservations; // mutable

        /* Creates an empty bin */
        public PhetHistogramBin( double startBoundary, double endBoundary ) {
            this.startBoundary = startBoundary;
            this.endBoundary = endBoundary;
            this.numberOfObservations = 0;
        }
        
        public double getStartBoundary() {
            return startBoundary;
        }
        
        public double getEndBoundary() {
            return endBoundary;
        }
        
        public int getNumberOfObservations() {
            return numberOfObservations;
        }
        
        public void increment() {
            numberOfObservations++;
        }
        
        public void clear() {
            numberOfObservations = 0;
        }
        
        public String toString() {
            return "startBoundary=" + startBoundary + " endBoundary=" + endBoundary 
            + " numberOfObservations=" + numberOfObservations;
        }
    }
    
    //----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------
    
    private double minimum; // lower boundary for the series
    private double maximum; // upper boundary for the series
    private double binWidth; // width of all bins
    private int numberOfBins;
    private int numberOfObservations; // improves performance for large number of bins
    private List<PhetHistogramBin> bins; // list of HistogramBin
    private boolean ignoreOutOfRangeObservations; // how to handle observations that are out of bounds
    private ParallelDoubleArray rowData;
    private boolean restrictedSize;
    private boolean restrictedBins;
    private boolean restrictedBinWidth;
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------
    
    /**
     * Constructs an empty histogram series. with restricted minimum, maximum and bin amount
     * 
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     */
    public PhetHistogramSeries( Comparable key, double minimum, double maximum, int numberOfBins ) {
        this( key, minimum, maximum, numberOfBins, null /* observations */ );
    }
    
    /**
     * Constructs a histogram series that contains some observations.
     * with restricted minimum, maximum and bin amount
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     * @param observations
     */
    public PhetHistogramSeries( Comparable key, double minimum, double maximum, 
    		int numberOfBins, double[] observations ) {
        super( key );
        
        if ( key == null ) {
            throw new IllegalArgumentException( "key is null" );
        }
        if ( !( minimum < maximum ) ) {
            throw new IllegalArgumentException( "minimum must be < maximum" );
        }
        if ( !( numberOfBins >= 1 ) ) {
            throw new IllegalArgumentException( "numberOfBins must be >= 1" );
        }
        
        this.restrictedSize=true;
        this.restrictedBins=true;
        this.restrictedBinWidth = false;
        
        this.numberOfBins = numberOfBins;
        this.minimum = minimum;
        this.maximum = maximum;
        this.binWidth = ( maximum - minimum ) / numberOfBins;
        this.ignoreOutOfRangeObservations = true;
        this.bins =new ArrayList<PhetHistogramBin>();
        
        if ((observations != null) && (observations.length > 0)) {
			rowData = ParallelDoubleArray.createFromCopy(observations,
					ParallelArray.defaultExecutor());
		} else
			rowData = ParallelDoubleArray.createEmpty(0, ParallelArray
					.defaultExecutor());
        
        createEmptyBins(numberOfBins);        
        fillBinsAnyRange(observations);
    }

    /**
     * Constructs an empty histogram series. with restricted bin amount
     * 
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     */
    public PhetHistogramSeries( Comparable key, int numberOfBins ) {
        this( key, numberOfBins, null /* observations */ );
    }
    
    /**
     * Constructs a histogram series that contains some observations.
     * with restricted bin amount
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     * @param observations
     */
    public PhetHistogramSeries( Comparable key, 
    		int numberOfBins, double[] observations ) {
        super( key );
        
        if ( key == null ) {
            throw new IllegalArgumentException( "key is null" );
        }        
        if ( !( numberOfBins >= 1 ) ) {
            throw new IllegalArgumentException( "numberOfBins must be >= 1" );
        }
        
        this.restrictedSize=false;
        this.restrictedBins=true;
        this.restrictedBinWidth = false;
        
        this.minimum = 0;
        this.maximum = 0;
        this.numberOfBins = numberOfBins;
        this.binWidth = ( maximum - minimum ) / numberOfBins;
        this.ignoreOutOfRangeObservations = false;
        
        if ((observations != null) && (observations.length > 0)) {
			rowData = ParallelDoubleArray.createFromCopy(observations,
					ParallelArray.defaultExecutor());
			this.minimum = rowData.min();
			this.maximum = rowData.max();
			this.binWidth = (this.maximum - this.minimum)/ numberOfBins;			
		} else
			rowData = ParallelDoubleArray.createEmpty(0, ParallelArray
					.defaultExecutor()); 
      this.bins =new ArrayList<PhetHistogramBin>();
        
        createEmptyBins(numberOfBins);        
        fillBinsInRange(observations);
    }
    
    /**
     * Constructs an empty histogram series. with restricted binWidth
     * 
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     */
    public PhetHistogramSeries( Comparable key, double binWidth ) {
        this( key, binWidth, null /* observations */ );
    }
    
    /**
     * Constructs a histogram series that contains some observations.
     * with restricted binWidth
     * @param key
     * @param minimum
     * @param maximum
     * @param numberOfBins
     * @param observations
     */
    public PhetHistogramSeries( Comparable key, 
    		double binWidth, double[] observations ) {
        super( key );
        
        if ( key == null ) {
            throw new IllegalArgumentException( "key is null" );
        }        
        if ( !( binWidth >= 0 ) ) {
            throw new IllegalArgumentException( "numberOfBins must be >= 1" );
        }
        
        this.restrictedSize=false;
        this.restrictedBins=false;
        this.restrictedBinWidth = true;
        
        this.binWidth = binWidth;
        
        this.minimum = 0;
        this.maximum = 0;
        this.numberOfBins = 1;
        
        if ((observations != null) && (observations.length > 0)) {
			rowData = ParallelDoubleArray.createFromCopy(observations,
					ParallelArray.defaultExecutor());
			this.minimum = rowData.min();
			recalculateNumOfBins(binWidth);
			recalcMaximum(binWidth);
		} else
			rowData = ParallelDoubleArray.createEmpty(0, ParallelArray
					.defaultExecutor());        
        
        this.ignoreOutOfRangeObservations = false;

        this.bins =new ArrayList<PhetHistogramBin>();
        
        createEmptyBins(1);        
        fillBinsInRange(observations);
    }

	private void recalcMaximum(double binWidth) {
		this.maximum = this.minimum + this.numberOfBins * binWidth;
	}

	private void recalculateNumOfBins(double binWidth) {
		this.numberOfBins = (int) Math.ceil((this.maximum - this.minimum)
				/ binWidth);
	}
    
    /**
     * Constructs an empty histogram series. with restricted minimum, maximum and binWidth
     * 
     * @param key
     * @param minimum
     * @param maximum
     * @param binWidth
     */
    public PhetHistogramSeries( Comparable key, double minimum, double maximum, double binWidth ) {
        this( key, minimum, maximum, binWidth, null /* observations */ );
    }
    
   
   /**
	 * Constructs a histogram series that contains some observations. with
	 * restricted minimum, maximum and binWidth
	 * 
	 * @param key
	 * @param minimum
	 * @param maximum
	 * @param binWidth
	 * @param observations
	 */
	public PhetHistogramSeries(Comparable key, double minimum, double maximum,
			double binWidth, double[] observations) {
		super(key);

		if (key == null) {
			throw new IllegalArgumentException("key is null");
		}
		if (!(minimum < maximum)) {
			throw new IllegalArgumentException("minimum must be < maximum");
		}
		if (!(binWidth >= 0)) {
			throw new IllegalArgumentException("numberOfBins must be >= 1");
		}

		this.restrictedSize = true;
		this.restrictedBins = false;
		this.restrictedBinWidth = true;

		this.minimum = minimum;
		this.binWidth = binWidth;
		this.ignoreOutOfRangeObservations = true;
		
		recalculateNumOfBins(binWidth);
		//this.maximum = minimum + binWidth * numberOfBins;
		recalcMaximum(binWidth);
		
		this.bins =new ArrayList<PhetHistogramBin>();
		
		
		 if ((observations != null) && (observations.length > 0)) {
			rowData = ParallelDoubleArray.createFromCopy(observations,
					ParallelArray.defaultExecutor());
		} else
			rowData = ParallelDoubleArray.createEmpty(0, ParallelArray
					.defaultExecutor());
		
		
		createEmptyBins(numberOfBins);
		fillBinsAnyRange(observations);
	}
    
	/**
	 * Filled bins with observations from double[]
	 * Maximum and minimum should be determined before this method
	 * Bins already should exist.
	 * @param observations
	 */
	private void fillBinsInRange(double[] observations) {
		// fill the bins
        if ( observations != null ) {
        	for ( int i = 0; i < observations.length; i++ ) {
        		addObservationInRange( observations[i] );
            }
        }        
	}
	
	private void fillBinsAnyRange(double[] observations) {
		// fill the bins
        if ( observations != null ) {
        	for ( int i = 0; i < observations.length; i++ ) {
        		addObservationAnyRange(observations[i]);
            }
        }       
	}

	/**
	 * Create empty bins. If already exist some bins, 
	 * new bins will be added to end
	 * @param finalNumberOfBins
	 */
	private void createEmptyBins(int finalNumberOfBins) {
		// create a set of empty bins		
		double startBoundary = minimum + bins.size()  * binWidth;

		for (int i = bins.size(); i < finalNumberOfBins; i++) {
			double endBoundary = startBoundary+ binWidth;
			bins.add(new PhetHistogramBin(startBoundary, endBoundary));
			startBoundary = endBoundary;			
		}		
	}
    
	/**
	 * Create new bins and place them before previous.
	 * @param finalNumberOfBins
	 */
	private void createEmptyBinsAtBegin(int finalNumberOfBins) {
		// create a set of empty bins
		
		ArrayList<PhetHistogramBin> newbins =new ArrayList<PhetHistogramBin>();
		double startBoundary = minimum - (finalNumberOfBins-bins.size())*binWidth;

		for (int i = bins.size(); i < finalNumberOfBins; i++) {
			double endBoundary = startBoundary+ binWidth;
			bins.add(new PhetHistogramBin(startBoundary, endBoundary));
			startBoundary = endBoundary;			
		}	
		
		bins.addAll(0, newbins);//TODO Ask Alexei. I want to add new bins before old. Is it correct?
	}
	
    //----------------------------------------------------------------------------
    // Setters and getters
    //----------------------------------------------------------------------------
    
    public double getMinimum() {
        return minimum;
    }
    
    public double getMaximum() {
        return maximum;
    }
    
    public int getNumberOfBins() {
        return bins.size();
    }
    
    public double getBinWidth() {
        return binWidth;
    }
    
    public int getBins(){
    	return numberOfBins;
    }
    
    
    public int getNumberOfObservations() {
        return numberOfObservations;
    }
    
    public void setIgnoreOutOfRangeObservations( boolean ignoreOutOfRangeObservations ) {
        this.ignoreOutOfRangeObservations = ignoreOutOfRangeObservations;
    }
    
    public boolean getIgnoreOutOfRangeObservations() { 
        return ignoreOutOfRangeObservations;
    }
    
    public int getNumberOfObservations( int binIndex ) {
        return getBin( binIndex ).getNumberOfObservations();
    }
    
    public double getStartBoundary( int binIndex ) {
        return getBin( binIndex ).getStartBoundary();
    }
    
    public double getEndBoundary( int binIndex ) {
        return getBin( binIndex ).getEndBoundary();
    }
    
    /**
     * Looks at all the bins and finds the maximum number of observations in a bin.
     * This is useful for adjusting the range of axes.
     * 
     * @return int
     */
    public int getMaxObservations() {
    	int maxBinSize = 0;
        final int numberOfBins = getNumberOfBins();
        for ( int i = 0; i < numberOfBins; i++ ) {
            int binSize = getBin( i ).getNumberOfObservations();
            if ( binSize > maxBinSize ) {
                maxBinSize = binSize;
            }
        }
        return maxBinSize;
    }
    
    //----------------------------------------------------------------------------
    // Bin management
    //----------------------------------------------------------------------------
    
    /*
     * Gets a bin.
     * 
     * @param binIndex
     */
    private PhetHistogramBin getBin( int binIndex ) {
        return (PhetHistogramBin) bins.get( binIndex );
    }
   
    /**
     * Adds an observation to the proper bin.
     * Notifies all SeriesChangedListeners.
     * 
     * @param observation
     * @throws IllegalArgumentException if the observation is out of range and getIgnoreOutOfRangeObservations if false
     */
    public void addObservation( double observation ) {
        addObservation( observation, true );
    }
    
    /**
	 * Adds an observation to the proper bin.
	 * 
	 * @param observation
	 * @param notifyListeners
	 *            whether to notify all SeriesChangeListeners
	 * @throws IllegalArgumentException
	 *             if the observation is out of range and
	 *             getIgnoreOutOfRangeObservations if false
	 */
	public void addObservation(double observation, boolean notifyListeners) {		
		
		rowData.asList().add(observation);

		addObservationAnyRange(observation);
		numberOfObservations++;
		numberOfBins = getNumberOfBins();
		if (notifyListeners) {
			fireSeriesChanged();
		}		
	}

	private void addObservationAnyRange(double observation) {
		if (observation >= minimum && observation <= maximum) {
			addObservationInRange(observation);
		} else {
			if (restrictedSize) {
				if (!ignoreOutOfRangeObservations) {
					throw new IllegalArgumentException("series " + getKey()
							+ " observation is out of range: " + observation);
				}
			} else {
				if (restrictedBins)
					increaseRangeByBins();// recalculate for restricted beens
				if (restrictedBinWidth)
					increaseRangeByWidth(observation);// recalculate for restricted width
			}
		}
	}

	/**
	 * Add new observation if it is between minimum and maximum
	 * @param observation
	 */
	private void addObservationInRange(double observation) {
		double fraction = 0;
		if ((maximum - minimum) > 0)
			fraction = (observation - minimum) / (maximum - minimum);
		if (fraction < 0.0) {
			fraction = 0.0;
		}
		int binIndex = (int) (fraction * numberOfBins);
		// rounding could result in binIndex being out of bounds
		if (binIndex >= numberOfBins) {
			binIndex = numberOfBins - 1;
		}
		getBin(binIndex).increment();
	}
    
    /**
     * If new observed value out of range and Bins amount fixed
     * then recalculate Histograme with constant amount of beans
     */
    private void increaseRangeByBins()
    {
    	maximum = rowData.max();
    	minimum = rowData.min();
    	this.bins =new ArrayList<PhetHistogramBin>();
    	
    	createEmptyBins(numberOfBins);
    	fillBinsInRange(rowData.getArray());    	
    }
    
    /**
     * If new observed value out of range and BinWidth fixed
     * then recalculate Histograme with constant BinWidth
     * @param newObservation
     */
    private void increaseRangeByWidth(double newObservation) {
		double newMax = rowData.max();
		double newMin = rowData.min();

		if (newMax > maximum) {
			maximum = newMax;
			recalculateNumOfBins(binWidth);
			recalcMaximum(binWidth);
			createEmptyBins(numberOfBins);
		}
		if (newMin < minimum) {
			minimum = newMin;
			recalculateNumOfBins(binWidth);
			//numberOfBins = (int) Math.ceil((maximum - newMin) / binWidth);
			recalcMin();
			createEmptyBinsAtBegin(numberOfBins);
		}

		addObservationInRange(newObservation);
	}

	private void recalcMin() {
		minimum = maximum - binWidth * numberOfBins;
	}
        
    public void setMin(double value)
    {
    	restrictedSize=true;
    	minimum = value;
    	recalcBinsAndWidth();
        recalculateHistogram();
    }
    
    public void setMax(double value)
    {
    	restrictedSize=true;
    	maximum = value;
    	if (restrictedBins)
    	{
    		//maximum didn't change
    		//numberOfBins didn't change
    		binWidth = (maximum - minimum)/ numberOfBins;
    	}
    	else
    	{
    		recalculateNumOfBins(binWidth);
			recalcMin();
    	}
        recalculateHistogram();
    }
    
    public void setSizeLimits(double min, double max) {
		restrictedSize = true;
		minimum = min;
		maximum = max;
		recalcBinsAndWidth();
		recalculateHistogram();
		fireSeriesChanged();
	}

    public void setMinMax(double min, double max) {
		minimum = min;
		maximum = max;
		recalcBinsAndWidth();
		recalculateHistogram();
		fireSeriesChanged();
	}
    
	private void recalcBinsAndWidth() {
		if (restrictedBins) {			
			// numberOfBins didn't change
			binWidth = (maximum - minimum) / numberOfBins;
		} else {
			recalculateNumOfBins(binWidth);
			recalcMaximum(binWidth);
		}
	}
    
    public void setBinsLimit(int value) {
		restrictedBins = true;
		restrictedBinWidth = false;
		setBins(value);
	}

	public void setBins(int value) {
		numberOfBins = value;
		if (!restrictedSize) {
			maximum = rowData.max();
			minimum = rowData.min();
		}
		binWidth = (maximum - minimum) / value;
		recalculateHistogram();
		fireSeriesChanged();
	}
    
    public void setBinWidthLimit(double value) {
		restrictedBins = false;
		restrictedBinWidth = true;
		setBinWidth(value);
	}

	public void setBinWidth(double value) {
		binWidth = value;
		if (!restrictedSize) {
			minimum = rowData.min();
			maximum = rowData.max();
		}
		recalculateNumOfBins(binWidth);
		recalcMaximum(binWidth);

		recalculateHistogram();
		fireSeriesChanged();
	}
    
    public void setRestrictionBySize(boolean value) {
		restrictedSize = value;
		if (!value) {
			maximum = rowData.max();
			minimum = rowData.min();
			recalcBinsAndWidth();
		}
		recalculateHistogram();
		fireSeriesChanged();
	}
    
    public void recalculateHistogram()
    {
    	bins =new ArrayList<PhetHistogramBin>();
    	createEmptyBins(numberOfBins);        
    	fillBinsAnyRange(rowData.getArray());
    }
    
    public boolean getRestrictedBySize()
    {return restrictedSize;}
    
    /**
	 * Clears the series. Notifies all SeriesChangedListeners.
	 */
    public void clear() {
    	// clear all bins
        Iterator i = bins.iterator();fireSeriesChanged();
        while ( i.hasNext() ) {
            ( (PhetHistogramBin) i.next() ).clear();
        }
        rowData = ParallelDoubleArray.createEmpty(0, ParallelArray
				.defaultExecutor());
        // clear the series
        numberOfObservations = 0;
        fireSeriesChanged();
    }

	@Override
	public int getItemCount() {
		// TODO Ask Alexei, what is this function mean for normal Series
		return getNumberOfBins();
	}
}
