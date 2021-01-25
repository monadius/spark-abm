/*
Based on
Title : BitmapFont.cs
Author : Chad Vernon
URL : http://www.c-unit.com

Description : Bitmap font wrapper based on the Angelcode bitmap font generator.
http://www.angelcode.com/products/bmfont/
*/

package org.sparkabm.gui.renderer.font;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import org.sparkabm.math.Vector4d;
import org.sparkabm.gui.renderer.font.BitmapCharacter.Kerning;

/**
 * Describes a bitmap font
 */
public class BitmapFont {
    // Log
    private static final Logger logger = Logger.getLogger(BitmapFont.class.getName());

    // Font alignments
    public enum Align {
        Left, Center, Right
    }

    private final BitmapCharacterSet m_charSet;
    private final ArrayList<FontQuad> m_quads;
    private final ArrayList<StringBlock> m_strings;
    private final File m_fntFile;
    private File m_textureFile;
    private Texture m_texture = null;


    /**
     * Constructor
     */
    public BitmapFont(File fntFile) throws Exception {
        m_quads = new ArrayList<>();
        m_strings = new ArrayList<>();
        m_fntFile = fntFile;
        m_charSet = new BitmapCharacterSet();
        ParseFNTFile();

        if (m_textureFile == null)
            throw new Exception("No texture file");
    }

    /**
     * Constructor
     */
    public BitmapFont(String fntFile) throws Exception {
        this(new File(fntFile));
    }

    /**
     * Parses the FNT file
     */
    private void ParseFNTFile() throws Exception {
        BufferedReader stream = new BufferedReader(new FileReader(m_fntFile));

        while (true) {
            // Read the next line
            String line = stream.readLine();
            if (line == null)
                break;

            // Split the line into tokens
            String[] tokens = line.split("[ =]");
            if (tokens.length == 0)
                continue;

            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].trim().intern();
            }

            // Get the head token
            String head = tokens[0];

            // Info
            if (head.equals("info")) {
                for (int i = 1; i < tokens.length; i++) {
                    String t = tokens[i];

                    if (t.equals("size")) {
                        m_charSet.RenderedSize = Integer.parseInt(tokens[i + 1]);
                    }
                }

                continue;
            }

            // Common
            if (head.equals("common")) {
                for (int i = 1; i < tokens.length; i++) {
                    String t = tokens[i];

                    switch (t) {
                        case "lineHeight":
                            m_charSet.LineHeight = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "base":
                            m_charSet.Base = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "scaleW":
                            m_charSet.Width = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "scaleH":
                            m_charSet.Height = Integer.parseInt(tokens[i + 1]);
                            break;
                    }
                }

                continue;
            }

            // Page
            if (head.equals("page")) {
                for (int i = 1; i < tokens.length; i++) {
                    String t = tokens[i];

                    if (t.equals("id")) {
                        int index = Integer.parseInt(tokens[i + 1]);
                        if (index != 0)
                            throw new Exception("Page 0 is supported only");
                    } else if (t.equals("file")) {
                        String fname = tokens[i + 1];
                        int len = fname.length();
                        // Remove quotes
                        if (fname.charAt(0) == '"')
                            fname = fname.substring(1, len - 1);

                        m_textureFile = new File(m_fntFile.getParentFile(), fname);
                    }
                }

                continue;
            }

            // Char
            if (head.equals("char")) {
                // New BitmapCharacter
                int index = 0;
                for (int i = 1; i < tokens.length; i++) {
                    String t = tokens[i];

                    switch (t) {
                        case "id":
                            index = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "x":
                            m_charSet.Characters[index].X = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "y":
                            m_charSet.Characters[index].Y = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "width":
                            m_charSet.Characters[index].Width = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "height":
                            m_charSet.Characters[index].Height = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "xoffset":
                            m_charSet.Characters[index].XOffset = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "yoffset":
                            m_charSet.Characters[index].YOffset = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                        case "xadvance":
                            m_charSet.Characters[index].XAdvance = Integer
                                    .parseInt(tokens[i + 1]);
                            break;
                    }
                }

                continue;
            }

            if (head.equals("kerning")) {
                // Build the kerning list
                int index = 0;
                BitmapCharacter.Kerning k = new BitmapCharacter.Kerning();
                for (int i = 1; i < tokens.length; i++) {
                    String t = tokens[i];

                    switch (t) {
                        case "first":
                            index = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "second":
                            k.Second = Integer.parseInt(tokens[i + 1]);
                            break;
                        case "amount":
                            k.Amount = Integer.parseInt(tokens[i + 1]);
                            break;
                    }
                }

                m_charSet.Characters[index].KerningList.add(k);
                continue;
            }
        }

        stream.close();
    }

    // / <summary>Call when the device is created.</summary>
    // / <param name="device">D3D device.</param>
    public void init(GL2 gl) throws Exception {
        m_texture = TextureIO.newTexture(m_textureFile, false);
        m_texture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        m_texture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
    }

    // / <summary>Call when the device is destroyed.</summary>
    public void dispose(GL2 gl) {
        if (m_texture != null) {
            m_texture.destroy(gl);
        }
    }

    // / <summary>Adds a new string to the list to render.</summary>
    // / <param name="text">Text to render</param>
    // / <param name="textBox">Rectangle to constrain text</param>
    // / <param name="alignment">Font alignment</param>
    // / <param name="size">Font size</param>
    // / <param name="color">Color</param>
    // / <param name="kerning">true to use kerning, false otherwise.</param>
    // / <returns>The index of the added StringBlock</returns>
    public int AddString(String text, Rectangle2D.Float textBox,
                         Align alignment, float size, Vector4d color, boolean kerning) {
        StringBlock b = new StringBlock(text, textBox, alignment, size, color,
                kerning);
        m_strings.add(b);
        int index = m_strings.size() - 1;
        m_quads.addAll(GetProcessedQuads(index));
        return index;
    }

    // / <summary>Removes a string from the list of strings.</summary>
    // / <param name="i">Index to remove</param>
    public void ClearString(int i) {
        m_strings.remove(i);
    }

    // / <summary>Clears the list of strings</summary>
    public void ClearStrings() {
        m_strings.clear();
        m_quads.clear();
    }


    public void testRender(char ch, GL2 gl) {
        BitmapCharacter c = m_charSet.Characters[ch];
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        m_texture.bind(gl);

        float u0 = c.X / (float) m_charSet.Width;
        float v0 = c.Y / (float) m_charSet.Height;
        float u1 = (c.X + c.Width) / (float) m_charSet.Width;
        float v1 = (c.Y + c.Height) / (float) m_charSet.Height;

        gl.glBegin(GL2.GL_TRIANGLES);
        // 0
        gl.glTexCoord2f(u0, v0);
        gl.glVertex2f(-1, 1);

        // 1
        gl.glTexCoord2f(u1, v1);
        gl.glVertex2f(1, -1);

        // 2
        gl.glTexCoord2f(u0, v1);
        gl.glVertex2f(-1, -1);

        // 3
        gl.glTexCoord2f(u0, v0);
        gl.glVertex2f(-1, 1);

        // 4
        gl.glTexCoord2f(u1, v0);
        gl.glVertex2f(1, 1);

        // 5
        gl.glTexCoord2f(u1, v1);
        gl.glVertex2f(1, -1);
        gl.glEnd();

        gl.glDisable(GL2.GL_TEXTURE_2D);
    }


    // / <summary>Renders the strings.</summary>
    // / <param name="device">D3D Device</param>
    public void Render(GL2 gl) {
        if (m_texture == null) {
            try {
                init(gl);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "exception", e);
                return;
            }
        }

        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        m_texture.bind(gl);

        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBegin(GL2.GL_TRIANGLES);
        for (FontQuad quad : m_quads) {
            Vector4d color = quad.m_vertices[0].ColorValue;
            gl.glColor4d(color.x, color.y, color.z, color.a);

            for (int i = 0; i < 6; i++) {
                TransformedColoredTextured v = quad.m_vertices[i];
                gl.glTexCoord2f(v.u, v.v);
                gl.glVertex2f(v.X, v.Y);
            }
        }
        gl.glEnd();

        gl.glDisable(GL2.GL_BLEND);
        gl.glDisable(GL2.GL_TEXTURE_2D);


        /*
         * if ( m_strings.Count <= 0 ) { return; }
         *
         * // Add vertices to the buffer
         * GraphicsBuffer<TransformedColoredTextured> gb =
         * m_vb.Lock<TransformedColoredTextured>( 0, 6 * m_quads.Count,
         * LockFlags.Discard );
         *
         * foreach ( FontQuad q in m_quads ) { gb.Write( q.Vertices ); }
         *
         * m_vb.Unlock();
         *
         * // Set render states device.SetRenderState( RenderStates.ZEnable,
         * false ); device.SetRenderState( RenderStates.FillMode,
         * (int)FillMode.Solid ); device.SetRenderState(
         * RenderStates.ZBufferWriteEnable, false ); device.SetRenderState(
         * RenderStates.FogEnable, false ); device.SetRenderState(
         * RenderStates.AlphaTestEnable, false ); device.SetRenderState(
         * RenderStates.AlphaBlendEnable, true ); device.SetRenderState(
         * RenderStates.SourceBlend, (int)Blend.SourceAlpha );
         * device.SetRenderState( RenderStates.DestinationBlend,
         * (int)Blend.InvSourceAlpha );
         *
         * // Blend Texture and Vertex alphas device.SetTextureState( 0,
         * TextureStates.ColorArgument1, (int)TextureArgument.Current );
         * device.SetTextureState( 0, TextureStates.AlphaArgument1,
         * (int)TextureArgument.Texture ); device.SetTextureState( 0,
         * TextureStates.AlphaArgument2, (int)TextureArgument.Diffuse );
         * device.SetTextureState( 0, TextureStates.AlphaOperation,
         * (int)TextureOperation.Modulate );
         *
         * // Set sampler states device.SetSamplerState( 0,
         * SamplerStates.MinFilter, (int)Filter.Linear );
         * device.SetSamplerState( 0, SamplerStates.MagFilter,
         * (int)Filter.Linear ); device.SetSamplerState( 0,
         * SamplerStates.MipFilter, (int)Filter.Linear );
         *
         * // Render device.VertexFormat = TransformedColoredTextured.Format;
         * device.SetTexture( 0, m_texture ); device.SetStreamSource( 0, m_vb,
         * 0, TransformedColoredTextured.StrideSize ); device.DrawPrimitives(
         * PrimitiveType.TriangleList, 0, 2 * m_quads.Count );
         */
    }

    // / <summary>Gets the list of Quads from a StringBlock all ready to
    // render.</summary>
    // / <param name="index">Index into StringBlock List</param>
    // / <returns>List of Quads</returns>
    public ArrayList<FontQuad> GetProcessedQuads(int index) {
        if (index >= m_strings.size() || index < 0) {
            throw new RuntimeException("String block index out of range.");
        }

        ArrayList<FontQuad> quads = new ArrayList<>();
        StringBlock b = m_strings.get(index);
        String text = b.Text;
        float x = b.TextBox.x;
        float y = b.TextBox.y;
        float maxWidth = b.TextBox.width;
        Align alignment = b.Alignment;
        float lineWidth = 0f;
        float sizeScale = b.Size / (float) m_charSet.RenderedSize;
        char lastChar = 0;
        int lineNumber = 1;
        int wordNumber = 1;
        float wordWidth = 0f;
        boolean firstCharOfLine = true;

        float z = 0f;
        float rhw = 1f;

        for (int i = 0; i < text.length(); i++) {
            BitmapCharacter c = m_charSet.Characters[text.charAt(i)];
            float xOffset = c.XOffset * sizeScale;
            float yOffset = c.YOffset * sizeScale;
            float xAdvance = c.XAdvance * sizeScale;
            float width = c.Width * sizeScale;
            float height = c.Height * sizeScale;

            // Check vertical bounds
            if (y + yOffset + height > b.TextBox.y + b.TextBox.height) {
                break;
            }

            // Newline
            // private VertexBuffer m_vb = null;
            //	private final static int MaxVertices = 4096;
            int m_nextChar;
            if (text.charAt(i) == '\n' || text.charAt(i) == '\r'
                    || (lineWidth + xAdvance >= maxWidth)) {
                if (alignment == Align.Left) {
                    // Start at left
                    x = b.TextBox.x;
                }
                if (alignment == Align.Center) {
                    // Start in center
                    x = b.TextBox.x + (maxWidth / 2f);
                } else if (alignment == Align.Right) {
                    // Start at right
                    x = b.TextBox.x + b.TextBox.width;
                }

                y += m_charSet.LineHeight * sizeScale;
                float offset = 0f;

                if ((lineWidth + xAdvance >= maxWidth) && (wordNumber != 1)) {
                    // Next character extends past text box width
                    // We have to move the last word down one line
                    char newLineLastChar = 0;
                    lineWidth = 0f;
                    for (FontQuad quad : quads) {
                        if (alignment == Align.Left) {
                            // Move current word to the left side of the text
                            // box
                            if ((quad.getLineNumber() == lineNumber)
                                    && (quad.getWordNumber() == wordNumber)) {
                                quad.setLineNumber(quad.getLineNumber() + 1);
                                quad.setWordNumber(1);
                                quad
                                        .setX(x
                                                + (quad.getBitmapCharacter().XOffset * sizeScale));
                                quad
                                        .setY(y
                                                + (quad.getBitmapCharacter().YOffset * sizeScale));
                                x += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                lineWidth += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                if (b.Kerning) {
                                    m_nextChar = quad.getCharacter();
                                    Kerning kern = m_charSet.Characters[newLineLastChar].FindKerningNode(m_nextChar);
                                    if (kern != null) {
                                        x += kern.Amount * sizeScale;
                                        lineWidth += kern.Amount * sizeScale;
                                    }
                                }
                            }
                        } else if (alignment == Align.Center) {
                            if ((quad.getLineNumber() == lineNumber)
                                    && (quad.getWordNumber() == wordNumber)) {
                                // First move word down to next line
                                quad.setLineNumber(quad.getLineNumber() + 1);
                                quad.setWordNumber(1);
                                quad
                                        .setX(x
                                                + (quad.getBitmapCharacter().XOffset * sizeScale));
                                quad
                                        .setY(y
                                                + (quad.getBitmapCharacter().YOffset * sizeScale));
                                x += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                lineWidth += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                offset += quad.getBitmapCharacter().XAdvance
                                        * sizeScale / 2f;
                                float kerning = 0f;
                                if (b.Kerning) {
                                    m_nextChar = quad.getCharacter();
                                    Kerning kern = m_charSet.Characters[newLineLastChar].FindKerningNode(m_nextChar);
                                    if (kern != null) {
                                        kerning = kern.Amount * sizeScale;
                                        x += kerning;
                                        lineWidth += kerning;
                                        offset += kerning / 2f;
                                    }
                                }
                            }
                        } else if (alignment == Align.Right) {
                            if ((quad.getLineNumber() == lineNumber)
                                    && (quad.getWordNumber() == wordNumber)) {
                                // Move character down to next line
                                quad.setLineNumber(quad.getLineNumber() + 1);
                                quad.setWordNumber(1);
                                quad
                                        .setX(x
                                                + (quad.getBitmapCharacter().XOffset * sizeScale));
                                quad
                                        .setY(y
                                                + (quad.getBitmapCharacter().YOffset * sizeScale));
                                lineWidth += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                x += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                offset += quad.getBitmapCharacter().XAdvance
                                        * sizeScale;
                                float kerning = 0f;
                                if (b.Kerning) {
                                    m_nextChar = quad.getCharacter();
                                    Kerning kern = m_charSet.Characters[newLineLastChar].FindKerningNode(m_nextChar);
                                    if (kern != null) {
                                        kerning = kern.Amount * sizeScale;
                                        x += kerning;
                                        lineWidth += kerning;
                                        offset += kerning;
                                    }
                                }
                            }
                        }
                        newLineLastChar = quad.getCharacter();
                    }

                    // Make post-newline justifications
                    if (alignment == Align.Center || alignment == Align.Right) {
                        // Justify the new line
                        for (FontQuad quad : quads) {
                            if (quad.getLineNumber() == lineNumber + 1) {
                                quad.setX(quad.getX() - offset);
                            }
                        }
                        x -= offset;

                        // Rejustify the line it was moved from
                        for (FontQuad quad : quads) {
                            if (quad.getLineNumber() == lineNumber) {
                                quad.setX(quad.getX() + offset);
                            }
                        }
                    }
                } else {
                    // New line without any "carry-down" word
                    firstCharOfLine = true;
                    lineWidth = 0f;
                }

                wordNumber = 1;
                lineNumber++;

            } // End new line check

            // Don't print these
            if (text.charAt(i) == '\n' || text.charAt(i) == '\r'
                    || text.charAt(i) == '\t') {
                continue;
            }

            // Set starting cursor for alignment
            if (firstCharOfLine) {
                if (alignment == Align.Left) {
                    // Start at left
                    x = b.TextBox.x;
                }
                if (alignment == Align.Center) {
                    // Start in center
                    x = b.TextBox.x + (maxWidth / 2f);
                } else if (alignment == Align.Right) {
                    // Start at right
                    x = b.TextBox.x + b.TextBox.width;
                }
            }

            // Adjust for kerning
            float kernAmount = 0f;
            if (b.Kerning && !firstCharOfLine) {
                m_nextChar = (char) text.charAt(i);
                Kerning kern = m_charSet.Characters[lastChar].FindKerningNode(m_nextChar);
                if (kern != null) {
                    kernAmount = kern.Amount * sizeScale;
                    x += kernAmount;
                    lineWidth += kernAmount;
                    wordWidth += kernAmount;
                }
            }

            firstCharOfLine = false;

            // Create the vertices
            TransformedColoredTextured topLeft = new TransformedColoredTextured(
                    x + xOffset, y + yOffset, z, rhw, b.Color,
                    (float) c.X / (float) m_charSet.Width, (float) c.Y
                    / (float) m_charSet.Height);
            TransformedColoredTextured topRight = new TransformedColoredTextured(
                    topLeft.X + width, y + yOffset, z, rhw, b.Color,
                    (float) (c.X + c.Width) / (float) m_charSet.Width,
                    (float) c.Y / (float) m_charSet.Height);
            TransformedColoredTextured bottomRight = new TransformedColoredTextured(
                    topLeft.X + width, topLeft.Y + height, z, rhw, b.Color,
                    (float) (c.X + c.Width) / (float) m_charSet.Width, (float) (c.Y + c.Height)
                    / (float) m_charSet.Height);
            TransformedColoredTextured bottomLeft = new TransformedColoredTextured(
                    x + xOffset, topLeft.Y + height, z, rhw, b.Color,
                    (float) c.X / (float) m_charSet.Width,
                    (float) (c.Y + c.Height) / (float) m_charSet.Height);

            // Create the quad
            FontQuad q = new FontQuad(topLeft, topRight, bottomLeft,
                    bottomRight);
            q.setLineNumber(lineNumber);
            if (text.charAt(i) == ' ' && alignment == Align.Right) {
                wordNumber++;
                wordWidth = 0f;
            }
            q.setWordNumber(wordNumber);
            wordWidth += xAdvance;
            q.setWordWidth(wordWidth);
            q.setBitmapCharacter(c);
            q.setSizeScale(sizeScale);
            q.setCharacter(text.charAt(i));
            quads.add(q);

            if (text.charAt(i) == ' ' && alignment == Align.Left) {
                wordNumber++;
                wordWidth = 0f;
            }

            x += xAdvance;
            lineWidth += xAdvance;
            lastChar = text.charAt(i);

            // Rejustify text
            if (alignment == Align.Center) {
                // We have to recenter all Quads since we addded a
                // new character
                float offset = xAdvance / 2f;
                if (b.Kerning) {
                    offset += kernAmount / 2f;
                }
                for (FontQuad quad : quads) {
                    if (quad.getLineNumber() == lineNumber) {
                        quad.setX(quad.getX() - offset);
                    }
                }
                x -= offset;
            } else if (alignment == Align.Right) {
                // We have to rejustify all Quads since we addded a
                // new character
                float offset = 0f;
                if (b.Kerning) {
                    offset += kernAmount;
                }
                for (FontQuad quad : quads) {
                    if (quad.getLineNumber() == lineNumber) {
                        offset = xAdvance;
                        quad.setX(quad.getX() - xAdvance);
                    }
                }
                x -= offset;
            }
        }
        return quads;
    }

    // / <summary>Gets the line height of a StringBlock.</summary>
    public float GetLineHeight(int index) {
        if (index < 0 || index > m_strings.size()) {
            throw new RuntimeException("StringBlock index out of range.");
        }
        return m_charSet.LineHeight
                * (m_strings.get(index).Size / m_charSet.RenderedSize);
    }


    // / <summary>Gets the font texture.</summary>
    public Texture getTexture() {
        return m_texture;
    }
}
