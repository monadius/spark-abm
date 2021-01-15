package org.sparkabm.runtime.data;

import org.sparkabm.math.RationalNumber;
import org.sparkabm.math.SimulationTime;

/**
 * Data object for the simulation state
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class DataObject_State extends DataObject {
    /* Current simulation time */
    private SimulationTime time;

    /* Random seed for the current simulation */
    private long seed;

    /* State flags */
    private int flags;

    /* Time passed since the start of the simulation */
    private long elapsedTime;


    // Indicates that a simulation is paused
    public static final int PAUSED_FLAG = 0x1;
    // Indicates that data contains information about initial simulation state
    public static final int INITIAL_STATE_FLAG = 0x2;
    // Indicates that data contains information about final simulation state
    public static final int FINAL_STATE_FLAG = 0x4;
    // Indicates that a simulation was terminated
    public static final int TERMINATED_FLAG = 0x8;


    /**
     * Default constructor
     */
    public DataObject_State(SimulationTime time, long seed, int flags, long startTime) {
        this.time = time;
        this.seed = seed;
        this.flags = flags;
        this.elapsedTime = System.currentTimeMillis() - startTime;
    }

    /**
     * Returns true if this is the first simulation step
     *
     * @return
     */
    public boolean isInitialState() {
        return (flags & INITIAL_STATE_FLAG) != 0;
    }

    /**
     * Returns true if this the last simulation step
     *
     * @return
     */
    public boolean isFinalState() {
        return (flags & FINAL_STATE_FLAG) != 0;
    }

    /**
     * Returns true if the simulation was terminated
     *
     * @return
     */
    public boolean isTerminated() {
        return (flags & TERMINATED_FLAG) != 0;
    }

    /**
     * Returns the simulation tick
     *
     * @return
     */
    public long getTick() {
        return time.getTick();
    }

    /**
     * Returns the simulation time as a rational number
     *
     * @return
     */
    public RationalNumber getTime() {
        return time.getTime();
    }

    /**
     * Returns the simulation time
     *
     * @return
     */
    public SimulationTime getSimulationTime() {
        return time;
    }

    /**
     * Returns true if the simulation is paused
     *
     * @return
     */
    public boolean isPaused() {
        return (flags & PAUSED_FLAG) != 0;
    }

    /**
     * Returns the random generator seed
     *
     * @return
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the time passed since the start of the simulation
     *
     * @return
     */
    public long getElapsedTime() {
        return elapsedTime;
    }


    @Override
    public String toString() {
        return String.valueOf(time.getTick());
    }

}
