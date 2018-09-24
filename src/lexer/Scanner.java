package lexer;

import java.io.*;

/**
 * @author cdubach
 */
public class Scanner {

    private BufferedReader input;
    private int peeked = -1;

    private int line = 1;
    private int column = 1;

    public Scanner(File source) throws FileNotFoundException {
        input = new BufferedReader(new FileReader(source));
    }


    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    public char peek() throws IOException {
        if (peeked != -1)
            return (char)peeked;

        int r = input.read();
        if (r == -1) {
            // return '\0';
            throw new EOFException();
        }

        peeked = r;
        return (char) r;
    }




    public char next() throws IOException {
        char r;
        if (peeked != -1) {
            r = (char) peeked;
            peeked = -1;
        } else {
            int i = input.read();
            if (i == -1)
                throw new EOFException();
            r = (char) i;
        }
        // System.out.printf("%s at %d\n", r, column);
        if (r == '\n' || r == '\r') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return r;
    }

    // consume does next() but doesn't return a character
    public void consume() throws IOException {
        next();
    }

    public void close() throws IOException {
        input.close();
    }



}
