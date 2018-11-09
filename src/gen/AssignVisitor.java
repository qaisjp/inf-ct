package gen;

import ast.*;

public class AssignVisitor extends TraverseVisitor<Void> {
    IndentWriter writer;

    public AssignVisitor() {
        this.writer = V.writer;
    }

    // store
    public void storeValue(Register sourceValue, Type type, Register targetAddress, int offset) {
        writer.comment("(%s + %d) = valueOf(%s, %s)", targetAddress, offset, sourceValue, type);
        if (type == BaseType.CHAR) {
            sourceValue.storeByteAt(targetAddress, offset);
        } else if (type == BaseType.INT || type instanceof PointerType) {
            sourceValue.storeWordAt(targetAddress, offset);
        } else if (type instanceof StructType) {

            // Note, sourceValue is actually referring to the struct's address
            // Don't forget this!

            try (IndentWriter scope = writer.scope()) {
                StructTypeDecl struct = ((StructType) type).decl;

                int totalSize = 0;
                for (VarDecl v : struct.varDeclList) {
                    // Read the value at the struct address (which may or may not have been incremented)
                    try (Register innerSourceValue = V.text.getValue(sourceValue, v.varType)) {
                        storeValue(innerSourceValue, v.varType, targetAddress, offset);
                    }

                    // Increment our read offset and struct address by the size we've just read
                    int size = GenUtils.wordAlign(v.varType.sizeof());
                    sourceValue.add(size);
                    offset += size;
                    totalSize += size;
                }

                // Restore sourceValue to original address
                sourceValue.sub(totalSize);
            }
        } else {
            // todo
            throw new RuntimeException(
                    "STUB! storeValue hasn't been implemented yet for type: " + type.toString());
        }
    }

    @Override
    public Void visitAssign(Assign a) {
        try (
                Register pointer = V.text.addressOf(a.lhs);
                Register value = a.rhs.accept(V.text)
        ) {
            storeValue(value, a.rhs.type, pointer, 0);
        }

        return null;
    }
}
