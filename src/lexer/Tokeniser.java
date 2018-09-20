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

            // comparisons
            put('<', TokenClass.LT);
            put('>', TokenClass.GT);
    }};


    private static final Map<Character,TokenClass> charTokComparisonMap = new HashMap<Character,TokenClass>() {{
            put('=', TokenClass.EQ);
            put('!', TokenClass.NE);
            put('<', TokenClass.LE);
            put('>', TokenClass.GE);
    }};

    private static final Map<String,TokenClass> stringTokMap = new HashMap<String,TokenClass>() {{
        // types
        put("int", TokenClass.INT);
        put("void", TokenClass.VOID);
        put("char", TokenClass.CHAR);

        // keywords
        put("if", TokenClass.IF);
        put("else", TokenClass.ELSE);
        put("while", TokenClass.WHILE);
        put("return", TokenClass.RETURN);
        put("struct", TokenClass.STRUCT);
        put("sizeof", TokenClass.SIZEOF);

        // include
        put("#include", TokenClass.INCLUDE);

        // logical operators
        put("&&", TokenClass.AND);
        put("||", TokenClass.OR);
    }};

    private boolean isIdentifier(char c) {
        // this method won't be usable. it's not just [A-Za-z0-9]+, it has a different regex.
        return false;
    }

    private TokenClass readString(char firstChar) throws IOException {
        boolean consumedCharacters = false;

        for (Map.Entry<String,TokenClass> e : stringTokMap.entrySet()) {
            String s = e.getKey();

            if (s.charAt(0) == firstChar) {
                char c = firstChar;
                // System.out.print(c);
                char i = 0;

                // While next character matches
                while (scanner.peek() == s.charAt(i + 1)) {
                    // Consume the next character
                    c = scanner.next();
                    i += 1;
                    consumedCharacters = true;
                    // System.out.print(c);

                    // If we have consumed our string
                    if (i == s.length()-1) {
                        // Return token if next is whitespace (or current is symbol, i.e not letter)
                        //
                        // Note; when thisChar is a symbol, we don't need to check if the next is a symbol
                        //       because we don't need to account for situations like "===" and "==".
                        //       It will also be annoying to deal with comparisons such as:
                        //          `2 >! false` (i.e 2>!false, i.e 2>true, i.e 2>1, i.e true)
                        //       (Our current code deals with this fine, I think)
                        if (Character.isWhitespace(scanner.peek()) || !Character.isLetter(c)) {
                            // System.out.print(scanner.peek());
                            return e.getValue();
                        }

                        // We've consumed the string but there's more!!
                        // We just break out of it. It could be another string or an identifier
                        break;
                    }
                }
            }
        }

        // If we haven't consumed characters, just try the single character in the map
        if (!consumedCharacters && charTokMap.containsKey(firstChar)) {
            return charTokMap.get(firstChar);
        }

        // todo: the below accounts for stuff like `ifner` being reported as an ID
        //       what about &|?
        //
        // investigate solution: if most recently consumed character is a non-identifier
        //                       character, then we just say it's an invalid token (?)

        // IMPORTANT! ONCE WE HAVE CONSUMED A CHARACTER WE CANNOT GO BACK!
        // This means we need to keep consuming until a non-identifier character
        if (consumedCharacters) {
            // TODO: Check if this works properly
        }

        return null;
    }

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c)) {
            // if (c == '\n')
            //     System.out.println("NEWLINE");
            return next();
        }

        // if this is / and next will be /
        if (c == '/' && scanner.peek() == '/') {
            // until we hit a newline
            while (scanner.next() != '\n') {
                // do nothing
            }
            return next();
        }

        // if this is / and next will be *
        if (c == '/' && scanner.peek() == '*') {
            // consume that opening *
            char prevChar = scanner.next();
            char nextChar = scanner.next();
            // System.out.print(c);
            // System.out.print(prevChar);
            // System.out.print(nextChar);

            // This loop continues until next() throws OR return next()
            while (true) {
                prevChar = nextChar;
                nextChar = scanner.next();
                // System.out.print(nextChar);

                if (prevChar == '*' && nextChar == '/') {
                    return next();
                }
            }
        }

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
                // If current character is a backlash we're starting an escape sequence
                if (c == '\\') {
                    // we read the escape character
                    c = scanner.next();

                    // If it is not a valid escape character, return invalid
                    if (c != 't' && c != 'b' && c != 'n' && c != 'r' && c != 'f' && c != '\'' && c != '"' && c != '\\') {
                        return new Token(TokenClass.INVALID, line, column);
                    }
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

        // Quickly check for comparisons
        if (scanner.peek() == '=') {
            TokenClass tok = charTokComparisonMap.get(c);
            if (tok != null) {
                // Consume the character we've peeked
                scanner.next();

                // Return the token we've found
                return new Token(tok, line, column);
            }
        }

        // Check for logical operators
        if (c == '&' || c == '|') {
            if (scanner.peek() == c) {
                TokenClass tok = null;
                switch (c) {
                    case '&':
                        tok = TokenClass.AND;
                        break;
                    case '|':
                        tok = TokenClass.OR;
                        break;
                }

                if (tok != null) {
                    // Consume the character we've peeked
                    scanner.next();

                    // Return the token we've found
                    return new Token(tok, line, column);
                }
            }
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
