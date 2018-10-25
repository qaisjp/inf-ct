package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
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

    private PrintWriter writer; // use this writer to output the assembly instructions

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        visitProgram(program);
        writer.close();
    }

    public List<Register> visitEach(List<? extends ASTNode> list) {
        List<Register> results = new ArrayList<>();
        for (ASTNode l : list) {
            results.add(l.accept(this));
        }

        return results;
    }

    @Override
    public Register visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        return null;
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

        writer.write(".data\n"); // todo: scratch
//        visitEach(p.structTypeDecls);
//        visitEach(p.varDecls);

        writer.write(".text\n"); // todo: scratch
        visitEach(p.funDecls); // todo: scratch
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt e) {
        e.expr.accept(this); // todo: scratch
        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        return null;
    }

    @Override
    public Register visitIf(If i) {
        return null;
    }

    @Override
    public Register visitWhile(While w) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public Register visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        return null;
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

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr soe) {
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        return null;
    }
}
