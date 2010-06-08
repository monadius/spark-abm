model Model

space GridSpace -40 40 -40 40 true true

@datalayer(color = "blue", min = 0, max = 5)
global cidal-compound : grid
global tissue-life : grid
global toxin : grid

@chart(interval = 1, name = "Total Tissue Damage")
global total-tissue-damage : number

@parameter(name = "Initial infection", min = 0, max = 1000, step = 1)
global initial-infection-number = 500

@parameter(name = "Cidal compound production", min = 0, max = 30, step = 0.1)
global cidal-compound-production = 4

@parameter(name = "Chemotaxis threshold", min = 0, max = 1, step = 0.1)
global chemotaxis-threshold = 0.5 : double

@parameter
global yes-toxin : boolean


global CONST = space-xsize * space-ysize * 100


to end-step [tick]
;  tissue-life -= cidal-compound + toxin

; Experimental field 'data' refers to all data values inside a grid
  tissue-life.data = tissue-life.data - cidal-compound.data - toxin.data

  cidal-compound.multiply 0.9
  toxin.multiply 0.9

  diffuse cidal-compound 0.9
  diffuse toxin 0.9


; local variant: process each value individually
;  modify tissue-life
;  [
;   tissue-life -= cidal-compound + toxin
;   damage += 100 - tissue-life
;  ]

   total-tissue-damage = CONST - sum tissue-life
end


to setup
  tissue-life.set-value 100
  total-tissue-damage = 0

  ask create InflamCell 500
  [
   jump random 100
  ]

  infect
end


to infect
  ask create InfectAgent initial-infection-number
  [
   jump random sqrt initial-infection-number
  ]
end



***end***
package ToyInfectionModel;

import org.spark.startup.ABMModel;
import org.spark.core.*;
import org.spark.space.*;
import org.spark.data.*;
import static org.spark.utils.RandomHelper.nextDoubleFromTo;


public class Model implements ABMModel {

        public boolean end(long tick) {
                double[][] tissueLife = ((Grid) ModelData.tissueLife).getData();
                double[][] cidalCompound = ((Grid) ModelData.cidalCompound).getData();
                double[][] toxin = ((Grid) ModelData.toxin).getData();

                int n = tissueLife.length;
                int m = tissueLife[0].length;

                double damage = 0;

                for (int i = 0; i < n; i++)
                        for (int j = 0; j < m; j++) {
                                tissueLife[i][j] -= cidalCompound[i][j] + toxin[i][j];
                                if (tissueLife[i][j] < 0) tissueLife[i][j] = 0;
                                damage += 100 - tissueLife[i][j];
                        }

                ModelData.cidalCompound.multiply(0.9);
                ModelData.toxin.multiply(0.9);

                ((Grid) ModelData.cidalCompound).diffuse(0.9);
                ((Grid) ModelData.toxin).diffuse(0.9);

//                ModelData.totalTissueDamage = damage / 1000;
//                ModelData.totalCidalCompound = ModelData.cidalCompound.getTotalNumber();

                return false;
        }

        public void setup() {
                System.err.println("Model");
                int xSize = 100, ySize = 100;

                Observer observer = Observer.getInstance();
                observer.setSpace(new GridSpace(-xSize, xSize, -ySize, ySize, true, true));
//                observer.setSpace(new StandardSpace(-xSize, xSize, -ySize, ySize, true, true));

                ModelData.tissueLife = observer.addDataLayer("tissue-life", new Grid(2 * xSize, 2 * ySize));
                ModelData.toxin = observer.addDataLayer("toxin", new Grid(2 * xSize, 2 * ySize));
                ModelData.cidalCompound = observer.addDataLayer("cidal-compound", new Grid(2 * xSize, 2 * ySize));

                ModelData.tissueLife.setValue(100);
                ModelData.totalTissueDamage = 0;
                ModelData.totalCidalCompound = 0;

                for (int i = 0; i < 500; i++) {
                        InflamCell inflamCell = new InflamCell();
//                        for (int k = 0; k < 10; k++)
//                                inflamCell.jump(nextDoubleFromTo(0, 100));
                        inflamCell.setRandomPosition();
                }


                infect();
        }


        public static void infect() {
                for (int i = 0; i < ModelData.initialInfectionNumber; i++) {
                        InfectAgent infectAgent = new InfectAgent();
                        infectAgent.jump(nextDoubleFromTo(0, Math.sqrt(ModelData.initialInfectionNumber)));
                }
        }

        /* data values for charts */

        public static double getInfectAgentsNumber() {
                return Observer.getInstance().getAgentsNumber(InfectAgent.class);
        }


        public static double getTissueDamage() {
                return ModelData.totalTissueDamage;
        }


        public static double getToxin() {
                return ModelData.toxin.getTotalNumber();
        }


        public static double getCidalCompound() {
                return ModelData.totalCidalCompound;
        }


        /* parameters */
        public static int getInitialInfectionNumber() {
                return ModelData.initialInfectionNumber;
        }

        public static void setInitialInfectionNumber(Integer val) {
                ModelData.initialInfectionNumber = val;
        }

        public static double getChemotaxisThreshold() {
                return ModelData.chemotaxisThreshold;
        }

        public static void setChemotaxisThreshold(Double val) {
                ModelData.chemotaxisThreshold = val;
        }

        public static double getCidalCompoundProduction() {
                return ModelData.cidalCompoundProduction;
        }

        public static void setCidalCompoundProduction(Double val) {
                ModelData.cidalCompoundProduction = val;
        }

        public static boolean getYesToxin() {
                return ModelData.yesToxin;
        }

        public static void setYesToxin(Boolean val) {
                ModelData.yesToxin = val;
        }


        public boolean begin(long tick) {
                return false;
        }


}