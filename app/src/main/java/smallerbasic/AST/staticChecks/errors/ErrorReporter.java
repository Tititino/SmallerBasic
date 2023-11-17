package smallerbasic.AST.staticChecks.errors;

import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.Check;

/**
 * Interface for a class used to report additional information about errors encountered during a {@link Check}.
 */
public interface ErrorReporter {
    /**
     * Default {@link ErrorReporter}, prints the message to {@code stderr}.
     */
    ErrorReporter STDERR_REPORTER = (n, msg) -> System.err.println(msg);

    /**
     * Report an error.
     * @param n The faulty node.
     * @param msg A description of the error.
     */
    void reportError(ASTNode n, String msg);
}
