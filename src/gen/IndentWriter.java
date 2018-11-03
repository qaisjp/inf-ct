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

        label = "";
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
        wasNewline = true;
        writer.printf("\n");
    }

    public void comment(String format, Object... args) {
        printf("# " + format, args);
    }
}
