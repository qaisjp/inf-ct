package gen;

import ast.*;

public class AssignVisitor extends TraverseVisitor<Void> {
    IndentWriter writer;

    public AssignVisitor() {
        this.writer = V.writer;
    }

    // store
    private void storeValue(Register sourceValue, Type type, Register targetAddress, int offset) {
        writer.leadNewline().comment("(%s + %d) = valueOf(%s, %s)", targetAddress, offset, sourceValue, type);
        if (type == BaseType.CHAR) {
            sourceValue.storeByteAt(targetAddress, offset);
        } else if (type == BaseType.INT || type instanceof PointerType) {
            sourceValue.storeWordAt(targetAddress, offset);
        } else {
            // arrays and structs need SPECIAL treatment!
            // oh and strings too topKEKKER
            // todo
            throw new RuntimeException(
                    "STUB! arrays, structs (& maybe strings) haven't been implemented yet");
        }
    }

    private void assignVarExpr(VarExpr lhs, Register rReg) {
        try (Register lReg = V.text.getVarExprAddress(lhs)) {
            VarDecl decl = lhs.vd;

            storeValue(rReg, decl.varType, lReg, 0);
        }
    }

    @Override
    public Void visitAssign(Assign a) {
        writer.comment("$? = %s", a.rhs);
        try (Register rReg = a.rhs.accept(V.text)) {

            if (a.lhs instanceof VarExpr) {
                assignVarExpr((VarExpr) a.lhs, rReg);
            } else if (a.lhs instanceof ArrayAccessExpr) {
                ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) a.lhs;

                writer.leadNewline().comment("$? = %s", arrayAccessExpr.expr);
                try (Register pointer = arrayAccessExpr.expr.accept(V.text)) {

                    writer.leadNewline().comment("%s = addressOf(%s)", pointer, a.lhs);
                    int size = arrayAccessExpr.getInnerType().sizeof();
                    try (Register index = arrayAccessExpr.index.accept(V.text)) {
                        index.mul(size);
                        pointer.add(index);
                    }

                    storeValue(rReg, a.rhs.type, pointer, 0);
                }

            } else {
                // todo
                throw new RuntimeException("structs, pointers, etc etc not implemented yet");
            }

        }

        return null;
    }
}
