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

    @Override
    public String toString() {
        return innerType.toString() + "[" + String.valueOf(elements) + "]";
    }

    public boolean equals(Type t) {
        if (!(t instanceof ArrayType)) {
            return false;
        }

        return equals((ArrayType) t);
    }

    public boolean equals(ArrayType t) {
        return innerType.equals(t.innerType) && (elements == t.elements);
    }
}
