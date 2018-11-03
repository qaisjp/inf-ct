package ast;

public class VarDecl implements ASTNode {
    public final Type varType;
    public final String varName;
    private String genLabel = null;

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


    public void setGlobalLabel(String label) {
        if (genLabel != null) {
            throw new RuntimeException("Can't set label if already set");
        }

        genLabel = label;
    }

    public String getGlobalLabel() {
        if (genLabel == null) {
            throw new NullPointerException("Can't get global label if not a global label (genLabel is null)");
        }
        return genLabel;
    }
}
