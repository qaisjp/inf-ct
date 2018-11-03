package gen;

import ast.*;

import java.util.List;

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
        writer.newline();
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
        writer.withLabel(s.genLabel).dataAsciiNullTerminated(s.escapedString());
        return null;
    }

    public void visitStructDeclGlobal(String varName, StructType structType) {
        List<VarDecl> varDeclList = structType.decl.varDeclList;

        // Comment the list of declarations
        writer.leadNewline().comment("'%s' (struct %s) [size %d]", varName, structType.str, structType.sizeof());

        // Prep varName to be a prefix
        varName += "_";

        writer.push();
        for (VarDecl v : varDeclList) {
            String label = globalLabeller.makeLabel(varName + v.varName);
            v.setGlobalLabel(label);

            writer.withLabel(label).dataSpace(GenUtils.byteAlign(v.varType.sizeof()));
        }
        writer.pop();

        writer.newline();
    }

    public void visitVarDeclGlobal(VarDecl varDecl) {
        super.visitVarDecl(varDecl);

        if (varDecl.varType instanceof StructType) {
            visitStructDeclGlobal(varDecl.varName, (StructType) varDecl.varType);
            return;
        }

        String label = globalLabeller.makeLabel(varDecl.varName);
        varDecl.setGlobalLabel(label);

        int size = varDecl.varType.sizeof();
        writer.withLabel(label).dataSpace(size);
    }
}
