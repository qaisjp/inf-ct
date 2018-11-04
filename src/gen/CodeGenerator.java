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

        // List of visitors
        ArrayList<ASTVisitor> visitors = new ArrayList<ASTVisitor>() {{
            add(new DataVisitor(indentWriter));
            add(new InbuiltVisitor(indentWriter, registers));
        }};

        // Apply each visitor to the AST
        for (ASTVisitor v : visitors) {
            program.accept(v);
        }

        writer.close();
    }
}
