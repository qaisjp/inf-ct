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

    private void stackAllocate(List<VarDecl> varDecls) {
        writer.comment("Allocate space on stack for varDecls");
        int totalSize = 0;
        for (VarDecl v : varDecls) {
            v.setGenStackOffset(frameOffset);

            int size = GenUtils.byteAlign(v.varType.sizeof());
            frameOffset += size;
            totalSize += size;
        }

        if (totalSize == 0) {
            writer.nop();
            return;
        }

        // Allocate all that on that stack by SUBTRACTING the stack pointer
        Register.sp.sub(totalSize);
    }

    private void stackFree(List<VarDecl> varDecls) {
        writer.comment("Free space on stack from varDecls");
        int totalSize = 0;
        for (VarDecl v : varDecls) {
            int size = GenUtils.byteAlign(v.varType.sizeof());
            frameOffset -= size;
            totalSize += size;
        }

        if (totalSize == 0) {
            writer.nop();
            return;
        }

        // Pop by adding all that to the stack pointer
        Register.sp.add(totalSize);
    }

    @Override
    public Register visitBlock(Block b) {
        stackAllocate(b.varDecls);
        visitEach(V.text, b.stmtList);
        stackFree(b.varDecls);

        // todo: don't forget that a block can complete jump to the end of a function
        return null;
    }

    private final static int PrologueSize = 4 * 31; // save 31 registers, so 31 * 4 bytes

    @Override
    public Register visitFunCallExpr(FunCallExpr f) {
        if (f.decl.isInbuilt) {
            return f.accept(V.inbuilt);
        }

        /*
        Plan for calling int foo(char bar):
        - WIND:
            - skip prologue
            - allocate and place arguments on our current function's stack frame (first argument is result address)
        - JUMP: jump to foo
        - UNWIND:
            -
        -
         */

        int oldFrameOffset;

        Register resultAddress = V.registers.get();

        writer.comment("precall");
        try (IndentWriter scope = writer.scope()) {
            // Allocate space on caller stack for callee to write to
            int resultSize = GenUtils.byteAlign(f.decl.result.sizeof());
            frameOffset += resultSize;
            Register.sp.sub(resultSize);
            writer.add(resultAddress, Register.fp, frameOffset);

            // Back up our frame offset, and reset
            oldFrameOffset = frameOffset;
            frameOffset = 0;

            // Skip the prologue size
            Register.sp.sub(PrologueSize);

            // Store a pointer to our result at the current SP
            Register.sp.sub(4);
            resultAddress.storeWordAt(Register.sp, 0);

            // Allocate space for arguments on stack
            stackAllocate(f.decl.params);

            // Iterate through args (skipping address of result)
            int argSize = 4;
            for (int i = 0; i < f.decl.params.size(); i++) {
                VarDecl decl = f.decl.params.get(i);
                Expr expr = f.exprList.get(i);
                Type type = expr.type;

                argSize += GenUtils.byteAlign(type.sizeof());

                try (Register sourceValue = expr.accept(V.text)) {
                    Register targetAddress = Register.sp;
                    int offset = decl.getGenStackOffset();
                    V.assign.storeValue(sourceValue, type, targetAddress, offset);
                }
                // todo: copy values
            }

            // Roll back the sp by PrologueSize + argSize
            Register.sp.add(PrologueSize + argSize);
        }

        writer.comment("perform jump to declaration");
        writer.jal(f.decl.genLabel);

        writer.comment("postreturn");
        try (IndentWriter scope = writer.scope()) {
//            // Pop return value from the stack
//            Register result = V.text.getValue(Register.sp, f.decl.result);

//            // Pop arguments off stack
//            stackFree(f.decl.params);

            frameOffset = oldFrameOffset;

//            return result;
            return resultAddress; // todo
        }
    }

    // Snapshot registers
    /*
        for (i=1; i <= 31; i++) {
            sp -= i * 4
            *sp = $i
        }
     */
    private void snapshotRegisters() {
        writer.comment("snapshot registers"); //todo
        try (IndentWriter scope = writer.scope()) {
            try (Register tempSp = V.registers.get()) {
                writer.sub(tempSp, Register.sp, PrologueSize);

                for (int i = 0; i < 31; i++) {
                    writer.printf("sw $%d, %d(%s)", i + 1, i * 4, tempSp);
                }

                Register.sp.set(tempSp);
            }
        }
    }

    // Restore registers
    private void restoreRegisters() {
        writer.comment("restore registers");
        try (IndentWriter scope = writer.scope()) {
            try (Register tempSp = V.registers.get()) {
                tempSp.set(Register.sp);
                for (int i = 30; i >= 0; i--) {
                    writer.printf("lw $%d, %d(%s)", i + 1, i * 4, tempSp);
                }
            }
        }
    }

    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isInbuilt) {
            return null;
        }

        f.genLabel = funcLabeller.label(f.name);
        writer.withLabel(f.genLabel).comment("%s", f);

        try (IndentWriter scope = writer.scope()) {
            //
            // DO NOT PUT ANYTHING ABOVE THIS SECTION INSIDE THIS SCOPE
            //
            // If this is the main function, we need a second labels!
//            if (f.name.equals("main")) {
//                writer.withLabel("main").directive("globl %s", "main");
//                writer.newline();
//            }

            /*
            PROLOGUE:
            - initialise the frame pointer
            - save all the temporary registers onto the stack
             */
            int argSize = 0;

            writer.comment("prologue");
            try (IndentWriter innerScope = writer.scope()) {
                // Snapshot our registers
                snapshotRegisters();

                // Jump over our parameters (as well as the initial return pointer)
                writer.comment("skip over return value & return pointer");
                for (VarDecl v : f.params) {
                    argSize += GenUtils.byteAlign(v.varType.sizeof());
                }
                Register.sp.sub(argSize + 4);

                // Our stack pointer is just to tell us where to allocate space next.
                // We already have arguments and the return value allocated.
                // Set frame pointer to the stack pointer so we know where the callee can look for our passed data
                writer.comment("reset frame pointer");
                Register.fp.set(Register.sp);

            }

            // do some stuff, todo: what about result, parameters?
            writer.comment("function contents");
            try (IndentWriter innerScope = writer.scope()) {
                visitBlock(f.block);
            }

            writer.comment("epilogue");
            try (IndentWriter innerScope = writer.scope()) {
                // Restore stack pointer
                writer.comment("Restore stack pointer");
                Register.sp.add(argSize + 4);

                // Restore registers
                restoreRegisters();

                // Jump to $ra
                Register.ra.jump();
            }
        }

        return null; // no register returned for function declarations
    }
}
