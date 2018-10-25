package ast;

public class IntLiteral extends Expr {
    public final int innerType;

    public IntLiteral(int innerType) {
        this.innerType = innerType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }

    @Override
    public String toString() {
        return Integer.toString(innerType);
    }
}
