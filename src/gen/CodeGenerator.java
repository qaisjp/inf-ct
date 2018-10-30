package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeGenerator {

    private PrintWriter writer; // use this writer to output the assembly instructions

    public CodeGenerator() {
    }

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        DataVisitor dataVisitor = new DataVisitor(writer);
        TextVisitor textVisitor = new TextVisitor(writer);

        program.accept(dataVisitor);
        program.accept(textVisitor);

        writer.close();
    }
}
