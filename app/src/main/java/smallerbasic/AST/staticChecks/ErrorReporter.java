package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public interface ErrorReporter {
    ErrorReporter STDERR_REPORTER = (n, msg) -> System.err.println(msg);

    void reportError(@NotNull ASTNode n, @NotNull String msg);
}
