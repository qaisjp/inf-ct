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

    @Override
    public String toString() {
        return "struct " + str;
    }
}
