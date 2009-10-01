agent BasicAgent : SpaceAgent

var heading


to create
   super 0.5 circle
   heading = random 360
end


to jump [n]
  move vector-in-direction n heading
end


to wiggle
  heading += random-in-interval -45 45
  jump 1
end


@#$#@#$#@
package ToyInfectionModel;

import static org.spark.utils.RandomHelper.nextDoubleFromTo;

import org.spark.space.SpaceAgent;
import org.spark.utils.Vector;

@SuppressWarnings("serial")
public class BasicAgent extends SpaceAgent {
        protected double        heading;


        public void setHeading(double heading) {
                this.heading = heading;
        }


        public void jump(double number) {
                move(Vector.getVector(number, heading));
        }


        public BasicAgent() {
                this(0.5, SpaceAgent.CIRCLE);
        }


        public BasicAgent(double r, int type) {
                super(r, type);
                heading = nextDoubleFromTo(0, 360);
                setColor(SpaceAgent.RED);
        }


        protected void wiggle() {
                heading += nextDoubleFromTo(-45, 45);
                jump(1);
        }

}
