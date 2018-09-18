package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */
    private static final Map<Character,TokenClass> charTokMap = new HashMap<Character,TokenClass>() {{
            put('{', TokenClass.LBRA); // left brace
            put('}', TokenClass.RBRA); // right brace
            put('(', TokenClass.LPAR); // left paren
            put(')', TokenClass.RPAR); // right paren
            put('[', TokenClass.LSBR); // left sq brace
            put(']', TokenClass.RSBR); // rigth sq brace

            put('+', TokenClass.PLUS);
            put('-', TokenClass.MINUS);
            put('*', TokenClass.ASTERIX); // ASTERISK!!!!!!
            put('/', TokenClass.DIV);
            put('%', TokenClass.REM); // modulo

            put('.', TokenClass.DOT); // struct member access
            put('=', TokenClass.ASSIGN);
            put(',', TokenClass.COMMA);
            put(';', TokenClass.SC); // semicolon
    }};

    static final Map<String,TokenClass> stringTokMap = new HashMap<String,TokenClass>();

    private TokenClass readString(char firstChar) {
        return charTokMap.get(firstChar);
    }

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // If open string literal
        if (c == '"') {
            // Keep reading until we encounter a closing quote
            // ESCAPE \"

            // System.out.print(c);

            // Read the next character
            c = scanner.next();

            // System.out.print(c);

            // keep scanning until we've hit a quote
            while (c != '"') {
                // If current character is a backlash
                if (c == '\\') {
                    // we read & ignore the next character (can be a quote)
                    scanner.next();
                    // System.out.print(c);
                }

                // Read the next char
                // if we ignored a char, it's after the ignored char
                c = scanner.next();
                // System.out.print(c);
            }
            return new Token(TokenClass.STRING_LITERAL, line, column);
        }

        // recognises integer literals (again no accounting for string literals)
        if (Character.isDigit(c)) {
            while(Character.isDigit(scanner.peek())) {
                scanner.next();
            }
            return new Token(TokenClass.INT_LITERAL, line, column);
        }

        // Use readstring trick to read strings (or individual characters)
        // starts with provided character
        TokenClass tok = readString(c);
        if (tok != null) {
            return new Token(tok, line, column);
        }

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
