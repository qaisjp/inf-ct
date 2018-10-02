package parser;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;


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
        // System.out.printf("%s:%s\n", token.toString(), token.position.toString());
        // new Exception().printStackTrace();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token mustExpectAny(TokenClass... expected) {
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

    // mustExpectAll runs expect on each of the following token classes
    private Token[] mustExpectAll(TokenClass... expected) {
        Token[] tokens = new Token[expected.length];
        for (int i = 0; i < expected.length; i++) {
            tokens[i] = token;
            mustExpectAny(expected[i]);
        }
        return tokens;
    }

    /*
     * If the current token is equals to the expected one, accept and return true.
     * Otherwise return false.
     */
    private boolean maybeExpectAny(TokenClass... expected) {
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

    /*
    * Returns true if the current token is equals to any of the expected ones.
    */
    private boolean lookAheadAccept(int i, TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == lookAhead(i).tokenClass);
        return result;
    }

    private void parseProgram() {
        parseIncludes();
        parseStructDecls();

        // Both vardecls and fundecls start with these...
        if (accept(typeNameFirst)) {
            parseType();
            mustExpectAny(TokenClass.IDENTIFIER);


            if (accept(TokenClass.SC, TokenClass.LSBR)) {
                parseVarDecls(false, true);
            }
            parseFunDecls(true);
        }

        mustExpectAny(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (maybeExpectAny(TokenClass.INCLUDE)) {
            mustExpectAny(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructDecls() {
        // First for parseStructType is STRUCT
        if (accept(TokenClass.STRUCT)) {
            parseStructType();
            mustExpectAny(TokenClass.LBRA);
            parseVarDecls(true,false);
            mustExpectAny(TokenClass.RBRA);
            mustExpectAny(TokenClass.SC);

            parseStructDecls();
        }
    }

    private TokenClass[] typeNameFirst = {
            TokenClass.INT,
            TokenClass.CHAR,
            TokenClass.VOID,
            TokenClass.STRUCT, // via structtype
    };

    private void parseVarDecls(boolean mustAccept, boolean preParsed) {
        boolean shouldContinue = true;
        if (!preParsed) {
            if (mustAccept || accept(typeNameFirst)) {
                parseType();
                mustExpectAny(TokenClass.IDENTIFIER);
            } else {
                shouldContinue = false;
            }
        }

        if (shouldContinue) {
            // Consume a semicolon now or...
            if (!maybeExpectAny(TokenClass.SC)) {
                // System.out.printf("Expecting LSBR...%s\n", Boolean.toString(mustAccept));
                // mustExpectAny(TokenClass.LSBR); // don't forget to comment out LSBR a few lines below
                // System.out.println("OK");
                mustExpectAll(
                    TokenClass.LSBR,
                    TokenClass.INT_LITERAL,
                    TokenClass.RSBR,
                    TokenClass.SC
                );
            }

            if (preParsed) {
                if (accept(typeNameFirst)) {
                    parseType();
                    mustExpectAny(TokenClass.IDENTIFIER);
                    if (accept(TokenClass.SC, TokenClass.LSBR)) {
                        parseVarDecls(false, true);
                    }
                }
            } else {
                parseVarDecls(false, false);
            }
        }
    }

    private void parseFunDecls(boolean preParsed) {
        boolean shouldContinue = true;
        if (!preParsed) {
            if (accept(typeNameFirst)) {
                parseType();
                mustExpectAny(TokenClass.IDENTIFIER);
            } else {
                shouldContinue = false;
            }
        }

        if (shouldContinue) {
            mustExpectAny(TokenClass.LPAR);
            parseParams();
            mustExpectAny(TokenClass.RPAR);
            parseBlock();

            parseFunDecls(false);
        }
    }

    private void parseBlock() {
        mustExpectAny(TokenClass.LBRA);
        parseVarDecls(false, false);

        while (accept(stmtFirst)) {
            parseStmt();
        }

        mustExpectAny(TokenClass.RBRA);
    }

    private TokenClass[] expFirst = {
            TokenClass.SIZEOF, // via exp_unary
            TokenClass.ASTERIX, // via exp_unary
            TokenClass.LPAR, // via exp_unary
            TokenClass.MINUS, // via exp_unary
            // still from exp... via exp_post this time
            TokenClass.IDENTIFIER, // via funcall (so exp -> exp_post -> funcall)
            // still from exp, via exp_post again
            TokenClass.LPAR, // via root_exp
            TokenClass.IDENTIFIER, // via root_exp
            TokenClass.INT_LITERAL, // via root_exp
            TokenClass.CHAR_LITERAL, // via root_exp
            TokenClass.STRING_LITERAL, // via root_exp
    };

    private TokenClass[] stmtFirst = Stream.concat(Arrays.asList(
            TokenClass.LBRA, // from block
            TokenClass.WHILE,
            TokenClass.IF,
            TokenClass.RETURN
    ).stream(), Arrays.stream(expFirst)).toArray(TokenClass[]::new);

    private void parseStmt() {
        if (accept(TokenClass.LBRA)) {
            parseBlock();
        } else if (accept(TokenClass.WHILE)) {
            mustExpectAll(TokenClass.WHILE, TokenClass.LPAR);
            parseExp();
            mustExpectAny(TokenClass.RPAR);
            parseStmt();
        } else if (accept(TokenClass.IF)) {
            mustExpectAll(TokenClass.IF, TokenClass.LPAR);
            parseExp();
            mustExpectAny(TokenClass.RPAR);
            parseStmt();

            if (maybeExpectAny(TokenClass.ELSE)) {
                parseStmt();
            }
        } else if (maybeExpectAny(TokenClass.RETURN)) {
            if (accept(expFirst)) {
                parseExp();
            }
            mustExpectAny(TokenClass.SC);
        } else {
            parseExp();
            if (maybeExpectAny(TokenClass.ASSIGN)) {
                parseExp();
            }
            mustExpectAny(TokenClass.SC);
        }
    }

    private void parseExp() {
        parseExpOr(false);
    }

    private void parseExpOr(boolean prime) {
        if (!prime) {
            parseExpAnd(false);
        }
        if (maybeExpectAny(TokenClass.OR)) {
            parseExpAnd(false);
            parseExpOr(true);
        }
    }

    private void parseExpAnd(boolean prime) {
        if (!prime) {
            parseExpEq(false);
        }
        if (maybeExpectAny(TokenClass.AND)) {
            parseExpEq(false);
            parseExpAnd(true);
        }
    }

    private void parseExpEq(boolean prime) {
        if (!prime) {
            parseExpRel(false);
        }
        if (maybeExpectAny(TokenClass.NE, TokenClass.EQ)) {
            parseExpRel(false);
            parseExpEq(true);
        }
    }

    private void parseExpRel(boolean prime) {
        if (!prime) {
            parseExpAdd(false);
        }
        if (maybeExpectAny(TokenClass.GE, TokenClass.GT, TokenClass.LE, TokenClass.LT)) {
            parseExpAdd(false);
            parseExpRel(true);
        }
    }

    private void parseExpAdd(boolean prime) {
        if (!prime) {
            parseExpMult(false);
        }
        if (maybeExpectAny(TokenClass.MINUS, TokenClass.PLUS)) {
            parseExpMult(false);
            parseExpAdd(true);
        }
    }

    private void parseExpMult(boolean prime) {
        if (!prime) {
            parseExpUnary();
        }
        if (maybeExpectAny(TokenClass.REM, TokenClass.DIV, TokenClass.ASTERIX)) {
            parseExpUnary();
            parseExpMult(true);
        }
    }

    private void parseExpUnary() {
        if (maybeExpectAny(TokenClass.SIZEOF)) {
            mustExpectAll(TokenClass.LPAR);
            parseType();
            mustExpectAll(TokenClass.RPAR);
            parseExpUnary();
        } else if (maybeExpectAny(TokenClass.ASTERIX)) {
            parseExpUnary();
        } else if (accept(TokenClass.LPAR)) {
            // This check is needed so that funcall is called
            // with tree exp_post -> root_exp -> funcall
            if (lookAheadAccept(1, typeNameFirst)) {
                mustExpectAny(TokenClass.LPAR);
                parseType();
                mustExpectAny(TokenClass.RPAR);
                parseExpUnary();
            } else {
                parseExpPost(false);
            }

        } else if (maybeExpectAny(TokenClass.MINUS)) {
            parseExpUnary();
        } else {
            parseExpPost(false);
        }
    }

    private void parseExpPost(boolean prime) {
        if (!prime) {
            parseRootExp();
        }

        if (maybeExpectAny(TokenClass.DOT)) {
            mustExpectAll(TokenClass.IDENTIFIER);
            parseExpPost(true);
        } else if (maybeExpectAny(TokenClass.LSBR)) {
            parseExp();
            mustExpectAny(TokenClass.RSBR);
            parseExpPost(true);
        }
    }

    private void parseRootExp() {
        if (maybeExpectAny(TokenClass.LPAR)) {
            parseExp();
            mustExpectAny(TokenClass.RPAR);
        } else if (maybeExpectAny(TokenClass.IDENTIFIER)) {
            if (maybeExpectAny(TokenClass.LPAR)) {
                if (!accept(TokenClass.RPAR)) {
                    parseArgList();
                }
                mustExpectAny(TokenClass.RPAR);
            }
        } else {
            mustExpectAny(TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);
        }
    }

    private void parseArgList() {
        parseExp();
        while (maybeExpectAny(TokenClass.COMMA)) {
            parseExp();
        }
    }

    private void parseParams() {
        // If we can't accept typeNameFirst, epsilon out of here.
        if (!accept(typeNameFirst)) {
            return;
        }

        parseType();
        mustExpectAny(TokenClass.IDENTIFIER);

        // Consume if available, and return true
        while (maybeExpectAny(TokenClass.COMMA)) {
            parseType();
            mustExpectAny(TokenClass.IDENTIFIER);
        }
    }

    // First: STRUCT
    private void parseStructType() {
        mustExpectAny(TokenClass.STRUCT);
        mustExpectAny(TokenClass.IDENTIFIER);
    }

    private void parseType() {
        // If we consume INT, CHAR, or VOID. We're done.. for now
        if (maybeExpectAny(TokenClass.INT) || maybeExpectAny(TokenClass.CHAR) || maybeExpectAny(TokenClass.VOID)) {
            //
        } else {
            // If we didn't consume any of the above, we expect a structtype
            parseStructType();
        }

        if (accept(TokenClass.ASTERIX)) {
            mustExpectAny(TokenClass.ASTERIX);
        }
    }
}
