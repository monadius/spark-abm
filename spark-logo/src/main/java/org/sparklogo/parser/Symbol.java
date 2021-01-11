package org.sparklogo.parser;

public class Symbol {
	public String	name;
	public int		id;
	public Object	value;

	public int 	line;
	public int	column;
	public String fileName;
	
	
	public static Symbol createIdentifier(String value) {
		return new Symbol("IDENTIFIER", sym.IDENTIFIER, value, -1, -1);
	}
	
	
	public Symbol(String name, int id, Object value, int line, int column, String fname) {
		this.name = name;
		this.id = id;
		this.value = value;
		
		this.line = line;
		this.column = column;
		this.fileName = fname;
	}

	
	public Symbol(String name, int id, Object value, int line, int column) {
		this(name, id, value, line, column, null);
	}
	
	
	public Symbol(String name, int id) {
		this(name, id, null, -1, -1);
	}
	
	
	public Symbol(String name, int id, int line, int column) {
		this(name, id, null, line, column);
	}
	

	/**
	 * Returns a string value (if any) of the symbol
	 * @return
	 */
	public String stringValue() {
		if (value instanceof String)
			return (String) value;
		
		return null;
	}
	
	
	/**
	 * Returns a double value of the symbol or 0.0 if the symbol is not a number
	 */
	public double doubleValue() {
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		
		return 0.0;
	}

	/**
	 * Returns a float value of the symbol or 0.0f if the symbol is not a number
	 */
	public float floatValue() {
		if (value instanceof Number)
			return ((Number) value).floatValue();
		
		return 0.0f;
	}
	
	/**
	 * Returns an integer value of the symbol or 0 if the symbol is not a number
	 */
	public int intValue() {
		if (value instanceof Number)
			return ((Number) value).intValue();
		
		return 0;
	}
	
	/**
	 * Returns a long value of the symbol or 0l if the symbol is not a number
	 */
	public long longValue() {
		if (value instanceof Number)
			return ((Number) value).longValue();
		
		return 0l;
	}


	
	@Override
	public String toString() {
		String at = " (at line = " + (line + 1) + "; column = " + (column + 1);
		if (fileName != null)
			at += " in " + fileName;
		at += ")";

		if (value != null)
			return name + ": " + value.toString() + at;
		else
			return name + at;
	}
}
