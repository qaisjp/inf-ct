package gen;

import java.io.PrintWriter;

public class IndentWriter {
    private PrintWriter writer;
    private int level = 0;

    public IndentWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void push() {
        level += 1;
    }

    public void pop() {
        level -= 1;
    }

    public void printf(String format, Object... args) {
        String indentation = new String(new char[level]).replace("\0", "\t");

        writer.printf(indentation + format + "\n", args);
    }

    public int getLevel() {
        return level;
    }
}
