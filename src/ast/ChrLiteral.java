package ast;

public class ChrLiteral extends Expr {
    public final char value;
    public String genLabel;

    public ChrLiteral(char value) {
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }

    @Override
    public String toString() {
        return "\'" + StrLiteral.escapedString(Character.toString(value)) + "\'";
    }
}
