@step(priority = 1)
agent InflamCell : BasicAgent


to create
  color = white
  radius = 0.3
end


to step [tick]
  ifelse count agents-here InfectAgent > 0
  [
   cidal-compound.add-value-here cidal-compound-production
  ]
  [
   ifelse cidal-compound.value-here > chemotaxis-threshold
   [
    uphill cidal-compound
   ]
   [
    wiggle
   ]
  ]
end


@#$#@#$#@
package ToyInfectionModel;

import static org.spark.core.Observer.getSpace;
import java.util.ArrayList;
import org.spark.space.*;
import org.spark.utils.Vector;

@SuppressWarnings("serial")
public class InflamCell extends BasicAgent {

        public InflamCell() {
                setColor(SpaceAgent.WHITE);
        }

        double[] dx = new double[]{-1, 0, 1};
        double[] dy = new double[]{-1, 0, 1};
        double[] vals = new double[9];
        double[] xshift = new double[]{-1, -1, -1, 0, 0, 0, 1, 1, 1};
        double[] yshift = new double[]{-1, 0, 1, -1, 0, 1, -1, 0, 1};

        @Override
        public void step(long tick) {
                ArrayList<InfectAgent> infectAgents =
                        getSpace().getAgents(this, InfectAgent.class);

                if (infectAgents.size() > 0) {
                        ModelData.cidalCompound.addValue(this, ModelData.cidalCompoundProduction);
                }
                else {
                        if (ModelData.cidalCompound.getValue(this) > ModelData.chemotaxisThreshold) {
                                // Complicated code for reproducing the behavior of the original
                                // NetLogo model.
/*                                Vector p = new Vector(node.getPosition());
                                p = ModelData.cidalCompound.getCenter(ModelData.cidalCompound.findX(p.x), ModelData.cidalCompound.findY(p.y));
                                double x = p.x;
                                double y = p.y;

                                int k = 0;
                                for (int i = 0; i < 3; i++)
                                        for (int j = 0; j < 3; j++) {
                                                p.set(x + dx[i], y + dy[j]);
                                                vals[k++] = Model.cidalCompound.getValue(p);
                                        }

                                double max = vals[0];
                                k = 0;
                                for (int i = 1; i < 9; i++) {
                                        if (max < vals[i]) {
                                                max = vals[i];
                                                k = i;
                                        }
                                }

                                p.set(x + xshift[k], y + yshift[k]);
                                node.moveTo(p);
*/
                                Vector gradient = ModelData.cidalCompound.getGradient(getPosition());
                                node.move( gradient.normalize() );
                        }
                        else {
                                wiggle();
                        }
                }

        }
}
