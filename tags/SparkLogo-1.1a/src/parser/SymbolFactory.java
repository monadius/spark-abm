package parser;

public class SymbolFactory {
	private String currentFileName;
	
	
	public void setCurrentFileName(String fname) {
		currentFileName = fname;
	}
	
	
	public Symbol newSymbol(String name, int id, int line, int column) {
		return new Symbol(name, id, null, line, column, currentFileName);
	}
	
	
	public Symbol newSymbol(String name, int id, Object val, int line, int column) {
		return new Symbol(name, id, val, line, column, currentFileName);
	}
	
	
}
