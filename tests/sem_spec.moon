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
    f = assert(io.popen "java -cp $PROJ/bin Main -sem $PROJ/tests/sem/#{filename} out 2>&1", "r")
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

    lastSlashIndex = string.find(filename, "/[^/]*$") or 0
    prefix = filename\sub(lastSlashIndex+1, lastSlashIndex+1)
    if prefix == "p" and errors then
        print("OK")
        error "test has errors in spec yet should pass"
    elseif prefix == "f" and not errors then
        error "test has no errors yet marked as should fail in filename"

    it "should match", ->
        assert.are.same t, lines
        return

    if errors then
        it "should fail", ->
            assert.equal(
                (string.format "Semantic analysis: Failed (%d)", errors),
                outcome
            )
            return
    else
        it "should pass", ->
            assert.equal "Semantic analysis: Pass", outcome
            return

tests =
    -- base originals
    ["p.fibonacci.c"]: to: {}
    ["p.tictactoe.c"]: to: {}

    -- base
    ["f.glob_decl.func.c"]: errors: 1, to: {"Parsing error: expected (EOF) found (LPAR) at 1:15"}

    ["p.fibonacci.c"]: to: {}
    ["p.return_void.c"]: to: {}
    ["p.struct_and_decl.c"]: to: {}
    ["p.struct_and_vardecl.c"]: to: {}


describe "#sem", ->
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
    iterate "sem"

    for filename, test in pairs tests do
        describe filename, -> it "should have file", ->
            unless test.visited
                pending "test exists but not file"
            return
