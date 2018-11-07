package gen;

import ast.*;

public class AssignVisitor extends TraverseVisitor<Void> {
    IndentWriter writer;

    public AssignVisitor() {
        this.writer = V.writer;
    }

    // store
    private void storeValue(Register sourceValue, Type type, Register targetAddress, int offset) {
        writer.comment("(%s + %d) = valueOf(%s, %s)", targetAddress, offset, sourceValue, type);
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

    @Override
    public Void visitAssign(Assign a) {

        if (a.lhs instanceof VarExpr) {
            try (
                    Register pointer = V.text.addressOf((VarExpr) a.lhs);
                    Register value = a.rhs.accept(V.text)
            ) {
                storeValue(value, a.rhs.type, pointer, 0);
            }
        } else if (a.lhs instanceof ArrayAccessExpr) {
            try (
                    Register pointer = V.text.addressOf((ArrayAccessExpr) a.lhs);
                    Register value = a.rhs.accept(V.text)
            ) {
                storeValue(value, a.rhs.type, pointer, 0);
            }

        } else {
            // todo
            throw new RuntimeException("structs, pointers, etc etc not implemented yet");
        }

        return null;
    }
}
