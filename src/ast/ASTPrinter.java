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
        writer.printf(", %s, ", fd.name);
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(", ");
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
        writer.printf("FunCallExpr(%s", f.name);
        for (Expr e: f.exprList) {
            writer.print(", ");
            e.accept(this);
        }
        writer.print(")");

        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr te) {
        writer.print("TypecastExpr(");
        te.type.accept(this);
        writer.print(", ");
        te.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr soe) {
        writer.print("SizeOfExpr(");
        soe.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr vae) {
        writer.print("ValueAtExpr(");
        vae.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral f) {
        writer.printf("IntLiteral(%d)", f.innerType);
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral f) {
        writer.printf("StrLiteral(%s)", f.innerType);
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral f) {
        writer.printf("ChrLiteral(%c)", f.innerType);
        return null;
    }

    @Override
    public Void visitPointerType(PointerType f) {
        writer.print("PointerType(");
        f.innerType.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructType(StructType f) {
        writer.printf("StructType(%s)", f.str);
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        writer.print("ArrayType(");
        at.innerType.accept(this);
        writer.printf(", %d)", at.elements);
        return null;
    }

    @Override
    public Void visitWhile(While f) {
        writer.print("While(");
        f.expr.accept(this);
        writer.print(", ");
        f.stmt.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If f) {
        writer.print("If(");
        f.expr.accept(this);
        writer.print(", ");
        f.stmt.accept(this);
        if (f.elseStmt != null) {
            writer.print(", ");
            f.elseStmt.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        writer.print("Return(");
        if (r.expr != null) {
            r.expr.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        writer.print("ExprStmt(");
        exprStmt.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign assign) {
        writer.print("Assign(");
        assign.lhs.accept(this);
        writer.print(", ");
        assign.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        bo.lhs.accept(this);
        writer.printf(", %s, ", bo.op.toString());
        bo.rhs.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr arrayAccessExpr) {
        writer.print("ArrayAccessExpr(");
        arrayAccessExpr.lhs.accept(this);
        writer.print(", ");
        arrayAccessExpr.rhs.accept(this);
        writer.print(")");

        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fieldAccessExpr) {
        writer.print("FieldAccessExpr(");
        fieldAccessExpr.expr.accept(this);
        writer.printf(", %s)", fieldAccessExpr.string);
        return null;
    }
}
