package ast;

public class StrLiteral extends Expr {
    public final String value;
    public String genLabel;

    public StrLiteral(String value) {
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }

    @Override
    public String toString() {
        return "\"...\"";
    }

    public String escapedString() {
        return value
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\"", "\\\"")
                .replace("\0", "\\0");
    }
}
