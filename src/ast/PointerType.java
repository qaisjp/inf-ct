package ast;

public class PointerType extends ContainerType<Type> {

    public PointerType(Type innerType) {
        super(innerType);
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
    }

    @Override
    public String toString() {
        return "*" + this.innerType.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }
}
