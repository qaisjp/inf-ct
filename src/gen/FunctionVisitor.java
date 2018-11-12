package gen;

import ast.*;

import java.util.Arrays;
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

            int size = GenUtils.wordAlign(v.varType.sizeof());
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
            int size = GenUtils.wordAlign(v.varType.sizeof());
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
        int oldOffset = frameOffset;
        stackAllocate(b.varDecls);
        visitEach(V.text, b.stmtList);
        stackFree(b.varDecls);
        assert frameOffset == oldOffset;

        // todo: don't forget that a block can complete jump to the end of a function
        return null;
    }

    private final static int PrologueSize = 4 * Register.snapshot.size();

    private void snapshotRegisters() {
        writer.comment("snapshot registers"); //todo
        try (IndentWriter scope = writer.scope()) {
            writer.comment("Adjust $sp for prologue");
            Register.sp.sub(PrologueSize);

            int i = 0;
            for (Register r: Register.snapshot) {
                writer.sw(r, Register.sp, i);
                i += 4;
            }
        }
    }

    // Restore registers
    private void restoreRegisters() {
        writer.comment("restore registers");
        try (IndentWriter scope = writer.scope()) {
            int i = 0;
            for (Register r: Register.snapshot) {
                writer.lw(r, Register.sp, i);
                i += 4;
            }
            Register.sp.add(PrologueSize);
        }
    }

    @Override
    public Register visitFunDecl(FunDecl f) {
        // Ignore inbuilt declarations
        if (f.isInbuilt) {
            return null;
        }

        f.genLabel = funcLabeller.label(f.name + "_start");
        writer.withLabel(f.genLabel).comment("%s", f);

        String epilogueLabel = funcLabeller.label(f.name + "_epilogue");

        try (IndentWriter scope = writer.scope()) {
            /*
            PROLOGUE:
            - initialise the frame pointer
            - save all the temporary registers onto the stack
             */
            int argSize = 0;

            writer.comment("prologue");
            try (IndentWriter innerScope = writer.scope()) {
                // Snapshot our caller's registers
                snapshotRegisters();

                // Jump over our parameters
                writer.comment("Skip over parameters: %s", Arrays.toString(f.params.toArray()));
                for (VarDecl v : f.params) {
                    argSize += GenUtils.wordAlign(v.varType.sizeof());
                }
                Register.sp.sub(argSize);

                // Our stack pointer is just to tell us where to allocate space next.
                // We already have arguments allocated.
                // Set frame pointer to the stack pointer so we know where the callee can look for our passed data
                writer.comment("reset frame pointer");
                Register.fp.set(Register.sp);

                // Set $ra to epilogue
                writer.comment("Set $ra to epilogue");
                Register.ra.loadAddress(epilogueLabel);
            }

            // do some stuff, todo: what about result, parameters?
            writer.comment("function contents");
            try (IndentWriter innerScope = writer.scope()) {
                visitBlock(f.block);
            }

            writer.withLabel(epilogueLabel).comment("epilogue");
            try (IndentWriter innerScope = writer.scope()) {
                // Restore stack pointer
                writer.comment("Restore stack pointer");
                Register.sp.add(argSize);

                // Restore registers to caller's state
                restoreRegisters();

                // Set stack pointer to caller's frame pointer
                Register.sp.set(Register.fp);

                // Jump to $ra
                Register.ra.jump();
            }
        }

        return null; // no register returned for function declarations
    }

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

        // Back up our frame offset, and reset the frame offset to 0
        int oldFrameOffset = frameOffset;
        frameOffset = 0;

        Register result = V.registers.get();
//        int resultSize = GenUtils.wordAlign(f.decl.result.sizeof());

        writer.comment("precall");
        try (IndentWriter scope = writer.scope()) {
            // Skip the prologue size
            writer.comment("Skip the prologue size (we will be writing into our callee stack frame)");
            Register.sp.sub(PrologueSize);

            // Allocate space for arguments on stack
            stackAllocate(f.decl.params);

            // Iterate through args
            int argSize = 0;
            for (int i = 0; i < f.decl.params.size(); i++) {
                VarDecl decl = f.decl.params.get(i);
                Expr expr = f.exprList.get(i);
                Type type = expr.type;

                argSize += GenUtils.wordAlign(type.sizeof());

                try (Register sourceValue = expr.accept(V.text)) {
                    Register targetAddress = Register.sp;
                    int offset = decl.getGenStackOffset();
                    V.assign.storeValue(sourceValue, type, targetAddress, offset);
                }
                // todo: copy values ?
            }

            // Roll back the sp by PrologueSize + argSize
            writer.comment("Roll back the sp by PrologueSize + argSize");
            Register.sp.add(PrologueSize + argSize);
        }

        writer.comment("perform jump to declaration");
        writer.jal(f.decl.genLabel);

        writer.comment("postreturn");
        try (IndentWriter scope = writer.scope()) {
            // Restore frame offset
            frameOffset = oldFrameOffset;

            result.set(Register.v0);
            return result;
        }
    }
}
