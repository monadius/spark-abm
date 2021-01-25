package org.sparkabm.gui.renderer.font;

import java.util.ArrayList;

/**
 * A bitmap character
 */
public class BitmapCharacter implements Cloneable {
    public int X;
    public int Y;
    public int Width;
    public int Height;
    public int XOffset;
    public int YOffset;
    public int XAdvance;
    public ArrayList<Kerning> KerningList = new ArrayList<Kerning>();

    /**
     * Kerning information
     */
    public static class Kerning {
        public int Second;
        public int Amount;
    }

    @Override
    public Object clone() {
        BitmapCharacter result = new BitmapCharacter();
        result.X = X;
        result.Y = Y;
        result.Width = Width;
        result.Height = Height;
        result.XOffset = XOffset;
        result.YOffset = YOffset;
        result.XAdvance = XAdvance;
        result.KerningList.addAll(KerningList);
        return result;
    }

    public Kerning FindKerningNode(int nextChar) {
        for (Kerning node : KerningList) {
            if (node.Second == nextChar)
                return node;
        }

        return null;
    }
}
