package org.sparkabm.gui.render.images;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Source of images
 *
 * @author Alexey
 */
abstract class ImageSrc {
    // Image source identifier
    private String id;

    /**
     * Protected constructor
     *
     * @param id
     */
    protected ImageSrc(String id) {
        this.id = id;
    }

    /**
     * Image source identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Makes all pixels of the given color transparent
     */
    public static BufferedImage makeTransparent(Image img, Color keyColor) {
        BufferedImage alpha = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g = alpha.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(img, 0, 0, null);
        g.dispose();

        for (int y = 0; y < alpha.getHeight(); y++) {
            for (int x = 0; x < alpha.getWidth(); x++) {
                int col = alpha.getRGB(x, y);
                if (col == keyColor.getRGB()) {
                    // make transparent
                    alpha.setRGB(x, y, col & 0x00ffffff);
                }
            }
        }

        return alpha;
    }


    /**
     * Returns image with the given name
     *
     * @param name
     * @return
     */
    public abstract BufferedImage getImage(String name);
}







