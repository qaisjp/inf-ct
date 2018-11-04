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
            // todo: scratch
            System.out.println("IS INBUILT");
            writer.printf("-- %s\n", f);
            writer.printf("\tli\t$v0, 1\n");
            writer.printf("\tli\t$a0, %d\n", ((IntLiteral) f.exprList.get(0)).value);
            writer.printf("\tsyscall\n");
            return null; // todo fix this
        }
        System.out.println("NOT INBUILT");
        return null;
    }
}
