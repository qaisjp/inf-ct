package gen;

import ast.*;

public class TextVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;

    public static final RuntimeException ExceptionVarTypeNotImplemented = new RuntimeException(
            "STUB! arrays, structs (& maybe strings) haven't been implemented yet");

    public TextVisitor() {
        this.writer = V.writer;
    }

    @Override
    public Register visitProgram(Program p) {
        writer.leadNewline().printf(".text");
        writer.suppressNextNewline();

        try (IndentWriter scope = writer.scope()) {
            super.visitProgram(p);
        }

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        return f.accept(V.function);
    }

    @Override
    public Register visitFunDecl(FunDecl f) {
        return f.accept(V.function);
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        return binOp.accept(V.binOp);
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr e) {
        // todo: do you need to accept type?
        return e.expr.accept(this);
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr e) {
        writer.leadNewline().comment("%s", e);

        try (IndentWriter scope = writer.scope()) {
            Register val = V.registers.get();
            val.loadImmediate(e.typeToCheck.sizeof());
            return val;
        }
    }

    @Override
    public Register visitIntLiteral(IntLiteral i) {
        Register val = V.registers.get();
        val.loadImmediate(i.value);
        return val;
    }

    @Override
    public Register visitAssign(Assign a) {
        writer.leadNewline().comment("%s", a);
        writer.suppressNextNewline();

        try (IndentWriter scope = writer.scope()) {
            a.accept(V.assign);
        }
        return null;
    }

    @Override
    public Register visitStrLiteral(StrLiteral s) {
        Register address = V.registers.get();
        address.loadAddress(s.genLabel);
        return address;
    }

    public Register getVarExprAddress(VarExpr v) {
        VarDecl decl = v.vd;

        // todo: isGlobal returns false for structs. getGlobalLabel should return the label of the first item
        if (decl.isGlobal()) {
            Register value = V.registers.get();

            if (decl.varType instanceof StructType) {
                // todo: structs need SPECIAL treatment!
                throw ExceptionVarTypeNotImplemented;
            }

            // Load address into "value"
            String label = decl.getGlobalLabel();
            value.loadAddress(label);

            return value;
        }

        // todo: if not global!!!!!!

        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        VarDecl decl = v.vd;

        // Get address of variable expression
        Register value = getVarExprAddress(v);

        // Load the word or byte now from the register
        Type t = decl.varType;
        if (t == BaseType.CHAR) {
            value.loadByte(value, 0);
        } else if (t == BaseType.INT) {
            value.loadWord(value, 0);
        } else {
            // arrays and structs need SPECIAL treatment!
            // and strings too
            // todo
            throw ExceptionVarTypeNotImplemented;
        }

        return value;
    }

    @Override
    public Register visitExprStmt(ExprStmt e) {
        writer.leadNewline().comment("%s", e);
        try (IndentWriter scope = writer.scope().suppressNextNewline()) {
            return super.visitExprStmt(e);
        }
    }

}
