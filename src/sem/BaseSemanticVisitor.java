package sem;


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
}
