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

string.splitlines = =>
    lines = {}
    for s in self\gmatch "[^\r\n]+" do
        table.insert lines, s
    lines

lexfile = (filename) ->
    f = assert(io.popen "java -cp $PROJ/bin Main -ast $PROJ/tests/parser/#{filename} out", "r")
    t = f\read "*all"
    f\close!
    t

firstAstLine = "Parsing: pass"
secondAstLine = "Printing out AST:"
validate_ast = (canonical, ours) ->
    canonical\gsub(" ", "")\gsub("\n", "")\gsub("\t", "") == ours\gsub(" ", "")\gsub("\t", "")\gsub("\n", "")

-- print(validate_ast("Parser\t( this, \nthat)", "Parser(this,that)"))

check_parses_to = (filename, t, errors) ->
    output = lexfile filename
    lines = output\splitlines!

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
                (string.format "Parsing: failed (%d errors)", errors),
                outcome
            )
            return
    else
        it "should pass", ->
            assert.equal "Parsing: pass", outcome
            return

tests = {}

describe "#ast", ->
    local iterate
    iterate = (base, f="") ->
        for d in lfs.dir("#{PROJ}/tests/#{base}/#{f}") do
            unless d == "." or d == ".."
                isDir = lfs.attributes("#{PROJ}/tests/#{base}/#{f}#{d}").mode == "directory"
                filename = "#{f}#{d}"
                if not isDir and d\sub(-2,-1) == ".c"
                    baseHash = if base == "" then "root" else base
                    tests[filename] = {"#{filename}#{(testData and testData.volatile or false) and " #volatile" or ""}", ->
                        if testData
                            testData.visited = true
                            check_parses_to filename, testData.to, testData.errors
                        else
                            it "should have test", ->
                                check_parses_to filename, pending: "file exists but not test"
                                return
                    }
                elseif isDir
                    iterate base, filename.."/"
    iterate "ast"

    for filename, test in pairs tests do
        {description, testFn} = test

        describe description, -> it "should have ast file", ->
            print(lfs.attributes(filename\gsub(".c^", ".ast")))
            unless test.visited
                pending "test exists but not file"
            return
