-- Requirements: busted, luarocks, luafilesystem

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
    f = assert(io.popen "java -cp $PROJ/bin Main -lexer $PROJ/tests/lexer/#{filename} out", "r")
    t = f\read "*all"
    f\close!
    t

check_lexes_to = (filename, t, errors) ->
    output = lexfile filename
    lines = {}
    for s in output\gmatch "[^\r\n]+" do
        table.insert lines, s

    if t.pending then
        pending "#{t.pending}\n#{output}"
        return

    outcome = lines[#lines]
    table.remove lines, #lines

    it "should match", ->
        assert.are.same t, lines
        return

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
    ["strings/p.escapes.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "STRING_LITERAL", "RBRA"}

describe "lexer", ->
    local iterate
    iterate = (base, f="") ->
        for d in lfs.dir("#{PROJ}/tests/#{base}/#{f}") do
            unless d == "." or d == ".."
                isDir = lfs.attributes("#{PROJ}/tests/#{base}/#{f}#{d}").mode == "directory"
                filename = "#{f}#{d}"
                if not isDir and d\sub(-2,-1) == ".c"
                    -- print filename
                    testData = tests[filename]

                    baseHash = if base == "" then "root" else base
                    describe "#{filename} ##{baseHash}", ->
                        if testData
                            testData.visited = true
                            check_lexes_to filename, testData.to, testData.errors
                        else
                            it "should have test", ->
                                check_lexes_to filename, pending: "file exists but not test"
                                return
                elseif isDir
                    iterate base, filename.."/"
    iterate "lexer"

    for filename, test in pairs tests do
        unless test.visited
            describe filename, -> it "should exist", ->
                pending "test exists but not flie"
                return
