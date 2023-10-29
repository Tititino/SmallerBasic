package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public abstract class AbstractCheck implements Check {
    private @NotNull ErrorReporter reporter = ErrorReporter.STDERR_REPORTER;
    @Override
    public void reportError(@NotNull ASTNode n, @NotNull String msg) {
        reporter.reportError(n, msg);
    }
    @Override
    public void setErrorReporter(@NotNull ErrorReporter e) {
        reporter = e;
    }
}
