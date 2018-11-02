package gen;

import ast.*;

public class DataVisitor extends TraverseVisitor<Void> {
    private IndentWriter writer;
    private Labeller strLabeller = new Labeller("str");

    public DataVisitor(IndentWriter writer) {
        this.writer = writer;
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
        s.genLabel = strLabeller.makeLabel();
        writer.printf("%s: .asciiz \"%s\"", s.genLabel, s.escapedString());
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        return null;
    }
}
