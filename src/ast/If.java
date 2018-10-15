package ast;

public class If extends Stmt {
    public final Expr expr;
    public final Stmt stmt;
    public final Stmt elseStmt;

    public If(Expr expr, Stmt stmt, Stmt elseStmt) {
        this.expr = expr;
        this.stmt = stmt;
        this.elseStmt = elseStmt;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }
}
