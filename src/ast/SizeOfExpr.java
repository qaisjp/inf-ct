package ast;

public class SizeOfExpr extends Expr {
    public final Type typeToCheck;

    public SizeOfExpr(Type type) {
        this.typeToCheck = type;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitSizeOfExpr(this);
    }

    @Override
    public String toString() {
        return "sizeof(" + typeToCheck.toString() + ")";
    }
}
