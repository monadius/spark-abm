package org.sparkabm.runtime.data;

import org.sparkabm.math.Vector;
import org.sparkabm.math.Vector4d;
import org.sparkabm.space.PhysicalNode;

/**
 * Data for a set of space agents
 *
 * @author Monad
 */
@SuppressWarnings("serial")
public class DataObject_SpaceAgents extends DataObject {
    private String[] labels;
    private Vector[] positions;
    private double[] radii;
    private Vector4d[] colors;
    private double[] rotations;
    private ShapeInfo[] shapeInfo;

    private int[] shapes;
    private int[] spaceIndices;

    private int counter;
    private int n;

    // Describes agent's shape
    public static class ShapeInfo {
        public static final int CIRCLE = 0;
        public static final int RECTANGLE = 1;

        public final int type;
        public final float hx, hy;

        public ShapeInfo(PhysicalNode.ShapeInfo si) {
            this.type = si.type;
            this.hx = si.hx;
            this.hy = si.hy;
        }
    }


    /**
     * Creates a data object for the given number of space agents
     *
     * @param agentsNumber
     */
    public DataObject_SpaceAgents(int agentsNumber) {
        if (agentsNumber < 0)
            agentsNumber = 0;

        if (agentsNumber > 0) {
            labels = new String[agentsNumber];
            positions = new Vector[agentsNumber];
            radii = new double[agentsNumber];
            colors = new Vector4d[agentsNumber];
            rotations = new double[agentsNumber];
            shapes = new int[agentsNumber];
            spaceIndices = new int[agentsNumber];
            shapeInfo = new ShapeInfo[agentsNumber];
        }

        n = agentsNumber;
        counter = 0;
    }


    /**
     * Empty protected constructor
     */
    protected DataObject_SpaceAgents() {

    }


    /**
     * Adds agent's parameters into the data object
     *
     * @param position
     * @param r
     * @param color
     * @param shape
     */
    public void addAgent(String label, Vector position, double r, Vector4d color, double rotation, ShapeInfo si, int shape, int spaceIndex) {
        // Cannot hold any more agents
        if (counter >= n)
            return;

        labels[counter] = label;
        positions[counter] = position;
        radii[counter] = r;
        colors[counter] = color;
        rotations[counter] = rotation;
        shapeInfo[counter] = si;
        shapes[counter] = shape;
        spaceIndices[counter] = spaceIndex;

        counter++;
    }


    /**
     * Returns the total number of agents in the data object
     *
     * @return
     */
    public int getTotalNumber() {
        return counter;
    }


    public String[] getLabels() {
        return labels;
    }

    public Vector[] getPositions() {
        return positions;
    }

    public double[] getRadii() {
        return radii;
    }

    public Vector4d[] getColors() {
        return colors;
    }

    public double[] getRotations() {
        return rotations;
    }

    public ShapeInfo[] getShapeInfo() {
        return shapeInfo;
    }

    public int[] getShapes() {
        return shapes;
    }


    public int[] getSpaceIndices() {
        return spaceIndices;
    }

    @Override
    public String toString() {
        if (positions == null)
            return "0";
        else
            return String.valueOf(positions.length);
    }

}
