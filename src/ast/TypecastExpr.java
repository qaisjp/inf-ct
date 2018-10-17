package ast;

public class TypecastExpr extends Expr {
    public final Type castTo;
    public final Expr expr;

    public TypecastExpr(Type type, Expr expr) {
        this.castTo = type;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecastExpr(this);
    }
}
