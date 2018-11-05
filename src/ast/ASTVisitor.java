package ast;

public interface ASTVisitor<T> {
    T visitProgram(Program p);

    // Types
    T visitBaseType(BaseType bt);
    T visitPointerType(PointerType pt);
    T visitStructType(StructType st);
    T visitArrayType(ArrayType at);

    // Declarations
    T visitStructTypeDecl(StructTypeDecl st);
    T visitVarDecl(VarDecl vd);
    T visitFunDecl(FunDecl p);

    // Expressions
    T visitIntLiteral(IntLiteral il);
    T visitStrLiteral(StrLiteral sl);
    T visitChrLiteral(ChrLiteral cl);
    T visitVarExpr(VarExpr v);
    T visitFunCallExpr(FunCallExpr f);
    T visitBinOp(BinOp bo);
    T visitArrayAccessExpr(ArrayAccessExpr aae);
    T visitFieldAccessExpr(FieldAccessExpr fae);
    T visitValueAtExpr(ValueAtExpr vae);
    T visitSizeOfExpr(SizeOfExpr soe);
    T visitTypecastExpr(TypecastExpr te);

    // Statements
    T visitBlock(Block b);
    T visitWhile(While w);
    T visitIf(If i);
    T visitAssign(Assign a);
    T visitReturn(Return r);
    T visitExprStmt(ExprStmt e);
}
