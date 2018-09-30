-- Requirements: busted, moonscript, luafilesystem

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
    -- base originals
    ["fibonacci.c"]: to:
        {"INCLUDE", "STRING_LITERAL", "VOID", "IDENTIFIER",
        "LPAR", "RPAR", "LBRA", "INT", "IDENTIFIER", "SC",
        "INT", "IDENTIFIER", "SC", "INT", "IDENTIFIER", "SC",
        "INT", "IDENTIFIER", "SC", "INT", "IDENTIFIER", "SC",
        "CHAR", "IDENTIFIER", "SC", "IDENTIFIER", "ASSIGN", "IDENTIFIER",
        "LPAR", "RPAR", "SC", "IDENTIFIER", "ASSIGN", "INT_LITERAL",
        "SC", "IDENTIFIER", "ASSIGN", "INT_LITERAL", "SC", "IDENTIFIER",
        "LPAR", "LPAR", "CHAR", "ASTERIX", "RPAR", "STRING_LITERAL",
        "RPAR", "SC", "IDENTIFIER", "LPAR", "IDENTIFIER", "RPAR",
        "SC", "IDENTIFIER", "LPAR", "LPAR", "CHAR", "ASTERIX", "RPAR",
        "STRING_LITERAL", "RPAR", "SC", "IDENTIFIER", "ASSIGN",
        "INT_LITERAL", "SC", "WHILE", "LPAR", "IDENTIFIER", "LT",
        "IDENTIFIER", "RPAR", "LBRA", "IF", "LPAR", "IDENTIFIER", "LE",
        "INT_LITERAL", "RPAR", "IDENTIFIER", "ASSIGN", "IDENTIFIER", "SC",
        "ELSE", "LBRA", "IDENTIFIER", "ASSIGN", "IDENTIFIER", "PLUS",
        "IDENTIFIER", "SC", "IDENTIFIER", "ASSIGN", "IDENTIFIER", "SC",
        "IDENTIFIER", "ASSIGN", "IDENTIFIER", "SC", "RBRA", "IDENTIFIER",
        "LPAR", "IDENTIFIER", "RPAR", "SC", "IDENTIFIER", "LPAR", "LPAR",
        "CHAR", "ASTERIX", "RPAR", "STRING_LITERAL", "RPAR", "SC",
        "IDENTIFIER", "ASSIGN", "IDENTIFIER", "PLUS", "INT_LITERAL", "SC",
        "RBRA", "RBRA"}
    -- base
    ["f.excl.c"]: errors: 1, to: {"Lexing error: unrecognised character (!) at 1:1", "INVALID"}
    ["p.empty.c"]: to: {}
    ["p.identifiers.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA"
        "INT", "IDENTIFIER", "SC", "INT", "IDENTIFIER", "SC",
        "INT", "IDENTIFIER", "SC", "INT", "IDENTIFIER", "SC",
        "INT", "IDENTIFIER", "SC", "INT", "IDENTIFIER", "SC", "RBRA"}
    ["p.ops.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT", "IDENTIFIER",
        "ASSIGN", "INT_LITERAL", "SC", "IF", "LPAR", "IDENTIFIER", "EQ",
        "INT_LITERAL", "RPAR", "LBRA", "RBRA", "ELSE", "IF", "LPAR",
        "IDENTIFIER", "GE", "INT_LITERAL", "RPAR", "LBRA", "RBRA", "ELSE",
        "IF", "LPAR", "INT_LITERAL", "LE", "IDENTIFIER", "RPAR", "LBRA",
        "RBRA", "ELSE", "IF", "LPAR", "IDENTIFIER", "GT", "INT_LITERAL",
        "RPAR", "LBRA", "RBRA", "ELSE", "IF", "LPAR", "IDENTIFIER", "LT",
        "INT_LITERAL", "RPAR", "LBRA", "RBRA","ELSE", "IF", "LPAR", "INT_LITERAL",
        "GT", "IDENTIFIER", "RPAR", "LBRA", "RBRA", "IF", "LPAR", "INT_LITERAL",
        "AND", "INT_LITERAL", "RPAR", "LBRA", "RBRA", "ELSE", "IF", "LPAR",
        "INT_LITERAL", "OR", "INT_LITERAL", "RPAR", "LBRA", "RBRA", "RBRA"}
    ["p.partial.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR", "IDENTIFIER", "SC",
        "CHAR", "IDENTIFIER", "SC", "CHAR", "IDENTIFIER", "SC", "RBRA"}
    ["p.twocharint.c"]: to: {
        "VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT", "IDENTIFIER", "ASSIGN",
        "INT_LITERAL", "SC", "RBRA"}
    ["p.types.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT",
        "IDENTIFIER", "SC", "CHAR", "IDENTIFIER", "SC", "RBRA"}

    -- lastchar
    ["lastchar/f.nonewline.hash.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:1", "INVALID"}
    ["lastchar/f.endnewline.hash.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:1", "INVALID"}
    ["lastchar/f.nonewline.excl.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (!) at 2:1", "INVALID"}
    ["lastchar/f.endnewline.excl.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (!) at 2:1", "INVALID"}

    -- string literals
    ["strings/f.badescapes.c"]: errors: 3, volatile: true, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR", "ASTERIX",
        "IDENTIFIER", 'ASSIGN', 'Lexing error: unrecognised character (q) at 2:18',
        'INVALID', 'Lexing error: unrecognised character (\\) at 2:18', 'INVALID',
        'IDENTIFIER', 'INVALID', 'RBRA'}
    ["strings/f.unclosed.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        -- "Lexing error: expected closing quote, got newline at 2:16",
        "INVALID", "RBRA"}
    ["strings/f.unclosed.eof.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (\") at 2:17",
        "INVALID"}
    ["strings/f.unclosed.eof.escape.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (\") at 2:17",
        "INVALID"}
    ["strings/p.empty.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR", "ASTERIX",
        "IDENTIFIER", "ASSIGN", "STRING_LITERAL", "SC", "RBRA"}
    ["strings/p.escapes.c"]: to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR", "ASTERIX",
        "IDENTIFIER", "ASSIGN", "STRING_LITERAL", "SC", "RBRA"}

    -- char literals
    ["chars/f.multichar.c"]: errors: 2, volatile: true, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA",
        "CHAR", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (a) at 2:14",
        "INVALID", "IDENTIFIER", "Lexing error: unrecognised character (;) at 2:19",
        "INVALID", "RBRA"}
    ["chars/f.unclosed.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (;) at 2:17",
        "INVALID", "RBRA"}
    ["chars/f.unclosed.eof.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (\') at 2:17",
        "INVALID"}
    ["chars/f.unclosed.eof.escape.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (\') at 2:17",
        "INVALID"}
    ["chars/f.unclosed.withchar.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (n) at 2:17",
        "INVALID", "SC", "RBRA"}
    ["chars/f.unclosed.withchar.eof.c"]: errors: 1, to:
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "CHAR",
        "ASTERIX", "IDENTIFIER", "ASSIGN",
        "Lexing error: unrecognised character (\') at 2:17",
        "INVALID"}

    -- comments
    ["comments/p.incomplete.c"]: to: {"INT", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "DIV"}
    ["comments/p.comments.c"]: to: {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA"}
    ["comments/f.unclosed.c"]: errors: 1, to:
        {"INT", "IDENTIFIER", "LPAR", "RPAR", "LBRA",
        "Lexing error: unrecognised character (/) at 2:5", "INVALID"}

    -- includes
    ["includes/f.caps.c"]: errors: 1, to:
        {"Lexing error: unrecognised character (#) at 1:1",
        "INVALID", "IDENTIFIER", "STRING_LITERAL"}
    ["includes/f.typo.c"]: errors: 1, to:
        {"Lexing error: unrecognised character (#) at 1:1", "INVALID", "IDENTIFIER"}
    ["includes/p.multiOneLine.c"]: to: {"INCLUDE", "STRING_LITERAL", "INCLUDE", "STRING_LITERAL"}
    ["includes/p.nospace.c"]: to: {"INCLUDE", "STRING_LITERAL"}
    ["includes/p.space.c"]: to: {"INCLUDE", "STRING_LITERAL"}
    ["includes/p.spaces.c"]: to: {"INCLUDE", "STRING_LITERAL"}


describe "#lexer", ->
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
                    describe "#{filename}#{(testData and testData.volatile or false) and " #volatile" or ""}", ->
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
        describe filename, -> it "should have file", ->
            unless test.visited
                pending "test exists but not file"
            return
