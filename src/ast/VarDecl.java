package ast;

public class VarDecl implements ASTNode {
    public final Type varType;
    public final String varName;

    public VarDecl(Type varType, String varName) {
	    this.varType = varType;
	    this.varName = varName;
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitVarDecl(this);
    }

    @Override
    public String toString() {
        return varType.toString() + " " + varName;
    }
}
