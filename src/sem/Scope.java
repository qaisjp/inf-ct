package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable;
	private Map<String, Symbol> structSymbolTable;
	
	public Scope(Scope outer) { 
		this.outer = outer;
		this.symbolTable = new HashMap<>();
		this.structSymbolTable = new HashMap<>();
	}
	
	public Scope() { this(null); }
	
	public Symbol lookup(String name, boolean isStruct) {
		Symbol s = lookupCurrent(name, isStruct);

		if (s == null && outer != null) {
			return outer.lookup(name, isStruct);
		}
		return s;
	}
	public Symbol lookupCurrent(String name, boolean isStruct) {
		return (isStruct ? structSymbolTable : symbolTable).get(name);
	}

	public void putStruct(StructSymbol sym) {
		structSymbolTable.put(sym.name, sym);
	}
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
