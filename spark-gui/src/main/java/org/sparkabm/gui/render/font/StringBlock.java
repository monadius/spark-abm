package org.sparkabm.gui.render.font;

import java.awt.geom.Rectangle2D;

import org.sparkabm.math.Vector4d;

/**
 * Individual string to load into vertex buffer
 */
public class StringBlock {
    public String Text;
    public Rectangle2D.Float TextBox;
    public BitmapFont.Align Alignment;
    public float Size;
    public Vector4d Color;
    public boolean Kerning;

    // <summary>Creates a new StringBlock</summary>
    // <param name="text">Text to render</param>
    // <param name="textBox">Text box to constrain text</param>
    // <param name="alignment">Font alignment</param>
    // <param name="size">Font size</param>
    // <param name="color">Color</param>
    // <param name="kerning">true to use kerning, false otherwise.</param>
    public StringBlock(String text, Rectangle2D.Float textBox,
                       BitmapFont.Align alignment, float size, Vector4d color,
                       boolean kerning) {
        Text = text;
        TextBox = textBox;
        Alignment = alignment;
        Size = size;
        Color = color;
        Kerning = kerning;
    }
}
