package ast;

public interface ASTVisitor<T> {
    public T visitBaseType(BaseType bt);
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitBlock(Block b);
    public T visitFunDecl(FunDecl p);
    public T visitProgram(Program p);
    public T visitVarDecl(VarDecl vd);
    public T visitVarExpr(VarExpr v);

    // to complete ... (should have one visit method for each concrete AST node class)
    public T visitAssign(Assign a);
    public T visitExprStmt(ExprStmt e);
    public T visitReturn(Return r);
    public T visitIf(If i);
    public T visitWhile(While w);
    public T visitStructType(StructType st);
    public T visitPointerType(PointerType pt);
    public T visitIntLiteral(IntLiteral il);
    public T visitStrLiteral(StrLiteral sl);
    public T visitChrLiteral(ChrLiteral cl);
    public T visitFunCallExpr(FunCallExpr f);
}
