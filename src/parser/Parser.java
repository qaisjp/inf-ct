package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
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

        error(sb.toString());
    }

    private void error(String s) {
        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        System.out.println("Parsing error: expected ("+s+") found ("+token+") at "+token.position);

        if ("true".equalsIgnoreCase(System.getenv("MINIC_TRACE_ERRORS"))) {
            new Exception().printStackTrace();
        }

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

    private Program parseProgram() {
        parseIncludes();

        List<StructTypeDecl> stds = parseStructDecls();
        List<VarDecl> vds = new ArrayList<>();
        List<FunDecl> fds = new ArrayList<>();

        // Both vardecls and fundecls start with these...
        if (accept(typeNameFirst)) {
            vds = parseVarDecls(false);
            fds = parseFunDecls();
        }

        mustExpectAny(TokenClass.EOF);
        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (maybeExpectAny(TokenClass.INCLUDE)) {
            mustExpectAny(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private List<StructTypeDecl> parseStructDecls() {
        List<StructTypeDecl> decls = new ArrayList<StructTypeDecl>();

        // First for parseStructType is STRUCT
        while (accept(TokenClass.STRUCT) && lookAheadAccept(2, TokenClass.LBRA)) {
            StructType t = parseStructType();
            mustExpectAny(TokenClass.LBRA);
            List<VarDecl> varDeclList = parseVarDecls(true);
            mustExpectAny(TokenClass.RBRA);
            mustExpectAny(TokenClass.SC);

            decls.add(new StructTypeDecl(t, varDeclList));
        }

        return decls;
    }

    private TokenClass[] typeNameFirst = {
            TokenClass.INT,
            TokenClass.CHAR,
            TokenClass.VOID,
            TokenClass.STRUCT, // via structtype
    };

    private List<VarDecl> parseVarDecls(boolean mustAccept) {
        List<VarDecl> varDecls = new ArrayList<>();

        while (true) {
            int offset = 1;

            // a struct typename consists of two tokens, so look an extra token ahead
            offset += accept(TokenClass.STRUCT) ? 1 : 0;

            // a typename *can* have an asterisk following it, so append that too
            offset += lookAheadAccept(offset, TokenClass.ASTERIX) ? 1 : 0;


            // both vardecl and fundecl have a IDENT we need to skip
            offset += 1;

            if (!mustAccept) {
                if (!accept(typeNameFirst) || !lookAheadAccept(offset, TokenClass.SC, TokenClass.LSBR)) {
                    break;
                }
            }

            Type type = parseType();
            String varName = token.data;

            mustExpectAny(TokenClass.IDENTIFIER);

            // Consume a semicolon now or...
            if (!maybeExpectAny(TokenClass.SC)) {
                mustExpectAny(TokenClass.LSBR);
                String intData = token.data;
                mustExpectAll(
                        TokenClass.INT_LITERAL,
                        TokenClass.RSBR,
                        TokenClass.SC
                );

                type = new ArrayType(type, Integer.parseInt(intData));
            }

            mustAccept = false;

            varDecls.add(new VarDecl(type, varName));
        }

        return varDecls;
    }

    private List<FunDecl> parseFunDecls() {
        List<FunDecl> funcDecls = new ArrayList<FunDecl>();

        while (accept(typeNameFirst)) {
            Type type;
            List<VarDecl> params;
            Block block;

            type = parseType();
            String name = token.data;
            mustExpectAny(TokenClass.IDENTIFIER);
            mustExpectAny(TokenClass.LPAR);
            params = parseParams();
            mustExpectAny(TokenClass.RPAR);
            block = parseBlock();

            funcDecls.add(new FunDecl(type, name, params, block));
        }

        return funcDecls;
    }

    private Block parseBlock() {
        mustExpectAny(TokenClass.LBRA);
        List<VarDecl> varDecls = parseVarDecls(false);

        List<Stmt> stmtList = new ArrayList<Stmt>();
        while (accept(stmtFirst)) {
            stmtList.add(parseStmt());
        }

        mustExpectAny(TokenClass.RBRA);

        return new Block(varDecls, stmtList);
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

    // list some stmt-specific tokens, and append the expFirst tokens too
    private TokenClass[] stmtFirst = Stream.concat(Arrays.asList(
            TokenClass.LBRA, // from block
            TokenClass.WHILE,
            TokenClass.IF,
            TokenClass.RETURN
    ).stream(), Arrays.stream(expFirst)).toArray(TokenClass[]::new);

    private Stmt parseStmt() {
        if (accept(TokenClass.LBRA)) {
            return parseBlock();
        } else if (accept(TokenClass.WHILE)) {
            mustExpectAll(TokenClass.WHILE, TokenClass.LPAR);
            Expr exp = parseExp();
            mustExpectAny(TokenClass.RPAR);
            Stmt stmt = parseStmt();

            return new While(exp, stmt);
        } else if (accept(TokenClass.IF)) {
            Expr exp;
            Stmt stmt;
            Stmt elseStmt = null;

            mustExpectAll(TokenClass.IF, TokenClass.LPAR);
            exp = parseExp();
            mustExpectAny(TokenClass.RPAR);
            stmt = parseStmt();

            if (maybeExpectAny(TokenClass.ELSE)) {
                elseStmt = parseStmt();
            }

            return new If(exp, stmt, elseStmt);
        } else if (maybeExpectAny(TokenClass.RETURN)) {
            Expr expr = null;

            if (accept(expFirst)) {
                expr = parseExp();
            }
            mustExpectAny(TokenClass.SC);

            return new Return(expr);
        }

        Expr lhs = parseExp();
        Expr rhs = null;

        if (maybeExpectAny(TokenClass.ASSIGN)) {
            rhs = parseExp();
        }
        mustExpectAny(TokenClass.SC);

        if (rhs != null) {
            return new Assign(lhs, rhs);
        }

        return new ExprStmt(lhs);
    }

    private Expr parseExp() {
        parseExpOr(false);

        return null; // todo
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

    private Expr parseExpUnary() {
        if (maybeExpectAny(TokenClass.SIZEOF)) {
            mustExpectAll(TokenClass.LPAR);
            Type t = parseType();
            mustExpectAll(TokenClass.RPAR);
            return new SizeOfExpr(t);
        } else if (maybeExpectAny(TokenClass.ASTERIX)) {
            return new ValueAtExpr(parseExpUnary());
        } else if (accept(TokenClass.LPAR)) {
            // This check is needed so that funcall is called
            // with tree exp_post -> root_exp -> funcall
            if (lookAheadAccept(1, typeNameFirst)) {
                mustExpectAny(TokenClass.LPAR);
                Type t = parseType();
                mustExpectAny(TokenClass.RPAR);
                Expr expr = parseExpUnary();

                return new TypecastExpr(t, expr);
            } else {
                return parseExpPost(false);
            }

        } else if (maybeExpectAny(TokenClass.MINUS)) {
            Expr e = parseExpUnary();
            return new BinOp(new IntLiteral(0), Op.SUB, e);
        } else {
            return parseExpPost(false);
        }
    }

    private Expr parseExpPost(boolean prime) {
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

        return null; // todo
    }

    private Expr parseRootExp() {
        // oldToken is only "old" after consuming this token
        Token oldToken = token;

        if (maybeExpectAny(TokenClass.LPAR)) {
            Expr e = parseExp();
            mustExpectAny(TokenClass.RPAR);
            return e;
        } else if (maybeExpectAny(TokenClass.IDENTIFIER)) {
            if (maybeExpectAny(TokenClass.LPAR)) {
                List<Expr> exprList = new ArrayList<>();

                // Only parse arg list if we aren't closing the func call
                if (!accept(TokenClass.RPAR)) {
                    exprList = parseArgList();
                }
                mustExpectAny(TokenClass.RPAR);

                return new FunCallExpr(oldToken.data, exprList);
            }

            return new VarExpr(oldToken.data);
        }

        mustExpectAny(TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.STRING_LITERAL);

        if (oldToken.tokenClass == TokenClass.INT_LITERAL) {
            return new IntLiteral(Integer.parseInt(oldToken.data));
        } else if (oldToken.tokenClass == TokenClass.CHAR_LITERAL) {
            return new ChrLiteral(oldToken.data.charAt(0));
        }

        // Otherwise return string literal
        return new StrLiteral(oldToken.data);
    }

    private List<Expr> parseArgList() {
        List<Expr> exprs = new ArrayList<>();
        exprs.add(parseExp());
        while (maybeExpectAny(TokenClass.COMMA)) {
            exprs.add(parseExp());
        }

        return exprs;
    }

    private List<VarDecl> parseParams() {
        List<VarDecl> varDeclList = new ArrayList<>();

        // If we can't accept typeNameFirst, epsilon out of here.
        if (!accept(typeNameFirst)) {
            return varDeclList;
        }

        Type t = parseType();
        String varName = token.data;
        mustExpectAny(TokenClass.IDENTIFIER);

        varDeclList.add(new VarDecl(t, varName));

        // Consume if available, and return true
        while (maybeExpectAny(TokenClass.COMMA)) {
            t = parseType();
            varName = token.data;
            mustExpectAny(TokenClass.IDENTIFIER);

            varDeclList.add(new VarDecl(t, varName));
        }

        return varDeclList;
    }

    // First: STRUCT
    private StructType parseStructType() {
        mustExpectAny(TokenClass.STRUCT);
        String name = token.data;
        mustExpectAny(TokenClass.IDENTIFIER);

        return new StructType(name);
    }

    private Type parseType() {
        Type innerType = null;

        TokenClass ourToken = token.tokenClass;

        // If we consume INT, CHAR, or VOID. We're done.. for now
        if (maybeExpectAny(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
            innerType = BaseType.fromTokenClass(ourToken);
        } else if (accept(TokenClass.STRUCT)) {
            // If we didn't consume any of the above, we expect a structtype
            innerType = parseStructType();
        } else {
            error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT);
            return null;
        }

        if (accept(TokenClass.ASTERIX)) {
            mustExpectAny(TokenClass.ASTERIX);
            return new PointerType(innerType);
        }

        return innerType;
    }
}
