package gen;

import ast.*;

import java.util.List;

public class FunctionVisitor extends TraverseVisitor<Register> {
    private IndentWriter writer;
    private Labeller funcLabeller = new Labeller("func");
    private int frameOffset = 0;

    public FunctionVisitor() {
        this.writer = V.writer;
    }

    // Store to stack (does not allocate)
//    private void storeToStack(List<VarDecl> varDecls) {
//        int size = 0;
//
//        for (VarDecl v : varDecls) {
//            if
//        }
//    }

    public void stackAllocate(List<VarDecl> varDecls) {
        int totalSize = 0;
        for (VarDecl v : varDecls) {
            v.setGenStackOffset(frameOffset);

            int size = GenUtils.byteAlign(v.varType.sizeof());
            frameOffset += size;
            totalSize += size;
        }

        // Allocate all that on that stack
        Register.sp.sub(-totalSize);
    }

    @Override
    public Register visitBlock(Block b) {
        stackAllocate(b.varDecls);
        visitEach(V.text, b.stmtList);

        return null; // todo: should a block return?
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            return f.accept(V.inbuilt);
        }

        // Store arguments on stack
        visitEach(V.text, f.exprList); // todo

        // Store return value on stack
        ;

        // Update stack pointer
        ;

        writer.comment("stub! %s", f); // todo
        Register result = V.registers.get();
        return result;
    }

    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isInbuilt) {
            return null;
        }

        String label = funcLabeller.label(f.name);
        writer.withLabel(label).comment("%s", f);

        try (IndentWriter scope = writer.scope()) {
            //
            // DO NOT PUT ANYTHING ABOVE THIS SECTION INSIDE THIS SCOPE
            //
            // If this is the main function, we need a second labels!
            if (f.name.equals("main")) {
                writer.withLabel("main").printf(".globl main");
                writer.newline();
            }

            // Our stack pointer is just to tell us where to allocate space next.
            // We already have arguments and the return value allocated.
            // Set frame pointer to the stack pointer so we know where to look for our function's data
            Register.fp.set(Register.sp);

            // Store variable declarations on stack
            ;

            // Adjust stack pointer for those variable declarations
            ;

            // do some stuff (todo)
            super.visitFunDecl(f);

            // Restore stack pointer to the frame pointer
            Register.sp.set(Register.fp);

            // Jump to $ra todo
//            Register.ra.jump();
        }

        return null; // no register returned for function declarations
    }
}
