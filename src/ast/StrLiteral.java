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

    public String escapedString() {
        return innerType
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
