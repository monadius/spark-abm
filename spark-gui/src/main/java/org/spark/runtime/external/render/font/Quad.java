package org.spark.runtime.external.render.font;

import org.sparkabm.math.Vector4d;

class TransformedColoredTextured implements Cloneable {
	public float X, Y, Z, rhw;
	public Vector4d ColorValue;
	public float u, v;

	public TransformedColoredTextured(float x, float y, float z, float rhw, Vector4d color, float u, float v) {
		this.X = x;
		this.Y = y;
		this.Z = z;
		this.rhw = rhw;
		this.ColorValue = color;
		this.u = u;
		this.v = v;
	}

	@Override
	public Object clone() {
		return copy();
	}

	public TransformedColoredTextured copy() {
		return new TransformedColoredTextured(X, Y, Z, rhw, ColorValue, u, v);
	}

}

public class Quad implements Cloneable {
	protected TransformedColoredTextured[] m_vertices;

	public Quad() {
		// Empty
	}

	// / <summary>Creates a new Quad</summary>
	// / <param name="topLeft">Top left vertex.</param>
	// / <param name="topRight">Top right vertex.</param>
	// / <param name="bottomLeft">Bottom left vertex.</param>
	// / <param name="bottomRight">Bottom right vertex.</param>
	public Quad(TransformedColoredTextured topLeft,
			TransformedColoredTextured topRight,
			TransformedColoredTextured bottomLeft,
			TransformedColoredTextured bottomRight) {
		m_vertices = new TransformedColoredTextured[6];
		m_vertices[0] = topLeft;
		m_vertices[1] = bottomRight;
		m_vertices[2] = bottomLeft;
		m_vertices[3] = topLeft;
		m_vertices[4] = topRight;
		m_vertices[5] = bottomRight;
	}

	// / <summary>Gets and sets the vertices.</summary>
	public TransformedColoredTextured[] getVertices() {
		return m_vertices;
	}

	// / <summary>Gets the top left vertex.</summary>
	public TransformedColoredTextured getTopLeft() {
		return m_vertices[0];
	}

	// / <summary>Gets the top right vertex.</summary>
	public TransformedColoredTextured getTopRight() {
		return m_vertices[4];
	}

	// / <summary>Gets the bottom left vertex.</summary>
	public TransformedColoredTextured getBottomLeft() {
		return m_vertices[2];
	}

	// / <summary>Gets the bottom right vertex.</summary>
	public TransformedColoredTextured getBottomRight() {
		return m_vertices[5];
	}

	// / <summary>Gets and sets the X coordinate.</summary>
	public float getX() {
		return m_vertices[0].X;
	}

	public void setX(float value) {
		float width = getWidth();
		m_vertices[0].X = value;
		m_vertices[1].X = value + width;
		m_vertices[2].X = value;
		m_vertices[3].X = value;
		m_vertices[4].X = value + width;
		m_vertices[5].X = value + width;
	}

	// / <summary>Gets and sets the Y coordinate.</summary>
	public float getY() {
		return m_vertices[0].Y;
	}

	public void setY(float value) {
		float height = getHeight();
		m_vertices[0].Y = value;
		m_vertices[1].Y = value + height;
		m_vertices[2].Y = value + height;
		m_vertices[3].Y = value;
		m_vertices[4].Y = value;
		m_vertices[5].Y = value + height;
	}

	// / <summary>Gets and sets the width.</summary>
	public float getWidth() {
		return m_vertices[4].X - m_vertices[0].X;
	}

	public void setWidth(float value) {
		m_vertices[1].X = m_vertices[0].X + value;
		m_vertices[4].X = m_vertices[0].X + value;
		m_vertices[5].X = m_vertices[0].X + value;
	}

	// / <summary>Gets and sets the height.</summary>
	public float getHeight() {
		return m_vertices[2].Y - m_vertices[0].Y;
	}

	public void setHeight(float value) {
		m_vertices[1].Y = m_vertices[0].Y + value;
		m_vertices[2].Y = m_vertices[0].Y + value;
		m_vertices[5].Y = m_vertices[0].Y + value;
	}

	// / <summary>Gets the X coordinate of the right.</summary>
	public float getRight() {
		return getX() + getWidth();
	}

	// / <summary>Gets the Y coordinate of the bottom.</summary>
	public float getBottom() {
		return getY() + getHeight();
	}

	// / <summary>Gets and sets the Quad's color.</summary>
	public Vector4d getColor() {
		return m_vertices[0].ColorValue;
	}

	public void setColor(Vector4d value) {
		for (int i = 0; i < 6; i++) {
			m_vertices[i].ColorValue = value;
		}
	}

	@Override
	public Object clone() {
		return new Quad(m_vertices[0].copy(), m_vertices[4].copy(),
				m_vertices[2].copy(), m_vertices[5].copy());
	}

}
