package gen;

import ast.BinOp;
import ast.Op;

public class BinOpVisitor extends TraverseVisitor<Register> {
    IndentWriter writer;

    public BinOpVisitor() {
        this.writer = V.writer;
    }

    private Register add(Register x, Register y) {
        Register result = V.registers.get();
        writer.add(result, x, y);
        return result;
    }

    private Register sub(Register x, Register y) {
        Register result = V.registers.get();
        writer.sub(result, x, y);
        return result;
    }

    private Register eq(Register x, Register y) {
        Register result = V.registers.get();
        writer.seq(result, x, y);
        return result;
    }

    private Register ne(Register x, Register y) {
        Register result = V.registers.get();
        writer.sne(result, x, y);
        return result;
    }

    // todo: check if this a legal boi because of hi lo stuff
    private Register mul(Register x, Register y) {
        Register result = V.registers.get();
        writer.mul(result, x, y);
        return result;
    }

    // todo: check if this legal
    private Register mod(Register num, Register dividedBy) {
        Register result = V.registers.get();
        writer.div(num, dividedBy);
        writer.mfhi(result);
        return result;
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        writer.leadNewline().comment("%s", binOp);

        Register result;

        try (
            Register x = binOp.x.accept(V.text);
            Register y = binOp.y.accept(V.text)
        ) {
            switch (binOp.op) {
                case ADD:
                    result = add(x, y);
                    break;
                case SUB:
                    result = sub(x, y);
                    break;
                case EQ:
                    result = eq(x, y);
                    break;
                case NE:
                    result = ne(x, y);
                    break;
                case MUL:
                    result = mul(x, y);
                    break;
                case MOD:
                    result = mod(x, y);
                    break;

                // todo
                case DIV:
                case AND:
                case OR:
                case LT:
                case GT:
                case LE:
                case GE:
                default:
                    throw new RuntimeException("unsupported operation");
            }
        }

        return result;
    }
}
