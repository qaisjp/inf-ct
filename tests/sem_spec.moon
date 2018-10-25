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

check_parses_to = (filename, t) ->

    if t.pending then
        pending "#{t.pending}\n#{output}"
        return

    local lines, outcome

    errors = if #t > 0 then #t else nil

    lazy_setup ->
        output = lexfile filename
        lines = {}
        for s in output\gmatch "[^\r\n]+" do
            table.insert lines, s

        outcome = lines[#lines]
        table.remove lines, #lines

        for i in ipairs t do
            t[i] = "semantic error: " .. t[i]

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
    ["f.array.access.c"]: to: {
        "Expected INT got CHAR"
    }
    ["f.assign.c"]: to: {
        "lvalue cannot be `i+2` (must be a variable, field access, array access or pointer dereference)"
        "lvalue cannot be `4` (must be a variable, field access, array access or pointer dereference)"
        "lvalue cannot be `sizeof(INT)` (must be a variable, field access, array access or pointer dereference)"
        "lvalue cannot be `main()` (must be a variable, field access, array access or pointer dereference)"
        "lvalue cannot be VOID"
        "Type mismatch in assignment (VOID != INT)"}
    ["f.builtins.c"]: to: {
        "Symbol print_s already exists!", "Symbol print_i already exists!", "Symbol print_c already exists!"
        "Symbol read_c already exists!", "Symbol read_i already exists!", "Symbol mcmalloc already exists!"
        "Function mcmalloc returns VOID when it should be returning *VOID" -- unavoidable
    }
    ["f.charray.c"]: to: {"lvalue cannot be CHAR[6]"}
    ["f.return.mismatch.c"]: to: {
        "Block returns differing types ([VOID, INT])"
        "Function main returns VOID when it should be returning INT"
        "Block returns differing types ([INT, VOID])"
        "Block returns differing types ([INT, VOID])"
        "Function mine returns INT when it should be returning VOID"}
    ["f.glob_decl.func.c"]: to: {"Symbol main already exists!"}
    ["f.glob_decl.mixed.c"]: to: {
        "Symbol MyStruct already exists!", "Symbol MyStruct already exists!"}
    ["f.glob_decl.struct.c"]: to: {"Symbol MyStruct already exists!"}
    ["f.improper.return.c"]: to: {"Function main returns INT when it should be returning VOID"}
    ["f.missing.symbol.c"]: to: {"Symbol true does not exist!", "Expression should be of type INT, currently of type VOID"}
    ["f.repeat_vardecl.c"]: to: {
        "Symbol a already exists!", "Symbol a already exists!", "Symbol c already exists!"}
    ["f.return.auto.c"]: to: {
        "Function main returns VOID when it should be returning INT"}
    ["f.strong.typed.c"]: to: {"Type mismatch in assignment (INT != CHAR)"}
    ["f.struct.access.c"]: to: {"Field c does not exist in struct", "Function main returns VOID when it should be returning INT"}
    ["f.struct.var.redef.c"]: to: {"Symbol bar already exists!"}
    ["f.undeclared_parameter.c"]: to: {
        "Symbol d already exists!"
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], expected 5 arguments, got 3"
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], expected 5 arguments, got 4"
        "Invalid cast from INT to CHAR"
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], param `*VOID c` was incorrectly given type *INT"
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], param `*INT d` was incorrectly given type *CHAR"
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], param `*CHAR d` was incorrectly given type CHAR[5]",
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], expected 5 arguments, got 0",
        "Could not call test_params[INT a, CHAR b, *VOID c, *INT d, *CHAR d], expected 5 arguments, got 1"}
    ["f.unk.c"]: to: {
        "Symbol a does not exist!"
        "Symbol a does not exist!"
        "Symbol a does not exist!"
        "Symbol a does not exist!"
        "Symbol a does not exist!"
        "Symbol a does not exist!"
        "Expected ArrayType or PointerType, got VOID"
        "Expression is not a struct"
        "lvalue cannot be VOID"
        "Type mismatch in assignment (VOID != INT)"
        "Expression is not a struct"
        "Type mismatch in assignment (INT != VOID)"
        "Could not call a[], expected 0 arguments, got 1"
        "Expression is not a struct"
        "Could not call test[INT example], param `INT example` was incorrectly given type VOID"
        "Expected ArrayType or PointerType, got VOID"
        "Could not call test[INT example], param `INT example` was incorrectly given type VOID"}
    ["f.vardecl.void.c"]: to: {"Cannot declare variable a as VOID"}

    ["p.array.access.c"]: to: {}
    ["p.assign.c"]: to: {}
    ["p.builtins.c"]: to: {}
    ["p.cast.c"]: to: {}
    ["p.fibonacci.c"]: to: {}
    ["p.return_void.c"]: to: {}
    ["p.struct_and_decl.c"]: to: {}
    ["p.struct_and_vardecl.c"]: to: {}
    ["p.struct.access.c"]: to: {}
    ["p.struct.self.c"]: to: {}


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
                            check_parses_to filename, testData.to
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
