PROJ = os.getenv("PROJ")
if PROJ == "" or not PROJ then
    error("PROJ environment variable is not set")
    return

getfile = (filename) ->
    f = assert(io.open filename, "r")
    t = f\read "*all"
    f\close!
    t

lexfile = (filename) ->
    f = assert(io.popen("java -cp $PROJ/bin Main -lexer $PROJ/tests/lexer/" .. filename .. " out", "r"))
    t = f\read "*all"
    f\close!
    t

check_lexes_to = (filename, t, errors) ->
    describe filename, ->
        output = lexfile filename
        lines = {}
        for s in output\gmatch "[^\r\n]+" do
            table.insert lines, s

        outcome = lines[#lines]
        table.remove lines, #lines

        it "should match", -> assert.are.same t, lines

        if errors then
            it "should fail", ->
                assert.equal(
                    (string.format "Lexing: failed (%d errors)", errors),
                    outcome
                )
                return
        else
            it "should pass", ->
                assert.equal "Lexing: pass", outcome
                return

tests =
    ["p.types.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT",
        "IDENTIFIER", "SC", "CHAR", "IDENTIFIER", "SC", "RBRA"}
    ["trailingnewline/f.nonewline.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0", "INVALID"}
    ["trailingnewline/f.endnewline.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0", "INVALID"}

describe "lexer", ->
    [check_lexes_to filename, to, errors for filename, {:to, :errors} in pairs tests]
