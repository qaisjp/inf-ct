package gen;

import java.io.PrintWriter;
import java.util.Arrays;

public class IndentWriter implements java.lang.AutoCloseable {
    private PrintWriter writer;
    private int level = 0;
    private static final int width = 4;

    // Magic stuff
    private String label = ""; // current label
    private boolean wasNewline = false;
    private final boolean debugWriter = System.getenv("DEBUG") != null;

    public IndentWriter(PrintWriter writer) {
        this.writer = writer;
    }

    // For try-with-resources scoping
    public IndentWriter scope() {
        wasNewline = true;
        level += 1;
        return this;
    }

    // Allows try-with-resources to be used for scoping
    @Override
    public void close() {
        wasNewline = false;
        leadNewline();
        level -= 1;
    }

    public IndentWriter withLabel(String label) {
        if (!this.label.isEmpty()) {
            throw new RuntimeException("withLabel overwritten");
        }
        this.label = label;
        return this;
    }

    public void printf(String format, Object... args) {

        String indentation = new String(new char[Math.max(0,level*width)]).replace("\0", " ");

        if (!label.isEmpty()) {
            label += ": ";
        }

        writer.printf(indentation + label + format, args);

        if (Arrays.asList(args).contains(null)) {
            writer.printf("\033[1;31m # <---- cannot print with null argument \033[0m\n");
            writer.flush();

            String message = String.format("Cannot print with null argument. Would be (see next line, between brackets):\n\n\t[" + format + "]\n\n", args);
            throw new NullPointerException(message);
        }

        writer.printf("\n");
        writer.flush();

        label = "";
        wasNewline = false;
    }

    // .space 4
    public void dataSpace(int space) {
        printf(".space %d", space);
    }

    // .asciiz "Hello, world!" # (trailing nul byte)
    public void dataAsciiNullTerminated(String s) {
        printf(".asciiz \"%s\"", s);
    }

    // .ascii "Hello," # (no trailing nul byte)
    public void dataAsciiWithoutNull(String s) {
        printf(".ascii \"%s\"", s);
    }

    // .byte 2
    public void dataByte(char c) {
        printf(".byte %d", (int) c);
    }

    // syscall
    public void syscall() {
        printf("syscall");
    }

    // load immediate: li $register, 1
    public void li(Register r, int i) {
        printf("li %s, %d", r, i);
    }

    // load address from label: la $register, some_global
    public void la(Register r, String label) {
        Labeller.verifyLabel(label);
        printf("la %s, %s", r, label);
    }

    // add
    public void add(Register value, Register x, Register y) {
        printf("add %s, %s, %s", value, x, y);
    }

    // addi
    public void add(Register value, Register x, int y) {
        printf("addi %s, %s, %d", value, x, y);
    }

    // sub: value = x - y
    public void sub(Register value, Register x, Register y) {
        printf("sub %s, %s, %s", value, x, y);
    }

    // subi: value = $x - i
    public void sub(Register value, Register x, int i) {
        printf("subi %s, %s, %d", value, x, i);
    }

    // seq: value = x == y
    public void seq(Register value, Register x, Register y) {
        printf("seq %s, %s, %s", value, x, y);
    }

    // sne: value = x != y
    public void sne(Register value, Register x, Register y) {
        printf("sne %s, %s, %s", value, x, y);
    }

    // mul: value = x * y (with some weird HI LO behaviour lol)
    public void mul(Register value, Register x, Register y) {
        printf("mul %s, %s, %s", value, x, y);
    }

    // mul: value = x * y (with some weird HI LO behaviour lol)
    public void mul(Register value, Register x, int y) {
        printf("mul %s, %s, %d", value, x, y);
    }

    // load byte: lb $target, $offset($from)
    public void lb(Register target, Register from, int offset) {
        printf("lb %s, %d(%s)", target, offset, from);
    }

    // load word: lw $target, $offset($from)
    public void lw(Register target, Register from, int offset) {
        printf("lw %s, %d(%s)", target, offset, from);
    }

    // move: move $target, $from
    public void move(Register target, Register from) {
        printf("move %s, %s", target, from);
    }

    // store word: sw $from, offset($target)
    public void sw(Register from, Register target, int offset) {
        printf("sw %s %d(%s)", from, offset, target);
    }

    // store byte: sw $from, offset($target)
    public void sb(Register from, Register target, int offset) {
        printf("sb %s %d(%s)", from, offset, target);
    }

    // divide: div $number, $dividedBy (lo = quotient, hi = remainder)
    public void div(Register number, Register dividedBy) {
        printf("div %s %s", number, dividedBy);
    }

    // move from hi to target: mfhi $target
    public void mfhi(Register target) {
        printf("mfhi %s", target);
    }

    // move from lo to target: mflo $target
    public void mflo(Register target) {
        printf("mflo %s", target);
    }

    // branch if equal zero: beq $x, $zero, label
    public void beqz(Register value, String label) {
        Labeller.verifyLabel(label);
        printf("beq %s, $zero, %s", value, label);
    }

    // branch if not equal zero: bnez $x, label
    public void bnez(Register value, String label) {
        Labeller.verifyLabel(label);
        printf("bnez %s, %s", value, label);
    }

    // branch if greater than zero: bgtz $x, label
    public void bgtz(Register value, String label) {
        Labeller.verifyLabel(label);
        printf("bgtz %s, %s", value, label);
    }

    // jump to label: j label
    public void j(String label) {
        Labeller.verifyLabel(label);
        printf("j %s", label);
    }

    // branch to label: b label
    public void b(String label) {
        Labeller.verifyLabel(label);
        printf("b %s", label);
    }

    // nop: nope!
    public void nop() {
        printf("nop");
    }

    public int getLevel() {
        return level;
    }

    public IndentWriter leadNewline() {
        if (!wasNewline) {
            newline();
        }
        return this;
    }

    public void newline() {
        if (debugWriter) {
            writer.printf("nop");
        }
        writer.printf("\n");
        wasNewline = true;
    }

    public void comment(Object o) {
        comment("%s", o);
    }

    public void comment(String format, Object... args) {
        leadNewline();
        if (debugWriter) {
            if (!label.isEmpty()) {
                label += ": ";
            }
            writer.printf(label + "nop ");
            label = "";
            level -= 1;
        }
        printf("# " + format, args);
        if (debugWriter) {
            level += 1;
        }
    }

    public void directive(String directive, Object... args) {
        leadNewline();
        printf("." + directive, args);
    }

    // jump register unconditionally: jr $target
    public void jr(Register register) {
        printf("jr %s", register);
    }

    public void jal(String genLabel) {
        printf("jal %s", genLabel);
    }
}
