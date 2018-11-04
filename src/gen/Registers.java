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
        freeRegs.addAll(Register.tmp);
    }

    public class RegisterAllocationError extends Error {}

    public Register get() {
        try {
            Register reg = freeRegs.pop();
//            System.out.printf("Getting %s\n", reg);
            return reg;
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    public void free(Register reg) {
        freeRegs.push(reg);
//        System.out.printf("Freeing %s\n", reg);
    }
}
