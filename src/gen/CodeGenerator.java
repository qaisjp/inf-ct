package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CodeGenerator {

    private PrintWriter writer; // use this writer to output the assembly instructions

    public CodeGenerator() {
    }

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        IndentWriter indentWriter = new IndentWriter(writer);
        Registers registers = new Registers();

        // Visit the data visitor
        program.accept(new DataVisitor(indentWriter));

        // Visit the text visitor
        TextVisitor textVisitor = new TextVisitor(indentWriter, registers);
        program.accept(textVisitor);

        writer.close();
    }
}
