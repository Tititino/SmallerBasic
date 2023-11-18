package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.staticChecks.errors.ErrorReporter;

/**
 * Default implementation of the error reporting for a {@link Check}.
 * This uses the null error reporter {@link ErrorReporter#STDERR_REPORTER}.
 */
public abstract class AbstractCheck implements Check {
    protected @NotNull ErrorReporter reporter = ErrorReporter.STDERR_REPORTER;
    @Override
    public void setErrorReporter(@NotNull ErrorReporter e) {
        reporter = e;
    }
}
