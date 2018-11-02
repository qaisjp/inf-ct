package ast;

public class IntLiteral extends Expr {
    public final int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
