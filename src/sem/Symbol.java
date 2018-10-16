package sem;

public abstract class Symbol {
	public String name;
	
	
	public Symbol(String name) {
		this.name = name;
	}

	boolean isVar() {
		return this instanceof VarSymbol;
	}

	boolean isFun() {
		return this instanceof FunSymbol;
	}
}
