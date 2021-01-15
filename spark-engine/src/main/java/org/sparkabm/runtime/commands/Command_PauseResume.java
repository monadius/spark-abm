package org.sparkabm.runtime.commands;

import org.sparkabm.core.SparkModel;
import org.sparkabm.runtime.internal.engine.AbstractSimulationEngine;

/**
 * Tells the server to pause a running simulation or to resume
 * a paused simulation
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class Command_PauseResume extends ModelManagerCommand {
    public Command_PauseResume() {
    }

    @Override
    public void execute(SparkModel model, AbstractSimulationEngine engine)
            throws Exception {
        // TODO Auto-generated method stub

    }

}
