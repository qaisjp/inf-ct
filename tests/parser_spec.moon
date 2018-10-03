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
    f = assert(io.popen "java -cp $PROJ/bin Main -parser $PROJ/tests/parser/#{filename} out", "r")
    t = f\read "*all"
    f\close!
    t

check_parses_to = (filename, t, errors) ->
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
                (string.format "Parsing: failed (%d errors)", errors),
                outcome
            )
            return
    else
        it "should pass", ->
            assert.equal "Parsing: pass", outcome
            return

tests =
    -- base originals
    ["p.fibonacci.c"]: to: {}
    ["p.tictactoe.c"]: to: {}

    -- base
    ["f.array_decl_fun_decl.c"]: errors: 1, volatile: true, to: {"Parsing error: expected (EOF) found (LPAR) at 1:15"}
    ["f.array_decl.c"]: errors: 1, volatile: true, to: {"Parsing error: expected (INT_LITERAL) found (RSBR) at 1:7"}
    ["f.block_ordering.1.c"]: errors: 2, to:
        {"Parsing error: expected (RBRA) found (INT) at 6:5", "Parsing error: expected (LPAR) found (SC) at 6:10"}
    ["f.block_ordering.2.c"]: errors: 2, to:
        {"Parsing error: expected (RBRA) found (INT) at 8:5", "Parsing error: expected (LPAR) found (SC) at 8:10"}
    ["f.declassign.c"]: errors: 2, volatile: true, to:
        {"Parsing error: expected (RBRA) found (INT) at 2:5",
        "Parsing error: expected (LPAR) found (ASSIGN) at 2:11"}
    ["f.include_sc.c"]: errors: 1, to: {"Parsing error: expected (EOF) found (SC) at 1:16"}
    ["f.ordering.c"]: errors: 1, to: {"Parsing error: expected (EOF) found (INCLUDE) at 5:1"}
    ["f.param.1.c"]: errors: 1, to: {"Parsing error: expected (RPAR) found (COMMA) at 1:9"}
    ["f.param.2.c"]: errors: 1, to: {"Parsing error: expected (INT|CHAR|VOID|STRUCT) found (RPAR) at 1:19"}
    ["f.struct_empty.c"]: errors: 1, to: {"Parsing error: expected (INT|CHAR|VOID|STRUCT) found (RBRA) at 1:11"}
    ["p.array_decl.c"]: to: {}
    ["p.blocky.c"]: to: {}
    ["p.braces.c"]: to: {}
    ["p.empty.c"]: to: {}
    ["p.escapes.c"]: to: {}
    ["p.everything.c"]: to: {}
    ["p.decl.c"]: to: {}
    ["p.comments.c"]: to: {}
    ["p.comparisons.c"]: to: {}
    ["p.funcs.c"]: to: {}
    ["p.identifiers.c"]: to: {}
    ["p.if.c"]: to: {}
    ["p.include.c"]: to: {}
    ["p.includes.c"]: to: {}
    ["p.struct_both.c"]: to: {}
    ["p.struct_declaration.c"]: to: {}
    ["p.struct_vardecl.c"]: to: {}
    ["p.while_loop.c"]: to: {}


describe "#parser", ->
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
                            check_parses_to filename, testData.to, testData.errors
                        else
                            it "should have test", ->
                                check_parses_to filename, pending: "file exists but not test"
                                return
                elseif isDir
                    iterate base, filename.."/"
    iterate "parser"

    for filename, test in pairs tests do
        describe filename, -> it "should have file", ->
            unless test.visited
                pending "test exists but not file"
            return
