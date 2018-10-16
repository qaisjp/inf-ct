package ast;

public class StructType implements Type {
    public final String str;
    public StructTypeDecl decl;

    public StructType(String s) {
        this.str = s;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }
}
