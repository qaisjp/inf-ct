package ast;

public class BinOp extends Expr {
    public final Expr x;
    public final Op op;
    public final Expr y;

    public BinOp(Expr x, Op op, Expr y) {
        if (op == null) {
            throw new NullPointerException();
        }

        this.x = x;
        this.op = op;
        this.y = y;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }

    @Override
    public String toString() {
        return x.toString() + op.toLang() + y.toString();
    }
}
