package ast;

import java.util.List;

public class FunCallExpr  extends Expr {
    public final String name;
    public final List<Expr> exprList;
    public FunDecl decl; // to be filled in by the name analyser

    public FunCallExpr(String name, List<Expr> exprList) {
        this.name = name;
        this.exprList = exprList;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }
}
