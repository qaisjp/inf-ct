package sem;

import ast.BaseType;
import ast.FunDecl;
import ast.PointerType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SemanticAnalyzer {
	
	public int analyze(ast.Program prog) {
		/*
			void print_s(char* s);
			void print_i(int i);
			void print_c(char c);
			char read_c();
			int read_i();
			void* mcmalloc(int size);
		*/
		List<FunDecl> funDecls = new LinkedList<>(Arrays.asList(
				new FunDecl(BaseType.VOID, "print_s", new PointerType(BaseType.CHAR)),
				new FunDecl(BaseType.VOID, "print_i", BaseType.INT),
				new FunDecl(BaseType.VOID, "print_c", BaseType.CHAR),
				new FunDecl(BaseType.VOID, "print_address", new PointerType(BaseType.VOID)), // todo
				new FunDecl(BaseType.CHAR, "read_c", null),
				new FunDecl(BaseType.INT, "read_i", null),
				new FunDecl(new PointerType(BaseType.VOID), "mcmalloc", BaseType.INT)
		));

		// Append real function declaration to our inbuilts list
		funDecls.addAll(prog.funDecls);

		// And set our new declarations as it should be
		prog.funDecls = funDecls;

		// List of visitors
		ArrayList<SemanticVisitor> visitors = new ArrayList<SemanticVisitor>() {{
			add(new NameAnalysisVisitor());
			add(new TypeCheckVisitor());
		}};
		// Error accumulator
		int errors = 0;
		
		// Apply each visitor to the AST
		for (SemanticVisitor v : visitors) {
			prog.accept(v);
			errors += v.getErrorCount();
		}
		
		// Return the number of errors.
		return errors;
	}
}
