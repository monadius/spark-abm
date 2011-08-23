package org.spark.runtime.external.render;

import java.awt.Font;
import java.io.File;

import javax.media.opengl.GL;

import org.spark.math.Vector;
import org.spark.runtime.external.render.images.TileManager;
import org.spark.runtime.external.render.images.TileManagerInfo;
import org.spark.utils.FileUtils;
import static org.spark.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.spinn3r.log5j.Logger;

public class AgentStyle implements Comparable<AgentStyle> {
	private static final Logger logger = Logger.getLogger();
	
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
	
	/* Label options */
	private Font font;
	// null means a default font
	private String fontFamily;
	private int fontSize;
	private int fontStyle;
	private float dxLabel, dyLabel;
	private Vector labelColor = new Vector();
	
	
	/* TileManager */
	private File tileFile;
	private TileManagerInfo tileManager;
	
	
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
	
	public static final RenderProperty[] textureEnvs = new RenderProperty[] {
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
	
	
	public static final RenderProperty[] alphaFuncs = new RenderProperty[] {
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

	public static final RenderProperty[] stencilFuncs = new RenderProperty[] {
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
	
	public static final RenderProperty[] stencilOps = new RenderProperty[] {
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
	 * Returns a font for printing labels
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
	 * @param dx
	 * @param dy
	 */
	public void setLabelOffset(float dx, float dy) {
		this.dxLabel = dx;
		this.dyLabel = dy;
	}
	
	
	/**
	 * Returns the color of a label
	 */
	public Vector getLabelColor() {
		return new Vector(labelColor);
	}
	
	
	/**
	 * Sets the color of a label
	 */
	public void setLabelColor(Vector c) {
		this.labelColor = new Vector(c);
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
	

	public static final RenderProperty[] srcBlends = new RenderProperty[] {
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
	
	public static final RenderProperty[] dstBlends = new RenderProperty[] {
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

	public AgentStyle(String agentTypeName, String name,
			boolean transparent, boolean visible, boolean border) {
		this.typeName = agentTypeName;
		this.name = name;
		this.transparent = transparent;
		this.visible = visible;
		this.border = border;
		
		// Default values of advanced parameters
		textureEnv = 0;
		
		// 0 means that a feature is disabled
		blendSrc = blendDst = 0;
		alphaFunc = 0;
		alphaFuncValue = 0.0f;
	}
	
	
	public AgentStyle(String agentTypeName) {
		this(agentTypeName, null, false, true, true);
	}

	
	/**
	 * Returns the tile manager for this agent style
	 * @return null if no tile manager is defined
	 */
	public TileManager getTileManager() {
		if (tileManager != null)
			return tileManager.getTileManager();
		
		if (tileFile != null) {
			try {
				tileManager = TileManagerInfo.loadFromXml(tileFile);
			}
			catch (Exception e) {
				tileFile = null;
				logger.error("File loading problem: " + tileFile);
				logger.error(e);
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
	 * @param doc
	 * @param position
	 * @return
	 */
	public Node createNode(Document doc, int position, File modelPath) {
		Node agentNode = doc.createElement("agentstyle");

		addAttr(doc, agentNode, "name", name);
		addAttr(doc, agentNode, "visible", visible);
		addAttr(doc, agentNode, "transparent", transparent);
		addAttr(doc, agentNode, "border", border);
		addAttr(doc, agentNode, "label", label);
		addAttr(doc, agentNode, "position", position);

		addAttr(doc, agentNode, "color-blending", colorBlending);
		addAttr(doc, agentNode, "draw-shape-with-image", drawShapeWithImage);
		
		// Save label options
		if (fontFamily != null) {
			addAttr(doc, agentNode, "font-family", fontFamily);
			addAttr(doc, agentNode, "font-size", fontSize);
			addAttr(doc, agentNode, "font-style", fontStyle);
		}
		
		addAttr(doc, agentNode, "dx-label", dxLabel);
		addAttr(doc, agentNode, "dy-label", dyLabel);
		addAttr(doc, agentNode, "label-color", labelColor);
		
		if (this.tileFile != null) {
			// Tile file
			addAttr(doc, agentNode, "tile-manager", 
					FileUtils.getRelativePath(modelPath, tileFile));
		}

		// Transparency
		addAttr(doc, agentNode, "transparency-coefficient", transparencyCoefficient);
		
		// Alpha
		addAttr(doc, agentNode, "alpha-function", alphaFunc);
		addAttr(doc, agentNode, "alpha-function-value", alphaFuncValue);
		
		// Stencil
		addAttr(doc, agentNode, "stencil-function", stencilFunc);
		addAttr(doc, agentNode, "stencil-ref", stencilRef);
		addAttr(doc, agentNode, "stencil-mask", stencilMask);
		if (stencilFail > 0)
			addAttr(doc, agentNode, "stencil-fail", stencilFail);
		if (stencilZFail > 0)
			addAttr(doc, agentNode, "stencil-zfail", stencilZFail);
		if (stencilZPass > 0)
			addAttr(doc, agentNode, "stencil-zpass", stencilZPass);
		
		// Blending
		if (blendDst > 0) {
			addAttr(doc, agentNode, "blend-dst", blendDst);
		}
		
		if (blendSrc > 0) {
			addAttr(doc, agentNode, "blend-src", blendSrc);
		}
		
		addAttr(doc, agentNode, "texture-env", textureEnv);

		return agentNode;
	}
	
	
	/**
	 * Loads the agent style configuration from an xml file
	 * @param node
	 * @param modelPath path to the model xml file
	 */
	public void load(Node node, File modelPath) {
		visible = getBooleanValue(node, "visible", true);
		transparent = getBooleanValue(node, "transparent", false);
		border = getBooleanValue(node, "border", true);
		label = getBooleanValue(node, "label", false);
		priority = getIntegerValue(node, "position", 0);
		
		// Blending
		textureEnv = getIntegerValue(node, "texture-env", 0);
		blendSrc = getIntegerValue(node, "blend-src", 0);
		blendDst = getIntegerValue(node, "blend-dst", 0);
		
		// Transparency
		transparencyCoefficient = getFloatValue(node, "transparency-coefficient", 0.5f);
		
		// Alpha
		alphaFunc = getIntegerValue(node, "alpha-function", 4);
		if (alphaFunc < 0 || alphaFunc >= alphaFuncs.length)
			alphaFunc = 4;
		
		alphaFuncValue = getFloatValue(node, "alpha-function-value", 0.0f);

		// Stencil
		stencilFunc = getIntegerValue(node, "stencil-function", 0);
		if (stencilFunc < 0 || stencilFunc >= stencilFuncs.length)
			stencilFunc = 0;
		stencilRef = getIntegerValue(node, "stencil-ref", 0);
		stencilMask = getIntegerValue(node, "stencil-mask", 0xFFFF);
		
		stencilFail = getIntegerValue(node, "stencil-fail", 0);
		stencilZFail = getIntegerValue(node, "stencil-zfail", 0);
		stencilZPass = getIntegerValue(node, "stencil-zpass", 0);
		
		// Flags
		colorBlending = getBooleanValue(node, "color-blending", false);
		drawShapeWithImage = getBooleanValue(node, "draw-shape-with-image", false);

		// Load label options
		fontFamily = getValue(node, "font-family", null);
		fontSize = getIntegerValue(node, "font-size", 10);
		fontStyle = getIntegerValue(node, "font-style", 0);
		dxLabel = getFloatValue(node, "dx-label", 0.0f);
		dyLabel = getFloatValue(node, "dy-label", 0.0f);
		labelColor = getVectorValue(node, "label-color", ";", new Vector());
		
		String tileFileName = getValue(node, "tile-manager", null);
		
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
