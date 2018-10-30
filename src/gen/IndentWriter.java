package gen;

import java.io.PrintWriter;

public class IndentWriter {
    private PrintWriter writer;
    private int level = 0;
    private static final int width = 4;

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
        String indentation = new String(new char[level*width]).replace("\0", " ");

        writer.printf(indentation + format + "\n", args);
    }

    public int getLevel() {
        return level;
    }
}
