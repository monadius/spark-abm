/*
 * Author of the original applet: Geoff Leach.  gl@cs.rmit.edu.au
 * Date: 29/3/96
 */

package org.spark.math;



/* 
 * Wrapper class for basic int type for pass by reference.
 */
class Int {
    private int i;

    public Int() {
    	i = 0;
    }

    public Int(int i) {
    	this.i = i;
    }

    public void setValue(int i) {
    	this.i = i;
    }

    public int getValue() {
    	return i;
    }
}



/*
 * Point class.  RealPoint to avoid clash with java.awt.Point.
 */
class RealPoint {
	double x, y;

	RealPoint() {
		x = y = 0.0;
	}

	RealPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	RealPoint(RealPoint p) {
		x = p.x;
		y = p.y;
	}

	public double x() {
		return this.x;
	}

	public double y() {
		return this.y;
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double distance(RealPoint p) {
		double dx, dy;

		dx = p.x - x;
		dy = p.y - y;
		return Math.sqrt((double) (dx * dx + dy * dy));
	}

	public double distanceSq(RealPoint p) {
		double dx, dy;

		dx = p.x - x;
		dy = p.y - y;
		return dx * dx + dy * dy;
	}
}



/*
 * Vector class. A few elementary vector operations.
 */
class Vector2d {
	double u, v;

	Vector2d() {
		u = v = 0.0;
	}

	Vector2d(RealPoint p1, RealPoint p2) {
		u = p2.x() - p1.x();
		v = p2.y() - p1.y();
	}

	Vector2d(double u, double v) {
		this.u = u;
		this.v = v;
	}

	double dotProduct(Vector2d v) {
		return u * v.u + this.v * v.v;
	}

	static double dotProduct(RealPoint p1, RealPoint p2, RealPoint p3) {
		double u1, v1, u2, v2;

		u1 = p2.x() - p1.x();
		v1 = p2.y() - p1.y();
		u2 = p3.x() - p1.x();
		v2 = p3.y() - p1.y();

		return u1 * u2 + v1 * v2;
	}

	double crossProduct(Vector2d v) {
		return u * v.v - this.v * v.u;
	}

	static double crossProduct(RealPoint p1, RealPoint p2, RealPoint p3) {
		double u1, v1, u2, v2;

		u1 = p2.x() - p1.x();
		v1 = p2.y() - p1.y();
		u2 = p3.x() - p1.x();
		v2 = p3.y() - p1.y();

		return u1 * v2 - v1 * u2;
	}

	void setRealPoints(RealPoint p1, RealPoint p2) {
		u = p2.x() - p1.x();
		v = p2.y() - p1.y();
	}
}

/*
 * Circle class. Circles are fundamental to computation of Delaunay
 * triangulations. In particular, an operation which computes a circle defined
 * by three points is required.
 */
class Circle {
	RealPoint c;
	double r;

	Circle() {
		c = new RealPoint();
		r = 0.0;
	}

	Circle(RealPoint c, double r) {
		this.c = c;
		this.r = r;
	}

	public RealPoint center() {
		return c;
	}

	public double radius() {
		return r;
	}

	public void set(RealPoint c, double r) {
		this.c = c;
		this.r = r;
	}

	/*
	 * Tests if a point lies inside the circle instance.
	 */
	public boolean inside(RealPoint p) {
		if (c.distanceSq(p) < r * r)
			return true;
		else
			return false;
	}

	/*
	 * Compute the circle defined by three points (circumcircle).
	 */
	public void circumCircle(RealPoint p1, RealPoint p2, RealPoint p3) {
		double cp;

		cp = Vector2d.crossProduct(p1, p2, p3);
		if (cp != 0.0) {
			double p1Sq, p2Sq, p3Sq;
			double num;
			double cx, cy;

			p1Sq = p1.x() * p1.x() + p1.y() * p1.y();
			p2Sq = p2.x() * p2.x() + p2.y() * p2.y();
			p3Sq = p3.x() * p3.x() + p3.y() * p3.y();
			num = p1Sq * (p2.y() - p3.y()) + p2Sq * (p3.y() - p1.y()) + p3Sq
					* (p1.y() - p2.y());
			cx = num / (2.0f * cp);
			num = p1Sq * (p3.x() - p2.x()) + p2Sq * (p1.x() - p3.x()) + p3Sq
					* (p2.x() - p1.x());
			cy = num / (2.0f * cp);

			c.set(cx, cy);
		}

		// Radius
		r = c.distance(p1);
	}
}





/*
 * QuadraticAlgorithm class. O(n^2) algorithm.
 */
public class DelaunayTriangulation {
	private int s, t, u, bP;
	private Circle bC = new Circle();
	final static String algName = "O(n^2)";

	public DelaunayTriangulation() {
	}

	public void triangulate(Triangulation tri) throws Exception {
		int currentEdge;
		int nFaces;
		Int s, t;

		// Initialise.
		nFaces = 0;
		s = new Int();
		t = new Int();

		// Find closest neighbours and add edge to triangulation.
		findClosestNeighbours(tri.point, tri.nPoints, s, t);

		// Create seed edge and add it to the triangulation.
		tri.addEdge(s.getValue(), t.getValue(),
				Triangulation.Undefined, Triangulation.Undefined);

		currentEdge = 0;
		while (currentEdge < tri.nEdges) {
			if (tri.edge[currentEdge].l == Triangulation.Undefined) {
				completeFacet(currentEdge, tri, nFaces);
			}
			if (tri.edge[currentEdge].r == Triangulation.Undefined) {
				completeFacet(currentEdge, tri, nFaces);
			}
			currentEdge++;
		}
	}

	// Find the two closest points.
	public void findClosestNeighbours(RealPoint p[], int nPoints, Int u, Int v) {
		int i, j;
		double d, min;
		int s, t;

		s = t = 0;
		min = Double.MAX_VALUE;
		for (i = 0; i < nPoints - 1; i++)
			for (j = i + 1; j < nPoints; j++) {
				d = p[i].distanceSq(p[j]);
				if (d < min) {
					s = i;
					t = j;
					min = d;
				}
			}
		u.setValue(s);
		v.setValue(t);
	}

	/*
	 * Complete a facet by looking for the circle free point to the left of the
	 * edge "e_i". Add the facet to the triangulation.
	 * 
	 * This function is a bit long and may be better split.
	 */
	public void completeFacet(int eI, Triangulation tri, int nFaces) throws Exception {
		double cP;
		Triangulation.Edge e[] = tri.edge;
		RealPoint p[] = tri.point;

		// Cache s and t.
		if (e[eI].l == Triangulation.Undefined) {
			s = e[eI].s;
			t = e[eI].t;
		} else if (e[eI].r == Triangulation.Undefined) {
			s = e[eI].t;
			t = e[eI].s;
		} else
			// Edge already completed.
			return;

		// Find a point on left of edge.
		for (u = 0; u < tri.nPoints; u++) {
			if (u == s || u == t)
				continue;
			if (Vector2d.crossProduct(p[s], p[t], p[u]) > 0.0)
				break;
		}

		// Find best point on left of edge.
		bP = u;
		if (bP < tri.nPoints) {
			bC.circumCircle(p[s], p[t], p[bP]);

			for (u = bP + 1; u < tri.nPoints; u++) {
				if (u == s || u == t)
					continue;

				cP = Vector2d.crossProduct(p[s], p[t], p[u]);

				if (cP > 0.0)
					if (bC.inside(p[u])) {
						bP = u;
						bC.circumCircle(p[s], p[t], p[u]);
					}
			}
		}

		// Add new triangle or update edge info if s-t is on hull.
		if (bP < tri.nPoints) {
			// Update face information of edge being completed.
			tri.updateLeftFace(eI, s, t, nFaces);
			nFaces++;

			// Add new edge or update face info of old edge.
			eI = tri.findEdge(bP, s);
			if (eI == Triangulation.Undefined)
				// New edge.
				eI = tri.addEdge(bP, s, nFaces, Triangulation.Undefined);
			else
				// Old edge.
				tri.updateLeftFace(eI, bP, s, nFaces);

			// Add new edge or update face info of old edge.
			eI = tri.findEdge(t, bP);
			if (eI == Triangulation.Undefined)
				// New edge.
				eI = tri.addEdge(t, bP, nFaces, Triangulation.Undefined);
			else
				// Old edge.
				tri.updateLeftFace(eI, t, bP, nFaces);
		} else
			tri.updateLeftFace(eI, s, t, Triangulation.Universe);
	}
}

