package gen;

import java.util.EmptyStackException;
import java.util.Stack;

/*
* Simple register allocator.
*/
public class Registers {
    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public Registers() {
        freeRegs.addAll(Register.tmpRegs);
    }

    public class RegisterAllocationError extends Error {}

    public Register get() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    public void free(Register reg) {
        freeRegs.push(reg);
    }
}
