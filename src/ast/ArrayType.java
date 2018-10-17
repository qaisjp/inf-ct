package ast;

public class ArrayType extends ContainerType<Type> {
    public final int elements;

    public ArrayType(Type innerType, int elements) {
        super(innerType);
        this.elements = elements;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

    @Override
    public String toString() {
        return innerType.toString() + "[" + String.valueOf(elements) + "]";
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }
}
