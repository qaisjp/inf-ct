package gen;

import ast.*;

public class AssignVisitor extends TraverseVisitor<Void> {
    IndentWriter writer;

    public AssignVisitor() {
        this.writer = V.writer;
    }



    private void assignVarExpr(VarExpr lhs, Register rReg) {
        try (Register lReg = V.text.getVarExprAddress(lhs)) {
            VarDecl decl = lhs.vd;

            Type varType = decl.varType;
            if (varType == BaseType.CHAR) {
                rReg.storeByteAt(lReg, 0);
            } else if (varType == BaseType.INT || varType instanceof PointerType) {
                rReg.storeWordAt(lReg, 0);
            } else {
                // arrays and structs need SPECIAL treatment!
                // oh and strings too topKEKKER
                // todo
                throw new RuntimeException(
                        "STUB! arrays, structs (& maybe strings) haven't been implemented yet");
            }
        }
    }

    @Override
    public Void visitAssign(Assign a) {
        try (Register rReg = a.rhs.accept(V.text)) {

            if (a.lhs instanceof VarExpr) {
                assignVarExpr((VarExpr) a.lhs, rReg);
            } else {
                // todo
                throw new RuntimeException("structs, pointers, etc etc not implemented yet");
            }

        }

        return null;
    }
}
