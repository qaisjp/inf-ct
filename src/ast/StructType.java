package ast;

public class StructType implements Type {
    public final String str;
    public StructTypeDecl decl; // to be filled in by the type analyser

    public StructType(String s) {
        this.str = s;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    public boolean equals(Type t) {
        if (!(t instanceof StructType)) {
            return false;
        }

        return equals((StructType) t);
    }

    public boolean equals(StructType t) {
        return str.equals(t.str);
    }
}
