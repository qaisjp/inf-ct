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

string.split = (sep) =>
   sep, fields = sep or ":", {}
   pattern = string.format("([^%s]+)", sep)
   self\gsub(pattern, (c) -> fields[#fields+1] = c)
   fields

lexfile = (filepath, input) ->
    cmd = "\"$PROJ/tests/gen.sh\" \"#{filepath}\" 2>&1"

    local inputPath
    if input then
        inputPath = os.tmpname!
        inputFile = io.open inputPath, "w"
        inputFile\write input
        inputFile\close!
        cmd = "cat \"#{inputPath}\" | #{cmd}"

    f = assert(io.popen cmd, "r")
    t = f\read "*all"
    f\close!

    if inputPath
        os.execute("rm \"#{inputPath}\"")
    return t

-- Extracts the directive and returns the contents
extract_directive = (directive, lines) ->
    open = "/*gen:#{directive}/*"
    close = "/*gen:#{directive}*/"

    out = {}
    read = false
    for i, v in ipairs(lines) do
        if v == open then
            read = true
        elseif v == close then
            read = false
            break
        elseif read then
            -- print "put"
            out[#out+1] = v
    out

-- Returns the simulated output from this thing
extract_simulated_output = (lines) ->
    read_mips = false
    out = {}
    for i, v in ipairs(lines) do
        if v\find "MARS output below" then
            read_mips = true
        elseif read_mips then
            out[#out+1] = v
    out

check_parses_to = (filename, filepath) ->
    t = {}
    -- if t.pending then
    --     pending "#{t.pending}\n#{output}"
    --     return

    local input, expected_output
    local isPending

    lazy_setup ->
        -- print filename
        c_lines = getfile(filepath)\splitlines!
        input = extract_directive "put", c_lines
        has_input = #input != 0
        input = nil unless has_input
    
        -- if has_input then
        --     print "input:", table.concat(input, "\n")

        expected_output = extract_directive "expect", c_lines
        has_output = #expected_output != 0

        -- if has_output then
        --     print "expected:", table.concat(expected_output, "\n")

        isPending = (not has_input) and (not has_output)
        -- print filename, has_input, has_output, isPending
        return

    it "should match output", ->
        if isPending then
            pending filename
            return

        input = table.concat(input, "\n") if input

        output = lexfile filepath, input
        lines = output\splitlines!
        their_output = extract_simulated_output lines
        -- print filename, table.concat(their_output, "\n")
        
        assert.are.same expected_output, their_output
        return

tests = {}

describe "#gen", ->
    if os.getenv("CI") then
        -- We need to lex some random file first because myMARS generates some unavoidable shit
        lexfile os.tmpname!, ""

    local iterate
    iterate = (base, f="") ->
        for d in lfs.dir("#{PROJ}/tests/#{base}/#{f}") do
            unless d == "." or d == ".."
                filename = "#{f}#{d}"
                filepath = "#{PROJ}/tests/#{base}/#{filename}"
                isDir = lfs.attributes(filepath).mode == "directory"
                if not isDir and d\sub(-2,-1) == ".c"
                    -- print filename
                    baseHash = if base == "" then "root" else base
                    describe filename, ->
                        check_parses_to filename, filepath
                elseif isDir
                    iterate base, filename.."/"
    iterate "gen"
