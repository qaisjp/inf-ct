package ast;

public class Return extends Stmt {
    public final Expr expr; // optional!

    public Return(Expr expr) {
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }

    @Override
    public String toString() {
        if (expr == null) {
            return "return;";
        }
        return "return " + expr.toString() + ";";
    }
}
