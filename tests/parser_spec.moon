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
    ["f.declassign.c"]: errors: 1, to: {"Parsing error: expected (LSBR) found (ASSIGN) at 2:11"}
    ["f.array_decl_fun_decl.c"]: errors: 1, volatile: true, to: {"Parsing error: expected (EOF) found (LPAR) at 1:15"}
    ["f.array_decl.c"]: errors: 1, volatile: true, to: {"Parsing error: expected (INT_LITERAL) found (RSBR) at 1:7"}
    ["f.ordering.c"]: errors: 1, to: {"Parsing error: expected (EOF) found (INCLUDE) at 5:1"}
    ["p.array_decl.c"]: to: {}
    ["p.braces.c"]: to: {}
    ["p.empty.c"]: to: {}
    ["p.decl.c"]: to: {}
    ["p.comments.c"]: to: {}
    ["p.funcs.c"]: to: {}
    ["p.identifiers.c"]: to: {}
    ["p.include.c"]: to: {}
    ["p.struct_both.c"]: to: {}
    ["p.struct_declaration.c"]: to: {}
    ["p.struct_vardecl.c"]: to: {}


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
