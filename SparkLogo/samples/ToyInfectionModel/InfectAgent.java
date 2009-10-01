@step(priority = 2)
agent InfectAgent : BasicAgent


to create
  color = grey
  radius = 0.8
end



to step [tick]
  if cidal-compound.value > 10
  [
   die
   exit
  ]


  var life = tissue-life.value-here - 1.0
  if life < 0 [ life = 0 ]

  tissue-life.set-value-here life

  if yes-toxin
  [
   toxin.add-value-here 1.0
  ]

  if tick % 100 == 0
  [
   if count agents-here InfectAgent < 3
   [
    ask hatch-one InfectAgent
    [
     jump 1.0
    ]
   ]

   jump 1.0
  ]
end


@#$#@#$#@
package ToyInfectionModel;

import static org.spark.core.Observer.getSpace;
import java.util.ArrayList;
import org.spark.space.*;

@SuppressWarnings("serial")
public class InfectAgent extends BasicAgent {
        public InfectAgent() {
                super(0.8, SpaceAgent.CIRCLE);
                setColor(SpaceAgent.GREY);
        }

        @Override
        public void step(long tick) {
                if (ModelData.cidalCompound.getValue(this) > 10) {
                        die();
                        return;
                }

                double life = ModelData.tissueLife.getValue(this) - 1.0;
                if (life < 0) life = 0;
                ModelData.tissueLife.setValue(this, life);

                if (ModelData.yesToxin) ModelData.toxin.addValue(this, 1.0);

                if (tick % 100 == 0) {
                        ArrayList<InfectAgent> agents =
                                getSpace().getAgents(this, InfectAgent.class);

                        if (agents.size() < 3) {
                                InfectAgent infectAgent = new InfectAgent();
                                infectAgent.jump(getPosition());
                                infectAgent.jump(1.0);
                        }

                        jump(1.0);
                }
        }


}
