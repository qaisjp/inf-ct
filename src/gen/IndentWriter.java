package gen;

import java.io.PrintWriter;

public class IndentWriter {
    private PrintWriter writer;
    private int level = 0;
    private static final int width = 4;

    // Magic stuff
    private String label = ""; // current label
    private boolean wasNewline = false;

    public IndentWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void push() {
        level += 1;
    }

    public void pop() {
        level -= 1;
    }

    public IndentWriter withLabel(String label) {
        this.label = label;
        return this;
    }

    public void printf(String format, Object... args) {
        String indentation = new String(new char[level*width]).replace("\0", " ");

        if (!label.isEmpty()) {
            label += ": ";
        }

        writer.printf(indentation + label + format + "\n", args);
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
        printf("sw %s %d(%s)", from, target, offset);
    }

    // store byte: sw $from, offset($target)
    public void sb(Register from, Register target, int offset) {
        printf("sb %s %d(%s)", from, target, offset);
    }

    public int getLevel() {
        return level;
    }

    public IndentWriter leadNewline() {
        if (!wasNewline) {
            writer.printf("\n");
            wasNewline = true;
        }
        return this;
    }

    public void newline() {
        writer.printf("\n");
        wasNewline = true;
    }

    public void comment(String format, Object... args) {
        printf("# " + format, args);
    }
}
