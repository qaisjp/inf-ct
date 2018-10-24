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
    f = assert(io.popen "java -cp $PROJ/bin Main -ast $PROJ/tests/ast/#{filename} out", "r")
    t = f\read "*all"
    f\close!
    t

firstAstLine = "Parsing: pass"
secondAstLine = "Printing out AST:"

simplify = (s) -> s\gsub(" ", "")\gsub("\n", "")\gsub("\t", "")

check_parses_to = (filename, astFilename, t={}) ->
    output = lexfile filename
    lines = output\splitlines!

    if t.pending then
        pending "#{t.pending}\n#{output}"
        return

    parses = lines[1] == firstAstLine and lines[2] == secondAstLine

    it "should parse", ->
        assert.true parses
        return

    table.remove lines, 1
    table.remove lines, 1

    ours = table.concat(lines, "\n")

    it "should match ast file", ->
        assert.are.same(simplify(ours), simplify(getfile(astFilename)))
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

                    astFilename = "#{PROJ}/tests/#{base}/#{filename\gsub(".c$", ".ast")}"
                    data = lfs.attributes(astFilename)

                    tests[filename] = ->
                        if data
                            check_parses_to filename, astFilename
                        else
                            it "should have ast file", ->
                                check_parses_to filename, astFilename, pending: "c exists but not ast"
                                return

                elseif isDir
                    iterate base, filename.."/"
    iterate "ast"

    for filename, testFn in pairs tests do
        describe filename, testFn