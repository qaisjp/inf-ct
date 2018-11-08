package gen;

import ast.BinOp;
import ast.Expr;
import ast.Op;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOpVisitor extends TraverseVisitor<Register> {
    private static IndentWriter writer;
    private static Map<Op, BiFunction<Register,Register,Register>> biFunctions = null;

    private static Labeller labeller = new Labeller("binop");

    public BinOpVisitor() {
        if (biFunctions != null) {
            return;
        }

        BinOpVisitor.writer = V.writer;

        // Pour biFunctions
        biFunctions = new HashMap<>();
        biFunctions.put(Op.ADD, BinOpVisitor::add);
        biFunctions.put(Op.SUB, BinOpVisitor::sub);
        biFunctions.put(Op.EQ, BinOpVisitor::eq);
        biFunctions.put(Op.NE, BinOpVisitor::ne);
        biFunctions.put(Op.MUL, BinOpVisitor::mul);
        biFunctions.put(Op.MOD, BinOpVisitor::mod);
        biFunctions.put(Op.DIV, BinOpVisitor::div);
        // todo
//        functions.put(Op.LT, BinOpVisitor::LT);
//        functions.put(Op.GT, BinOpVisitor::GT);
//        functions.put(Op.LE, BinOpVisitor::LE);
//        functions.put(Op.GE, BinOpVisitor::GE);
    }

    private static Register add(Register x, Register y) {
        Register result = V.registers.get();
        writer.add(result, x, y);
        return result;
    }

    private static Register sub(Register x, Register y) {
        Register result = V.registers.get();
        writer.sub(result, x, y);
        return result;
    }

    private static Register eq(Register x, Register y) {
        Register result = V.registers.get();
        writer.seq(result, x, y);
        return result;
    }

    private static Register ne(Register x, Register y) {
        Register result = V.registers.get();
        writer.sne(result, x, y);
        return result;
    }

    // todo: check if this a legal boi because of hi lo stuff
    // andrius says it is legal
    private static Register mul(Register x, Register y) {
        Register result = V.registers.get();
        writer.mul(result, x, y);
        return result;
    }

    // todo: check if this legal
    // andrius says it is legal
    private static Register mod(Register num, Register dividedBy) {
        Register result = V.registers.get();
        writer.div(num, dividedBy);
        writer.mfhi(result);
        return result;
    }

    // todo check if this is legal because of hi low stuff
    // andrius says it is legal
    private static Register div(Register num, Register dividedBy) {
        Register result = V.registers.get();
        writer.div(num, dividedBy);
        writer.mflo(result);
        return result;
    }

    // todo: needs testing
    private static Register and(Register x, Expr yExpr) {
        // Generate a result register
        Register result = V.registers.get();

        // Generate a "false", "true", "end" label ahead of time
        String falseLabel = labeller.num("and_false");
        String trueLabel = labeller.num("and_true");
        String finishLabel = labeller.num("and_finish");

        // Plan:
        // - jump to FALSE if X fails, otherwise continue (jump to CHECK_Y)
        // - CHECK_Y: jump to TRUE if Y success, otherwise continue (jump to FALSE)
        // - FALSE  : set result to 0, then finish (jump to FINISH)
        // - TRUE   : set result to 1
        // - FINISH : return the result

        // Jump to FALSE if X is zero
        writer.beqz(x, falseLabel);

        // Jump to TRUE if Y success
        try (Register y = yExpr.accept(V.text)) {
            // If y is greater than zero, we want to skip to the true label
            writer.bgtz(y, trueLabel);
        }

        // FALSE: Set result to 0, jump to finish
        writer.withLabel(falseLabel).li(result, 0);
        writer.j(finishLabel);

        // TRUE : Set result to 1
        writer.withLabel(trueLabel).li(result, 1);

        // Emit finish label
        writer.withLabel(finishLabel).nop();

        return result;
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        writer.comment("%s", binOp);

        if (binOp.op == Op.AND) {
            writer.comment("%s", binOp);
            try (
                    Register x = binOp.x.accept(V.text);
                    IndentWriter scope = writer.scope()
            ) {
                return and(x, binOp.y);
            }
//        } else if (binOp.op == Op.OR) {
//            try (Register x = binOp.x.accept(V.text)) {
//                or(x);
//            }
        } else if (biFunctions.containsKey(binOp.op)) {
            try (
                    Register x = binOp.x.accept(V.text);
                    Register y = binOp.y.accept(V.text)
            ) {
                return biFunctions.get(binOp.op).apply(x, y);
            }
        }

        throw new RuntimeException("unsupported operation");
    }
}
