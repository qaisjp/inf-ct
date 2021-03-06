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

            writer.withLabel("main").directive("globl %s", "main");
            writer.jal("func_main_start");
            Register.arg[0].set(Register.v0);
            Register.v0.loadImmediate(17);
            writer.syscall();
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
    public Register visitReturn(Return r) {
        return r.accept(V.function);
    }

    @Override
    public Register visitChrLiteral(ChrLiteral c) {
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
        int size = e.type.sizeof();
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
        // Set a new register to the address of this item on stack. $fp + item.offset
        writer.add(value, Register.fp, decl.getGenStackOffset());

        return value;
    }

    private Register addressOf(FieldAccessExpr f) {
        assert f.expr.type instanceof StructType;

        // Only varExpr.fieldName can take advantage of directly addressing by label
        if (f.expr instanceof VarExpr) {
            if (((VarExpr) f.expr).vd.isGlobal()) {
                String label = ((VarExpr) f.expr).vd.getStructFieldLabel(f.string);

                Register address = V.registers.get();
                address.loadAddress(label);
                return address;
            }
        }

        // Get the address of the struct
        Register address = f.expr.accept(V.text);
        writer.comment("%s = addressOf(%s)", address, f);

        // Offset the address by whichever amount
        StructType structType = (StructType) f.expr.type;
        int offset = 0; // todo: only calculate this stuff once (also elsewhere in assign)
        for (VarDecl v : structType.decl.varDeclList) {
            if (v.varName.equals(f.string)) {
                address.add(offset);
                return address;
            }

            offset += GenUtils.wordAlign(v.varType.sizeof());
        }

        throw new RuntimeException("could not find field in: " + f.toString());
    }

    private Register addressOf(ValueAtExpr e) {
        assert e.expr.type instanceof PointerType;
        writer.comment(e);
        try (IndentWriter scope = writer.scope()) {
            // Store address of the pointer in a register
            Register locationOfPointer = addressOf(e.expr);

            // Read the pointer into our register
            writer.lw(locationOfPointer, locationOfPointer, 0);

            // This is what the above load word has done
            Register pointer = locationOfPointer;

            return pointer;
        }
    }

    public Register addressOf(Expr e) {
        if (e instanceof VarExpr) {
            return addressOf((VarExpr) e);
        } else if (e instanceof ArrayAccessExpr) {
            return addressOf((ArrayAccessExpr) e);
        } else if (e instanceof FieldAccessExpr) {
            return addressOf((FieldAccessExpr) e);
        } else if (e instanceof ValueAtExpr) {
            return addressOf((ValueAtExpr) e);
        }
        throw new RuntimeException("Got expression that I don't know how to addressOf - " + e.toString());
    }

    // Get value of a certain type stored at this address
    public Register getValue(Register address, Type t) {
        Register value = V.registers.get();

        writer.comment("%s = valueAt(%s, %s)", value, address, t);

        if (t == BaseType.CHAR) {
            value.loadByte(address, 0);
        } else if (t == BaseType.INT || t instanceof PointerType) {
            value.loadWord(address, 0);
        } else if (t instanceof ArrayType || t instanceof StructType) {
            value.set(address);
        } else if (t == BaseType.VOID) {
            writer.nop();
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
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr e) {
        assert e.type != null;
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr e) {
        assert e.expr.type instanceof PointerType;
        return visitAddressableExpr(e);
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr e) {
        // visitSizeOfExpr is a value, so we don't need to get addr
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
            try (Register autoFreedRegisterThatCanBeNull = e.expr.accept(V.text)) {

            }
        }
        return null;
    }

    private Labeller ifLabeller = new Labeller("if");

    @Override
    public Register visitIf(If s) {
        String endLabel = ifLabeller.num("end");
        String elseLabel = (s.elseStmt == null) ? endLabel : ifLabeller.num("else");

        writer.comment("if (%s)", s.expr);
        try (
            IndentWriter scope = writer.scope();
            Register shouldSkip = s.expr.accept(V.text)
        ) {
            writer.beqz(shouldSkip, elseLabel);

            s.stmt.accept(V.text);

            writer.b(endLabel);
        }

        if (s.elseStmt == null) {
            writer.withLabel(endLabel).nop();
            return null;
        }

        writer.withLabel(elseLabel).comment("else");
        try (IndentWriter scope = writer.scope()) {
            s.elseStmt.accept(V.text);
        }

        writer.withLabel(endLabel).nop();

        return null;
    }

    private Labeller whileLabeller = new Labeller("while");
    @Override
    public Register visitWhile(While s) {
        String startLabel = whileLabeller.num("this");
        String endLabel = whileLabeller.num("end");

        writer.withLabel(startLabel).comment("while (%s)", s.expr);
        try (
            IndentWriter scope = writer.scope();
            Register shouldContinue = s.expr.accept(V.text)
        ) {
            writer.beqz(shouldContinue, endLabel);

            s.stmt.accept(V.text);

            writer.b(startLabel);
        }

        writer.withLabel(endLabel).nop();
        return null;
    }
}
