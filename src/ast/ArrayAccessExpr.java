package ast;

public class ArrayAccessExpr extends Expr {
    public final Expr expr;
    public final Expr index;

    public ArrayAccessExpr(Expr expr, Expr index) {
        this.expr = expr;
        this.index = index;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }

    @Override
    public String toString() {
        return expr.toString() + "[" + index.toString() + "]";
    }
}
