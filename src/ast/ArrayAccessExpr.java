package ast;

public class ArrayAccessExpr extends Expr {
    public final Expr expr;
    public final Expr index;

    private final String InnerTypeError = "Expression is not pointer or array";

    public ArrayAccessExpr(Expr expr, Expr index) {
        this.expr = expr;
        this.index = index;
    }

    public Type getInnerType() {
        if (expr.type instanceof PointerType) {
            return ((PointerType) expr.type).innerType;
        } else if (expr.type instanceof ArrayType) {
            return ((ArrayType) expr.type).elemType;
        }
        throw new RuntimeException(InnerTypeError + " is expr:" + expr.toString() + "type:" + expr.type.toString());
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }

    @Override
    public String toString() {
        return expr.toString() + "[" + index.toString() + "]";
    }
}
