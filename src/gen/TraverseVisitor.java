package gen;

import ast.*;

import java.util.ArrayList;
import java.util.List;

public abstract class TraverseVisitor<T> implements ASTVisitor<T> {

    public List<T> visitEach(List<? extends ASTNode> list) {
        List<T> results = new ArrayList<>();
        for (ASTNode l : list) {
            results.add(l.accept(this));
        }
        return results;
    }


    @Override
    public T visitProgram(Program p) {
        visitEach(p.structTypeDecls);
        visitEach(p.varDecls);
        visitEach(p.funDecls);
        return null;
    }

    @Override
    public T visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public T visitPointerType(PointerType pt) {
        pt.innerType.accept(this);
        return null;
    }

    @Override
    public T visitStructType(StructType st) {
        return null;
    }

    @Override
    public T visitArrayType(ArrayType at) {
        at.innerType.accept(this);
        return null;
    }

    @Override
    public T visitStructTypeDecl(StructTypeDecl st) {
        st.structType.accept(this);
        visitEach(st.varDeclList);
        return null;
    }

    @Override
    public T visitVarDecl(VarDecl vd) {
        vd.varType.accept(this);
        return null;
    }

    @Override
    public T visitFunDecl(FunDecl p) {
        p.type.accept(this);
        visitEach(p.params);
        p.block.accept(this);
        return null;
    }

    @Override
    public T visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public T visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public T visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public T visitVarExpr(VarExpr v) {
        return null;
    }

    @Override
    public T visitFunCallExpr(FunCallExpr f) {
        visitEach(f.exprList);
        return null;
    }

    @Override
    public T visitBinOp(BinOp bo) {
        bo.lhs.accept(this);
        bo.rhs.accept(this);
        return null;
    }

    @Override
    public T visitArrayAccessExpr(ArrayAccessExpr aae) {
        aae.expr.accept(this);
        aae.index.accept(this);
        return null;
    }

    @Override
    public T visitFieldAccessExpr(FieldAccessExpr fae) {
        fae.expr.accept(this);
        return null;
    }

    @Override
    public T visitValueAtExpr(ValueAtExpr vae) {
        vae.expr.accept(this);
        return null;
    }

    @Override
    public T visitSizeOfExpr(SizeOfExpr soe) {
        soe.typeToCheck.accept(this);
        return null;
    }

    @Override
    public T visitTypecastExpr(TypecastExpr te) {
        te.castTo.accept(this);
        te.expr.accept(this);
        return null;
    }

    @Override
    public T visitBlock(Block b) {
        visitEach(b.varDecls);
        visitEach(b.stmtList);
        return null;
    }

    @Override
    public T visitWhile(While w) {
        w.expr.accept(this);
        w.stmt.accept(this);
        return null;
    }

    @Override
    public T visitIf(If i) {
        i.expr.accept(this);
        i.stmt.accept(this);
        if (i.elseStmt != null) {
            i.elseStmt.accept(this);
        }
        return null;
    }

    @Override
    public T visitAssign(Assign a) {
        a.lhs.accept(this);
        a.rhs.accept(this);
        return null;
    }

    @Override
    public T visitReturn(Return r) {
        r.expr.accept(this);
        return null;
    }

    @Override
    public T visitExprStmt(ExprStmt e) {
        e.expr.accept(this);
        return null;
    }
}
