package org.sparkabm.gui.renderer;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

public class JavaRendererCanvas extends Canvas {
    private static final long serialVersionUID = 1L;

    private JavaRenderer renderer;
    boolean reshape = true;

    private Image bufferImage;


    /**
     * Internal constructor
     *
     * @param renderer
     */
    JavaRendererCanvas(JavaRenderer renderer) {
        super();
        this.renderer = renderer;
    }


    /**
     * Internal display method
     */
    void display() {
        if (bufferImage == null) {
            if (getWidth() <= 0 || getHeight() <= 0)
                return;
            bufferImage = this.createImage(getWidth(), getHeight());
            if (bufferImage == null)
                return;
        }

        if (!isVisible())
            return;

        Graphics g = bufferImage.getGraphics();
        display(g);
        if (g != null)
            g.dispose();

        g = this.getGraphics();
        if (g != null) {
            g.drawImage(bufferImage, 0, 0, this);
            g.dispose();
        }
    }


    /**
     * Internal display method
     *
     * @param g
     */
    void display(Graphics g) {
        if (reshape || renderer.reshapeRequested) {
            renderer.reshapeRequested = false;
            reshape = false;
            renderer.reshape(0, 0, getWidth(), getHeight());
        }

        if (g == null)
            return;


        g.clearRect(0, 0, getWidth(), getHeight());
        renderer.display((Graphics2D) g);
    }


    @Override
    public void paint(Graphics g) {
        display(g);
    }


    @Override
    public void validate() {
        super.validate();
        reshape = true;
    }


    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if (width > 0 && height > 0)
            bufferImage = this.createImage(width, height);

        reshape = true;
    }
}
