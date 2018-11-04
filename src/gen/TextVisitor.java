package gen;

import ast.BinOp;
import ast.FunCallExpr;
import ast.Program;

public class TextVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;

    public TextVisitor() {
        this.writer = V.writer;
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
            V.inbuilt.visitFunCallExpr(f);
            return null; // todo
        }

        return null; // todo
    }

    @Override
    public Register visitBinOp(BinOp binOp) {
        return binOp.accept(V.binOp);
    }
}
