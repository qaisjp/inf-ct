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

    @Override
    public Register visitTypecastExpr(TypecastExpr e) {
        // todo: do you need to accept type?
        Register value;
        writer.comment("%s", e);
        try (IndentWriter scope = writer.scope()) {
            value = e.expr.accept(V.text);
        }
        return value;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr e) {
        writer.comment("%s", e);

        try (IndentWriter scope = writer.scope()) {
            Register val = V.registers.get();
            val.loadImmediate(e.typeToCheck.sizeof());
            return val;
        }
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

    public Register addressOf(ArrayAccessExpr e) {
        Register pointer = e.expr.accept(V.text);

        writer.comment("%s = addressOf(%s)", pointer, e);
        int size = e.getInnerType().sizeof();
        try (Register index = e.index.accept(V.text)) {
            index.mul(size);
            pointer.add(index);
        }

        return pointer;
    }

    public Register addressOf(VarExpr v) {
        VarDecl decl = v.vd;

        Register value = V.registers.get();
        writer.comment("%s = addressOf(%s)", value, v);

        // todo: isGlobal returns true for structs. but getGlobalLabel crashes. should it return the label of the first item?
        if (decl.isGlobal()) {

            if (decl.varType instanceof StructType) {
                // todo: structs need SPECIAL treatment!
                throw new RuntimeException(
                        "STUB! addressOf(struct) not been implemented yet");
            }

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

    // Get value of a certain type stored at this address
    public Register getValue(Register address, Type t) {
        Register value = V.registers.get();

        if (t == BaseType.CHAR) {
            value.loadByte(address, 0);
        } else if (t == BaseType.INT || t instanceof PointerType) {
            value.loadWord(address, 0);
        } else if (t instanceof ArrayType) {
            value.set(address);
        } else {
            // todo: structs need SPECIAL treatment!
            throw new RuntimeException(
                    "STUB! structs not implemented yet with type " + t.toString());
        }

        return value;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        VarDecl decl = v.vd;

        // Get address of variable expression
        try (Register address = addressOf(v)) {
            return getValue(address, decl.varType);
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
