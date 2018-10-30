package gen;

import ast.*;

import java.io.PrintWriter;

public class DataVisitor implements ASTVisitor<Void> {
    private PrintWriter writer;

    public DataVisitor(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Void visitBlock(Block b) {
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl p) {
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt e) {
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        return null;
    }

    @Override
    public Void visitIf(If i) {
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr f) {
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr te) {
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr soe) {
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr vae) {
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fae) {
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        return null;
    }
}
