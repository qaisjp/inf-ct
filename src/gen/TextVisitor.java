package gen;

import ast.*;

public class TextVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;

    public TextVisitor() {
        this.writer = V.writer;
    }

    @Override
    public Register visitProgram(Program p) {
        writer.directive("text");

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
    public Register visitBlock(Block b) {
        return b.accept(V.function);
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        return binOp.accept(V.binOp);
    }

    @Override public Register visitChrLiteral(ChrLiteral c) {
        Register val = V.registers.get();
        writer.comment("%s = %s", val, c.toString());
        val.loadImmediate(c.value);
        return val;
    }

    @Override
    public Register visitIntLiteral(IntLiteral i) {
        Register val = V.registers.get();
        val.loadImmediate(i.value);
        return val;
    }

    @Override
    public Register visitAssign(Assign a) {
        writer.comment("%s", a);

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

    private Register addressOf(ArrayAccessExpr e) {
        Register pointer = e.expr.accept(V.text);

        writer.comment("%s = addressOf(%s)", pointer, e);
        int size = e.getInnerType().sizeof();
        try (Register index = e.index.accept(V.text)) {
            index.mul(size);
            pointer.add(index);
        }

        return pointer;
    }

    private Register addressOf(VarExpr v) {
        VarDecl decl = v.vd;

        Register value = V.registers.get();
        writer.comment("%s = addressOf(%s)", value, v);

        if (decl.isGlobal()) {
            // Load address into "value"
            String label = decl.getGlobalLabel();
            value.loadAddress(label);

            return value;
        }

        // Local variables have an offset defined from the current frame pointer.
        // Set a new register to the address of this item on stack. $fp-item.offset
        writer.sub(value, Register.fp, decl.getGenStackOffset());

        return value;
    }

    private Register addressOf(FieldAccessExpr f) {
        assert f.expr.type instanceof StructType;

        Register value = V.registers.get();
        writer.comment("%s = addressOf(%s)", value, f);

        writer.nop(); // todo

        return value;
    }

    public Register addressOf(Expr e) {
        if (e instanceof VarExpr) {
            return addressOf((VarExpr) e);
        } else if (e instanceof ArrayAccessExpr) {
            return addressOf((ArrayAccessExpr) e);
        } else if (e instanceof FieldAccessExpr) {
            return addressOf((FieldAccessExpr) e);
        }
        throw new RuntimeException("Got expression that I don't know how to addressOf - " + e.toString());
    }

    // Get value of a certain type stored at this address
    public Register getValue(Register address, Type t) {
        Register value = V.registers.get();

        if (t == BaseType.CHAR) {
            value.loadByte(address, 0);
        } else if (t == BaseType.INT || t instanceof PointerType) {
            value.loadWord(address, 0);
        } else if (t instanceof ArrayType || t instanceof StructType) {
            value.set(address);
        } else {
            throw new RuntimeException(
                    "getValue not implemented yet with type " + t.toString());
        }

        return value;
    }

    private Register visitAddressableExpr(Expr e) {
        try (Register address = addressOf(e)) {
            return getValue(address, e.type);
        }
    }

    @Override
    public Register visitVarExpr(VarExpr e) {
        assert e.type == e.vd.varType;
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr e) {
        assert e.type == e.getInnerType();
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr e) {
        assert e.type != null;
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr e) {
        // visitSizeOfExpr is a value, so we don't need to get addr
        // todo: do you need to accept type?
        Register value;
        writer.comment(e);
        try (IndentWriter scope = writer.scope()) {
            value = e.expr.accept(V.text);
        }
        return value;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr e) {
        // visitSizeOfExpr is a value, so we don't need to get addr

        writer.comment(e);

        try (IndentWriter scope = writer.scope()) {
            Register val = V.registers.get();
            val.loadImmediate(e.typeToCheck.sizeof());
            return val;
        }
    }

    @Override
    public Register visitExprStmt(ExprStmt e) {
        writer.comment("%s", e);
        try (IndentWriter scope = writer.scope()) {
            return super.visitExprStmt(e);
        }
    }
}
