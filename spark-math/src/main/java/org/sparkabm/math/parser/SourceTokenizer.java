package org.sparkabm.math.parser;

/**
 * Author: Solovyev Alexey
 * Date: 01.11.2005
 * Time: 11:08:40
 */

/**
 * Tokens
 */
final class Token {
    static final int PLUS = 1;
    static final int MINUS = 2;
    static final int MUL = 3;
    static final int DIV = 4;
    static final int POWER = 5;
    static final int LBRACKET = 10;
    static final int RBRACKET = 11;
    static final int COMMA = 20;
    static final int NUMBER = 100;
    static final int IDENTIFIER = 200;
}

/**
 * Splits the input string into tokens
 */
class SourceTokenizer {
    // Input characters
    private final char[] src;
    // Position in the input stream
    private int pos, oldPos;
    // Buffer
    private final StringBuffer val;

    /**
     * Constructor
     */
    public SourceTokenizer(String str) {
        this.src = str.toCharArray();
        this.pos = 0;
        this.oldPos = 0;
        this.val = new StringBuffer(100);
    }

    /**
     * Returns the last token into the stream
     */
    public void putBackLastToken() {
        pos = oldPos;
    }

    /**
     * Returns the value of the last token
     */
    public String getValue() {
        return val.toString();
    }

    /**
     * Reads an identifier
     */
    private int readIdentifier(char firstChar) {
        val.setLength(0);
        val.append(firstChar);

        for (; pos < src.length; pos++) {
            char ch = src[pos];
            if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '-' || ch == '$')
                val.append(ch);
            else
                break;
        }

        return Token.IDENTIFIER;
    }

    /**
     * Reads a number
     */
    private int readNumber(char firstDigit) throws Exception {
        val.setLength(0);
        val.append(firstDigit);

        int dotPos = -1;

        for (; pos < src.length; pos++) {
            char ch = src[pos];

            if (Character.isDigit(ch)) {
                val.append(ch);
            } else if (ch == '.' && dotPos == -1) {
                val.append(ch);
                dotPos = val.length();
            } else {
                break;
            }
        }

        if (dotPos == val.length())
            throw new Exception("Bad number format");

        return Token.NUMBER;
    }

    /**
     * Returns the next token
     */
    public int nextToken() throws Exception {
        // Skip white spaces
        for (; pos < src.length && Character.isWhitespace(src[pos]); pos++)
            ;

        if (pos >= src.length)
            return -1;

        oldPos = pos;
        char ch = src[pos++];

        switch (ch) {
            // +
            case '+':
                return Token.PLUS;

            // -
            case '-':
                return Token.MINUS;

            // *
            case '*':
                return Token.MUL;

            // /
            case '/':
                return Token.DIV;

            // ^
            case '^':
                return Token.POWER;

            // (
            case '(':
                return Token.LBRACKET;

            // )
            case ')':
                return Token.RBRACKET;

            // ,
            case ',':
                return Token.COMMA;
        }

        // number
        if (Character.isDigit(ch))
            return readNumber(ch);

        // identifier
        if (Character.isLetter(ch))
            return readIdentifier(ch);

        throw new Exception("Unknown token: " + ch);
    }
}
