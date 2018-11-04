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
        writer.push();
        super.visitProgram(p);
        writer.pop();

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
        Register lReg = a.lhs.accept(this);
        Register rReg = a.rhs.accept(this);

        if (a.lhs instanceof VarExpr) {
            VarDecl decl = ((VarExpr) a.lhs).vd;

            int size = decl.varType.sizeof();
            if (size == 1) {
                lReg.storeByte(rReg, 0);
            } else if (size == 4) {
                lReg.storeWord(rReg, 0);
            } else {
                // arrays and structs need SPECIAL treatment!
                // todo
                throw ExceptionVarTypeNotImplemented;
            }
        } else {
            // todo
            throw new RuntimeException("structs, pointers, etc etc not implemented yet");
        }

        rReg.free();
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        VarDecl decl = v.vd;

        if (decl.isGlobal()) {
            Register value = V.registers.get();

            // Load address into "value"
            String label = decl.getGlobalLabel();
            value.loadAddress(label);

            // Load the word or byte now from the register
            int size = decl.varType.sizeof(); // sizes are 4, 1, or other (in case of arrays, structs)
            if (size == 1) {
                value.loadByte(value, 0);
            } else if (size == 4) {
                value.loadWord(value, 0);
            } else {
                // arrays and structs need SPECIAL treatment!
                // todo
                throw ExceptionVarTypeNotImplemented;
            }

            return value;
        }

        // todo: if not global!!!!!!

        return null;
    }
}
