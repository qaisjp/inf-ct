package gen;

// Horrible V "singleton"
public class V {
    // General stuff
    public static IndentWriter writer;
    public static Registers registers;

    // Actual visitors
    public static TextVisitor text;
    public static InbuiltVisitor inbuilt;
    public static BinOpVisitor binOp;
    public static AssignVisitor assign;
}
