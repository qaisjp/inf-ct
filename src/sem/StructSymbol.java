package sem;

import ast.StructTypeDecl;

public class StructSymbol extends Symbol {
    public final StructTypeDecl decl;

    public StructSymbol(StructTypeDecl decl) {
        super(decl.structType.str);
        this.decl = decl;
    }
}
