package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {
    public final StructType structType;
    public final List<VarDecl> varDeclList;

    public StructTypeDecl(StructType structType, List<VarDecl> varDeclList) {
        this.structType = structType;
        this.varDeclList = varDeclList;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
