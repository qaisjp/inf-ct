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
            InbuiltVisitor.inbuilts.put("print_s", InbuiltVisitor::print_s);
            InbuiltVisitor.inbuilts.put("read_i", InbuiltVisitor::read_i);
            InbuiltVisitor.inbuilts.put("mcmalloc", InbuiltVisitor::mcmalloc);
            InbuiltVisitor.inbuilts.put("print_c", InbuiltVisitor::print_c);
            InbuiltVisitor.inbuilts.put("read_c", InbuiltVisitor::read_c);

            if (System.getenv("DEBUG") != null) {
                InbuiltVisitor.inbuilts.put("print_address", InbuiltVisitor::print_i);
                InbuiltVisitor.inbuilts.put("get_register", InbuiltVisitor::get_register);
            }
        }
    }

    private static Register get_register(FunDecl f, List<Expr> args) {
        int reg = ((IntLiteral) args.get(0)).value;
        Register val = registers.get();
        writer.comment("get_register %d", reg);
        writer.printf("move %s, $%d", val, reg);
        return val;
    }

    private static Register print_i(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (IndentWriter scope = writer.scope()) {
            if (arg instanceof IntLiteral) {
                // this is left here for efficiency. removing this will just make an extra intermediary register.
                V.writer.li(Register.arg[0], ((IntLiteral) arg).value);
            } else {
                try (Register val = arg.accept(V.text)) {
                    writer.comment("$a0 = %s", val);
                    Register.arg[0].set(val);
                }
            }
        }

        writer.comment("print_i($a0)");
        Register.v0.loadImmediate(1);
        V.writer.syscall();

        return null;
    }

    private static Register print_s(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (IndentWriter scope = writer.scope();
             Register val = arg.accept(V.text)
        ) {
            writer.comment("$a0 = %s", val);
            Register.arg[0].set(val);
        }

        writer.comment("print_s($a0)");
        Register.v0.loadImmediate(4);
        writer.syscall();
        return null;
    }

    private static Register read_i(FunDecl f, List<Expr> args) {
        writer.comment("$v0 = read_i()");
        // Call syscall 5 - this sets the read integer to v0
        Register.v0.loadImmediate(5);
        writer.syscall();

        Register value = V.registers.get();
        Register.v0.moveTo(value);
        return value;
    }

    private static Register mcmalloc(FunDecl f, List<Expr> args) {
        // Get the bytes required to allocate
        Expr arg = args.get(0);
        writer.comment("$a0 = %s", arg);
        try (
            IndentWriter scope = writer.scope();
            Register byteCount = arg.accept(V.text)
        ) {
            // Set the argument of the syscall to these bytes
            Register.arg[0].set(byteCount);
        }

        // Call syscall 9 - this puts the address in v0
        writer.comment("mcmalloc($a0)");
        Register.v0.loadImmediate(9);
        writer.syscall();

        Register value = V.registers.get();
        Register.v0.moveTo(value);
        return value;
    }

    private static Register print_c(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);

        writer.comment("$a0 = %s", arg);
        try (IndentWriter scope = writer.scope()) {
            if (arg instanceof ChrLiteral) {
                // this is left here for efficiency. removing this will just make an extra intermediary register.
                V.writer.li(Register.arg[0], ((ChrLiteral) arg).value);
            } else {
                try (Register val = arg.accept(V.text)) {
                    writer.comment("$a0 = %s", val);
                    Register.arg[0].set(val);
                }
            }
        }

        writer.comment("print_c($a0)");
        Register.v0.loadImmediate(11);
        V.writer.syscall();

        return null;
    }

    private static Register read_c(FunDecl f, List<Expr> args) {
        // Call syscall 12 - this sets the read character to v0
        writer.comment("$v0 = read_c()");
        Register.v0.loadImmediate(12);
        writer.syscall();

        Register value = V.registers.get();
        writer.comment("%s = $v0", value);
        Register.v0.moveTo(value);
        return value;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (!f.decl.isInbuilt) {
            return null;
        }

        writer.comment("%s", f);

        try (IndentWriter scope = writer.scope()) {
            if (!inbuilts.containsKey(f.decl.name)) {
                throw new RuntimeException("attempt to call undefined inbuilt " + f.decl.name);
            }

            return inbuilts.get(f.decl.name).apply(f.decl, f.exprList);
        }
    }
}
