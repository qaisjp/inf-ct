package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        // to complete
        String delimiter = "";
        for (VarDecl varDecl: b.varDecls) {
            writer.printf(delimiter);
            varDecl.accept(this);
            delimiter = ", ";
        }
        for (Stmt stmt: b.stmtList) {
            writer.printf(delimiter);
            stmt.accept(this);
            delimiter = ", ";
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print(bt.toString());
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        // maybe still todo
        writer.print("StructTypeDecl(");
        st.structType.accept(this);
        for (VarDecl varDecl: st.varDeclList) {
            writer.print(", ");
            varDecl.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr f) {
        // todo
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
    public Void visitIntLiteral(IntLiteral f) {
        // todo
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral f) {
        // todo
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral f) {
        // todo
        return null;
    }

    @Override
    public Void visitPointerType(PointerType f) {
        // todo
        return null;
    }

    @Override
    public Void visitStructType(StructType f) {
        // todo
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Void visitWhile(While f) {
        // todo
        return null;
    }

    @Override
    public Void visitIf(If f) {
        // todo
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        // todo
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        // todo
        return null;
    }

    @Override
    public Void visitAssign(Assign assign) {
        // todo
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        // todo
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
        // todo
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
        // todo
        return null;
    }

    // todo
}
