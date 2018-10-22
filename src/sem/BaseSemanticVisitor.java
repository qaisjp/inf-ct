package sem;


import ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author dhil
 * A base class providing basic error accumulation.
 */
public abstract class BaseSemanticVisitor<T> implements SemanticVisitor<T> {
	private int errors;
	
	
	public BaseSemanticVisitor() {
		errors = 0;
	}
	
	public int getErrorCount() {
		return errors;
	}
	
	protected void error(String format, Object... args) {
		System.err.printf("semantic error: " + format, args);
		errors++;
	}

	public List<T> visitEach(List<? extends ASTNode> list) {
		List<T> results = new ArrayList<>();
		for (ASTNode l : list) {
			results.add(l.accept(this));
		}

		return results;
	}

	public boolean eq(Type a, Type b) {
		if (a instanceof BaseType && b instanceof BaseType) {
			return a == b;
		}

		if (a instanceof PointerType && b instanceof PointerType) {
			return eq(((PointerType) a).innerType, ((PointerType) b).innerType);
		}

		if (a instanceof StructType && b instanceof StructType) {
			// Make name not equal then fail
			if (!((StructType) a).str.equals(((StructType) b).str)) {
				return false;
			}

			// In our version of C we can only do struct declarations at the top of the file.
			// The name analysis visitor will prevent two struct declarations from being declared in the same name here
			return true;
		}

		return a == b;
	}
}
