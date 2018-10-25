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

    public String toLang() {
        switch (this) {
            case GE:return ">=";
            case EQ:return "==";
            case GT:return ">";
            case LE:return "<=";
            case LT:return "<";
            case NE:return "!=";
            case OR:return "||";
            case ADD:return "+";
            case AND:return "&&";
            case DIV:return "/";
            case MOD:return "%";
            case MUL:return "*";
            case SUB:return "-";
            default:return "??";
        }
    }
}
