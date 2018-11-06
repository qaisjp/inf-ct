package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CodeGenerator {

    public CodeGenerator() {
    }

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(outputFile);

        // Create some utility items
        V.writer = new IndentWriter(writer);
        V.registers = new Registers();

        // Visit the data visitor
        program.accept(new DataVisitor(V.writer));

        // Create text visitor and other auxiliary visitors
        V.text = new TextVisitor();
        V.inbuilt = new InbuiltVisitor();
        V.binOp = new BinOpVisitor();
        V.assign = new AssignVisitor();
        V.function = new FunctionVisitor();

        // Visit the text visitor
        program.accept(V.text);

        writer.close();
    }
}
