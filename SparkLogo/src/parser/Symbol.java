package parser;

public class Symbol {
	public String	name;
	public int		id;
	public Object	value;

	public int 	line;
	public int	column;
	public String fileName;
	
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
