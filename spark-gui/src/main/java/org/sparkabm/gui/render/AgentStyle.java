package org.sparkabm.gui.render;

import java.awt.Font;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL;

import org.sparkabm.math.Vector4d;
import org.sparkabm.gui.render.font.BitmapFont;
import org.sparkabm.gui.render.images.TileManager;
import org.sparkabm.gui.render.images.TileManagerInfo;
import org.sparkabm.utils.FileUtils;

import static org.sparkabm.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AgentStyle implements Comparable<AgentStyle> {
    private static final Logger logger = Logger.getLogger(AgentStyle.class.getName());

    /* Agent's type name */
    public String typeName;

    /* Agent's (short) name */
    public String name;

    /* Basic options */

    public boolean transparent = false;
    public boolean visible = true;
    public boolean border = true;
    public boolean label = false;

    /* Advanced options */

    // Transparency
    private float transparencyCoefficient;

    // Blending
    private int blendSrc, blendDst;
    private int textureEnv;

    // Alpha
    private int alphaFunc;
    private float alphaFuncValue;

    // Stencil
    private int stencilFunc;
    private int stencilRef;
    private int stencilMask;

    private int stencilFail;
    private int stencilZFail;
    private int stencilZPass;

    // If true then the color is blended with the agent's texture
    private boolean colorBlending;

    // Indicates that a shape is rendering along with an image
    private boolean drawShapeWithImage;

    // The image scale factor
    private float scaleFactor;
    // If true, then the scale factor is multiplied by the agent's size
    private boolean modulateSize;

    /* Label options */
    private Font font;
    // null means a default font
    private String fontFamily;
    private int fontSize;
    private int fontStyle;
    private float dxLabel, dyLabel;
    private float labelWidth, labelHeight;
    // If true then the agent's color is mixed with the label color
    private boolean modulateLabelColor;
    // If true then the label size is multiplied by the agent's size
    private boolean modulateLabelSize;
    private Vector4d labelColor = new Vector4d();

    // Bitmap font options
    private String bitmapFontName;
    private float bitmapFontSize;
    private BitmapFont.Align textAlignment;


    /* TileManager */
    private File tileFile;
    private TileManagerInfo tileManager;


    /***************************************/
    /* Constants */
    private static final String ATTR_VISIBLE = "visible";
    private static final String ATTR_TRANSPARENT = "transparent";
    private static final String ATTR_BORDER = "border";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_PRIORITY = "position";
    private static final String ATTR_SCALE = "scale";
    // Blending
    private static final String ATTR_TEXTURE_ENV = "texture-env";
    private static final String ATTR_BLEND_SRC = "blend-src";
    private static final String ATTR_BLEND_DST = "blend-dst";
    private static final String ATTR_TRANSPARENCY = "transparency-coefficient";
    private static final String ATTR_ALPHA_FUNC = "alpha-function";
    private static final String ATTR_ALPHA_FUNC_VALUE = "alpha-function-value";
    // Stencil
    private static final String ATTR_STENCIL_FUNC = "stencil-function";
    private static final String ATTR_STENCIL_REF = "stencil-ref";
    private static final String ATTR_STENCIL_MASK = "stencil-mask";
    private static final String ATTR_STENCIL_FAIL = "stencil-fail";
    private static final String ATTR_STENCIL_ZFAIL = "stencil-zfail";
    private static final String ATTR_STENCIL_ZPASS = "stencil-zpass";
    // Flags
    private static final String ATTR_MODULATE_SIZE = "modulate-size";
    private static final String ATTR_COLOR_BLENDING = "color-blending";
    private static final String ATTR_DRAW_SHAPE = "draw-shape-with-image";
    // Label
    private static final String ATTR_FONT_FAMILY = "font-family";
    private static final String ATTR_FONT_SIZE = "font-size";
    private static final String ATTR_FONT_STYLE = "font-style";
    private static final String ATTR_DX_LABEL = "dx-label";
    private static final String ATTR_DY_LABEL = "dy-label";
    private static final String ATTR_LABEL_WIDTH = "label-width";
    private static final String ATTR_LABEL_HEIGHT = "label-height";
    private static final String ATTR_TEXT_ALIGNMENT = "text-alignment";
    private static final String ATTR_LABEL_COLOR = "label-color";
    private static final String ATTR_MODULATE_LABEL_COLOR = "modulate-label-color";
    private static final String ATTR_MODULATE_LABEL_SIZE = "modulate-label-size";
    // Bitmap font
    private static final String ATTR_BITMAP_FONT_NAME = "bitmap-font";
    private static final String ATTR_BITMAP_FONT_SIZE = "bitmap-font-size";
    // Tile manager
    private static final String ATTR_TILE_MANAGER = "tile-manager";


    /***************************************/
    /**
     * Constructor
     */
    public AgentStyle(String agentTypeName, String name,
                      boolean transparent, boolean visible, boolean border) {
        this.typeName = agentTypeName;
        this.name = name;
        this.transparent = transparent;
        this.visible = visible;
        this.border = border;

        // Default values of advanced parameters
        textureEnv = 0;

        // Set default values
        // 0 means that a feature is disabled
        blendSrc = blendDst = 0;
        alphaFunc = 4;
        alphaFuncValue = 0.0f;
        transparencyCoefficient = 0.5f;
        bitmapFontSize = 1.0f;
        modulateLabelColor = false;
        modulateLabelSize = false;
        labelWidth = 100.0f;
        labelHeight = 100.0f;
        labelColor.a = 1.0d;
        textAlignment = BitmapFont.Align.Left;
        scaleFactor = 1.0f;
        modulateSize = true;
    }


    public AgentStyle(String agentTypeName) {
        this(agentTypeName, null, false, true, true);
    }


    /**
     * Describes a rendering property
     */
    public static class RenderProperty {
        public final String name;
        public final int value;

        public RenderProperty(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final RenderProperty[] textureEnvs = new RenderProperty[]{
            new RenderProperty("GL_MODULATE", GL.GL_MODULATE),
            new RenderProperty("GL_REPLACE", GL.GL_REPLACE),
            new RenderProperty("GL_BLEND", GL.GL_BLEND),
            new RenderProperty("GL_DECAL", GL.GL_DECAL)
    };

    public int getTextureEnv() {
        if (textureEnv < 0 || textureEnv >= textureEnvs.length)
            textureEnv = 0;

        return textureEnvs[textureEnv].value;
    }

    public int getTextureEnvIndex() {
        return textureEnv;
    }

    public void setTextureEnv(int index) {
        if (index >= 0 && index < textureEnvs.length)
            textureEnv = index;
    }


    public static final RenderProperty[] alphaFuncs = new RenderProperty[]{
            // For the first value (index = 0) the alpha test is turned off
            new RenderProperty("NONE", -1),
            // GL_ALWAYS equivalent to turned off alpha function
            new RenderProperty("GL_ALWAYS", GL.GL_ALWAYS),
            new RenderProperty("GL_NEVER", GL.GL_NEVER),
            new RenderProperty("GL_LESS", GL.GL_LESS),
            new RenderProperty("GL_GREATER", GL.GL_GREATER),
            new RenderProperty("GL_EQUAL", GL.GL_EQUAL),
            new RenderProperty("GL_LEQUAL", GL.GL_LEQUAL),
            new RenderProperty("GL_GEQUAL", GL.GL_GEQUAL),
            new RenderProperty("GL_NOTEQUAL", GL.GL_NOTEQUAL)
    };

    public static final RenderProperty[] stencilFuncs = new RenderProperty[]{
            // For the first value (index = 0) the stencil test is turned off
            new RenderProperty("NONE", -1),
            // GL_ALWAYS equivalent to turned off alpha function
            new RenderProperty("GL_ALWAYS", GL.GL_ALWAYS),
            new RenderProperty("GL_NEVER", GL.GL_NEVER),
            new RenderProperty("GL_LESS", GL.GL_LESS),
            new RenderProperty("GL_GREATER", GL.GL_GREATER),
            new RenderProperty("GL_EQUAL", GL.GL_EQUAL),
            new RenderProperty("GL_LEQUAL", GL.GL_LEQUAL),
            new RenderProperty("GL_GEQUAL", GL.GL_GEQUAL),
            new RenderProperty("GL_NOTEQUAL", GL.GL_NOTEQUAL)
    };

    public static final RenderProperty[] stencilOps = new RenderProperty[]{
            new RenderProperty("GL_KEEP", GL.GL_KEEP),
            new RenderProperty("GL_ZERO", GL.GL_ZERO),
            new RenderProperty("GL_REPLACE", GL.GL_REPLACE),
            new RenderProperty("GL_INCR", GL.GL_INCR),
            new RenderProperty("GL_DECR", GL.GL_DECR),
            new RenderProperty("GL_INVERT", GL.GL_INVERT),
    };

    // Transparency
    public float getTransparencyCoefficient() {
        return transparencyCoefficient;
    }

    public void setTransparencyCoefficient(float val) {
        transparencyCoefficient = val;
    }

    // Scaling
    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scale) {
        this.scaleFactor = scale;
    }

    public boolean getModulateSize() {
        return modulateSize;
    }

    public void setModulateSize(boolean flag) {
        modulateSize = flag;
    }

    // Alpha

    public int getAlphaFunc() {
        return alphaFuncs[alphaFunc].value;
    }

    public int getAlphaFuncIndex() {
        return alphaFunc;
    }

    public void setAlphaFunc(int index) {
        if (index >= 0 && index < alphaFuncs.length)
            alphaFunc = index;
    }

    public float getAlphaFuncValue() {
        return alphaFuncValue;
    }

    public void setAlphaFuncValue(float v) {
        alphaFuncValue = v;
    }

    // Stencil

    public int getStencilFunc() {
        return stencilFuncs[stencilFunc].value;
    }

    public int getStencilFuncIndex() {
        return stencilFunc;
    }

    public void setStencilFunc(int index) {
        if (index >= 0 && index < stencilFuncs.length)
            stencilFunc = index;
    }

    public int getStencilRef() {
        return stencilRef;
    }

    public int getStencilMask() {
        return stencilMask;
    }

    public void setStencilRef(int val) {
        stencilRef = val;
    }

    public void setStencilMask(int mask) {
        stencilMask = mask;
    }

    public int getStencilFail() {
        return stencilOps[stencilFail].value;
    }

    public int getStencilZFail() {
        return stencilOps[stencilZFail].value;
    }

    public int getStencilZPass() {
        return stencilOps[stencilZPass].value;
    }

    public int getStencilFailIndex() {
        return stencilFail;
    }

    public int getStencilZFailIndex() {
        return stencilZFail;
    }

    public int getStencilZPassIndex() {
        return stencilZPass;
    }

    public void setStencilFail(int index) {
        if (index >= 0 && index < stencilOps.length)
            stencilFail = index;
    }

    public void setStencilZFail(int index) {
        if (index >= 0 && index < stencilOps.length)
            stencilZFail = index;
    }

    public void setStencilZPass(int index) {
        if (index >= 0 && index < stencilOps.length)
            stencilZPass = index;
    }

    /**
     * If true, then the agent color should be used for the label
     */
    public boolean getModulateLabelColor() {
        return modulateLabelColor;
    }

    /**
     * If true, then the agent color should be used for the label
     */
    public void setModulateLabelColor(boolean flag) {
        this.modulateLabelColor = flag;
    }

    /**
     * If true, then the agent color should be used for the label
     */
    public boolean getModulateLabelSize() {
        return modulateLabelSize;
    }

    /**
     * If true, then the agent color should be used for the label
     */
    public void setModulateLabelSize(boolean flag) {
        this.modulateLabelSize = flag;
    }

    /**
     * Returns the name of the bitmap font
     */
    public String getBitmapFontName() {
        return bitmapFontName;
    }

    /**
     * Sets the name of the bitmap font
     */
    public void setBitmapFontName(String name) {
        this.bitmapFontName = name;
    }

    /**
     * Returns the relative size of the bitmap font
     */
    public float getBitmapFontSize() {
        return bitmapFontSize;
    }

    /**
     * Sets the relative size of the bitmap font
     */
    public void setBitmapFontSize(float value) {
        if (value < 0)
            return;

        this.bitmapFontSize = value;
    }

    /**
     * Returns the label rectangle width
     */
    public float getLabelWidth() {
        return labelWidth;
    }


    /**
     * Returns the label rectangle height
     */
    public float getLabelHeight() {
        return labelHeight;
    }

    /**
     * Sets the label rectangle dimensions
     */
    public void setLabelDimension(float width, float height) {
        this.labelWidth = width;
        this.labelHeight = height;
    }

    /**
     * Returns a font for printing labels
     *
     * @return
     */
    public Font getFont() {
        if (font == null) {
            if (fontFamily == null)
                font = new Font("Arial", 0, 10);
            else
                font = new Font(fontFamily, fontStyle, fontSize);
        }

        return font;
    }


    /**
     * Sets a new font for printing labels
     */
    public void setFont(String fontFamily, int fontStyle, int fontSize) {
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.fontSize = fontSize;
        // Invalidate the existing font
        this.font = null;
    }


    /**
     * Returns the x-offset of a label
     */
    public float getLabelDx() {
        return dxLabel;
    }


    /**
     * Returns the y-offset of a label
     */
    public float getLabelDy() {
        return dyLabel;
    }


    /**
     * Sets the x and y offsets of a label
     *
     * @param dx
     * @param dy
     */
    public void setLabelOffset(float dx, float dy) {
        this.dxLabel = dx;
        this.dyLabel = dy;
    }


    /**
     * Returns the text alignment
     */
    public BitmapFont.Align getTextAlignment() {
        return textAlignment;
    }


    /**
     * Sets the text alignment
     */
    public void setTextAlignment(BitmapFont.Align alignment) {
        this.textAlignment = alignment;
    }


    /**
     * Returns the color of a label
     */
    public Vector4d getLabelColor() {
        return new Vector4d(labelColor);
    }


    /**
     * Sets the color of a label
     */
    public void setLabelColor(Vector4d c) {
        this.labelColor = new Vector4d(c);
    }


    /**
     * Returns a flag
     */
    public boolean getColorBlending() {
        return colorBlending;
    }

    /**
     * Sets a flag
     */
    public void setColorBlending(boolean flag) {
        this.colorBlending = flag;
    }

    /**
     * Returns a flag
     */
    public boolean getDrawShapeWithImageFlag() {
        return drawShapeWithImage;
    }

    /**
     * Sets a flag
     */
    public void setDrawShapeWithImageFlag(boolean flag) {
        this.drawShapeWithImage = flag;
    }


    public static final RenderProperty[] srcBlends = new RenderProperty[]{
            new RenderProperty("NONE", -1),
            new RenderProperty("GL_ZERO", GL.GL_ZERO),
            new RenderProperty("GL_ONE", GL.GL_ONE),
            new RenderProperty("GL_DST_COLOR", GL.GL_DST_COLOR),
            new RenderProperty("GL_ONE_MINUS_DST_COLOR", GL.GL_ONE_MINUS_DST_COLOR),
            new RenderProperty("GL_SRC_ALPHA", GL.GL_SRC_ALPHA),
            new RenderProperty("GL_ONE_MINUS_SRC_ALPHA", GL.GL_ONE_MINUS_SRC_ALPHA),
            new RenderProperty("GL_DST_ALPHA", GL.GL_DST_ALPHA),
            new RenderProperty("GL_ONE_MINUS_DST_ALPHA", GL.GL_ONE_MINUS_DST_ALPHA),
            new RenderProperty("GL_SRC_ALPHA_SATURATE", GL.GL_SRC_ALPHA_SATURATE)
    };

    public int getSrcBlend() {
        return srcBlends[blendSrc].value;
    }

    public int getSrcBlendIndex() {
        return blendSrc;
    }

    public void setSrcBlend(int index) {
        if (index >= 0 && index < srcBlends.length)
            blendSrc = index;
    }

    public static final RenderProperty[] dstBlends = new RenderProperty[]{
            new RenderProperty("NONE", -1),
            new RenderProperty("GL_ZERO", GL.GL_ZERO),
            new RenderProperty("GL_ONE", GL.GL_ONE),
            new RenderProperty("GL_SRC_COLOR", GL.GL_SRC_COLOR),
            new RenderProperty("GL_ONE_MINUS_SRC_COLOR", GL.GL_ONE_MINUS_SRC_COLOR),
            new RenderProperty("GL_SRC_ALPHA", GL.GL_SRC_ALPHA),
            new RenderProperty("GL_ONE_MINUS_SRC_ALPHA", GL.GL_ONE_MINUS_SRC_ALPHA),
            new RenderProperty("GL_DST_ALPHA", GL.GL_DST_ALPHA),
            new RenderProperty("GL_ONE_MINUS_DST_ALPHA", GL.GL_ONE_MINUS_DST_ALPHA),
    };

    public int getDstBlend() {
        return dstBlends[blendDst].value;
    }

    public int getDstBlendIndex() {
        return blendDst;
    }

    public void setDstBlend(int index) {
        if (index >= 0 && index < dstBlends.length)
            blendDst = index;
    }


    // Only used when agent styles are loading
    public int priority = Integer.MAX_VALUE;


    /**
     * Returns the tile manager for this agent style
     *
     * @return null if no tile manager is defined
     */
    public TileManager getTileManager() {
        if (tileManager != null)
            return tileManager.getTileManager();

        if (tileFile != null) {
            try {
                tileManager = TileManagerInfo.loadFromXml(tileFile);
            } catch (Exception e) {
                tileFile = null;
                logger.log(Level.SEVERE, "File loading problem: " + tileFile, e);
                return null;
            }

            return tileManager.getTileManager();
        }

        return null;
    }


    /**
     * Sets a tile manager
     */
    public void setTileManager(File file) {
        this.tileFile = file;
        // Invalidate the existing tile manager
        this.tileManager = null;
    }


    public int compareTo(AgentStyle o) {
        return priority - o.priority;
    }


    /**
     * Creates an xml node for this agent style
     *
     * @param doc
     * @param position
     * @return
     */
    public Node createNode(Document doc, int position, File modelPath) {
        Node agentNode = doc.createElement("agentstyle");

        addAttr(doc, agentNode, "name", name);
        addAttr(doc, agentNode, ATTR_VISIBLE, visible);
        addAttr(doc, agentNode, ATTR_TRANSPARENT, transparent);
        addAttr(doc, agentNode, ATTR_BORDER, border);
        addAttr(doc, agentNode, ATTR_LABEL, label);
        addAttr(doc, agentNode, ATTR_PRIORITY, position);

        // Parameters
        addAttr(doc, agentNode, ATTR_SCALE, scaleFactor);

        // Flags
        addAttr(doc, agentNode, ATTR_COLOR_BLENDING, colorBlending);
        addAttr(doc, agentNode, ATTR_DRAW_SHAPE, drawShapeWithImage);
        addAttr(doc, agentNode, ATTR_MODULATE_SIZE, modulateSize);

        // Save label options
        if (fontFamily != null) {
            addAttr(doc, agentNode, ATTR_FONT_FAMILY, fontFamily);
            addAttr(doc, agentNode, ATTR_FONT_SIZE, fontSize);
            addAttr(doc, agentNode, ATTR_FONT_STYLE, fontStyle);
        }

        // Bitmap font
        if (bitmapFontName != null) {
            addAttr(doc, agentNode, ATTR_BITMAP_FONT_NAME, bitmapFontName);
        }

        addAttr(doc, agentNode, ATTR_BITMAP_FONT_SIZE, bitmapFontSize);
        addAttr(doc, agentNode, ATTR_TEXT_ALIGNMENT, textAlignment.toString());

        // Label properties
        addAttr(doc, agentNode, ATTR_DX_LABEL, dxLabel);
        addAttr(doc, agentNode, ATTR_DY_LABEL, dyLabel);
        addAttr(doc, agentNode, ATTR_LABEL_WIDTH, labelWidth);
        addAttr(doc, agentNode, ATTR_LABEL_HEIGHT, labelHeight);
        addAttr(doc, agentNode, ATTR_LABEL_COLOR, labelColor);
        addAttr(doc, agentNode, ATTR_MODULATE_LABEL_COLOR, modulateLabelColor);
        addAttr(doc, agentNode, ATTR_MODULATE_LABEL_SIZE, modulateLabelSize);

        if (this.tileFile != null) {
            // Tile file
            addAttr(doc, agentNode, ATTR_TILE_MANAGER,
                    FileUtils.getRelativePath(modelPath, tileFile));
        }

        // Transparency
        addAttr(doc, agentNode, ATTR_TRANSPARENCY, transparencyCoefficient);

        // Alpha
        addAttr(doc, agentNode, ATTR_ALPHA_FUNC, alphaFunc);
        addAttr(doc, agentNode, ATTR_ALPHA_FUNC_VALUE, alphaFuncValue);

        // Stencil
        addAttr(doc, agentNode, ATTR_STENCIL_FUNC, stencilFunc);
        addAttr(doc, agentNode, ATTR_STENCIL_REF, stencilRef);
        addAttr(doc, agentNode, ATTR_STENCIL_MASK, stencilMask);
        if (stencilFail > 0)
            addAttr(doc, agentNode, ATTR_STENCIL_FAIL, stencilFail);
        if (stencilZFail > 0)
            addAttr(doc, agentNode, ATTR_STENCIL_ZFAIL, stencilZFail);
        if (stencilZPass > 0)
            addAttr(doc, agentNode, ATTR_STENCIL_ZPASS, stencilZPass);

        // Blending
        if (blendDst > 0) {
            addAttr(doc, agentNode, ATTR_BLEND_DST, blendDst);
        }

        if (blendSrc > 0) {
            addAttr(doc, agentNode, ATTR_BLEND_SRC, blendSrc);
        }

        addAttr(doc, agentNode, ATTR_TEXTURE_ENV, textureEnv);

        return agentNode;
    }


    /**
     * Loads the agent style configuration from an xml file
     *
     * @param node
     * @param modelPath path to the model xml file
     */
    public void load(Node node, File modelPath) {
        visible = getBooleanValue(node, ATTR_VISIBLE, true);
        transparent = getBooleanValue(node, ATTR_TRANSPARENT, false);
        border = getBooleanValue(node, ATTR_BORDER, true);
        label = getBooleanValue(node, ATTR_LABEL, false);
        priority = getIntegerValue(node, ATTR_PRIORITY, 0);

        // Blending
        textureEnv = getIntegerValue(node, ATTR_TEXTURE_ENV, 0);
        blendSrc = getIntegerValue(node, ATTR_BLEND_SRC, 0);
        blendDst = getIntegerValue(node, ATTR_BLEND_DST, 0);

        // Transparency
        transparencyCoefficient = getFloatValue(node, ATTR_TRANSPARENCY, 0.5f);

        // Alpha
        alphaFunc = getIntegerValue(node, ATTR_ALPHA_FUNC, 4);
        if (alphaFunc < 0 || alphaFunc >= alphaFuncs.length)
            alphaFunc = 4;

        alphaFuncValue = getFloatValue(node, ATTR_ALPHA_FUNC_VALUE, 0.0f);

        // Stencil
        stencilFunc = getIntegerValue(node, ATTR_STENCIL_FUNC, 0);
        if (stencilFunc < 0 || stencilFunc >= stencilFuncs.length)
            stencilFunc = 0;
        stencilRef = getIntegerValue(node, ATTR_STENCIL_REF, 0);
        stencilMask = getIntegerValue(node, ATTR_STENCIL_MASK, 0xFFFF);

        stencilFail = getIntegerValue(node, ATTR_STENCIL_FAIL, 0);
        stencilZFail = getIntegerValue(node, ATTR_STENCIL_ZFAIL, 0);
        stencilZPass = getIntegerValue(node, ATTR_STENCIL_ZPASS, 0);

        // Parameters
        scaleFactor = getFloatValue(node, ATTR_SCALE, 1.0f);

        // Flags
        modulateSize = getBooleanValue(node, ATTR_MODULATE_SIZE, true);
        colorBlending = getBooleanValue(node, ATTR_COLOR_BLENDING, false);
        drawShapeWithImage = getBooleanValue(node, ATTR_DRAW_SHAPE, false);

        // Load label options
        fontFamily = getValue(node, ATTR_FONT_FAMILY, null);
        fontSize = getIntegerValue(node, ATTR_FONT_SIZE, 10);
        fontStyle = getIntegerValue(node, ATTR_FONT_STYLE, 0);
        dxLabel = getFloatValue(node, ATTR_DX_LABEL, 0.0f);
        dyLabel = getFloatValue(node, ATTR_DY_LABEL, 0.0f);
        labelWidth = getFloatValue(node, ATTR_LABEL_WIDTH, 100.0f);
        labelHeight = getFloatValue(node, ATTR_LABEL_HEIGHT, 100.0f);
        labelColor = getVector4dValue(node, ATTR_LABEL_COLOR, ";", Vector4d.BLACK);
        modulateLabelColor = getBooleanValue(node, ATTR_MODULATE_LABEL_COLOR, false);
        modulateLabelSize = getBooleanValue(node, ATTR_MODULATE_LABEL_SIZE, false);

        // Bitmap font options
        bitmapFontName = getValue(node, ATTR_BITMAP_FONT_NAME, null);
        bitmapFontSize = getFloatValue(node, ATTR_BITMAP_FONT_SIZE, 1.0f);
        textAlignment = BitmapFont.Align.valueOf(getValue(node, ATTR_TEXT_ALIGNMENT, BitmapFont.Align.Left.toString()));

        // Tile manager
        String tileFileName = getValue(node, ATTR_TILE_MANAGER, null);

        if (tileFileName != null) {
            tileFile = new File(tileFileName);
            if (!tileFile.exists()) {
                // Try relative path
                tileFile = new File(modelPath, tileFileName);
                if (!tileFile.exists())
                    tileFile = null;
            }
        }
    }
}
