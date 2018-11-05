package gen;

import ast.BinOp;
import ast.Op;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinOpVisitor extends TraverseVisitor<Register> {
    private static IndentWriter writer;
    private static Map<Op, Function<Register,Register>> functions = null;
    private static Map<Op, BiFunction<Register,Register,Register>> biFunctions = null;

    public BinOpVisitor() {
        if (biFunctions != null) {
            return;
        }

        BinOpVisitor.writer = V.writer;

        // Pour functions
        functions = new HashMap<>();
        // todo
//        functions.put(Op.AND, BinOpVisitor::AND);
//        functions.put(Op.OR, BinOpVisitor::OR);
//        functions.put(Op.LT, BinOpVisitor::LT);
//        functions.put(Op.GT, BinOpVisitor::GT);
//        functions.put(Op.LE, BinOpVisitor::LE);
//        functions.put(Op.GE, BinOpVisitor::GE);

        // Pour biFunctions
        biFunctions = new HashMap<>();
        biFunctions.put(Op.ADD, BinOpVisitor::add);
        biFunctions.put(Op.SUB, BinOpVisitor::sub);
        biFunctions.put(Op.EQ, BinOpVisitor::eq);
        biFunctions.put(Op.NE, BinOpVisitor::ne);
        biFunctions.put(Op.MUL, BinOpVisitor::mul);
        biFunctions.put(Op.MOD, BinOpVisitor::mod);
        biFunctions.put(Op.DIV, BinOpVisitor::div);
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

    @Override
    public Register visitBinOp(BinOp binOp) {
        writer.leadNewline().comment("%s", binOp);

        if (functions.containsKey(binOp.op)) {
            try (Register x = binOp.x.accept(V.text)) {
                return functions.get(binOp.op).apply(x);
            }
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
