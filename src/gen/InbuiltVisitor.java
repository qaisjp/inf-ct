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

    public List<Register> visitEach(List<? extends ASTNode> list) {
        List<Register> results = new ArrayList<>();
        for (ASTNode l : list) {
            results.add(l.accept(this));
        }

        return results;
    }

    @Override
    public Register visitBlock(Block b) {
        visitEach(b.varDecls); // todo: scratch
        visitEach(b.stmtList);// todo: scratch
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        // TODO: check if return null is ok
        if (p.isInbuilt) { return null; }

        // todo: scratch
        writer.printf(p.name + ":\n");

        // todo: scratch
        visitEach(p.params);
        p.block.accept(this);// todo: scratch

        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        // todo: empty program should generate empty asm file

        writer.printf(".text\n"); // todo: scratch
        visitEach(p.funDecls); // todo: scratch
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt e) {
        e.expr.accept(this); // todo: scratch
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        Register register = registers.get();
        writer.printf("li\t%s, %d", register, il.value);
        return register;
    }


    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
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
