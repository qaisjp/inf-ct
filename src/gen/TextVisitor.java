package gen;

import ast.FunCallExpr;
import ast.Program;

public class TextVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;
    private Registers registers;

    private InbuiltVisitor inbuiltVisitor;

    public TextVisitor(IndentWriter writer, Registers registers) {
        this.writer = writer;
        this.registers = registers;

        inbuiltVisitor = new InbuiltVisitor(writer, registers);
    }

    @Override
    public Register visitProgram(Program p) {
        writer.leadNewline().printf(".text");
        writer.push();
        super.visitProgram(p);
        writer.pop();

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            inbuiltVisitor.visitFunCallExpr(f);
            return null; // todo
        }

        return null; // todo
    }
}
