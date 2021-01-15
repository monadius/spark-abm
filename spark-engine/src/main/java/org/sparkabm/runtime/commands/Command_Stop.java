package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells the server to stop the current simulation (if any)
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class Command_Stop extends ModelManagerCommand {
    public Command_Stop() {
    }

    @Override
    public void execute(SparkModel model, AbstractSimulationEngine engine)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        return "Stop";
    }
}
