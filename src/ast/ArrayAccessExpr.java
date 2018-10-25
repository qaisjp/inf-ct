package ast;

public class ArrayAccessExpr extends Expr {
    public final Expr lhs;
    public final Expr rhs;

    public ArrayAccessExpr(Expr lhs, Expr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }

    @Override
    public String toString() {
        return lhs.toString() + "[" + rhs.toString() + "]";
    }
}
