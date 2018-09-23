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

describe "lexer", ->
    describe "types", ->
        check_lexes_to "p.types.c",
            {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT",
            "IDENTIFIER", "SC", "CHAR", "IDENTIFIER", "SC", "RBRA"}

    check_lexes_to "trailingnewline/f.nonewline.c",
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0",
        "INVALID"}, 1

    check_lexes_to "trailingnewline/f.endnewline.c",
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0",
        "INVALID"}, 1
