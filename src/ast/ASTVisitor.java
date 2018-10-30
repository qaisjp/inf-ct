package ast;

public interface ASTVisitor<T> {
    public T visitProgram(Program p);

    // Types
    public T visitBaseType(BaseType bt);
    public T visitPointerType(PointerType pt);
    public T visitStructType(StructType st);
    public T visitArrayType(ArrayType at);

    // Declarations
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitVarDecl(VarDecl vd);
    public T visitFunDecl(FunDecl p);

    // Expressions
    public T visitIntLiteral(IntLiteral il);
    public T visitStrLiteral(StrLiteral sl);
    public T visitChrLiteral(ChrLiteral cl);
    public T visitVarExpr(VarExpr v);
    public T visitFunCallExpr(FunCallExpr f);
    public T visitBinOp(BinOp bo);
    public T visitArrayAccessExpr(ArrayAccessExpr aae);
    public T visitFieldAccessExpr(FieldAccessExpr fae);
    public T visitValueAtExpr(ValueAtExpr vae);
    public T visitSizeOfExpr(SizeOfExpr soe);
    public T visitTypecastExpr(TypecastExpr te);

    // Statements
    public T visitBlock(Block b);
    public T visitWhile(While w);
    public T visitIf(If i);
    public T visitAssign(Assign a);
    public T visitReturn(Return r);
    public T visitExprStmt(ExprStmt e);
}
