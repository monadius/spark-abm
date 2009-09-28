/* Copyright 2007, University of Colorado */

package org.spark.gui;

import java.io.Serializable;
import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;

/**
 * PhetHistogramDataset is a JFreeChart dataset for creating histograms.
 * It is loosely based on org.jfree.data.statistics.HistogramDataset, 
 * which was unfortunately not written to be extensible or dynamic.
 * <p>
 * This dataset can contain multiple histogram series.
 * When a histogram series is rendered, each bin is drawn as a vertical bar,
 * where the height of the bar corresponds to the number of observations in the bin.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class PhetHistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2290945173098629170L;
	//----------------------------------------------------------------------------
    // Instance data
    //----------------------------------------------------------------------------

    private HistogramType histogramType;
    private List<PhetHistogramSeries> seriesList; // list of PhetHistogramSeries
    
    public void setBinsLimit(int bins)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setBinsLimit(bins);    			
    		}        	
    }
    
    public void setBins(int bins)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setBins(bins);    			
    		}    	
    }
    
    public int getBins (){
    	int bins = 0;    	
    	boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		bins = seriesList.get(0).getBins();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (bins<seriesList.get(i).getBins()) 
    				{
    				bins=seriesList.get(i).getBins();
    				different = true;
    				}
    		}  
    	if (different) setBins(bins);
    	return bins;    	
    }
    
    public double getBinsWidth (){
    	double binsWidth = 0;    	
    	boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		binsWidth = seriesList.get(0).getBinWidth();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (binsWidth<seriesList.get(i).getBinWidth()) 
    				{
    				binsWidth=seriesList.get(i).getBinWidth();
    				different = true;
    				}
    		}  
    	if (different) setBinWidth(binsWidth);
    	return binsWidth;    	
    }
    
    public void setBinWidthLimit(double binSize)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setBinWidthLimit(binSize);    			
    		}
    }
    
    public void setBinWidth(double binSize)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setBinWidth(binSize);    			
    		}
    }
    
    public void setSizeRestriction(boolean value)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setRestrictionBySize(value);    			
    		}    	
    }
    
    public void setSizeRestriction(double min, double max)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setSizeLimits(min, max);    			
    		}    	
    }
    
    public void setSizeMinMax(double min, double max)
    {
    	if((seriesList != null)&&(seriesList.size()>0))
    		for(int i = 0; i< seriesList.size(); i++){
    			seriesList.get(i).setMinMax(min, max);    			
    		}    	
    }
    
    public double getMaximum()
    {
    	double max = 0;    	
    	boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		max = seriesList.get(0).getMaximum();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (max<seriesList.get(i).getMaximum()) 
    				{
    				max=seriesList.get(i).getMaximum();
    				different = true;
    				}
    		}  
    	if (different) setSizeMinMax(getMin(), max);
    	return max;
    }
    
    private double getMax()
    {
    	double max = 0;    	
    	//boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		max = seriesList.get(0).getMaximum();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (max<seriesList.get(i).getMaximum()) 
    				{
    				max=seriesList.get(i).getMaximum();
    				//different = true;
    				}
    		}  
    	//if (different) setSizeRestriction(getMin(), max);
    	return max;
    }
    
    public double getMinimum()
    {
    	double min = 0;    	
    	boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		min = seriesList.get(0).getMinimum();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (min>seriesList.get(i).getMinimum()) 
    			{
    				min=seriesList.get(i).getMinimum();
    				different = true;
    			}
    		}    	
    		if (different) setSizeMinMax(min, getMax());
    	return min;
    }
    
    private double getMin()
    {
    	double min = 0;    	
    	//boolean different = false;
    	if((seriesList != null)&&(seriesList.size()>0))
    		min = seriesList.get(0).getMinimum();
    		for(int i = 0; i< seriesList.size(); i++){
    			if (min>seriesList.get(i).getMinimum()) 
    			{
    				min=seriesList.get(i).getMinimum();
    				//different = true;
    			}
    		} 
    	return min;
    }
    //----------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------

    /**
     * Creates an empty dataset of type HistogramType.FREQUENCY.
     */
    public PhetHistogramDataset() {
        this( HistogramType.FREQUENCY );
    }

    /**
     * Create an empty dataset with a specified HistogramType.
     * 
     * @param histogramType the histogram type (null not permitted)
     */
    public PhetHistogramDataset( HistogramType histogramType ) {
        if ( histogramType == null ) {
            throw new IllegalArgumentException( "histogramType is null" );
        }
        this.histogramType = histogramType;
        this.seriesList = new ArrayList<PhetHistogramSeries>();
    }

    //----------------------------------------------------------------------------
    // Setters and getters
    //----------------------------------------------------------------------------

    /**
     * Gets the histogram type. 
     * 
     * @return the type (never null)
     */
    public HistogramType getHistogramType() {
        return histogramType;
    }

    /**
     * Sets the histogram type.
     * Sends a DatasetChangeEvent to all registered listeners.
     * 
     * @param histogramType the histogram type (null not permitted)
     */
    public void setHistogramType( HistogramType histogramType ) {
        if ( histogramType == null ) {
            throw new IllegalArgumentException( "histogramType is null" );
        }
        if ( histogramType != this.histogramType ) {
            this.histogramType = histogramType;
            notifyListeners( new DatasetChangeEvent( this, this ) );
        }
    }

    /**
     * Gets a series by index.
     * 
     * @param seriesIndex
     * @return the series
     */
    public PhetHistogramSeries getSeries( int seriesIndex ) {
        return (PhetHistogramSeries) seriesList.get( seriesIndex );
    }

    /**
     * Gets a series by key.
     * 
     * @param seriesKey
     * @return PhetHistogramSeries, null if not found
     */
    public PhetHistogramSeries getSeries( Comparable seriesKey ) {
        PhetHistogramSeries series = null;
        for ( int i = 0; i < seriesList.size(); i++ ) {
            series = (PhetHistogramSeries) seriesList.get( i );
            if ( series.getKey().compareTo( seriesKey ) == 0 ) {
                break;
            }
        }
        return series;
    }
    
    /**
     * Gets the index of a series.
     * 
     * @param series
     * @return index, -1 if the dataset does not contain this series
     */
    public int getSeriesIndex( PhetHistogramSeries series ) {
        return seriesList.indexOf( series );
    }
    
    /**
     * Gets the index of a series using its key.
     * 
     * @param seriesKey
     * @return series index, -1 if not found
     */
    public int getSeriesIndex( Comparable seriesKey ) {
        int seriesIndex = -1;
        PhetHistogramSeries series = getSeries( seriesKey );
        if ( series != null ) {
            seriesIndex = getSeriesIndex( series );
        }
        return seriesIndex;
    }
    
    //----------------------------------------------------------------------------
    // Series
    //----------------------------------------------------------------------------

    /**
     * Adds a series.
     * Makes this dataset a listener for changes to the series.
     * Notifies all DatasetChangedListeners.
     * 
     * @param series
     * @return the index of the series
     */
    public int addSeries( PhetHistogramSeries series ) {
        seriesList.add( series );
        series.addChangeListener( this );
        fireDatasetChanged();
        return seriesList.indexOf( series );
    }
    
    /**
     * Removes a series.
     * Removes this dataset as a listener for changes to the series.
     * Notifies all DatasetChangedListeners.
     * <p>
     * Calling this method changes the indicies of series that the dataset is managing.
     * Clients should refresh any indicies they are using by requesting new indicies.
     * 
     * @param seriesIndex
     */
    public void removeSeries( PhetHistogramSeries series ) {
        seriesList.remove( series );
        series.removeChangeListener( this );
        fireDatasetChanged();
    }
    
    /**
     * Removes a series by index.
     * Removes this dataset as a listener for changes to the series.
     * Notifies all DatasetChangedListeners.
     * <p>
     * Calling this method changes the indicies of series that the dataset is managing.
     * Clients should refresh any indicies they are using by requesting new indicies.
     * 
     * @param seriesIndex
     */
    public void removeSeries( int seriesIndex ) {
        PhetHistogramSeries series = (PhetHistogramSeries) seriesList.get( seriesIndex );
        removeSeries( series );
    }
    
    /**
     * Removes all series.
     */
    public void removeAllSeries() {
        for ( int i = 0; i < seriesList.size(); i++ ) {
            removeSeries( i );
        }
    }

    //----------------------------------------------------------------------------
    // Implementation of AbstractIntervalXYDataset, et. al.
    //----------------------------------------------------------------------------
    
    /**
     * Gets the key for a series.
     * 
     * @param seriesIndex the series index (zero based)
     * @return the series key
     */
    public Comparable getSeriesKey( int seriesIndex ) {
        PhetHistogramSeries series = getSeries( seriesIndex );
        return series.getKey();
    }

    /**
     * Gets the number of series in the dataset.
     * 
     * @return the series count
     */
    public int getSeriesCount() {
        return seriesList.size();
    }

    /**
     * Gets the number of data items (bins) for a series.
     * 
     * @param seriesIndex the series index (zero based)
     * @return the item (bin) count
     */
    public int getItemCount( int seriesIndex ) {
        PhetHistogramSeries series = getSeries( seriesIndex );
        return series.getNumberOfBins();
    }
    
    /**
     * Returns the X value for a bin.  This value won't be used for plotting 
     * histograms, since the renderer will ignore it.  But other renderers can 
     * use it. For example, you could use the dataset to create a line chart.
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the start value
     */
    public Number getX( int seriesIndex, int binIndex ) {
        PhetHistogramSeries series = getSeries( seriesIndex );
        double startBoundary = series.getStartBoundary( binIndex );
        double endBoundary = series.getEndBoundary( binIndex );
        final double x = ( startBoundary + endBoundary ) / 2.;
        return new Double( x );
    }

    /**
     * Returns the y-value for a bin 
     * (calculated to take into account the histogram type).
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the y value
     */
    public Number getY( int seriesIndex, int binIndex ) {

        PhetHistogramSeries series = getSeries( seriesIndex );
        final double totalObservations = series.getNumberOfObservations();
        final double binObservations = series.getNumberOfObservations( binIndex );
        final double binWidth = series.getBinWidth();

        double y = 0;
        if ( histogramType == HistogramType.FREQUENCY ) {
            y = binObservations;
        }
        else if ( histogramType == HistogramType.RELATIVE_FREQUENCY ) {
            y = binObservations / totalObservations;
        }
        else if ( histogramType == HistogramType.SCALE_AREA_TO_1 ) {
            y = binObservations / ( binWidth * totalObservations );
        }
        else {
            throw new IllegalStateException( "unsupported HistogramType: " + histogramType );
        }
        return new Double( y );
    }

    /**
     * Returns the start value for a bin.
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the start value
     */
    public Number getStartX( int seriesIndex, int binIndex ) {
        PhetHistogramSeries series = getSeries( seriesIndex );
        double startBoundary = series.getStartBoundary( binIndex );
        return new Double( startBoundary );
    }

    /**
     * Returns the end value for a bin.
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the end value
     */
    public Number getEndX( int seriesIndex, int binIndex ) {
        PhetHistogramSeries series = getSeries( seriesIndex );
        double endBoundary = series.getEndBoundary( binIndex );
        return new Double( endBoundary );
    }

    /**
     * Returns the start y-value for a bin (which is the same as the y-value, 
     * this method exists only to support the general form of the 
     * {@link IntervalXYDataset} interface).
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the y value
     */
    public Number getStartY( int seriesIndex, int binIndex ) {
        return getY( seriesIndex, binIndex );
    }

    /**
     * Returns the end y-value for a bin (which is the same as the y-value, 
     * this method exists only to support the general form of the 
     * {@link IntervalXYDataset} interface).
     * 
     * @param seriesIndex the series index (zero based)
     * @param binIndex the bin index (zero based)
     * @return the y value
     */
    public Number getEndY( int seriesIndex, int binIndex ) {
        return getY( seriesIndex, binIndex );
    }

    //----------------------------------------------------------------------------
    // Comparable implementation
    //----------------------------------------------------------------------------

    /**
     * Tests this dataset for equality with an arbitrary object.
     * 
     * @param obj the object to test against (null permitted)
     * @return true or false
     */
    public boolean equals( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !( obj instanceof PhetHistogramDataset ) ) {
            return false;
        }
        PhetHistogramDataset that = (PhetHistogramDataset) obj;
        if ( !ObjectUtilities.equal( this.histogramType, that.histogramType ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal( this.seriesList, that.seriesList ) ) {
            return false;
        }
        return true;
    }

    //----------------------------------------------------------------------------
    // PublicCloneable implementation
    //----------------------------------------------------------------------------

    /**
     * Returns a clone of the dataset.
     * 
     * @return a clone of the dataset.
     * @throws CloneNotSupportedException if the object cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
