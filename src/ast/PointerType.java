package ast;

public class PointerType implements Type {
    public final Type innerType;

    public PointerType(Type innerType) {
        this.innerType = innerType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }

    @Override
    public String toString() {
        return "*" + innerType.toString();
    }

    @Override
    public int sizeof() {
        return BaseType.INT.sizeof(); // size of pointer are size of INT
    }
}
