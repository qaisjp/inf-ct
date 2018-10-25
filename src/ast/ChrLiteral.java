package ast;

public class ChrLiteral extends Expr {
    public final char innerType;

    public ChrLiteral(char innerType) {
        this.innerType = innerType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

    @Override
    public String toString() {
        return "\'...\'";
    }
}
