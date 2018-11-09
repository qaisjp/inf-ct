package ast;

import lexer.Token;

public enum BaseType implements Type {
    INT, CHAR, VOID;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }

    public static BaseType fromTokenClass(Token.TokenClass t) {
        if (t == Token.TokenClass.INT) {
            return INT;
        } else if (t == Token.TokenClass.CHAR) {
            return CHAR;
        } else if (t == Token.TokenClass.VOID) {
            return VOID;
        }

        return null;
    }

    @Override
    public int sizeof() {
        switch (this) {
            case INT:
                return 4;
            case CHAR:
                return 1;
            case VOID:
                return 0;
            default:
                throw new RuntimeException("Sizeof called on " + this.toString());
        }
    }
}
