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
    private StringBuilder stringSoFar = new StringBuilder();

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
             if (result.tokenClass != TokenClass.INVALID) {
                 stringSoFar.setLength(0);
             }
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

    // Strings in stringTokMap DO NOT ACCEPT SPECIAL CHARACTERS (I think?)
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
    }};

    private TokenClass readString(char firstChar) throws IOException {
        boolean consumedCharacters = false;

        stringSoFar.append(firstChar);

        for (Map.Entry<String,TokenClass> e : stringTokMap.entrySet()) {
            String s = e.getKey();

            // todo: doesn't catch char sometimes hmmm
            if (s.charAt(0) == firstChar) {
                char c = firstChar;
                // System.out.print(c);
                char i = 0;

                // While next character matches
                while (scanner.peek() == s.charAt(i + 1)) {
                    // Consume the next character
                    c = scanner.next();
                    stringSoFar.append(c);

                    i += 1;
                    consumedCharacters = true;
                    // System.out.print(c);

                    // If we have consumed our string
                    if (i == s.length()-1) {
                        // Return token if next is NOT identifier
                        if (!isIdentifierCharacterMid(scanner.peek())) {
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

        if (consumedCharacters || Character.isLetter(firstChar) || firstChar == '_') {
            readIdentifier();
            // System.out.printf("Consumed: %s\n", stringSoFar);
            return TokenClass.IDENTIFIER;
        }

        return null;
    }

    private boolean isIdentifierCharacterMid(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // readIdentifier keeps scanning until we hit a non-identifier character
    private void readIdentifier() throws IOException {
        char nextChar = scanner.peek();
        while (isIdentifierCharacterMid(nextChar)) {
            // Consume the character we have peeked
            scanner.next();

            stringSoFar.append(nextChar);

            nextChar = scanner.peek();
        }
    }

    boolean isEscapeCharacter(char c) {
        return (c == 't' || c == 'b' || c == 'n' || c == 'r' || c == 'f' || c == '\'' || c == '"' || c == '\\');
    }

    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // Are we currently reading an identifier? (this happens if we read ahead and ran into an error)
        if (stringSoFar.length() > 0) {
            // Put the column back a few, precisely the length of the partially read identifier
            column -= stringSoFar.length();
            readIdentifier();
            // System.out.printf("%s\n", stringSoFar);
            return new Token(TokenClass.IDENTIFIER, line, column);
        }

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
            scanner.next();

            char prevChar;
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
                    if (!isEscapeCharacter(c)) {
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

        // If open character literal
        if (c == '\'') {
            // Read the next character
            c = scanner.next();

            // If current character is a backlash we're starting an escape sequence
            if (c == '\\') {
                // we read the escape character
                c = scanner.next();

                // If it is not a valid escape character, return invalid
                if (!isEscapeCharacter(c)) {
                    return new Token(TokenClass.INVALID, line, column);
                }
            }

            // Check if the next character is a close quote
            if (scanner.peek() == '\'') {
                scanner.consume();

                return new Token(TokenClass.CHAR_LITERAL, line, column);
            }

            // If we're here, then there's a mistake. Uh oh!
            return new Token(TokenClass.INVALID, line, column);
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
        if ((c == '&' || c == '|') && (scanner.peek() == c)) {
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

        if (c == '#') {
            int startColumn = column;
            // TODO: column is supposed to be reported at start
            for (char nextChar : "include".toCharArray()) {
                c = scanner.next();
                stringSoFar.append(c);
                if (c != nextChar) {

                    return new Token(TokenClass.INVALID, line, startColumn);
                }
            }
            return new Token(TokenClass.INCLUDE, line, column);
        }

        // Check for simple character
        if (charTokMap.containsKey(c)) {
            return new Token(charTokMap.get(c), line, column);
        }

        // Use readstring trick to read strings
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
