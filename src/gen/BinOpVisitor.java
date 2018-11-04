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

    @Override
    public Register visitBinOp(BinOp binOp) {
        if (binOp.op != Op.ADD) {
            throw new RuntimeException("unsupported operation"); // todo
        }

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
                default:
                    throw new RuntimeException("unsupported operation");
            }
        }

        return result;
    }
}
