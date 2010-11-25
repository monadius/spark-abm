package org.spark.runtime.external.render;

import java.awt.Image;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import org.spark.utils.FileUtils;
import static org.spark.utils.XmlDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class AgentStyle implements Comparable<AgentStyle> {
	/* Agent's type name */
	public String typeName;
	
	/* Agent's (short) name */
	public String name;
	
	/* Basic options */
	
	public boolean transparent = false;
	public boolean visible = true;
	public boolean border = true;
	
	/* Advanced options */
	
	String textureFileName;
	InputStream textureStream;
	
	private Texture texture;
	private Image image;
	
	private int blendSrc, blendDst;
	private int alphaFunc;
	public float alphaFuncValue;
	private int textureEnv;
	
	
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
		this.textureFileName = null;
		
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

	
	public String getTextureFileName() {
		return textureFileName;
	}
	
	
	public File getTextureFile() {
		if (textureFileName == null)
			return null;
		else
			return new File(textureFileName);
	}
	

	public Texture getTexture() {
		if (texture != null)
			return texture;

		if (textureFileName != null) {
			try {
				texture = TextureIO.newTexture(new File(textureFileName), false);
				texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,
						GL.GL_NEAREST);
				texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,
						GL.GL_NEAREST);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Error loading texture "
						+ new File(textureFileName).getAbsolutePath());
				texture = null;
			}

			return texture;
		}
		else if (textureStream != null) {
			try {
				texture = TextureIO.newTexture(textureStream, false, "png");
				texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER,
						GL.GL_NEAREST);
				texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER,
						GL.GL_NEAREST);
				textureStream.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Error loading texture from an input stream");
				texture = null;
			}

			return texture;
			
		}

		return null;

	}
	
	
	public Image getImage() {
		if (image != null)
			return image;
		
		if (textureFileName != null) {
			try {
				image = ImageIO.read(new File(textureFileName));
				return image;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public synchronized void setTexture(String textureFileName) {
		if (textureFileName == null || !textureFileName.equals(this.textureFileName)) {
			// TODO: texture can be disposed only on OpenGL thread.
			// Maybe put it into a event queue?
//			if (texture != null)
//				texture.dispose();
			
			texture = null;
			image = null;
		}

		this.textureFileName = textureFileName;
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
		addAttr(doc, agentNode, "position", position);
		
		if (this.textureFileName != null) {
			// Texture
			addAttr(doc, agentNode, "texture", 
					FileUtils.getRelativePath(modelPath,
							new File(this.textureFileName)));
		}
		
		if (alphaFunc > 0) {
			addAttr(doc, agentNode, "alpha-function", alphaFunc);
		}
		
		addAttr(doc, agentNode, "alpha-function-value", alphaFuncValue);
		
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
		priority = getIntegerValue(node, "position", 0);
		
		textureEnv = getIntegerValue(node, "texture-env", 0);
		blendSrc = getIntegerValue(node, "blend-src", 0);
		blendDst = getIntegerValue(node, "blend-dst", 0);
		
		alphaFunc = getIntegerValue(node, "alpha-function", 0);
		alphaFuncValue = getFloatValue(node, "alpha-function-value", 0.0f);
		
		String texture = getValue(node, "texture", null);

		if (texture != null) {
			// TODO: find better solution. Another argument?
			// modelPath == null means loading from the jar-file
			if (modelPath == null) {
				try {
					InputStream is = this.getClass().getClassLoader().getResourceAsStream(texture);
					if (is != null) {
						this.textureStream = is;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
			
				File textureFile = new File(texture);
			
				if (textureFile.isAbsolute())
					this.setTexture(textureFile.getAbsolutePath());
				else
					this.setTexture(new File(modelPath, texture).getAbsolutePath());
			}
		}
	}
}
