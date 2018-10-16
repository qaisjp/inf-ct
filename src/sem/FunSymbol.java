package sem;

import ast.FunDecl;

public class FunSymbol extends Symbol {
    public final FunDecl funDecl;

    public FunSymbol(FunDecl f) {
        super(f.name);
        this.funDecl = f;
    }
}
