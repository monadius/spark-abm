package org.sparkabm.space;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.sparkabm.core.Agent;
import org.sparkabm.math.Vector;

/**
 * A distance joint for physical nodes
 *
 * @author Alexey
 */
@SuppressWarnings("serial")
public class DistanceJointLink extends SpaceLink {
    // Parameters for creating a joint
    private Vec2 anchor1, anchor2;
    private float freq, dampingRatio;

    private PhysicalSpace2d space;
    private Joint joint;

    /**
     * Default constructor
     */
    public DistanceJointLink() {
    }


    @Override
    public void die() {
        if (joint != null) {
            space.destroyJoint(joint);
        }

        super.die();
    }


    public void setAnchor1(Vector v) {
        anchor1 = new Vec2((float) v.x, (float) v.y);
    }


    public void setAnchor2(Vector v) {
        anchor2 = new Vec2((float) v.x, (float) v.y);
    }


    public void setParams(float freq, float dampingRatio) {
        this.freq = freq;
        this.dampingRatio = dampingRatio;
    }


    @Override
    public void connect(Agent end1, Agent end2) {
        // Cannot reconnect the link
        if (joint != null)
            return;

        if (!(end1 instanceof SpaceAgent) || !(end2 instanceof SpaceAgent))
            return;

        SpaceAgent e1 = (SpaceAgent) end1;
        SpaceAgent e2 = (SpaceAgent) end2;

        // Cannot connect agents in different spaces */
        if (e1.getSpace() != e2.getSpace())
            return;

        SpaceNode node1 = e1.getNode();
        SpaceNode node2 = e2.getNode();

        if (!(node1 instanceof PhysicalNode) || !(node2 instanceof PhysicalNode))
            return;

        PhysicalNode n1 = (PhysicalNode) node1;
        PhysicalNode n2 = (PhysicalNode) node2;

        if (n1.body == null || n2.body == null)
            return;


        DistanceJointDef jd = new DistanceJointDef();

        if (anchor1 == null)
            anchor1 = n1.body.getWorldCenter();

        if (anchor2 == null)
            anchor2 = n2.body.getWorldCenter();

        jd.initialize(n1.body, n2.body, anchor1, anchor2);
        jd.frequencyHz = freq;
        jd.dampingRatio = dampingRatio;

        space = (PhysicalSpace2d) n1.getSpace();
        joint = space.createJoint(jd);

        super.connect(end1, end2);
    }


    /**
     * Returns the first point to which the link is connected
     *
     * @return
     */
    public Vector getPoint1() {
        if (joint == null)
            return null;
        Vec2 v = joint.getAnchor1();
        return new Vector(v.x, v.y, 0);
    }


    /**
     * Returns the second point to which the link is connected
     *
     * @return
     */
    public Vector getPoint2() {
        if (joint == null)
            return null;
        Vec2 v = joint.getAnchor2();
        return new Vector(v.x, v.y, 0);
    }


    /**
     * Returns a vector-distance (componentwise distance)
     * between connected agents
     *
     * @return null if one or both ends are not initialized
     */
    public Vector getVector() {
        if (joint == null)
            return null;

        Space space = ((SpaceAgent) end1).getSpace();
        return space.getVector(getPoint1(), getPoint2());
    }

}
