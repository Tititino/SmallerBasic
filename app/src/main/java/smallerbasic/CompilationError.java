package smallerbasic;

/**
 * Special exception to signal an error during parsing or compilation.
 */
public class CompilationError extends RuntimeException {
    public CompilationError(String message) {
        super(message);
    }
}
