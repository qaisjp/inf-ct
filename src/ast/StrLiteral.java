package ast;

public class StrLiteral extends Expr {
    public final String innerType;

    public StrLiteral(String innerType) {
        this.innerType = innerType;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }

    @Override
    public String toString() {
        return "\"...\"";
    }
}
