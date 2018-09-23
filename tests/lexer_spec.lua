local PROJ = os.getenv("PROJ")
if PROJ == "" or not PROJ then
    error("PROJ environment variable is not set")
    return
end

function getfile(filename)
    local f = assert(io.open(filename, "r"))
    local t = f:read("*all")
    f:close()
    return t
end

function lexfile(filename)
    local f = assert(io.popen("java -cp $PROJ/bin Main -lexer $PROJ/tests/lexer/" .. filename .. " out", "r"))
    local t = f:read("*all")
    f:close()
    return t
end

function check_lexes_to(filename, t, errors)
    describe(filename, function()
        local output = lexfile(filename)
        local lines = {}
        for s in output:gmatch("[^\r\n]+") do
            table.insert(lines, s)
        end

        local outcome = lines[#lines]
        table.remove(lines, #lines)

        it("should match", function()
            assert.are.same(t, lines)
        end)

        if errors then
            it("should fail", function()
                assert.equal(string.format("Lexing: failed (%d errors)", errors), outcome)
            end)
        else
            it("should pass", function()
                assert.equal("Lexing: pass", outcome)
            end)
        end
    end)
end

describe("lexer", function()
    describe("types", function()
        check_lexes_to("p.types.c",
            {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "INT",
            "IDENTIFIER", "SC", "CHAR", "IDENTIFIER", "SC", "RBRA"}
        )
    end)

    check_lexes_to("trailingnewline/f.nonewline.c",
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0",
        "INVALID"}, 1
    )
    check_lexes_to("trailingnewline/f.endnewline.c",
        {"VOID", "IDENTIFIER", "LPAR", "RPAR", "LBRA", "RBRA",
        "Lexing error: unrecognised character (#) at 2:0",
        "INVALID"}, 1
    )
end)
