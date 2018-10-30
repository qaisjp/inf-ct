package gen;

import ast.*;

import java.io.PrintWriter;

public class DataVisitor extends TraverseVisitor<Void> {
    private IndentWriter writer;

    public DataVisitor(PrintWriter writer) {
        this.writer = new IndentWriter(writer);
    }

    @Override
    public Void visitProgram(Program p) {
        writer.printf(".data");
        writer.push();
        super.visitProgram(p);
        writer.pop();

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral s) {
        writer.printf(".asciiz \"%s\"", s.escapedString());
        return null;
    }
}
