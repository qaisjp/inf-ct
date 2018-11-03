package ast;

import gen.GenUtils;

public class StructType implements Type {
    public final String str;
    public StructTypeDecl decl; // to be filled in by the type analyser

    public StructType(String s) {
        this.str = s;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    @Override
    public String toString() {
        return "struct " + str;
    }

    @Override
    public int sizeof() {
        int size = 0;
        for (VarDecl v : decl.varDeclList) {
            size += v.varType.sizeof();
        }
        return GenUtils.byteAlign(size);
    }
}
