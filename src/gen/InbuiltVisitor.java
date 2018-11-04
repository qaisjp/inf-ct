package gen;

import ast.*;

import java.util.ArrayList;
import java.util.List;

public class InbuiltVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer; // use this writer to output the assembly instructions
    private Registers registers;

    public InbuiltVisitor(IndentWriter writer, Registers registers) {
        this.writer = writer;
        this.registers = registers;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            if (!f.decl.name.equals("print_i")) {
                writer.comment("stub: %s", f); // todo
                return null;
            }

            writer.leadNewline().comment("%s", f);
            writer.printf("li\t$v0, 1");

            Expr arg = f.exprList.get(0);
            if (!(arg instanceof IntLiteral)) {
                writer.comment("stub: %s", f); // todo
                return null;
            }

            writer.printf("li\t$a0, %d", ((IntLiteral) arg).value);
            writer.syscall();
            return null; // todo fix this
        }
        System.out.println("NOT INBUILT");
        return null;
    }
}
