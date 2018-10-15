package ast;

public class PointerType implements Type {
    public final Type innerType;

    public PointerType(Type innerType) {
        this.innerType = innerType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }
}
