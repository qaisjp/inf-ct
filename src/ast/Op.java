package ast;

import lexer.Token;

public enum Op {
    ADD, SUB, MUL, DIV, MOD, GT, LT, GE, LE, NE, EQ, OR, AND;

    public static Op fromTokenClass(Token.TokenClass tok) {
        switch (tok) {
            case PLUS: return ADD;
            case MINUS: return SUB;
            case ASTERIX: return MUL;
            case DIV: return DIV;
            case REM: return MOD;
            case GT: return GT;
            case LT: return LT;
            case GE: return GE;
            case LE: return LE;
            case NE: return NE;
            case EQ: return EQ;
            case OR: return OR;
            case AND: return AND;
            default: return null;
        }
    }
}
