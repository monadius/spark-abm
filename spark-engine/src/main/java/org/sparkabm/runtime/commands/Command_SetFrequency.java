package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Sets a delay for simulation
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class Command_SetFrequency extends ModelManagerCommand {
    /* The number of updates per second */
    private int frequency;

    public Command_SetFrequency(int freq) {
        if (freq < 0)
            freq = 0;

        this.frequency = freq;
    }


    @Override
    public void execute(SparkModel model, AbstractSimulationEngine engine)
            throws Exception {
        engine.setFrequency(frequency);
    }

    @Override
    public String toString() {
        return "SetFrequency: " + frequency;
    }

}
