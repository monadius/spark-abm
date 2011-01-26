package parser;

import java.util.*;

public class SymbolList {
	ArrayList<Symbol>	list = new ArrayList<Symbol>(100);
	int	position;
	Stack<Integer> marks = new Stack<Integer>(); 
	
	public SymbolList() {
		position = 0;
	}
	
	
	public int size() {
		return list.size();
	}
	
	
	public void pushPosition() {
		marks.push(position);
	}
	
	
	public void popPosition() throws Exception {
		if (marks.size() == 0)
			throw new Exception("No saved positions");
		
		this.position = marks.pop();
	}
	
	
	public void insert(Symbol symbol, int position) {
		list.add(position, symbol);
	}
	
	
	public void insertHere(List<Symbol> symbols)
	{
		list.addAll(position, symbols);
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
	
	
	public void RemoveFirstAndReset() {
		if (list.size() > 0)
			list.remove(0);
		position = 0;
	}
}
