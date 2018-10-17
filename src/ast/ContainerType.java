package ast;

public abstract class ContainerType<T> implements Type {
    public final T innerType;

    public ContainerType(T t) {
        innerType = t;
    }

    public boolean equals(Type t) {
        if (!(t instanceof ContainerType)) {
            return false;
        }

        return equals((ContainerType) t);
    }

    public boolean equals(ContainerType t) {
        return innerType.equals(t.innerType);
    }
}
