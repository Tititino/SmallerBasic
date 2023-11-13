package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

/**
 * An interface for static checks.
 * These checks take a {@link ASTNode} and return {@code true} if the check succeeds, {@code false} otherwise.
 * A check may also convey additional information about why it failed by using the {@code reportError} function.
 */
public interface Check {

    boolean check(@NotNull ASTNode n);

    void setErrorReporter(@NotNull ErrorReporter e);

}
