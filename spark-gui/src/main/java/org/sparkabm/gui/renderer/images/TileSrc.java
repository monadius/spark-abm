package org.sparkabm.gui.renderer.images;

import static org.sparkabm.utils.XmlDocUtils.getIntegerValue;
import static org.sparkabm.utils.XmlDocUtils.getValue;

import java.awt.image.BufferedImage;

import org.w3c.dom.Node;

/**
 * Returns images from a tile grid
 *
 * @author Alexey
 */
class TileSrc extends ImageSrc {
    private final ImageSrc src;
    private final int xSize, ySize;
    private final int xTiles, yTiles;
    private final int xPad, yPad;


    /**
     * Constructs a tile source
     *
     * @param id:     the id of the tile
     * @param src:    the image source
     * @param xSize:  the width of each tile
     * @param ySize:  the height of each tile
     * @param xTiles: the number of tiles in x-direction
     * @param yTiles: the number of tiles in y-direction
     * @param xPad:   the x-space between tiles
     * @param yPad    the y-space between tiles
     */
    public TileSrc(String id, ImageSrc src, int xSize, int ySize,
                   int xTiles, int yTiles, int xPad, int yPad) {
        super(id);
        this.src = src;
        this.xSize = xSize;
        this.ySize = ySize;

        if (xTiles < 1)
            xTiles = 1;

        if (yTiles < 1)
            yTiles = 1;

        this.xTiles = xTiles;
        this.yTiles = yTiles;

        this.xPad = xPad;
        this.yPad = yPad;
    }

    /**
     * Returns an image
     */
    public BufferedImage getImage(String name) {
        BufferedImage image = src.getImage(name);

        String[] els = name.split(",");
        if (els == null || els.length != 2)
            return null;

        int x = 0;
        int y = 0;
        try {
            x = Integer.parseInt(els[0]);
            y = Integer.parseInt(els[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (x < 0)
            x = 0;

        if (y < 0)
            y = 0;

        if (x >= xTiles)
            x = xTiles - 1;

        if (y >= yTiles)
            y = yTiles - 1;

        image = image.getSubimage(x * (xSize + xPad), y * (ySize + yPad), xSize, ySize);
        return image;
    }


    // Reads information about a tile set
    public static TileSrc loadXml(ImageSrc src, Node tilesetNode) {
        String id = getValue(tilesetNode, "id", "tiles");
        int xSize = getIntegerValue(tilesetNode, "x-size", 1);
        int ySize = getIntegerValue(tilesetNode, "y-size", 1);
        int xTiles = getIntegerValue(tilesetNode, "x-tiles", 1);
        int yTiles = getIntegerValue(tilesetNode, "y-tiles", 1);
        int xPad = getIntegerValue(tilesetNode, "x-pad", 0);
        int yPad = getIntegerValue(tilesetNode, "y-pad", 0);

        return new TileSrc(id, src, xSize, ySize, xTiles, yTiles, xPad, yPad);
    }
}
