package gen;

import ast.*;

public class FunctionVisitor extends TraverseVisitor<Register> {
    IndentWriter writer;

    public FunctionVisitor() {
        this.writer = V.writer;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            return f.accept(V.inbuilt);
        }

        super.visitFunCallExpr(f);
        writer.comment("stub! %s", f); // todo
        Register result = V.registers.get();
        return result;
    }

    private Labeller funcLabeller = new Labeller("func");
    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isInbuilt) {
            return null;
        }

        // Our stack pointer is just to tell us where to allocate space next.
        // Set frame pointer to the stack pointer so we know where to look for our function's data
        Register.fp.set(Register.sp);

        // Store arguments on stack
        ;

        // Store return value on stack
        ;

        // Store variable declarations on stack
        ;

        String label = funcLabeller.label(f.name);
        writer.leadNewline().withLabel(label).comment("%s", f);

        writer.suppressNextNewline();
        try (IndentWriter scope = writer.scope()) {
            // If this is the main function, we need a second labels!
            if (f.name.equals("main")) {
                writer.withLabel("main").printf(".globl main");
            }

            // do some stuff (todo)
            super.visitFunDecl(f);
        }

        // Restore stack pointer to the frame pointer
        Register.sp.set(Register.fp);

        // Jump to $ra
        Register.ra.jump();

        return null; // no register returned for function declarations
    }
}
