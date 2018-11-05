package gen;

// Horrible V "singleton"
class V {
    // General stuff
    static IndentWriter writer;
    static Registers registers;

    // Actual visitors
    static TextVisitor text;
    static InbuiltVisitor inbuilt;
    static BinOpVisitor binOp;
    static AssignVisitor assign;
}
