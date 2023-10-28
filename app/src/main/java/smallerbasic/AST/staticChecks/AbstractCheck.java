package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractCheck implements Check {

    private @NotNull ErrorReporter reporter = ErrorReporter.STDERR_REPORTER;

    @Override
    public void reportError(@NotNull String msg) {
        reporter.reportError(msg);
    }

    @Override
    public void setErrorReporter(@NotNull ErrorReporter e) {
        reporter = e;
    }
}
