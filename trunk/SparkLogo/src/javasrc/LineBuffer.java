package javasrc;

import java.util.ArrayList;

/**
 * Buffer for storing java source lines
 * @author Monad
 *
 */
public class LineBuffer {
	/* Lines in this buffer */
	protected ArrayList<StringBuilder> lines;
	/* Common indent of all lines */
	protected int indent;
	/* Current active line */
	private StringBuilder currentLine;

	
	/**
	 * Creates an empty buffer
	 */
	public LineBuffer(int indent) {
		this.lines = new ArrayList<StringBuilder>(100);
		this.indent = indent;
	}
	
	
	/**
	 * Initializes the current line
	 */
	private void initCurrentLine() {
		if (currentLine == null) {
			currentLine = new StringBuilder();
			lines.add(currentLine);
		}
	}
	
	
	/**
	 * Prints out a separate line
	 * @param str
	 */
	public void println(String str) {
		currentLine = null;
		initCurrentLine();
		currentLine.append(str);
		currentLine = null;
	}
	
	
	/**
	 * Prints out an empty line;
	 */
	public void println() {
		println("");
	}
	
	
	/**
	 * Ends a line
	 */
	public void endLine() {
		currentLine = null;
	}
	
	
	/**
	 * Ends a line and puts ; if necessary
	 */
	public void endJavaLine() {
		if (currentLine != null && currentLine.length() > 0) {
			if (currentLine.charAt(currentLine.length() - 1) != '}')
				currentLine.append(';');
		}
		
		currentLine = null;
	}
	
	
	/**
	 * Appends a string to the current line
	 * @param str
	 */
	public void print(String str) {
		initCurrentLine();
		currentLine.append(str);
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for (int i = 0; i < lines.size(); i++) {
			str.append(lines.get(i));
			if (i != lines.size() - 1)
				str.append('\n');
		}
		
		return str.toString();
	}
}
