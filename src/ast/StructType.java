package ast;

public class StructType extends ContainerType<String> {
    public final String str;
    public StructTypeDecl decl; // to be filled in by the type analyser

    public StructType(String s) {
        super(s);
        this.str = s;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }
}
