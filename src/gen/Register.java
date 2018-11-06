package gen;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cdubach
 */
public class Register implements java.lang.AutoCloseable {


    /*
     * definition of registers
     */

    public static final Register zero = new Register(0, "zero");
    public static final Register v0 = new Register(2,"v0");
    public static final Register[] arg = {
            new Register(4,"a0"),
            new Register(5,"a1"),
            new Register(6,"a2"),
            new Register(7,"a3")};

    public static final List<Register> tmp = new ArrayList<Register>();
    static {
        for (int i=8; i<=15; i++)
            tmp.add(new Register(i,"t"+(i-8)));
        for (int i=16; i<=23; i++)
            tmp.add(new Register(i,"s"+(i-16)));
        for (int i=24; i<=25; i++)
            tmp.add(new Register(i,"t"+(i-24+8)));
    }

    public static final Register gp = new Register(28,"gp");
    public static final Register sp = new Register(29,"sp");
    public static final Register fp = new Register(30,"fp");
    public static final Register ra = new Register(31,"ra");

    public static final Register[] unfreeable = {
            // syscall register
            v0,

            // argument registers
            arg[0], arg[1], arg[2], arg[3],

            // pointers and return address
            gp, sp, fp, ra,
    };


    private final int num;      // register number
    private final String name;  // register name


    private Register(int num, String name) {
        this.num = num;
        this.name = name;
    }

    public String toString() {
        return "$"+name;
    }

    public void loadImmediate(int i) {
        V.writer.li(this, i);
    }

    public void loadAddress(String label) {
        V.writer.la(this, label);
    }

    public void loadByte(Register from, int offset) {
        V.writer.lb(this, from, offset);
    }

    public void loadWord(Register from, int offset) {
        V.writer.lw(this, from, offset);
    }

    public void storeByteAt(Register toAddress, int offset) {
        V.writer.sb(this, toAddress, offset);
    }

    public void storeWordAt(Register toAddress, int offset) {
        V.writer.sw(this, toAddress, offset);
    }

    public void set(Register from) {
        V.writer.move(this, from);
    }

    public void moveTo(Register target) {
        V.writer.move(target, this);
    }

    public void jump() {
        V.writer.jr(this);
    }

    public void sub(int i) {
        V.writer.subi(this, this, i);
    }

    public void free() {
        for (Register r : unfreeable) {
            if (r == this) {
                throw new RuntimeException("Attempted to free non-freeable register.");
            }
        }
        V.registers.free(this);
    }

    // Allows try-with-resources to be used to free registers
    @Override
    public void close() {
        free();
    }
}
