package gen;

import ast.*;

public class TextVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;

    private static RuntimeException ExceptionVarTypeNotImplemented = new RuntimeException(
            "STUB! arrays and structs haven't been implemented yet");

    public TextVisitor() {
        this.writer = V.writer;
    }

    @Override
    public Register visitProgram(Program p) {
        writer.leadNewline().printf(".text");

        try (IndentWriter scope = writer.scope()) {
            super.visitProgram(p);
        }

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            V.inbuilt.visitFunCallExpr(f);
            return null; // todo
        }

        return null; // todo
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        return binOp.accept(V.binOp);
    }

    @Override
    public Register visitIntLiteral(IntLiteral i) {
        Register val = V.registers.get();
        val.loadImmediate(i.value);
        return val;
    }

    public Register visitAssign(Assign a) {
        Register rReg = a.rhs.accept(this);

        if (a.lhs instanceof VarExpr) {
            try (Register lReg = getVarExprAddress((VarExpr) a.lhs)) {
                VarDecl decl = ((VarExpr) a.lhs).vd;

                Type varType = decl.varType;
                if (varType == BaseType.CHAR) {
                    rReg.storeByteAt(lReg, 0);
                } else if (varType == BaseType.INT) {
                    rReg.storeWordAt(lReg, 0);
                } else {
                    // arrays and structs need SPECIAL treatment!
                    // oh and strings too topKEKKER
                    // todo
                    throw ExceptionVarTypeNotImplemented;
                }
            }
        } else {
            // todo
            throw new RuntimeException("structs, pointers, etc etc not implemented yet");
        }

        rReg.free();
        return null;
    }

    public Register getVarExprAddress(VarExpr v) {
        VarDecl decl = v.vd;

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

}
