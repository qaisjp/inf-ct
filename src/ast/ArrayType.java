package ast;

public class ArrayType implements Type {
    public final Type innerType;
    public final int elements;

    public ArrayType(Type innerType, int elements) {
        this.innerType = innerType;
        this.elements = elements;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }
}
