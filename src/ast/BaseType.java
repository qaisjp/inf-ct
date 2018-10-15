package ast;

import lexer.Token;

public enum BaseType implements Type {
    INT, CHAR, VOID;

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }

    public static BaseType fromTokenClass(TokenClass t) {
        if (t == Token.TokenClass.INT) {
            return INT;
        } else if (t == Token.TokenClass.CHAR) {
            return CHAR;
        } else if (t == Token.TokenClass.VOID) {
            return VOID;
        }

        return null;
    }
}
