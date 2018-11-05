package gen;

import ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

public class InbuiltVisitor extends TraverseVisitor<Register> {
    private static IndentWriter writer; // use this writer to output the assembly instructions
    private static Registers registers;
    private static HashMap<String, BiFunction<FunDecl, List<Expr>, Register>> inbuilts = null;

    /*
        1:  void print_i(int i);
        4:  void print_s(char* s);
        5:  int read_i();
        9:  void* mcmalloc(int size);
        11: void print_c(char c);
        12: char read_c();
    */
    public InbuiltVisitor() {
        InbuiltVisitor.writer = V.writer;
        InbuiltVisitor.registers = V.registers;

        if (inbuilts == null) {
            // Initialise inbuilts
            InbuiltVisitor.inbuilts = new HashMap<>();
            InbuiltVisitor.inbuilts.put("print_i", InbuiltVisitor::print_i);
        }
    }

    private static Register print_i(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);

        Register.v0.loadImmediate(1);

        if (arg instanceof IntLiteral) {
            V.writer.li(Register.arg[0], ((IntLiteral) arg).value);
        } else {
            try (Register val = arg.accept(V.text)) {
                Register.arg[0].set(val);
            }
        }

        V.writer.syscall();

        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (!f.decl.isInbuilt) {
            return null;
        }

        writer.leadNewline().comment("%s", f);

        if (!inbuilts.containsKey(f.decl.name)) {
            writer.comment("stub: %s", f);
            throw new RuntimeException("attempt to call undefined inbuilt " + f.decl.name);
        }

        return inbuilts.get(f.decl.name).apply(f.decl, f.exprList);
    }
}
