package org.sparkabm.gui.render.font;

public class FontQuad extends Quad {
    private int m_lineNumber;
    private int m_wordNumber;
    private float m_sizeScale;
    private BitmapCharacter m_bitmapChar = null;
    private char m_character;
    private float m_wordWidth;

    // / <summary>Creates a new FontQuad</summary>
    // / <param name="topLeft">Top left vertex</param>
    // / <param name="topRight">Top right vertex</param>
    // / <param name="bottomLeft">Bottom left vertex</param>
    // / <param name="bottomRight">Bottom right vertex</param>
    public FontQuad(TransformedColoredTextured topLeft,
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

    // / <summary>Gets and sets the line number.</summary>
    public int getLineNumber() {
        return m_lineNumber;
    }

    public void setLineNumber(int value) {
        m_lineNumber = value;
    }

    // / <summary>Gets and sets the word number.</summary>
    public int getWordNumber() {
        return m_wordNumber;
    }

    public void setWordNumber(int value) {
        m_wordNumber = value;
    }

    // / <summary>Gets and sets the word width.</summary>
    public float getWordWidth() {
        return m_wordWidth;
    }

    public void setWordWidth(float value) {
        m_wordWidth = value;
    }

    // / <summary>Gets and sets the BitmapCharacter.</summary>
    public BitmapCharacter getBitmapCharacter() {
        return m_bitmapChar;
    }

    public void setBitmapCharacter(BitmapCharacter value) {
        m_bitmapChar = (BitmapCharacter) value.clone();
    }

    // / <summary>Gets and sets the character displayed in the quad.</summary>
    public char getCharacter() {
        return m_character;
    }

    public void setCharacter(char value) {
        m_character = value;
    }

    // / <summary>Gets and sets the size scale.</summary>
    public float getSizeScale() {
        return m_sizeScale;
    }

    public void setSizeScale(float value) {
        m_sizeScale = value;
    }
}
