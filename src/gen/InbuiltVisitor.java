package gen;

import ast.*;

import java.util.ArrayList;
import java.util.List;

public class InbuiltVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer; // use this writer to output the assembly instructions
    private Registers registers;

    public InbuiltVisitor() {
        this.writer = V.writer;
        this.registers = V.registers;
    }

    private void print_i(FunDecl f, List<Expr> args) {
        Expr arg = args.get(0);
        if (!(arg instanceof IntLiteral)) {
            writer.comment("stub: %s", f); // todo
            return;
        }

        writer.leadNewline().comment("%s", f);

        writer.li(Register.v0, 1);
        writer.li(Register.paramRegs[0], ((IntLiteral) arg).value);
        writer.syscall();
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (!f.decl.isInbuilt) {
            return null;
        }

        switch (f.decl.name) {
            case "print_i":
                print_i(f.decl, f.exprList);
                return null;
        }

        writer.comment("stub: %s", f); // todo: replace with RuntimeException

        return null; // todo fix this
    }
}
