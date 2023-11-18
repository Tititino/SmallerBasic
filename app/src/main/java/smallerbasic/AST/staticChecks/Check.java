package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;
import smallerbasic.AST.staticChecks.errors.ErrorReporter;

/**
 * An interface for static checks.
 * These checks take an {@link ASTNode} and return {@code true} if the check succeeds, {@code false} otherwise.
 * A check may also convey additional information about why it failed by using the {@code reportError} function.
 */
public interface Check {

    /**
     * Checks that a certain property holds on an AST.
     * During the check the single errors may be reported through the {@link ErrorReporter}.
     * @param n The {@link ASTNode} under scrutiny.
     * @return {@code true} if the checks succeeds, {@code false} otherwise.
     */
    boolean check(@NotNull ASTNode n);

    /**
     * Set the {@link ErrorReporter} used by this check.
     * {@code this} will call {@link ErrorReporter#reportError} for each error it encounters in the AST.
     * @param e the new {@link ErrorReporter}.
     */
    void setErrorReporter(@NotNull ErrorReporter e);

}
