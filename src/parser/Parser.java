package parser;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;



    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();

        parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt=1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
     * If the current token is equals to the expected one, accept and return true.
     * Otherwise return false.
     */
    private boolean expectOr(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return true;
            }
        }

        return false;
    }

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private void parseProgram() {
        parseIncludes();
        parseStructDecls();
        parseVarDecls();
        parseFunDecls();
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructDecls() {
        // First for parseStructType is STRUCT
        if (accept(TokenClass.STRUCT)) {
            nextToken();

            parseStructType();
            expect(TokenClass.LBRA);
            parseVarDecls(true);
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);

            parseStructDecls();
        }
    }

    private TokenClass[] typeNameFirst = {
            TokenClass.INT,
            TokenClass.CHAR,
            TokenClass.VOID,
            TokenClass.STRUCT, // via structtype
    };

    private void parseVarDecls(boolean mustAccept) {
        if (mustAccept || accept(typeNameFirst)) {
            parseType();
            expect(TokenClass.IDENTIFIER);

            // Consume a semicolon now or...
            if (!expectOr(TokenClass.SC)) {
                expect(TokenClass.LBRA);
                expect(TokenClass.INT_LITERAL);
                expect(TokenClass.RBRA);
                expect(TokenClass.SC);
            }

            parseVarDecls(false);
        }
    }

    private void parseVarDecls() {
        parseVarDecls(false);
    }

    private void parseFunDecls() {
        // to be completed ...
    }

    // First: STRUCT
    private void parseStructType() {
        expect(TokenClass.STRUCT);
        expect(TokenClass.IDENTIFIER);
    }

    private void parseType() {
        // If we consume INT, CHAR, or VOID. We're done.. for now
        if (expectOr(TokenClass.INT) || expectOr(TokenClass.CHAR) || expectOr(TokenClass.VOID)) {
            //
        } else {
            // If we didn't consume any of the above, we expect a structtype
            parseStructType();
        }

        if (accept(TokenClass.ASTERIX)) {
            expect(TokenClass.ASTERIX);
        }
    }

    private void parseBinaryOp() {
        Object _ = false ||
                expectOr(TokenClass.ASTERIX) ||
                expectOr(TokenClass.DIV) ||
                expectOr(TokenClass.REM) ||
                expectOr(TokenClass.PLUS) ||
                expectOr(TokenClass.MINUS) ||
                expectOr(TokenClass.LT) ||
                expectOr(TokenClass.LE) ||
                expectOr(TokenClass.GT) ||
                expectOr(TokenClass.GE) ||
                expectOr(TokenClass.EQ) ||
                expectOr(TokenClass.NE) ||
                expectOr(TokenClass.AND) ||
                // we must end with expect
                // so that at least ONE of
                // these are expected!
                (expect(TokenClass.OR) != null);
    }

    // to be completed ...
}
