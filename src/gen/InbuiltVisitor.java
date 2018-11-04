package gen;

import ast.*;

import java.util.List;

public class InbuiltVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer; // use this writer to output the assembly instructions
    private Registers registers;

    public InbuiltVisitor() {
        this.writer = V.writer;
        this.registers = V.registers;
    }

    private Register print_i(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);


        writer.leadNewline().comment("%s", f);

        Register.v0.loadImmediate(1);

        if (arg instanceof IntLiteral) {
            writer.li(Register.arg[0], ((IntLiteral) arg).value);
        } else {
            Register val = arg.accept(V.text);
            Register.arg[0].set(val);
            val.free();
        }

        writer.syscall();

        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (!f.decl.isInbuilt) {
            return null;
        }

        switch (f.decl.name) {
            case "print_i":
                return print_i(f.decl, f.exprList);
        }

        writer.comment("stub: %s", f); // todo: replace with RuntimeException

        return null; // todo fix this
    }
}
