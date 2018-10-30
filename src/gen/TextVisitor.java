package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class TextVisitor extends TraverseVisitor<Register> {
    private PrintWriter writer; // use this writer to output the assembly instructions

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public TextVisitor(PrintWriter writer) {
        this.writer = writer;

        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
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
        writer.write(p.name + ":\n");

        // todo: scratch
        visitEach(p.params);
        p.block.accept(this);// todo: scratch

        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        // todo: empty program should generate empty asm file

        writer.write(".text\n"); // todo: scratch
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
        Register register = getRegister();
        writer.printf("li\t%s, %d", register, il.innerType);
        return register;
    }


    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            // todo: scratch
            System.out.println("IS INBUILT");
            writer.printf("-- %s\n", f);
            writer.printf("\tli\t$v0, 1\n");
            writer.printf("\tli\t$a0, %d\n", ((IntLiteral) f.exprList.get(0)).innerType);
            writer.printf("\tsyscall\n");
            return null; // todo fix this
        }
        System.out.println("NOT INBUILT");
        return null;
    }
}
