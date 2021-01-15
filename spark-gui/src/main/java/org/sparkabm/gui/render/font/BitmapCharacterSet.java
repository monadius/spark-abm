package org.sparkabm.gui.render.font;

/**
 * A set of bitmap characters
 */
public class BitmapCharacterSet {
    public int LineHeight;
    public int Base;
    public int RenderedSize;
    public int Width;
    public int Height;
    public BitmapCharacter[] Characters;

    /**
     * Constructor
     */
    public BitmapCharacterSet() {
        Characters = new BitmapCharacter[256];
        for (int i = 0; i < 256; i++) {
            Characters[i] = new BitmapCharacter();
        }
    }
}
