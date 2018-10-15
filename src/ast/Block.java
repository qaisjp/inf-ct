package ast;

import java.util.List;

public class Block extends Stmt {

    public final List<VarDecl> varDecls;
    public final List<Stmt> stmtList;

    public Block(List<VarDecl> varDecls, List<Stmt> stmtList) {
        this.varDecls = varDecls;
        this.stmtList = stmtList;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }
}
