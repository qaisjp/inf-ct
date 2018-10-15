package ast;

public class FieldAccessExpr extends Expr {
    public final Expr expr;
    public final String string;

    public FieldAccessExpr(Expr expr, String string) {
        this.expr = expr;
        this.string = string;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }
}
