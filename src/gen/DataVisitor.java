package gen;

import ast.*;

import java.util.List;

public class DataVisitor extends TraverseVisitor<Void> {
    private IndentWriter writer;
    private Labeller strLabeller = new Labeller("str");
    private Labeller chrLabeller = new Labeller("chr");
    private Labeller globalLabeller = new Labeller("g");

    public DataVisitor(IndentWriter writer) {
        this.writer = writer;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.printf(".data");
        writer.newline();

        try (IndentWriter scope = writer.scope()) {
            visitEach(p.structTypeDecls);
            for (VarDecl v : p.varDecls) {
                visitVarDeclGlobal(v);
            }
            visitEach(p.funDecls);
        }

        assert writer.getLevel() == 0;
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral s) {
        super.visitStrLiteral(s);

        s.genLabel = strLabeller.makeLabel();
        writer.withLabel(s.genLabel).dataAsciiNullTerminated(s.escapedString());
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral c) {
        super.visitChrLiteral(c);

        c.genLabel = chrLabeller.makeLabel();
        writer.withLabel(c.genLabel).dataByte(c.value);
        return null;
    }

    public void visitStructDeclGlobal(VarDecl varDecl, StructType structType) {
        List<VarDecl> varDeclList = structType.decl.varDeclList;
        String varName = varDecl.varName;

        // Comment the list of declarations
        writer.leadNewline().comment("'%s' (struct %s) [size %d]", varName, structType.str, structType.sizeof());

        // Prep varName to be a prefix
        varName += "_";

        try (IndentWriter scope = writer.scope()) {
            for (VarDecl v : varDeclList) {
                String label = globalLabeller.makeLabel(varName + v.varName);
                // v.setGlobalLabel(label); // we can't use this. each `v` is global to all declarations of this struct
                varDecl.setStructFieldLabel(v.varName, label);

                writer.withLabel(label).dataSpace(GenUtils.byteAlign(v.varType.sizeof()));
            }
        }

        writer.newline();
    }

    public void visitVarDeclGlobal(VarDecl varDecl) {
        super.visitVarDecl(varDecl);

        if (varDecl.varType instanceof StructType) {
            visitStructDeclGlobal(varDecl, (StructType) varDecl.varType);
            return;
        }

        String label = globalLabeller.makeLabel(varDecl.varName);
        varDecl.setGlobalLabel(label);

        int size = varDecl.varType.sizeof();
        writer.withLabel(label).dataSpace(size);
    }
}
