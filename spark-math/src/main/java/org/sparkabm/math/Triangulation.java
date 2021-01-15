/*
 * Author of the original applet: Geoff Leach.  gl@cs.rmit.edu.au
 * Date: 29/3/96
 */

package org.sparkabm.math;

/*
 * Triangulation class. A triangulation is represented as a set of points and
 * the edges which form the triangulation.
 */
public class Triangulation {
    static final int Undefined = -1;
    static final int Universe = 0;
    final int nPoints;
    final RealPoint point[];

    int nEdges;
    int maxEdges;
    public Edge edge[];


    /*
     * Edge class. Edges have two vertices, s and t, and two faces, l (left) and r
     * (right). The triangulation representation and the Delaunay triangulation
     * algorithms require edges.
     */
    public static class Edge {
        public int s, t;
        int l, r;

        Edge() {
            s = t = 0;
        }

        Edge(int s, int t) {
            this.s = s;
            this.t = t;
        }

        int s() {
            return this.s;
        }

        int t() {
            return this.t;
        }

        int l() {
            return this.l;
        }

        int r() {
            return this.r;
        }
    }

    public Triangulation(Vector[] pts) {
        // Allocate points.
        this.nPoints = pts.length;
        this.point = new RealPoint[nPoints];

        for (int i = 0; i < nPoints; i++) {
            Vector v = pts[i];
            point[i] = new RealPoint(v.x, v.y);
        }

        // Allocate edges.
        maxEdges = 3 * nPoints - 6; // Max number of edges.
        edge = new Edge[maxEdges];
        for (int i = 0; i < maxEdges; i++)
            edge[i] = new Edge();
        nEdges = 0;
    }


    /*
     * Copies a set of points.
     */
    public void copyPoints(Triangulation t) {
        int n;

        if (t.nPoints < nPoints)
            n = t.nPoints;
        else
            n = nPoints;

        for (int i = 0; i < n; i++) {
            point[i].x = t.point[i].x;
            point[i].y = t.point[i].y;
        }

        nEdges = 0;
    }

    void addTriangle(int s, int t, int u) {
        addEdge(s, t);
        addEdge(t, u);
        addEdge(u, s);
    }

    public int addEdge(int s, int t) {
        return addEdge(s, t, Undefined, Undefined);
    }

    /*
     * Adds an edge to the triangulation. Store edges with lowest vertex first
     * (easier to debug and makes no other difference).
     */
    public int addEdge(int s, int t, int l, int r) {
        int e;

        // Add edge if not already in the triangulation.
        e = findEdge(s, t);
        if (e == Undefined)
            if (s < t) {
                edge[nEdges].s = s;
                edge[nEdges].t = t;
                edge[nEdges].l = l;
                edge[nEdges].r = r;
                return nEdges++;
            } else {
                edge[nEdges].s = t;
                edge[nEdges].t = s;
                edge[nEdges].l = r;
                edge[nEdges].r = l;
                return nEdges++;
            }
        else
            return Undefined;
    }

    public int findEdge(int s, int t) {
        boolean edgeExists = false;
        int i;

        for (i = 0; i < nEdges; i++)
            if (edge[i].s == s && edge[i].t == t || edge[i].s == t
                    && edge[i].t == s) {
                edgeExists = true;
                break;
            }

        if (edgeExists)
            return i;
        else
            return Undefined;
    }

    /*
     * Update the left face of an edge.
     */
    public void updateLeftFace(int eI, int s, int t, int f) throws Exception {
        if (!((edge[eI].s == s && edge[eI].t == t) || (edge[eI].s == t && edge[eI].t == s)))
            throw new Exception("updateLeftFace: adj. matrix and edge table mismatch");
        if (edge[eI].s == s && edge[eI].l == Triangulation.Undefined)
            edge[eI].l = f;
        else if (edge[eI].t == s && edge[eI].r == Triangulation.Undefined)
            edge[eI].r = f;
        else
            throw new Exception("updateLeftFace: attempt to overwrite edge info");
    }

}