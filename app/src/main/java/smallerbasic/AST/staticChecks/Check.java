package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public interface Check {
    /**
     * An interface for static checks.
     * These checks take a tree and return true if the check succeeds, false otherwise.
     * Check may also convey additional information about why they failed using the reportError function.
     */
    boolean check(@NotNull ASTNode n);
    default void reportError(@NotNull String msg) {
        System.out.println(msg);
    }

}
