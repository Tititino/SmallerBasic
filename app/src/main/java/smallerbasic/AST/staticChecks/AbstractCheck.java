package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractCheck implements Check {
    protected @NotNull ErrorReporter reporter = ErrorReporter.STDERR_REPORTER;
    @Override
    public void setErrorReporter(@NotNull ErrorReporter e) {
        reporter = e;
    }
}
