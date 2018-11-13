package gen;

import ast.BinOp;
import ast.Expr;
import ast.Op;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOpVisitor extends TraverseVisitor<Register> {
    private static IndentWriter writer;
    private static Map<Op, BiFunction<Register,Register,Register>> biFunctions = null;

    private static Labeller labeller = new Labeller("binop");
    private static Map<Op, String> comparators = new HashMap<Op, String>(){{
        comparators.put(Op.LT, "slt");
        comparators.put(Op.GT, "sgt");
        comparators.put(Op.LE, "sle");
        comparators.put(Op.GE, "sge");
        comparators.put(Op.ADD, "add");
        comparators.put(Op.SUB, "sub");
        comparators.put(Op.EQ, "seq");
        comparators.put(Op.NE, "sne");

    }};

    public BinOpVisitor() {
        if (biFunctions != null) {
            return;
        }

        BinOpVisitor.writer = V.writer;

        // Pour biFunctions
        biFunctions = new HashMap<>();
        biFunctions.put(Op.MUL, BinOpVisitor::mul);
        biFunctions.put(Op.MOD, BinOpVisitor::mod);
        biFunctions.put(Op.DIV, BinOpVisitor::div);
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
        writer.b(finishLabel);

        // TRUE : Set result to 1
        writer.withLabel(trueLabel).li(result, 1);

        // Emit finish label
        writer.withLabel(finishLabel).nop();

        return result;
    }

    private static Register or(Register x, Expr yExpr) {
        // Generate a result register
        Register result = V.registers.get();

        // Generate a "false", "true", "end" label ahead of time
        String falseLabel = labeller.num("and_false");
        String trueLabel = labeller.num("and_true");
        String finishLabel = labeller.num("and_finish");

        // Plan:
        // - jump to TRUE if X success, otherwise continue (jump to CHECK_Y)
        // - CHECK_Y: continue if Y success, otherwise jump to FALSE
        // - TRUE   : set result to 1 (jump to FINISH)
        // - FALSE  : set result to 0
        // - FINISH : return the result

        // Jump to TRUE if X success
        writer.bnez(x, trueLabel);

        // Jump to FALSE if Y fail
        try (Register y = yExpr.accept(V.text)) {
            // If y is greater than zero, we want to skip to the true label
            writer.beqz(y, falseLabel);
        }

        // TRUE : Set result to 1, jump to finish
        writer.withLabel(trueLabel).li(result, 1);
        writer.b(finishLabel);

        // FALSE: Set result to 0
        writer.withLabel(falseLabel).li(result, 0);

        // Emit finish label
        writer.withLabel(finishLabel).nop();

        return result;
    }

    private static Register compare(Register x, Register y, String operator) {
        Register result = V.registers.get();
        writer.printf("%s %s, %s, %s", operator, operator, result, x, y);
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
        } else if (binOp.op == Op.OR) {
            writer.comment("%s", binOp);
            try (
                    Register x = binOp.x.accept(V.text);
                    IndentWriter scope = writer.scope()
            ) {
                return or(x, binOp.y);
            }
        } else if (biFunctions.containsKey(binOp.op)) {
            try (
                    Register x = binOp.x.accept(V.text);
                    Register y = binOp.y.accept(V.text)
            ) {
                return biFunctions.get(binOp.op).apply(x, y);
            }
        } else if (comparators.containsKey(binOp.op)) {
            try (
                Register x = binOp.x.accept(V.text);
                Register y = binOp.y.accept(V.text)
            ) {
                return BinOpVisitor.compare(x, y, comparators.get(binOp.op));
            }
        }

        throw new RuntimeException("unsupported operation");
    }
}
