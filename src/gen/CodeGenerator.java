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

        // Create the writer
        IndentWriter indentWriter = new IndentWriter(writer);
        V.writer = indentWriter;

        // Create registers
        Registers registers = new Registers();
        V.registers = registers;

        // Visit the data visitor
        program.accept(new DataVisitor(indentWriter));

        // Create text visitor and other auxiliary visitors
        V.text = new TextVisitor();
        V.inbuilt = new InbuiltVisitor();
        V.binOp = new BinOpVisitor();

        // Visit the text visitor
        program.accept(V.text);

        writer.close();
    }
}
