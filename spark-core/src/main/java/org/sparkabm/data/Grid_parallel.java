package org.sparkabm.data;

import org.sparkabm.math.Function;
import org.sparkabm.space.Space;
import org.sparkabm.math.Vector;

/**
 * Implementation of the grid for the parallel execution mode
 *
 * @author Monad
 */
public class Grid_parallel extends Grid_concurrent {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 7481145279817269519L;

    /**
     * Basic constructor
     *
     * @param space
     * @param xSize
     * @param ySize
     */
    protected Grid_parallel(Space space, int xSize, int ySize) {
        super(space, xSize, ySize);
    }


    //************************************
    // DataLayer interface implementation
    //************************************


    public double getValue(Vector p) {
        return readData[findX(p.x)][findY(p.y)];
    }


    public double addValue(Vector p, double value) {
        int x = findX(p.x);
        int y = findY(p.y);

        synchronized (writeData) {
            writeData[x][y] += value;
        }

        return readData[x][y];
    }


    public void setValue(Vector p, double value) {
        int x = findX(p.x);
        int y = findY(p.y);

        // FIXME: this function should not work in the parallel mode
        // or collision resolution should be implemented

        synchronized (writeData) {
            writeData[x][y] = value;
        }
    }


    public void setValue(double value) {
        synchronized (writeData) {
            for (int i = 0; i < xSize; i++)
                for (int j = 0; j < ySize; j++)
                    writeData[i][j] = value;
        }
    }


    public void setValue(Function f) {
        Vector v = new Vector();
        v.x = xMin + xStep / 2;

        synchronized (writeData) {
            for (int i = 0; i < xSize; i++, v.x += xStep) {
                v.y = yMin + yStep / 2;

                for (int j = 0; j < ySize; j++, v.y += yStep) {
                    writeData[i][j] = f.getValue(v);
                }
            }
        }
    }

    //************************************
    // AdvancedDataLayer interface implementation
    //************************************

    public void multiply(double value) {
        synchronized (writeData) {
            for (int i = 0; i < xSize; i++) {
                double[] data = writeData[i];
                for (int j = 0; j < ySize; j++)
                    data[j] *= value;
            }
        }
    }


    public void add(double value) {
        synchronized (writeData) {
            for (int i = 0; i < xSize; i++) {
                double[] data = writeData[i];
                for (int j = 0; j < ySize; j++)
                    data[j] += value;
            }
        }
    }


    public void setValue(int x, int y, double value) {
        synchronized (writeData) {
            writeData[x][y] = value;
        }
    }


    /**
     * Does nothing
     */
    public void beginStep() {
        // TODO: probably, we don't need to synchronize data here,
        // because this method is always called from the same thread
        // Synchronize all cached data
        synchronized (readData) {
            synchronized (writeData) {
                super.beginStep();
            }
        }
    }


    /**
     * Does nothing
     */
    public void endStep() {
        // Synchronize all cached data
        synchronized (readData) {
            synchronized (writeData) {
                super.endStep();
            }
        }
    }

}
