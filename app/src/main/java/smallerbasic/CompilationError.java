package smallerbasic;

/**
 * Exception class to be thrown during compilation to halt the process in case of syntax errors, or failed checks.
 */
public class CompilationError extends RuntimeException {
    public CompilationError(String message) {
        super(message);
    }
}
