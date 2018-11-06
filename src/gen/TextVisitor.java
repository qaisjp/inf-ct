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
        if (f.decl.isInbuilt) {
            return f.accept(V.inbuilt);
        }

        super.visitFunCallExpr(f);
        writer.comment("stub! %s", f); // todo
        Register result = V.registers.get();
        return result;
    }

    private Labeller funcLabeller = new Labeller("func");
    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isInbuilt) {
            return null;
        }

        // Store arguments on stack
        ;

        // Store return value on stack
        ;

        // Our stack already has space allocated for the arguments and result.
        // Set frame pointer to the stack pointer so we always have a base address for the args and result.
        Register.fp.set(Register.sp);

        // Store variable declarations on stack
        ;

        String label = funcLabeller.label(f.name);
        writer.leadNewline().withLabel(label).comment("%s", f);

        writer.suppressNextNewline();
        try (IndentWriter scope = writer.scope()) {
            // If this is the main function, we need a second labels!
            if (f.name.equals("main")) {
                writer.withLabel("main").printf(".globl main");
            }

            // do some stuff (todo)
            super.visitFunDecl(f);
        }

        // Restore stack pointer to the frame pointer
        Register.sp.set(Register.fp);

        // Jump to $ra
        Register.ra.jump();

        return null; // no register returned for function declarations
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

            // Load address into "value"
            String label = decl.getGlobalLabel();
            value.loadAddress(label);

            Type t = decl.varType;
            if (t != BaseType.CHAR && t != BaseType.INT) {
                // arrays and structs need SPECIAL treatment!
                // oh and strings too topKEK
                // todo
                throw ExceptionVarTypeNotImplemented;
            }

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
