package smallerbasic.AST.staticChecks;

import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public interface Check {
    boolean check(@NotNull ASTNode n);
    default void reportError(@NotNull String msg, @NotNull Token where) {
        return;
    }

}
