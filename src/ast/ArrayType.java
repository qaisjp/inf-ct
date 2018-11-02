package ast;

public class ArrayType implements Type {
    public final Type elemType;
    public final int elements;

    public ArrayType(Type elemType, int elements) {
        this.elemType = elemType;
        this.elements = elements;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

    public int sizeof() {
        return elements * elemType.sizeof();
    }

    @Override
    public String toString() {
        return elemType.toString() + "[" + String.valueOf(elements) + "]";
    }
}
