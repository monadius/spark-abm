package parser;

import java.util.*;

public class SymbolList {
	ArrayList<Symbol>	list = new ArrayList<Symbol>(100);
	int	position;
	
	public SymbolList() {
		position = 0;
	}
	
	
	public int size() {
		return list.size();
	}
	
	
	public void insert(Symbol symbol, int position) {
		list.add(position, symbol);
	}
	
	
	public void add(Symbol symbol) {
		list.add(symbol);
	}
	
	
	public Symbol get(int i) {
		return list.get(i);
	}
	
	
	public void reset() {
		position = 0;
	}
	
	
	public Symbol next() {
		if (position < list.size())
			return list.get(position++);
		else
			return new Symbol("END", sym.END);
	}
	
	
	public Symbol peek() {
		if (position < list.size())
			return list.get(position);
		else
			return new Symbol("END", sym.END);
	}
}
