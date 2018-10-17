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

    public boolean equals(Type t) {
        if (!(t instanceof PointerType)) {
            return false;
        }

        return equals((PointerType) t);
    }

    public boolean equals(PointerType t) {
        System.out.printf("=Comparison called for %s against %s=", innerType, t.innerType);
        return innerType.equals(t.innerType);
    }
}
