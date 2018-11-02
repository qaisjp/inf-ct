package gen;

import ast.*;

public class DataVisitor extends TraverseVisitor<Void> {
    private IndentWriter writer;
    private Labeller strLabeller = new Labeller("str");
    private Labeller globalLabeller = new Labeller("g");

    public DataVisitor(IndentWriter writer) {
        this.writer = writer;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.printf(".data");
        writer.push();

        visitEach(p.structTypeDecls);
        for (VarDecl v : p.varDecls) {
            visitVarDeclGlobal(v);
        }
        visitEach(p.funDecls);

        writer.pop();

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral s) {
        super.visitStrLiteral(s);

        s.genLabel = strLabeller.makeLabel();
        writer.printf("%s: .asciiz \"%s\"", s.genLabel, s.escapedString());
        return null;
    }

    public Void visitVarDeclGlobal(VarDecl varDecl) {
        super.visitVarDecl(varDecl);

        int size;
        if (varDecl.varType instanceof ChrLiteral) {
            size = 1;
        } else if (varDecl.varType instanceof IntLiteral) {
            size = 4;
        } else if (varDecl.varType instanceof PointerType) {
            size = 4;
        } else {
            throw new RuntimeException("Encountered unknown variable type in global variable declaration " + varDecl.varType.toString());
        }

        writer.printf("%s: .space %d", globalLabeller.makeLabel(varDecl.varName), size);
        return null;
    }
}
