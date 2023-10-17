package smallerbasic.AST.staticChecks;

import org.jetbrains.annotations.NotNull;
import smallerbasic.AST.nodes.ASTNode;

public interface Check {
    boolean check(@NotNull ASTNode n);
}
